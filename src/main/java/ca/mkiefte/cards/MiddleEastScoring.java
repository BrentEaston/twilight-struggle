package ca.mkiefte.cards;
import ca.mkiefte.Influence;
import VASSAL.counters.GamePiece;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class MiddleEastScoring extends ShuttleDiplomacyRegion {
	public static final String ID = "middleeastscoring;";
	public static final String DESCRIPTION = "Middle East Scoring";
	
	public static final int PRESENCE_VPS = 3;
	public static final int DOMINATION_VPS = 5;
	public static final int CONTROL_VPS = 7;

	public MiddleEastScoring() {
		this(ID, null);
	}

	public MiddleEastScoring(final String type, final GamePiece inner) {
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
	public String getRegion() {
		return Influence.MIDDLE_EAST;
	}

	@Override
	protected int getVpsForPresence() {
		return PRESENCE_VPS;
	}

	@Override
	protected int getVpsForDomination() {
		return DOMINATION_VPS;
	}

	@Override
	protected int getVpsForControl() {
		return CONTROL_VPS;
	}
}
