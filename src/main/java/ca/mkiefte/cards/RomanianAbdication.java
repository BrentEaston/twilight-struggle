package ca.mkiefte.cards;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class RomanianAbdication extends CardEvent {
	public static final String ID = "romanianabdication;";
	public static final String DESCRIPTION = "Romanian Abdication*";

	public RomanianAbdication() {
		this(ID, null);
	}

	public RomanianAbdication(final String type, final GamePiece inner) {
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
	public Command myPlayEvent(String who) {
		Command comm = super.myPlayEvent(who);
		Influence marker = Influence.getInfluenceMarker(Influence.ROMANIA, TSPlayerRoster.US);
		comm = comm.append(marker.removeAllInfluence());
		marker = marker.getOpponentInfluenceMarker();
		comm = comm.append(marker.takeControl());
		return comm;
	}
}
