package ca.mkiefte.cards;

import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.Utilities;

public final class CapturedNaziScientist extends CardEvent {
	public static String ID = "capturednaziscientist;";
	public static String DESCRIPTION = "Caputured Nazi Scientist*";	
	
	public CapturedNaziScientist() {
		this(ID, null);
	}

	public CapturedNaziScientist(final String type, final GamePiece inner) {
		super(type, inner);
	}

	@Override
	public Command myPlayEvent(final String who) {
		Command comm = super.myPlayEvent(who);
		comm = comm.append(Utilities.advanceSpaceRace(getOwner()));
		return comm;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	protected String getIdName() {
		return ID;
	}
}