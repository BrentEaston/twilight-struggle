package ca.mkiefte;

import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.Map;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.properties.MutablePropertiesContainer;
import VASSAL.build.module.properties.MutableProperty;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.command.NullCommand;
import VASSAL.counters.Decorator;
import VASSAL.counters.Embellishment;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceFilter;
import VASSAL.counters.Stack;
import ca.mkiefte.cards.CardEvent;
import ca.mkiefte.cards.Norad;

public final class Utilities extends AbstractConfigurable {
	
	private static final int SIDES_ON_DICE = 6;
	public static final String VPS = "VPs";
	public static final String DEFCON = "DefCon";
	public static final String MILITARY_OPS = "Military Ops";
	public static final String MILITARY_OPS_PROP_NAME = "MilitaryOps";
	public static final String SOVIET_SPACE_RACE = "SovietSpaceRace";
	public static final String AMERICAN_SPACE_RACE = "AmericanSpaceRace";
	public static final String SOVIET_SPACE_RACE_MARKER = "Soviet Space Race";
	public static final String AMERICAN_SPACE_RACE_MARKER = "American Space Race";
	public static final String LOCATION_NAME = "LocationName";
	static final String NO_COUP_OR_REALIGNMENTS_ACTIVE = "NoCoupsOrReallignments_Active";
	protected static final String CONTINENT_PROP_NAME = "Continent";
	public static final String HAS_ROLLED_DIE = "HasRolledDie";
	public static final String MINIMUM_DIE_ROLL_PROP_NAME = "Die";
	public static final String CARDS_PLAYED_ON_SPACE_RACE = "CardsPlayed";
	public static final String SOVIET_MILITARY_OPS_PROP_NAME = TSPlayerRoster.USSR + MILITARY_OPS_PROP_NAME;
	public static final String AMERICAN_MILITARY_OPS_PROP_NAME = TSPlayerRoster.US + MILITARY_OPS_PROP_NAME;
	public static final int AMERICAN_AUTOMATIC_VICTORY = Integer.MAX_VALUE;
	public static final int SOVIET_AUTOMATIC_VICTORY = Integer.MIN_VALUE;


	public static final String CANCEL = "Cancel";
	public static final String OK = "OK";

	// global properties
	private static ArrayList<MutablePropertiesContainer> propertyContainers = null;

	// change global DEFCON property and move DEFCON counter.
	public static Command adjustDefcon(int delta) {
		Command comm = new NullCommand();
		final MutableProperty prop = getGlobalProperty(DEFCON);
		int currentDefcon = Integer.valueOf(prop.getPropertyValue());
		if (currentDefcon + delta < 1)
			delta = 1 - currentDefcon;
		else if (currentDefcon + delta > 5)
			delta = 5 - currentDefcon;
		final Chatter chatter = GameModule.getGameModule().getChatter();
		if (delta != 0) {
			int oldDefcon = currentDefcon;
			currentDefcon += delta;
			comm = setDefcon(currentDefcon);

			StringBuilder builder = new StringBuilder("DEFCON ").append(delta > 0 ? "increased" : "decreased").append(" to ").append(currentDefcon).append('.');
			Command chat = new Chatter.DisplayText(chatter, builder.toString());
			chat.execute();
			comm = comm.append(chat);
			
			if (currentDefcon == 1) {
				String side = getActivePlayer();
				final String opponent = TSPlayerRoster.US.equals(side) ? TSPlayerRoster.USSR : TSPlayerRoster.US;
				builder = new StringBuilder("!!! Global Thermal Nuclear War - ").append(opponent).append(" player wins. !!!");
				chat = new Chatter.DisplayText(chatter, builder.toString());
				chat.execute();
				comm = comm.append(chat);
				return comm;
			}
			
			final HashMap<Integer, GamePiece> map = getDefconRegionMarkers();			
			final boolean increasing = delta > 0;
			if (increasing) {
				for (int i = oldDefcon+1; i <= currentDefcon; ++i) {
					builder = new StringBuilder("Coup and Realignment attempts now permitted in ").append(map.get(i).getProperty("Continent")).append('.');
					chat = new Chatter.DisplayText(chatter, builder.toString());
					chat.execute();
					comm = comm.append(chat);
				}
			} else {
				for (int i = currentDefcon+1; i <= oldDefcon; ++i) {
					builder = new StringBuilder("Coup and Realignment attempts no longer permitted in ").append(map.get(i).getProperty("Continent")).append('.');
					chat = new Chatter.DisplayText(chatter, builder.toString());
					chat.execute();
					comm = comm.append(chat);
				}
			}
			
			final Norad norad = CardEvent.getCard(Norad.class);
			if (currentDefcon == 2 && TSTurnTracker.getCurrentRound() != 0)
				comm = comm.append(norad.setBonusThisRound(true));
		}
		else {
			Command chat = new Chatter.DisplayText(chatter, "No change in DEFCON.");
			chat.execute();
			comm = comm.append(chat);
		}
		return comm;
	}

