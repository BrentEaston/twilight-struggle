package ca.mkiefte.cards;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;

public abstract class BonusCard extends CardEvent {

	public BonusCard(String type, GamePiece inner) {
		super(type, inner);
	}
	
	protected abstract String getRecipient();

	@Override
	protected Command myPlayEvent(final String who) {
		Command comm = super.myPlayEvent(who);
		final Chatter chatter = GameModule.getGameModule().getChatter();
		final StringBuilder builder = new StringBuilder(getRecipient()).append(" adds +1 to all card Operations Values to a maximum of 4.");
		final Command chat = new Chatter.DisplayText(chatter, builder.toString());
		chat.execute();
		comm = comm.append(chat);
		return comm;
	}

	@Override
	public boolean isEventInEffect() {
		return isEventPlayedThisTurn();
	}
}
