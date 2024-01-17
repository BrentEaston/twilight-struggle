package ca.mkiefte;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.BasicLogger;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.PlayerRoster.SideChangeListener;
import VASSAL.build.module.properties.MutableProperty;
import VASSAL.build.module.turn.TurnTracker;
import VASSAL.command.Command;
import VASSAL.command.NullCommand;
import VASSAL.configure.NamedHotKeyConfigurer;
import VASSAL.counters.Deck;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceFilter;
import VASSAL.i18n.Resources;
import VASSAL.tools.IconButton;
import VASSAL.tools.NamedKeyStroke;
import VASSAL.tools.SequenceEncoder;
import ca.mkiefte.cards.BearTrap;
import ca.mkiefte.cards.CardEvent;
import ca.mkiefte.cards.CiaCreated;
import ca.mkiefte.cards.CubanMissileCrisis;
import ca.mkiefte.cards.LoneGunman;
import ca.mkiefte.cards.MissileEnvy;
import ca.mkiefte.cards.Norad;
import ca.mkiefte.cards.NorthSeaOil;
import ca.mkiefte.cards.Quagmire;
import ca.mkiefte.cards.RevealHandEvent;
import ca.mkiefte.cards.ScoringCard;
import ca.mkiefte.cards.TheChinaCard;
import ca.mkiefte.cards.Wargames;

/**
 * Customized TurnTracker.
 * Checks whether Turn/Round counter can advance and whether any actions need to be performed.
 * 
 * @author Michael Kiefte
 * Copyright 2010 Michael Kiefte.
 */
public final class TSTurnTracker extends TurnTracker implements SideChangeListener {
	
	private static final String ROUND_MARKER = "Round Marker";
	private static final String TURN_MARKER = "Turn Marker";
	private static final String ROUND = "Round";
	private static final String TURN = "Turn";
	private static HashSet<UpdateStateCommand> probes = new HashSet<UpdateStateCommand>();
	private static boolean alerted;
	public static final String MID_WAR_DECK_NAME = "Mid War Cards";
	public static final String LATE_WAR_DECK_NAME = "Late War Cards";
	private static final String SPACE_RACE_DISCARDED_CARD = "SpaceRaceDiscardedCard";
	protected MutableProperty.Impl myValue = new MutableProperty.Impl("PhasingPlayer", this); //$NON-NLS-1$
	private static final String dealer = TSPlayerRoster.USSR;
	private int turn = 1;
	private int round = 0;
	private String phasingPlayer = null;
	private static boolean turnAdvanceAlert = false;

	private Set<TSTimer> timers = new HashSet<TSTimer>();
	
	protected class TSTurnWidget extends TurnWidget {
		private static final long serialVersionUID = 29672439434789298L;
		private IconButton nextButton;
		
		@Override
	    public void setNextStroke(NamedKeyStroke key) {
			final String tooltip = Resources.getString("TurnTracker.next_turn") +
					(key == null ? "" : " " + NamedHotKeyConfigurer.getFancyString(key));
			nextButton.setToolTipText(tooltip);
		}

		@Override
	    public void setPrevStroke(NamedKeyStroke key) {
		}

