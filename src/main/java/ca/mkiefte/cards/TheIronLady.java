package ca.mkiefte.cards;

import static ca.mkiefte.Utilities.adjustVps;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.TSTurnTracker;

public final class TheIronLady extends CardEvent {
	public static final String ID = "TheIronLady;";
	public static final String DESCRIPTION = "The Iron Lady*";

	public TheIronLady() {
		this(ID, null);
	}

	public TheIronLady(final String type, final GamePiece inner) {
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
		comm = comm.append(adjustVps(+1));
		if (TSTurnTracker.isGameOver())
			return comm;
		comm = comm.append(Influence.getInfluenceMarker(Influence.ARGENTINA, TSPlayerRoster.USSR).adjustInfluence(+1));
		comm = comm.append(Influence.getInfluenceMarker(Influence.U_K, TSPlayerRoster.USSR).removeAllInfluence());
		final Command chat = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), "Socialist Governments no longer playable.");
		chat.execute();
		comm = comm.append(chat);
		return comm;
	}

	@Override
	protected boolean isUnderlined() {
		return true;
	}
}