	public static String getActivePlayer() {
		String side = TSTurnTracker.getPhasingPlayer();
		if (side == null) {
			final CardEvent card = CardEvent.eventCurrentlyPlaying();
			if (card != null)
				side = card.getOwner();
		}
		return side;
	}

	public static HashMap<Integer, GamePiece> getDefconRegionMarkers() {
		final Set<GamePiece> markers = findAllPiecesMatching(new PieceFilter() {				
			public boolean accept(final GamePiece piece) {
				return piece.getProperty("Continent") != null
						&& piece.getProperty("Defcon") != null;
			}
		});
		
		final HashMap<Integer,GamePiece> map = new HashMap<Integer,GamePiece>(markers.size());
		for (GamePiece m : markers) {
			final int min = Integer.valueOf((String) m.getProperty("Defcon"));
			map.put(min, m);				
		}
		return map;
	}

	public static Command setDefcon(final int currentDefcon) {
		final GamePiece defcon = getPieceNamed(DEFCON);
		final MutableProperty prop = getGlobalProperty(DEFCON);
		Command comm = prop.setPropertyValue(Integer.toString(currentDefcon));
		final GamePiece defconDestination = Utilities.getPieceNamed(new StringBuilder("DefCon ").append(currentDefcon).toString());
		comm = comm.append(defconDestination.getMap().placeAt(defcon, defconDestination.getPosition()));
		
		final HashMap<Integer, GamePiece> map = getDefconRegionMarkers();
		for (int i : map.keySet()) {
			GamePiece m = map.get(i);
			while (!(m instanceof Embellishment))
				m = ((Decorator) m).getInner();
			Embellishment em = (Embellishment) m;
			int min = Integer.valueOf((String) m.getProperty("Defcon"));
			boolean active = currentDefcon < min;
			if (active != em.isActive()) {
				ChangeTracker tracker = new ChangeTracker(m);
				em.setActive(active);
				comm = comm.append(tracker.getChangeCommand());
			}
		}
		return comm;
	}

	// change military operations and move marker
	public static Command adjustMilitaryOps(int delta, String side) {
		Command comm = new NullCommand();
		final GamePiece milops = getPieceNamed(side + " " + MILITARY_OPS);
		final MutableProperty prop = getGlobalProperty(side + MILITARY_OPS_PROP_NAME);
		int currentMilOps = Integer.valueOf(prop.getPropertyValue());
		final Chatter chatter = GameModule.getGameModule().getChatter();
		if (currentMilOps + delta < 0)
			delta = -currentMilOps;
		else if (currentMilOps + delta > 5)
			delta = 5 - currentMilOps;
		if (delta != 0) {
			currentMilOps += delta;
			comm = comm.append(prop.setPropertyValue(Integer.toString(currentMilOps)));
			final GamePiece milopsDestination = Utilities.getPieceNamed(new StringBuilder("").append(currentMilOps).append(' ').append(MILITARY_OPS).toString());
			comm = comm.append(milopsDestination.getMap().placeAt(milops, milopsDestination.getPosition()));
			StringBuilder builder = new StringBuilder(side).append(" Military Ops ").append(delta > 0 ? "increased" : "decreased").append(" to ").append(currentMilOps).append('.');
			Command chat = new Chatter.DisplayText(chatter, builder.toString());
			chat.execute();
			comm = comm.append(chat);
		}
		else {
			StringBuilder builder = new StringBuilder("No change in ").append(side).append(" Military Ops.");
			comm = new Chatter.DisplayText(chatter, builder.toString());
			comm.execute();
		}
		return comm;
	}

