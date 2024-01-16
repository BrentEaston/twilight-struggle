package ca.mkiefte.cards;
import ca.mkiefte.TSPlayerRoster;
import VASSAL.counters.GamePiece;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class LoneGunman extends RevealHandEvent {
	public static final String ID = "lonegunman;";
	public static final String DESCRIPTION = "\"Lone Gunman\"*";

	public LoneGunman() {
		this(ID, null);
	}

	public LoneGunman(final String type, final GamePiece inner) {
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
	protected String getTarget() {
		return TSPlayerRoster.US;
	}
}
