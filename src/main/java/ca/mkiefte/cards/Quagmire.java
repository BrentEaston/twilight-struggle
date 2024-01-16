package ca.mkiefte.cards;

import ca.mkiefte.TSPlayerRoster;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;

public final class Quagmire extends TrapEvent {
	public final static String ID = "quagmire;";
	public final static String DESCRIPTION = "Quagmire*";

	public Quagmire() {
		this(ID, null);
	}

	public Quagmire(final String type, final GamePiece inner) {
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
		final Norad norad = getCard(Norad.class);
		if (norad.isEventInEffect())
			comm = comm.append(norad.cancelEvent());
		return comm;
	}

	@Override
	protected String getTarget() {
		return TSPlayerRoster.US;
	}
}
