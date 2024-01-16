package ca.mkiefte.cards;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.properties.MutableProperty;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.command.NullCommand;
import VASSAL.counters.GamePiece;
import VASSAL.tools.SequenceEncoder;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.TSTurnTracker;
import ca.mkiefte.Utilities;

public final class Norad extends CardEvent {
	private static final int INFLUENCE = 1;
	public static final String LAST_DEFCON_PROP_NAME = "LastDefcon";
	public static final String ID = "norad;";
	public static final String DESCRIPTION = "NORAD*";
	private boolean thisRound;
	private int lastCard;

	public Norad() {
		this(ID, null);
	}

	public Norad(final String type, final GamePiece inner) {
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
	public Command myPlayEvent(final String who) {
		Command comm = super.myPlayEvent(who);
		final Chatter chatter = GameModule.getGameModule().getChatter();
		final Command chat = new Chatter.DisplayText(chatter, "US player may add 1 Influence to any country already containing US Influence if DEFCON reduced to 2 and Canada is US controlled.");
		chat.execute();
		comm = comm.append(chat);
		return comm;
	}

	@Override
	public String myGetState() {
		final SequenceEncoder encoder = new SequenceEncoder(super.myGetState(), '+');
		encoder.append(thisRound);
		encoder.append(lastCard);
		return encoder.getValue();
	}

	@Override
	public void mySetState(final String newState) {
		final SequenceEncoder.Decoder decoder = new SequenceEncoder.Decoder(newState, '+');
		super.mySetState(decoder.nextToken());
		thisRound = decoder.nextBoolean(false);
		lastCard = decoder.nextInt(0);
	}
	
	public boolean bonusThisRound() {
		return thisRound;
	}
	
	public Command setBonusThisRound(final boolean set) {
		if (set == thisRound)
			return null;
		final ChangeTracker tracker = new ChangeTracker(this);
		thisRound = set;
		return tracker.getChangeCommand();
	}

	public Command setup() {
		Command comm = Influence.setAllStartingInfluence();
		MutableProperty property = Utilities.getGlobalProperty(Influence.OPS_REMAINING);
		comm = comm.append(property.setPropertyValue(Integer.toString(INFLUENCE)));
		
		// this is so that we'll get called back if the game is saved.
		property = Utilities.getGlobalProperty(AMERICAN_CARD_PLAYED);
		final String propertyValue = property.getPropertyValue();
		final ChangeTracker tracker = new ChangeTracker(this);
		if (propertyValue.equals(Utilities.FALSE))
			lastCard = -1;
		else
			lastCard = Integer.valueOf(propertyValue);
		comm = comm.append(tracker.getChangeCommand());
		comm = comm.append(property.setPropertyValue(Integer.toString(getCardNumber())));
		return comm;
	}
	
	@Override
	public Command updateState() {
		Influence.getInfluenceDialog(getDescription(), 
				"Add 1 Influence to any country already containing\n"
						+ "US Influence.",				
						new Influence.Delegate(null) {
			@Override
			public boolean canIncreaseInfluence(final Influence marker) {
				return super.canIncreaseInfluence(marker)
						&& TSPlayerRoster.US.equals(marker.getSide())
						&& marker.getInfluence() > 0;
			}

			@Override
			public Command finish() {
				Command comm = super.finish();
				comm = comm.append(setBonusThisRound(false));
				comm = comm.append(TSTurnTracker.myDoNext(true));
				return comm;
			}
		});
		return super.updateState();
	}
	
	@Override
	protected boolean isUnderlined() {
		return true;
	}

	@Override
	public boolean isEventFinished() {
		return lastCard == 0 || !Influence.isOpsRemaining();
	}

	@Override
	public Command discard() {
		if (lastCard == 0)
			return super.discard();
		Command comm = new NullCommand();
		if (lastCard > 0)
			comm = comm.append(getCard(lastCard).discard());
		final ChangeTracker tracker = new ChangeTracker(this);
		lastCard = 0;
		comm = comm.append(tracker.getChangeCommand());
		return comm;
	}
}
