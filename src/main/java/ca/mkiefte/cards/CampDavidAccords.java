package ca.mkiefte.cards;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.Utilities;

public final class CampDavidAccords extends CardEvent {
	private static final String[] COUNTRIES = new String[] {
		Influence.ISRAEL, 
		Influence.JORDAN, 
		Influence.EGYPT};
	public static final String ID = "campdavidaccords;";
	public static final String DESCRIPTION = "Camp David Accords*";

	public CampDavidAccords() {
		this(ID, null);
	}

	public CampDavidAccords(final String type, final GamePiece inner) {
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
		comm = comm.append(Utilities.adjustVps(+1));
		for (String name : COUNTRIES)
			comm = comm.append(Influence.getInfluenceMarker(name, TSPlayerRoster.US).adjustInfluence(+1));
		final Command chat = new Chatter.DisplayText(GameModule.getGameModule().getChatter(),
				new StringBuilder(getCard(ArabIsraeliWar.class).getDescription()).append(" no longer playable.").toString());
		chat.execute();
		comm = comm.append(chat);
		return comm;
	}

	@Override
	protected boolean isUnderlined() {
		return true;
	}
}
