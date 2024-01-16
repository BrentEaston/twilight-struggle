package ca.mkiefte.cards;

import java.awt.Point;
import java.awt.Dialog.ModalityType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.Map;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceFilter;
import VASSAL.tools.SequenceEncoder;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.Utilities;

public final class Chernobyl extends CardEvent {

	private static final String DESCRIPTION = "Chernobyl*";
	static final public String ID = "chernobyl;";
	private String targetRegion;

	public String getTargetRegion() {
		return targetRegion;
	}

	public Chernobyl() {
		this(ID, null);
	}

	public Chernobyl(final String type, final GamePiece inner) {
		super(type, inner);
	}

	@Override
	protected String getIdName() {
		return ID;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public Command updateState() {
		final HashMap<String,Set<Influence>> regions = new HashMap<String,Set<Influence>>();
		final Map map = Map.getMapById(Influence.MAIN_MAP_NAME);
		final Set<GamePiece> pieces = Utilities.findAllPiecesMatching(new PieceFilter() {			
			public boolean accept(final GamePiece piece) {
				final Influence marker = Influence.getInfluenceMarker(piece);
				return marker != null && TSPlayerRoster.US.equals(marker.getSide());
			}
		});
		for (final GamePiece gp : pieces) {
			final Influence marker = Influence.getInfluenceMarker(gp);
			final String region = marker.getRegion();
			Set<Influence> countries = regions.get(region);
			if (countries == null) {
				countries = new HashSet<Influence>();
				regions.put(region, countries);
			}
			countries.add(marker);
		}
		
		final Set<String> regionNames = regions.keySet();
		final HashMap<String,Point> centres = new HashMap<String,Point>(regionNames.size());
		for (final String region : regionNames) {
			final Point position = new Point();
			final Set<Influence> set = regions.get(region);
			for (final Influence marker : set) {
				final Point p = marker.getPosition();
				position.translate(p.x, p.y);
			}
			final int size = set.size();
			position.x /= size;
			position.y /= size;
			centres.put(region, position);
		}
		final JList list = new JList(regionNames.toArray(new String[regionNames.size()]));
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		final String message = "Select a region in which USSR may not place Influence this turn:";
		final Object objects[] = new Object[] {message, list};
		final JOptionPane pane;
		if (canUndoEvent())
			pane = new JOptionPane(objects, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, asIcon());
		else
			pane = new JOptionPane(objects, JOptionPane.QUESTION_MESSAGE, JOptionPane.DEFAULT_OPTION, asIcon(), new String[] {"Ok"});
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					final String selection = (String) list.getSelectedValue();
					if (selection == null)
						return;
					map.centerAt(centres.get(selection));
				}
			}
		});
		list.setSelectedIndex(0);
		final JDialog dialog = pane.createDialog(getName());
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);
		final Object selectedValue = pane.getValue();
		if (selectedValue == null || selectedValue == JOptionPane.UNINITIALIZED_VALUE)
			return undoPlayEvent();
		final Chatter chatter = GameModule.getGameModule().getChatter();
		final Command chat;
		final ChangeTracker tracker = new ChangeTracker(this);
		targetRegion = (String) list.getSelectedValue();
		eventFinished = true;
		Command comm = tracker.getChangeCommand();
		final StringBuilder builder = new StringBuilder("USSR may not add Influence to ").append(targetRegion).append(" by play of Ops.");
		chat = new Chatter.DisplayText(chatter, builder.toString());
		chat.execute();
		comm = comm.append(chat);
		return comm;
	}

	@Override
	public boolean isEventInEffect() {
		return isEventPlayedThisTurn();
	}

	@Override
	protected boolean eventFinishedOnExit() {
		return false;
	}

	@Override
	public String myGetState() {
		final SequenceEncoder encoder = new SequenceEncoder(super.myGetState(), '+');
		encoder.append(targetRegion);
		return encoder.getValue();
	}

	@Override
	public void mySetState(final String newState) {
		final SequenceEncoder.Decoder decoder = new SequenceEncoder.Decoder(newState, '+');
		super.mySetState(decoder.nextToken());
		targetRegion = decoder.nextToken(null);
	}

	@Override
	public boolean canUndoEvent() {
		return super.canUndoEvent() && TSPlayerRoster.US.equals(getOwner());
	}
}