package ca.mkiefte.cards;

import static ca.mkiefte.Utilities.adjustVps;

import java.util.HashSet;
import java.util.Set;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.command.NullCommand;
import VASSAL.counters.GamePiece;
import VASSAL.counters.KeyCommand;
import VASSAL.counters.PieceFilter;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.Utilities;

public abstract class ScoringCard extends CardEvent {
	public static final String REGION_MARKER = "RegionMarker";
	public static final String STATE = "State";
	public static final String NATION = "Nation";
	private static final String USSR_LAYER = "2";
	private static final String US_LAYER = "1";
	private static final String BOTH_LAYER = "3";
	private static final String NULL_LAYER = "0";
	public static final String CONTROL = "Control";
	public static final String DOMINATION = "Domination";
	public static final String PRESENCE = "Presence";
	private static final String CONTROL_LAYER = "3";
	private static final String DOMINATION_LAYER = "2";
	private static final String PRESENCE_LAYER = "1";

	protected static boolean gameOver;
	protected int nBattlegrounds;
	protected int sovietCountries;
	protected int americanCountries;
	protected int sovietBattlegrounds;
	protected int americanBattlegrounds;
	protected int sovietAdjacent;
	protected int americanAdjacent;
	protected GamePiece regionMarker;

	public ScoringCard(final String type, final GamePiece inner) {
		super(type, inner);
	}

	/* Region corresponding to the Scoring Card. Could return null in the case of a Scoring Card
	 * that does not represent a continent such as SouthEast Asia Scoring.
	 */
	public abstract String getRegion();
	
	protected abstract int getVpsForPresence();
	
	protected abstract int getVpsForDomination();
	
	protected abstract int getVpsForControl();
	
	protected int getVpsForStatus(final String status) {
		if (PRESENCE.equals(status))
			return getVpsForPresence();
		else if (DOMINATION.equals(status))
			return getVpsForDomination();
		else if (CONTROL.equals(status))
			return getVpsForControl();
		else
			return 0;
	}
		
	public Command calculateAndReportVps() {
		int vps = 0;	
		updateRegion(false);
		Command comm = new NullCommand();
		final String region = getRegion();
		final Chatter chatter = GameModule.getGameModule().getChatter();
		for (final String who : new String[] {TSPlayerRoster.USSR, TSPlayerRoster.US}) {
			final String status = getRegionStatus(who, true);
			final int sign = TSPlayerRoster.US.equals(who) ? 1 : -1;
			if (status != null) {
				final int bonus = getVpsForStatus(status);
				final StringBuilder builder = new StringBuilder(who).append(" has ").append(status).append(" in ").append(region);
				builder.append(" for ").append(bonus).append(" VP").append(bonus > 1 ? "s." : ".");
				comm = comm.append(new Chatter.DisplayText(chatter, builder.toString()));
				vps += bonus * sign;
			}
			else {
				final StringBuilder builder = new StringBuilder(who).append(" has no Presence in ").append(region).append(".");
				comm = comm.append(new Chatter.DisplayText(chatter, builder.toString()));
			}
			final int bg = getNBattlegrounds(who, true);
			if (bg > 0) {
				final StringBuilder builder = new StringBuilder(who).append(" controls ").append(bg).append(" Battleground ");
				if (bg > 1)
					builder.append(" Countries in ");
				else
					builder.append(" Country in ");
				builder.append(region).append(" for ").append(bg);
				if (bg > 1)
					builder.append(" VPs.");
				else
					builder.append(" VP.");
				comm = comm.append(new Chatter.DisplayText(chatter, builder.toString()));
				vps += bg * sign;
			}
			final int adj = getNAdjacent(who, true);
			if (adj > 0) {
				final StringBuilder builder = new StringBuilder(who).append(" controls ").append(adj);
				if (adj > 1)
					builder.append(" countries");
				else
					builder.append(" country");
				builder.append(" adjacent to ").append(TSPlayerRoster.US.equals(who) ? Influence.U_S_S_R : Influence.U_S_A).append(" for ").append(adj);
				if (adj > 1)
					builder.append(" VPs.");
				else
					builder.append(" VP.");
				comm = comm.append(new Chatter.DisplayText(chatter, builder.toString()));
				vps += adj * sign;
			}
		}
		comm.execute();
		return comm = comm.append(adjustVps(vps));
	}

	protected int getNCountries(final String who, final boolean scoring) {
		return TSPlayerRoster.US.equals(who) ? americanCountries : sovietCountries;
	}
	
	protected int getNBattlegrounds(final String who, final boolean scoring) {
		return TSPlayerRoster.US.equals(who) ? americanBattlegrounds : sovietBattlegrounds;
	}

