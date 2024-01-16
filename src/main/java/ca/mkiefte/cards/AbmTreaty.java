package ca.mkiefte.cards;
import static ca.mkiefte.Utilities.adjustDefcon;
import ca.mkiefte.Influence;
import ca.mkiefte.Influence.Delegate;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;


/**
 * ABM Treaty.
 * Improves DEFCON one level.
 * Owner may then play a generic 4 Ops card.
 * @author Michael Kiefte
 */
public final class AbmTreaty extends ConductOperations {
	private static final int INFLUENCE = 4;
	public static final String ID = "abmtreaty;";
	public static final String DESCRIPTION = "ABM Treaty";

	public AbmTreaty() {
		this(ID, null);
	}

	public AbmTreaty(final String type, final GamePiece inner) {
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
	public Command myPlayEvent(final String who) {
		Command comm = super.myPlayEvent(who);
		comm = comm.append(adjustDefcon(+1));
		return comm;
	}

	@Override
	protected int nInfluence() {
		return INFLUENCE;
	}

	@Override
	protected Delegate getDelegate() {
		return new Influence.ConductOperationsDelegate(this) {
			@Override
			public boolean isOptional() {
				return true;
			}
			
		};

	}
}
