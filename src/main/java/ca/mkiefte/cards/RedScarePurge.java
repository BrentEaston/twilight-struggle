package ca.mkiefte.cards;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import VASSAL.tools.SequenceEncoder;
import ca.mkiefte.China;
import ca.mkiefte.TSPlayerRoster;

public final class RedScarePurge extends CardEvent {
	public static final String ID = "redscarepurge;";
	public static final String DESCRIPTION = "Red Scare/Purge";
	private static int americanPenalty = 0;
	private static int sovietPenalty = 0;

	public RedScarePurge() {
		this(ID, null);
	}

	public RedScarePurge(final String type, final GamePiece inner) {
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
	public boolean isEventPlayable(final String who) {
		return super.isEventPlayable(who) 
				&& (TSPlayerRoster.US.equals(who) || !China.isChineseCivilWarInEffect());
	}

	@Override
	public Command myPlayEvent(final String who) {
		final boolean newTurn = !isEventPlayedThisTurn();
		Command comm = super.myPlayEvent(who);
		final String opponent = TSPlayerRoster.US.equals(who) ? TSPlayerRoster.USSR : TSPlayerRoster.US;
		final Chatter chatter = GameModule.getGameModule().getChatter();
		final ChangeTracker change = new ChangeTracker(this);
		if (newTurn) {
			americanPenalty = 0;
			sovietPenalty = 0;
		}
		final int penalty;
		if (TSPlayerRoster.USSR.equals(opponent)) {
			--sovietPenalty;
			penalty = sovietPenalty;
		} else {
			--americanPenalty;
			penalty = americanPenalty;
		}
		comm = comm.append(change.getChangeCommand());
		final StringBuilder builder = new StringBuilder("All cards played by ").append(opponent).append(" this turn are ").append(penalty).append(" Ops. (min 1 Op)");
		final Command chat = new Chatter.DisplayText(chatter, builder.toString());
		chat.execute();
		comm = comm.append(chat);
		return comm;
	}

	@Override
	public String myGetState() {
		final SequenceEncoder se = new SequenceEncoder(super.myGetState(), '+');
		se.append(sovietPenalty);
		se.append(americanPenalty);
		return se.getValue();
	}

	@Override
	public void mySetState(String newState) {
		final SequenceEncoder.Decoder se = new SequenceEncoder.Decoder(newState, '+');
		super.mySetState(se.nextToken());
		sovietPenalty = se.nextInt(0);
		americanPenalty = se.nextInt(0);
	}

	public int getPenalty(final String who) {
		if (isEventInEffect())
			return TSPlayerRoster.US.equals(who) ? americanPenalty : sovietPenalty;
		else
			return 0;
	}

	@Override
	public boolean isEventInEffect() {
		return isEventPlayedThisTurn();
	}
}
