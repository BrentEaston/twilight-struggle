package ca.mkiefte.cards;

import java.util.HashSet;
import java.util.Set;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.CountingPlayerHand;
import ca.mkiefte.Influence;
import ca.mkiefte.Influence.Delegate;
import ca.mkiefte.Utilities;

public abstract class RevealHandEvent extends ConductOperations {
	
	private static final int OPERATIONS = 1;
	public static final String ID = "revealhand;";
	public static final String DESCRIPTION = "Reveal Hand Event";
	
	public RevealHandEvent(final String type, final GamePiece inner) {
		super(type, inner);
	}

	protected abstract String getTarget();
	
	@Override
	public Command updateState() {
		final String target = getTarget();
		final CountingPlayerHand hand = CountingPlayerHand.getHand(target);
		final Set<CardEvent> cards = hand.getAllCards();
		final int nCards = cards.size();
		StringBuilder builder = new StringBuilder(target).append(" hand is revealed.");
		Command comm = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), builder.toString());
		comm = comm.append(new CountingPlayerHand.ChangeVisibility(hand, true));
		Command chat;
		final Chatter chatter = GameModule.getGameModule().getChatter();
		if (nCards == 0)
			chat = new Chatter.DisplayText(chatter, new StringBuilder(target).append(" has no cards.").toString());
		else {
			builder = new StringBuilder(target).append(" has the following cards:");
			final Set<String> descriptions = new HashSet<String>(nCards);
			for (final CardEvent card : hand.getAllCards())
				descriptions.add(card.getDescription());
			Utilities.listAsString(builder, descriptions, "and");
			chat = new Chatter.DisplayText(chatter, builder.toString());
		}
		comm = comm.append(chat);
		comm.execute();
		
		return comm.append(super.updateState());
	}

	@Override
	protected int nInfluence() {
		return OPERATIONS;
	}

	@Override
	protected boolean passesFilter(final Influence inf) {
		return true;
	}

	@Override
	public boolean isEventInEffect() {
		return CountingPlayerHand.getHand(getTarget()).isVisibleToAll();
	}

	public Command hideOpponentsHand() {
		final CountingPlayerHand hand = CountingPlayerHand.getHand(getTarget());
		final Command comm = new CountingPlayerHand.ChangeVisibility(hand, false);
		comm.execute();
		return comm;
	}

	@Override
	protected Delegate getDelegate() {
		return new Influence.ConductOperationsDelegate(this) {
			@Override
			public boolean isOptional() {
				return true;
			}		
		};
	}

	@Override
	public boolean canUndoEvent() {
		return super.canUndoEvent() && CountingPlayerHand.getHand(getTarget()).getAllCards().size() == 0;
	}
}
