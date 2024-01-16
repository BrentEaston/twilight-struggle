package ca.mkiefte.cards;
import java.awt.Point;
import java.awt.Rectangle;

import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.Influence;
import ca.mkiefte.Influence.Delegate;
import ca.mkiefte.TSPlayerRoster;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class SouthAfricanUnrest extends ChangeInfluenceOptions {
	private static final int INFLUENCE = 2;
	public static final String ID = "southafricanunrest;";
	public static final String DESCRIPTION = "South African Unrest";
	private static final String IN = "+2 in S. Africa";
	private static final String ADJACENT = "+1 in S.A.; +2 adjacent";
	private static final String[] OPTIONS = {IN, ADJACENT};


	public SouthAfricanUnrest() {
		this(ID, null);
	}

	public SouthAfricanUnrest(final String type, final GamePiece inner) {
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
	protected int nInfluence() {
		return INFLUENCE;
	}

	@Override
	protected Command autoPlace() {
		if (IN.equals(option))
			return Influence.getInfluenceMarker(Influence.SOUTH_AFRICA, TSPlayerRoster.USSR).adjustInfluence(+2);
		else
			return null;
	}

	@Override
	protected String getMessage() {
		return "Add 2 Influence in any countries adjacent to South Africa.";
	}

	@Override
	protected Delegate getDelegate() {
		return new Influence.Delegate(this) {
			@Override
			public boolean canIncreaseInfluence(Influence marker) {
				return super.canIncreaseInfluence(marker)
						&& passesFilter(marker);
			}

			@Override
			public boolean canDecreaseInfluence(Influence marker) {
				return super.canDecreaseInfluence(marker) 
						&& !Influence.SOUTH_AFRICA.equals(marker.getLocation());
			}
		};
	}

	@Override
	protected boolean passesFilter(final Influence marker) {
		return Influence.getInfluenceMarker(Influence.SOUTH_AFRICA, TSPlayerRoster.USSR).getNeighbours().contains(marker);
	}

	@Override
	protected String[] getOptions() {
		return OPTIONS;
	}

	@Override
	protected String getOptionsMessage() {
		return "Add 2 Influence to South Africa or 1 Influence in South Africa\n"
				+ "and 2 Influence in any countries adjacent ot South Africa";
	}

	@Override
	protected String getStringForOption(final String option) {
		if (IN.equals(option))
			return "USSR places 2 Influence in South Africa.";
		else
			return "USSR places 1 Influence in South Africa and 2 Influence in any countries adjacent to South Africa.";
	}

	@Override
	protected Command setup() {
		final Influence marker = Influence.getInfluenceMarker(Influence.SOUTH_AFRICA, TSPlayerRoster.USSR);
		if (ADJACENT.equals(option) && marker.getInfluence() == marker.getStartingInfluence()) {
			return marker.adjustInfluence(+1).append(super.setup());
		} else
			return super.setup();
	}

	@Override
	protected Rectangle getFocusPosition() {
		final Influence sAfrica = Influence.getInfluenceMarker(Influence.SOUTH_AFRICA, TSPlayerRoster.USSR);
		final Rectangle rect = sAfrica.boundingBox();
		final Point pos = sAfrica.getPosition();
		rect.translate(pos.x, pos.y);
		return rect;
	}

}
