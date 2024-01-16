package ca.mkiefte.cards;


import javax.swing.JOptionPane;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.command.NullCommand;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceFilter;
import ca.mkiefte.CountingPlayerHand;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;

public final class Blockade extends CardEvent {	
	public final static String ID = "blockade;";
	public final static String DESCRIPTION = "Blockade Event*";
	
	public Blockade() {
		this(ID, null);
	}

	public Blockade(final String type, final GamePiece inner) {
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
	protected boolean eventFinishedOnExit() {
		return false;
	}

	@Override
	public String activeSide() {
		return TSPlayerRoster.US;
	}

	@Override
	public Command updateState() {
		Command comm = new NullCommand();
		final int nCards = CountingPlayerHand.getHand(TSPlayerRoster.US).countCards();
		boolean discarded = false;
		if (nCards > 0) {
			comm = comm.append(selectDiscardCard(TSPlayerRoster.US, getName(), 
					"Discard a 3 or more Ops card or\n'Cancel' to eliminate all US Influence in W. Germany", 
					new PieceFilter(){
				public boolean accept(final GamePiece gp) {
					final CardEvent card = getCard(gp);
					return card.getOps() >= 3;
				}}, true));
			discarded = CountingPlayerHand.getHand(TSPlayerRoster.US).countCards() != nCards;
			if (!discarded && nCards > 0) {
				final int n = JOptionPane.showConfirmDialog(GameModule.getGameModule().getFrame(), 
						"Are you sure you want to remove all Influence in W. Germany?",
						getDescription(),
						JOptionPane.YES_NO_OPTION);
				if (n == JOptionPane.NO_OPTION) {
					if (canUndoEvent())
						return undoPlayEvent();
					else
						return updateState();
				}
			}
		}
		if (!discarded)
			comm = comm.append(Influence.getInfluenceMarker(Influence.W_GERMANY, TSPlayerRoster.US).removeAllInfluence());
		comm = comm.append(setFinished(true));
		return comm;
	}

	@Override
	protected Command myPlayEvent(final String who) {
		Command comm = super.myPlayEvent(who);
		final int nCards = CountingPlayerHand.getHand(TSPlayerRoster.US).countCards();
		if (nCards == 0) {
			final Chatter chatter = GameModule.getGameModule().getChatter();
			final Command chat = new Chatter.DisplayText(chatter, "US has no cards.");
			chat.execute();
			comm = comm.append(chat);
			comm = comm.append(updateState());
		}
		return comm;
	}
}
