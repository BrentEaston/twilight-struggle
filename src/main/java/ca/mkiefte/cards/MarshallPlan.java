package ca.mkiefte.cards;
import java.util.Set;

import ca.mkiefte.Influence;
import ca.mkiefte.Influence.Delegate;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.Utilities;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.command.NullCommand;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceFilter;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class MarshallPlan extends ChangeInfluence {
	private static final int INFLUENCE = 7;
	public static final String ID = "marshallplan;";
	public static final String DESCRIPTION = "Marshall Plan*";

	public MarshallPlan() {
		this(ID, null);
	}

	public MarshallPlan(final String type, final GamePiece inner) {
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
	protected boolean isUnderlined() {
		return true;
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
				return marker != null && passesFilter(marker);
			}
		});
		
		if (pieces.size() == 0) {
			final Chatter chatter = GameModule.getGameModule().getChatter();
			Command chat = new Chatter.DisplayText(chatter, "There are no non-USSR-controlled countries in Western Europe.");
			chat.execute();
			return chat;
		} else if (pieces.size() <= INFLUENCE) {
			Command comm = new NullCommand();
			for (final GamePiece gp : pieces) {
				final Influence marker = Influence.getInfluenceMarker(gp);
				comm = comm.append(marker.addInfluence(+1));
			}
			return comm;
		} else
			return null;
	}

	@Override
	protected String getMessage() {
		return "Add 1 US Influence to 7 non-USSR-controlled\n"
				+ "countries in Western Europe.";
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
		};
	}

	@Override
	protected boolean passesFilter(final Influence marker) {
		return TSPlayerRoster.US.equals(marker.getSide()) 
				&& marker.isInWesternEurope()
				&& !marker.getOpponentInfluenceMarker().hasControl();
	}
}
