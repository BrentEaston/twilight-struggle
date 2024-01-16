package ca.mkiefte.cards;

import static ca.mkiefte.Utilities.adjustVps;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.Influence;
import ca.mkiefte.Influence.Delegate;
import ca.mkiefte.TSPlayerRoster;

public final class SpecialRelationship extends ChangeInfluence {
	public static final String ID = "specialrelationship;";
	public static final String DESCRIPTION = "Special Relationship";

	public SpecialRelationship() {
		this(ID, null);
	}

	public SpecialRelationship(final String type, final GamePiece inner) {
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
		if (getCard(Nato.class).isEventInEffect())
			comm = comm.append(adjustVps(+2));
		return comm;
	}

	@Override
	public boolean isEventPlayable(final String who) {
		return super.isEventPlayable(who) && Influence.getInfluenceMarker(Influence.U_K, TSPlayerRoster.US).hasControl();
	}

	@Override
	protected int nInfluence() {
		if (getCard(Nato.class).isEventInEffect())
			return 2;
		else
			return 1;
	}

	@Override
	protected Command autoPlace() {
		return null;
	}

	@Override
	protected String getMessage() {
		if (getCard(Nato.class).isEventInEffect())
			return "Add 2 Influence to any Western European country.";
		else
			return "Add 1 Influence to any country adjacent to the UK.";
	}

	@Override
	protected Delegate getDelegate() {
		return new Influence.Delegate(this) {
			@Override
			public boolean canIncreaseInfluence(final Influence marker) {
				return super.canIncreaseInfluence(marker) 
						&& passesFilter(marker)
						&& marker.getInfluence() == marker.getStartingInfluence();
			}
			@Override
			public Command incrementInfluence(final Influence inf, int amount) {
				if (getCard(Nato.class).isEventInEffect())
					amount *= 2;
				return super.incrementInfluence(inf, amount);
			}
		};
	}

	@Override
	protected boolean passesFilter(final Influence marker) {
		if (getCard(Nato.class).isEventInEffect())
			return TSPlayerRoster.US.equals(marker.getSide()) && marker.isInWesternEurope();
		else
			return TSPlayerRoster.US.equals(marker.getSide()) && Influence.getNeighbours(Influence.U_K).contains(marker.getLocation());
	}
}
