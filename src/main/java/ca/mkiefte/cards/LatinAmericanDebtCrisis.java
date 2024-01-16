package ca.mkiefte.cards;


import java.util.Set;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.command.NullCommand;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceFilter;
import VASSAL.tools.SequenceEncoder;
import ca.mkiefte.CountingPlayerHand;
import ca.mkiefte.Influence;
import ca.mkiefte.Influence.Delegate;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.Utilities;

public final class LatinAmericanDebtCrisis extends ChangeInfluence {
	
	private static final int N_COUNTRIES = 2;
	public final static String ID = "latinamericandebtcrisis;";
	public final static String DESCRIPTION = "Latin American Debt Crisis";
	private boolean doubleUssrInfluence;
	
	
	public LatinAmericanDebtCrisis() {
		this(ID, null);
	}

	public LatinAmericanDebtCrisis(final String type, final GamePiece inner) {
		super(type, inner);
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	protected String getIdName() {
		return ID;
	}

	@Override
	protected boolean eventFinishedOnExit() {
		return false;
	}

	@Override
	public String activeSide() {
		return doubleUssrInfluence ? TSPlayerRoster.USSR : TSPlayerRoster.US;
	}

	@Override
	public Command updateState() {
		if (!doubleUssrInfluence) {
			Command comm = new NullCommand();
			final int nCards = CountingPlayerHand.getHand(TSPlayerRoster.US).countCards();
			if (nCards > 0) {
				comm = comm.append(selectDiscardCard(TSPlayerRoster.US, getName(), 
						"Discard a 3 or more Ops card or 'Cancel' to allow USSR\n"
								+ "to double Influence in 2 countries in South America.", 
								new PieceFilter(){
					public boolean accept(final GamePiece gp) {
						final CardEvent card = getCard(gp);
						return card.getOps() >= 3;
					}}, true));
			}
			if (CountingPlayerHand.getHand(TSPlayerRoster.US).countCards() == nCards) {
				final ChangeTracker tracker = new ChangeTracker(this);
				doubleUssrInfluence = true;
				comm = comm.append(tracker.getChangeCommand());
				comm = comm.append(setup());
			} else 
				comm = comm.append(setFinished(true));
			return comm;
		} else
			return super.updateState();
	}

	@Override
	public Command myPlayEvent(final String who) {
		Command comm = super.myPlayEvent(who);
		final int nCards = CountingPlayerHand.getHand(TSPlayerRoster.US).countCards();
		if (nCards == 0) {
			final Chatter chatter = GameModule.getGameModule().getChatter();
			final Command chat = new Chatter.DisplayText(chatter, "US has no cards.");
			chat.execute();
			comm = comm.append(chat);
			comm = comm.append(updateState());
		}
		return comm;
	}

	@Override
	public String myGetState() {
		final SequenceEncoder encoder = new SequenceEncoder(super.myGetState(), '#');
		encoder.append(doubleUssrInfluence);
		return encoder.getValue();
	}

	@Override
	public void mySetState(final String newState) {
		final SequenceEncoder.Decoder decoder = new SequenceEncoder.Decoder(newState, '#');
		super.mySetState(decoder.nextToken());
		doubleUssrInfluence = decoder.nextBoolean(false);
	}

	@Override
	protected void myClearState() {
		super.myClearState();
		doubleUssrInfluence = false;
	}

	@Override
	protected int nInfluence() {
		return doubleUssrInfluence ? N_COUNTRIES : 0;
	}

	@Override
	protected Command autoPlace() {
		if (!doubleUssrInfluence)
			return null;
		final Set<GamePiece> pieces = Utilities.findAllPiecesMatching(new PieceFilter() {		
			public boolean accept(final GamePiece piece) {
				final Influence marker = Influence.getInfluenceMarker(piece);
				return marker != null && passesFilter(marker) && marker.getInfluence() > 0;
			}
		});
		if (pieces.size() == 0) {
			Command chat = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), "USSR has no Influence in South America.");
			chat.execute();
			return chat;
		} else if (pieces.size() <= N_COUNTRIES) {
			Command comm = new NullCommand();
			for (final GamePiece gp : pieces) {
				final Influence marker = Influence.getInfluenceMarker(gp);
				comm = comm.append(marker.adjustInfluence(marker.getInfluence()));
			}
			return comm;
		} else
			return null;
	}

	@Override
	protected String getMessage() {
		return "Double Influence in 2 countries in South America.";
	}

	@Override
	protected Delegate getDelegate() {
		return new Delegate(this) {
			@Override
			public boolean canIncreaseInfluence(final Influence marker) {
				return super.canIncreaseInfluence(marker)
						&& passesFilter(marker)
						&& marker.getInfluence() > 0;
			}

			@Override
			public Command incrementInfluence(final Influence marker, int amount) {
				if (amount > 0)
					amount = marker.getInfluence();
				else
					amount = -marker.getInfluence()/2;
				return super.incrementInfluence(marker, amount);
			}

			@Override
			protected Command changeRemainingInfluence(final Influence marker,
					int amount) {
				if (amount > 0)
					amount = 1;
				else
					amount = -1;
				return super.changeRemainingInfluence(marker, amount);
			}
		};
	}

	@Override
	protected boolean passesFilter(final Influence marker) {
		return TSPlayerRoster.USSR.equals(marker.getSide()) && Influence.SOUTH_AMERICA.equals(marker.getRegion());
	}

	@Override
	public boolean canUndoEvent() {
		return super.canUndoEvent();
	}
}
