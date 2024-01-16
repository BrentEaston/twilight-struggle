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
public final class Comecon extends ChangeInfluence {
	public static final String ID = "comecon;";
	public static final String DESCRIPTION = "Comecon*";	
	private static final int OPS_TO_ADD = 4;

	public Comecon() {
		this(ID, null);
	}

	public Comecon(final String type, final GamePiece inner) {
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
	protected Command autoPlace() {
		final Set<GamePiece> pieces = Utilities.findAllPiecesMatching(new PieceFilter() {		
			public boolean accept(final GamePiece piece) {
				final Influence marker = Influence.getInfluenceMarker(piece);
				return marker != null 
						&& passesFilter(marker);
			}
		});
		
		if (pieces.size() == 0) {
			final Chatter chatter = GameModule.getGameModule().getChatter();
			final Command chat = new Chatter.DisplayText(chatter, "There are no non-US-controlled countries in Eastern Europe.");
			return chat;
		} else if (pieces.size() <= 4) {
			Command comm = new NullCommand();
			for (final GamePiece gp: pieces) {
				final Influence marker = Influence.getInfluenceMarker(gp);
				comm = comm.append(marker.addInfluence(+1));
			}
			return comm;
		} else
			return null;
	}

	protected boolean passesFilter(final Influence marker) {
		return TSPlayerRoster.USSR.equals(marker.getSide()) 
				&& marker.isInEasternEurope()
				&& !marker.getOpponentInfluenceMarker().hasControl();
	}
	
	@Override
	public boolean isEventPlayable(final String who) {
		return super.isEventPlayable(who) && !getCard(TheIronLady.class).isEventInEffect();
	}

	@Override
	protected int nInfluence() {
		return OPS_TO_ADD;
	}

	@Override
	protected String getMessage() {
		return "Add 1 USSR Influence in each of 4\n"
				+ "non-US-controlled countries in Eastern Europe.";
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
}
