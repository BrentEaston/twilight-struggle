package ca.mkiefte.cards;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.Utilities;

public final class NixonPlaysTheChinaCard extends CardEvent {
	public static final String ID = "nixonplaysthechinacard;";
	public static final String DESCRIPTION = "Nixon Plays the China Card*";

	public NixonPlaysTheChinaCard() {
		this(ID, null);
	}

	public NixonPlaysTheChinaCard(String type, GamePiece inner) {
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
		final TheChinaCard chinaCard = getCard(TheChinaCard.class);
		if (TSPlayerRoster.USSR.equals(chinaCard.getOwner())) {
			comm = comm.append(chinaCard.sendCardToHand(TSPlayerRoster.US));
			comm = comm.append(chinaCard.setAvailable(false));
			final Chatter chatter = GameModule.getGameModule().getChatter();
			final Command chat = new Chatter.DisplayText(chatter, "US receives The China Card face down and unavailable for play.");
			chat.execute();
			comm = comm.append(chat);
		} else
			comm = comm.append(Utilities.adjustVps(+2));
		return comm;
	}
}
