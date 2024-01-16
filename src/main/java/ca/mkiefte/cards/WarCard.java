package ca.mkiefte.cards;

import static ca.mkiefte.Utilities.adjustMilitaryOps;
import static ca.mkiefte.Utilities.adjustVps;

import java.util.HashSet;
import java.util.Set;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.TSTurnTracker;
import ca.mkiefte.Utilities;

public abstract class WarCard extends CardEvent {

	public WarCard(final String type, final GamePiece inner) {
		super(type, inner);
	}

	protected abstract Influence getTarget();
	protected abstract String getInvader();
	protected abstract String getResultString(final boolean invaderWins);
	protected abstract int getMilitaryOps();
	protected abstract int getVps(final String who);
	protected abstract boolean isSuccessful(final int result);

	protected String getInstigator() {
		final String cardSide = getCardSide();
		if (TSPlayerRoster.BOTH.equals(cardSide))
			return getOwner();
		else
			return cardSide;
	}

	protected int getModifier(final StringBuilder adjacent) {
		final Influence target = getTarget();
		int modifier = 0;
		final Set<String> controlledNeighbours = new HashSet<String>();
		final Set<Influence> neighbours = target.getNeighbours();
		for (final Influence marker : neighbours) {
			if (marker.hasControl()) {
				--modifier;
				controlledNeighbours.add(marker.getLocation());
			}
		}
		if (modifier == 0) {
			adjacent.append(target.getSide()).append(" does not control any of");
			Utilities.listAsString(adjacent, Influence.getNeighbours(target.getLocation()), "or");
		} else {
			adjacent.append(target.getSide()).append(" controls");
			Utilities.listAsString(adjacent, controlledNeighbours, "and");
		}
		return modifier;
	}

	protected Command doWar() {
		final Chatter chatter = GameModule.getGameModule().getChatter();
		StringBuilder builder = new StringBuilder(getInvader()).append(" invades ").append(getTarget().getLocation()).append('.');
		Command chat = new Chatter.DisplayText(chatter, builder.toString());
		chat.execute();
		Command comm = chat;
		final Influence target = getTarget();
		builder = new StringBuilder("");
		final int modifier = getModifier(builder);
		builder.append(" ").append(modifier).append(" die-roll modifier.");
		final String instigator = getInstigator();
		comm = comm.append(adjustMilitaryOps(getMilitaryOps(), instigator));
		chat = new Chatter.DisplayText(chatter, builder.toString());
		chat.execute();
		comm = comm.append(chat);
		final String dieName;
		if (TSPlayerRoster.USSR.equals(instigator)) {
			dieName = TSPlayerRoster.USSR;
		}
		else {
			dieName = TSPlayerRoster.US;
		}
		final int[] result = {0};
		comm = comm.append(Utilities.rollDie(dieName, result));
		final boolean success = isSuccessful(result[0]+modifier);
		chat = new Chatter.DisplayText(chatter, getResultString(success));
		chat.execute();
		comm = comm.append(chat);
		if (success) {
			int vps = getVps(instigator);
			comm = comm.append(adjustVps(vps));
			if (TSTurnTracker.isGameOver())
				return comm;
			final int inf = target.getInfluence();
			comm = comm.append(target.removeAllInfluence());
			final Influence influence = target.getOpponentInfluenceMarker();
			comm = comm.append(influence.adjustInfluence(+inf));
		}
		return comm;
	}

	@Override
	protected Command myPlayEvent(final String who) {
		Command comm = super.myPlayEvent(who);
		comm = comm.append(flowerPowerVp(who));
		if (TSTurnTracker.isGameOver())
			return comm;
		return comm.append(doWar());
	}

	@Override
	public Command playOps(final String who) {
		Command comm = super.playOps(who);
		comm = comm.append(flowerPowerVp(who));
		return comm;
	}

	protected Command flowerPowerVp(final String who) {
		if (TSPlayerRoster.US.equals(who)
				&& getCard(FlowerPower.class).isEventInEffect()) {
			Command chat = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), "USSR receives +2 VPs (Flower Power).");
			chat.execute();
			return chat.append(Utilities.adjustVps(-2));
		} else
			return null;
	}
}