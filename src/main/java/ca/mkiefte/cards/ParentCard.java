package ca.mkiefte.cards;

import java.util.Set;

import javax.swing.JOptionPane;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.command.NullCommand;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceFilter;
import VASSAL.tools.SequenceEncoder;
import ca.mkiefte.Influence;
import ca.mkiefte.TSTurnTracker;
import ca.mkiefte.Utilities;

public abstract class ParentCard extends CardEvent {

	protected enum ChildCardAction {
		DISCARD,
		PLAY_EVENT,
		PLAY_OPS,
		PLAY_CARD;
	}
	
	protected int childCardNumber;

	public ParentCard(String type, GamePiece inner) {
		super(type, inner);
	}

	protected abstract Set<CardEvent> getEligibleCards();
	
	protected abstract PieceFilter getPieceFilter();
	
	protected abstract String noChildCardMessage();
	
	protected abstract ChildCardAction getChildCardAction(CardEvent event);
	
	protected abstract String whoPlaysChildCard(CardEvent event);
	
	protected abstract String whoSelectsCard();
	
	protected abstract String getMessage();

	protected Command selectCard(final Set<CardEvent> cards) {
		Command comm = new NullCommand();
		Command chat;
		final Chatter chatter = GameModule.getGameModule().getChatter();
		ChangeTracker tracker = new ChangeTracker(this);
		CardEvent childCard;
		
		if (cards == null || cards.size() == 0) {
			chat = new Chatter.DisplayText(chatter, noChildCardMessage());
			chat.execute();
			comm = comm.append(chat);
			eventFinished = true;
			comm = comm.append(tracker.getChangeCommand());
			if (undoPlayEventCommand != null)
				undoPlayEventCommand = undoPlayEventCommand.append(comm.getUndoCommand());
			return comm;
		} else if (cards.size() == 1)
			childCard = cards.iterator().next();
		else {
			childCard = selectCard(cards, getDescription(), getMessage(), getPieceFilter(), canUndoEvent());
			if (childCard == null) {
				eventFinished = true;
				comm = comm.append(tracker.getChangeCommand());
				if (undoPlayEventCommand != null)
					undoPlayEventCommand = undoPlayEventCommand.append(comm.getUndoCommand());
				return undoPlayEvent();
			}
		}
		childCardNumber = childCard.getCardNumber();
		comm = comm.append(tracker.getChangeCommand());
		tracker = new ChangeTracker(childCard);
		if (childCard != this)
			childCard.myClearState();
		childCard.parentCardNumber = getCardNumber();
		comm = comm.append(tracker.getChangeCommand());
		if (undoPlayEventCommand != null)
			undoPlayEventCommand = undoPlayEventCommand.append(comm.getUndoCommand());
		return comm;
	}
	
