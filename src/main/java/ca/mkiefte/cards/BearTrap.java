package ca.mkiefte.cards;

import ca.mkiefte.TSPlayerRoster;
import VASSAL.counters.GamePiece;

public final class BearTrap extends TrapEvent {
	public final static String ID = "beartrap;";
	public final static String DESCRIPTION = "Bear Trap*";

	public BearTrap() {
		this(ID, null);
	}

	public BearTrap(final String type, final GamePiece inner) {
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
