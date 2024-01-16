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
public final class MarineBarracksBombing extends ChangeInfluence {
	private static final int INFLUENCE = 2;
	public static final String ID = "marinebarracksbombing;";
	public static final String DESCRIPTION = "Marine Barracks Bombing*";

	public MarineBarracksBombing() {
		this(ID, null);
	}

	public MarineBarracksBombing(final String type, final GamePiece inner) {
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
				return marker != null
						&& passesFilter(marker)
						&& marker.getInfluence() > 0;
			}
		});
		int influence = 0;
		for (final GamePiece gp : pieces) {
			final Influence marker = Influence.getInfluenceMarker(gp);
			influence += marker.getInfluence();
		}
		if (influence == 0)
			return new Chatter.DisplayText(GameModule.getGameModule().getChatter(), "US has no Influence in Middle East.");
		else if (influence <= INFLUENCE) {
			Command comm = new NullCommand();
			for (final GamePiece gp : pieces) {
				final Influence marker = Influence.getInfluenceMarker(gp);
				comm = comm.append(marker.removeAllInfluence());
			}
			return comm;
		} else
			return null;
	}

	@Override
	protected String getMessage() {
		return "Remove 2 US Influence from anywhere in the Middle East.";
	}

	@Override
	protected Delegate getDelegate() {
		return new Delegate(this, Influence.OPS_REMAINING, false) {
			@Override
			public boolean canDecreaseInfluence(final Influence marker) {
				return super.canDecreaseInfluence(marker) && passesFilter(marker);
			}			
		};
	}

	@Override
	protected boolean passesFilter(final Influence marker) {
		return TSPlayerRoster.US.equals(marker.getSide()) && Influence.MIDDLE_EAST.equals(marker.getRegion());
	}

	@Override
	protected Command setup() {
		Command comm = Influence.getInfluenceMarker(Influence.LEBANON, TSPlayerRoster.US).removeAllInfluence();
		comm = comm.append(super.setup());
		return comm;
	}
}
