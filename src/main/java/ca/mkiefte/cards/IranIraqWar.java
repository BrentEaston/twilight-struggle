package ca.mkiefte.cards;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;
import VASSAL.counters.GamePiece;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class IranIraqWar extends BiWar {
	private static final int VPS = 2;
	private static final int MILITARY_OPS = 2;
	public static final String ID = "iraniraqwar;";
	public static final String DESCRIPTION = "Iran-Iraq War*";
	
	private static final String[] TARGETS = {Influence.IRAN, Influence.IRAQ};

	public IranIraqWar() {
		this(ID, null);
	}

	public IranIraqWar(final String type, final GamePiece inner) {
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
	protected String[] getPotentialTargetNames() {
		return TARGETS;
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
}
