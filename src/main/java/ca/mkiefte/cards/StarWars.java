package ca.mkiefte.cards;

import static ca.mkiefte.Utilities.getGlobalProperty;

import java.util.Set;

import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceFilter;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.Utilities;

public final class StarWars extends ParentCard {
	public static final String ID = "starwars;";
	public static final String DESCRIPTION = "Star Wars*";

	public StarWars() {
		this(ID, null);
	}

	public StarWars(final String type, final GamePiece inner) {
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
	public boolean isEventPlayable(final String who) {
		final int americanSpaceRace = Integer.valueOf(getGlobalProperty(Utilities.AMERICAN_SPACE_RACE).getPropertyValue());
		final int sovietSpaceRace = Integer.valueOf(getGlobalProperty(Utilities.SOVIET_SPACE_RACE).getPropertyValue());
		return super.isEventPlayable(who) && americanSpaceRace > sovietSpaceRace;
	}

	@Override
	protected boolean eventFinishedOnExit() {
		return false;
	}

	@Override
	protected Set<CardEvent> getEligibleCards() {
		return getDiscardedCards();
	}

	@Override
	protected PieceFilter getPieceFilter() {
		return new PieceFilter() {			
			public boolean accept(final GamePiece piece) {
				final CardEvent event = getCard(piece);
				return !event.isScoringCard();
			}
		};
	}

	@Override
	protected String noChildCardMessage() {
		return "There are no playable non-Scoring Events in the discard pile.";
	}

	@Override
	protected ChildCardAction getChildCardAction(final CardEvent event) {
		return ChildCardAction.PLAY_EVENT;
	}

	@Override
	protected String whoPlaysChildCard(final CardEvent event) {
		return TSPlayerRoster.US;
	}

	@Override
	protected String whoSelectsCard() {
		return TSPlayerRoster.US;
	}

	@Override
	protected String getMessage() {
		return "Select a non-Scoring Event to play from the discard pile.";
	}
}
