package ca.mkiefte.cards;

import static ca.mkiefte.Utilities.findPiece;
import static ca.mkiefte.Utilities.selectGamePiece;

import java.util.HashSet;
import java.util.Set;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.command.NullCommand;
import VASSAL.counters.Deck;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceFilter;
import ca.mkiefte.CountingPlayerHand;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.Utilities;

public final class OurManInTehran extends CardEvent {
	public static final String ID = "ourmanintehran;";
	public static final String DESCRIPTION = "Our Man in Tehran*";

	public OurManInTehran() {
		this(ID, null);
	}

	public OurManInTehran(final String type, final GamePiece inner) {
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
		final Command chat = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), 
				"US player may discard any of the top 5 cards from the draw pile.");
		chat.execute();
		comm = comm.append(chat);
		return comm;
	}

	@Override
	public Command updateState() {
		final Deck deck = CountingPlayerHand.getDeck();
		deck.shuffle();
		final Set<GamePiece> options = new HashSet<GamePiece>(5);
		Command comm = new NullCommand();
		for (int i = 0; i < 5; ++i) {
			final GamePiece gp = deck.getPieceAt(i);
			gp.setProperty(CardEvent.OBSCURED_BY, null);
			options.add(gp);
			if (deck.getPieceCount()-1 == i) {
				final Deck discardDeck = CardEvent.getDiscardDeck();
				discardDeck.shuffle();
				discardDeck.sendToDeck();
			}
		}
		final Set<GamePiece> choices = selectGamePiece(
				options, 
				getName(), 
				"Select which cards to discard or \"Cancel\" to discard none:\n"
				+ "(Remaning cards are sent back to draw pile.)\n"
				+ "CTRL/COMMAND & SHIFT to select multiple cards.", 
				null, false, true, true);
		final Chatter chatter = GameModule.getGameModule().getChatter();
		
		for (final GamePiece gp : options)
			gp.setProperty(CardEvent.OBSCURED_BY, Deck.NO_USER);
		if (choices == null || choices.isEmpty()) {
			Command chat = new Chatter.DisplayText(chatter, "No cards are discarded from the Draw Pile.");
			chat.execute();
			comm = comm.append(chat);
		}
		else {
			final Set<String> cardNames = new HashSet<String>();			
			for (final GamePiece gp : choices) {
				final ChangeTracker tracker = new ChangeTracker(gp);
				gp.setProperty(CardEvent.OBSCURED_BY, null);
				comm = comm.append(tracker.getChangeCommand());
				final CardEvent card = getCard(gp);
				comm = comm.append(card.discard());
				cardNames.add(card.getDescription());
			}
			final StringBuilder builder = new StringBuilder("US player discards");
			Utilities.listAsString(builder, cardNames, "and");
			Command chat = new Chatter.DisplayText(chatter, builder.toString());
			chat.execute();
			comm = comm.append(chat);
		}
		comm = comm.append(setFinished(true));
		return comm;
	}

	@Override
	public boolean isEventPlayable(final String who) {
		final GamePiece controlledCountry = findPiece(new PieceFilter() {				
			public boolean accept(final GamePiece piece) {
				final Influence marker = Influence.getInfluenceMarker(piece);
				return marker != null 
						&& TSPlayerRoster.US.equals(marker.getSide())
						&& Influence.MIDDLE_EAST.equals(marker.getRegion())
						&& marker.hasControl();
			}
		});
		return super.isEventPlayable(who) && controlledCountry != null;
	}

	@Override
	protected boolean eventFinishedOnExit() {
		return false;
	}

	@Override
	public boolean canUndoEvent() {
		return false;
	}
}
