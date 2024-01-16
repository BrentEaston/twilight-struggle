package ca.mkiefte.cards;

import java.util.Set;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceFilter;
import ca.mkiefte.Influence;
import ca.mkiefte.Utilities;

public abstract class DoActionOnLocation extends CardEvent {
	private Set<GamePiece> locations;
	private boolean actionPerformed;
	
	public DoActionOnLocation(String type, GamePiece inner) {
		super(type, inner);
	}
	
	abstract protected PieceFilter getLocationFilter();
	
	abstract protected Command doActionOnLocation(Influence inf);
	
	abstract protected String getNoMatchingDescription();
	
	abstract protected String getMessage();
	
	final Set<GamePiece> getLocations() {
		if (locations == null)
			locations = Utilities.findAllPiecesMatching(getLocationFilter());
		return locations;
	}

	@Override
	public Command myPlayEvent(final String who) {
		Command comm = super.myPlayEvent(who);
		final Set<GamePiece> l = getLocations();
		final Chatter chatter = GameModule.getGameModule().getChatter();
		final Command chat;
		if (l.size() == 0) {
			chat = new Chatter.DisplayText(chatter, getNoMatchingDescription());
			chat.execute();
			comm = comm.append(chat);
			actionPerformed = true;
		} else if (l.size() == 1) {
			comm = comm.append(doActionOnLocation(Influence.getInfluenceMarker(l.iterator().next())));
			actionPerformed = true;
		}
		return comm;
	}

	@Override
	protected void myClearState() {
		super.myClearState();
		locations = null;
	}

	@Override
	protected boolean eventFinishedOnExit() {
		return actionPerformed;
	}

	@Override
	public Command updateState() {
		final Set<GamePiece> options = getLocations();
		final GamePiece gp = Utilities.selectGamePiece(options, getName(), getMessage(), asIcon(), true, false);
		Command comm = doActionOnLocation(Influence.getInfluenceMarker(gp));
		comm = comm.append(setFinished(true));
		return comm;
	}

	@Override
	public boolean canUndoEvent() {
		return super.canUndoEvent() && (actionPerformed || !isOpponentsCard());
	}	
}
