package ca.mkiefte.cards;

import static ca.mkiefte.Utilities.adjustDefcon;
import static ca.mkiefte.Utilities.adjustVps;
import static ca.mkiefte.Utilities.getGlobalProperty;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.Utilities;

public final class NuclearTestBan extends CardEvent {
	public final static String ID = "nucleartestban;";
	public final static String DESCRIPTION = "Nuclear Test Ban";

	public NuclearTestBan() {
		this(ID, null);
	}

	public NuclearTestBan(final String type, final GamePiece inner) {
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
	public Command myPlayEvent(final String who) {
		Command comm = super.myPlayEvent(who);
		final int defcon = Integer.valueOf(getGlobalProperty(Utilities.DEFCON).getPropertyValue());
		int vps = defcon - 2;
		vps *= TSPlayerRoster.US.equals(getOwner()) ? 1 : -1;
		comm = comm.append(adjustVps(vps));
		comm = comm.append(adjustDefcon(+2));
		return comm;
	}
}
