package ca.mkiefte;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;

import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.Map;
import VASSAL.build.module.PlayerHand;
import VASSAL.build.module.PlayerRoster;
import VASSAL.build.module.map.DrawPile;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.counters.Deck;
import VASSAL.counters.GamePiece;
import VASSAL.counters.Stack;
import VASSAL.tools.SequenceEncoder;
import ca.mkiefte.cards.CardEvent;
import ca.mkiefte.cards.TheChinaCard;

public final class CountingPlayerHand extends PlayerHand implements CommandEncoder {
	
	public static final String COMMAND_PREFIX = "CHANGEVISIBILITY:";
	public static final String SOVIET_HAND = "Soviet Hand";
	public static final String AMERICAN_HAND = "American Hand";
	
	public static class ChangeVisibility extends Command {
		private CountingPlayerHand target;
		private boolean isVisible;
		
		public ChangeVisibility(final CountingPlayerHand target, final boolean isVisible) {
			this.target = target;
			this.isVisible = isVisible;
		}

		@Override
		protected void executeCommand() {
			final String side = PlayerRoster.getMySide();
			if (side == null)
				return;
			final String mapName = target.getMapName();
			if (target.visibleToAll != isVisible) {
				if ((mapName.equals(CountingPlayerHand.SOVIET_HAND) && side.equals(TSPlayerRoster.US)
						|| mapName.equals(CountingPlayerHand.AMERICAN_HAND) && side.equals(TSPlayerRoster.USSR))) {
					target.setVisibility(isVisible);
				}
				else
					target.visibleToAll = isVisible;
			}
		}

		@Override
		protected Command myUndoCommand() {
			return new ChangeVisibility(target, !isVisible);
		}
	}

	public void setVisibility(boolean isVisible) {
		visibleToAll = isVisible;
		getLaunchButton().setEnabled(isVisible);
		theMap.getTopLevelAncestor().setVisible(isVisible);
		theMap.revalidate();
	}

	public String encode(final Command c) {
		if (c instanceof ChangeVisibility) {
			final ChangeVisibility cv = (ChangeVisibility) c;
			final SequenceEncoder se = new SequenceEncoder(':');
			se.append(cv.target.getMapName()).append(cv.isVisible);
			return COMMAND_PREFIX + se.getValue();
		}
		else
			return null;
	}
	
	public Command decode(final String s) {
		if (s.startsWith(COMMAND_PREFIX)) {
			final SequenceEncoder.Decoder se = new SequenceEncoder.Decoder(s.substring(COMMAND_PREFIX.length()), ':');
			String id = se.nextToken();
			final boolean isVisible = se.nextBoolean(false);
			final CountingPlayerHand target = (CountingPlayerHand) Map.getMapById(id);
			return new ChangeVisibility(target, isVisible);
		}
		else
			return null;
	}

	public int countCards() {
		return getAllCards().size();
	}
	
	@Override
	public void repaint() {
		super.repaint();
		redrawCardCount();
	}

	protected void redrawCardCount() {
		final int count = countCards();
//		final TheChinaCard card = CardEvent.getCard(TheChinaCard.class);
		final StringBuilder builder = new StringBuilder("").append(count);
//		if (card != null && card.getMap().equals(this)) {
//			if (Decorator.getOutermost(card).getProperty(CardEvent.OBSCURED_BY) == null)
//				builder.append('c');
//			else
//				builder.append('(c)');
//		}
		getLaunchButton().setText(builder.toString());
	}

	@Override
	public void repaint(final Rectangle r) {
		super.repaint(r);
		redrawCardCount();
	}
	
	@Override
	public void addTo(final Buildable b) {
		super.addTo(b);
		GameModule.getGameModule().addCommandEncoder(this); 
	}	
	
	public JPanel getTheMap() {
		return theMap;
	}

	public Command dealCard() {
		final Chatter chatter = GameModule.getGameModule().getChatter();
		final Deck deck = getDeck();
		if (deck.getPieceCount() == 0) {
			final Command chat = new Chatter.DisplayText(chatter, "!!! FATAL ERROR: THERE ARE NO CARDS IN THE DRAW PILE!");
			chat.execute();
			return chat;
		}
		final GamePiece gp = deck.drawCards().nextPiece();
		if (CardEvent.getCard(gp) == null) {
			final Command chat = new Chatter.DisplayText(chatter, "!!! FATAL ERROR: THERE'S SOMETHING OTHER THAN A CARD IN THE DRAW PILE!:" + gp.getName());
			chat.execute();
			return chat;
		}
		final ChangeTracker tracker = new ChangeTracker(gp);
		gp.setProperty(CardEvent.OBSCURED_BY, null);
		Command comm = tracker.getChangeCommand();
		comm = comm.append(CardEvent.getCard(gp).sendCardToHand(this));
		if (deck.getPieceCount() == 0) {
			comm = comm.append(CardEvent.getDeckNamed(CardEvent.DISCARDS).sendToDeck());
			comm = comm.append(TSTurnTracker.setCurrentPositionInDeck(CardEvent.DRAW_DECK));
		}
		return comm;
	}

	private static Deck drawDeck;
	public static Deck getDeck() {
		if (drawDeck == null) {
			final DrawPile dp = getDrawPile();
			final int xPos = Integer.valueOf(dp.getAttributeValueString(DrawPile.X_POSITION));
			final int yPos = Integer.valueOf(dp.getAttributeValueString(DrawPile.Y_POSITION));
			final Point deckPos = new Point(xPos, yPos);
			GamePiece[] gamePieces = getDecks().getPieces();
			for (GamePiece gp : gamePieces)
				if (gp.getPosition().equals(deckPos) && gp instanceof Deck) {
					drawDeck = (Deck) gp;
					break;
				}
		}
		return drawDeck;
	}

	protected static Map getDecks() {
		return Map.getMapById(CardEvent.DECKS);
	}

	protected static DrawPile getDrawPile() {
		final List<DrawPile> dpList = getDecks().getComponentsOf(DrawPile.class);
		for (DrawPile dp : dpList) {
			if (dp.getConfigureName().equals(CardEvent.DRAW_DECK))
				return dp;
		}
		return null;
	}

	// get a player's hand
	public static CountingPlayerHand getHand(final String who) {
		if (who.equals(TSPlayerRoster.USSR) || who.equals(SOVIET_HAND))
			return (CountingPlayerHand) Map.getMapById(SOVIET_HAND);
		else 
			return (CountingPlayerHand) Map.getMapById(AMERICAN_HAND);
	}

	public Set<CardEvent> getAllCards() {
		GamePiece[] pieces = getAllPieces();
		final Set<CardEvent> cards = new HashSet<CardEvent>(pieces.length);
		for (GamePiece piece : pieces) {
			if (piece instanceof Stack) {
				final Stack stack = (Stack) piece;
				for (int i = 0; i < stack.getPieceCount(); ++i) {
					final CardEvent card = CardEvent.getCard(stack.getPieceAt(i));
					if (!(card instanceof TheChinaCard))
						cards.add(card);
				}
			} else {
				final CardEvent card = CardEvent.getCard(piece);
				if (!(card instanceof TheChinaCard))
					cards.add(card);
			}
		}
		return cards;
	}
}