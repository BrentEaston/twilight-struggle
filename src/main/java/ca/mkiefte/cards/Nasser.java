package ca.mkiefte.cards;


import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;

public final class Nasser extends CardEvent {
	public static final String ID = "nasser;";
	public static final String DESCRIPTION = "Nasser*";

	public Nasser() {
		this(ID, null);
	}

	public Nasser(final String type, final GamePiece inner) {
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
		Influence marker = Influence.getInfluenceMarker(Influence.EGYPT, TSPlayerRoster.USSR);
		comm = comm.append(marker.adjustInfluence(+2));
		marker = marker.getOpponentInfluenceMarker();
		final int influence = marker.getInfluence();
		final int decrease = influence/2 + influence%2;
		comm = comm.append(marker.adjustInfluence(-decrease));
		return comm;
	}
}
