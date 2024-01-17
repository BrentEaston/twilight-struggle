package ca.mkiefte.cards;

import static ca.mkiefte.Utilities.advanceSpaceRace;
import static ca.mkiefte.Utilities.getGlobalProperty;
import static ca.mkiefte.Utilities.getPieceNamed;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Dialog.ModalityType;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.Map;
import VASSAL.build.module.PlayerRoster;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.map.DrawPile;
import VASSAL.build.module.properties.MutableProperty;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.command.NullCommand;
import VASSAL.counters.Deck;
import VASSAL.counters.Decorator;
import VASSAL.counters.EditablePiece;
import VASSAL.counters.Embellishment;
import VASSAL.counters.GamePiece;
import VASSAL.counters.KeyCommand;
import VASSAL.counters.PieceFilter;
import VASSAL.counters.Properties;
import VASSAL.tools.SequenceEncoder;
import ca.mkiefte.CountingPlayerHand;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.TSTurnTracker;
import ca.mkiefte.TSTurnTracker.CurrentState;
import ca.mkiefte.Utilities;

/**
 * The event related to a card.
 * @author mkiefte
 */
public abstract class CardEvent extends Decorator implements EditablePiece {

	private static final String PLAY_WITH_UN_INTERVENTION_FOR = "Play with UN Intervention for ";
	private static final String PLAY_EVENT = "Play Event";
	public final static String ID = "cardevent;";
	public final static String DESCRIPTION = "Card Event";
	
	// Property names
	public static final String WAR = "War";
	public static final String CARD_SIDE = "CardSide";	
	public static final String AMERICAN_CARD_PLAYED = "AmericanCardPlayed";
	public static final String SOVIET_CARD_PLAYED = "SovietCardPlayed";
	public static final String MAY_NOT_BE_HELD = "MayNotBeHeld";
	public static final String REMOVE_FROM_PLAY = "RemoveFromPlay";
	public static final String CARD_NUMBER = "CardNumber";
	public static final String OPS = "Ops";
	public static final Object REMINDER = "Reminder";
	public static final String OBSCURED_TO_OTHERS = Properties.OBSCURED_TO_OTHERS;
	public static final String DECKS = "Decks";
	public static final String DISCARDS = "Discards";
	public static final String REMOVED_FROM_PLAY_DECK_NAME = "Removed From Play";
	public static final String MID_WAR_CARDS = "Mid War Cards";
	public static final String LATE_WAR_CARDS = "Late War Cards";
	public static final String DRAW_DECK = "Draw Deck";
	private static final List<String> SIDE_STRING = Arrays.asList(new String[] {TSPlayerRoster.BOTH, TSPlayerRoster.US, TSPlayerRoster.USSR});
	
