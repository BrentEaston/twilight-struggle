package ca.mkiefte.cards;

import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.command.NullCommand;
import VASSAL.counters.GamePiece;
import VASSAL.tools.SequenceEncoder;
import ca.mkiefte.Influence;
import ca.mkiefte.Influence.Delegate;
import ca.mkiefte.TSPlayerRoster;

public abstract class ConductOperations extends ChangeInfluence {
	
	protected boolean playOps;

	public ConductOperations(String type, GamePiece inner) {
		super(type, inner);
	}

	@Override
	protected Command autoPlace() {
		return null;
	}

	@Override
	protected String getMessage() {
		return null;
	}

	@Override
	protected Delegate getDelegate() {
		return new Influence.ConductOperationsDelegate(this);
	}
	
	@Override
	public Command updateState() {
		Command comm;
		if (!playOps) {
			final String side = getCardSide();
			final String who = TSPlayerRoster.BOTH.equals(side) ? getOwner() : side;
			comm = playCardDialog(who, null, nInfluence(), false, asIcon());
			final ChangeTracker tracker = new ChangeTracker(this);
			playOps = true;
			if (comm == null)
				comm = tracker.getChangeCommand();
			else
				comm = comm.append(tracker.getChangeCommand());
		} else 
			comm = new NullCommand();
		return comm.append(super.updateState());
	}

	@Override
	public String myGetState() {
		final SequenceEncoder encoder = new SequenceEncoder(super.myGetState(), '$');
		encoder.append(playOps);
		return encoder.getValue();
	}

	@Override
	public void mySetState(String newState) {
		final SequenceEncoder.Decoder decoder = new SequenceEncoder.Decoder(newState, '$');
		super.mySetState(decoder.nextToken());
		playOps = decoder.nextBoolean(false);
	}

	@Override
	protected void myClearState() {
		super.myClearState();
		playOps = false;
	}

	@Override
	protected boolean passesFilter(Influence inf) {
		return true;
	}
}
