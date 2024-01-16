package ca.mkiefte.cards;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.TSTurnTracker;
import ca.mkiefte.Utilities;

public final class Defectors extends CardEvent {
	public static final String ID = "defectors;";
	public static final String DESCRIPTION = "Defectors";
	
	public Defectors() {
		this(ID, null);
	}

	public Defectors(final String type, final GamePiece inner) {
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
		if (!super.isEventPlayable(who))
			return false;
		
		if (TSTurnTracker.getCurrentRound() == 0) {
			for (String propertyName : new String[] {AMERICAN_CARD_PLAYED, SOVIET_CARD_PLAYED}) {
				if (Integer.toString(getCardNumber()).equals(Utilities.getGlobalProperty(propertyName).getPropertyValue()))
					return false;
			}
			return true;
		} else
			return false;
	}

	@Override
	public Command playOps(final String who) {
		Command comm = super.playOps(who);
		if (TSTurnTracker.getCurrentRound() != 0 && TSPlayerRoster.USSR.equals(who))
			comm = comm.append(Utilities.adjustVps(+1));
		return comm;
	}
}
