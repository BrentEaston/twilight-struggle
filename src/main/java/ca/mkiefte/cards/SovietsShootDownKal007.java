package ca.mkiefte.cards;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.Influence;
import ca.mkiefte.Influence.Delegate;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.TSTurnTracker;
import ca.mkiefte.Utilities;

public final class SovietsShootDownKal007 extends ConductOperations {
	private static final int OPS = 4;
	public static final String ID = "sovietsshootdownkal007;";
	public static final String DESCRIPTION = "Soviets Shoot Down KAL 007*";

	public SovietsShootDownKal007() {
		this(ID, null);
	}

	public SovietsShootDownKal007(final String type, final GamePiece inner) {
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
		if (!Influence.getInfluenceMarker(Influence.S_KOREA, TSPlayerRoster.US).hasControl()) {
			Command chat = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), 
					"US does not control S. Korea.");
			chat.execute();
			return chat;
		} else
			return null;
	}

	@Override
	protected Command setup() {
		Command comm = Utilities.adjustDefcon(-1);
		if (TSTurnTracker.isGameOver()) {
			comm = comm.append(setFinished(true));
			return comm;
		}
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
