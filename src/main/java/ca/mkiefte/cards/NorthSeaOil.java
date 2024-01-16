package ca.mkiefte.cards;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class NorthSeaOil extends CardEvent {
	public static final String ID = "northseaoil;";
	public static final String DESCRIPTION = "North Sea Oil*";

	public NorthSeaOil() {
		this(ID, null);
	}

	public NorthSeaOil(final String type, final GamePiece inner) {
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
	protected boolean isUnderlined() {
		return true;
	}

	@Override
	protected Command myPlayEvent(final String who) {
		Command comm = super.myPlayEvent(who);
		final Chatter chatter = GameModule.getGameModule().getChatter();
		Command chat = new Chatter.DisplayText(chatter, "OPEC Event no longer playable.");
		chat = chat.append(new Chatter.DisplayText(chatter, "US may play 8 cards this turn."));
		chat.execute();
		comm = comm.append(chat);
		return comm;
	}
}
