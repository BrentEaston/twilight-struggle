package ca.mkiefte.cards;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class IranianHostageCrisis extends CardEvent {
	public static final String ID = "iranianhostagecrisis;";
	public static final String DESCRIPTION = "Iranian Hostage Crisis*";

	public IranianHostageCrisis() {
		this(ID, null);
	}

	public IranianHostageCrisis(final String type, final GamePiece inner) {
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
		Influence iran = Influence.getInfluenceMarker(Influence.IRAN, TSPlayerRoster.US);
		comm = comm.append(iran.removeAllInfluence());
		iran = iran.getOpponentInfluenceMarker();
		comm = comm.append(iran.adjustInfluence(+2));
		Command chat = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), "Effect of Terrorism is doubled against the US.");
		chat.execute();
		comm = comm.append(chat);
		return comm;
	}
}
