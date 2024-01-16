package ca.mkiefte.cards;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;

public final class Nato extends CardEvent {
	public static final String ID = "nato;";
	public static final String DESCRIPTION = "NATO*";
	
	public Nato() {
		this(ID, null);
	}

	public Nato(final String type, final GamePiece inner) {
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
	public boolean isEventPlayable(final String who) {
		return super.isEventPlayable(who) 
				&& (CardEvent.getCard(MarshallPlan.class).isEventPlayed() 
						|| CardEvent.getCard(WarsawPactFormed.class).isEventPlayed());
	}

	@Override
	public Command myPlayEvent(final String who) {
		Command comm = super.myPlayEvent(who);
		final Chatter chatter = GameModule.getGameModule().getChatter();
		Command chat = new Chatter.DisplayText(chatter, "USSR may no longer attempt Coups or Realignments rolls in US controlled countries in Europe.");
		chat = chat.append(new Chatter.DisplayText(chatter, "US controlled countries may no longer be targeted with Brush War."));
		chat.execute();
		comm = comm.append(chat);
		return comm;
	}

	public boolean canBrushWar(Influence marker) {
		if (TSPlayerRoster.USSR.equals(marker.getSide()))
			marker = marker.getOpponentInfluenceMarker();
		return canCoupOrRealign(marker, TSPlayerRoster.USSR);
	}
	
	public boolean canCoupOrRealign(Influence marker, final String who) {
		if (getCard(DeGaulleLeadsFrance.class).isEventInEffect() 
				&& Influence.FRANCE.equals(marker.getLocation()))
			return true;
		else if (getCard(WillyBrandt.class).isEventInEffect() 
				&& Influence.W_GERMANY.equals(marker.getLocation()))
			return true;
		else if (isEventInEffect() 
				&& TSPlayerRoster.USSR.equals(who) 
				&& Influence.EUROPE.equals(marker.getRegion()) 
				&& marker.hasControl())
			return false;
		else
			return true;
	}

	@Override
	protected boolean isUnderlined() {
		return true;
	}
}
