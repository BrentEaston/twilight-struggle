package ca.mkiefte.cards;


import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.properties.MutableProperty;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import VASSAL.tools.SequenceEncoder;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.Utilities;

public final class VietnamRevolts extends CardEvent {
	public static final String ID = "vietnamrevolts;";
	public static final String DESCRIPTION = "Vietnam Revolts*";
	private boolean all;
	private int cardOps; 

	public boolean allOpsInSoutheastAsia() {
		return all;
	}
	
	public VietnamRevolts() {
		this(ID, null);
	}

	public VietnamRevolts(final String type, final GamePiece inner) {
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
		final Influence inf = Influence.getInfluenceMarker(Influence.VIETNAM, TSPlayerRoster.USSR);
		comm = comm.append(inf.adjustInfluence(+2));
		final Chatter chatter = GameModule.getGameModule().getChatter();
		final Command chat = new Chatter.DisplayText(chatter, "USSR adds +1 Op when all points used in Southeast Asia this Turn.");
		chat.execute();
		comm = comm.append(chat);
		return comm;
	}

	public Command setup(final int cardOps) {
		final ChangeTracker tracker = new ChangeTracker(this);
		this.cardOps = cardOps;
		Command comm = tracker.getChangeCommand();
		comm = comm.append(setAllInSoutheastAsia(true));
		return comm;
	}
	
	public Command setAllInSoutheastAsia(final boolean val) {
		if (val == all)
			return null;
		final ChangeTracker tracker = new ChangeTracker(this);
		all = val;
		Command comm = tracker.getChangeCommand();
		if (cardOps > 0) {
			int ops = addBonusOps(cardOps, Utilities.getGlobalProperty(Influence.WHO_PLAYED_OPS).getPropertyValue());
			if (ops >= 1) {
				final MutableProperty prop = Utilities.getGlobalProperty(Influence.CONDITIONAL_INFLUENCE);
				int inf = Integer.valueOf(prop.getPropertyValue());
				inf += val ? +1 : -1;
				comm = comm.append(prop.setPropertyValue(Integer.toString(inf)));
			}
		}
		return comm;
	}

	@Override
	public String myGetState() {
		final SequenceEncoder se = new SequenceEncoder(super.myGetState(), '+');
		se.append(all);
		se.append(cardOps);
		return se.getValue();
	}

	@Override
	public void mySetState(final String newState) {
		final SequenceEncoder.Decoder se = new SequenceEncoder.Decoder(newState, '+');
		super.mySetState(se.nextToken());
		all = se.nextBoolean(false);
		cardOps = se.nextInt(0);
	}

	public int getBonus(final String who, final Influence marker) {
		if (TSPlayerRoster.US.equals(who) 
				|| !isEventInEffect() 
				|| !marker.isInSouthEastAsia() 
				|| !all
				|| addBonusOps(cardOps, Utilities.getGlobalProperty(Influence.WHO_PLAYED_OPS).getPropertyValue()) == 0)
			return 0;
		else
			return 1;
	}
	
	@Override
	public boolean isEventInEffect() {
		return isEventPlayedThisTurn();
	}
}