	protected int getNAdjacent(final String who, final boolean scoring) {
		return TSPlayerRoster.US.equals(who) ? americanAdjacent : sovietAdjacent;
	}
	
	protected int getTotalBattlegrounds(final boolean scoring) {
		return nBattlegrounds;
	}

	final public boolean hasPresence(final String who, final boolean scoring) {
		return getNCountries(who, scoring) > 0;
	}
	
	final public boolean hasDomination(final String me, final boolean scoring) {
		final String op = TSPlayerRoster.US.equals(me) ? TSPlayerRoster.USSR : TSPlayerRoster.US;
		return getNCountries(me, scoring) > getNCountries(op, scoring)
				&& getNBattlegrounds(me, scoring) > getNBattlegrounds(op, scoring)
				&& getNCountries(me, scoring) > getNBattlegrounds(me, scoring);
	}
	
	final public boolean hasControl(final String me, final boolean scoring) {
		final String op = TSPlayerRoster.US.equals(me) ? TSPlayerRoster.USSR : TSPlayerRoster.US;
		return getNCountries(me, scoring) > getNCountries(op, scoring)
				&& getNBattlegrounds(me, scoring) == getTotalBattlegrounds(scoring);
	}
	
	final protected String getRegionStatus(final String who, final boolean scoring) {
		if (hasControl(who, scoring))
			return CONTROL;
		else if (hasDomination(who, scoring))
			return DOMINATION;
		else if (hasPresence(who, scoring))
			return PRESENCE;
		else
			return null;
	}
		
	final protected GamePiece getRegionMarker() {
		if (regionMarker == null) {
			final String region = getRegion();
			regionMarker = Utilities.findPiece(new PieceFilter() {
				public boolean accept(final GamePiece piece) {
					return region.equals(piece.getProperty(REGION_MARKER));
				}
			});
		}
		return regionMarker;
	}
	
	final public static ScoringCard getScoringCard(final String region) {
		final GamePiece gp = Utilities.findPiece(new PieceFilter() {			
			public boolean accept(final GamePiece piece) {
				final CardEvent card = CardEvent.getCard(piece);
				return card != null 
						&& card instanceof ScoringCard 
						&& region.equals(((ScoringCard) card).getRegion());
			}
		});
		return (ScoringCard) gp;
	}
	
	public static Command setGameOver(final boolean b) {
		final ChangeTracker tracker = new ChangeTracker(getCard(AsiaScoring.class));
		gameOver = true;
		return tracker.getChangeCommand();
	}

	public static boolean isGameOver() {
		return gameOver;
	}

	/* Counts number of controlled countries, controlled Battlegrounds, and countries adjacent to the
	 * enemy power that are controlled.  These values are stored as fields which can be later
	 * accessed by other methods. Finally, if updateMarker is true adjusts the control marker on the board
	 * to reflect the region status.
	 */
	final public Command updateRegion(final boolean updateMarker) {
		final String region = getRegion();
		if (region != null) {
			sovietCountries = 0;
			sovietBattlegrounds = 0;
			sovietAdjacent = 0;
			americanCountries = 0;
			americanBattlegrounds = 0;
			americanAdjacent = 0;
			nBattlegrounds = 0;
			
			final Set<GamePiece> markers = Utilities.findAllPiecesMatching(new PieceFilter() {
				public boolean accept(final GamePiece gp) {
					final Influence piece = Influence.getInfluenceMarker(gp);
					return piece != null 
							&& piece.getRegion().equals(region);
				}
			});
			
			for (final GamePiece gp : markers) {
				final Influence marker = Influence.getInfluenceMarker(gp);
				// count battlegrounds only once
				final String side = marker.getSide();
				
				if (marker.isBattleground() && TSPlayerRoster.US.equals(side))
					++nBattlegrounds; // only count each battleground once
				
				if (marker.hasControl()) {
					if (TSPlayerRoster.USSR.equals(side)) {
						++sovietCountries;
						if (marker.isBattleground())
							++sovietBattlegrounds;
						if (marker.isAdjacentToOpponentSuperpower())
							++sovietAdjacent;
					} else {
						++americanCountries;
						if (marker.isBattleground())
							++americanBattlegrounds;
						if (marker.isAdjacentToOpponentSuperpower())
							++americanAdjacent;
					}
				}
			}
			
			if (updateMarker) {
				final GamePiece rm = getRegionMarker();
				final ChangeTracker tracker = new ChangeTracker(rm);
				if (hasControl(TSPlayerRoster.USSR, false)) {
					rm.setProperty(NATION, USSR_LAYER);
					rm.setProperty(STATE, CONTROL_LAYER);
				} else if (hasControl(TSPlayerRoster.US, false)) {
					rm.setProperty(NATION, US_LAYER);
					rm.setProperty(STATE, CONTROL_LAYER);				
				} else if (hasDomination(TSPlayerRoster.USSR, false)) {
					rm.setProperty(NATION, USSR_LAYER);
					rm.setProperty(STATE, DOMINATION_LAYER);				
				} else if (hasDomination(TSPlayerRoster.US, false)) {
					rm.setProperty(NATION, US_LAYER);
					rm.setProperty(STATE, DOMINATION_LAYER);				
				} else if (hasPresence(TSPlayerRoster.US, false) && hasPresence(TSPlayerRoster.USSR, false)) {
					rm.setProperty(NATION, BOTH_LAYER);
					rm.setProperty(STATE, NULL_LAYER);				
				} else if (hasPresence(TSPlayerRoster.USSR, false)) {
					rm.setProperty(NATION, USSR_LAYER);
					rm.setProperty(STATE, PRESENCE_LAYER);			
				} else if (hasPresence(TSPlayerRoster.US, false)) {
					rm.setProperty(NATION, US_LAYER);
					rm.setProperty(STATE, PRESENCE_LAYER);				
				} else {
					rm.setProperty(NATION, NULL_LAYER);
					rm.setProperty(STATE, NULL_LAYER);				
				}

				return tracker.getChangeCommand();
			}
		} 
		
		return null;
	}

