package ca.mkiefte.cards;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class DeGaulleLeadsFrance extends CardEvent {
	public static final String ID = "degaulleleadsfrance;";
	public static final String DESCRIPTION = "De Gaulle Leads France*";

	public DeGaulleLeadsFrance() {
		this(ID, null);
	}

	public DeGaulleLeadsFrance(final String type, final GamePiece inner) {
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
		Influence marker = Influence.getInfluenceMarker(Influence.FRANCE, TSPlayerRoster.US);
		comm = comm.append(marker.adjustInfluence(-2));
		marker = marker.getOpponentInfluenceMarker();
		comm = comm.append(marker.adjustInfluence(+1));
		final Chatter chatter = GameModule.getGameModule().getChatter();
		final Command chat = new Chatter.DisplayText(chatter, "Effect of NATO is canceled for France.");
		chat.execute();
		comm = comm.append(chat);
		return comm;
	}

	@Override
	protected boolean isUnderlined() {
		return true;
	}
}
