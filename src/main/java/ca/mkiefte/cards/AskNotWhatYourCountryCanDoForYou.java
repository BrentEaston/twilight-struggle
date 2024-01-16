package ca.mkiefte.cards;

import static ca.mkiefte.Utilities.selectGamePiece;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.command.NullCommand;
import VASSAL.counters.GamePiece;
import ca.mkiefte.CountingPlayerHand;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.Utilities;

public final class AskNotWhatYourCountryCanDoForYou extends CardEvent {
	public static final String ID = "asknotwhatyourcountrycandoforyou;";
	public static final String DESCRIPTION = "\"Ask Not What Your Country Can Do For You...\"*";

	public AskNotWhatYourCountryCanDoForYou() {
		this(ID, null);
	}

	public AskNotWhatYourCountryCanDoForYou(final String type, final GamePiece inner) {
		super(type, inner);
	}

	@Override
	public Command updateState() {
		final CountingPlayerHand hand = CountingPlayerHand.getHand(TSPlayerRoster.US);
		final Set<CardEvent> cards = hand.getAllCards();
		final Set<CardEvent> discards = selectGamePiece(cards, getName(), 
				"Select cards to discard and replace with cards from draw deck:\n"
				+ "(CTRL/COMMAND & SHIFT for multiple selections) or \"Cancel\" to select none.", 
				null, false, true, true);
		final Chatter chatter = GameModule.getGameModule().getChatter();
		Command comm;
		if (discards == null || discards.isEmpty()) {
			final int n = JOptionPane.showConfirmDialog(GameModule.getGameModule().getFrame(), 
					"Are you sure you wish to discard no cards?",
					getDescription(),
					JOptionPane.YES_NO_OPTION);
			if (n == JOptionPane.NO_OPTION) {
				if (canUndoEvent())
					return undoPlayEvent();
				else
					return updateState();
			}
			comm = new Chatter.DisplayText(chatter, "US player does not discard any cards.");
			comm.execute();
		} else {
			comm = new NullCommand();
			final Set<String> names = new HashSet<String>(discards.size());
			for (final GamePiece card : discards) {
				comm = comm.append(getCard(card).discard());
				names.add(getCard(card).getDescription());
			}
			for (int i = 0; i < discards.size(); ++i)
				comm = comm.append(hand.dealCard());
			final StringBuilder builder = new StringBuilder("US player discards");
			Utilities.listAsString(builder, names, "and");
			Command chat = new Chatter.DisplayText(chatter, builder.toString());
			chat.execute();			
			comm = comm.append(chat);
		}
		comm = comm.append(setFinished(true));
		return comm;
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
}
