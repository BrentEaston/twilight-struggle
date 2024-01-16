package ca.mkiefte.cards;

import ca.mkiefte.China;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;
import VASSAL.counters.GamePiece;

public final class KoreanWar extends WarCard {
	private static final int VPS = 2;
	private static final int MILITARY_OPS = 2;
	public final static String ID = "koreanwar;";
	public final static String DESCRIPTION = "Korean War*";
		
	public KoreanWar() {
		this(ID, null);
	}

	public KoreanWar(final String type, final GamePiece inner) {
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
	protected String getResultString(final boolean invaderWins) {
		if (invaderWins)
			return "North wins Korean War.";
		else
			return "South wins Korean War.";
	}
	
	@Override
	public Influence getTarget() {
		return Influence.getInfluenceMarker(Influence.S_KOREA, TSPlayerRoster.US);
	}

	@Override
	protected int getModifier(final StringBuilder adjacent) {
		int modifier = super.getModifier(adjacent);
		if (China.isChineseCivilWarInEffect()) {
			adjacent.append(" Chinese Civil War in effect.");
			--modifier;
		}
		return modifier;
	}

	@Override
	protected int getMilitaryOps() {
		return MILITARY_OPS;
	}

	@Override
	protected int getVps(final String who) {
		return -VPS;
	}

	@Override
	protected boolean isSuccessful(int result) {
		return result >= 4;
	}

	@Override
	protected String getInvader() {
		return Influence.N_KOREA;
	}
}