	@Override
	final public KeyCommand[] myGetKeyCommands() {
		final KeyCommand[] commands = super.myGetKeyCommands();
		if (commands.length == 0)
			return commands;
		int i;
		if (canPlayEvent()) {
			for (i = 0; i < commands.length; ++i) {
				if (commands[i].getKeyStroke().equals(PLAY_FOR_EVENT))
					break;
			}
			commands[i] = new KeyCommand("Score Region", PLAY_FOR_EVENT, getOutermost(this), true);
		} else if (canUndoEvent()) {
			for (i = 0; i < commands.length; ++i) {
				if (commands[i].getKeyStroke().equals(UNDO_KEY))
					break;
			}
			commands[i] = new KeyCommand("Undo Score Region", UNDO_KEY, getOutermost(this), true);
		} else {
			for (i = 0; i < commands.length; ++i) {
				if (commands[i].getKeyStroke().equals(PLAY_FOR_EVENT))
					break;
			}
			commands[i] = new KeyCommand("Cannot Score Region", PLAY_FOR_EVENT, getOutermost(this), false);
		}
		return commands;
	}

	@Override
	public Command myPlayEvent(String who) {
		Command comm = super.myPlayEvent(who);
		comm = comm.append(calculateAndReportVps());
		return comm;
	}

	@Override
	final protected Command sendEventMessage() {
		final Chatter chatter = GameModule.getGameModule().getChatter();
		final StringBuilder message = new StringBuilder("*** ").append(getOwner()).append(" scores ").append(getRegion()).append('.');
		Command comm = new Chatter.DisplayText(chatter, message.toString());
		comm.execute();
		return comm;
	}

	final public int calculateVps() {
		int total = 0;
		for (final String side: new String[] {TSPlayerRoster.US, TSPlayerRoster.USSR}) {
			int subtotal = 0;
			if (hasControl(side, true))
				subtotal += getVpsForControl();
			else if (hasDomination(side, true))
				subtotal += getVpsForDomination();
			else if (hasPresence(side, true))
				subtotal += getVpsForPresence();
			subtotal += getNBattlegrounds(side, true);
			subtotal += getNAdjacent(side, true);
			subtotal *= TSPlayerRoster.US.equals(side) ? 1 : -1;
			total += subtotal;
		}
		return total;
	}

	@Override
	final protected boolean canPlayOps() {
		return false;
	}

	@Override
	final protected boolean canPlaySpaceRace() {
		return false;
	}
	
	public static Set<ScoringCard> getScoringCards() {
		Set<GamePiece> cards = Utilities.findAllPiecesMatching(new PieceFilter() {		
			public boolean accept(final GamePiece piece) {
				final CardEvent card = getCard(piece);
				return card != null && card.isScoringCard() && !(card instanceof SouthEastAsiaScoring);
			}
		});
		final Set<ScoringCard> scoringCards = new HashSet<ScoringCard>(cards.size());
		for (final GamePiece gp : cards)
			scoringCards.add((ScoringCard) getCard(gp));
		return scoringCards;
	}

	@Override
	public int getOps() {
		return 0;
	}
}