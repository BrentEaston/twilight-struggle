package ca.mkiefte.cards;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.China;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.Utilities;

public final class CulturalRevolution extends CardEvent {
	public static final String ID = "culturalrevolution;";
	public static final String DESCRIPTION = "Cultural Revolution*";

	public CulturalRevolution() {
		this(ID, null);
	}

	public CulturalRevolution(String type, GamePiece inner) {
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
		if (TSPlayerRoster.US.equals(chinaCard.getOwner())) {
			comm = comm.append(chinaCard.sendCardToHand(TSPlayerRoster.USSR));
			comm = comm.append(chinaCard.setAvailable(true));
			final Chatter chatter = GameModule.getGameModule().getChatter();
			final Command chat = new Chatter.DisplayText(chatter, "USSR claims The China Card available for play.");
			chat.execute();
			comm = comm.append(chat);
		} else
			comm = comm.append(Utilities.adjustVps(-1));
		return comm;
	}

	@Override
	public boolean isEventPlayable(final String who) {
		return super.isEventPlayable(who) && !China.isChineseCivilWarInEffect();
	}
}
