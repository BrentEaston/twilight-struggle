package ca.mkiefte.cards;

import static ca.mkiefte.Utilities.adjustVps;

import java.util.Collections;
import java.util.Set;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.command.NullCommand;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceFilter;
import ca.mkiefte.CountingPlayerHand;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.TSTurnTracker;

public final class UnIntervention extends ParentCard {
	public static final String ID = "unintervention;";
	public static final String DESCRIPTION = "UN Intervention";
	
	public UnIntervention() {
		this(ID, null);
	}

	public UnIntervention(final String type, final GamePiece inner) {
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
		Command comm = new NullCommand();
		if (CardEvent.getCard(U2Incident.class).isEventInEffect()) {
			comm = comm.append(adjustVps(-1));
			final Command chat = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), 
					"USSR receives +1 VP (U2 Incident).");
			chat.execute();
			comm = comm.append(chat);
		}
		if (!TSTurnTracker.isGameOver())
			comm = comm.append(super.myPlayEvent(who));
		return comm;
	}

	public Command playWith(final CardEvent event) {
		ChangeTracker tracker = new ChangeTracker(this);
		childCardNumber = event.getCardNumber();
		Command comm = tracker.getChangeCommand();
		tracker = new ChangeTracker(event);
		event.parentCardNumber = getCardNumber();
		comm = comm.append(tracker.getChangeCommand());
		comm = comm.append(playEvent(getOwner()));
		undoPlayEventCommand = comm.getUndoCommand();
		return comm;
	}

	@Override
	protected boolean canPlayHeadline() {
		return false;
	}

	@Override
	public boolean isEventPlayable(final String who) {
		if (!super.isEventPlayable(who))
			return false;
		return TSTurnTracker.getCurrentRound() != 0;
	}
	
	@Override
	public boolean canPlayEvent() {	
		if (!super.canPlayEvent())
			return false;
		final String owner = getOwner();
		final String opponent = TSPlayerRoster.US.equals(owner) ? TSPlayerRoster.USSR : TSPlayerRoster.US;
		final Set<CardEvent> cards = CountingPlayerHand.getHand(owner).getAllCards();
		for (CardEvent piece: cards) {
			if (piece.getCardSide().equals(opponent))
				return true;
		}
		return false;
	}

	@Override
	protected Set<CardEvent> getEligibleCards() {
		if (childCardNumber != 0)
			return Collections.singleton(getCard(childCardNumber));
		else
			return CountingPlayerHand.getHand(getOwner()).getAllCards();
	}

	@Override
	protected PieceFilter getPieceFilter() {
		return new PieceFilter() {		
			public boolean accept(final GamePiece piece) {
				final CardEvent card = getCard(piece);
				final String owner = getOwner();
				final String opponent = TSPlayerRoster.US.equals(owner) ? TSPlayerRoster.USSR : TSPlayerRoster.US;
				return card != null && card.getCardSide().equals(opponent);
			}
		};
	}

	@Override
	protected String noChildCardMessage() {
		final StringBuilder builder = new StringBuilder(getOwner()).append(" does not have any cards with opponent's associated events.");
		return builder.toString();
	}

	@Override
	protected ChildCardAction getChildCardAction(final CardEvent event) {
		return ChildCardAction.PLAY_OPS;
	}

	@Override
	protected String whoPlaysChildCard(final CardEvent event) {
		return getOwner();
	}

	@Override
	protected String whoSelectsCard() {
		return getOwner();
	}

	@Override
	protected String getMessage() {
		return "Select an opponent's associated event to play for Operations.";
	}
}
