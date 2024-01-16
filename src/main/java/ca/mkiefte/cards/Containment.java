package ca.mkiefte.cards;
import ca.mkiefte.TSPlayerRoster;
import VASSAL.counters.GamePiece;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class Containment extends BonusCard {
	public static final String ID = "containment;";
	public static final String DESCRIPTION = "Containment*";

	public Containment() {
		this(ID, null);
	}

	public Containment(final String type, final GamePiece inner) {
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
	protected String getRecipient() {
		return TSPlayerRoster.US;
	}
}
