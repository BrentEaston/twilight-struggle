package ca.mkiefte.cards;
import ca.mkiefte.Influence;
import VASSAL.counters.GamePiece;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class SouthAmericaScoring extends ScoringCard {
	public static final String ID = "southamericascoring;";
	public static final String DESCRIPTION = "South America Scoring";
	
	public static final int PRESENCE = 2;
	public static final int DOMINATION = 5;
	public static final int CONTROL = 6;

	public SouthAmericaScoring() {
		this(ID, null);
	}

	public SouthAmericaScoring(final String type, final GamePiece inner) {
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
		return Influence.SOUTH_AMERICA;
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
