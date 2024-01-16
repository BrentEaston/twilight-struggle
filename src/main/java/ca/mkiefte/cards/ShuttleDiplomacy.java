package ca.mkiefte.cards;

import java.awt.Point;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.Map;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;

public final class ShuttleDiplomacy extends CardEvent {
	private static final Point PT = new Point(400, 400);
	public static final String ID = "shuttlediplomacy;";
	public static final String DESCRIPTION = "Shuttle Diplomacy";

	public ShuttleDiplomacy() {
		this(ID, null);
	}

	public ShuttleDiplomacy(final String type, final GamePiece inner) {
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
		final Chatter chatter = GameModule.getGameModule().getChatter();
		Command chat = new Chatter.DisplayText(chatter, "Next scoring: -1 Battleground country from USSR in Middle East or Asia.");
		chat = chat.append(new Chatter.DisplayText(chatter, "!!! Note: Region status reminders for Asia and Middle East will not reflect this. !!!"));
		chat.execute();	
		comm = comm.append(chat);
		final ChangeTracker tracker = new ChangeTracker(this);
		eventInEffect = true;
		comm = comm.append(tracker.getChangeCommand());
		return comm;
	}

	@Override
	public Command discard() {
		if (isEventInEffect()) {
			Command comm = setOwner(null);
			comm = comm.append(Map.getMapById(MAIN_MAP).placeOrMerge(getOutermost(this), PT));
			comm = comm.append(setCurrentPosition());
			return comm;
		} else
			return super.discard();
	}
}