	// find a game piece
	public static GamePiece findPiece(final PieceFilter filter) {
		for (final GamePiece piece : GameModule.getGameModule().getGameState().getAllPieces()) {
			if (piece instanceof Stack) {
				final Stack s = (Stack) piece;
				for (int i = 0; i < s.getPieceCount(); i++) {
					final GamePiece gp = s.getPieceAt(i);
					if (filter.accept(gp))
						return gp;
				}
			}
			else if (filter.accept(piece))
				return piece;
		}
		return null;
	}
	
	public static Set<GamePiece> findAllPiecesMatching(final PieceFilter filter) {
		final Set<GamePiece> found = new HashSet<GamePiece>();
		for (final GamePiece piece : GameModule.getGameModule().getGameState().getAllPieces()) {
			if (piece instanceof Stack) {
				final Stack s = (Stack) piece;
				for (int i = 0; i < s.getPieceCount(); i++) {
					final GamePiece gp = s.getPieceAt(i);
					if (filter.accept(gp))
						found.add(gp);
				}
			}
			else if (filter.accept(piece))
				found.add(piece);
		}
		return found;
	}
	
	// find a piece with a given $BasicName$
	public static GamePiece getPieceNamed(final String name) {
		final GamePiece piece = findPiece(new PieceFilter() {
			public boolean accept(final GamePiece piece) {
				return piece.getName().equals(name);
			}});
		return piece;
	}

	// change VPs and move marker
	public static Command adjustVps(final int VPs) {
		Command comm = new NullCommand();
		final MutableProperty prop = getGlobalProperty(VPS);
		final Chatter chatter = GameModule.getGameModule().getChatter();
		// auto victory
		if (VPs == Utilities.SOVIET_AUTOMATIC_VICTORY) {
			comm = new Chatter.DisplayText(chatter, "!!! Automatic Soviet Victory !!!");
			comm.execute();
			comm = comm.append(prop.setPropertyValue(Integer.toString(-20)));
		}
		else if (VPs == Utilities.AMERICAN_AUTOMATIC_VICTORY) {
			comm = new Chatter.DisplayText(chatter, "!!! Automatic American Victory !!!");
			comm.execute();
			comm = comm.append(prop.setPropertyValue(Integer.toString(20)));
		}
		
		int currentVPs = Integer.valueOf(prop.getPropertyValue());			
		if (currentVPs >= 20 || currentVPs <= -20)
			return comm;
		
		if (VPs != 0) {
			final StringBuilder message = new StringBuilder("");
			if (VPs > 0)
				message.append("US");
			else
				message.append("USSR");
			message.append(" VPs increased by ").append(Math.abs(VPs)).append('.');
			Command chat = new Chatter.DisplayText(chatter, message.toString());
			chat.execute();
			comm.append(chat);
			currentVPs += VPs;
			currentVPs = currentVPs < -20 ? -20 : currentVPs;
			currentVPs = currentVPs > 20 ? 20 : currentVPs;
			comm = comm.append(setVps(currentVPs));
			if (Math.abs(currentVPs) == 20) {
				final String winner = currentVPs == 20 ? TSPlayerRoster.US : TSPlayerRoster.USSR;
				chat = new Chatter.DisplayText(chatter, new StringBuilder("!!! ").append(winner).append(" wins !!!").toString());
				chat.execute();
				comm = comm.append(chat);
			}
		} else {
			Command chat = new Chatter.DisplayText(chatter, "No change in VPs.");
			chat.execute();
			comm = comm.append(chat);
		}
		return comm;
	}
	
