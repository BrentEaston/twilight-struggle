package ca.mkiefte.cards;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class Allende extends CardEvent {
	public static final String ID = "allende;";
	public static final String DESCRIPTION = "Allende*";

	public Allende() {
		this(ID, null);
	}

	public Allende(final String type, final GamePiece inner) {
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
		comm = comm.append(Influence.getInfluenceMarker(Influence.CHILE, TSPlayerRoster.USSR).adjustInfluence(+2));
		return comm;
	}
}
