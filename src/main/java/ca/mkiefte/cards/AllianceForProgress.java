package ca.mkiefte.cards;

import static ca.mkiefte.Utilities.adjustVps;

import java.util.Set;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceFilter;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.Utilities;

public final class AllianceForProgress extends CardEvent {
	public static final String ID = "allianceforprogress;";
	public static final String DESCRIPTION = "Alliance for Progress*";

	public AllianceForProgress() {
		this(ID, null);
	}

	public AllianceForProgress(final String type, final GamePiece inner) {
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
	public Command myPlayEvent(final String who) {
		Command comm = super.myPlayEvent(who);
		final Set<GamePiece> pieces = Utilities.findAllPiecesMatching(new PieceFilter() {					
			public boolean accept(final GamePiece piece) {
				final Influence marker = Influence.getInfluenceMarker(piece);
				return marker != null 
						&& TSPlayerRoster.US.equals(marker.getSide())
						&& (Influence.CENTRAL_AMERICA.equals(marker.getRegion()) || Influence.SOUTH_AMERICA.equals(marker.getRegion()))
						&& marker.isBattleground()
						&& marker.hasControl();
			}
		});
		final int n = pieces.size();
		final Command chat;
		final Chatter chatter = GameModule.getGameModule().getChatter();
		if (n > 0) {
			final StringBuilder builder = new StringBuilder("US controls ").append(n);
			if (n > 1)
				builder.append(" Battleground countries");
			else
				builder.append(" Battleground country");
			builder.append(" in Central and South America.");
			chat = new Chatter.DisplayText(chatter, builder.toString());
		}
		else
			chat = new Chatter.DisplayText(chatter, "US controls no Battleground countries in Central or South America.");
		chat.execute();
		comm = comm.append(chat);
		comm = comm.append(adjustVps(+n));
		return comm;
	}
}
