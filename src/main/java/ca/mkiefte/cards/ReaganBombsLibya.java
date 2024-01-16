package ca.mkiefte.cards;

import static ca.mkiefte.Utilities.adjustVps;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;

public final class ReaganBombsLibya extends CardEvent {
	public static final String ID = "reaganbombslibya;";
	public static final String DESCRIPTION = "Reagan Bombs Libya*";

	public ReaganBombsLibya() {
		this(ID, null);
	}

	public ReaganBombsLibya(final String type, final GamePiece inner) {
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
		final int influence = Influence.getInfluenceMarker(Influence.LIBYA, TSPlayerRoster.USSR).getInfluence();
		if (influence >= 2)
			comm = comm.append(adjustVps(influence/2));
		else {				
			final Chatter chatter = GameModule.getGameModule().getChatter();
			Command chat = new Chatter.DisplayText(chatter, "USSR has less than 2 Influence in Libya.");
			chat.execute();
			comm = comm.append(chat);
		}
		return comm;
	}
}
