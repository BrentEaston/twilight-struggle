package ca.mkiefte.cards;
import ca.mkiefte.Influence;
import VASSAL.counters.GamePiece;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class CentralAmericaScoring extends ScoringCard {
	public static final String ID = "centralamericascoring;";
	public static final String DESCRIPTION = "Central America Scoring";
	
	public static final int PRESENCE = 1;
	public static final int DOMINATION = 3;
	public static final int CONTROL = 5;

	public CentralAmericaScoring() {
		this(ID, null);
	}

	public CentralAmericaScoring(final String type, final GamePiece inner) {
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
		return Influence.CENTRAL_AMERICA;
	}

	@Override
	protected int getVpsForPresence() {
		return PRESENCE;
	}

	@Override
	protected int getVpsForDomination() {
		return DOMINATION;
	}

	@Override
	protected int getVpsForControl() {
		return CONTROL;
	}
}
