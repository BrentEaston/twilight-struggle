package ca.mkiefte.cards;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class JohnPaulIIElectedPope extends CardEvent {
	public static final String ID = "johnpauliielectedpope;";
	public static final String DESCRIPTION = "John Paul II Elected Pope*";

	public JohnPaulIIElectedPope() {
		this(ID, null);
	}

	public JohnPaulIIElectedPope(final String type, final GamePiece inner) {
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
	protected boolean isUnderlined() {
		return true;
	}

	@Override
	protected Command myPlayEvent(final String who) {
		Command comm = super.myPlayEvent(who);
		Influence marker = Influence.getInfluenceMarker(Influence.POLAND, TSPlayerRoster.USSR);
		comm = comm.append(marker.adjustInfluence(-2));
		marker = marker.getOpponentInfluenceMarker();
		comm = comm.append(marker.adjustInfluence(+1));
		final Chatter chatter = GameModule.getGameModule().getChatter();
		final StringBuilder builder = new StringBuilder(getCard(Solidarity.class).getDescription()).append(" event now playable.");
		final Command chat = new Chatter.DisplayText(chatter, builder.toString());
		chat.execute();
		comm = comm.append(chat);
		return comm;
	}
}
