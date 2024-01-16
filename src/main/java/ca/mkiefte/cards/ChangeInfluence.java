package ca.mkiefte.cards;

import VASSAL.build.module.properties.MutableProperty;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.Influence;
import ca.mkiefte.Utilities;

public abstract class ChangeInfluence extends CardEvent {
	protected boolean auto;

	public ChangeInfluence(String type, GamePiece inner) {
		super(type, inner);
	}

	@Override
	protected boolean eventFinishedOnExit() {
		return false;
	}

	protected abstract int nInfluence();
	
	protected abstract Command autoPlace();
	
	protected abstract String getMessage();
	
	protected abstract Influence.Delegate getDelegate();
	
	protected abstract boolean passesFilter(Influence marker);

	@Override
	public Command myPlayEvent(String who) {
		Command comm = super.myPlayEvent(who);
		comm = comm.append(setup());
		return comm;
	}

	protected Command setup() {
		auto = false;
		final MutableProperty prop = Utilities.getGlobalProperty(Influence.OPS_REMAINING);
		Command comm = autoPlace();
		if (comm != null && !comm.isNull()) {
			comm = comm.append(prop.setPropertyValue(""));
			comm = comm.append(setFinished(true));
			auto = true;
		} else if (nInfluence() > 0) {
			comm = prop.setPropertyValue(Integer.toString(nInfluence()));
			comm = comm.append(Utilities.getGlobalProperty(Influence.WHO_PLAYED_OPS).setPropertyValue(activeSide()));
		}
		return comm;
	}

	@Override
	public Command updateState() {
		Influence.getInfluenceDialog(getDescription(), getMessage(), getDelegate());
		return super.updateState();
	}

	@Override
	public boolean canUndoEvent() {
		return super.canUndoEvent() && (auto || !isOpponentsCard() || nInfluence() == 0);
	}

	@Override
	public Command undoPlayEvent() {
		Command comm;
		if (!auto)
			comm = Influence.revertToStartingInfluence().append(super.undoPlayEvent());
		else
			comm = super.undoPlayEvent();
		return comm;
	}
}
