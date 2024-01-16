package ca.mkiefte;

import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.cards.CardEvent;
import ca.mkiefte.cards.TheChinaCard;

public final class China extends Influence {
	public static final String ID = "china;";
	public static final String DESCRIPTION = "China";

	public China() {
		this(ID, null);
	}

	public China(String type, GamePiece inner) {
		super(type, inner);
	}

	@Override
	protected String getID() {
		return ID;
	}

	@Override
	public String getDescription() {
		return "China";
	}

	@Override
	public Influence getOpponentInfluenceMarker() {
		return this;
	}

	@Override
	public boolean hasControl() {
		return false;
	}
	
	public static boolean isChineseCivilWarInEffect() {
		final Influence marker = getInfluenceMarker(Influence.CHINA, TSPlayerRoster.USSR);
		return marker.getInfluence() < marker.getStability();
	}

	@Override
	public int getStability() {
		return 3;
	}

	@Override
	public String getRegion() {
		return Influence.ASIA;
	}

	@Override
	public boolean isBattleground() {
		return false;
	}

	@Override
	public boolean isAdjacentToOpponentSuperpower() {
		return false;
	}

	@Override
	public String myGetType() {
		return getID();
	}

	@Override
	public void mySetType(String type) {
	}

	@Override
	public Command setInfluence(int inf) {
		if (inf > getStability())
			inf = getStability();
		Command comm = super.setInfluence(inf);
		if (comm == null || comm.isNull())
			return comm;
		if (getInfluence() == getStability())
			comm = comm.append(CardEvent.getCard(TheChinaCard.class).sendCardToHand(TSPlayerRoster.USSR));
		return comm;
	}

	@Override
	public Object getProperty(Object key) {
		if (CONTROL.equals(key)) {
			if (getInfluence() == 0)
				return NO_PRESENCE_LEVEL;
			else
				return getInfluence() >= getStability() ? CONTROL_LEVEL : PRESENCE_LEVEL;
		} else
			return super.getProperty(key);
	}

	@Override
	public Object getLocalizedProperty(Object key) {
		if (CONTROL.equals(key)) {
			if (getInfluence() == 0)
				return NO_PRESENCE_LEVEL;
			else
				return getInfluence() >= getStability() ? CONTROL_LEVEL : PRESENCE_LEVEL;
		} else
			return super.getLocalizedProperty(key);
	}

	@Override
	protected boolean canIncreaseInfluence() {
		return super.canIncreaseInfluence() && getInfluence() < getStability();
	}

	@Override
	protected boolean canDecreaseInfluence() {
		return false;
	}

	@Override
	public boolean canRealign() {
		return false;
	}

	@Override
	public boolean canCoup() {
		return false;
	}
}
