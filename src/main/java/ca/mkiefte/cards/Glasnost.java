package ca.mkiefte.cards;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.Influence;
import ca.mkiefte.TSTurnTracker;
import ca.mkiefte.Influence.Delegate;
import ca.mkiefte.Utilities;

public final class Glasnost extends ConductOperations {
	private static final int OPS = 4;
	public static final String ID = "glasnost;";
	public static final String DESCRIPTION = "Glasnost*";

	public Glasnost() {
		this(ID, null);
	}

	public Glasnost(final String type, final GamePiece inner) {
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
		return OPS;
	}

	@Override
	protected Command autoPlace() {
		if (!getCard(TheReformer.class).isEventInEffect()) {
			Command chat = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), 
					"The Reformer is not in effect.");
			chat.execute();
			return chat;
		} else
			return null;
	}

	@Override
	protected Command setup() {
		Command comm = Utilities.adjustVps(-2);
		if (TSTurnTracker.isGameOver()) {
			comm = comm.append(setFinished(true));
			return comm;
		}
		comm = comm.append(Utilities.adjustDefcon(+1));
		comm = comm.append(super.setup());
		return comm;
	}

	@Override
	protected Delegate getDelegate() {
		return new Influence.ConductOperationsDelegate(this) {
			@Override
			public boolean canCoup() {
				return false;
			}

			@Override
			public boolean isOptional() {
				return true;
			}			
		};
	}
}
