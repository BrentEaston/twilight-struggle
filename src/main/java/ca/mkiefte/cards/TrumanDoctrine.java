package ca.mkiefte.cards;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceFilter;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class TrumanDoctrine extends DoActionOnLocation {
	public static final String ID = "trumandoctrine;";
	public static final String DESCRIPTION = "Truman Doctrine*";

	public TrumanDoctrine() {
		this(ID, null);
	}

	public TrumanDoctrine(final String type, final GamePiece inner) {
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
	protected PieceFilter getLocationFilter() {
		return new PieceFilter() {			
			public boolean accept(final GamePiece piece) {
				final Influence marker = Influence.getInfluenceMarker(piece);
				return marker != null
						&& TSPlayerRoster.USSR.equals(marker.getSide())
						&& Influence.EUROPE.equals(marker.getRegion())
						&& marker.getInfluence() > 0
						&& !marker.hasControl()
						&& !marker.getOpponentInfluenceMarker().hasControl();
			}
		};
	}

	@Override
	protected Command doActionOnLocation(final Influence inf) {
		return inf.removeAllInfluence();
	}

	@Override
	protected String getNoMatchingDescription() {
		return "There are no uncontrolled countries in Europe with USSR influence.";
	}

	@Override
	protected String getMessage() {
		return "Select a country to remove all USSR influence";
	}
}
