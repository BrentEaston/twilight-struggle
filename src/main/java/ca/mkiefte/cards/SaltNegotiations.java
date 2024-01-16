package ca.mkiefte.cards;

import static ca.mkiefte.Utilities.adjustDefcon;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceFilter;

public final class SaltNegotiations extends CardEvent {
	public static final String ID = "saltnegotiations;";
	public static final String DESCRIPTION = "Salt Negotiations*";

	public SaltNegotiations() {
		this(ID, null);
	}

	public SaltNegotiations(final String type, final GamePiece inner) {
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
		comm = comm.append(adjustDefcon(+2));
		final Command chat = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), 
				"All coups attempts this turn have -1 die-roll modifier.");
		chat.execute();
		comm = comm.append(chat);
		return comm;
	}

	@Override
	public Command updateState() {
		final CardEvent target = selectCard(CardEvent.getDiscardedCards(), getName(), 
				"Select a card from the discard pile or \"Cancel\" to not.",
				new PieceFilter() {			
			public boolean accept(final GamePiece card) {
				final CardEvent event = getCard(card);
				return event != null && !event.isScoringCard();
			}
		}, true);
		Command comm;
		final Chatter chatter = GameModule.getGameModule().getChatter();
		final StringBuilder builder;
		final String owner = getOwner();
		if (target == null) {
			builder = new StringBuilder(owner).append(" does not reclaim a card from the discard pile.");
			comm = new Chatter.DisplayText(chatter, builder.toString());
			comm.execute();
		} else {
			builder = new StringBuilder(owner).append(" reclaims ").append(target.getDescription()).append(" from the discard pile.");
			comm = new Chatter.DisplayText(chatter, builder.toString());
			comm.execute();
			comm = comm.append(target.sendCardToHand(owner));
		}
		comm = comm.append(setFinished(true));
		return comm;
	}

	@Override
	protected boolean eventFinishedOnExit() {
		return false;
	}

	@Override
	public boolean isEventInEffect() {
		return isEventPlayedThisTurn();
	}
}
