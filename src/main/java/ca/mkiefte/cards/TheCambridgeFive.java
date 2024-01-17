package ca.mkiefte.cards;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.CountingPlayerHand;
import ca.mkiefte.Influence;
import ca.mkiefte.Influence.Delegate;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.TSTurnTracker;
import ca.mkiefte.Utilities;

public final class TheCambridgeFive extends ChangeInfluence {
	private static final int INFLUENCE = 1;
	public static final String ID = "thecambridgefive;";
	public static final String DESCRIPTION = "The Cambridge Five";
	private Set<String> regions = null;

	public TheCambridgeFive() {
		this(ID, null);
	}

	public TheCambridgeFive(final String type, final GamePiece inner) {
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
	public boolean isEventPlayable(final String who) {
		return super.isEventPlayable(who) && !TSTurnTracker.isLateWar();
	}

	@Override
	protected int nInfluence() {
		return INFLUENCE;
	}

	private Set<String> getRegions() {
		if (regions == null) {
			regions = new HashSet<String>();
			final CountingPlayerHand hand = CountingPlayerHand.getHand(TSPlayerRoster.US);
			final Set<CardEvent> cards = hand.getAllCards();
			for (final CardEvent card : cards)
				if (card.isScoringCard())
					regions.add(((ScoringCard) card).getRegion());
		}
		return regions;
	}
	
	@Override
	protected Command autoPlace() {
		if (getRegions().size() == 0) {
			final Chatter chatter = GameModule.getGameModule().getChatter();
			final Command chat = new Chatter.DisplayText(chatter, "US player has no Scoring Cards.");
			chat.execute();
			return chat;
		} else
			return null;
	}

	@Override
	protected String getMessage() {
		final StringBuilder builder = new StringBuilder("Place 1 influence anywhere in");
		Utilities.listAsString(builder, getRegions(), "or");
		return builder.toString();
	}

	@Override
	protected Delegate getDelegate() {
		return new Influence.Delegate(this) {

			@Override
			public boolean canIncreaseInfluence(final Influence marker) {
				return super.canIncreaseInfluence(marker) && passesFilter(marker)
						&& marker.getInfluence() == marker.getStartingInfluence();
			}

			@Override
			public boolean isFinished() {
				if (!super.isFinished()) {
					return JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(
							GameModule.getGameModule().getPlayerWindow(),
							"You have not placed any Influence.\n"
							+ "Are you sure you are finished?",
							getDescription(),
							JOptionPane.YES_NO_OPTION, 
							JOptionPane.WARNING_MESSAGE);
				} else
					return true;
			}
			
		};
	}

	@Override
	protected boolean passesFilter(final Influence marker) {
		return getRegions().contains(marker.getRegion());
	}

	@Override
	protected void myClearState() {
		super.myClearState();
		regions = null;
	}

	@Override
	public Command myPlayEvent(String who) {
		Command comm = super.myPlayEvent(who);
		final Set<String> regions = getRegions();
		if (regions.size() > 0) {
			final Chatter chatter = GameModule.getGameModule().getChatter();
			final StringBuilder builder = new StringBuilder("US player has");
			Utilities.listAsString(builder, regions, "and");
			Command chat = new Chatter.DisplayText(chatter, builder.toString());
			chat.execute();
			comm = comm.append(chat);
		}
		return comm;
	}

	@Override
	public boolean canUndoEvent() {
		if (!super.canUndoEvent())
			return false;
		if (TSPlayerRoster.USSR.equals(getOwner()))
			return false;
		final Set<CardEvent> cards = CountingPlayerHand.getHand(TSPlayerRoster.US).getAllCards();
		for (final CardEvent card : cards)
			if (card.isScoringCard())
				return false;
		return true;
	}
}
