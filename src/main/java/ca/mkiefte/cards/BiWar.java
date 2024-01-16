package ca.mkiefte.cards;

import java.awt.Dialog.ModalityType;
import java.awt.Point;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import VASSAL.build.module.Map;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;

public abstract class BiWar extends WarCard {
	private static final String CANCEL = "Cancel";
	public final static String ID = "biwar;";
	public final static String DESCRIPTION = "Bi War";
	protected int target = -1;
		
	public BiWar(final String type, final GamePiece inner) {
		super(type, inner);
	}
	
	protected abstract String[] getPotentialTargetNames();

	protected Influence[] getPotentialTargets() {
		final String[] targetNames = getPotentialTargetNames();
		final Influence[] targets = new Influence[targetNames.length];
		final String opponent = TSPlayerRoster.US.equals(getOwner()) ? TSPlayerRoster.USSR : TSPlayerRoster.US;
		for (int i = 0; i < targetNames.length; ++i)
			targets[i] = Influence.getInfluenceMarker(targetNames[i], opponent);
		return targets;
	}

	@Override
	protected String getResultString(final boolean invaderWins) {
		final int winner = target == 0 ^ invaderWins ? 0 : 1;
		final StringBuilder builder = new StringBuilder(getPotentialTargets()[winner].getLocation()).append(" wins ").append(getDescription());
		return builder.toString();
	}

	@Override
	public Command updateState() {
		final String instigator = getInstigator();
		final Point p = new Point(0,0);
		final Influence[] beligerents = getPotentialTargets();
		for (final Influence inf : beligerents) {
			final Point pos = inf.getPosition();
			p.x += pos.x/2;
			p.y += pos.y/2;
		}
		Map.getMapById(Influence.MAIN_MAP_NAME).centerAt(p);
		final ArrayList<String> options = new ArrayList<String>();
		options.add(beligerents[1].getLocation() + " invades " + beligerents[0].getLocation());
		options.add(beligerents[0].getLocation() + " invades " + beligerents[1].getLocation());
		if (canUndoEvent())
			options.add(CANCEL);
		final JOptionPane optionPane = new JOptionPane(instigator + " Player's choice:", 
				JOptionPane.QUESTION_MESSAGE, 
				JOptionPane.YES_NO_OPTION, 
				asIcon(), 
				options.toArray(), 
				options.get(0));
		final JDialog dialog = optionPane.createDialog(getDescription());
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);
		target = options.indexOf(optionPane.getValue());
		if (target == 2)
			return undoPlayEvent();
		Command comm = super.doWar();
		comm = comm.append(setFinished(true));
		return comm;
	}

	@Override
	protected Command doWar() {
		return null;
	}

	@Override
	protected boolean eventFinishedOnExit() {
		return false;
	}

	@Override
	protected void myClearState() {
		super.myClearState();
		target = -1;
	}

	@Override
	protected Influence getTarget() {
		return getPotentialTargets()[target];
	}

	@Override
	protected String getInvader() {
		return getPotentialTargets()[1-target].getLocation();
	}
}