	protected Command playChildCard() {
		final CardEvent childCard = getCard(childCardNumber);
		Command chat;
		final Chatter chatter = GameModule.getGameModule().getChatter();
		Command comm = new NullCommand();
		final ChangeTracker tracker = new ChangeTracker(this);
		final ChildCardAction action = getChildCardAction(childCard);
		StringBuilder builder;
		final String whoPlays = whoPlaysChildCard(childCard);
		switch (action) {
		case DISCARD:
			builder = new StringBuilder(childCard.getDescription()).append(" is discarded.");
			chat = new Chatter.DisplayText(chatter, builder.toString());
			chat.execute();
			comm = comm.append(chat);
			comm = comm.append(childCard.discard());
			eventFinished = true;
			comm = comm.append(tracker.getChangeCommand());
			comm = comm.append(getCard(WeWillBuryYou.class).awardVps(true, getOwner()));
			if (undoPlayEventCommand != null)
				undoPlayEventCommand = undoPlayEventCommand.append(comm.getUndoCommand());
			break;
		case PLAY_EVENT:
			childCardNumber = childCard.getCardNumber();
			if (childCard.isEventPlayable(whoPlays)) {
				Command evt = childCard.myPlayEvent(whoPlays);
				if (childCard.eventFinishedOnExit()) {
					eventFinished = true;
					comm = comm.append(tracker.getChangeCommand());
					if (undoPlayEventCommand != null)
						undoPlayEventCommand = undoPlayEventCommand.append(comm.getUndoCommand());
				}
				childCard.undoPlayEventCommand = evt.getUndoCommand();
				comm = comm.append(evt);
			} else {
				builder = new StringBuilder(childCard.getDescription()).append(" event is not currently playable.");
				chat = new Chatter.DisplayText(chatter, builder.toString());
				chat.execute();
				comm = comm.append(chat);
				comm = comm.append(childCard.placeCardOnBoard(whoPlays));
				eventFinished = true;
				comm = comm.append(tracker.getChangeCommand());
				if (undoPlayEventCommand != null)
					undoPlayEventCommand = undoPlayEventCommand.append(comm.getUndoCommand());
			}
			break;
		case PLAY_OPS:
			childCardNumber = childCard.getCardNumber();
			comm = comm.append(tracker.getChangeCommand());
			if (undoPlayEventCommand != null)
				undoPlayEventCommand = undoPlayEventCommand.append(comm.getUndoCommand());
			Command ops = playCardDialog(whoPlays, childCard, childCard.getCardOps(), false, null);
			childCard.undoPlayOpsCommand = ops.getUndoCommand();
			comm = comm.append(ops);
			break;
		case PLAY_CARD:
			comm = comm.append(playCardDialog(whoPlays, childCard, childCard.getCardOps(), true, null));
			childCardNumber = childCard.getCardNumber();
			final UnIntervention unIntervention = getCard(UnIntervention.class);
			if (unIntervention.childCardNumber == childCardNumber
					&& !(this instanceof UnIntervention))
				childCardNumber = unIntervention.getCardNumber();
			comm = comm.append(tracker.getChangeCommand());
		}
		comm = comm.append(updateState());
		return comm;
	}

	@Override
	public String myGetState() {
		final SequenceEncoder encoder = new SequenceEncoder(super.myGetState(), '+');
		encoder.append(childCardNumber);
		return encoder.getValue();
	}

	@Override
	public void mySetState(String newState) {
		final SequenceEncoder.Decoder decoder = new SequenceEncoder.Decoder(newState, '+');
		super.mySetState(decoder.nextToken());
		childCardNumber = decoder.nextInt(0);
	}

	@Override
	public boolean isEventFinished() {
		if (super.isEventFinished())
			return true;
		
		if (childCardNumber == 0)
			return false;

		final String whoPlayedOps = Utilities.getGlobalProperty(Influence.WHO_PLAYED_OPS).getPropertyValue();
		final CardEvent childCard = getCard(childCardNumber);
		final String whoPlays = whoPlaysChildCard(childCard);
		switch (getChildCardAction(childCard)) {
		case PLAY_EVENT:
			return !childCard.isEventPlayable(whoPlays) 
					|| childCard.isEventFinished();
		case PLAY_OPS:
			return childCard.isSpaceRacePlayed()
					|| childCard.isScoringCard()
					|| (childCard.isOpsPlayed() 
							&& (!Influence.isOpsRemaining() 
									|| !whoPlayedOps.equals(whoPlays)));
		case PLAY_CARD:
			if (childCard.isSpaceRacePlayed())
				return true;
			if (!childCard.isOpponentsCard()) {
				if (childCard.isEventPlayed())
					return childCard.isEventFinished();
				else if (childCard.isOpsPlayed())
					return !Influence.isOpsRemaining() || !whoPlayedOps.equals(whoPlays);
				else
					return false;
			} else {
				final boolean eventFinished = !childCard.isEventPlayable(whoPlays) 
						|| (childCard.isEventPlayed() && childCard.isEventFinished());
				final boolean opsFinished = childCard.isScoringCard() 
						|| (childCard.isOpsPlayed() 
								&& (!Influence.isOpsRemaining()
										|| !whoPlayedOps.equals(whoPlays)));
				return eventFinished && opsFinished;
			}
		case DISCARD:
			return false;
		default:
			return true;
		}
	}

	@Override
	protected boolean eventFinishedOnExit() {
		return false;
	}

