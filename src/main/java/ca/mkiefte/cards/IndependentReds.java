package ca.mkiefte.cards;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceFilter;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.Utilities;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class IndependentReds extends DoActionOnLocation {
	public static final String ID = "independentreds;";
	public static final String DESCRIPTION = "Independent Reds*";
	
	public static final Set<String> COUNTRIES = new HashSet<String>(Arrays.asList(new String[]{
		Influence.YUGOSLAVIA, 
		Influence.ROMANIA, 
		Influence.BULGARIA, 
		Influence.HUNGARY, 
		Influence.CZECHOSLOVAKIA
	}));

	public IndependentReds() {
		this(ID, null);
	}

	public IndependentReds(final String type, final GamePiece inner) {
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
						&& COUNTRIES.contains(marker.getLocation())
						&& marker.getInfluence() > 0;
			}
		};
	}

	@Override
	protected Command doActionOnLocation(final Influence marker) {
		return marker.getOpponentInfluenceMarker().adjustInfluence(marker.getInfluence());
	}

	@Override
	protected String getNoMatchingDescription() {
		final StringBuilder builder = new StringBuilder("USSR has no influence in any of");
		Utilities.listAsString(builder, COUNTRIES, "or");
		return builder.toString();
	}

	@Override
	protected String getMessage() {
		return "Select a country to add US influence\n"
				+ "to equal USSR influence.";
	}
}
