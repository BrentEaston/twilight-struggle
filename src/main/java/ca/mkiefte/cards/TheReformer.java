package ca.mkiefte.cards;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.Influence;
import ca.mkiefte.Influence.Delegate;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.Utilities;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class TheReformer extends ChangeInfluence {
	public static final String ID = "thereformer;";
	public static final String DESCRIPTION = "The Reformer*";

	public TheReformer() {
		this(ID, null);
	}

	public TheReformer(final String type, final GamePiece inner) {
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
	protected int nInfluence() {
		final int vps = Integer.valueOf(Utilities.getGlobalProperty(Utilities.VPS).getPropertyValue());
		return vps < 0 ? 6 : 4;
	}

	@Override
	protected Command autoPlace() {
		return null;
	}

	@Override
	protected String getMessage() {
		final StringBuilder builder = new StringBuilder("Add ").append(nInfluence()).append(" in Europe; no more than 2 per country.");
		return builder.toString();
	}

	@Override
	protected Delegate getDelegate() {
		return new Delegate(this) {
			@Override
			public boolean canIncreaseInfluence(final Influence marker) {
				return super.canIncreaseInfluence(marker)
						&& passesFilter(marker)
						&& marker.getInfluence() < marker.getStartingInfluence() + 2;
			}

			@Override
			public boolean isOptional() {
				final int vps = Integer.valueOf(Utilities.getGlobalProperty(Utilities.VPS).getPropertyValue());
				final int ops = Integer.valueOf(prop.getPropertyValue());
				return (vps < 0 && ops <= 2);
			}		
		};
	}

	@Override
	protected boolean passesFilter(final Influence marker) {
		return TSPlayerRoster.USSR.equals(marker.getSide()) && Influence.EUROPE.equals(marker.getRegion());
	}

	@Override
	public Command myPlayEvent(final String who) {
		Command comm = super.myPlayEvent(who);
		final Chatter chatter = GameModule.getGameModule().getChatter();
		Command chat = new Chatter.DisplayText(chatter, "USSR may no longer conduct coups in Europe.");
		chat = chat.append(new Chatter.DisplayText(chatter, "Glasnost effect is improved."));
		chat.execute();
		comm = comm.append(chat);
		return comm;
	}

	public boolean canCoup(final Influence marker, final String who) {
		return !isEventInEffect() || !TSPlayerRoster.USSR.equals(who) || !Influence.EUROPE.equals(marker.getRegion());
	}
}
