package ca.mkiefte.cards;

import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;

public final class Solidarity extends CardEvent {
	public static final String ID = "solidarity;";
	public static final String DESCRIPTION = "Solidarity*";

	public Solidarity() {
		this(ID, null);
	}

	public Solidarity(final String type, final GamePiece inner) {
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
	public boolean isEventPlayable(final String who) {
		return super.isEventPlayable(who) 
				&& CardEvent.getCard(JohnPaulIIElectedPope.class).isEventInEffect();
	}

	@Override
	public Command myPlayEvent(final String who) {
		Command comm = super.myPlayEvent(who);
		comm = comm.append(Influence.getInfluenceMarker(Influence.POLAND, TSPlayerRoster.US).adjustInfluence(+3));
		return comm;
	}
}
