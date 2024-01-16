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
public final class OASFounded extends ChangeInfluence {
	private static final int INFLUENCE = 2;
	public static final String ID = "oasfounded;";
	public static final String DESCRIPTION = "OAS Founded*";

	public OASFounded() {
		this(ID, null);
	}

	public OASFounded(final String type, final GamePiece inner) {
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
		return "Add 2 Influence in Central and/or South America.";
	}

	@Override
	protected Delegate getDelegate() {
		return new Delegate(this) {
			@Override
			public boolean canIncreaseInfluence(final Influence marker) {
				return super.canIncreaseInfluence(marker) && passesFilter(marker);
			}			
		};
	}

	@Override
	protected boolean passesFilter(final Influence marker) {
		final String region = marker.getRegion();
		return TSPlayerRoster.US.equals(marker.getSide())
				&& (Influence.CENTRAL_AMERICA.equals(region)
						|| Influence.SOUTH_AMERICA.equals(region));
	}
}
