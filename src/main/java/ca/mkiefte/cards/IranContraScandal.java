package ca.mkiefte.cards;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class IranContraScandal extends CardEvent {
	public static final String ID = "irancontrascandal;";
	public static final String DESCRIPTION = "Iran Contra Scandal*";

	public IranContraScandal() {
		this(ID, null);
	}

	public IranContraScandal(final String type, final GamePiece inner) {
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
	protected Command myPlayEvent(final String who) {
		Command comm = super.myPlayEvent(who);
		final Command chat = new Chatter.DisplayText(GameModule.getGameModule().getChatter(),
				"All US Realignment rolls are -1 for the rest of the Turn.");
		chat.execute();
		comm = comm.append(chat);
		return comm;
	}

	@Override
	public boolean isEventInEffect() {
		return isEventPlayedThisTurn();
	}
}