	public static Command setVps(final int vps) {
		final MutableProperty prop = getGlobalProperty(VPS);
		final GamePiece vp = getPieceNamed(VPS);
		Command comm = prop.setPropertyValue(Integer.toString(vps));
		final GamePiece vpDestination = getPieceNamed(new StringBuilder("").append(vps).append(" VPs").toString());
		comm = comm.append(vpDestination.getMap().placeAt(vp, vpDestination.getPosition()));
		return comm;
	}

	public static Command advanceSpaceRace(final String side) {
		return advanceSpaceRace(side, true);
	}
	
	public static Command setCardPlayedOnSpaceRace(final String who, final int n) {
		final String pieceName;
		if (TSPlayerRoster.USSR.equals(who)) { 
			pieceName = SOVIET_SPACE_RACE_MARKER;
		}
		else {
			pieceName = AMERICAN_SPACE_RACE_MARKER;
		}
		final GamePiece marker = getPieceNamed(pieceName);
		final ChangeTracker tracker = new ChangeTracker(marker);
		marker.setProperty(CARDS_PLAYED_ON_SPACE_RACE, Integer.toString(n));
		return tracker.getChangeCommand();
	}
	
	public static Command advanceSpaceRace(final String side, final boolean addVps) {
		final String myPropName;
		final String opPropName;
		final boolean soviet = TSPlayerRoster.USSR.equals(side);
		Command comm = new NullCommand();
		
		if (soviet) {
			myPropName = SOVIET_SPACE_RACE;
			opPropName = AMERICAN_SPACE_RACE;
		}
		else {
			myPropName = AMERICAN_SPACE_RACE;
			opPropName = SOVIET_SPACE_RACE;
		}

		final Chatter chatter = GameModule.getGameModule().getChatter();

		final MutableProperty myProp = getGlobalProperty(myPropName);
		final MutableProperty opProp = getGlobalProperty(opPropName);
		int mySpaceRace = Integer.valueOf(myProp.getPropertyValue());
		
		if (mySpaceRace == 8) {
			Command chat = new Chatter.DisplayText(chatter, "Unable to advance space race beyond Space Station.");
			chat.execute();
			return chat;
		}
		
		int opSpaceRace = Integer.valueOf(opProp.getPropertyValue());
		
		++mySpaceRace;
		comm = comm.append(setSpaceRace(side, mySpaceRace));
		final GamePiece spaceDestination = getPieceNamed(new StringBuilder("").append(mySpaceRace).append(" Space Race").toString());
		final String name = (String) spaceDestination.getProperty("Name");
		StringBuilder builder = new StringBuilder(side).append(" Space Race advanced to ").append(name).append('.');
		Command chat = new Chatter.DisplayText(chatter, builder.toString());
		chat.execute();
		comm = comm.append(chat);
		
		final boolean first = mySpaceRace > opSpaceRace;
		if (addVps) {
			int vps = 0;
			if (first) {
				if (mySpaceRace == 1 || mySpaceRace == 3 || mySpaceRace == 8)
					vps = 2;
				else if (mySpaceRace == 5)
					vps = 3;
				else if (mySpaceRace == 7)
					vps = 4;
			}
			else {
				if (mySpaceRace == 1 || mySpaceRace == 5)
					vps = 1;
				else if (mySpaceRace == 7)
					vps = 2;
			}
			if (soviet)
				vps *= -1;
			if (vps != 0)
				comm = comm.append(adjustVps(vps));
		}
		
		builder = null;
		final String op = TSPlayerRoster.US.equals(side) ? TSPlayerRoster.USSR : TSPlayerRoster.US;
		if (mySpaceRace == 2) {
			if (first)
				builder = new StringBuilder(side).append(" man now play 2 Space Race Cards per turn");
			else
				builder = new StringBuilder(op).append(" may no longer play 2 Space Race Cards per turn.");
		} else if (mySpaceRace == 4) {
			if (first)
				builder = new StringBuilder(op).append(" must choose and show headline card first.");
			else
				builder = new StringBuilder(side).append(" no longer has to choose and show headline card first.");
		} else if (mySpaceRace == 6) {
			if (first)
				builder = new StringBuilder(side).append(" may discard 1 held card at the end of the turn.");
			else
				builder = new StringBuilder(op).append(" may no longer discard 1 held card at the end of the turn.");
		} else if (mySpaceRace == 8) {
			if (first)
				builder = new StringBuilder(side).append(" may take 8 Action Rounds per Turn.");
			else
				builder = new StringBuilder(op).append(" may no longer take 8 Action Rounds per Turn.");
		}
		
		if (builder != null) {
			chat = new Chatter.DisplayText(chatter, builder.toString());
			chat.execute();
			comm = comm.append(chat);
		}
		
		return comm;
	}
	
