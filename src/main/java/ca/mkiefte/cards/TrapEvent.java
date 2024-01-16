package ca.mkiefte.cards;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JOptionPane;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceFilter;
import ca.mkiefte.CountingPlayerHand;
import ca.mkiefte.TSTurnTracker;
import ca.mkiefte.Utilities;

public abstract class TrapEvent extends CardEvent {

	protected abstract String getTarget();
	
	protected Set<CardEvent> getEligibleCards() {
		final MissileEnvy missileEnvy = getCard(MissileEnvy.class);
		if (missileEnvy.isEventInEffect() 
				&& missileEnvy.getTargetedPlayer().equals(getTarget())
				&& missileEnvy.getOps() >= 2)
			return new HashSet<CardEvent>(Collections.singleton(missileEnvy));
		final CountingPlayerHand hand = CountingPlayerHand.getHand(getTarget());
		final Set<CardEvent> cards = hand.getAllCards();
		final Iterator<CardEvent> iter = cards.iterator();
		int nScoringCards = 0;
		int nEligible = 0;
		for (final CardEvent c : cards) {
			if (c.isScoringCard())
				++nScoringCards;
			if (c.getOps() >= 2)
				++nEligible;
		}
		if (nScoringCards >= TSTurnTracker.getNRoundsRemaing(getTarget())
				|| nEligible == 0) {
			while (iter.hasNext())
				if (!iter.next().isScoringCard())
					iter.remove();
		} else {
			while (iter.hasNext())
				if (iter.next().getOps() < 2)
					iter.remove();
		}
		return cards;
	}

	public TrapEvent(final String type, final GamePiece inner) {
		super(type, inner);
	}

	@Override
	public Command myPlayEvent(final String who) {
		Command comm = super.myPlayEvent(who);
		final Chatter chatter = GameModule.getGameModule().getChatter();
		final StringBuilder builder = new StringBuilder(getTarget()).append(" must discard a 2+ Op card and roll 1-4 in the next Round to cancel.");
		final Command chat = new Chatter.DisplayText(chatter, builder.toString());
		chat.execute();
		comm = comm.append(chat);
		return comm;
	}

	public Command discardCard() {
		final Set<CardEvent> cards = getEligibleCards();
		Command comm = null;
		final Chatter chatter = GameModule.getGameModule().getChatter();
		if (cards.size() == 0 || cards.iterator().next().isScoringCard()) {
			StringBuilder builder = new StringBuilder(getTarget()).append(" has no more appropriate cards to discard.");
			comm = new Chatter.DisplayText(chatter, builder.toString());
			builder = new StringBuilder(getTarget()).append(" may only play Scoring Cards for the rest of the Turn.");
			comm = comm.append(new Chatter.DisplayText(chatter, builder.toString()));
			comm.execute();
			CardEvent target = null;
			if (cards.size() == 0) {
				JOptionPane.showMessageDialog(GameModule.getGameModule().getFrame(), 
						"You don't have any appropriate cards to discard.",
						getDescription(),
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				builder = new StringBuilder("Select a Scoring Card to play.");
				final boolean optional = cards.size() < TSTurnTracker.getNRoundsRemaing(getTarget());
				if (optional)
					builder.append("\nPress \"Cancel\" to not play a Scoring Card.");
				target = selectCard(cards, 
						getDescription(), 
						builder.toString(),
						null, 
						optional);
			}
			if (target != null)
				comm = comm.append(target.playEvent(target.getOwner()));
			else 
				comm = comm.append(TSTurnTracker.pass(getTarget()));
		} else {
			comm = selectDiscardCard(getTarget(), getDescription(), null, new PieceFilter() {
				public boolean accept(final GamePiece piece) {
					return cards.contains(getCard(piece));
				}				
			}, false);
			final int[] roll = {0};
			comm = comm.append(Utilities.rollDie(getTarget(), roll));
			Command chat;
			if (roll[0] <= 4)
				comm = comm.append(cancelEvent());
			else {
				final StringBuilder builder = new StringBuilder(getDescription()).append(" continues...");
				chat = new Chatter.DisplayText(chatter, builder.toString());
				chat.execute();
				comm = comm.append(chat);
			}
			comm = comm.append(TSTurnTracker.pass(getTarget()));
		}
		return comm;
	}

	@Override
	final protected boolean isUnderlined() {
		return true;
	}
}