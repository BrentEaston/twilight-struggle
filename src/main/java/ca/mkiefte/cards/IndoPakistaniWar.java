package ca.mkiefte.cards;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;
import VASSAL.counters.GamePiece;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class IndoPakistaniWar extends BiWar {
	private static final int VPS = 2;
	private static final int MILITARY_OPS = 2;
	public static final String ID = "indopakistaniwar;";
	public static final String DESCRIPTION = "Indo-Pakistani War";
	
	private static final String[] TARGETS = {Influence.INDIA, Influence.PAKISTAN};

	public IndoPakistaniWar() {
		this(ID, null);
	}

	public IndoPakistaniWar(final String type, final GamePiece inner) {
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
	protected int getMilitaryOps() {
		return MILITARY_OPS;
	}

	@Override
	protected int getVps(final String who) {
		return TSPlayerRoster.US.equals(who) ? VPS : -VPS;
	}

	@Override
	protected boolean isSuccessful(final int result) {
		return result >= 4;
	}

	@Override
	protected String[] getPotentialTargetNames() {
		return TARGETS;
	}
}