	public static Command setSpaceRace(final String who, final int level) {
		final String propertyName;
		final String markerName;
		if (TSPlayerRoster.US.equals(who)) {
			propertyName = AMERICAN_SPACE_RACE;
			markerName = AMERICAN_SPACE_RACE_MARKER;
		} else {
			propertyName = SOVIET_SPACE_RACE;
			markerName = SOVIET_SPACE_RACE_MARKER;
		}
		final GamePiece marker = getPieceNamed(markerName);
		final MutableProperty property = getGlobalProperty(propertyName);
		Command comm = property.setPropertyValue(Integer.toString(level));
		final GamePiece spaceDestination = getPieceNamed(new StringBuilder(0).append(level).append(" Space Race").toString());
		comm = comm.append(spaceDestination.getMap().placeAt(marker, spaceDestination.getPosition()));
		return comm;
	}

	// get a global property
	public static MutableProperty getGlobalProperty(final String propertyName) {
		if (propertyContainers == null) {
			propertyContainers = new ArrayList<MutablePropertiesContainer>();
			propertyContainers.add(GameModule.getGameModule());
		}
		return MutableProperty.Util.findMutableProperty(propertyName, propertyContainers);
	}

	public static final String FALSE = "false";
	public static final String TRUE = "true";

	@Override
	public String[] getAttributeDescriptions() {
		return new String[0];
	}

	@Override
	public Class<?>[] getAttributeTypes() {
		return new Class[0];
	}

	@Override
	public String[] getAttributeNames() {
		return new String[0];
	}

	@Override
	public String getAttributeValueString(final String key) {
		return null;
	}

	@Override
	public void setAttribute(final String key, final Object value) {
	}

	@SuppressWarnings("rawtypes")
	public Class[] getAllowableConfigureComponents() {
		return new Class[0];
	}

	public HelpFile getHelpFile() {
		return null;
	}

	public void removeFrom(final Buildable parent) {
	}

	static public void listAsString(final StringBuilder builder, final Set<String> list, final String conjuction) {
		if (builder.charAt(builder.length()-1) != ' ')
			builder.append(' ');
		final Iterator<String> iter = list.iterator();
		String elem = iter.next();
		do {
			builder.append(elem);
			if (iter.hasNext()) {
				if (list.size() > 2)
					builder.append(", ");
				else if (list.size() == 2)
					builder.append(' ');
				elem = iter.next();
				if (!iter.hasNext()) {
					builder.append(conjuction);
					if (!conjuction.endsWith(" "))
						builder.append(' ');
				}
			} else {
				builder.append('.');
				break;
			}
		} while (true);
	}

	public static <T extends GamePiece> T selectGamePiece(
			final Set<T> options, 
			final String title, 
			final String message, 
			final Icon icon,
			final boolean canCancel
			) {
		return selectGamePiece(options, title, message, icon, false, canCancel);
	}
	