	// reminder markers
	public static final KeyStroke PLACE_ON_MAP_KEY = KeyStroke.getKeyStroke('M', KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
	public static final KeyStroke SHOW_REMINDER_KEY	= KeyStroke.getKeyStroke('R', KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK);
	public static final KeyStroke HIDE_REMINDER_KEY = KeyStroke.getKeyStroke('H', KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK);

	// Miscellaneous strings
	private static final String _SPACE_RACE = " Space Race";
	
	// keystrokes available to players
	public static final KeyStroke PLAY_FOR_OPS = KeyStroke.getKeyStroke('O', KeyEvent.CTRL_DOWN_MASK);
	public static final KeyStroke PLAY_FOR_EVENT = KeyStroke.getKeyStroke('E', KeyEvent.CTRL_DOWN_MASK);
	public static final KeyStroke PLAY_WITH_UN_INTERVENTION = KeyStroke.getKeyStroke('I', KeyEvent.CTRL_DOWN_MASK);
	public static final KeyStroke PLAY_HEADLINE	= KeyStroke.getKeyStroke('H', KeyEvent.CTRL_DOWN_MASK);
	public static final KeyStroke FLIP_KEY = KeyStroke.getKeyStroke('F', KeyEvent.CTRL_DOWN_MASK);
	public static final KeyStroke UNDO_KEY = KeyStroke.getKeyStroke('U', KeyEvent.CTRL_DOWN_MASK);
	public static final KeyStroke PLAY_SPACE_RACE_KEY = KeyStroke.getKeyStroke('S', KeyEvent.CTRL_DOWN_MASK);

	// point at which cards are placed in a player's hand
	static Point HAND_DESTINATION = new Point(194,269);
	
	// where cards are placed when they're played from a hand.
	protected final static Point AMERICAN_DESTINATION = new Point(450, 1000);
	protected final static Point SOVIET_DESTINATION = new Point(3050, 500);
	protected static final String MAIN_MAP = "Main Map";
	public static final String OBSCURED_BY = VASSAL.counters.Properties.OBSCURED_BY;
	
	// when the menu is not available to a player.
	private static final KeyCommand[] NO_KEY_COMMANDS = new KeyCommand[0];
	
	private static final String INFLUENCE_COUP_REALIGNMENT = "Influence/Coup/Realignment";
	private static final String SPACE_RACE = "Space Race";
	public static final String CARD_PLAYED_FOR_OPS = "CardPlayedForOps";

	protected int turnEventPlayed = 0;
	protected int roundEventPlayed = 0;
	protected boolean eventInEffect = false;
	private boolean eventPlayed = false;
	protected boolean eventFinished = false;
	protected boolean opsPlayed = false;
	protected boolean spaceRacePlayed = false;
	protected boolean opsPlayedFirst = false;
	protected String owner = null;
	protected int cardNumber;
	protected boolean optional;	
	protected int war;
	protected int ops;	
	protected int cardSide;
	protected boolean removeFromPlay;
	protected boolean notHeld;
	private String currentMapName;
	private Point currentPosition = new Point();
	private static boolean handAlert;
	protected int parentCardNumber;
	protected Command undoPlayOpsCommand;
	public Command undoPlayEventCommand;
	private boolean spaceRaceAlert;
	private static boolean undoAlert;

	// cache of cards to make it easier to search
	private static CardEvent[] cardNumbers = new CardEvent[111];
	private static HashMap<Class<? extends CardEvent>, CardEvent> cardEvents = new HashMap<Class <? extends CardEvent>,CardEvent>(110);
		
	// Constructors
	public CardEvent() {
		this(ID, null);
	}
	
	public CardEvent(final String type, final GamePiece inner) {
		mySetType(type);
		setInner(inner);
		cardEvents.remove(getClass());
		cardNumbers[cardNumber] = null;
	}
	
	// when was the event played
	public int getTurnEventPlayed() {
		return turnEventPlayed;
	}

	public int getRoundEventPlayed() {
		return roundEventPlayed;
	}

	// whether the event is ongoing. By default this is not true.
	public boolean isEventInEffect() {
		return eventInEffect;
	}

	// whether the event was played. This is cleared when dealt to a hand.
	public boolean isEventPlayed() {
		return eventPlayed;
	}
	
	// whether the event is finished or still in progress.  By default the event is finished as soon as it's played.
	public boolean isEventFinished() {
		return eventFinished;
	}
	
	protected boolean eventFinishedOnExit() {
		return true;
	}

	// whether the card has been played for ops
	public boolean isOpsPlayed() {
		return opsPlayed;
	}
	
	// whether the card has been played on the space race.
	public boolean isSpaceRacePlayed() {
		return spaceRacePlayed;
	}
	
	// whether opponent's card has had ops played first.
	public boolean isOpsPlayedFirst() {
		return opsPlayedFirst;
	}
	
	// who owns the card -- i.e. whose hand did it come from.
	public String getOwner() {
		return owner;
	}
	
	// give it to another player and return the command that changes the state.
	public Command setOwner(final String who) {
		if (who == getOwner() || getOwner().equals(who))
			return new NullCommand();
		final ChangeTracker tracker = new ChangeTracker(this);
		owner = who;
		return tracker.getChangeCommand();
	}
	
	protected boolean isUnderlined() {
		return false;
	}
	
	public int getCardNumber() {
		return cardNumber;
	}
	
	public boolean isOptional() {
		return optional;
	}
	
	public boolean isRemoveFromPlay() {
		return removeFromPlay;
	}
	
	public String getCardSide() {
		return cardSide < 0 ? TSPlayerRoster.BOTH : SIDE_STRING.get(cardSide);
	}
	
	public int getWar() {
		return war;
	}
	
	public boolean isScoringCard() {
		return notHeld;
	}

	protected abstract String getIdName();
	
	// Obtains the CardEvent from a GamePiece by traversing the linked list.
	public static CardEvent getCard(GamePiece piece) {
		piece = getOutermost(piece);
		while (!(piece instanceof CardEvent) 
				&& piece instanceof Decorator
				&& ((Decorator) piece).getInner() instanceof Decorator)
			piece = ((Decorator) piece).getInner();
		if (!(piece instanceof CardEvent))
			return null;
		else
			return (CardEvent) piece;
	}
	
	// this player can play the card (for ops, event, or space race) or can headline.
	protected boolean canPlayCard() {
		// Can only play your own card
		if (!PlayerRoster.getMySide().equals(getOwner()))
			return false;
		
		final String propertyName = TSPlayerRoster.US.equals(getOwner()) ? AMERICAN_CARD_PLAYED : SOVIET_CARD_PLAYED;
		final MutableProperty property = Utilities.getGlobalProperty(propertyName);
		final String propertyValue = property.getPropertyValue();
		if (!Utilities.FALSE.equals(propertyValue) && Integer.valueOf(propertyValue) != getCardNumber())
			return false;
		
		final MissileEnvy missile = getCard(MissileEnvy.class);
		if (TSTurnTracker.getCurrentRound() > 0 
				&& missile.isEventInEffect() 
				&& missile.getTargetedPlayer().equals(getOwner()) 
				&& this != missile)
			return false;
		else
			return true;
	}
	
	protected boolean canPlayHeadline() {
		if (!canPlayCard())
			return false;
		final CurrentState state = TSTurnTracker.getCurrentState(getOwner());
		if (state != CurrentState.CHOOSE_HEADLINE_CARD)
			return false;
		
		return true;
	}
	
	public boolean canUndoPlayOps() {
		return isOpsPlayed()
				&& undoPlayOpsCommand != null 
				&& !undoPlayOpsCommand.isNull() 
				&& isOpsPlayed()
				&& TSPlayerRoster.getMySide().equals(getOwner())
				&& !Utilities.TRUE.equals(Utilities.getGlobalProperty(Utilities.HAS_ROLLED_DIE).getPropertyValue())
				&& (!isEventPlayed() || !isOpsPlayedFirst());
	}
	
	public static boolean canPlayCardIntoSpaceRace(String who, int ops) {
		if (ops < 2) // must have at least 2 ops
			return false;

		final String propName;
		final String otherSide;
		final GamePiece piece;
		if (TSPlayerRoster.US.equals(who)) {
			propName = Utilities.AMERICAN_SPACE_RACE;
			otherSide = Utilities.SOVIET_SPACE_RACE;
			piece = getPieceNamed(Utilities.AMERICAN_SPACE_RACE_MARKER);
		} else {
			propName = Utilities.SOVIET_SPACE_RACE;
			otherSide = Utilities.AMERICAN_SPACE_RACE;
			piece = getPieceNamed(Utilities.SOVIET_SPACE_RACE_MARKER);
		}
		final int spaceRace = Integer.valueOf(getGlobalProperty(propName).getPropertyValue());
		if (spaceRace == 8) // no more space race cards
			return false;
		if (spaceRace > 3 && ops < 3) 
			return false;
		if (spaceRace == 7 && ops < 4)
			return false;
		final int cardsPlayed = Integer.valueOf((String) piece.getProperty(Utilities.CARDS_PLAYED_ON_SPACE_RACE));
		if (cardsPlayed == 2)
			return false;
		else if (cardsPlayed == 1) {
			if (spaceRace >= 2) {
				final int other = Integer.valueOf(getGlobalProperty(otherSide).getPropertyValue());
				return other < 2;
			}
			else
				return false;
		}
		return true;
	}
	
	protected boolean canFlipHeadlineCards() {
		final CurrentState state = TSTurnTracker.getCurrentState(TSPlayerRoster.getMySide());
		if (state != CurrentState.FLIP_HEADLINE_CARD)
			return false;
		
		final String me = getOwner();
		// Only my cards that are flipped
		if (!getOutermost(this).getProperty(CardEvent.OBSCURED_BY).equals(me))
			return false;
		
		// Only if it's on the main map
		if (!getMap().getMapName().equals(MAIN_MAP))
			return false;
		
		// Only if this is the card I played (and not the China Card)
		final String propertyName = TSPlayerRoster.USSR.equals(me) ? SOVIET_CARD_PLAYED : AMERICAN_CARD_PLAYED;
		final MutableProperty prop = Utilities.getGlobalProperty(propertyName);
		final String cardPlayed = prop.getPropertyValue();
		if (Integer.valueOf(cardPlayed) != getCardNumber())
			return false;
		
		return true;
	}

	// this player can play this card for the event as their action
	protected boolean canPlayEvent() {
		if (!canPlayCard())
			return false;
		if (isEventPlayed())
			return false;
		if (!isEventPlayable(getOwner()))
			return false;

		final CurrentState state = TSTurnTracker.getCurrentState(getOwner());
		if (state != CurrentState.PLAY_EVENT && state != CurrentState.PLAY_CARD)
			return false;

		// all's clear
		return true;		
	}
	
	protected boolean canPlayOps() {
		if (!canPlayCard())
			return false;		
		if (isOpsPlayed())
			return false;

		if (TSTurnTracker.getCurrentRound() == 0) // not in Headline phase
			return false;
		
		if (getOps() == 0)
			return false;
		
		final CurrentState state = TSTurnTracker.getCurrentState(getOwner());
		if (state != CurrentState.PLAY_OPS && state != CurrentState.PLAY_CARD)
			return false;
				
		return true;
	}
	
	protected boolean canPlaySpaceRace() {
		if (!canPlayCard())
			return false;
		if (isEventPlayed() || isOpsPlayed())
			return false;
		if (!canPlayCardIntoSpaceRace(getOwner(), getOps()))
			return false;
		
		// cannot during headline phase
		if (TSTurnTracker.getCurrentRound() == 0)
			return false;
		
		final CurrentState state = TSTurnTracker.getCurrentState(getOwner());
		if (state != CurrentState.PLAY_CARD)
			return false;
		
		return true;
	}
	
	@Override
	public KeyCommand[] myGetKeyCommands() {
		// Must own the card in order to see a menu
		if (PlayerRoster.getMySide() == null || !PlayerRoster.getMySide().equals(getOwner()) || parentCardNumber != 0)
			return NO_KEY_COMMANDS;
		
		// [0] Play Headline, [1] Play Event, [2] Play Space Race [3] Play Other Ops
		// alt: [0] Undo Play Headline, [1] Undo Play Event, [2] Play Space Race, [3] Undo Play Ops, 
		final List<KeyCommand> commands = new ArrayList<KeyCommand>(5);
		String message;
		boolean enable;
		KeyStroke key;
		final GamePiece outermost = getOutermost(this);
		if (canPlayHeadline()) {
			final StringBuilder builder = new StringBuilder("Play As Headline Event");
			if (!isEventPlayable(getOwner()))
				builder.append(" (for no effect)");
			enable = true;
			key = PLAY_HEADLINE;
			message = builder.toString();
		} else {
			message = "Cannot Play Headline Card";
			enable = false;
			key = PLAY_HEADLINE;
		}
		commands.add(new KeyCommand(message, key, outermost, enable));
		
		final int ops = getOps();
		if (canPlayOps()) {
			final StringBuilder builder = new StringBuilder("Play for ").append(ops).append(" Op");
			if (ops > 1)
				builder.append('s');
			final String opponent = TSPlayerRoster.USSR.equals(getOwner()) ? TSPlayerRoster.US : TSPlayerRoster.USSR;
			if (!isEventPlayed() && isEventPlayable(getOwner()) && getCardSide().equals(opponent))
				builder.append(" before Event");
			enable = true;
			message = builder.toString();
			key = PLAY_FOR_OPS;
		} else if (canUndoPlayOps()) {
			enable = true;
			key = UNDO_KEY;
			message = "Undo Play Ops";
		} else {
			message = "Cannot play Ops";
			enable = false;
			key = PLAY_FOR_OPS;
		}		
		commands.add(new KeyCommand(message, key, outermost, enable));
		
		if (canPlayEvent()) {
			final StringBuilder builder = new StringBuilder("Play for Event");
			final String cardSide = getCardSide();
			final String opponent = TSPlayerRoster.USSR.equals(getOwner()) ? TSPlayerRoster.US : TSPlayerRoster.USSR;
			if (canPlayOps() && cardSide.equals(opponent))
				builder.append(" before Ops");
			enable = true;
			key = PLAY_FOR_EVENT;
			message = builder.toString();
		} else if (canUndoEvent()) {
			enable = true;
			key = UNDO_KEY;
			message = "Undo Play Event";
		} else {
			message = "Cannot Play Event";
			enable = false;
			key = PLAY_FOR_EVENT;
		}
		commands.add(new KeyCommand(message, key, outermost, enable));
		
		if (canPlaySpaceRace()) {
			message = "Play on Space Race";
			enable = true;
		}
		else {
			message = "Cannot Play on Space Race";
			enable = false;
		}
		commands.add(new KeyCommand(message, PLAY_SPACE_RACE_KEY, outermost, enable));
		
		if (canPlayWithUnIntervention()) {
			final StringBuilder builder = new StringBuilder(PLAY_WITH_UN_INTERVENTION_FOR).append(ops).append(" Op");
			if (ops > 1)
				builder.append('s');
			
			commands.add(new KeyCommand(builder.toString(), PLAY_WITH_UN_INTERVENTION, outermost, canPlayOps()));
		}
		
		return commands.toArray(new KeyCommand[commands.size()]);
	}

	public boolean canUndoEvent() {
		return TSTurnTracker.getCurrentRound() != 0
				&& undoPlayEventCommand != null 
				&& !undoPlayEventCommand.isNull()
				&& isEventPlayed()
				&& !Utilities.TRUE.equals(Utilities.getGlobalProperty(Utilities.HAS_ROLLED_DIE).getPropertyValue())
				&& TSPlayerRoster.getMySide().equals(getOwner())
				&& (!isOpsPlayed() || isOpsPlayedFirst());
	}

	protected boolean canPlayWithUnIntervention() {
		return isOpponentsCard()
				&& getOwner().equals(getCard(UnIntervention.class).getOwner())
				&& TSTurnTracker.getCurrentRound() != 0
				&& isEventPlayable(getOwner())
				&& !isEventPlayed();
	}

	public int getCardOps() {
		return ops;
	}
	
	public int getOps() {
		if (getCardOps() > 0) {
			int o = addBonusOps(getCardOps(), getOwner());
			o = o < 1 ? 1 : o;
			o = o > 4 ? 4 : o;
			return o;
		} else
			return 0;
	}

	protected static int addBonusOps(int ops, final String who) {
		if (ops == 0) // Scoring card
			return ops;
		if (TSPlayerRoster.US.equals(who) && getCard(Containment.class).isEventInEffect())
			++ops;
		else if (TSPlayerRoster.USSR.equals(who) && getCard(BrezhnevDoctrine.class).isEventInEffect())
			++ops;		
		ops += getCard(RedScarePurge.class).getPenalty(who);
		return ops;
	}

	@Override
	public String myGetState() {
		final SequenceEncoder encoder = new SequenceEncoder(';');
		encoder.append(new StringBuilder(3).append(turnEventPlayed).append('.').append(roundEventPlayed).toString());
		encoder.append(isEventPlayed());
		encoder.append(opsPlayed);
		encoder.append(eventInEffect);
		encoder.append(owner);
		encoder.append(spaceRacePlayed);
		encoder.append(opsPlayedFirst);
		encoder.append(eventFinished);
		encoder.append(currentMapName);
		encoder.append(currentPosition.x);
		encoder.append(currentPosition.y);
		encoder.append(parentCardNumber);
		return encoder.getValue();
	}

	/**
	 * Stores the turn and round in which this event was played.
	 * @see VASSAL.counters.Decorator#mySetState(java.lang.String)
	 */
	@Override
	public void mySetState(final String newState) {
		final SequenceEncoder.Decoder decoder = new SequenceEncoder.Decoder(newState, ';');
		final SequenceEncoder.Decoder timeDecoder = new SequenceEncoder.Decoder(decoder.nextToken(), '.');
		turnEventPlayed = timeDecoder.nextInt(0);
		roundEventPlayed = timeDecoder.nextInt(0);
		setEventPlayed(decoder.nextBoolean(false));
		opsPlayed = decoder.nextBoolean(false);
		eventInEffect = decoder.nextBoolean(false);
		owner = decoder.nextToken(null);
		spaceRacePlayed = decoder.nextBoolean(false);
		opsPlayedFirst = decoder.nextBoolean(false);
		eventFinished = decoder.nextBoolean(false);
		currentMapName = decoder.nextToken(null);
		currentPosition.x = decoder.nextInt(0);
		currentPosition.y = decoder.nextInt(0);
		parentCardNumber = decoder.nextInt(0);
	}

	@Override
	public String myGetType() {
		SequenceEncoder encoder = new SequenceEncoder(';');
		encoder.append(getCardNumber());
		encoder.append(isOptional());
		encoder.append(getWar());
		encoder.append(getCardOps());
		encoder.append(SIDE_STRING.indexOf(getCardSide()));
		encoder.append(isRemoveFromPlay());
		encoder.append(isScoringCard());
		return getIdName() + encoder.getValue();
	}


	public void mySetType(final String type) {
		final SequenceEncoder.Decoder decoder = new SequenceEncoder.Decoder(type, ';');
		decoder.nextToken();
		cardNumber = decoder.nextInt(0);
		optional = decoder.nextBoolean(false);
		war = decoder.nextInt(0);
		ops = decoder.nextInt(0);
		cardSide = decoder.nextInt(0);
		removeFromPlay = decoder.nextBoolean(false);
		notHeld = decoder.nextBoolean(false);
	}
	
	public Rectangle boundingBox() {
		return piece.boundingBox();
	}

	public void draw(final Graphics g, final int x, final int y, final Component obs, final double zoom) {
		piece.draw(g, x, y, obs, zoom);
	}

	public Shape getShape() {
		return piece.getShape();
	}

	public HelpFile getHelpFile() {
		return null;
	}

	public String getName() {
		return getDescription();
	}

	protected Command playCard() {
		boolean close = Utilities.TRUE.equals(Utilities.getGlobalProperty("CloseHand").getPropertyValue());
		if (close) {
			final Map map = getMap();
			if (map instanceof CountingPlayerHand)
				((CountingPlayerHand) map).getTheMap().getTopLevelAncestor().setVisible(false);
		} else if (!handAlert) {
			final Command chat = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), "- Player->Preferences->Twilight Struggle to close player hand automatically.");
			chat.execute();
			handAlert = true;
		}
		final String whoPlayedCard;
		if (TSPlayerRoster.USSR.equals(getOwner()))
			whoPlayedCard = SOVIET_CARD_PLAYED;
		else
			whoPlayedCard = AMERICAN_CARD_PLAYED;
		final MutableProperty globalProperty = getGlobalProperty(whoPlayedCard);
		if (Utilities.FALSE.equals(globalProperty.getPropertyValue())) {
			final String cardPlayed = Integer.toString(getCardNumber());
			return globalProperty.setPropertyValue(cardPlayed);
		} else
			return new NullCommand();
	}
	
	protected Command undoPlayCard() {
		return null;
	}

	public Command playOps() {
		final Command comm = playOps(getOwner());
		if (comm != null && !comm.isNull())
			undoPlayOpsCommand = comm.getUndoCommand();
		else
			undoPlayOpsCommand = null;
		return comm;
	}
	
	public Command playOps(final String who) {
		Command comm = setOwner(who);
		comm = comm == null ? playCard() : comm.append(playCard());
		comm = comm.append(playOps(who, this, getCardOps()));
		final ChangeTracker tracker = new ChangeTracker(this);
		opsPlayed = true;
		if (!isEventPlayed())
			opsPlayedFirst = true;
		comm = comm.append(tracker.getChangeCommand());
		comm = comm.append(placeCardOnBoard(getOwner()));
		final String propertyName = TSPlayerRoster.US.equals(who) ? AMERICAN_CARD_PLAYED : SOVIET_CARD_PLAYED;
		final String cardPlayed = Utilities.getGlobalProperty(propertyName).getPropertyValue();
		final String cardNumber = Integer.toString(getCardNumber());
		final MutableProperty cardPlayedForOps = Utilities.getGlobalProperty(CARD_PLAYED_FOR_OPS);
		if (cardPlayed.equals(cardNumber))
			comm = comm.append(cardPlayedForOps.setPropertyValue(cardNumber));
		return comm;
	}

	public Command undoPlayOps() {
		return undoPlayOps(getOwner());
	}
	
	public Command undoPlayOps(final String who) {
		Command undo = undoPlayOpsCommand;
		Command comm;
		undoPlayOpsCommand = null;
		if (undo != null) {
			comm = Influence.revertToAtStartInfluence();
			undo.execute();
			comm = comm.append(undo);
		} else
			comm = Influence.revertToAtStartInfluence();
		comm = comm.append(CardEvent.getCard(VietnamRevolts.class).setAllInSoutheastAsia(false));
		return comm;
	}
	
	// Play operations.
	// This is static because you can play generic ops from events.
	final public static Command playOps(final String side, 
			final CardEvent gp, 
			final int ops) {
		int adjusted = 0;
		if (ops > 0) {
			adjusted = addBonusOps(ops, side);
			adjusted = adjusted < 1 ? 1 : adjusted;
			adjusted = adjusted > 4 ? 4 : adjusted;
		}
		final String name;
		if (gp == null)
			name = "card";
		else
			name = gp.getDescription();
		final Chatter chatter = GameModule.getGameModule().getChatter();
		final StringBuilder message = new StringBuilder("*** ").append(side).append(" plays ").append(name).append(" for ").append(adjusted).append(" Op");
		message.append(adjusted > 1 ? "s." : ".");
		Command comm = new Chatter.DisplayText(chatter, message.toString());
		comm.execute();
		comm = comm.append(getCard(WeWillBuryYou.class).awardVps(true, side));
		if (TSTurnTracker.isGameOver())
			return comm;
		comm = comm.append(getGlobalProperty(Influence.OPS_REMAINING).setPropertyValue(Integer.toString(adjusted)));
		comm = comm.append(getGlobalProperty(Influence.WHO_PLAYED_OPS).setPropertyValue(side));
		final VietnamRevolts vietnamRevolts = getCard(VietnamRevolts.class);
		if (TSPlayerRoster.USSR.equals(side) && vietnamRevolts.isEventInEffect()) {
			final Command chat = new Chatter.DisplayText(chatter, "+1 Op in Southeast Asia (Vietnam Revolts).");
			chat.execute();
			comm = comm.append(chat);
			comm = comm.append(vietnamRevolts.setup(ops));
		}
		comm = comm.append(Influence.setAllStartingInfluence());
		comm = comm.append(getGlobalProperty(Utilities.HAS_ROLLED_DIE).setPropertyValue(Utilities.FALSE));
		return comm;
	}
	
	// overriding methods must eventually set eventFinished to true
	protected Command myPlayEvent(final String who) {
		ChangeTracker tracker = new ChangeTracker(this);
		owner = who;
		setEventPlayed(true);
		turnEventPlayed = TSTurnTracker.getCurrentTurn();
		roundEventPlayed = TSTurnTracker.getCurrentRound();
		Command comm = tracker.getChangeCommand();
		comm = comm.append(sendEventMessage());
		if (this instanceof UnIntervention)
			comm = comm.append(getCard(WeWillBuryYou.class).awardVps(false, who));
		else if (!(this instanceof ParentCard))
			comm = comm.append(getCard(WeWillBuryYou.class).awardVps(true, who));
		if (TSTurnTracker.isGameOver())
			return comm;
		comm = comm.append(placeCardOnBoard(who));
		comm = comm.append(Influence.setAllStartingInfluence());
		if (isUnderlined())
			comm = comm.append(setEventInEffect(true));
		comm = comm.append(getGlobalProperty(Utilities.HAS_ROLLED_DIE).setPropertyValue(Utilities.FALSE));
		return comm;
	}

	public Command setEventInEffect(boolean inEffect) {
		if (inEffect == eventInEffect)
			return null;
		ChangeTracker tracker = new ChangeTracker(this);
		eventInEffect = inEffect;
		Command comm = tracker.getChangeCommand();
		final String cardNumber = Integer.toString(getCardNumber());
		GamePiece reminder = Utilities.findPiece(new PieceFilter() {
			public boolean accept(final GamePiece piece) {
				return cardNumber.equals((String) piece.getProperty(REMINDER));
			}});
		if (reminder != null) {
			tracker = new ChangeTracker(reminder);
			while (!(reminder instanceof Embellishment) 
					|| !((Embellishment) reminder).getLayerName().equals(REMINDER))
				reminder = ((Decorator) reminder).getInner();
			((Embellishment) reminder).setActive(inEffect);
			comm = comm.append(tracker.getChangeCommand());
		}
		return comm;
	}
	
	protected Command sendEventMessage() {
		final Chatter chatter = GameModule.getGameModule().getChatter();
		final StringBuilder message = new StringBuilder("*** ").append(getOwner()).append(" plays ").append(getDescription()).append(" Event.");
		Command comm = new Chatter.DisplayText(chatter, message.toString());
		comm.execute();
		return comm;
	}
	
	public Command undoPlayEvent() {
		final Command temp = undoPlayEventCommand;
		undoPlayEventCommand = null;
		temp.execute();
		return temp;
	}
	
	protected Command playHeadline() {
		final String me = getOwner();
		final Chatter chatter = GameModule.getGameModule().getChatter();
		Command comm = playCard();
		final boolean soviet = TSPlayerRoster.USSR.equals(me);
		String propertyName = soviet ? Utilities.SOVIET_SPACE_RACE : Utilities.AMERICAN_SPACE_RACE;
		final int mySpaceRace = Integer.valueOf(getGlobalProperty(propertyName).getPropertyValue());
		propertyName = soviet ? Utilities.AMERICAN_SPACE_RACE : Utilities.SOVIET_SPACE_RACE;
		final int opSpaceRace = Integer.valueOf(getGlobalProperty(propertyName).getPropertyValue());
		final StringBuilder builder = new StringBuilder("*** ").append(me);
		if (opSpaceRace < 4 || mySpaceRace >= 4) {
			builder.append(" plays headline card.");
			Command chat = new Chatter.DisplayText(chatter, builder.toString());
			chat.execute();
			comm = comm.append(chat);
			final ChangeTracker tracker = new ChangeTracker(this);
			getOutermost(this).setProperty(CardEvent.OBSCURED_BY, me);
			comm = comm.append(tracker.getChangeCommand());
		}
		else {
			builder.append(" plays ").append(getName()).append(" as headline card. ***");
			Command chat = new Chatter.DisplayText(chatter, builder.toString());
			chat.execute();
			comm = comm.append(chat);
		}
		comm = comm.append(placeCardOnBoard(getOwner()));
		return comm;
	}
	
	protected Command playSpaceRace(final String who) {
		if (Utilities.TRUE.equals(Utilities.getGlobalProperty("AskSpaceRace").getPropertyValue())) {
			final int answer = JOptionPane.showConfirmDialog(GameModule.getGameModule().getPlayerWindow(),
					new StringBuilder("Are you sure you want to play ").append(getName()).append("\nin the Space Race?\nThis cannot be undone.").toString(), 
					"Space Race", 
					JOptionPane.YES_NO_CANCEL_OPTION, 
					JOptionPane.QUESTION_MESSAGE, 
					asIcon());
			if (!spaceRaceAlert) {
				final Command chat = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), "- Player->Preferences->Twilight Struggle to turn off Space Race warnings.");
				chat.execute();
				spaceRaceAlert = true;
			}
			if (answer != JOptionPane.YES_OPTION)
				return null;
		}
		return playCard().append(myPlaySpaceRace(who, this));
	}
	
	protected static Command myPlaySpaceRace(final String side, final CardEvent card) {
		final String propName;
		final String pieceName;
		if (TSPlayerRoster.USSR.equals(side)) { 
			propName = Utilities.SOVIET_SPACE_RACE;
			pieceName = Utilities.SOVIET_SPACE_RACE_MARKER;
		}
		else {
			propName = Utilities.AMERICAN_SPACE_RACE;
			pieceName = Utilities.AMERICAN_SPACE_RACE_MARKER;
		}
		
		final String name = card == null ? "card" : card.getDescription();
		
		final int spaceRace = Integer.valueOf(getGlobalProperty(propName).getPropertyValue());
		final int target = Integer.valueOf((String) getPieceNamed(spaceRace + _SPACE_RACE).getProperty(Utilities.MINIMUM_DIE_ROLL_PROP_NAME));
		final Chatter chatter = GameModule.getGameModule().getChatter();
		final StringBuilder message = new StringBuilder("*** ").append(side).append(" plays ").append(name).append(" in the Space Race.");
		Command comm = new Chatter.DisplayText(chatter, message.toString());
		comm.execute();
		comm = comm.append(getCard(WeWillBuryYou.class).awardVps(true, side));
		if (TSTurnTracker.isGameOver())
			return comm;
		if (card != null) {
			comm = comm.append(card.setOwner(side));
			comm = comm.append(card.placeCardOnBoard(side));
			final ChangeTracker tracker = new ChangeTracker(card);
			card.spaceRacePlayed = true;
			comm = comm.append(tracker.getChangeCommand());
		}
		comm = comm.append(Utilities.getGlobalProperty(Influence.OPS_REMAINING).setPropertyValue(""));
		final int[] result = {0};
		comm = comm.append(Utilities.rollDie(side, result));
		final GamePiece marker = getPieceNamed(pieceName);
		final int cardsPlayed = Integer.valueOf((String) marker.getProperty(Utilities.CARDS_PLAYED_ON_SPACE_RACE));
		comm = comm.append(Utilities.setCardPlayedOnSpaceRace(side, cardsPlayed+1));
		if (result[0] <= target)
			comm = comm.append(advanceSpaceRace(side));
		else {
			final Command chat = new Chatter.DisplayText(chatter, "Space Race attempt not successful.");
			chat.execute();
			comm = comm.append(chat);
		}
		return comm;
	}

	public static Command flipHeadlineCards() {
		Command comm = new NullCommand();
		final Chatter chatter = GameModule.getGameModule().getChatter();
		for (final String propertyName : new String[] {AMERICAN_CARD_PLAYED, SOVIET_CARD_PLAYED}) {
			final String propertyValue = getGlobalProperty(propertyName).getPropertyValue();
			CardEvent card = getCard(Integer.valueOf(propertyValue));
			final GamePiece gp = getOutermost(card);
			ChangeTracker tracker = new ChangeTracker(gp);
			gp.setProperty(CardEvent.OBSCURED_BY, null);
			comm = comm.append(tracker.getChangeCommand());
			if (!card.isEventPlayable(card.getOwner())) {
				final StringBuilder builder = new StringBuilder(card.getDescription()).append(" is not playable.");
				final Command chat = new Chatter.DisplayText(chatter, builder.toString());
				chat.execute();
				comm = comm.append(chat);
			}
		}
		return comm;
	}

	/**
	 * sets the card played global property to this card number
	 * @see VASSAL.counters.Decorator#myKeyEvent(javax.swing.KeyStroke)
	 */
	@Override
	public Command myKeyEvent(final KeyStroke stroke) {
		Command comm = null;
		if (stroke.equals(PLACE_ON_MAP_KEY)) {
			if (!getMap().getMapName().equals(currentMapName)
					|| "Decks".equals(currentMapName)) {
				final GamePiece outermost = getOutermost(this);
				final Map mapById = Map.getMapById(currentMapName);
				comm = mapById.placeOrMerge(outermost, currentPosition);
				mapById.repaint();
				final JOptionPane optionPane = new JOptionPane("Right-click on cards in order to play.", JOptionPane.ERROR_MESSAGE);
				final JDialog dialog = optionPane.createDialog("Invalid Move");
				dialog.setModal(false);
				dialog.setVisible(true);
				return comm;
			}
		}
		else if (stroke.equals(UNDO_KEY) && canUndoEvent())
			comm = undoPlayEvent();
		else if (stroke.equals(UNDO_KEY) && canUndoPlayOps())
			comm = undoPlayOps();
		else if (!scoringCardCheck(stroke))
			return null;
		else if (stroke.equals(PLAY_FOR_EVENT) && canPlayEvent() && parentCardNumber == 0)
			comm = playEvent();
		else if (stroke.equals(PLAY_FOR_OPS) && canPlayOps() && parentCardNumber == 0)
			comm = playOps();
		else if (stroke.equals(PLAY_HEADLINE) && canPlayHeadline())			
			comm = playHeadline();
		else if (stroke.equals(PLAY_SPACE_RACE_KEY) && canPlaySpaceRace() && parentCardNumber == 0)
			comm = playSpaceRace(getOwner());
		else if (stroke.equals(PLAY_WITH_UN_INTERVENTION) && canPlayWithUnIntervention() && canPlayOps() && parentCardNumber == 0)
			comm = getCard(UnIntervention.class).playWith(this);	
		
		if (comm != null)
			comm = comm.append(TSTurnTracker.updateState());
		
		return comm;
	}

	public Command playEvent() {
		Command comm = playEvent(getOwner());
		undoPlayEventCommand = comm.getUndoCommand();
		if (!undoAlert && canUndoEvent() 
				&& (TSTurnTracker.getCurrentState(getOwner()) != CurrentState.ADVANCE_TURN 
				|| !Utilities.TRUE.equals(Utilities.getGlobalProperty("AtomaticAdvance").getPropertyValue()))) {
			undoAlert = true;
			new Chatter.DisplayText(GameModule.getGameModule().getChatter(), "- Right-click card to undo.").execute();
		}
		return comm;
	}
	
	final public Command playEvent(final String who) {
		Command comm = playCard();
		comm = comm.append(myPlayEvent(who));
		if (eventFinishedOnExit()) 
			comm = comm.append(setFinished(true));
		return comm;
	}

	public Command cancelEvent() {
		if (!isEventInEffect())
			return null;
		Command comm = setEventInEffect(false);
		final Chatter chatter = GameModule.getGameModule().getChatter();
		final StringBuilder builder = new StringBuilder(getDescription()).append(" Event is cancelled.");
		final Command chat = new Chatter.DisplayText(chatter, builder.toString());
		chat.execute();
		comm = comm.append(chat);
		return comm;
	}
	
	protected Command placeCardOnBoard(final String who) {
		Command comm;
		if (!who.equals(getOwner()))
			comm = setOwner(who);
		else
			comm = new NullCommand();
		if (MAIN_MAP.equals(getMap().getMapName()))
			return comm;
		final Point destination;
		if (TSPlayerRoster.USSR.equals(who))
			destination = SOVIET_DESTINATION;
		else
			destination = AMERICAN_DESTINATION;
		final GamePiece outermost = getOutermost(this);
		final Map mapById = Map.getMapById(MAIN_MAP);
		comm = comm.append(mapById.placeOrMerge(outermost, destination));
		comm = comm.append(setCurrentPosition());
		mapById.repaint();
		return comm;
	}
	
	public abstract String getDescription();

	// will the player be left holding a scoring card. returns false if player can't play a card.
	// Assume current player is the onwer.
	private boolean scoringCardCheck(final KeyStroke stroke) {
		if (isOpsPlayed() || isEventPlayed())
			return true;
		if (isScoringCard())
			return true;
		
		int nScoringCards = 0;
		final int nRoundsRemaining = TSTurnTracker.getNRoundsRemaing(getOwner());
		final Set<CardEvent> cards = CountingPlayerHand.getHand(getOwner()).getAllCards();
		for (GamePiece card : cards) {	
			// failsafe: There's a card in my hand that isn't mine.
			if (!getOwner().equals(getCard(card).getOwner()))
				continue;
			final boolean mayNotBeHeld = getCard(card).isScoringCard();
			if (mayNotBeHeld && ++nScoringCards >= nRoundsRemaining) {
				return JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(
						GameModule.getGameModule().getPlayerWindow(),
						"You have more Scoring Cards then there are rounds left in the Turn\n"
						+ "Are you sure you want to play this card and risk holding a Scoring Card"
						+ "at the end of the Turn?", 
						"Hold Scoring Card?",
						JOptionPane.YES_NO_OPTION, 
						JOptionPane.WARNING_MESSAGE);
			}
		}
		
		// all's clear
		return true;
	}
	
	/**
	 * Whether the event is playable or not; 
	 * i.e., whether preconditions have been met
	 * or whether it's been prevented.
	 * Defectors always prevents Soviet headline card
	 * @return true if event is playable
	 */
	public boolean isEventPlayable(final String who) {
		// Defectors played as headline
		// This has to be here so that the TurnTracker knows it can advance without playing events.
		final String value = getGlobalProperty(AMERICAN_CARD_PLAYED).getPropertyValue();
		if (Utilities.FALSE.equals(value))
			return true;
		final int round = TSTurnTracker.getCurrentRound();
		final int cardPlayed = Integer.valueOf(value);
		final Defectors defectors = getCard(Defectors.class);
		if (round == 0 && defectors.getCardNumber() == cardPlayed
				&& getOutermost(defectors).getProperty(CardEvent.OBSCURED_BY) == null)
			return false;
		
		return true;
	}
	
	public static CardEvent selectCard(final Set<CardEvent> options, 
			String title, 
			String message, 
			final PieceFilter filter, 
			final boolean canCancel) 
	{
		if (options != null && !options.isEmpty() && filter != null) {
			final Iterator<CardEvent> iter = options.iterator();
			while (iter.hasNext())
				if (!filter.accept(iter.next()))
					iter.remove();
		}
		if (title == null)
			title = "Select a Card";
		if (message == null)
			message = "Select a card:";
		if (options == null || options.isEmpty()) {
			JOptionPane.showMessageDialog(GameModule.getGameModule().getPlayerWindow(),
					"There are no cards for you to select!", 
					title, JOptionPane.INFORMATION_MESSAGE);
			return null;
		}
		return (CardEvent) Utilities.selectGamePiece(options, title, message, null, canCancel);
	}

	public static Command playCardDialog(final String who, 
			final CardEvent card, 
			int nOps, 
			boolean canPlayEvent, 
			final Icon icon) {	
		int adjusted = 0;
		if (nOps > 0) {
			adjusted = nOps > 0 ? addBonusOps(nOps, who) : 0;
			adjusted = adjusted < 1 ? 1 : adjusted;
			adjusted = adjusted > 4 ? 4 : adjusted;
		}
		final List<String> options = new ArrayList<String>();
		if (card == null || !card.isEventPlayable(who) || card.isEventPlayed())
			canPlayEvent = false;
		final boolean canPlaySpaceRace = canPlayCardIntoSpaceRace(who, adjusted);
		StringBuilder builder;
		final String opponent = TSPlayerRoster.US.equals(who) ? TSPlayerRoster.USSR : TSPlayerRoster.US;	
		
		if (adjusted > 0) {
			builder = new StringBuilder(INFLUENCE_COUP_REALIGNMENT).append(" with ").append(adjusted).append(" Op");
			if (adjusted > 1)
				builder.append("s");
			if (canPlayEvent && opponent.equals(card.getCardSide()))
				builder.append(" before Event");
			options.add(builder.toString());
		}
		
		if (canPlaySpaceRace)
			options.add(SPACE_RACE);
		
		if (canPlayEvent) {
			builder = new StringBuilder(PLAY_EVENT);
			if (adjusted > 0 && opponent.equals(card.getCardSide()))
				builder.append(" before Ops");
			options.add(builder.toString());
			
			if (card.canPlayWithUnIntervention()) {
				builder = new StringBuilder(PLAY_WITH_UN_INTERVENTION_FOR).append(adjusted).append(" Op");
				if (adjusted > 1)
					builder.append('s');
				options.add(builder.toString());
			}
		}
		
		String choice;
		if (options.size() == 0)
			return null;
		else if (options.size() == 1)
			choice = options.get(0);
		else {
			final JList list = new JList(options.toArray(new String[options.size()]));
			final Object[] objects = {"How do you want to Conduct Operations?", list};
			final JOptionPane pane = new JOptionPane(objects);
			if (icon != null)
				pane.setIcon(icon);
			else if (card != null)
				pane.setIcon(card.asIcon());
			final String title;
			if (card != null)
				title = card.getDescription();
			else
				title = "Conduct Operations";
			list.setSelectedIndex(0);
			final JDialog dialog = pane.createDialog(null, title);
			dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
			dialog.setAlwaysOnTop(true);
			dialog.setVisible(true);
			choice = options.get(list.getSelectedIndex());
		}
		
		if (choice.startsWith(PLAY_EVENT)) {
			return card.playEvent(who);
		} else if (choice.startsWith(INFLUENCE_COUP_REALIGNMENT)) {
			if (card != null)
				return card.playOps(who);
			else
				return playOps(who, null, nOps);
		} else if (choice.startsWith(PLAY_WITH_UN_INTERVENTION_FOR)) {
			return getCard(UnIntervention.class).playWith(card);
		} else {
			return myPlaySpaceRace(who, card);
		}
	}

	// get CardEvent object by card number. Intended for searching a card from the {PLAYER}_EVENT_PLAYED_PROP_NAME property.
	public static CardEvent getCard(final int number) {
		CardEvent target = cardNumbers[number];
		
		if (target == null) {
			Set<GamePiece> pieces = Utilities.findAllPiecesMatching(new PieceFilter() {				
				public boolean accept(GamePiece piece) {
					final CardEvent event = getCard(piece);
					if (event == null)
						return false;
					return number == event.getCardNumber();
				}
			});
			if (pieces.size() != 1) {
				JOptionPane.showMessageDialog(GameModule.getGameModule().getPlayerWindow(),
						"The current game is in an illegal state\n" +
						"It may help to resync the game (or it may not).\n" +
						"After the following dialog which reports an error,\n" +
						"try resyncing the game (right-click on your opponent's\n" +
						"name in the server controls.",
						"Well this is interesting", 
						JOptionPane.ERROR_MESSAGE);
				throw new IllegalStateException("Card Number: " + number + "; " + pieces.size() + "copies.");
			}
			target = (CardEvent) pieces.iterator().next();
			cardNumbers[number] = target;
		}
		
		return target;
	}

	// find a single instance of a particular CardEvent based on its class
	@SuppressWarnings("unchecked")
	public static <T extends CardEvent> T getCard(final Class<T> cl) {
		T card = (T) cardEvents.get(cl);
		if (card == null) {
			final Set<GamePiece> pieces = Utilities.findAllPiecesMatching(new PieceFilter() {
				public boolean accept(GamePiece piece) {
					return cl.isInstance(getCard(piece));
				}
			});
			if (pieces.size() != 1) {
				JOptionPane.showMessageDialog(GameModule.getGameModule().getPlayerWindow(),
						"The current game is in an illegal state\n" +
						"It may help to resync the game (or it may not).\n" +
						"After the following dialog which reports an error,\n" +
						"try resyncing the game (right-click on your opponent's\n" +
						"name in the server controls.",
						"Well this is interesting", 
						JOptionPane.ERROR_MESSAGE);
				throw new IllegalStateException("Card: " + cl + "; " + pieces.size() + "copies.");
			}
			card = (T) pieces.iterator().next();
			cardEvents.put(cl, card);
		}
		
		return card;
	}

	public boolean isEventPlayedThisTurn() {
		final int turn = TSTurnTracker.getCurrentTurn();
		return turn == turnEventPlayed;
	}

	public boolean isEventPlayedThisRound() {
		final int round = TSTurnTracker.getCurrentRound();
		return round == roundEventPlayed;
	}

	public Command sendCardToHand(final String who) {
		final CountingPlayerHand hand = CountingPlayerHand.getHand(who);
		return sendCardToHand(hand);
	}
	
	public Command sendCardToHand(final CountingPlayerHand hand) {
		final GamePiece outermost = getOutermost(this);
		Command comm = hand.placeOrMerge(outermost, hand.snapTo(HAND_DESTINATION));
		final ChangeTracker tracker = new ChangeTracker(this);
		myClearState();
		owner = hand.getMapName().equals(CountingPlayerHand.SOVIET_HAND) ? TSPlayerRoster.USSR : TSPlayerRoster.US;
		setCurrentPosition();
		comm = comm.append(tracker.getChangeCommand());
		hand.repaint();
		return comm;
	}
	
	public Command setCurrentPosition() {
		final ChangeTracker tracker = new ChangeTracker(this);
		currentMapName = getMap().getMapName();
		currentPosition = getPosition();		
		return tracker.getChangeCommand();
	}

	protected void myClearState() {
		setEventPlayed(false);
		opsPlayed = false;
		spaceRacePlayed = false;
		opsPlayedFirst = false;
		eventFinished = false;
		parentCardNumber = 0;
		undoPlayOpsCommand = null;
		undoPlayEventCommand = null;
	}

	public Command discard() {
		final DrawPile pile;
		if (isRemoveFromPlay() && isEventPlayed())
			pile = DrawPile.findDrawPile(REMOVED_FROM_PLAY_DECK_NAME);
		else
			pile = DrawPile.findDrawPile(DISCARDS);
		Command comm = setOwner(null);
		comm = comm.append(pile.addToContents(getOutermost(this)));
		comm = comm.append(setCurrentPosition());
		pile.getMap().repaint();
		return comm;
	}
	
	@Override
	public Object getLocalizedProperty(Object key) {
		if (CARD_NUMBER.equals(key)) {
			final StringBuilder sb = new StringBuilder(Integer.toString(getCardNumber()));
			if (isOptional())
				sb.append(" - OPTIONAL");
			return sb.toString();
		}
		else if (WAR.equals(key))
			return Integer.toString(getWar());
		else if (OPS.equals(key))
			return Integer.toString(getCardOps());
		else if (CARD_SIDE.equals(key)) 
			return Integer.toString(SIDE_STRING.indexOf(getCardSide()));
		else if (REMOVE_FROM_PLAY.equals(key))
			return isRemoveFromPlay() ? "1" : "0";
		else if (MAY_NOT_BE_HELD.equals(key))
			return isScoringCard() ? "1" : "0";
		else
			return super.getLocalizedProperty(key);
	}

	@Override
	public Object getProperty(Object key) {
		if (CARD_NUMBER.equals(key)) {
			final StringBuilder sb = new StringBuilder(Integer.toString(getCardNumber()));
			if (isOptional())
				sb.append(" - OPTIONAL");
			return sb.toString();
		}
		else if (WAR.equals(key))
			return Integer.toString(getWar());
		else if (OPS.equals(key))
			return Integer.toString(getCardOps());
		else if (CARD_SIDE.equals(key))
			return Integer.toString(SIDE_STRING.indexOf(getCardSide()));
		else if (REMOVE_FROM_PLAY.equals(key))
			return isRemoveFromPlay() ? "1" : "0";
		else if (MAY_NOT_BE_HELD.equals(key))
			return isScoringCard() ? "1" : "0";
		else
			return super.getProperty(key);
	}

	public static Deck getDeckNamed(final String name) {	
		final GamePiece[] gamePieces = Map.getMapById(DECKS).getPieces();
		for (GamePiece gp : gamePieces)
			if (gp instanceof Deck) {
				Deck deck = (Deck) gp;
				if (deck.getDeckName().equals(name))
					return deck;
			}
		throw new IllegalArgumentException(name);
	}

	public static Deck getDiscardDeck() {
		return CardEvent.getDeckNamed(DISCARDS);
	}

	// get a list of the cards in the Discard Pile
	public static Set<CardEvent> getDiscardedCards() {
		final Deck deck = CardEvent.getDiscardDeck();
		final Iterator<GamePiece> iter = deck.getPiecesIterator();
		final Set<CardEvent> cards = new HashSet<CardEvent>(deck.getPieceCount());
		while (iter.hasNext())
			cards.add((CardEvent) iter.next());
		return cards;
	}
	
	public static Command selectDiscardCard(
			final String who, 
			String title, 
			String message, 
			final PieceFilter filter, 
			final boolean canCancel) 
	{
		if (title == null)
			title = "Discard a Card";
		if (message == null) {
			final StringBuilder builder = new StringBuilder(who).append(" player ");
			if (canCancel)
				builder.append("may");
			else
				builder.append("must");
			builder.append(" discard one card.");
			message = builder.toString();
		}
		final CardEvent target = (CardEvent) selectCard(CountingPlayerHand.getHand(who).getAllCards(), title, message, filter, canCancel);
		Command comm;
		final Chatter chatter = GameModule.getGameModule().getChatter();
		if (target != null) {
			final StringBuilder builder = new StringBuilder(who).append(" discards ").append(target.getName()).append('.');
			comm = new Chatter.DisplayText(chatter, builder.toString());
			comm.execute();
			comm = comm.append(target.discard());
		}
		else {
			final StringBuilder builder = new StringBuilder(who).append(" does not discard a card.");
			comm = new Chatter.DisplayText(chatter, builder.toString());
			comm.execute();
		}
		return comm;
	}
	
	public boolean isFlipped() {
		return getOutermost(this).getProperty(CardEvent.OBSCURED_BY) != null;
	}

	// The side we're waiting on
	public String activeSide() {
		final String side = getCardSide();
		if (TSPlayerRoster.BOTH.equals(side))
			return getOwner();
		else
			return side;
	}

	public boolean isOpponentsCard() {
		if (getOwner() == null)
			return false;
		return (TSPlayerRoster.USSR.equals(getOwner()) ? TSPlayerRoster.US : TSPlayerRoster.USSR).equals(getCardSide());
	}
	
	public static CardEvent eventCurrentlyPlaying() {
		for (String side : new String[] {TSPlayerRoster.US, TSPlayerRoster.USSR}) {
			final String propertyName = TSPlayerRoster.US.equals(side) ? AMERICAN_CARD_PLAYED : SOVIET_CARD_PLAYED;
			String propertyValue = Utilities.getGlobalProperty(propertyName).getPropertyValue();
			if (Utilities.FALSE.equals(propertyValue) || Integer.valueOf(propertyValue) == -1)
				continue;
			final CardEvent card = getCard(Integer.valueOf(propertyValue));
			if (card.isEventPlayed() && !card.isEventFinished())
				return card;
		}
		return null;
	}

	public Command updateState() {
		return null;
	}
	
	public Icon asIcon() {
		return new Icon() {		
			public void paintIcon(Component c, Graphics g, int x, int y) {
				draw(g, x+getIconWidth()/2, y+getIconHeight()/2, c, 0.5);
			}		
			public int getIconWidth() {
				return boundingBox().width/2;
			}			
			public int getIconHeight() {
				return boundingBox().height/2;
			}
		};
	}

	public Command setFinished(final boolean b) {
		if (eventFinished == b)
			return null;
		final ChangeTracker tracker = new ChangeTracker(this);
		eventFinished = b;
		return tracker.getChangeCommand();
	}

	public Command setEventPlayed(boolean eventPlayed) {
		if (eventPlayed == this.eventPlayed)
			return null;
		final ChangeTracker tracker = new ChangeTracker(this);
		this.eventPlayed = eventPlayed;
		return tracker.getChangeCommand();
	}
}