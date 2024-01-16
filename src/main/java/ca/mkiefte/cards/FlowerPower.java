package ca.mkiefte.cards;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class FlowerPower extends CardEvent {
	public static final String ID = "flowerpower;";
	public static final String DESCRIPTION = "Flower Power*";

	public FlowerPower() {
		this(ID, null);
	}

	public FlowerPower(final String type, final GamePiece inner) {
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
		final Chatter chatter = GameModule.getGameModule().getChatter();
		final Command chat = new Chatter.DisplayText(chatter, "USSR gains 2 VP for every war card played by the US.");
		chat.execute();
		comm = comm.append(chat);
		return comm;
	}
}