	public static <T extends GamePiece> T selectGamePiece(
			final Set<T> options, 
			final String title, 
			final String message,
			final Icon icon,
			final boolean byLocation, 
			final boolean canCancel
			) {
		final Set<T> pieces = selectGamePiece(options, title, message, icon, byLocation, false, canCancel);
		if (pieces == null || pieces.size() == 0)
			return null;
		else
			return pieces.iterator().next();
	}

	public static <T extends GamePiece> Set<T> selectGamePiece(
			final Set<T> options, 
			String title, 
			String message, 
			final Icon icon,
			final boolean byLocation,
			final boolean multiple, 
			final boolean canCancel
			) {
		if (title == null)
			title = "Select a Game Piece";
		if (message == null)
			message = "Select a game piece:";
		final HashMap<String,T> names = new HashMap<String,T>(options.size());
		for (T gp : options) {
			if (byLocation)
				names.put((String) gp.getProperty(LOCATION_NAME), gp);
			else
				names.put(gp.getName(), gp);
		}
		final String[] items = names.keySet().toArray(new String[names.size()]);
		Arrays.sort(items);
		final JList list = new JList(items);
		final int mode = multiple ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION;
		list.setSelectionMode(mode);
		list.setLayoutOrientation(JList.VERTICAL);
//		list.setVisibleRowCount(names.size() > 8 ? 8 : names.size());		
		final Object objects[] = new Object[] {message, new JScrollPane(list)};
		final JOptionPane pane = new JOptionPane(objects, JOptionPane.QUESTION_MESSAGE);
		if (canCancel)
			pane.setOptions(new String[] {OK, CANCEL});
		else
			pane.setOptions(new String[] {OK});
		if (icon != null)
			pane.setIcon(icon);
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					final String sel = (String) list.getSelectedValue();
					if (sel == null)
						return;
					final GamePiece gamePiece = names.get(sel);
					final Map map = gamePiece.getMap();
					if (byLocation) {
						final Rectangle rect = gamePiece.boundingBox();
						final Point p = gamePiece.getPosition();
						rect.translate(p.x, p.y);
						map.ensureVisible(rect);
					} else if (icon == null) { 
						pane.setIcon(new Icon() {					
							public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
								gamePiece.draw(g, x+getIconWidth()/2, y+getIconHeight()/2, pane, 0.5);
							}					
							public int getIconWidth() {
								return gamePiece.boundingBox().width/2;
							}					
							public int getIconHeight() {
								return gamePiece.boundingBox().height/2;
							}
						});
						list.grabFocus();
					}
				}
			}
		});
		list.setSelectedIndex(0);
		final JDialog dialog = pane.createDialog(null, title);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);
		final Object selectedValue = pane.getValue();
		if (selectedValue == null || selectedValue == JOptionPane.UNINITIALIZED_VALUE || 
				selectedValue instanceof Integer && ((Integer) selectedValue).intValue() == JOptionPane.DEFAULT_OPTION)
			return null;
		final String val = (String) selectedValue;
		if (!val.equals(CANCEL)) {
			final int[] sels = list.getSelectedIndices();
			final Set<T> pieces = new HashSet<T>(sels.length);
			for (int i : sels)
				pieces.add(names.get(items[i]));
			return pieces;
		}
		else
			return null;
	}

	public static Command rollDie(final String who, final int[] i) {
		return rollDie(new String[] {who}, i);
	}
	
	public static Command rollDie(final String[] who, final int[] i) {
		final GameModule gameModule = GameModule.getGameModule();
		final Chatter chatter = gameModule.getChatter();
		Command comm = new NullCommand();
		for (int j = 0; j < i.length; ++j) {
			i[j] = 1+gameModule.getRNG().nextInt(SIDES_ON_DICE);
			final StringBuilder builder = new StringBuilder(who[j]).append(" rolls a ").append(i[j]).append('.');
			comm = comm.append(new Chatter.DisplayText(chatter, builder.toString()));
		}
		comm.execute();
		comm = comm.append(getGlobalProperty(HAS_ROLLED_DIE).setPropertyValue(Utilities.TRUE));
		return comm;
	}

	public void addTo(final Buildable parent) {
	}
}
