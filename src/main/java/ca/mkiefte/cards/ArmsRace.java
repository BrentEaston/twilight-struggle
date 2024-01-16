package ca.mkiefte.cards;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.TSTurnTracker;
import ca.mkiefte.Utilities;

public final class ArmsRace extends CardEvent {
	public final static String ID = "armsrace;";
	public final static String DESCRIPTION = "Arms Race";

	public ArmsRace() {
		this(ID, null);
	}

	public ArmsRace(final String type, final GamePiece inner) {
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
		final int militaryOps;
		String phasing = Utilities.getActivePlayer();
		final boolean soviet = phasing.equals(TSPlayerRoster.USSR);
		if (soviet)
			militaryOps = Integer.valueOf(Utilities.getGlobalProperty(Utilities.SOVIET_MILITARY_OPS_PROP_NAME).getPropertyValue());
		else
			militaryOps = Integer.valueOf(Utilities.getGlobalProperty(Utilities.AMERICAN_MILITARY_OPS_PROP_NAME).getPropertyValue());
		int bonus = 1;
		final int defcon = Integer.valueOf(Utilities.getGlobalProperty(Utilities.DEFCON).getPropertyValue());
		final Chatter chatter = GameModule.getGameModule().getChatter();
		final Command chat;
		final StringBuilder builder;
		if (militaryOps >= defcon) {
			bonus = 3;
			builder = new StringBuilder(phasing).append(" has more Military Ops AND has met the Required Military Ops for ").append(bonus).append(" VPs.");
			chat = new Chatter.DisplayText(chatter, builder.toString());
		} else {
			builder = new StringBuilder(phasing).append(" has more Military Ops for ").append(bonus).append(" VP.");
			chat = new Chatter.DisplayText(chatter, builder.toString());
		}
		chat.execute();
		comm = comm.append(chat);
		bonus *= soviet ? -1 : 1;
		comm = comm.append(Utilities.adjustVps(bonus));
		return comm;
	}

	@Override
	public boolean isEventPlayable(final String who) {
		String phasing = TSTurnTracker.getPhasingPlayer();
		if (phasing == null) {
			phasing = who;
			int parent = parentCardNumber;
			while (parent > 0) {
				parent = getCard(parent).parentCardNumber;
				phasing = getCard(parent).getOwner();
			}
		}
		final String myPropName;
		final String opPropName;
		if (TSPlayerRoster.US.equals(phasing)) {
			myPropName = Utilities.AMERICAN_MILITARY_OPS_PROP_NAME;
			opPropName = Utilities.SOVIET_MILITARY_OPS_PROP_NAME;
		}
		else {
			myPropName = Utilities.SOVIET_MILITARY_OPS_PROP_NAME;
			opPropName = Utilities.AMERICAN_MILITARY_OPS_PROP_NAME;
		}
		final int myMilOps = Integer.valueOf(Utilities.getGlobalProperty(myPropName).getPropertyValue());
		final int opMilOps = Integer.valueOf(Utilities.getGlobalProperty(opPropName).getPropertyValue());		
		return super.isEventPlayable(who) && myMilOps > opMilOps;
	}
}
