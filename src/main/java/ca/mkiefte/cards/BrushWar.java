package ca.mkiefte.cards;

import java.awt.Dialog.ModalityType;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.Map;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceFilter;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.Utilities;

public final class BrushWar extends WarCard {
	private static final int VPS = 1;
	private static final int MIL_OPS = 3;
	public final static String ID = "brushwar;";
	public final static String DESCRIPTION = "Brush War";
	protected Influence target;
		
	public BrushWar() {
		this(ID, null);
	}

	public BrushWar(final String type, final GamePiece inner) {
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
	protected String getResultString(final boolean invaderWins) {
		final StringBuilder builder;
		if (invaderWins)
			builder = new StringBuilder("Insurgents win Brush War in ");
		else
			builder = new StringBuilder("Insurgents defeated in ");
		builder.append(getTarget().getLocation()).append('.');
		return builder.toString();
	}
	
	protected Set<Influence> getPotentialTargets() {
		final String instigator = getInstigator();
		final String opponent = TSPlayerRoster.US.equals(instigator) ? TSPlayerRoster.USSR : TSPlayerRoster.US;
		final Set<GamePiece> pieces = Utilities.findAllPiecesMatching(new PieceFilter() {			
			public boolean accept(final GamePiece piece) {
				final Influence marker = Influence.getInfluenceMarker(piece);
				return marker != null 
						&& opponent.equals(marker.getSide())
						&& marker.getStability() <= 2
						&& getCard(Nato.class).canBrushWar(marker);
			}
		});
		final Set<Influence> markers = new HashSet<Influence>(pieces.size());
		for (final GamePiece gp : pieces)
			markers.add(Influence.getInfluenceMarker(gp));
		return markers;
	}
	
	@Override
	public Command updateState() {
		final Set<Influence> markers = getPotentialTargets();
		target = mySelectGamePiece(markers, getDescription(), "Target of Brush War (die-roll modifier):");
		if (target == null)
			return undoPlayEvent();
		Command comm = super.doWar();
		comm = comm.append(setFinished(true));
		return comm;
	}
	
	private static class MyListModel extends AbstractListModel implements ActionListener {
		
		private static final long serialVersionUID = -8417831980624830138L;
		String[] options;
		String[] subOptions;
		String[] which;
		
		MyListModel(final String[] options, final String[] subOptions, final boolean isSelected) {
			this.options = options;
			this.subOptions = subOptions;
			which = isSelected ? subOptions : options;
		}
		
		public void actionPerformed(final ActionEvent e) {
			final boolean selected = ((JCheckBox) e.getSource()).isSelected();			
			which = selected ? subOptions : options;
			fireContentsChanged(this, 0, subOptions.length);
			if (selected)
				fireIntervalRemoved(this, subOptions.length, options.length-1);
			else
				fireIntervalAdded(this, subOptions.length, options.length-1);
		}

		public Object getElementAt(final int index) {
			return which[index];
		}

		public int getSize() {
			return which.length;
		}
	}
	
	private Influence mySelectGamePiece(final Set<Influence> options, final String title, final String message) {
		final HashMap<String,Influence> names = new HashMap<String,Influence>(options.size());
		final String opponent = TSPlayerRoster.US.equals(getInstigator()) ? TSPlayerRoster.USSR : TSPlayerRoster.US;
		for (Influence marker : options) {
			final String location = marker.getLocation();
			final Set<Influence> neighbours = marker.getNeighbours();
			int modifier = 0;
			for (Influence m : neighbours)
				if (m.hasControl())
					--modifier;			
			names.put(location + " (" + modifier + ")", marker);
		}
		final Set<String> subSet = new HashSet<String>(names.keySet());
		final String[] items = subSet.toArray(new String[subSet.size()]);
		Arrays.sort(items);
		final Iterator<String> iter = subSet.iterator();
		while(iter.hasNext()) {
			final Influence marker = names.get(iter.next());
			if (marker.getInfluence() == 0)
				iter.remove();
		}
		final String[] subItems = subSet.toArray(new String[subSet.size()]);
		Arrays.sort(subItems);
		final JCheckBox checkBox = new JCheckBox("Only targets with " + opponent + " Influence.");
		if (subItems.length == 0)
			checkBox.setEnabled(false);
		else
			checkBox.setSelected(true);
		final MyListModel listModel = new MyListModel(items, subItems, subItems.length > 0);
		final JList list = new JList(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(subSet.size() > 8 ? 8 : subSet.size());
		checkBox.addActionListener(listModel);
		checkBox.addActionListener(new ActionListener() {			
			public void actionPerformed(final ActionEvent e) {
				list.setSelectedIndex(0);
			}
		});
		final Object objects[] = new Object[] {message, new JScrollPane(list), checkBox};
		int optionType = canUndoEvent() ? JOptionPane.OK_CANCEL_OPTION : JOptionPane.OK_OPTION;
		final JOptionPane pane;
		if (optionType == JOptionPane.OK_CANCEL_OPTION)
			pane = new JOptionPane(objects, JOptionPane.QUESTION_MESSAGE, optionType, null);
		else
			pane = new JOptionPane(objects, JOptionPane.QUESTION_MESSAGE, optionType, null, new String[] {"Ok"});			
		pane.setIcon(asIcon());
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					final String sel = (String) list.getSelectedValue();
					if (sel == null)
						return;
					final Influence marker = names.get(sel);
					final Map map = marker.getMap();
					final Rectangle rect = marker.boundingBox();
					final Point p = marker.getPosition();
					rect.translate(p.x, p.y);
					map.ensureVisible(rect);
				}
			}
		});
		list.setSelectedIndex(0);
		final JDialog dialog = pane.createDialog(null, title);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);
		if (pane.getValue() == null || pane.getValue() == JOptionPane.UNINITIALIZED_VALUE)
			return null;
		else
			return names.get((String) list.getSelectedValue());
	}
	
	public Command getQueryCommand(final String who) {
		target = null;
		getTarget();
		final Chatter chatter = GameModule.getGameModule().getChatter();
		Command comm;
		if (target != null) {
			comm = new Chatter.DisplayText(chatter, "* " + who + " selects " + target + " as target of Brush War.");
			comm.execute();
			comm = comm.append(super.doWar());
		}
		else {
			comm = new Chatter.DisplayText(chatter, "* " + who + " does not select a target.");
			comm.execute();
		}
		return comm;
	}

	public String getInfoMessage(String who) {
		return who + " player must select target of invasion.";
	}

	@Override
	protected int getMilitaryOps() {
		return MIL_OPS;
	}

	@Override
	protected int getVps(final String who) {
		return TSPlayerRoster.US.equals(who) ? VPS : -VPS;
	}

	@Override
	protected boolean isSuccessful(final int result) {
		return result >= 3;
	}

	@Override
	protected String getInvader() {
		return "Insurgent force";
	}

	@Override
	protected Influence getTarget() {
		return target;
	}

	@Override
	protected void myClearState() {
		super.myClearState();
		target = null;
	}

	@Override
	protected Command doWar() {
		return null;
	}

	@Override
	protected boolean eventFinishedOnExit() {
		return false;
	}
}