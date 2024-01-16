package ca.mkiefte.cards;

import static ca.mkiefte.Utilities.adjustVps;

import java.awt.Dialog.ModalityType;
import java.util.Arrays;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import VASSAL.tools.SequenceEncoder;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.Utilities;

public final class Summit extends CardEvent {
	public static final String ID = "summit;";
	public static final String DESCRIPTION = "Summit";
	private String winner;
	
	public Summit() {
		this(ID, null);
	}

	public Summit(final String type, final GamePiece inner) {
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
		int american = 0;
		int soviet = 0;
		
		for (final ScoringCard card : ScoringCard.getScoringCards()) {
			card.updateRegion(false);
			american += card.hasControl(TSPlayerRoster.US, false) || card.hasDomination(TSPlayerRoster.US, false) ? 1 : 0;
			soviet += card.hasControl(TSPlayerRoster.USSR, false) || card.hasDomination(TSPlayerRoster.USSR, false) ? 1 : 0;
		}

		final Chatter chatter = GameModule.getGameModule().getChatter();
		StringBuilder builder = new StringBuilder("US die-roll modifer = +").append(american);
		Command chat = new Chatter.DisplayText(chatter, builder.toString());
		builder = new StringBuilder("USSR die-roll modifer = +").append(soviet);
		chat = chat.append(new Chatter.DisplayText(chatter, builder.toString()));
		chat.execute();
		comm = comm.append(chat);
		final int[] dieRolls = {0,0};
		comm = comm.append(Utilities.rollDie(new String[] {TSPlayerRoster.US,TSPlayerRoster.USSR}, dieRolls));
		dieRolls[0] += american;
		dieRolls[1] += soviet;
		final int vps;
		final ChangeTracker tracker = new ChangeTracker(this);
		if (dieRolls[1] > dieRolls[0]) {
			winner = TSPlayerRoster.USSR;
			vps = -2;
		} else if (dieRolls[0] > dieRolls[1]) {
			winner = TSPlayerRoster.US;
			vps = 2;
		} else {
			winner = null;
			vps = 0;
			chat = new Chatter.DisplayText(chatter, "Tie: no reroll.");
			chat.execute();
			return comm.append(chat);
		}
		if (winner != null) {
			builder = new StringBuilder(winner).append(" receives 2 VPs.");
			chat = new Chatter.DisplayText(chatter, builder.toString());
			chat.execute();
			comm = comm.append(chat);
			comm = comm.append(adjustVps(vps));
			builder = new StringBuilder(winner).append(" may change DEFCON one step in either direction.");
			chat = new Chatter.DisplayText(chatter, builder.toString());
			chat.execute();
			comm = comm.append(chat);
			comm = comm.append(tracker.getChangeCommand());
		}
		return comm;
	}

	@Override
	protected boolean eventFinishedOnExit() {
		return winner == null;
	}

	@Override
	public String myGetState() {
		final SequenceEncoder encoder = new SequenceEncoder(super.myGetState(), '+');
		encoder.append(winner);
		return encoder.getValue();
	}

	@Override
	public void mySetState(final String newState) {
		final SequenceEncoder.Decoder decoder = new SequenceEncoder.Decoder(newState, '+');
		super.mySetState(decoder.nextToken());
		winner = decoder.nextToken(null);
	}

	@Override
	protected void myClearState() {
		super.myClearState();
		winner = null;
	}

	@Override
	public String activeSide() {
		return winner;
	}

	@Override
	public Command updateState() {
		final String[] options = new String[3];
		final int defcon = Integer.valueOf(Utilities.getGlobalProperty(Utilities.DEFCON).getPropertyValue());
		StringBuilder builder = new StringBuilder("Increase to ").append(defcon+1);
		options[0] = builder.toString();
		builder = new StringBuilder("Keep at ").append(defcon);
		options[1] = builder.toString();
		builder = new StringBuilder("Decrease to ").append(defcon-1);
		options[2] = builder.toString();
		final JOptionPane optionPane = new JOptionPane("How would you like to move the DEFCON marker?",
				JOptionPane.QUESTION_MESSAGE,
				JOptionPane.YES_NO_CANCEL_OPTION,
				asIcon(),
				options,
				options[1]);
		final JDialog dialog = optionPane.createDialog(getDescription());
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);
		Command comm = null;
		final int n = Arrays.asList(options).indexOf(optionPane.getValue());
		switch (n) {
		case 0:
			comm = Utilities.adjustDefcon(+1);
			break;
		case 1:
			comm = Utilities.adjustDefcon(0);
			break;
		case 2:
			comm = Utilities.adjustDefcon(-1);
			break;
		}
		comm = comm.append(setFinished(true));
		return comm;
	}
}
