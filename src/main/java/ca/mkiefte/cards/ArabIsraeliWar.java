package ca.mkiefte.cards;

import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;
import VASSAL.counters.GamePiece;

public final class ArabIsraeliWar extends WarCard {
	private static final int VPS = 2;
	private static final int MILITARY_OPS = 2;
	public final static String ID = "arabisraeliwar;";
	public final static String DESCRIPTION = "Arab-Israeli War";
		
	public ArabIsraeliWar() {
		this(ID, null);
	}

	public ArabIsraeliWar(final String type, final GamePiece inner) {
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
			return "Arab Coalition wins Arab-Israeli War.";
		else
			return "Israel wins Arab-Israeli War.";
	}

	@Override
	protected Influence getTarget() {
		return Influence.getInfluenceMarker(Influence.ISRAEL, TSPlayerRoster.US);
	}

	@Override
	protected int getModifier(final StringBuilder builder) {
		int modifier = super.getModifier(builder);
		if (getTarget().hasControl()) {
			builder.append(" US controls Isreal.");
			--modifier;
		}
		return modifier;
	}

	@Override
	public boolean isEventPlayable(final String who) {
		return super.isEventPlayable(who) && !CardEvent.getCard(CampDavidAccords.class).isEventInEffect();
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
	protected boolean isSuccessful(final int result) {
		return result >= 4;
	}

	@Override
	protected String getInvader() {
		return "A Pan-Arab Coalition";
	}
}