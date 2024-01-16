package ca.mkiefte.cards;

import java.util.HashSet;
import java.util.Set;

import ca.mkiefte.CountingPlayerHand;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.Utilities;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceFilter;

/**
 * US player exposes hand for the rest of the turn. USSR then chooses a card from US hand and discards it.
 * @author Michael Kiefte
 */
public final class AldrichAmesRemix extends ParentCard {
	
	public static final String ID = "aldrichamesremix;";
	public static final String DESCRIPTION = "Aldrich Ames Remix*";

	public AldrichAmesRemix() {
		this(ID, null);
	}

	public AldrichAmesRemix(final String type, final GamePiece inner) {
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
	protected Set<CardEvent> getEligibleCards() {
		return CountingPlayerHand.getHand(TSPlayerRoster.US).getAllCards();
	}

	@Override
	protected PieceFilter getPieceFilter() {
		return null;
	}

	@Override
	protected String noChildCardMessage() {
		return "US player has no cards to discard.";
	}

	@Override
	protected ChildCardAction getChildCardAction(CardEvent event) {
		return ChildCardAction.DISCARD;
	}

	@Override
	protected String whoPlaysChildCard(CardEvent event) {
		return TSPlayerRoster.USSR;
	}

	@Override
	protected String whoSelectsCard() {
		return TSPlayerRoster.USSR;
	}

	@Override
	protected String getMessage() {
		return "Select a card from US hand to discard.";
	}

	@Override
	public Command updateState() {
		if (childCardNumber == 0) {
			Command chat;
			final Chatter chatter = GameModule.getGameModule().getChatter();
			final Set<CardEvent> hand = CountingPlayerHand.getHand(TSPlayerRoster.US).getAllCards();
			final int nCards = hand.size();
			if (nCards > 0) {
				final StringBuilder builder = new StringBuilder("US has the following cards:");
				final Set<String> descriptions = new HashSet<String>(nCards);
				for (final CardEvent card : hand)
					descriptions.add(card.getDescription());
				Utilities.listAsString(builder, descriptions, "and");
				chat = new Chatter.DisplayText(chatter, builder.toString());
				chat.execute();
				return chat.append(super.updateState());
			}
		}
		return super.updateState();
	}
}
