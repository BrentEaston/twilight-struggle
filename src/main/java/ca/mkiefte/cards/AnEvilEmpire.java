package ca.mkiefte.cards;
import ca.mkiefte.Utilities;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class AnEvilEmpire extends CardEvent {
	public static final String ID = "anevilempire;";
	public static final String DESCRIPTION = "\"An Evil Empire\"*";

	public AnEvilEmpire() {
		this(ID, null);
	}

	public AnEvilEmpire(final String type, final GamePiece inner) {
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
	protected Command myPlayEvent(String who) {
		Command comm = super.myPlayEvent(who);
		comm = comm.append(getCard(FlowerPower.class).cancelEvent());
		comm = comm.append(Utilities.adjustVps(+1));
		return comm;
	}
}
