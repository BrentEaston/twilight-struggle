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

public final class OrtegaElectedInNicaragua extends ChangeInfluence {
	public static final String ID = "ortegaelectedinnicaragua;";
	public static final String DESCRIPTION = "Ortega Elected in Nicaragua*";

	public OrtegaElectedInNicaragua() {
		this(ID, null);
	}

	public OrtegaElectedInNicaragua(final String type, final GamePiece inner) {
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
				return marker != null && marker.getInfluence() > 0 && passesFilter(marker);
			}
		});
		if (gp == null) {
			Command chat = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), "US has no Influence adjacent to Nicaragua.");
			chat.execute();
			return chat;
		} else
			return null;
	}

	@Override
	protected String getMessage() {
		return "Make one free Coup attempt in a country adjacent to Nicaragua.";
	}

	@Override
	protected Delegate getDelegate() {
		return new Delegate(this) {
			@Override
			public boolean canCoup() {
				return true;
			}

			@Override
			public boolean canChangeInfluence() {
				return false;
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
		return TSPlayerRoster.US.equals(marker.getSide()) 
				&& Influence.getNeighbours(Influence.NICARAGUA).contains(marker.getLocation());
	}

	@Override
	protected Command setup() {
		Command comm = Influence.getInfluenceMarker(Influence.NICARAGUA, TSPlayerRoster.US).removeAllInfluence();
		comm = comm.append(super.setup());
		return comm;
	}
}
