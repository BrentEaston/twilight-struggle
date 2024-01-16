package ca.mkiefte.cards;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.command.NullCommand;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceFilter;
import ca.mkiefte.Influence;
import ca.mkiefte.Influence.Delegate;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.Utilities;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class SuezCrisis extends ChangeInfluence {
	private static final int INFLUENCE = 4;
	public static final String ID = "suezcrisis;";
	public static final String DESCRIPTION = "Suez Crisis*";
	private static final Set<String> COUNTRIES = new HashSet<String>(Arrays.asList(new String[] {
			Influence.FRANCE, Influence.U_K, Influence.ISRAEL
	}));

	public SuezCrisis() {
		this(ID, null);
	}

	public SuezCrisis(final String type, final GamePiece inner) {
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
		final Set<GamePiece> pieces = Utilities.findAllPiecesMatching(new PieceFilter() {		
			public boolean accept(final GamePiece piece) {
				final Influence marker = Influence.getInfluenceMarker(piece);
				return marker != null && passesFilter(marker) && marker.getInfluence() > 0;
			}
		});
		
		int total = 0;
		for (final GamePiece gp : pieces) {
			final Influence marker = Influence.getInfluenceMarker(gp);
			final int inf = marker.getInfluence();
			total += inf >= 2 ? 2 : inf;
		}
		
		if (total == 0) {
			final Chatter chatter = GameModule.getGameModule().getChatter();
			final StringBuilder builder = new StringBuilder("There is no US influence in");
			Utilities.listAsString(builder, COUNTRIES, "and");
			final Command chat = new Chatter.DisplayText(chatter, builder.toString());
			chat.execute();
			return chat;
		} else if (total <= 4) {
			Command comm = new NullCommand();
			for (final GamePiece gp : pieces) {
				final Influence marker = Influence.getInfluenceMarker(gp);
				comm = comm.append(marker.adjustInfluence(-2));
			}
			return comm;
		} else
			return null;
	}

	@Override
	protected String getMessage() {
		final StringBuilder builder = new StringBuilder("Remove 4 US Influence from"); 
		Utilities.listAsString(builder, COUNTRIES, "and");
		builder.append("\nRemove no more than 2 per country.");
		return builder.toString();
	}

	@Override
	protected Delegate getDelegate() {
		return new Influence.Delegate(this, Influence.OPS_REMAINING, false) {
			@Override
			public boolean canDecreaseInfluence(final Influence marker) {
				return super.canDecreaseInfluence(marker)
						&& passesFilter(marker)
						&& marker.getInfluence() > marker.getStartingInfluence() - 2;
			}
		};
	}

	protected boolean passesFilter(final Influence marker) {
		return TSPlayerRoster.US.equals(marker.getSide())
				&& COUNTRIES.contains(marker.getLocation());
	}
}