	@Override
	public Command discard() {
		Command comm = super.discard();
		if (childCardNumber != 0 && childCardNumber != getCardNumber())
			comm = comm.append(getCard(childCardNumber).discard());
		final ChangeTracker tracker = new ChangeTracker(this);
		childCardNumber = 0;
		comm = comm.append(tracker.getChangeCommand());
		return comm;
	}

	@Override
	public String activeSide() {
		if (childCardNumber == 0)
			return whoSelectsCard();
		else {
			final CardEvent childCard = getCard(childCardNumber);
			final String who = whoPlaysChildCard(childCard);
			if (childCard.isEventPlayed() && !childCard.isEventFinished())
				return childCard.activeSide();
			else if (childCard.isOpsPlayed() && Influence.isOpsRemaining())
				return Utilities.getGlobalProperty(Influence.WHO_PLAYED_OPS).getPropertyValue();
			else 
				return who;
		}
	}

	@Override
	public Command updateState() {
		if (isEventFinished())
			return super.updateState();
		else if (childCardNumber == 0) {
			return selectCard(getEligibleCards()).append(TSTurnTracker.updateState());
		} else {
			final CardEvent card = getCard(childCardNumber);
			final ChildCardAction action = getChildCardAction(card);
			if (card.isEventPlayed() && !card.isEventFinished() && card != this)
				return card.updateState();
			else if (card.isOpsPlayed() && Influence.isOpsRemaining()) {
				Influence.getInfluenceDialog("Conduct Operations", new Influence.ConductOperationsDelegate(null) {
					@Override
					public Command cancel() {
						Command comm = super.cancel();
						comm = comm.append(undoPlayEvent());
						return comm;
					}

					@Override
					public boolean canCancel() {
						return canUndoEvent();
					}
				});
				return super.updateState();
			} else if (!card.isEventPlayed() && !card.isOpsPlayed() && !card.isSpaceRacePlayed()) {
				return playChildCard();
			} else if (action == ChildCardAction.PLAY_CARD && card.isOpponentsCard()) {
				if (!card.isEventPlayed() && card.isEventPlayable(whoPlaysChildCard(card))) {
					JOptionPane.showMessageDialog(GameModule.getGameModule().getPlayerWindow(),
							new StringBuilder(card.getDescription()).append(" Event is played automatically.").toString(),
							card.getDescription(),
							JOptionPane.INFORMATION_MESSAGE,
							asIcon());
					Command comm = card.playEvent(whoPlaysChildCard(card));
					comm = comm.append(updateState());
					return comm;
				} else if (!card.isOpsPlayed()) {
					JOptionPane.showMessageDialog(GameModule.getGameModule().getPlayerWindow(),
							"Ops are played automatically.",
							card.getDescription(),
							JOptionPane.INFORMATION_MESSAGE,
							asIcon());
					Command comm = card.playOps(whoPlaysChildCard(card));
					comm = comm.append(updateState());
					return comm;
				} else
					return super.updateState();
			}
		}
		return super.updateState();
	}

	@Override
	protected void myClearState() {
		super.myClearState();
		childCardNumber = 0;
	}

	@Override
	public boolean canUndoEvent() {
		if (!super.canUndoEvent())
			return false;
		if (childCardNumber == 0)
			return true;
		final CardEvent card = getCard(childCardNumber);
		if (!whoPlaysChildCard(card).equals(getOwner()))
			return false;
		switch (getChildCardAction(card)) {
		case PLAY_OPS:
			return card.canUndoPlayOps();
		case PLAY_EVENT:
			return card.canUndoEvent();
		case DISCARD:
			return whoSelectsCard().equals(getOwner());
		default:
			return false;
		}
	}

	@Override
	public Command undoPlayEvent() {
		Command comm = null;
		if (childCardNumber != 0) {
			final CardEvent card = getCard(childCardNumber);
			switch (getChildCardAction(card)) {
			case PLAY_OPS:
				comm = card.undoPlayOps(whoPlaysChildCard(card));
				break;
			case PLAY_EVENT:
				comm = card.undoPlayEvent();
				break;
			default:
				comm = new NullCommand();
			}
			return comm.append(super.undoPlayEvent());
		} else
			return super.undoPlayEvent();
	}
}
