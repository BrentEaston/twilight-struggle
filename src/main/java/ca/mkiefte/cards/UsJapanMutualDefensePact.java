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
public final class UsJapanMutualDefensePact extends CardEvent {
	public static final String ID = "usjapanmutualdefensepact;";
	public static final String DESCRIPTION = "US/Japan Mutual Defense Pact*";

	public UsJapanMutualDefensePact() {
		this(ID, null);
	}

	public UsJapanMutualDefensePact(final String type, final GamePiece inner) {
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
	protected Command myPlayEvent(final String who) {
		Command comm = super.myPlayEvent(who);
		comm = comm.append(Influence.getInfluenceMarker(Influence.JAPAN, TSPlayerRoster.US).takeControl());
		final Chatter chatter = GameModule.getGameModule().getChatter();
		final Command chat = new Chatter.DisplayText(chatter, "USSR may not longer attempt Coups or Realignments in Japan.");
		chat.execute();
		comm = comm.append(chat);
		return comm;
	}

	public boolean canCoupOrRealign(final Influence marker, final String who) {
		return !isEventInEffect() || !Influence.JAPAN.equals(marker.getLocation()) || !TSPlayerRoster.USSR.equals(who);
	}

	@Override
	protected boolean isUnderlined() {
		return true;
	}
}
