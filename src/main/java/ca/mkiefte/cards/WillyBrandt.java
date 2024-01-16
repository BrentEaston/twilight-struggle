package ca.mkiefte.cards;

import static ca.mkiefte.Utilities.adjustVps;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;

public final class WillyBrandt extends CardEvent {
	public static final String ID = "willybrandt;";
	public static final String DESCRIPTION = "Willy Brandt*";

	public WillyBrandt() {
		this(ID, null);
	}

	public WillyBrandt(String type, GamePiece inner) {
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
		comm = comm.append(Influence.getInfluenceMarker(Influence.W_GERMANY, TSPlayerRoster.USSR).addInfluence(+1));
		final Command chat = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), "NATO canceled for W. Germany.");
		chat.execute();
		comm = comm.append(chat);
		return comm;
	}

	@Override
	public boolean isEventPlayable(final String who) {
		return super.isEventPlayable(who) && !getCard(TearDownThisWall.class).isEventPlayed();
	}

	@Override
	protected boolean isUnderlined() {
		return true;
	}
}
