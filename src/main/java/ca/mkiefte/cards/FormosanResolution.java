package ca.mkiefte.cards;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.China;

public final class FormosanResolution extends CardEvent {
	public static final String ID = "formosanresolution;";
	public static final String DESCRIPTION = "Formosan Resolution*";

	public FormosanResolution() {
		this(ID, null);
	}

	public FormosanResolution(final String type, final GamePiece inner) {
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
		return super.isEventPlayable(who) && !China.isChineseCivilWarInEffect();
	}

	@Override
	public Command myPlayEvent(final String who) {
		Command comm = super.myPlayEvent(who);
		final Chatter chatter = GameModule.getGameModule().getChatter();
		Command chat = new Chatter.DisplayText(chatter, "Taiwan is a Battleground country for Asia Scoring.");
		chat = chat.append(new Chatter.DisplayText(chatter, "!!! Note: Region status reminder for Asia will not reflect this. !!!"));
		chat.execute();
		comm = comm.append(chat);
		return comm;
	}

	@Override
	protected boolean isUnderlined() {
		return true;
	}
}
