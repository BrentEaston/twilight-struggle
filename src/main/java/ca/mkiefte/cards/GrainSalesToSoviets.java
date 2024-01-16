package ca.mkiefte.cards;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceFilter;
import ca.mkiefte.CountingPlayerHand;
import ca.mkiefte.TSPlayerRoster;

public final class GrainSalesToSoviets extends ParentCard {
	public static final String ID = "grainsalestosoviets;";
	public static final String DESCRIPTION = "Grain Sales To Soviets";
	private CardEvent other;

	public GrainSalesToSoviets() {
		this(ID, null);
	}

	public GrainSalesToSoviets(final String type, final GamePiece inner) {
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
		Set<CardEvent> cards;
		if (other == null) {
			cards = CountingPlayerHand.getHand(TSPlayerRoster.USSR).getAllCards();
			final int size = cards.size();
			if (size > 0) {
				final int index = GameModule.getGameModule().getRNG().nextInt(size);
				other = new ArrayList<CardEvent>(cards).get(index);
			}
		}		
		cards = new HashSet<CardEvent>(2);
		cards.add(this);
		if (other != null)
			cards.add(other);
		return cards;
	}

	@Override
	protected PieceFilter getPieceFilter() {
		return null;
	}

	@Override
	protected String noChildCardMessage() {
		return null;
	}

	@Override
	protected ChildCardAction getChildCardAction(final CardEvent event) {
		if (event == this)
			return ChildCardAction.PLAY_OPS;
		else
			return ChildCardAction.PLAY_CARD;
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
		final Set<CardEvent> cards = getEligibleCards();
		cards.remove(this);
		final CardEvent other = cards.iterator().next();
		final StringBuilder builder = new StringBuilder("Select ").append(getDescription()).append(" to play it for Ops\nor ");
		builder.append(other.getDescription()).append(" to play for Event and/or Ops.");
		return builder.toString();
	}

	@Override
	public String activeSide() {
		if (childCardNumber == getCardNumber())
			return TSPlayerRoster.US;
		else 
			return super.activeSide();
	}

	@Override
	protected void myClearState() {
		super.myClearState();
		other = null;
	}

	@Override
	protected Command playChildCard() {
		Command comm;
		if (other == null)
			comm = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), "No card drawn.");
		else
			comm = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), new StringBuilder(other.getDescription()).append(" drawn from Soviets.").toString());
		comm.execute();
		comm = comm.append(super.playChildCard());
		return comm;
	}

	@Override
	public boolean canUndoEvent() {
		return super.canUndoEvent() && CountingPlayerHand.getHand(TSPlayerRoster.USSR).getAllCards().size() == 0;
	}

	@Override
	public Command updateState() {
		if (childCardNumber == getCardNumber() && !isOpsPlayed() && !isSpaceRacePlayed())
			return playChildCard();			
		else
			return super.updateState();
	}
}
