package ca.mkiefte.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import VASSAL.build.GameModule;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceFilter;
import ca.mkiefte.CountingPlayerHand;
import ca.mkiefte.TSPlayerRoster;

public final class FiveYearPlan extends ParentCard {
	private static final String DESCRIPTION = "Five-Year Plan";
	static final public String ID = "fiveyearplan;";
	
	public FiveYearPlan() {
		this(ID, null);
	}

	public FiveYearPlan(final String type, final GamePiece inner) {
		super(type, inner);
	}

	@Override
	protected String getIdName() {
		return ID;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	protected Set<CardEvent> getEligibleCards() {
		final Set<CardEvent> cards = CountingPlayerHand.getHand(TSPlayerRoster.USSR).getAllCards();
		final int size = cards.size();
		if (size == 0)
			return null;
		final int index;
		if (size == 1)
			index = 0;
		else
			index = GameModule.getGameModule().getRNG().nextInt(size);
		return Collections.singleton(new ArrayList<CardEvent>(cards).get(index));
	}

	@Override
	protected String noChildCardMessage() {
		return "Soviet player has no cards to discard.";
	}

	@Override
	protected ChildCardAction getChildCardAction(final CardEvent event) {
		if (TSPlayerRoster.US.equals(event.getCardSide()))
			return ChildCardAction.PLAY_EVENT;
		else
			return ChildCardAction.DISCARD;
	}

	@Override
	protected String whoPlaysChildCard(final CardEvent event) {
		return TSPlayerRoster.US;
	}

	@Override
	protected PieceFilter getPieceFilter() {
		return null;
	}

	@Override
	protected String whoSelectsCard() {
		return TSPlayerRoster.US;
	}

	@Override
	protected String getMessage() {
		return null;
	}

	@Override
	public boolean canUndoEvent() {
		return super.canUndoEvent() && childCardNumber == 0;
	}
}