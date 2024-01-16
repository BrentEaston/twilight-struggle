package ca.mkiefte.cards;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class Fidel extends CardEvent {
	public static final String ID = "fidel;";
	public static final String DESCRIPTION = "Fidel*";

	public Fidel() {
		this(ID, null);
	}

	public Fidel(final String type, final GamePiece inner) {
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
	public Command myPlayEvent(String who) {
		Command comm = super.myPlayEvent(who);
		Influence inf = Influence.getInfluenceMarker(Influence.CUBA, TSPlayerRoster.US);
		comm = comm.append(inf.removeAllInfluence());
		inf = inf.getOpponentInfluenceMarker();
		comm = comm.append(inf.takeControl());
		return comm;
	}
}
