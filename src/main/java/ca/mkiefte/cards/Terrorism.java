package ca.mkiefte.cards;

import java.util.HashSet;
import java.util.Set;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.CountingPlayerHand;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.Utilities;

public final class Terrorism extends CardEvent {

	private static final String DESCRIPTION = "Terrorism";
	static final public String ID = "terrorism;";

	public Terrorism() {
		this(ID, null);
	}

	public Terrorism(final String type, final GamePiece inner) {
		super(type, inner);
	}

	@Override
	protected String getIdName() {
		return ID;
	}

	@Override
	public Command myPlayEvent(final String who) {
		final GameModule gameModule = GameModule.getGameModule();
		final Chatter chatter = gameModule.getChatter();
		Command comm = super.myPlayEvent(who);
		final String owner = getOwner();
		final String opponent = TSPlayerRoster.US.equals(owner) ? TSPlayerRoster.USSR : TSPlayerRoster.US;
		int nCards;
		if (TSPlayerRoster.USSR.equals(owner) && getCard(IranianHostageCrisis.class).isEventPlayed()) {
			nCards = 2;
			final Command chat = new Chatter.DisplayText(chatter, "Iranian Hostage Crisis is in effect.");
			chat.execute();
			comm = comm.append(chat);
		} else
			nCards = 1;
		final Set<CardEvent> list = CountingPlayerHand.getHand(opponent).getAllCards();
		int handSize = list.size();
		nCards = nCards > handSize ? handSize : nCards;
		
		if (nCards == 0) {
			final StringBuilder builder = new StringBuilder(opponent).append(" has no cards.");
			Command chat = new Chatter.DisplayText(chatter, builder.toString());
			chat.execute();
			comm = comm.append(chat);
			return comm;
		}
		
		final Set<String> cardNames = new HashSet<String>(nCards);		
		for (int i = 0; i < nCards; ++i) {
			final int index = gameModule.getRNG().nextInt(handSize);
			final CardEvent card = list.toArray(new CardEvent[handSize])[index];
			cardNames.add(card.getDescription());
			comm = comm.append(card.discard());
			list.remove(card);
			--handSize;
		}
		
		final StringBuilder builder = new StringBuilder(opponent).append(" discards");
		Utilities.listAsString(builder, cardNames, "and");
		Command chat = new Chatter.DisplayText(gameModule.getChatter(), builder.toString());
		chat.execute();
		comm = comm.append(chat);
		return comm;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public boolean canUndoEvent() {
		return false;
	}
}