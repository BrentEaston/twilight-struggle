package ca.mkiefte.cards;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class PortugueseEmpireCrumbles extends CardEvent {
	public static final String ID = "portugueseempirecrumbles;";
	public static final String DESCRIPTION = "Portuguese Empire Crumbles*";

	public PortugueseEmpireCrumbles() {
		this(ID, null);
	}

	public PortugueseEmpireCrumbles(final String type, final GamePiece inner) {
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
		comm = comm.append(Influence.getInfluenceMarker(Influence.SE_AFRICAN_STATES, TSPlayerRoster.USSR).adjustInfluence(+2));
		comm = comm.append(Influence.getInfluenceMarker(Influence.ANGOLA, TSPlayerRoster.USSR).adjustInfluence(+2));
		return comm;
	}
}
