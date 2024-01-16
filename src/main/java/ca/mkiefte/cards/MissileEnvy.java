package ca.mkiefte.cards;

import java.util.Set;

import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceFilter;
import VASSAL.tools.SequenceEncoder;
import ca.mkiefte.CountingPlayerHand;
import ca.mkiefte.TSPlayerRoster;

public final class MissileEnvy extends ParentCard {
	public static final String ID = "missileenvy;";
	public static final String DESCRIPTION = "Missile Envy";
	private String targetedPlayer;
	private int maxOps;

	public MissileEnvy() {
		this(ID, null);
	}

	public MissileEnvy(final String type, final GamePiece inner) {
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

	public String getTargetedPlayer() {
		return targetedPlayer;
	}

	@Override
	public String myGetState() {
		final SequenceEncoder encoder = new SequenceEncoder('!');
		encoder.append(super.myGetState());
		encoder.append(targetedPlayer);
		return encoder.getValue();
	}

	@Override
	public void mySetState(String newState) {
		final SequenceEncoder.Decoder decoder = new SequenceEncoder.Decoder(newState, '!');
		super.mySetState(decoder.nextToken());
		targetedPlayer = decoder.nextToken(null);
	}

	@Override
	protected boolean canPlayHeadline() {
		if (isEventInEffect())
			return false;
		else
			return super.canPlayHeadline();
	}

	@Override
	public Command discard() {
		if (isEventInEffect()) {
			if (targetedPlayer.equals(getOwner())) {
				final ChangeTracker tracker = new ChangeTracker(this);
				targetedPlayer = null;
				Command comm = tracker.getChangeCommand();
				comm = comm.append(super.discard());
				return comm;
			} else {
				Command comm = getCard(childCardNumber).discard();
				comm = comm.append(sendCardToHand(targetedPlayer));
				return comm;
			}
		} else
			return super.discard();
	}

	@Override
	public boolean isEventInEffect() {
		return targetedPlayer != null && !targetedPlayer.isEmpty();
	}

	@Override
	protected Set<CardEvent> getEligibleCards() {
		final Set<CardEvent> cards = CountingPlayerHand.getHand(TSPlayerRoster.US.equals(getOwner()) ? TSPlayerRoster.USSR : TSPlayerRoster.US).getAllCards();
		maxOps = 0;
		for (final CardEvent c : cards)
			if (c.getCardOps() > maxOps)
				maxOps = c.getCardOps();
		return cards;
	}

	@Override
	protected PieceFilter getPieceFilter() {
		return new PieceFilter() {		
			public boolean accept(final GamePiece piece) {
				final CardEvent card = getCard(piece);
				return card != null && card.getCardOps() == maxOps;
			}
		};
	}

	@Override
	protected String noChildCardMessage() {
		final StringBuilder builder = new StringBuilder(targetedPlayer).append(" has no cards.");
		return builder.toString();
	}

	@Override
	protected ChildCardAction getChildCardAction(final CardEvent event) {
		if (event.getCardSide().equals(targetedPlayer))
			return ChildCardAction.PLAY_OPS;
		else
			return ChildCardAction.PLAY_EVENT;
	}

	@Override
	protected String whoPlaysChildCard(final CardEvent event) {
		return getOwner();
	}

	@Override
	protected String whoSelectsCard() {
		return targetedPlayer;
	}

	@Override
	protected String getMessage() {
		return "Choose a card to give to opponent.\n"
		+ "If the card has an opponent event\n"
		+ "or an event applicable to both players,\n"
		+ "event will occur immediately.";
	}

	@Override
	public boolean canPlayEvent() {
		return super.canPlayEvent() && !isEventInEffect();
	}

	@Override
	public Command myPlayEvent(final String who) {
		Command comm = super.myPlayEvent(who);
		final ChangeTracker tracker = new ChangeTracker(this);
		targetedPlayer = TSPlayerRoster.US.equals(getOwner()) ? TSPlayerRoster.USSR : TSPlayerRoster.US;
		comm = comm.append(tracker.getChangeCommand());
		return comm;
	}

	@Override
	public boolean canUndoPlayOps() {
		return super.canUndoPlayOps() && !getOwner().equals(targetedPlayer);
	}

	@Override
	public boolean canUndoEvent() {
		return super.canUndoEvent() && childCardNumber == 0;
	}	
}
