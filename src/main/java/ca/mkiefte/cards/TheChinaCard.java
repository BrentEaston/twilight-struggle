package ca.mkiefte.cards;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.properties.MutableProperty;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceFilter;
import VASSAL.tools.SequenceEncoder;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.Utilities;

public final class TheChinaCard extends CardEvent {

	public final static String ID = "thechinacard;";
	public final static String DESCRIPTION = "The China Card";
	private boolean allOpsInAsia;
	
	private class ChinaCardDelegate extends Influence.ConductOperationsDelegate {

		public ChinaCardDelegate() {
			super(null);
		}
		
		@Override
		public Command incrementInfluence(final Influence inf, final int amount) {
			Command comm = super.incrementInfluence(inf, amount);
			if (amount > 0 && !appliesTo(inf))
				comm = comm.append(setAllOpsInAsia(false));
			else if (amount < 0 && !appliesTo(inf)) {			
				final GamePiece piece = Utilities.findPiece(new PieceFilter() {						
					public boolean accept(final GamePiece piece) {
						final Influence inf = Influence.getInfluenceMarker(piece);
						return inf != null 
								&& inf.getInfluence() > inf.getStartingInfluence()
								&& !appliesTo(inf);
					}
				});
				if (piece == null)
					comm = comm.append(setAllOpsInAsia(true));
			}
			return comm;
		}

		public boolean appliesTo(final Influence inf) {
			return Influence.ASIA.equals(inf.getRegion());
		}
		
		@Override
		protected int getInfluenceAvailableFor(final Influence marker) {
			int ops = super.getInfluenceAvailableFor(marker);
			if (isAllOpsInAsia() && appliesTo(marker))
				++ops;
			return ops;
		}

		@Override
		public Command finish() {
			Command comm = super.finish();
			comm = comm.append(setAllOpsInAsia(false));
			return comm;
		}

		@Override
		public Command realign(Influence marker) {
			Command comm = super.realign(marker);
			if (!appliesTo(marker))
				comm = comm.append(setAllOpsInAsia(false));
			return comm;
		}

		@Override
		public Command cancel() {
			Command comm = super.cancel();
			comm = comm.append(undoPlayOps(who));
			return comm;
		}

		@Override
		public boolean canCancel() {
			return canUndoPlayOps();
		}
	}
	
	public TheChinaCard() {
		this(ID, null);
	}
	
	public TheChinaCard(final String type, final GamePiece inner) {
		super(type, inner);
	}

	public String getDescription() {
		return DESCRIPTION;
	}

	public boolean isAllOpsInAsia() {
		return allOpsInAsia;
	}
	
	public Command setAllOpsInAsia(final boolean val) {
		if (val == allOpsInAsia)
			return null;
		final ChangeTracker tracker = new ChangeTracker(this);
		allOpsInAsia = val;
		Command comm = tracker.getChangeCommand();
		final MutableProperty globalProperty = Utilities.getGlobalProperty(Influence.CONDITIONAL_INFLUENCE);
		int ops = Integer.valueOf(globalProperty.getPropertyValue());
		ops += val ? 1 : -1;
		comm = comm.append(globalProperty.setPropertyValue(Integer.toString(ops)));
		return comm;
	}

	private Command cancelFormosanResolution() {
		final FormosanResolution card = getCard(FormosanResolution.class);
		if (TSPlayerRoster.US.equals(getOwner()) && card.isEventInEffect())
			return card.cancelEvent();
		else
			return null;
	}

	@Override
	protected Command playSpaceRace(final String who) {
		Command comm = super.playSpaceRace(who);
		if (comm == null)
			return comm;
		comm = comm.append(cancelFormosanResolution());
		return comm;
	}

	public Command setAvailable(boolean available) {
		final GamePiece outermost = getOutermost(this);
		if (available ^ outermost.getProperty(CardEvent.OBSCURED_BY) == null) {
			final ChangeTracker tracker = new ChangeTracker(this);
			outermost.setProperty(CardEvent.OBSCURED_BY, available ? null : getOwner());
			return tracker.getChangeCommand();
		} else
			return null;
	}

	@Override
	protected boolean canPlayHeadline() {
		return false;
	}

	@Override
	public boolean isEventPlayable(final String who) {
		return false;
	}

	// can't play it if it's face down
	@Override
	protected boolean canPlayCard() {
		return super.canPlayCard() && getOutermost(this).getProperty(CardEvent.OBSCURED_BY) == null;
	}

	@Override
	public Command discard() {
		Command comm = setAvailable(false);
		final String opponent = TSPlayerRoster.US.equals(getOwner()) ? TSPlayerRoster.USSR : TSPlayerRoster.US;
		comm = comm.append(sendCardToHand(opponent));
		return comm;
	}

	@Override
	protected String getIdName() {
		return ID;
	}

	@Override
	protected boolean canPlayEvent() {
		return false;
	}

	// trick the turn tracker into thinking this has an event that's been played so that
	// it will call updateState so we can insert the proper delegate.
	@Override
	public boolean isEventPlayed() {
		return isOpsPlayed();
	}

	@Override
	public boolean isEventFinished() {
		return !Influence.isOpsRemaining();
	}

	@Override
	public Command updateState() {
		Influence.getInfluenceDialog("Conduct Operations", new ChinaCardDelegate());
		return super.updateState();
	}

	@Override
	public Command playOps(final String who) {
		Command comm = super.playOps(who);
		comm = comm.append(cancelFormosanResolution());
		final Chatter chatter = GameModule.getGameModule().getChatter();
		Command chat = new Chatter.DisplayText(chatter, "+1 if all Ops played in Asia.");
		chat.execute();
		comm = comm.append(chat);
		comm = comm.append(setAllOpsInAsia(true));
		return comm;
	}

	@Override
	public String myGetState() {
		final SequenceEncoder encoder = new SequenceEncoder(super.myGetState(), '+');
		encoder.append(allOpsInAsia);
		return encoder.getValue();
	}

	@Override
	public void mySetState(final String newState) {
		final SequenceEncoder.Decoder decoder = new SequenceEncoder.Decoder(newState, '+');
		super.mySetState(decoder.nextToken());
		allOpsInAsia = decoder.nextBoolean(false);
	}

	@Override
	protected void myClearState() {
		super.myClearState();
		allOpsInAsia = false;
	}
}