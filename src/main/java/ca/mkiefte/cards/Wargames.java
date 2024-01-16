package ca.mkiefte.cards;

import static ca.mkiefte.Utilities.adjustVps;
import static ca.mkiefte.Utilities.getGlobalProperty;

import java.awt.Dialog.ModalityType;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.properties.MutableProperty;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import VASSAL.tools.SequenceEncoder;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.TSTurnTracker;
import ca.mkiefte.Utilities;

public final class Wargames extends CardEvent {
	public static final String ID = "wargames;";
	public static final String DESCRIPTION = "Wargames*";
	private boolean gameOver;

	public Wargames() {
		this(ID, null);
	}

	public Wargames(final String type, final GamePiece inner) {
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
		final int defcon = Integer.valueOf(getGlobalProperty(Utilities.DEFCON).getPropertyValue());
		return super.isEventPlayable(who) && defcon == 2;
	}

	@Override
	public Command updateState() {
		Command comm = null;
		final String who = getOwner();
		final String opponent = TSPlayerRoster.USSR.equals(who) ? TSPlayerRoster.US : TSPlayerRoster.USSR;
		final GameModule gameModule = GameModule.getGameModule();
		final MutableProperty prop = Utilities.getGlobalProperty(Utilities.VPS);
		final int currentVPs = Integer.valueOf(prop.getPropertyValue());
		final StringBuilder builder = new StringBuilder(currentVPs < 0 ? "USSR" : "US");
		builder.append(" currently has ").append(Math.abs(currentVPs)).append(" VP");
		if (Math.abs(currentVPs) > 1)
			builder.append('s');
		builder.append(".\nDo you wish to end the game after giving ").append(opponent).append(" 6 VPs?");
		final JOptionPane optionPane = new JOptionPane(builder.toString(), 
				JOptionPane.QUESTION_MESSAGE,
				JOptionPane.YES_NO_OPTION,
				asIcon());
		final JDialog dialog = optionPane.createDialog(getDescription());
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);
		final int answer = (Integer) optionPane.getValue();
		final Chatter chatter = gameModule.getChatter();
		switch (answer) {
		case JOptionPane.YES_OPTION: // Ends game
			comm = adjustVps(TSPlayerRoster.US.equals(opponent) ? +6 : -6);
			if (TSTurnTracker.isGameOver())
				return comm.append(setFinished(true));
			final int vps = Integer.valueOf(prop.getPropertyValue());
			Command chat;
			if (vps < 0)
				chat = new Chatter.DisplayText(chatter, "!!! Soviet player wins !!!");
			else if (vps > 0)
				chat = new Chatter.DisplayText(chatter, "!!! American player wins !!!");
			else
				chat = new Chatter.DisplayText(chatter, "!!! Game is tied !!!");
			chat = chat.append(new Chatter.DisplayText(chatter, "You may now play chess."));
			chat.execute();
			comm = comm.append(chat);
			comm = comm.append(setGameOver(true));
			break;
		case JOptionPane.NO_OPTION: // Doesn't end game
			comm = new Chatter.DisplayText(chatter, new StringBuilder(who).append(" does not end the game.").toString());
			comm.execute();
			break;
		}
		return comm.append(setFinished(true));
	}

	@Override
	protected boolean eventFinishedOnExit() {
		return false;
	}
	
	public Command setGameOver(final boolean b) {
		final ChangeTracker tracker = new ChangeTracker(this);
		gameOver = true;
		return tracker.getChangeCommand();
	}
	
	public boolean isGameOver() {
		return gameOver;
	}

	@Override
	public String myGetState() {
		final SequenceEncoder encoder = new SequenceEncoder(super.myGetState(), '+');
		encoder.append(gameOver);
		return encoder.getValue();
	}

	@Override
	public void mySetState(String newState) {
		final SequenceEncoder.Decoder decoder = new SequenceEncoder.Decoder(newState, '+');
		super.mySetState(decoder.nextToken());
		gameOver = decoder.nextBoolean(false);
	}
}
