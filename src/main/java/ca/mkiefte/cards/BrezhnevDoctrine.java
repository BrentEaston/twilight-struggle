package ca.mkiefte.cards;
import ca.mkiefte.TSPlayerRoster;
import VASSAL.counters.GamePiece;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class BrezhnevDoctrine extends BonusCard {
	public static final String ID = "brezhnevdoctrine;";
	public static final String DESCRIPTION = "Brezhnev Doctrine*";

	public BrezhnevDoctrine() {
		this(ID, null);
	}

	public BrezhnevDoctrine(final String type, final GamePiece inner) {
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
	public boolean isEventInEffect() {
		return isEventPlayedThisTurn();
	}
	
	@Override
	protected String getRecipient() {
		return TSPlayerRoster.USSR;
	}
}
