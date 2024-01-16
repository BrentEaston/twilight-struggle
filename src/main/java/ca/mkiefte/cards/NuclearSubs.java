package ca.mkiefte.cards;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class NuclearSubs extends CardEvent {
	public static final String ID = "nuclearsubs;";
	public static final String DESCRIPTION = "Nuclear Subs*";

	public NuclearSubs() {
		this(ID, null);
	}

	public NuclearSubs(final String type, final GamePiece inner) {
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
		final Chatter chatter = GameModule.getGameModule().getChatter();
		final Command chat = new Chatter.DisplayText(chatter, "US Coup Attempts do not affect DEFCON this Turn.");
		chat.execute();
		comm = comm.append(chat);
		return comm;
	}

	@Override
	public boolean isEventInEffect() {
		return isEventPlayedThisTurn();
	}
}
