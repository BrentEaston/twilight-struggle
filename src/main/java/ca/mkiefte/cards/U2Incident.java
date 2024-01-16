package ca.mkiefte.cards;

import static ca.mkiefte.Utilities.adjustVps;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;

public final class U2Incident extends CardEvent {
	public static final String ID = "u2incident;";
	public static final String DESCRIPTION = "U2 Incident*";

	public U2Incident() {
		this(ID, null);
	}

	public U2Incident(final String type, final GamePiece inner) {
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
		comm = comm.append(adjustVps(-1));
		final Chatter chatter = GameModule.getGameModule().getChatter();
		final Command chat = new Chatter.DisplayText(chatter, "USSR recieves additional VP If UN Intervention played later this turn.");
		chat.execute();
		comm = comm.append(chat);
		return comm;
	}

	@Override
	public boolean isEventInEffect() {
		return isEventPlayedThisTurn();
	}
}
