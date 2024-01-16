package ca.mkiefte.cards;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class AwacsSaleToSaudis extends CardEvent {
	public static final String ID = "awacssaletosaudis;";
	public static final String DESCRIPTION = "AWACS Sale to Saudis*";

	public AwacsSaleToSaudis() {
		this(ID, null);
	}

	public AwacsSaleToSaudis(final String type, final GamePiece inner) {
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
		comm = comm.append(Influence.getInfluenceMarker(Influence.SAUDI_ARABIA, TSPlayerRoster.US).adjustInfluence(+2));
		final Command chat = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), "Muslim Revolution is no longer playable.");
		chat.execute();
		comm = comm.append(chat);
		return comm;
	}
}
