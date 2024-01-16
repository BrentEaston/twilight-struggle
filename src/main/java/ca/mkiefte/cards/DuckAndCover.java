package ca.mkiefte.cards;

import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.TSTurnTracker;
import ca.mkiefte.Utilities;

public final class DuckAndCover extends CardEvent {
	public static final String ID = "duckandcover;";
	public static final String DESCRIPTION = "Duck and Cover";

	public DuckAndCover() {
		this(ID, null);
	}

	public DuckAndCover(final String type, final GamePiece inner) {
		super(type, inner);
	}

	@Override
	protected String getIdName() {
		return ID;
	}

	@Override
	public Command myPlayEvent(String who) {
		Command comm = super.myPlayEvent(who);
		comm = comm.append(Utilities.adjustDefcon(-1));
		if (TSTurnTracker.isGameOver())
			return comm;
		final int defcon = Integer.valueOf(Utilities.getGlobalProperty(Utilities.DEFCON).getPropertyValue());
		comm = comm.append(Utilities.adjustVps(5-defcon));
		return comm;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}
}
