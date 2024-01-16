package ca.mkiefte.cards;
import java.util.Set;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
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
public final class SocialistGovernments extends ChangeInfluence {
	public static final String ID = "socialistgovernments;";
	public static final String DESCRIPTION = "Socialist Governments";
	private static final int OPS_TO_REMOVE = 3;

	public SocialistGovernments() {
		this(ID, null);
	}

	public SocialistGovernments(final String type, final GamePiece inner) {
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

	protected boolean passesFilter(final Influence marker) {
		return TSPlayerRoster.US.equals(marker.getSide()) && marker.isInWesternEurope();
	}
	
	@Override
	protected Command autoPlace() {
		Command comm = null;
		
		final Set<GamePiece> pieces = Utilities.findAllPiecesMatching(new PieceFilter() {		
			public boolean accept(final GamePiece piece) {
				final Influence marker = Influence.getInfluenceMarker(piece);
				return marker != null && passesFilter(marker) && marker.getInfluence() > 0;
			}
		});
		
		int total = 0;
		for (final GamePiece gp : pieces) {
			final Influence inf = Influence.getInfluenceMarker(gp);
			final int i = inf.getInfluence();
			total += i > 2 ? 2 : i;
		}
		
		if (total == 0) {
			final Chatter chatter = GameModule.getGameModule().getChatter();
			comm = new Chatter.DisplayText(chatter, "There is no US Influence in Western Europe.");
			comm.execute();
		} else if (total <= OPS_TO_REMOVE) {
			for (final GamePiece gp : pieces) {
				final Influence marker = Influence.getInfluenceMarker(gp);
				final int inf = marker.getInfluence();
				comm = comm.append(marker.setInfluence(inf <= 2 ? 0 : inf-2));
			}
		} 
		
		return comm;
	}

	@Override
	protected String getMessage() {
		return "Remove 3 US Influence in Western Europe, no more than 2 per country.";
	}
	
	@Override
	protected Influence.Delegate getDelegate() {
		return new Influence.Delegate(this, Influence.OPS_REMAINING, false) {
			@Override
			public boolean canDecreaseInfluence(Influence marker) {
				return super.canDecreaseInfluence(marker)
					&& passesFilter(marker)
					&& marker.getInfluence() > marker.getStartingInfluence() - 2;
			}
		};
	}

	@Override
	public boolean isEventPlayable(final String who) {
		return super.isEventPlayable(who) && !getCard(TheIronLady.class).isEventInEffect();
	}

	@Override
	public int nInfluence() {
		return OPS_TO_REMOVE;
	}
}
