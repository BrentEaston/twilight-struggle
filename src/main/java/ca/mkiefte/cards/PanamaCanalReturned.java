package ca.mkiefte.cards;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class PanamaCanalReturned extends CardEvent {
	private static final String[] COUNTRIES = new String[] {Influence.PANAMA, Influence.COSTA_RICA, Influence.VENEZUELA};
	public static final String ID = "panamacanalreturned;";
	public static final String DESCRIPTION = "Panama Canal Returned*";

	public PanamaCanalReturned() {
		this(ID, null);
	}

	public PanamaCanalReturned(final String type, final GamePiece inner) {
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
	protected Command myPlayEvent(String who) {
		Command comm = super.myPlayEvent(who);
		for (final String country : COUNTRIES)
			comm = comm.append(Influence.getInfluenceMarker(country, TSPlayerRoster.US).adjustInfluence(+1));
		return comm;
	}
}
