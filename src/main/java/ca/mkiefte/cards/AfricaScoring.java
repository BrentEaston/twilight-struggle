package ca.mkiefte.cards;
import ca.mkiefte.Influence;
import VASSAL.counters.GamePiece;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class AfricaScoring extends ScoringCard {
	public static final String ID = "africascoring;";
	public static final String DESCRIPTION = "Africa Scoring";
	
	public static final int PRESENCE = 1;
	public static final int DOMINATION = 4;
	public static final int CONTROL = 6;

	public AfricaScoring() {
		this(ID, null);
	}

	public AfricaScoring(final String type, final GamePiece inner) {
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
		return Influence.AFRICA;
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
