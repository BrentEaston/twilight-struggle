package ca.mkiefte.cards;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class YuriAndSamantha extends CardEvent {
	public static final String ID = "yuriandsamantha;";
	public static final String DESCRIPTION = "Yuri and Semantha*";

	public YuriAndSamantha() {
		this(ID, null);
	}

	public YuriAndSamantha(final String type, final GamePiece inner) {
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
	protected Command myPlayEvent(String who) {
		final Command comm = super.myPlayEvent(who);
		final Command chat = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), "USSR receives 1 VP for each US Coup attempt for the remainder of this Turn.");
		chat.execute();
		return comm.append(chat);
	}

	@Override
	public boolean isEventInEffect() {
		return isEventPlayedThisTurn();
	}
}
