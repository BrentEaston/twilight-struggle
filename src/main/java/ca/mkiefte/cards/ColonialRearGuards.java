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
public final class ColonialRearGuards extends ChangeInfluence {
	public static final String ID = "colonialrearguards;";
	public static final String DESCRIPTION = "Colonial Rear Guards";
	private static final int INFLUENCE = 4;

	public ColonialRearGuards() {
		this(ID, null);
	}

	public ColonialRearGuards(final String type, final GamePiece inner) {
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
		return "Add 1 US Influence to 4 African and/or SE Asian countries.";
	}

	@Override
	protected Delegate getDelegate() {
		return new Influence.Delegate(this) {
			@Override
			public boolean canIncreaseInfluence(final Influence inf) {
				return super.canIncreaseInfluence(inf)
						&& passesFilter(inf)
						&& inf.getInfluence() == inf.getStartingInfluence();
			}
		};
	}

	@Override
	protected boolean passesFilter(final Influence marker) {
		return TSPlayerRoster.US.equals(marker.getSide())
				&& (Influence.AFRICA.equals(marker.getRegion()) || marker.isInSouthEastAsia());
	}
}
