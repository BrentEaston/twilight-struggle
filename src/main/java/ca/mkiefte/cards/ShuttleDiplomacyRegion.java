package ca.mkiefte.cards;

import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.command.NullCommand;
import VASSAL.counters.GamePiece;

public abstract class ShuttleDiplomacyRegion extends ScoringCard {
	
	public ShuttleDiplomacyRegion(final String type, final GamePiece inner) {
		super(type, inner);
	}

	protected final boolean shuttleDiplomacyInEffect() {
		return CardEvent.getCard(ShuttleDiplomacy.class).isEventInEffect();
	}

	@Override
	protected int getNBattlegrounds(final String who, final boolean scoring) {
		int n = super.getNBattlegrounds(who, scoring);
		if (scoring && TSPlayerRoster.USSR.equals(who) && shuttleDiplomacyInEffect() && sovietBattlegrounds > 0)
			return n - 1;
		else
			return n;
	}

	@Override
	public Command calculateAndReportVps() {
		Command comm = new NullCommand();
		if (shuttleDiplomacyInEffect()) { 
			final Chatter chatter = GameModule.getGameModule().getChatter();
			comm = new Chatter.DisplayText(chatter, "Shuttle Diplomacy: -1 Soviet Battleground.");
			comm.execute();
		}
		return comm.append(super.calculateAndReportVps());
	}

	@Override
	final public Command myPlayEvent(final String who) {
		Command comm = super.myPlayEvent(who);
		if (shuttleDiplomacyInEffect()) {
			final ShuttleDiplomacy shuttle = CardEvent.getCard(ShuttleDiplomacy.class);
			final ChangeTracker tracker = new ChangeTracker(this);
			shuttle.eventInEffect = false;
			comm = comm.append(tracker.getChangeCommand());
			comm = comm.append(shuttle.discard());
		}
		return comm;
	}

	@Override
	protected int getNCountries(String who, boolean scoring) {
		int n = super.getNCountries(who, scoring);
		if (scoring 
				&& TSPlayerRoster.USSR.equals(who) 
				&& shuttleDiplomacyInEffect() 
				&& sovietBattlegrounds > 0)
			return n - 1;
		else
			return n;
	}

	@Override
	protected int getNAdjacent(String who, boolean scoring) {
		int n = super.getNAdjacent(who, scoring);
		if (scoring
				&& TSPlayerRoster.USSR.equals(who)
				&& shuttleDiplomacyInEffect()
				&& Influence.getInfluenceMarker(Influence.JAPAN, TSPlayerRoster.USSR).hasControl())
			return n - 1;
		else
			return n;
	}
}
