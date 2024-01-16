package ca.mkiefte.cards;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.Utilities;

public final class Opec extends CardEvent {
	public static final String ID = "opec;";
	public static final String DESCRIPTION = "OPEC";
	private String[] COUNTRIES = new String[] {
			Influence.EGYPT, 
			Influence.IRAN, 
			Influence.LIBYA, 
			Influence.SAUDI_ARABIA, 
			Influence.IRAQ, 
			Influence.GULF_STATES, 
			Influence.VENEZUELA};

	public Opec() {
		this(ID, null);
	}

	public Opec(final String type, final GamePiece inner) {
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
		return super.isEventPlayable(who) && !getCard(NorthSeaOil.class).isEventInEffect();
	}

	@Override
	public Command myPlayEvent(final String who) {
		Command comm = super.myPlayEvent(who);
		final Chatter chatter = GameModule.getGameModule().getChatter();
		final Set<String> countries = new HashSet<String>(Arrays.asList(COUNTRIES));
		Iterator<String> iter = countries.iterator();
		while (iter.hasNext())
			if (!Influence.getInfluenceMarker(iter.next(), TSPlayerRoster.USSR).hasControl())
				iter.remove();
		StringBuilder builder = new StringBuilder("");
		if (countries.size() == 0)
			Utilities.listAsString(builder.append("USSR does not control any of"), 
					new HashSet<String>(Arrays.asList(COUNTRIES)), "or");
		else
			Utilities.listAsString(builder.append("USSR controls"), countries, "and");
		final Command chat = new Chatter.DisplayText(chatter, builder.toString());
		chat.execute();
		comm = comm.append(chat);
		comm = comm.append(Utilities.adjustVps(-countries.size()));
		return comm;
	}
}
