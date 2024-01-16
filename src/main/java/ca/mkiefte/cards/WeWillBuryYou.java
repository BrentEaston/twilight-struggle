package ca.mkiefte.cards;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.TSTurnTracker;
import ca.mkiefte.Utilities;

public final class WeWillBuryYou extends CardEvent {
	public static final String ID = "wewillburyyou;";
	public static final String DESCRIPTION = "\"We Will Bury You\"*";

	public WeWillBuryYou() {
		this(ID, null);
	}

	public WeWillBuryYou(final String type, final GamePiece inner) {
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
		comm = comm.append(Utilities.adjustDefcon(-1));
		if (!TSTurnTracker.isGameOver()) {
			final Command chat = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), "USSR gains 3 VP if UN Intervention is not played as an event in the next US player round.");
			chat.execute();
			comm = comm.append(chat);
			final ChangeTracker tracker = new ChangeTracker(this);
			eventInEffect = true;
			comm = comm.append(tracker.getChangeCommand());
		}
		return comm;
	}

	public Command awardVps(final boolean award, final String who) {
		if (!isEventInEffect() 
				|| !TSPlayerRoster.US.equals(who) 
				|| TSTurnTracker.getCurrentRound() == 0
				|| !TSTurnTracker.getPhasingPlayer().equals(TSPlayerRoster.US))
			return null;
		final Chatter chatter = GameModule.getGameModule().getChatter();
		final ChangeTracker tracker = new ChangeTracker(this);
		eventInEffect = false;
		Command comm = tracker.getChangeCommand();
		Command chat;
		if (award) {
			chat = new Chatter.DisplayText(chatter, "US did not play UN Intervention as Event.");
			chat.execute();
			comm = comm.append(chat);
			comm = comm.append(Utilities.adjustVps(-3));
		} else {
			final StringBuilder builder = new StringBuilder(getDescription()).append(" is cancelled.");
			chat = new Chatter.DisplayText(chatter, builder.toString());
			chat.execute();
			comm = comm.append(chat);
		}
		return comm;
	}
}
