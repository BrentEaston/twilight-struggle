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


public final class KitchenDebates extends CardEvent {
	public static final String ID = "kitchendebates;";
	public static final String DESCRIPTION = "Kitchen Debates*";
	
	private int americanBattlegrounds;
	private int sovietBattlegrounds;

	public KitchenDebates() {
		this(ID, null);
	}

	public KitchenDebates(final String type, final GamePiece inner) {
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
	public Command myPlayEvent(String who) {
		final Chatter chatter = GameModule.getGameModule().getChatter();		
		Command comm = super.myPlayEvent(who);		
		getNBattlegrounds();

		for (String side : new String[] {TSPlayerRoster.US, TSPlayerRoster.USSR}) {
			final int nBattlegrounds = TSPlayerRoster.US.equals(side) ? americanBattlegrounds : sovietBattlegrounds;
			final StringBuilder builder = new StringBuilder(side).append(" controls ").append(nBattlegrounds).append(" Battleground countries.");
			final Command chat = new Chatter.DisplayText(chatter, builder.toString());
			chat.execute();
			comm = comm.append(chat);
		}

		comm = comm.append(adjustVps(+2));
		final Command chat = new Chatter.DisplayText(chatter, "US player may poke opponent in the chest.");
		chat.execute();
		comm = comm.append(chat);
		
		return comm;
	}

	protected void getNBattlegrounds() {
		americanBattlegrounds = 0;
		sovietBattlegrounds = 0;
		final Set<GamePiece> pieces = Utilities.findAllPiecesMatching(new PieceFilter() {		
			public boolean accept(final GamePiece piece) {
				final Influence marker = Influence.getInfluenceMarker(piece);
				return marker != null && marker.isBattleground() && marker.hasControl();
			}
		});
		
		for (final GamePiece piece : pieces) {
			final Influence marker = Influence.getInfluenceMarker(piece);
			if (TSPlayerRoster.US.equals(marker.getSide()))
				++americanBattlegrounds;
			else
				++sovietBattlegrounds;
		}
	}

	@Override
	public boolean isEventPlayable(final String who) {
		getNBattlegrounds();
		return super.isEventPlayable(who) && americanBattlegrounds > sovietBattlegrounds;
	}
}
