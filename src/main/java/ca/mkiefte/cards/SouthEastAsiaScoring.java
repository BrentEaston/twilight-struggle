package ca.mkiefte.cards;

import static ca.mkiefte.Utilities.adjustVps;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.command.NullCommand;
import VASSAL.counters.GamePiece;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;

public final class SouthEastAsiaScoring extends ScoringCard {
	public static final String[] COUNTRIES = {
		Influence.BURMA, 
		Influence.LAOS_CAMBODIA, 
		Influence.VIETNAM, 
		Influence.MALAYSIA, 
		Influence.INDONESIA, 
		Influence.PHILIPPINES, 
		Influence.THAILAND};
	public static final String ID = "southeastasiascoring;";
	private static final String DESCRIPTION = "South East Asia Scoring";
	
	public SouthEastAsiaScoring() {
		this(ID, null);
	}
	
	public SouthEastAsiaScoring(final String type, final GamePiece inner) {
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
	public Command calculateAndReportVps() {
		int vps = 0;
		Command chat = new NullCommand();
		final Chatter chatter = GameModule.getGameModule().getChatter();
		boolean control = false;
		
		for (String c : COUNTRIES) {
			for (String side : new String[] {TSPlayerRoster.USSR, TSPlayerRoster.US}) {
				final Influence marker = Influence.getInfluenceMarker(c, side);
				if (marker.hasControl()) {
					final int bonus = Influence.THAILAND.equals(c) ? 2 : 1;
					final StringBuilder builder = new StringBuilder(side).append(" controls ").append(c).append(" for ").append(bonus).append(" VPs.");
					chat = chat.append(new Chatter.DisplayText(chatter, builder.toString()));
					vps += (TSPlayerRoster.US.equals(side) ? 1 : -1) * bonus;
					control = true;
					continue;
				}
			}
		}

		if (!control)
			chat = chat.append(new Chatter.DisplayText(chatter, "No countries in Southeast Asia are controlled by either player."));
		chat.execute();		
		return chat.append(adjustVps(vps));
	}

	@Override
	public String getRegion() {
		return null;
	}

	@Override
	protected int getVpsForPresence() {
		return 0;
	}

	@Override
	protected int getVpsForDomination() {
		return 0;
	}

	@Override
	protected int getVpsForControl() {
		return 0;
	}
}