		@Override
		protected void initComponents() {
			setLayout(new BorderLayout(5, 5));
			
			nextButton = new IconButton(IconButton.PLUS_ICON, 22);
			setNextStroke(nextListener.getNamedKeyStroke());
			nextButton.setAlignmentY(Component.TOP_ALIGNMENT);
			nextButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					final Command comm = myDoNext();
					GameModule.getGameModule().sendAndLog(comm);
				}});

			// Next, the Label containing the Turn Text
			turnLabel.setFont(getDisplayFont());
			turnLabel.setFocusable(false);
			turnLabel.setHorizontalTextPosition(JLabel.CENTER);
			turnLabel.setHorizontalAlignment(SwingConstants.CENTER);
			turnLabel.setBackground(Color.WHITE);

			add(turnLabel, BorderLayout.CENTER);
			add(nextButton, BorderLayout.LINE_END);

			addMouseListener(this);
		}	
	}

	public static class UpdateStateCommand extends Command {
		public static final String COMMAND_PREFIX = "UPDATE_STATE:";
		private long key;
		private long seed;
		
		UpdateStateCommand() {
			this(System.currentTimeMillis(), GameModule.getGameModule().getRNG().nextLong());
		}
		
		UpdateStateCommand(long key, long seed) {
			this.key = key;
			this.seed = seed;
		}

		@Override
		protected void executeCommand() {
			Command comm = TSTurnTracker.updateState();
			if (!(comm instanceof UpdateStateCommand) && comm != null && !comm.isNull())
				GameModule.getGameModule().sendAndLog(comm);
		}

		@Override
		protected Command myUndoCommand() {
			return null;
		}

		@Override
		public boolean isLoggable() {
			return false;
		}

		@Override
		public void execute() {
			if (probes.contains(this)) {
				super.execute();
				final Iterator<UpdateStateCommand> iter = probes.iterator();
				while(iter.hasNext()) {
					final UpdateStateCommand comm = iter.next();
					if (comm.key <= key)
						iter.remove();
				}
			} else
				probes.add(this);
		}

		@Override
		public boolean equals(final Object arg) {
			if (!(arg instanceof UpdateStateCommand))
				return false;
			return ((UpdateStateCommand) arg).key == key && ((UpdateStateCommand) arg).seed == seed;
		}

		@Override
		public int hashCode() {
			return Long.valueOf(seed).hashCode();
		}		
	}

	public static int getCurrentTurn() {
		return getInstance().turn;
	}
	
	public static int getCurrentRound() {
		return getInstance().round;
	}
	
	// return the phasing player
	// will return null if it's the headline phase
	public static String getPhasingPlayer() {
		if (getCurrentRound() == 0) 
			return null;
		else
			return GameModule.getGameModule().getComponentsOf(TSTurnTracker.class).get(0).phasingPlayer;
	}
	
	public TSTurnTracker() {
		super();
	    turnWidget = new TSTurnWidget();
	}
	
	static TSTurnTracker getInstance() {
		return GameModule.getGameModule().getAllDescendantComponentsOf(TSTurnTracker.class).get(0);
	}

	@Override
	protected String getTurnString() {
		if (round == 0)
			return new StringBuilder("Turn ").append(turn).append(" Headline Phase").toString();
		return new StringBuilder("Turn ").append(turn).append(" Round ").append(round).append(" (").append(phasingPlayer).append(")").toString();
	}

	@Override
	public void addTo(Buildable parent) {
		super.addTo(parent);		
//		launch.setVisible(false);
		myValue.addTo(GameModule.getGameModule());
		GameModule.getGameModule().addSideChangeListenerToPlayerRoster(this);
	}

	@Override
	public void removeFrom(Buildable parent) {
		super.removeFrom(parent);
		myValue.removeFromContainer();
	}

	private static Command checkMilitaryOperationsStatus() {
		Command comm = new NullCommand();
		final int defcon = Integer.valueOf(Utilities.getGlobalProperty(Utilities.DEFCON).getPropertyValue());
		int vps = 0;
		final GameModule gameModule = GameModule.getGameModule();
		final Chatter chatter = gameModule.getChatter();
		for (String who : new String[] {TSPlayerRoster.USSR, TSPlayerRoster.US}) {		
			final String militaryOpsPropName = TSPlayerRoster.USSR.equals(who) ? Utilities.SOVIET_MILITARY_OPS_PROP_NAME : Utilities.AMERICAN_MILITARY_OPS_PROP_NAME;
			final int militaryOps = Integer.valueOf(Utilities.getGlobalProperty(militaryOpsPropName).getPropertyValue());
			if (militaryOps < defcon) {
				StringBuilder builder = new StringBuilder(who).append(" did not perform enough Military Operatins.");
				Command chat = new Chatter.DisplayText(chatter, builder.toString());
				final int penalty = defcon - militaryOps;
				builder = new StringBuilder(who).append(" penalized ").append(penalty).append(" VP");
				if (penalty > 1)
					builder.append("s.");
				else
					builder.append(".");
				chat = chat.append(new Chatter.DisplayText(chatter, builder.toString()));
				chat.execute();
				comm = comm.append(chat);
				vps += penalty * (TSPlayerRoster.US.equals(who) ? -1 : 1);
			}
			comm = comm.append(Utilities.adjustMilitaryOps(-militaryOps, who));
		}
		if (vps != 0)
			comm = comm.append(Utilities.adjustVps(vps));
		return comm;
	}

	private Command finalScoring() {
		final GameModule gameModule = GameModule.getGameModule();
		final Chatter chatter = gameModule.getChatter();
		Command chat = new Chatter.DisplayText(chatter, "!!! FINAL SCORING !!!");
		int vps = 0;
		
		final ScoringCard europeScoring = ScoringCard.getScoringCard(Influence.EUROPE);
		for (final String side : new String[] {TSPlayerRoster.US,TSPlayerRoster.USSR}) {
			if (europeScoring.hasControl(side, true)) {
				final Command comm = ScoringCard.setGameOver(true);
				final StringBuilder builder = new StringBuilder("!!! ").append(side).append(" Controls Europe -- ").append(side).append(" wins !!!");
				chat = chat.append(new Chatter.DisplayText(chatter, builder.toString()));
				chat.execute();
				return comm.append(chat);
			}
		} 
		
		for (final ScoringCard scoring : ScoringCard.getScoringCards()) {
			final int bonus = scoring.calculateVps();
			final StringBuilder builder = new StringBuilder("");
			if (bonus != 0) {
				builder.append(bonus > 0 ? "US" : "USSR").append(" receives ").append(Math.abs(bonus)).append(" VP");
				if (Math.abs(bonus) > 1)
					builder.append('s');
				builder.append(" for ").append(scoring.getRegion()).append('.');
			} else
				builder.append("No one receives VPs for ").append(scoring.getRegion()).append('.');
			chat = chat.append(new Chatter.DisplayText(chatter, builder.toString()));
			vps += bonus;
		}

		final String owner = CardEvent.getCard(TheChinaCard.class).getOwner();
		if (TSPlayerRoster.USSR.equals(owner) || TSPlayerRoster.US.equals(owner)) {
			vps += TSPlayerRoster.USSR.equals(owner) ? -1 : +1;	
			final StringBuilder builder = new StringBuilder(owner).append(" has The China Card for 1 VP.");
			chat = chat.append(new Chatter.DisplayText(chatter, builder.toString()));
		}
		else
			chat = chat.append(new Chatter.DisplayText(chatter, "No one has The China Card."));
		
		chat.execute();
		
		Command comm;
		if (vps != 0)
			comm = chat.append(Utilities.adjustVps(vps));
		else
			comm = chat;

		vps = Integer.valueOf(Utilities.getGlobalProperty(Utilities.VPS).getPropertyValue());
		if (TSOptions.isLateWarScenario()) {
			if (vps < 20)
				chat = new Chatter.DisplayText(chatter, "!!! USSR Victory: " + Math.abs(vps) + (vps <= 0 ? " Soviet" : " American") + " VPs !!!");
			else
				chat = new Chatter.DisplayText(chatter, "!!! US Vicotry: 20 VPs !!!");
		} else {
			if (vps < 0)
				chat = new Chatter.DisplayText(chatter, "!!! USSR Victory: " + (-vps) + " VPs !!!");
			else if (vps > 0)
				chat = new Chatter.DisplayText(chatter, "!!! US Victory: " + vps + " VPs !!!");
			else 
				chat = new Chatter.DisplayText(chatter, "!!! Draw: 0 VPs !!!");
		}
		chat.execute();
		
		comm = comm.append(chat);
		comm = comm.append(ScoringCard.setGameOver(true));
		return comm;
	}

	private static Command revealHeldCards() {
		final String[] hands = {TSPlayerRoster.USSR, TSPlayerRoster.US};
		final GameModule gameModule = GameModule.getGameModule();
		final Chatter chatter = gameModule.getChatter();
		for (String hand : hands) {
			final Set<CardEvent> cards = CountingPlayerHand.getHand(hand).getAllCards();
			for (CardEvent card : cards) {
				if (card.isScoringCard()) {
					StringBuilder builder = new StringBuilder("!!! ").append(card.getDescription()).append(" still in ").append(hand).append(" Hand !!!");
					Command comm = new Chatter.DisplayText(chatter, builder.toString());
					builder = new StringBuilder(hand).append(" has lost the game.");
					comm = comm.append(new Chatter.DisplayText(chatter, builder.toString()));
					comm.execute();
					comm = comm.append(ScoringCard.setGameOver(true));
					return comm;
				}
			}
		}
		return null;
	}
	
	protected Command myNext() {
		return myNext(false);
	}
	
	protected Command myNext(final boolean override) {
		Command comm = new NullCommand();

		if (!override && !getCurrentState(TSPlayerRoster.getMySide()).equals(CurrentState.ADVANCE_TURN))
			return comm;
		
		// check Norad
		final Norad norad = CardEvent.getCard(Norad.class);
		if (norad.bonusThisRound() && norad.isEventInEffect()) {
			if (Influence.getInfluenceMarker(Influence.CANADA, TSPlayerRoster.US).hasControl()) {
				final Chatter chatter = GameModule.getGameModule().getChatter();
				final Command chat = new Chatter.DisplayText(chatter, "US player to place Influence from NORAD Event.");
				chat.execute();
				comm = comm.append(chat);
				comm = comm.append(norad.setup());
				return comm;
			}
		}
		comm = comm.append(norad.setBonusThisRound(false));
		
		// clear and discard any played cards.
		for (String prop : new String[] {CardEvent.AMERICAN_CARD_PLAYED, CardEvent.SOVIET_CARD_PLAYED}) {
			final MutableProperty globalProperty = Utilities.getGlobalProperty(prop);
			final String cardPlayed = globalProperty.getPropertyValue();
			if (!cardPlayed.equals(Utilities.FALSE)) {
				comm = comm.append(globalProperty.setPropertyValue(Utilities.FALSE));
				if (Integer.valueOf(cardPlayed) != -1)
					comm = comm.append(CardEvent.getCard(Integer.valueOf(cardPlayed)).discard());
			}
		}
		
		comm = comm.append(Influence.setAllAtStartInfluence());
		
		for (final RevealHandEvent event : new RevealHandEvent[] {
				CardEvent.getCard(CiaCreated.class), 
				CardEvent.getCard(LoneGunman.class)
				}) {
			if (event.isEventInEffect())
				comm = comm.append(event.hideOpponentsHand());
		}
		
		boolean nextTurn = false;
		if (round == 0) {
			round = 1;
			phasingPlayer = TSPlayerRoster.USSR;
		} else if (TSPlayerRoster.USSR.equals(phasingPlayer)) {
			if (getNRoundsRemaing(TSPlayerRoster.US) > 0)
				phasingPlayer = TSPlayerRoster.US;
			else if (getNRoundsRemaing(TSPlayerRoster.USSR) > 1)
				++round;
			else 
				nextTurn = true;
		} else if (TSPlayerRoster.US.equals(phasingPlayer)) {
			if (getNRoundsRemaing(TSPlayerRoster.USSR) > 0) {
				++round;
				phasingPlayer = TSPlayerRoster.USSR;
			} else if (getNRoundsRemaing(TSPlayerRoster.US) > 1)
				++round;
			else 
				nextTurn = true;
		}
		
		if (nextTurn) {
			final Command held = revealHeldCards();
			if (held != null)
				return comm.append(held);
	
			comm = comm.append(checkMilitaryOperationsStatus());
			if (isGameOver())
				return comm;

			comm = comm.append(CardEvent.getCard(TheChinaCard.class).setAvailable(true));
			
			if (turn < 10) {
				++turn;
				round = 0;
				phasingPlayer = null;

				if (turn == 4) {
					final Deck deck = CardEvent.getDeckNamed(MID_WAR_DECK_NAME);
					comm = comm.append(deck.sendToDeck());
					comm = comm.append(setCurrentPositionInDeck(CardEvent.DRAW_DECK));
				} else if (turn == 8) {
					final Deck deck = CardEvent.getDeckNamed(LATE_WAR_DECK_NAME);
					comm = comm.append(deck.sendToDeck());
					comm = comm.append(setCurrentPositionInDeck(CardEvent.DRAW_DECK));
				}

				for (final String side : new String[] {TSPlayerRoster.US, TSPlayerRoster.USSR})
					comm = comm.append(Utilities.setCardPlayedOnSpaceRace(side, 0));
				comm = comm.append(Utilities.adjustDefcon(+1));				
				comm = comm.append(checkSpaceRaceDiscard());
			} else
				comm = comm.append(finalScoring());
		} 		
		
		comm = comm.append(setActionRound(turn, round, phasingPlayer));
		return comm;		
	}
	
	public static Command setCurrentPositionInDeck(final String deckName) {
		final Deck deck = CardEvent.getDeckNamed(deckName);
		Iterator<GamePiece> iter = deck.getPiecesIterator();
		Command comm = new NullCommand();
		while (iter.hasNext()) {
			final CardEvent event = CardEvent.getCard(iter.next());
			comm = comm.append(event.setCurrentPosition());
		}
		return comm;
	}
	
	private static Command checkSpaceRaceDiscard() {
		int usSpaceRace = Integer.valueOf(Utilities.getGlobalProperty(Utilities.AMERICAN_SPACE_RACE).getPropertyValue());
		int ussrSpaceRace = Integer.valueOf(Utilities.getGlobalProperty(Utilities.SOVIET_SPACE_RACE).getPropertyValue());
		final MutableProperty discarded = Utilities.getGlobalProperty(SPACE_RACE_DISCARDED_CARD);
		if (usSpaceRace >= 6 
				&& ussrSpaceRace < 6 
				&& CountingPlayerHand.getHand(TSPlayerRoster.US).getAllCards().size() > 0)
			return discarded.setPropertyValue(TSPlayerRoster.US);
		else if (ussrSpaceRace >= 6 
				&& usSpaceRace < 6 
				&& CountingPlayerHand.getHand(TSPlayerRoster.USSR).getAllCards().size() > 0)
			return discarded.setPropertyValue(TSPlayerRoster.USSR);
		else
			return null;
	}
	
	public Command setActionRound(final int turn, final int round, final String phasingPlayer) {
		this.turn = turn;
		this.round = round;
		this.phasingPlayer = phasingPlayer;
		updateTurnDisplay(NEXT);
		GamePiece turnMarker = getTurnMarker();
		GamePiece turnDestination = Utilities.getPieceNamed(new StringBuilder(TURN).append(' ').append(turn).toString());
		Command comm = turnDestination.getMap().placeAt(turnMarker, turnDestination.getPosition());
		
		GamePiece roundMarker = getRoundMarker();
		GamePiece roundDestination = Utilities.getPieceNamed(new StringBuilder(ROUND).append(' ').append(round).toString());
		comm = comm.append(roundDestination.getMap().placeAt(roundMarker, roundDestination.getPosition()));
		setPhasingLevel();
		return comm;
	}
	
	protected GamePiece getTurnMarker() {
		return Utilities.getPieceNamed(TURN_MARKER);
	}

	protected GamePiece getRoundMarker() {
		return Utilities.getPieceNamed(ROUND_MARKER);
	}

	public static Command dealCards() {
		synchronized(getInstance()) {
			final CountingPlayerHand sovietHand = CountingPlayerHand.getHand(TSPlayerRoster.USSR);
			final CountingPlayerHand americanHand = CountingPlayerHand.getHand(TSPlayerRoster.US);
			int nSoviet = sovietHand.countCards();
			int nAmerican = americanHand.countCards();
			final int target = isEarlyWar() ? 8 : 9;
			if (nSoviet == target && nAmerican == target)
				return null;
			Command comm = new NullCommand();
			while (nSoviet < target || nAmerican < target) {
				if (nSoviet < target) {
					comm = comm.append(sovietHand.dealCard());
					++nSoviet;
				}
				if (nAmerican < target) {
					comm = comm.append(americanHand.dealCard());
					++nAmerican;
				}
			}

			// ensure redraw
			//		comm.execute();
			sovietHand.redrawCardCount();
			americanHand.redrawCardCount();

			final Chatter chatter = GameModule.getGameModule().getChatter();
			final Command chat = new Chatter.DisplayText(chatter, "Cards dealt.");
			chat.execute();

			comm = comm.append(chat);

			return comm;
		}
	}
	
	private void setPhasingLevel() {
		final String phaseLevel;
		if (phasingPlayer == null || "".equals(phasingPlayer))
			phaseLevel = "0";
		else if (TSPlayerRoster.USSR.equals(phasingPlayer))
			phaseLevel = "1";
		else
			phaseLevel = "2";
		myValue.setPropertyValue(phaseLevel);
	}

	@Override
	public Command decode(final String command) {
		if (command.startsWith(UpdateStateCommand.COMMAND_PREFIX)) {
			final SequenceEncoder.Decoder decoder = new SequenceEncoder.Decoder(command, ':');
			decoder.nextToken();		
			long key = decoder.nextLong(0);
			long seed = decoder.nextLong(GameModule.getGameModule().getRNG().nextLong());
			UpdateStateCommand comm = new UpdateStateCommand(key, seed);
			return comm;
		} else
			return super.decode(command);
	}

	@Override
	public String encode(final Command c) {
		if (c instanceof UpdateStateCommand) {
			final UpdateStateCommand comm = (UpdateStateCommand) c;
			final SequenceEncoder encoder = new SequenceEncoder(':');
			encoder.append(comm.key);
			encoder.append(comm.seed);
			return UpdateStateCommand.COMMAND_PREFIX + encoder.getValue();
		} else 
			return super.encode(c);
	}

	// how many rounds for the given person
	protected static int getNumberOfRounds(final String who) {
		if (getCurrentTurn() < 4)
			return 6;
		final int americanSpaceRace = Integer.valueOf((String) Utilities.getGlobalProperty(Utilities.AMERICAN_SPACE_RACE).getPropertyValue());
		final int sovietSpaceRace = Integer.valueOf((String) Utilities.getGlobalProperty(Utilities.SOVIET_SPACE_RACE).getPropertyValue());
		if (TSPlayerRoster.US.equals(who) && americanSpaceRace == 8 && sovietSpaceRace < 8
				|| TSPlayerRoster.USSR.equals(who) && sovietSpaceRace == 8 && americanSpaceRace < 8)
			return 8;
		else if (TSPlayerRoster.US.equals(who) && CardEvent.getCard(NorthSeaOil.class).isEventPlayedThisTurn())
			return 8;
		else
			return 7;
	}

	// number of rounds remaining including this one
	public static int getNRoundsRemaing(final String who) {
		final int numberOfRounds = getNumberOfRounds(who);
		final int currentRound = getCurrentRound();
		if (currentRound == 0)
			return numberOfRounds;
		int nRounds = numberOfRounds - currentRound + 1;
		/* 
		 * If this is the American round and we're asking about the number
		 * of rounds the Soviet player gets, then we don't add an extra round
		 * because when the Soviet round comes around again, we'll have added
		 * one to the round.
		 */
		if (TSPlayerRoster.USSR.equals(who) && TSPlayerRoster.US.equals(getPhasingPlayer()))
			--nRounds;
		return nRounds;
	}

	@Override
	public String getState() {
	    final SequenceEncoder se = new SequenceEncoder('|');
	    se.append(turn);
	    se.append(round);
	    se.append(phasingPlayer);
	    return se.getValue();
	}

	@Override
	public void setState(String newState) {
		final SequenceEncoder.Decoder sd = new SequenceEncoder.Decoder(newState, '|');
		turn = sd.nextInt(1);
		if (turn == 0)
			turn = 1;
		round = sd.nextInt(0);
		phasingPlayer = sd.nextToken(null);

		setPhasingLevel();
		setLaunchToolTip();
		updateTurnDisplay(SET);
	}

	protected Command updateRegionMarkers() {
		Command comm = new NullCommand();

		final Set<GamePiece> pieces = Utilities.findAllPiecesMatching(new PieceFilter() {		
			public boolean accept(final GamePiece piece) {
				final CardEvent event = CardEvent.getCard(piece);
				if (event != null && event instanceof ScoringCard)
					return true;
				else
					return false;
			}
		});
		
		for (GamePiece gp : pieces)
			comm = comm.append(((ScoringCard) gp).updateRegion(true));
		return comm;
	}
	
	public enum CurrentState {
		DEAL_CARDS("deal cards"),
		PLACE_STARTING_INFLUENCE("place starting influence"),
		PLACE_EXTRA_INFLUENCE("place extra influence"),
		CHOOSE_HEADLINE_CARD("choose headline card (right-click on card)"),
		FLIP_HEADLINE_CARD("flip headline cards"),
		PLAY_EVENT("play card for event (right-click on card)"),
		ADVANCE_TURN("advance Turn Tracker (in the toolbar)"),
		WAITING("wait for opponent"), 
		PLAY_CARD("play a card (right-click on card)"), 
		PLAY_OPS("play card for operations (right-click on card)"),
		CONDUCT_OPERATIONS("conduct Operations"),
		EVENT_NOT_FINISHED("finish event"), 
		DISCARD_CARD("discard card"), 
		GAME_OVER(null), 
		QUAGMIRE("discard card due to Quagmire"), 
		BEAR_TRAP("discard card due to Bear Trap"); 
		
		public String description;
		
		CurrentState(final String description) {
			this.description = description;
		}
		
		public String toString() {
			return description;
		}
	}
	
	protected void addTimer(TSTimer timer) {
		timers.add(timer);
	}
	
	protected void removeTimer(TSTimer timer) {
		timers.remove(timer);
	}
	
	public static Command updateState() {
		// make sure dialogs are closed
		Influence.disposeInluenceDialog();
		
		final String me = TSPlayerRoster.getMySide();
		CurrentState state = getCurrentState(me);

		// observer
		if (state == null) 
			return null;
		
		Command comm = new NullCommand();
		
		Chatter chatter = GameModule.getGameModule().getChatter();

		if (state == CurrentState.ADVANCE_TURN) {
				if (Utilities.TRUE.equals(Utilities.getGlobalProperty("AtomaticAdvance").getPropertyValue()))
					return myDoNext().append(updateState());
				else if (!turnAdvanceAlert) {
					new Chatter.DisplayText(GameModule.getGameModule().getChatter(), 
							"- Player->Preferences->Twilight Struggle to automatically advance turn tracker.").execute();
					turnAdvanceAlert = true;
				}
		}
		
		if (state == CurrentState.DEAL_CARDS) {
			comm = comm.append(dealCards());
			state = getCurrentState(me);
		} 
		
		// still has to place starting influence
		if (state == CurrentState.PLACE_STARTING_INFLUENCE) {
			final String propertyName = TSPlayerRoster.US.equals(me) ? Influence.AMERICAN_STARTING_INFLUENCE : Influence.SOVIET_STARTING_INFLUENCE;
			final String where = TSPlayerRoster.US.equals(me) ? "Western Europe" : "Eastern Europe";
			final StringBuilder builder = new StringBuilder("Place Starting Influence anywhere in ").append(where).append('.');
			Influence.getInfluenceDialog("Place Starting Influence", builder.toString(), 
					new Influence.Delegate(null, propertyName) {
						@Override
						public Command finish() {
							return super.finish().append(Influence.setAllAtStartInfluence());
						}

						@Override
						public boolean canIncreaseInfluence(final Influence inf) {
							if (!super.canIncreaseInfluence(inf))
								return false;
							final Set<String> region = TSPlayerRoster.US.equals(me) ? Influence.WESTERN_EUROPE : Influence.EASTERN_EUROPE;
							return region.contains(inf.getLocation());
						}
			});
		// one side still has to place bonus influence
		} else if (state == CurrentState.PLACE_EXTRA_INFLUENCE) {
			if (Influence.getExtraInfluenceRemaining() == 0) {
				final int totalPlaced = Influence.getTotalInfluencePlaced(me);
				if (totalPlaced == 0) {
					comm = comm.append(Utilities.getGlobalProperty(Influence.BONUS_INFLUENCE).setPropertyValue(Integer.toString(-1)));
					state = getCurrentState(me);
				}
			} else {
				final String message = "Place Extra Influence anywhere with existing influence\n"
						+ "up 2 more than needed for control.";
				Influence.getInfluenceDialog("Place Extra Influence", message, new Influence.Delegate(null, Influence.BONUS_INFLUENCE) {
					@Override
					public Command finish() {
						Command comm = super.finish();
						return comm.append(Influence.setAllAtStartInfluence());
					}

					@Override
					public boolean canIncreaseInfluence(Influence inf) {
						if (!super.canIncreaseInfluence(inf))
							return false;
						if (inf.getInfluence() == 0)
							return false;
						return inf.getInfluence() < inf.getOpponentInfluence() + inf.getStability() + 2;
					}
				});
			}
		}
		
		if (state == CurrentState.FLIP_HEADLINE_CARD) {
			comm = comm.append(CardEvent.flipHeadlineCards());
			state = getCurrentState(me);
		} 
		
		if (state == CurrentState.DISCARD_CARD) {
			final MutableProperty globalProperty = Utilities.getGlobalProperty(SPACE_RACE_DISCARDED_CARD);
			comm = comm.append(CardEvent.selectDiscardCard(
					me, 
					new StringBuilder(TSPlayerRoster.US.equals(me) ? "Eagle" : "Bear").append(" has Landed").toString(), 
					"Select a held card to discard or\npress \"Cancel\" to continue to hold.", 
					null, 
					true));
			comm = comm.append(globalProperty.setPropertyValue(Utilities.FALSE));
			state = getCurrentState(me);
		}

		if (state == CurrentState.PLAY_CARD || state == CurrentState.QUAGMIRE || state == CurrentState.BEAR_TRAP) {
			final CountingPlayerHand hand = CountingPlayerHand.getHand(me);			
			final TheChinaCard chinaCard = CardEvent.getCard(TheChinaCard.class);
			if (getCurrentRound() == 8
					&& (hand.getAllCards().size() > 0
							|| chinaCard.getOwner().equals(me) && !chinaCard.isFlipped())) {
				hand.getView().getTopLevelAncestor().setVisible(true);
				final int n = JOptionPane.showConfirmDialog(GameModule.getGameModule().getPlayerWindow(),
						"Would you like to play another Action Round?",
						"Eighth Action Round",
						JOptionPane.YES_NO_OPTION);
				if (n != JOptionPane.YES_OPTION) {
					final Command chat = new Chatter.DisplayText(chatter, 
							new StringBuilder(me).append(" does not play eighth Action Round.").toString());
					chat.execute();
					comm = comm.append(chat);
					comm = comm.append(pass(me));
					state = getCurrentState(me);
				}
			}
			
			if (state == CurrentState.QUAGMIRE) {
				comm = comm.append(CardEvent.getCard(Quagmire.class).discardCard());
				state = getCurrentState(me);
			} else if (state == CurrentState.BEAR_TRAP) {
				comm = comm.append(CardEvent.getCard(BearTrap.class).discardCard());
				state = getCurrentState(me);
			} else if (state == CurrentState.PLAY_CARD) {
				final MissileEnvy missileEnvy = CardEvent.getCard(MissileEnvy.class);
				if (missileEnvy.isEventInEffect() 
						&& getCurrentRound() > 0
						&& missileEnvy.getTargetedPlayer().equals(me)) {
					JOptionPane.showMessageDialog(GameModule.getGameModule().getPlayerWindow(),
							new StringBuilder("You must play ").append(missileEnvy.getDescription()).append(" for Ops.").toString(), 
							missileEnvy.getDescription(), 
							JOptionPane.INFORMATION_MESSAGE, 
							missileEnvy.asIcon());
					comm = comm.append(missileEnvy.playOps(me));
					state = getCurrentState(me);
				} else if (hand.getAllCards().size() == 0) {
					if (chinaCard.getOwner().equals(me) && !chinaCard.isFlipped()) {
						final int n = JOptionPane.showConfirmDialog(GameModule.getGameModule().getPlayerWindow(),
								"Do you wish to play The China Card?", 
								"The China Card", 
								JOptionPane.YES_NO_OPTION);
						if (n == JOptionPane.YES_OPTION) {
							comm = comm.append(chinaCard.playOps());
							state = getCurrentState(me);
						} else {
							final StringBuilder builder = new StringBuilder(me).append(" does not play The China Card.");
							Command chat = new Chatter.DisplayText(chatter, builder.toString());
							chat.execute();
							comm = comm.append(chat);
							comm = comm.append(pass(me));
							state = getCurrentState(me);
						}
					} else {
						final StringBuilder builder = new StringBuilder(me).append(" has no cards left to play.");
						Command chat = new Chatter.DisplayText(chatter, builder.toString());
						chat.execute();
						comm = comm.append(chat);
						comm = comm.append(pass(me));
						state = getCurrentState(me);
					}
				}
			}
		}
		
		if (state == CurrentState.CONDUCT_OPERATIONS) {
			final String propertyName = TSPlayerRoster.US.equals(me) ? CardEvent.AMERICAN_CARD_PLAYED : CardEvent.SOVIET_CARD_PLAYED;
			final String propertyValue = Utilities.getGlobalProperty(propertyName).getPropertyValue();
			final CardEvent card = CardEvent.getCard(Integer.valueOf(propertyValue));
			Influence.getInfluenceDialog("Conduct Operations", new Influence.ConductOperationsDelegate(null) {
				@Override
				public Command cancel() {
					Command comm = super.cancel();
					comm = comm.append(card.undoPlayOps(who));
					return comm;
				}

				@Override
				public boolean canCancel() {
					return card.canUndoPlayOps();
				}
			});
		}
		
		// there's an event that isn't finished
		if (state == CurrentState.EVENT_NOT_FINISHED) {
			final CardEvent cardCurrentlyPlaying = CardEvent.eventCurrentlyPlaying();
			comm = comm.append(cardCurrentlyPlaying.updateState());
			state = getCurrentState(me);
		}
		
		if (state == CurrentState.GAME_OVER) {
			final Command chat = new Chatter.DisplayText(chatter, "*** Game Over ***");
			chat.execute();
		} else {
			final String message;
			final String opponent = TSPlayerRoster.US.equals(me) ? TSPlayerRoster.USSR : TSPlayerRoster.US;
			final CurrentState opState = getCurrentState(opponent);
			if (state == CurrentState.WAITING)
				message = new StringBuilder("Waiting for ").append(opponent).append(" to ").append(opState.toString()).append(".").toString();
			else {
				message = new StringBuilder(me).append(" to ").append(state.toString()).append(".").toString();
				if (Utilities.TRUE.equals(Utilities.getGlobalProperty("TSWakeup").getPropertyValue())) {
					Toolkit.getDefaultToolkit().beep();
					if (!alerted) {
						final Command chat = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), "- Player->Preferences->Twilight Struggle to turn off audio alerts.");
						chat.execute();
						alerted = true;						
					}
				}
			}
			new Chatter.DisplayText(chatter, message).execute();
		}

		Command update = new UpdateStateCommand();

		for (TSTimer t : getInstance().timers)
			update = update.append(t.updateState());

		GameModule.getGameModule().getServer().sendToOthers(update);
				
		return comm.append(update);
	}

	public static Command pass(final String me) {
		final String propertyName = TSPlayerRoster.US.equals(me) ? CardEvent.AMERICAN_CARD_PLAYED : CardEvent.SOVIET_CARD_PLAYED;
		final MutableProperty prop = Utilities.getGlobalProperty(propertyName);
		return prop.setPropertyValue(Integer.toString(-1));
	}
	
	public static CurrentState getCurrentState(final String who) {
		if (!TSPlayerRoster.USSR.equals(who) && !TSPlayerRoster.US.equals(who))
			return null;
		// initial setup
		
		if (isGameOver())
			return CurrentState.GAME_OVER;
		
		final int turn = getCurrentTurn();
		final int round = getCurrentRound();
		final String op = TSPlayerRoster.US.equals(who) ? TSPlayerRoster.USSR : TSPlayerRoster.US;
		final String whoDiscards = Utilities.getGlobalProperty(SPACE_RACE_DISCARDED_CARD).getPropertyValue();
		if (round == 0 && !Utilities.FALSE.equals(whoDiscards))
			return who.equals(whoDiscards) ? CurrentState.DISCARD_CARD : CurrentState.WAITING;
		else if (round == 0 
				&& CountingPlayerHand.getHand(TSPlayerRoster.USSR).countCards() < (isEarlyWar() ? 8 : 9)
				&& Utilities.FALSE.equals(Utilities.getGlobalProperty(CardEvent.SOVIET_CARD_PLAYED).getPropertyValue()))
			return dealer.equals(who) ? CurrentState.DEAL_CARDS : CurrentState.WAITING;
		else if (turn == 1 && round == 0) {
			if (Influence.getStartingInfluenceRemaining(TSPlayerRoster.USSR) >= 0)
				return TSPlayerRoster.USSR.equals(who) ? CurrentState.PLACE_STARTING_INFLUENCE : CurrentState.WAITING;
			else if (Influence.getStartingInfluenceRemaining(TSPlayerRoster.US) >= 0)
				return TSPlayerRoster.US.equals(who) ? CurrentState.PLACE_STARTING_INFLUENCE : CurrentState.WAITING;
			if (Influence.getExtraInfluenceRemaining() >= 0) {
				if (who.equals(Utilities.getGlobalProperty(Influence.WHO_RECEIVES_BONUS_INFLUENCE).getPropertyValue()))
					return CurrentState.PLACE_EXTRA_INFLUENCE;
				else 
					return CurrentState.WAITING;
			}
		}
		
		if (round == 0) {
			String propName = TSPlayerRoster.USSR.equals(who) ? CardEvent.SOVIET_CARD_PLAYED : CardEvent.AMERICAN_CARD_PLAYED;
			final String myProp = Utilities.getGlobalProperty(propName).getPropertyValue();
			propName = TSPlayerRoster.USSR.equals(who) ? CardEvent.AMERICAN_CARD_PLAYED : CardEvent.SOVIET_CARD_PLAYED;
			final String opProp = Utilities.getGlobalProperty(propName).getPropertyValue();
			// haven't chosen a headline card yet.
			if (Utilities.FALSE.equals(myProp)) {
				propName = TSPlayerRoster.USSR.equals(who) ? Utilities.SOVIET_SPACE_RACE : Utilities.AMERICAN_SPACE_RACE;
				final int mySpaceRace = Integer.valueOf(Utilities.getGlobalProperty(propName).getPropertyValue());
				propName = TSPlayerRoster.USSR.equals(who) ? Utilities.AMERICAN_SPACE_RACE : Utilities.SOVIET_SPACE_RACE;
				final int opSpaceRace = Integer.valueOf(Utilities.getGlobalProperty(propName).getPropertyValue());
				// I'm at 4 on space race
				if (mySpaceRace >= 4 
						// and my opponent is less than 4
						&& opSpaceRace < 4
						// and my opponent hasn't played yet
						&& Utilities.FALSE.equals(opProp))
					// opponent plays first
					return CurrentState.WAITING;
				else
					return CurrentState.CHOOSE_HEADLINE_CARD;
			}
			//opponent hasn't chosen a headline card yet
			if (Utilities.FALSE.equals(opProp))
				return CurrentState.WAITING;
			final CardEvent myCard = CardEvent.getCard(Integer.valueOf(myProp));
			final CardEvent opCard = CardEvent.getCard(Integer.valueOf(opProp));
			// at least one of hte headline cards has not yet been revealed
			if (myCard.isFlipped())
				return CurrentState.FLIP_HEADLINE_CARD;
			else if (opCard.isFlipped())
				return CurrentState.WAITING;
			// check to see if either of the cards has an event that is in progress
			// if so, ask the event what the state is
			final CardEvent currentEvent = CardEvent.eventCurrentlyPlaying();
			if (currentEvent != null) {
				if (who.equals(currentEvent.activeSide()))
					return CurrentState.EVENT_NOT_FINISHED;
				else
					return CurrentState.WAITING;
			}
			final boolean myEventFinished = !myCard.isEventPlayable(who) || myCard.isEventPlayed() && myCard.isEventFinished();
			final boolean opEventFinished = !opCard.isEventPlayable(op) || opCard.isEventPlayed() && opCard.isEventFinished();
			if (opEventFinished && !myEventFinished)
				return CurrentState.PLAY_EVENT;
			else if (myEventFinished && !opEventFinished)
				return CurrentState.WAITING;
			final int myOps = myCard.getCardOps();
			final int opOps = opCard.getCardOps();
			if (myEventFinished && opEventFinished) {
				if (!myCard.isEventPlayable(who) && !opCard.isEventPlayable(op))
					return TSPlayerRoster.US.equals(who) ? CurrentState.WAITING : CurrentState.ADVANCE_TURN;
				else if (!myCard.isEventPlayable(who))
					return CurrentState.WAITING;
				else if (!opCard.isEventPlayable(op))
					return CurrentState.ADVANCE_TURN;
				else if (myOps < opOps || myOps == opOps && TSPlayerRoster.USSR.equals(who))
					return CurrentState.ADVANCE_TURN;
				else
					return CurrentState.WAITING;
			} else if (myOps > opOps || myOps == opOps && TSPlayerRoster.US.equals(who))
				return CurrentState.PLAY_EVENT;
			else
				return CurrentState.WAITING;
		} 

		// find out of a card has been played and we're waiting for it to finish
		// if so, ask the card what the state is.
		final CardEvent currentEvent = CardEvent.eventCurrentlyPlaying();
		if (currentEvent != null) {
			if (who.equals(currentEvent.activeSide()))
				return CurrentState.EVENT_NOT_FINISHED;
			else
				return CurrentState.WAITING;
		} else if (Influence.isOpsRemaining()) {
			if (who.equals(Utilities.getGlobalProperty(Influence.WHO_PLAYED_OPS).getPropertyValue()))
				return CurrentState.CONDUCT_OPERATIONS;
			else
				return CurrentState.WAITING;
		}
		
		final boolean phasing = getPhasingPlayer().equals(who);

		// haven't played a card yet
		final String propName = TSPlayerRoster.USSR.equals(getPhasingPlayer()) ? CardEvent.SOVIET_CARD_PLAYED : CardEvent.AMERICAN_CARD_PLAYED;
		final String prop = Utilities.getGlobalProperty(propName).getPropertyValue();
		if (Utilities.FALSE.equals(prop)) {
			if (!phasing)
				return CurrentState.WAITING;
			final Quagmire quagmire = CardEvent.getCard(Quagmire.class);
			final BearTrap bearTrap = CardEvent.getCard(BearTrap.class);
			if (quagmire.isEventInEffect() && TSPlayerRoster.US.equals(who))
				return CurrentState.QUAGMIRE;
			else if (bearTrap.isEventInEffect() && TSPlayerRoster.USSR.equals(who))
				return CurrentState.BEAR_TRAP;
			else
				return CurrentState.PLAY_CARD;
		}
		
		final Integer cardNumber = Integer.valueOf(prop);
		if (cardNumber != -1) {
			// if card was played on the Space Race, we're waiting to advance the turn
			final CardEvent card = CardEvent.getCard(cardNumber);
			if (card.isSpaceRacePlayed())
				return phasing ? CurrentState.ADVANCE_TURN : CurrentState.WAITING;

			// more complicated if we've played an opponent's card
			if (card.isOpponentsCard()) {
				if ((!card.isEventPlayable(who) || card.isEventPlayed() && card.isEventFinished()) && card.isOpsPlayed())
					return phasing ? CurrentState.ADVANCE_TURN : CurrentState.WAITING;
				if (card.isOpsPlayed())
					return phasing ? CurrentState.PLAY_EVENT : CurrentState.WAITING;
				else
					return phasing ? CurrentState.PLAY_OPS : CurrentState.WAITING;
			}
		}
		
		return phasing ? CurrentState.ADVANCE_TURN : CurrentState.WAITING;
	}
	
	@Override
	public void setup(boolean gameStarting) {
		// ensure escape key does nothing
		final Object[] mapping = {"ESCAPE", "none"};
        UIManager.put("OptionPane.windowBindings", mapping);

        super.setup(gameStarting);
		if (gameStarting) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					final GameModule gameModule = GameModule.getGameModule();
					gameModule.getToolBar().getComponent(0).setVisible(false);
					updateRegionMarkers();
					if (!((BasicLogger) gameModule.getLogger()).hasMoreCommands())
						gameModule.sendAndLog(updateState());
					else {
						final Chatter chatter = gameModule.getChatter();
						Command chat = new Chatter.DisplayText(chatter, "When finished playing the log, advance the Turn Tracker (in the toolbar) to get the current game state and reveal pending dialogs.");
						chat.execute();
					}
				}
			});			
		}
	}

	public void sideChanged(String oldSide, String newSide) {
		GameModule.getGameModule().sendAndLog(updateState());
	}

	protected Command mySave() {
		Command c = null;
	    if (!savedState.equals(getState())) {

	        reportFormat.setProperty(OLD_TURN, savedTurn);
	        reportFormat.setProperty(NEW_TURN, getTurnString());

	        String s = updateString(reportFormat.getText(this, "Editor.TurnTracker.report_default"), new String[] { "\\n", "\\t" }, new String[] { " - ", " " }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	        c = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), "*** "+s);
	        c.execute();
	        c.append(new SetTurn(this, savedState));

	        setLaunchToolTip();
	      }

	    captureState();
	    return c;
	}

	public static boolean isEarlyWar() {
		return getCurrentTurn() < 4;
	}
	
	public static boolean isMidWar() {
		final int currentTurn = getCurrentTurn();
		return currentTurn >= 4 && currentTurn < 8;
	}

	public static boolean isLateWar() {
		return getCurrentTurn() >= 8;
	}

	public static Command myDoNext() {
		return myDoNext(false);
	}
	
	public static Command myDoNext(boolean override) {
		final TSTurnTracker tracker = getInstance();
		tracker.captureState();
		Command comm = tracker.myNext(override);
		comm = comm.append(tracker.mySave());
		comm = comm.append(updateState());
		return comm;
	}
	
	public static boolean isGameOver() {
		MutableProperty prop = Utilities.getGlobalProperty(Utilities.VPS);
		final int vps = Integer.valueOf(prop.getPropertyValue());
		if (Math.abs(vps) >= 20)
			return true;
		prop = Utilities.getGlobalProperty(Utilities.DEFCON);
		if (Integer.valueOf(prop.getPropertyValue()) == 1)
			return true;
		if (CardEvent.getCard(CubanMissileCrisis.class).isGameOver())
			return true;
		if (ScoringCard.isGameOver())
			return true;
		if (CardEvent.getCard(Wargames.class).isGameOver())
			return true;
		return false;
	}
}