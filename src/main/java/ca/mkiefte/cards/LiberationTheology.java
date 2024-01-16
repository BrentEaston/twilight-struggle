package ca.mkiefte.cards;
import ca.mkiefte.Influence;
import ca.mkiefte.Influence.Delegate;
import ca.mkiefte.TSPlayerRoster;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class LiberationTheology extends ChangeInfluence {
	private static final int INFLUENCE = 3;
	public static final String ID = "liberationtheology;";
	public static final String DESCRIPTION = "Liberation Theology";

	public LiberationTheology() {
		this(ID, null);
	}

	public LiberationTheology(final String type, final GamePiece inner) {
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
	protected int nInfluence() {
		return INFLUENCE;
	}

	@Override
	protected Command autoPlace() {
		return null;
	}

	@Override
	protected String getMessage() {
		return "Add 3 USSR Influence in Central America, no more than 2 per country.";
	}

	@Override
	protected Delegate getDelegate() {
		return new Delegate(this) {
			@Override
			public boolean canIncreaseInfluence(final Influence marker) {
				return super.canIncreaseInfluence(marker) 
						&& passesFilter(marker) 
						&& marker.getInfluence() < marker.getStartingInfluence() + 2;
			}		
		};
	}

	@Override
	protected boolean passesFilter(final Influence marker) {
		return TSPlayerRoster.USSR.equals(marker.getSide()) && Influence.CENTRAL_AMERICA.equals(marker.getRegion());
	}
}
