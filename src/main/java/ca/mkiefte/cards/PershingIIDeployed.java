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
import ca.mkiefte.TSTurnTracker;
import ca.mkiefte.Utilities;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class PershingIIDeployed extends ChangeInfluence {
	private static final int COUNTRIES = 3;
	public static final String ID = "pershingiideployed;";
	public static final String DESCRIPTION = "Pershing II Deployed*";

	public PershingIIDeployed() {
		this(ID, null);
	}

	public PershingIIDeployed(final String type, final GamePiece inner) {
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
		return COUNTRIES;
	}

	@Override
	protected Command autoPlace() {
		final Set<GamePiece> pieces = Utilities.findAllPiecesMatching(new PieceFilter() {			
			public boolean accept(final GamePiece piece) {
				final Influence marker = Influence.getInfluenceMarker(piece);
				return marker != null && passesFilter(marker) && marker.getInfluence() > 0;
			}
		});
		if (pieces.size() == 0) {
			final Command chat = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), 
					"US has no Influence in Western Europe.");
			chat.execute();
			return chat;
		} else if (pieces.size() <= COUNTRIES) {
			Command comm = new NullCommand();
			for (final GamePiece gp : pieces) {
				final Influence marker = Influence.getInfluenceMarker(gp);
				comm = comm.append(marker.adjustInfluence(-1));
			}
			return comm;
		} else
			return null;
	}

	@Override
	protected String getMessage() {
		return "Remove 1 US Influence from up to 3 countries in Western Europe.";
	}

	@Override
	protected Delegate getDelegate() {
		return new Delegate(this, Influence.OPS_REMAINING, false) {
			@Override
			public boolean canDecreaseInfluence(final Influence marker) {
				return super.canDecreaseInfluence(marker)
						&& passesFilter(marker)
						&& marker.getInfluence() == marker.getStartingInfluence();
			}

			@Override
			public boolean isOptional() {
				return true;
			}
		};
	}

	@Override
	protected boolean passesFilter(final Influence marker) {
		return TSPlayerRoster.US.equals(marker.getSide()) && marker.isInWesternEurope();
	}

	@Override
	protected Command setup() {
		Command comm = Utilities.adjustVps(-1);
		if (TSTurnTracker.isGameOver())
			return comm.append(setFinished(true));
		else
			return comm.append(super.setup());
	}
}
