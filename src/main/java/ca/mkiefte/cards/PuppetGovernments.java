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
public final class PuppetGovernments extends ChangeInfluence {
	private static final int INFLUENCE = 3;
	public static final String ID = "puppetgovernments;";
	public static final String DESCRIPTION = "Puppet Governments*";

	public PuppetGovernments() {
		this(ID, null);
	}

	public PuppetGovernments(final String type, final GamePiece inner) {
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
				return marker != null && passesFilter(marker);
			}
		});
		final Chatter chatter = GameModule.getGameModule().getChatter();
		if (pieces.size() == 0) {
			final Command chat = new Chatter.DisplayText(chatter, "There are no countries that contain no influence from either power.");
			chat.execute();
			return chat;
		} else if (pieces.size() <= 3) {
			Command comm = new NullCommand();
			for (final GamePiece gp: pieces) {
				final Influence marker = Influence.getInfluenceMarker(gp);
				comm = comm.append(marker.adjustInfluence(+1));
			}
			return comm;
		} else
			return null;
	}

	@Override
	protected String getMessage() {
		return "Add 1 Influence in 3 countries that have no Influence from either power.";
	}

	@Override
	protected Delegate getDelegate() {
		return new Delegate(this) {
			@Override
			public boolean canIncreaseInfluence(final Influence marker) {
				return super.canIncreaseInfluence(marker)
						&& passesFilter(marker);
			}

			@Override
			public boolean isOptional() {
				return true;
			}
		};
	}

	@Override
	protected boolean passesFilter(final Influence marker) {
		return TSPlayerRoster.US.equals(marker.getSide()) 
				&& marker.getInfluence() == 0 
				&& marker.getOpponentInfluenceMarker().getInfluence() == 0;
	}
}
