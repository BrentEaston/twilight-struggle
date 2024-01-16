package ca.mkiefte.cards;
import ca.mkiefte.TSPlayerRoster;
import VASSAL.counters.GamePiece;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class CiaCreated extends RevealHandEvent {
	public static final String ID = "ciacreated;";
	public static final String DESCRIPTION = "CIA Created*";

	public CiaCreated() {
		this(ID, null);
	}

	public CiaCreated(final String type, final GamePiece inner) {
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
		return TSPlayerRoster.USSR;
	}
}
