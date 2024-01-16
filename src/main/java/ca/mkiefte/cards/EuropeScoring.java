package ca.mkiefte.cards;

import static ca.mkiefte.Utilities.adjustVps;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.Utilities;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;

public final class EuropeScoring extends ScoringCard {
	public static final String ID = "europescoring;";
	public static final String DESCRIPTION = "Europe Scoring";
	
	public static final int PRESENCE_VPS = 3;
	public static final int DOMINATION_VPS = 7;
	
	public EuropeScoring() {
		this(ID, null);
	}

	public EuropeScoring(final String type, final GamePiece inner) {
		super(type, inner);
	}

	@Override
	public Command calculateAndReportVps() {
		updateRegion(false);
		for (String who : new String[] {TSPlayerRoster.USSR, TSPlayerRoster.US})
			if (hasControl(who, true)) {
				final Chatter chatter = GameModule.getGameModule().getChatter();
				final StringBuilder builder = new StringBuilder(who).append(" has Control of Europe.");
				final Command chat = new Chatter.DisplayText(chatter, builder.toString());
				chat.execute();
				// auto victory
				return chat.append(adjustVps(TSPlayerRoster.USSR.equals(who) ? Utilities.SOVIET_AUTOMATIC_VICTORY : Utilities.AMERICAN_AUTOMATIC_VICTORY));
			}
		return super.calculateAndReportVps();
	}

	@Override
	protected String getIdName() {
		return ID;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getRegion() {
		return Influence.EUROPE;
	}

	@Override
	protected int getVpsForPresence() {
		return PRESENCE_VPS;
	}

	@Override
	protected int getVpsForDomination() {
		return DOMINATION_VPS;
	}

	@Override
	protected int getVpsForControl() {
		return Integer.MAX_VALUE;
	}
}
