package ca.mkiefte.cards;

import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.Utilities;

public final class OneSmallStep extends CardEvent {
	public static String ID = "onesmallstep;";
	public static String DESCRIPTION = "\"One Small Step...\"";	
	
	public OneSmallStep() {
		this(ID, null);
	}

	public OneSmallStep(final String type, final GamePiece inner) {
		super(type, inner);
	}

	@Override
	public Command myPlayEvent(final String who) {
		Command comm = super.myPlayEvent(who);
		final String owner = getOwner();
		comm = comm.append(Utilities.advanceSpaceRace(owner, false));
		comm = comm.append(Utilities.advanceSpaceRace(owner));
		return comm;
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
		final String mySpaceRaceProp;
		final String opponentSpaceRaceProp;
		if (TSPlayerRoster.USSR.equals(who)) {
			mySpaceRaceProp = Utilities.SOVIET_SPACE_RACE;
			opponentSpaceRaceProp = Utilities.AMERICAN_SPACE_RACE;
		}
		else {
			mySpaceRaceProp = Utilities.AMERICAN_SPACE_RACE;
			opponentSpaceRaceProp = Utilities.SOVIET_SPACE_RACE;
		}
		final int mySpaceRace = Integer.valueOf(Utilities.getGlobalProperty(mySpaceRaceProp).getPropertyValue());
		final int opponentSpaceRace = Integer.valueOf(Utilities.getGlobalProperty(opponentSpaceRaceProp).getPropertyValue());
		return super.isEventPlayable(who) && mySpaceRace < opponentSpaceRace;
	}
}