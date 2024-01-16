package ca.mkiefte.cards;

import java.awt.Dialog.ModalityType;
import java.awt.Rectangle;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.Map;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import VASSAL.tools.SequenceEncoder;

public abstract class ChangeInfluenceOptions extends ChangeInfluence {
	protected String option;

	public ChangeInfluenceOptions(String type, GamePiece inner) {
		super(type, inner);
	}

	protected abstract String[] getOptions();
	protected abstract String getOptionsMessage();	
	protected abstract String getStringForOption(String option);
	protected abstract Rectangle getFocusPosition(); 
	

	@Override
	public String myGetState() {
		final SequenceEncoder encoder = new SequenceEncoder(super.myGetState(), '+');
		encoder.append(option);
		return encoder.getValue();
	}

	@Override
	public void mySetState(String newState) {
		final SequenceEncoder.Decoder decoder = new SequenceEncoder.Decoder(newState, '+');
		super.mySetState(decoder.nextToken());
		option = decoder.nextToken(null);
	}

	@Override
	protected void myClearState() {
		super.myClearState();
		option = null;
	}
	
	@Override
	public Command updateState() {
		Command comm = null;
		
		if (option == null || option.isEmpty()) {
			Map.getMapById(MAIN_MAP).ensureVisible(getFocusPosition());
			final String[] options = getOptions();
			final JOptionPane optionPane = new JOptionPane(getOptionsMessage(),
					JOptionPane.QUESTION_MESSAGE, 
					JOptionPane.YES_NO_OPTION, 
					asIcon(), 
					options, 
					options[0]);
			final JDialog dialog = optionPane.createDialog(getDescription());
			dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
			dialog.setAlwaysOnTop(true);
			dialog.setVisible(true);
			final ChangeTracker tracker = new ChangeTracker(this);
			option = (String) optionPane.getValue();
			comm = tracker.getChangeCommand();
			final String message = getStringForOption(option);
			final Chatter chatter = GameModule.getGameModule().getChatter();
			final Command chat = new Chatter.DisplayText(chatter, message);
			chat.execute();
			comm = comm.append(chat).append(setup());
		}
		
		if (!eventFinished) {
			return comm == null ? super.updateState() : comm.append(super.updateState());
		}
		else
			return comm;	
	}

	@Override
	protected Command setup() {		
		if (option == null || option.isEmpty()) {
			final String[] options = getOptions();
			if (options.length == 1) {
				final ChangeTracker tracker = new ChangeTracker(this);
				option = options[0];
				Command comm = tracker.getChangeCommand();
				return comm.append(super.setup());
			} else
				return null;
		} else {
			Command comm = super.setup();
			if (!eventFinished)
				auto = false;
			return comm;
		}
	}
}
