package ca.mkiefte.cards;
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
public final class TheVoiceOfAmerica extends ChangeInfluence {
	private static final int INFLUENCE = 4;
	public static final String ID = "thevoiceofamerica;";
	public static final String DESCRIPTION = "The Voice of America";

	public TheVoiceOfAmerica() {
		this(ID, null);
	}

	public TheVoiceOfAmerica(final String type, final GamePiece inner) {
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
		
		if (pieces == null || pieces.size() == 0) {
			final Chatter chatter = GameModule.getGameModule().getChatter();
			final Command chat = new Chatter.DisplayText(chatter, "USSR has no Influence in non-European countries.");
			chat.execute();
			return chat;
		}
		
		int total = 0;
		for (final GamePiece gp: pieces) {
			final Influence marker = Influence.getInfluenceMarker(gp);
			final int influence = marker.getInfluence();
			total += influence >= 2 ? 2 : influence;
		}
		
		if (total <= 4) {
			Command comm = new NullCommand();
			for (final GamePiece gp : pieces) {
				final Influence marker = Influence.getInfluenceMarker(gp);
				comm = comm.append(marker.addInfluence(-2));
			}
			return comm;
		}
		
		return null;
	}

	@Override
	protected String getMessage() {
		return "Remove 4 USSR influenc from non-European countries, no more than 2 per country.";
	}

	@Override
	protected Delegate getDelegate() {
		return new Delegate(this, Influence.OPS_REMAINING, false) {
			@Override
			public boolean canDecreaseInfluence(final Influence marker) {
				return super.canDecreaseInfluence(marker)
						&& passesFilter(marker)
						&& marker.getInfluence() > marker.getStartingInfluence()-2;
			}		
		};
	}

	@Override
	protected boolean passesFilter(final Influence marker) {
		return TSPlayerRoster.USSR.equals(marker.getSide())
				&& !Influence.EUROPE.equals(marker.getRegion());
	}
}
