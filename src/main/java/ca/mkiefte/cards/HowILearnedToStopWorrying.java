package ca.mkiefte.cards;

import static ca.mkiefte.Utilities.adjustMilitaryOps;
import static ca.mkiefte.Utilities.getGlobalProperty;

import java.awt.Dialog.ModalityType;

import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import VASSAL.build.GameModule;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.Utilities;

public final class HowILearnedToStopWorrying extends CardEvent {
	public static final String ID = "howilearnedtostopworrying;";
	public static final String DESCRIPTION = "How I Learned to Stop Worrying*"; 

	public HowILearnedToStopWorrying() {
		this(ID, null);
	}

	public HowILearnedToStopWorrying(final String type, final GamePiece inner) {
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
		final String owner = getOwner();
		final String propertyName = TSPlayerRoster.US.equals(owner) ? Utilities.AMERICAN_MILITARY_OPS_PROP_NAME : Utilities.SOVIET_MILITARY_OPS_PROP_NAME;
		final int milOps = Integer.valueOf(getGlobalProperty(propertyName).getPropertyValue());
		comm = comm.append(adjustMilitaryOps(5 - milOps, owner));
		return comm;
	}

	@Override
	protected boolean eventFinishedOnExit() {
		return false;
	}

	@Override
	public Command updateState() {
		final String[] options = new String[5];
		for (int i = 1; i <= 5; ++i)
			options[i-1] = "DEFCON " + i;
		final JList list = new JList(options);
		list.setLayoutOrientation(JList.VERTICAL);
		final int defcon = Integer.valueOf(Utilities.getGlobalProperty(Utilities.DEFCON).getPropertyValue());
		Integer n;
		final StringBuilder message = new StringBuilder("Set the DEFCON at any level.");
		if (canUndoEvent())
			message.append("\n \"Cancel\" to Undo Play Event.");
		else
			message.append("\n \"Cancel\" to leave Defcon unchanged.");
		final Object objects[] = new Object[] {message, new JScrollPane(list)};

		do {
			final JOptionPane optionPane = new JOptionPane(objects, JOptionPane.QUESTION_MESSAGE);
			optionPane.setOptions(new String[] {Utilities.OK, Utilities.CANCEL});
			optionPane.setIcon(asIcon());
			list.setSelectedIndex(defcon-1);
			final JDialog dialog = optionPane.createDialog(getDescription());
			dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
			dialog.setAlwaysOnTop(true);
			dialog.setVisible(true);
			final Object value = optionPane.getValue();
			if (value.equals(JOptionPane.UNINITIALIZED_VALUE) || value.equals(Utilities.CANCEL)) {
				if (canUndoEvent())
					return undoPlayEvent();
				else if (JOptionPane.showConfirmDialog(GameModule.getGameModule().getPlayerWindow(),
						"Are you sure you wish to leave Defcon unchanged?",
						getDescription(),
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						asIcon()) == JOptionPane.YES_OPTION) {
					n = defcon;
					break;
				}
			} else {
				n = list.getSelectedIndex() + 1;
				break;
			}
		} while (true);
		Command comm = Utilities.adjustDefcon(n-defcon);
		comm = comm.append(setFinished(true));
		if (undoPlayEventCommand != null)
			undoPlayEventCommand = undoPlayEventCommand.append(comm.getUndoCommand());
		return comm;
	}
}
