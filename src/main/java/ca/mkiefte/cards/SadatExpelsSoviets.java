package ca.mkiefte.cards;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class SadatExpelsSoviets extends CardEvent {
	public static final String ID = "sadatexpelssoviets;";
	public static final String DESCRIPTION = "Sadat Expels Soviets*";

	public SadatExpelsSoviets() {
		this(ID, null);
	}

	public SadatExpelsSoviets(final String type, final GamePiece inner) {
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
		Influence marker = Influence.getInfluenceMarker(Influence.EGYPT, TSPlayerRoster.USSR);
		comm = comm.append(marker.removeAllInfluence());
		marker = marker.getOpponentInfluenceMarker();
		comm = comm.append(marker.adjustInfluence(+1));
		return comm;
	}
}
