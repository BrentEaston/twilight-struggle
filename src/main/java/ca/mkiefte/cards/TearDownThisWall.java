package ca.mkiefte.cards;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceFilter;
import ca.mkiefte.Influence;
import ca.mkiefte.Influence.Delegate;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.Utilities;

public final class TearDownThisWall extends ChangeInfluence {
	public static final String ID = "teardownthiswall;";
	public static final String DESCRIPTION = "Tear Down This Wall*";

	public TearDownThisWall() {
		this(ID, null);
	}

	public TearDownThisWall(final String type, final GamePiece inner) {
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
		return getCardOps();
	}

	@Override
	protected Command autoPlace() {
		final GamePiece gp = Utilities.findPiece(new PieceFilter() {			
			public boolean accept(final GamePiece piece) {
				final Influence marker = Influence.getInfluenceMarker(piece);
				return marker != null && passesFilter(marker) && marker.getInfluence() > 0;
			}
		});
		if (gp == null) {
			final Command chat = new Chatter.DisplayText(GameModule.getGameModule().getChatter(),
					"USSR has no Influence in Europe.");
			chat.execute();
			return chat;
		} else
			return null;
	}

	@Override
	protected String getMessage() {
		return "Make a free Coup or Realignment attempts in Europe.";
	}

	@Override
	protected Delegate getDelegate() {
		return new Delegate(this) {
			@Override
			public boolean canRealign() {
				return true;
			}

			@Override
			public boolean canCoup() {
				return true;
			}

			@Override
			public boolean canChangeInfluence() {
				return false;
			}

			@Override
			protected boolean canCoupOrRealign(Influence marker) {
				return super.canCoupOrRealign(marker) && passesFilter(marker);
			}

			@Override
			public boolean freeCoup() {
				return true;
			}

			@Override
			public boolean isOptional() {
				return true;
			}
		};
	}

	@Override
	protected boolean passesFilter(final Influence marker) {
		return TSPlayerRoster.USSR.equals(marker.getSide()) && Influence.EUROPE.equals(marker.getRegion());
	}

	@Override
	protected Command setup() {
		Command comm = Influence.getInfluenceMarker(Influence.E_GERMANY, TSPlayerRoster.US).adjustInfluence(+3);
		final WillyBrandt willy = getCard(WillyBrandt.class);
		if (willy.isEventInEffect())
			comm = comm.append(willy.cancelEvent());
		return comm.append(super.setup());
	}
}
