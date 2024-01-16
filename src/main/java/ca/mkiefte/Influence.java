package ca.mkiefte;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.Map;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.properties.MutableProperty;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.command.NullCommand;
import VASSAL.counters.BasicPiece;
import VASSAL.counters.Decorator;
import VASSAL.counters.EditablePiece;
import VASSAL.counters.Embellishment;
import VASSAL.counters.GamePiece;
import VASSAL.counters.KeyCommand;
import VASSAL.counters.PieceFilter;
import VASSAL.tools.SequenceEncoder;
import ca.mkiefte.cards.CardEvent;
import ca.mkiefte.cards.Chernobyl;
import ca.mkiefte.cards.CubanMissileCrisis;
import ca.mkiefte.cards.IranContraScandal;
import ca.mkiefte.cards.LatinAmericanDeathSquads;
import ca.mkiefte.cards.Nato;
import ca.mkiefte.cards.NuclearSubs;
import ca.mkiefte.cards.SaltNegotiations;
import ca.mkiefte.cards.ScoringCard;
import ca.mkiefte.cards.TheReformer;
import ca.mkiefte.cards.UsJapanMutualDefensePact;
import ca.mkiefte.cards.VietnamRevolts;
import ca.mkiefte.cards.YuriAndSamantha;

public class Influence extends Decorator implements EditablePiece {
	public static final String ID = "influence;";
	public static final String DESCRIPTION = "Influence";
	
	private static final String CANNOT_COUP = "Cannot Coup";
	private static final String COUP = "Coup";
	private static final String CANNOT_REALIGN = "Cannot Realign";
	private static final String REALIGN = "Realign";
	protected static final String DECREASE_INFLUENCE = "Decrease Influence";
	protected static final String INCREASE_INFLUENCE = "Increase Influence";
	protected static final String PRESENCE_LEVEL = "1";
	protected static final String CONTROL_LEVEL = "2";
	protected static final String NO_PRESENCE_LEVEL = "0";

	public static final String INFLUENCE = "Influence";
	public static final String CONTROL = "Control";
	public static final String SIDE = "Side";
	public static final String HIGHLIGHTER = "Highlighter";
	public static final String WHO_RECEIVES_BONUS_INFLUENCE = "WhoReceivesBonusInfluence";
	public static final String BONUS_INFLUENCE = "BonusInfluence";
	public static final String SOVIET_STARTING_INFLUENCE = "SovietStartingInfluence";
	public static final String AMERICAN_STARTING_INFLUENCE = "AmericanStartingInfluence";
	public static final String OPS_REMAINING = "OpsRemaining";
	public static final String CONDITIONAL_INFLUENCE = "ConditionalInfluence";
	public static final String OPS_USED_AS = "OperationsUsedAs";
	public static final String PLACE_INFLUENCE = "Place Influence";
	public static final String REMOVE_INFLUENCE = "Remove Influence";
	public static final String REALIGNMENT = "Realignment";
	public static final String WHO_PLAYED_OPS = "WhoPlayedOps";
	
	public static final int TOTAL_AMERICAN_STARTING_INFLUENCE = 7;
	public static final int TOTAL_SOVIET_STARTING_INFLUENCE = 6;
	
	private static InfluenceDialog dialog;
	protected int atStartInfluence;
	protected int startingInfluence;
	protected int stability;
	protected String region;
	protected boolean isBattleground;
	protected int influence;
	private String location;
	private String side;
	
	public static final String COSTA_RICA = "Costa Rica";
	public static final String HONDURAS = "Honduras";
	public static final String EL_SALVADOR = "El Salvador";
	public static final String GUATEMALA = "Guatemala";
	public static final String MEXICO = "Mexico";
	public static final String CANADA = "Canada";
	public static final String U_K = "U.K.";
	public static final String NORWAY = "Norway";
	public static final String SWEDEN = "Sweden";
	public static final String FINLAND = "Finland";
	public static final String POLAND = "Poland";
	public static final String E_GERMANY = "E. Germany";
	public static final String W_GERMANY = "W. Germany";
	public static final String FRANCE = "France";
	public static final String ITALY = "Italy";
	public static final String AUSTRIA = "Austria";
	public static final String HUNGARY = "Hungary";
	public static final String S_KOREA = "S. Korea";
	public static final String JAPAN = "Japan";
	public static final String CUBA = "Cuba";
	public static final String NICARAGUA = "Nicaragua";
	public static final String PANAMA = "Panama";
	public static final String COLOMBIA = "Colombia";
	public static final String ECUADOR = "Ecuador";
	public static final String PERU = "Peru";
	public static final String CHILE = "Chile";
	public static final String ARGENTINA = "Argentina";
	public static final String PARAGUAY = "Paraguay";
	public static final String URUGUAY = "Uruguay";
	public static final String BRAZIL = "Brazil";
	public static final String VENEZUELA = "Venezuela";
	public static final String N_KOREA = "N. Korea";
	public static final String TAIWAN = "Taiwan";
	public static final String PHILIPPINES = "Philippines";
	public static final String INDONESIA = "Indonesia";
	public static final String MALAYSIA = "Malaysia";
	public static final String AUSTRALIA = "Australia";
	public static final String HAITI = "Haiti";
	public static final String DOMINICAN_REP = "Dominican Rep";
	public static final String BOLIVIA = "Bolivia";
	public static final String DENMARK = "Denmark";
	public static final String BENELUX = "Benelux";
	public static final String SPAIN_PORTUGAL = "Spain/Portugal";
	public static final String ALGERIA = "Algeria";
	public static final String TUNISIA = "Tunisia";
	public static final String LIBYA = "Libya";
	public static final String EGYPT = "Egypt";
	public static final String SUDAN = "Sudan";
	public static final String ETHIOPIA = "Ethiopia";
	public static final String SOMALIA = "Somalia";
	public static final String KENYA = "Kenya";
	public static final String SE_AFRICAN_STATES = "SE African States";
	public static final String ZIMBABWE = "Zimbabwe";
	public static final String BOTSWANA = "Botswana";
	public static final String THAILAND = "Thailand";
	public static final String VIETNAM = "Vietnam";
	public static final String LAOS_CAMBODIA = "Laos/Cambodia";
	public static final String BURMA = "Burma";
	public static final String INDIA = "India";
	public static final String PAKISTAN = "Pakistan";
	public static final String ROMANIA = "Romania";
	public static final String AFGHANISTAN = "Afghanistan";
	public static final String IRAN = "Iran";
	public static final String IRAQ = "Iraq";
	public static final String GULF_STATES = "Gulf States";
	public static final String SAUDI_ARABIA = "Saudi Arabia";
	public static final String MOROCCO = "Morocco";
	public static final String YUGOSLAVIA = "Yugoslavia";
	public static final String GREECE = "Greece";
	public static final String CZECHOSLOVAKIA = "Czechoslovakia";
	public static final String NIGERIA = "Nigeria";
	public static final String SAHARAN_STATES = "Saharan States";
	public static final String WEST_AFRICAN_STATES = "West African States";
	public static final String IVORY_COAST = "Ivory Coast";
	public static final String CAMEROON = "Cameroon";
	public static final String ZAIRE = "Zaire";
	public static final String ANGOLA = "Angola";
	public static final String TURKEY = "Turkey";
	public static final String BULGARIA = "Bulgaria";
	public static final String SYRIA = "Syria";
	public static final String ISRAEL = "Israel";
	public static final String JORDAN = "Jordan";
	public static final String SOUTH_AFRICA = "South Africa";
	public static final String LEBANON = "Lebanon";
	public static final String CHINA = "China";
	public static final String U_S_S_R = "U.S.S.R.";
	public static final String U_S_A = "U.S.A.";
	
	public static final String MAIN_MAP_NAME = "Main Map";
	
	public static final String SOUTH_AMERICA = "South America";
	public static final String CENTRAL_AMERICA = "Central America";
	public static final String AFRICA = "Africa";
	public static final String MIDDLE_EAST = "Middle East";
	public static final String ASIA = "Asia";
	public static final String EUROPE = "Europe";
	
	public static final KeyStroke REALIGN_KEY = KeyStroke.getKeyStroke('R', KeyEvent.CTRL_DOWN_MASK);
	public static final KeyStroke COUP_KEY = KeyStroke.getKeyStroke('C', KeyEvent.CTRL_DOWN_MASK);
	public static final KeyStroke DECREASE_INFLUENCE_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
	public static final KeyStroke INCREASE_INFLUENCE_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
	
	
	static private Delegate delegator;

	/**
	 * @author mkiefte
	 *
	 */
	static private class Superpower extends Influence {
		String name;
		boolean own;
		
		Superpower(final String name, final boolean own) {
			this.name = name;
			this.own = own;
		}
		
		@Override
		public int getStartingInfluence() {
			return own ? 99 : 0;
		}

		@Override
		public int getStability() {
			return 99;
		}

		@Override
		public boolean hasControl() {
			return own;
		}
		@Override
		public int getAtStartInfluence() {
			return own ? 99 : 0;
		}

		@Override
		public String getLocation() {
			return name;
		}		
	}
	
	// Chains of adjacent countries on map board
	private static final String[][] CHAINS = {
		{COSTA_RICA,HONDURAS,EL_SALVADOR,GUATEMALA,MEXICO,U_S_A,CANADA,U_K,NORWAY,SWEDEN,FINLAND,U_S_S_R,POLAND, 
			E_GERMANY,W_GERMANY,FRANCE,ITALY,AUSTRIA,HUNGARY},
		{S_KOREA,JAPAN,U_S_A,CUBA,NICARAGUA,COSTA_RICA,PANAMA,COLOMBIA,ECUADOR,PERU,CHILE,ARGENTINA,PARAGUAY, 
				URUGUAY,BRAZIL,VENEZUELA,COLOMBIA},
		{GUATEMALA,HONDURAS,NICARAGUA},
		{CHINA,U_S_S_R,N_KOREA,S_KOREA,TAIWAN,JAPAN,PHILIPPINES,INDONESIA,MALAYSIA,AUSTRALIA},
		{CUBA,HAITI,DOMINICAN_REP},
		{PERU,BOLIVIA,PARAGUAY},
		{SWEDEN,DENMARK,W_GERMANY,BENELUX,U_K,FRANCE,SPAIN_PORTUGAL,ITALY},
		{FRANCE,ALGERIA,TUNISIA,LIBYA,EGYPT,SUDAN,ETHIOPIA,SOMALIA,KENYA,SE_AFRICAN_STATES,ZIMBABWE,BOTSWANA},
		{MALAYSIA,THAILAND,VIETNAM,LAOS_CAMBODIA,BURMA,INDIA,PAKISTAN},
		{HUNGARY,ROMANIA,U_S_S_R,AFGHANISTAN,IRAN,IRAQ,GULF_STATES,SAUDI_ARABIA,IRAQ},
		{SPAIN_PORTUGAL,MOROCCO},
		{YUGOSLAVIA,ITALY,GREECE,YUGOSLAVIA,HUNGARY,CZECHOSLOVAKIA,E_GERMANY,AUSTRIA,W_GERMANY},
		{NIGERIA,SAHARAN_STATES,ALGERIA,MOROCCO,WEST_AFRICAN_STATES,IVORY_COAST,NIGERIA,CAMEROON,ZAIRE,ANGOLA,BOTSWANA},
		{THAILAND,LAOS_CAMBODIA},
		{POLAND,CZECHOSLOVAKIA},
		{YUGOSLAVIA,ROMANIA,TURKEY,BULGARIA,GREECE,TURKEY,SYRIA,LEBANON,JORDAN},
		{AFGHANISTAN,PAKISTAN,IRAN},
		{ARGENTINA,URUGUAY},
		{EGYPT,ISRAEL,JORDAN,IRAQ,SAUDI_ARABIA,JORDAN},
		{ANGOLA,SOUTH_AFRICA,BOTSWANA,ZIMBABWE,ZAIRE},
		{SYRIA,ISRAEL,LEBANON,SYRIA},
	};
	
	static final Set<String> EASTERN_EUROPE = new HashSet<String>(Arrays.asList(new String[] {
			FINLAND,E_GERMANY,POLAND,CZECHOSLOVAKIA,AUSTRIA,HUNGARY,ROMANIA,YUGOSLAVIA,BULGARIA
	}));
	static final Set<String> WESTERN_EUROPE = new HashSet<String>(Arrays.asList(new String[] {
			CANADA,U_K,NORWAY,SWEDEN,FINLAND,DENMARK,BENELUX,W_GERMANY,FRANCE,AUSTRIA,SPAIN_PORTUGAL,ITALY,GREECE,TURKEY
	}));
	private static final Set<String> SOUTH_EAST_ASIA = new HashSet<String>(Arrays.asList(new String[] {
			BURMA,LAOS_CAMBODIA,THAILAND,VIETNAM,MALAYSIA,INDONESIA,PHILIPPINES
	}));
	
	// for finding neighbouring countries
	static final HashMap<String,HashSet<String>> neighbours = new HashMap<String,HashSet<String>>();
	
	// generate hash of neighbouring countries
	static {
		for (String[] chain : CHAINS) {
			// put every pair of adjacent countries into the Hash
			// first one is redundant
			for (int i = 1; i < chain.length; ++i) {
				if (!neighbours.containsKey(chain[i-1]))
					neighbours.put(chain[i-1], new HashSet<String>());
				neighbours.get(chain[i-1]).add(chain[i]);
				if (!neighbours.containsKey(chain[i]))
					neighbours.put(chain[i], new HashSet<String>());
				neighbours.get(chain[i]).add(chain[i-1]);
			}
		}
	}

	// Constructors
	public Influence() {
		this(ID, null);
	}
	
	public Influence(final String type, final GamePiece inner) {
		mySetType(type);
		setInner(inner);
	}
	
	public static abstract class Delegate {
		protected MutableProperty prop;
		protected boolean increasing;
		protected String who;
		protected CardEvent event;
		protected static MutableProperty usedAs = Utilities.getGlobalProperty(OPS_USED_AS);
	
		public Delegate(final CardEvent event) {
			this(event, OPS_REMAINING, true);
		}
		
		public Delegate(final CardEvent event, final String propertyName) {
			this(event, propertyName, true);
		}
		
		public Delegate(final CardEvent event, final String propertyName, final boolean increasing) {
			this(event, propertyName, increasing, 
					increasing ? TSPlayerRoster.getMySide() 
							: (TSPlayerRoster.US.equals(TSPlayerRoster.getMySide()) ? TSPlayerRoster.USSR : TSPlayerRoster.US));
		}
		
		public Delegate(final CardEvent event,
				final String propertyName, 
				final boolean increasing, 
				final String who) {
			this.event = event;
			this.prop = Utilities.getGlobalProperty(propertyName);
			this.increasing = increasing;
			this.who = who;
		}
		
		protected int getInfluenceAvailableFor(final Influence marker) {
			return Integer.valueOf(prop.getPropertyValue());
		}
		
		protected Command changeRemainingInfluence(final Influence marker, final int amount) {
			final int opsRemaining = Integer.valueOf(prop.getPropertyValue());
			return prop.setPropertyValue(Integer.toString(opsRemaining+amount));
		}
		
		public boolean canRealign() {
			return false;
		}
		
		public Command realign(final Influence inf) {
			Command comm = inf.realign(who);
			comm = comm.append(changeRemainingInfluence(inf, -1));
			if (usedAs.getPropertyValue().isEmpty())
				comm = comm.append(usedAs.setPropertyValue(REALIGNMENT));
			return comm;
		}
		
		public boolean canCoup() {
			return false;
		}
		
		public boolean canChangeInfluence() {
			return true;
		}
		
		public boolean canIgnoreDefconRestrictions() {
			return true;
		}
		
		protected boolean canCoupOrRealign(final Influence marker) {
			if (marker instanceof China)
				return false;
			if (marker.getSide().equals(who))
				return false;
			if (!CardEvent.getCard(Nato.class).canCoupOrRealign(marker, who))
				return false;
			if (!CardEvent.getCard(UsJapanMutualDefensePact.class).canCoupOrRealign(marker, who))
				return false;
			if (getInfluenceAvailableFor(marker.getOpponentInfluenceMarker()) <= 0)
				return false;
			if (marker.getInfluence() == 0)
				return false;
			return true;
		}
		
		public boolean canRealign(final Influence marker) {
			final String propertyValue = usedAs.getPropertyValue();
			return canRealign() 
					&& canCoupOrRealign(marker)
					&& (propertyValue.isEmpty() || REALIGNMENT.equals(propertyValue));
		}

		public boolean canCoup(final Influence marker) {
			return canCoup() 
					&& canCoupOrRealign(marker)
					&& usedAs.getPropertyValue().isEmpty()
					&& CardEvent.getCard(TheReformer.class).canCoup(marker, who);
		}

		public boolean freeCoup() {
			return false;
		}
		
		public Command coup(final Influence marker) {
			final int defcon = Integer.valueOf(Utilities.getGlobalProperty(Utilities.DEFCON).getPropertyValue());
			if (defcon == 2 
					&& marker.isBattleground 
					&& TSPlayerRoster.getMySide().equals(Utilities.getActivePlayer())
					&& (!CardEvent.getCard(NuclearSubs.class).isEventInEffect() || TSPlayerRoster.USSR.equals(who))) {
				final int n = JOptionPane.showConfirmDialog(GameModule.getGameModule().getFrame(), 
						"DEFCON is currently at 2 and you are about to\n" +
						"attempt a coup in a Battleground country.\n" +
						"Are you sure you want to attempt a coup?",
						"Coup Attempt in Battleground Country",
						JOptionPane.YES_NO_CANCEL_OPTION);
				if (n != JOptionPane.YES_OPTION)
					return null;
			} 
			final CubanMissileCrisis cmc = CardEvent.getCard(CubanMissileCrisis.class);
			if (cmc.isEventInEffect() && cmc.getTarget().equals(TSPlayerRoster.getMySide())) {
				final int n = JOptionPane.showConfirmDialog(GameModule.getGameModule().getFrame(),
						new StringBuilder(cmc.getDescription()).append(" is still in effect.\n" +
								"Are you sure you want to attempt a coup?").toString(),
						cmc.getDescription(),
						JOptionPane.YES_NO_CANCEL_OPTION);
				if (n != JOptionPane.YES_OPTION)
					return null;
			}
			Command comm = marker.coup(who, getInfluenceAvailableFor(marker), freeCoup());
			if (comm == null || comm.isNull())
				return comm;
			comm = comm.append(getDialog().finish());
			return comm;
		}
		
		public boolean canIncreaseInfluence(final Influence marker) {
			if (!canChangeInfluence())
				return false;
			final String how = Utilities.getGlobalProperty(OPS_USED_AS).getPropertyValue();
			if (REALIGNMENT.equals(how) || COUP.equals(how))
				return false;
			if (!who.equals(marker.getSide()))
				return false;
			if (increasing && getInfluenceAvailableFor(marker) <= 0)
				return false;
			if (!increasing && marker.getStartingInfluence() == marker.getInfluence())
				return false;
			return true;
		}
		
		public boolean canDecreaseInfluence(final Influence marker) {
			if (!canChangeInfluence())
				return false;
			final String how = Utilities.getGlobalProperty(OPS_USED_AS).getPropertyValue();
			if (REALIGNMENT.equals(how) || COUP.equals(how))
				return false;
			if (!who.equals(marker.getSide()))
				return false;
			if (marker.getInfluence() == 0)
				return false;
			if (increasing && marker.getStartingInfluence() >= marker.getInfluence())
				return false;
			if (!increasing && getInfluenceAvailableFor(marker) <= 0)
				return false;
			return true;
		}
		
		public Command incrementInfluence(final Influence marker, int amount) {
			Command comm = marker.addInfluence(amount);
			final int cost = increasing ? -amount : amount;
			comm = comm.append(changeRemainingInfluence(marker, cost));
			if (increasing) {
				if (amount > 0 && usedAs.getPropertyValue().isEmpty())
					comm = comm.append(usedAs.setPropertyValue(PLACE_INFLUENCE));
				else if (amount < 0 && getTotalInfluencePlaced(who) == 0)
					comm = comm.append(usedAs.setPropertyValue(""));
			} else {
				if (amount < 0 && usedAs.getPropertyValue().isEmpty())
					comm = comm.append(usedAs.setPropertyValue(REMOVE_INFLUENCE));
				else if (amount > 0 && getTotalInfluencePlaced(who) == 0)
					comm = comm.append(usedAs.setPropertyValue(""));
			}
			return comm;
		}
		
		public boolean isOptional() {
			return false;
		}
		
		public boolean isFinished() {
			final String propertyValue = usedAs.getPropertyValue();
			if (increasing && propertyValue.isEmpty())
				return false;
			final boolean placeInfluence = PLACE_INFLUENCE.equals(propertyValue);
			final GamePiece gp = Utilities.findPiece(new PieceFilter() {				
				public boolean accept(final GamePiece piece) {
					final Influence marker = getInfluenceMarker(piece);
					if (marker == null)
						return false;
					if (!increasing)
						return marker.canDecreaseInfluence();
					else if (placeInfluence)
						return marker.canIncreaseInfluence();
					else
						return marker.canRealign();
				}
			});
			if (gp == null)
				return true;
			else if (!isOptional())
				return false;
			else
				return JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(
						GameModule.getGameModule().getFrame(),
						"You have not used all available Influence points.\n"
								+ "Are you sure you are finished?",
								event.getDescription(),
								JOptionPane.YES_NO_OPTION, 
								JOptionPane.WARNING_MESSAGE);
		}
		
		public Command finish() {
			Command comm = prop.setPropertyValue("");
			comm = comm.append(Utilities.getGlobalProperty(CardEvent.CARD_PLAYED_FOR_OPS).setPropertyValue(""));
			final MutableProperty globalProperty = Utilities.getGlobalProperty(OPS_USED_AS);
			final String propertyValue = globalProperty.getPropertyValue();
			if (PLACE_INFLUENCE.equals(propertyValue) || REMOVE_INFLUENCE.equals(propertyValue))
				comm = comm.append(reportInfluenceChanges());
			if (!propertyValue.isEmpty())
				comm = comm.append(globalProperty.setPropertyValue(""));
			if (event != null)
				comm = comm.append(event.setFinished(true));
			return comm;
		}
		
		public boolean canCancel() {
			return event != null && event.canUndoEvent();
		}
		
		public Command cancel() {
			Command comm = Utilities.getGlobalProperty(OPS_USED_AS).setPropertyValue("");
			if (event != null)
				comm = comm.append(event.undoPlayEvent());
			return comm;
		}
	}
	
	public static class InfluenceDialog extends JDialog implements PropertyChangeListener {
		private static final String SHOW = "Show";
		private static final String HIDE = "Hide";
		private static final String FINISH = "Finish";
		private static final String CANCEL = "Cancel";
		private static final String[] OPTIONS = new String[] {SHOW, FINISH};
		private static final String[] CANCEL_OPTIONS = new String[] {SHOW, FINISH, CANCEL};
		private static final String[] HIDE_OPTIONS = new String[] {HIDE, FINISH};
		private static final String[] HIDE_CANCEL = new String[] {HIDE, FINISH, CANCEL};
		private static final long serialVersionUID = -1606243366679294393L;
		private static Point dialogLocation = new Point();
		private JOptionPane pane;
		private Influence dummy;
		private Influence bonusDummy;
		private boolean showingAvailable;
		private static MutableProperty condProp = Utilities.getGlobalProperty(CONDITIONAL_INFLUENCE);

		public InfluenceDialog(final Frame frame, 
				final String title, 
				final String message) {
			super(frame, title);			
			
			dummy = createDummy(new Influence() {
				@Override
				public Object getProperty(Object key) {
					if (CONTROL.equals(key)) {
						return delegator.increasing ? CONTROL_LEVEL : PRESENCE_LEVEL;
					} else
						return super.getProperty(key);
				}
			});
			
			bonusDummy = createDummy(new Influence() {
				@Override
				public Object getLocalizedProperty(final Object key) {
					if (INFLUENCE.equals(key)) {
						final String inf = (String) super.getProperty(INFLUENCE);
						if (inf.equals("0"))
							return inf;
						else
							return "+" + inf;
					} else
						return super.getLocalizedProperty(key);
				}
				public Object getProperty(final Object key) {
					if (CONTROL.equals(key)) {
						return getInfluence() == 0 ? NO_PRESENCE_LEVEL : CONTROL_LEVEL;
					} else
						return super.getProperty(key);
				}
			});

			pane = new JOptionPane(title);
			pane.setIcon(new Icon() {			
				public void paintIcon(Component c, Graphics g, int x, int y) {
					final int iconWidth = getIconWidth();
					dummy.draw(g, x+iconWidth/2, y+iconWidth/2, c, 0.5);
					bonusDummy.draw(g, x+3*iconWidth/5, y+iconWidth+1*(iconWidth-8)/3, c, 0.33);
				}
				
				public int getIconWidth() {
					return dummy.boundingBox().width/2 + 8;
				}
				
				public int getIconHeight() {
					return dummy.boundingBox().height/2 + bonusDummy.boundingBox().height/3 + 16;
				}
			});

			if (delegator.canCancel())
				pane.setOptions(CANCEL_OPTIONS);
			else
				pane.setOptions(OPTIONS);
			pane.setInitialValue(FINISH);
			
			final StringBuilder builder = new StringBuilder(message);
			if (delegator.canChangeInfluence())
				builder.append("\nUp and down arrows on Influence markers to change");
			if (delegator.canCoup() || delegator.canRealign())
				builder.append("\nRight click on opponent's Influence marker to attempt ");
			if (delegator.canCoup() && delegator.canRealign())
				builder.append("Coup or Realignment.");
			else if (delegator.canRealign())
				builder.append("Realignment.");
			else if (delegator.canCoup())
				builder.append("Coup.");
			builder.append("\nClick \"Show\" to show legal Operations."
					+ "\nClick \"Finish\" or [Enter] when finished.");
			
			pane.setMessage(builder.toString());
			
			pane.addPropertyChangeListener(this);
			setContentPane(pane);
			
			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			
			delegator.prop.addMutablePropertyChangeListener(this);
			condProp.addMutablePropertyChangeListener(this);
			Delegate.usedAs.addMutablePropertyChangeListener(this);
			dummy.influence = Integer.valueOf(delegator.prop.getPropertyValue());
			bonusDummy.influence = Integer.valueOf(condProp.getPropertyValue());
			
			setModalityType(ModalityType.MODELESS);
			setFocusableWindowState(false);
			setLocation(dialogLocation);
			
			pack();
		}

		private Influence createDummy(final Influence temp) {
			final Influence template = getInfluenceMarker(Influence.ANGOLA, delegator.who);
			Influence model = (Influence) GameModule.getGameModule().createPiece(template.getType());
			temp.mySetType(model.myGetType());
			temp.setInner(model.piece);
			model = temp;
			Map orig = template.getMap();
			template.setMap(null);
			model.setState(template.getState());
			template.setMap(orig);
			return model;
		}

		public void propertyChange(PropertyChangeEvent evt) {
			if (isVisible()) {
				if (evt.getSource() == pane) {
					Object value = pane.getValue();
					if (value == JOptionPane.UNINITIALIZED_VALUE)
						return;
					pane.setValue(JOptionPane.UNINITIALIZED_VALUE);
					if (value.equals(FINISH)) {
						if (!delegator.isFinished()) {
							JOptionPane.showMessageDialog(getDialog(), 
									"You have not used all Operations Points.\n"
									+ "Hit \"Show\" to see possible Operations.",
									"Operations not Finished",
									JOptionPane.ERROR_MESSAGE);
							return;
						}				
						final Command comm = finish();
						GameModule.getGameModule().sendAndLog(comm);
					} else if (value.equals(CANCEL)) {
						final String usedAs = Delegate.usedAs.getPropertyValue();
						if (!usedAs.isEmpty() && !usedAs.equals(PLACE_INFLUENCE) && !usedAs.equals(REMOVE_INFLUENCE))
							JOptionPane.showMessageDialog(GameModule.getGameModule().getFrame(), "You cannot undo play of Operations after Realignment or Coup attempts.", "Cannot Cancel", JOptionPane.ERROR_MESSAGE);
						else {
							final Command comm = cancel();
							GameModule.getGameModule().sendAndLog(comm);
						}
					} else if (value.equals(SHOW)) {
						String propertyValue = Delegate.usedAs.getPropertyValue();
						if (propertyValue == null || propertyValue.isEmpty()) {
							final List<String> options = new ArrayList<String>(3);
							if (delegator.canChangeInfluence())
								options.add(PLACE_INFLUENCE);
							if (delegator.canRealign())
								options.add(REALIGNMENT);
							if (delegator.canCoup())
								options.add(COUP);
							if (options.size() == 1)
								propertyValue = options.get(0);
							else {
								final int n = JOptionPane.showOptionDialog(GameModule.getGameModule().getFrame(),
										"Use this option to show where you can\n"
										+ "conduct Operations. For which type of\n"
										+ "Operations do you want to see?", 
										"Show Available Locations",
										JOptionPane.YES_NO_CANCEL_OPTION,
										JOptionPane.QUESTION_MESSAGE, 
										null, 
										options.toArray(), 
										options.get(0));
								if (n >= 0)
									propertyValue = options.get(n);
							}
						}
						if (propertyValue != null && !propertyValue.isEmpty()) {
							showHighlighters(propertyValue);
							if (delegator.canCancel())
								((JOptionPane) evt.getSource()).setOptions(HIDE_CANCEL);
							else
								((JOptionPane) evt.getSource()).setOptions(HIDE_OPTIONS);
							showingAvailable = true;
						}
					} else if (value.equals(HIDE)) {
						resetAllAvailableHighlighters();
						Map.getMapById(Influence.MAIN_MAP_NAME).getView().repaint();
						if (delegator.canCancel())
							((JOptionPane) evt.getSource()).setOptions(CANCEL_OPTIONS);
						else
							((JOptionPane) evt.getSource()).setOptions(OPTIONS);
						showingAvailable = false;
					}
				} else if (evt.getSource() == Delegate.usedAs) {
					if (evt.getNewValue().equals(REALIGNMENT))
						pane.setOptions(OPTIONS);
				} else {
					final String propertyValue = delegator.prop.getPropertyValue();
					if (propertyValue.isEmpty())
						return;
					int inf = Integer.valueOf(propertyValue);
					int bonus = Integer.valueOf(condProp.getPropertyValue());
					if (inf < 0) {
						bonus += inf;
						inf = 0;
					}
					dummy.influence = inf;
					bonusDummy.influence = bonus;
					repaint();
				}
			}
		}

		public Command finish() {
			setVisible(false);
			this.getLocation(dialogLocation);
			final Delegate temp = delegator;
			delegator = null;
			temp.prop.removeMutablePropertyChangeListener(this);
			condProp.removeMutablePropertyChangeListener(this);
			Delegate.usedAs.removeMutablePropertyChangeListener(this);
			resetAllAvailableHighlighters();
			return temp.finish().append(TSTurnTracker.updateState());
		}	
		
		public Command cancel() {
			setVisible(false);
			this.getLocation(dialogLocation);
			final Delegate temp = delegator;
			delegator = null;
			temp.prop.removeMutablePropertyChangeListener(this);
			condProp.removeMutablePropertyChangeListener(this);
			Delegate.usedAs.removeMutablePropertyChangeListener(this);
			resetAllAvailableHighlighters();
			return temp.cancel().append(TSTurnTracker.updateState());
		}
	}
	
	public static class ConductOperationsDelegate extends Delegate {
	
		private static VietnamRevolts vietnam = CardEvent.getCard(VietnamRevolts.class);
		
		public ConductOperationsDelegate(final CardEvent event) {
			super(event, OPS_REMAINING, true, TSPlayerRoster.getMySide());
		}
	
		@Override
		public boolean canIncreaseInfluence(final Influence marker) {
			if (!super.canIncreaseInfluence(marker))
				return false;
			
			final Chernobyl chernobyl = CardEvent.getCard(Chernobyl.class);
			if (TSPlayerRoster.USSR.equals(who) 
					&& chernobyl.isEventInEffect() 
					&& marker.getRegion().equals(chernobyl.getTargetRegion()))
				return false;
			
			if (marker.getInfluence() == 0) {
				boolean neighbour = false;
				for (final Influence c : marker.getNeighbours()) {
					if (c.getAtStartInfluence() > 0) {
						neighbour = true;
						break;
					}
				}
				if (!neighbour)
					return false;
			}
			
			if (marker.getOpponentInfluenceMarker().hasControl())
				return getInfluenceAvailableFor(marker) >= 2;
			else
				return true;				
		}
	
		@Override
		public Command incrementInfluence(final Influence marker, final int amount) {
			Command comm;
			if (amount > 0) {
				final int cost = marker.getOpponentInfluenceMarker().hasControl() ? 2 : 1;
				comm = marker.addInfluence(amount);
				comm = comm.append(changeRemainingInfluence(marker, -cost));
				if (vietnam.isEventInEffect() && !marker.isInSouthEastAsia())
					comm = comm.append(vietnam.setAllInSoutheastAsia(false));
				if (usedAs.getPropertyValue().isEmpty() && increasing)
					comm = comm.append(usedAs.setPropertyValue(PLACE_INFLUENCE));
			// removing influence
			} else {
				comm = marker.addInfluence(amount);
				final int cost = marker.getOpponentInfluenceMarker().hasControl() ? 2 : 1;
				comm = comm.append(changeRemainingInfluence(marker, cost));
				if (vietnam.isEventInEffect() && !marker.isInSouthEastAsia()) {
					final GamePiece piece = Utilities.findPiece(new PieceFilter() {						
						public boolean accept(final GamePiece piece) {
							final Influence marker = getInfluenceMarker(piece);
							return marker != null 
									&& marker.getInfluence() > marker.getStartingInfluence()
									&& !marker.isInSouthEastAsia();
						}
					});
					if (piece == null)
						comm = comm.append(vietnam.setAllInSoutheastAsia(true));
				}
				if (Influence.getTotalInfluencePlaced(who) == 0)
					comm = comm.append(usedAs.setPropertyValue(""));
			}
			return comm;
		}

		@Override
		protected int getInfluenceAvailableFor(final Influence marker) {
			int base = super.getInfluenceAvailableFor(marker);
			base += vietnam.getBonus(who, marker);
			return base;
		}

		@Override
		public Command realign(final Influence marker) {
			Command comm = super.realign(marker);
			final VietnamRevolts vietnam = CardEvent.getCard(VietnamRevolts.class);
			if (vietnam.isEventInEffect() && !marker.isInSouthEastAsia())
				comm = comm.append(vietnam.setAllInSoutheastAsia(false));
			return comm;
		}

		@Override
		public boolean canRealign() {
			return true;
		}

		@Override
		public boolean canCoup() {
			return true;
		}

		@Override
		public Command finish() {
			Command comm = super.finish();
			comm = comm.append(vietnam.setAllInSoutheastAsia(false));
			return comm;
		}	
		
		@Override
		protected boolean canCoupOrRealign(final Influence marker) {
			GamePiece defcon = Utilities.findPiece(new PieceFilter() {				
				public boolean accept(GamePiece piece) {
					return marker.getRegion().equals(piece.getProperty(Utilities.CONTINENT_PROP_NAME));
				}
			});
			if (defcon != null && Utilities.TRUE.equals(defcon.getProperty(Utilities.NO_COUP_OR_REALIGNMENTS_ACTIVE)))
				return false;
			else
				return super.canCoupOrRealign(marker);
		}
	}

	public Influence getOpponentInfluenceMarker() {
		return getInfluenceMarker(getLocation(), TSPlayerRoster.US.equals(getSide()) ? TSPlayerRoster.USSR : TSPlayerRoster.US);
	}
	
	public static Command resetAllAvailableHighlighters() {
		final Set<GamePiece> pieces = Utilities.findAllPiecesMatching(new PieceFilter() {		
			public boolean accept(final GamePiece piece) {
				final Influence inf = getInfluenceMarker(piece);
				return inf != null;
			}
		});
		Command comm = new NullCommand();
		for (final GamePiece gp : pieces)
			comm = comm.append(getInfluenceMarker(gp).setAvailableHighlighter(false));
		return comm;
	}
	
	public Command setAvailableHighlighter(boolean b) {
		GamePiece highlighter = getOutermost(this);
		final ChangeTracker tracker = new ChangeTracker(highlighter);
		while (!(highlighter instanceof Embellishment) 
				|| !((Embellishment) highlighter).getLayerName().equals("Available"))
			highlighter = ((Decorator) highlighter).getInner();
		Embellishment embellish = (Embellishment) highlighter;
		if (embellish.isActive() ^ b) {
			embellish.setActive(b);
			return tracker.getChangeCommand();
		} else
			return null;
	}
	
	public boolean isAvailable() {
		GamePiece highlighter = getOutermost(this);
		while (!(highlighter instanceof Embellishment) 
				|| !((Embellishment) highlighter).getLayerName().equals("Available"))
			highlighter = ((Decorator) highlighter).getInner();
		return ((Embellishment) highlighter).isActive();
	}

	protected String getID() {
		return ID;
	}

	public int getInfluence() {
		return influence;
	}
	
	public static Command reportInfluenceChanges() {
		final Set<GamePiece> pieces = Utilities.findAllPiecesMatching(new PieceFilter() {							
			public boolean accept(final GamePiece piece) {
				final Influence inf = getInfluenceMarker(piece);
				return inf != null
						&& inf.getInfluence() != inf.getStartingInfluence();
			}
		});
		
		final Chatter chatter = GameModule.getGameModule().getChatter();
		if (pieces.size() == 0) {
			final Command chat = new Chatter.DisplayText(chatter, "No changes in Influence.");
			chat.execute();
			return chat;
		}
		
		Command comm = new NullCommand();
		for (GamePiece gp : pieces) {
			final Influence inf = getInfluenceMarker(gp);
			final int i = inf.getInfluence();
			final int s = inf.getStartingInfluence();
			final StringBuilder builder = new StringBuilder(inf.getSide()).append(" Influence in ").append(inf.getLocation()).append(' ').append(i > s ? "increased" : "decreased").append(" from ").append(s).append(" to ").append(i).append('.');
			final Command chat = new Chatter.DisplayText(chatter, builder.toString());
			chat.execute();
			comm = comm.append(chat);
		}
		return comm;
	}
	
	public Command addInfluence(final int inf) {
		return setInfluence(getInfluence() + inf);
	}
	
	public Command setInfluence(int inf) {
		if (inf < 0)
			inf = 0;
		if (inf == influence)
			return null;
		ChangeTracker tracker = new ChangeTracker(this);
		influence = inf;
		
		GamePiece highlighter = getOutermost(this);
		while (!(highlighter instanceof Embellishment) 
				|| !((Embellishment) highlighter).getLayerName().equals("Highlighter"))
			highlighter = ((Decorator) highlighter).getInner();
		
		if (getInfluence() == getStartingInfluence())
			((Embellishment) highlighter).setActive(false);
		else
			((Embellishment) highlighter).setActive(true);

		Command comm = tracker.getChangeCommand();
		comm = comm.append(ScoringCard.getScoringCard(getRegion()).updateRegion(true));
		getMap().getView().repaint();
		return comm;
	}

	public Command removeAllInfluence() {
		final Command comm;
		final StringBuilder builder;
		final int inf = getInfluence();
		if (inf == 0) {
			builder = new StringBuilder("Already no ").append(getSide()).append(" Influence in ").append(getLocation()).append('.');
			comm = null;
		} else {
			comm = setInfluence(0);
			builder = new StringBuilder("All ").append(getSide()).append(" Influence (").append(inf).append(") removed from ").append(getLocation()).append('.');
		}
		final Chatter chatter = GameModule.getGameModule().getChatter();
			final Command chat = new Chatter.DisplayText(chatter, builder.toString());
		chat.execute();
		return chat.append(comm);
	}
	
	public Command takeControl() {
		final Command comm;
		final StringBuilder builder;
		final int control = getStability() + getOpponentInfluence();
		if (getInfluence() >= control) {
			builder = new StringBuilder(getSide()).append(" already controls ").append(getLocation()).append('.');
			comm = null;
		} else {
			comm = setInfluence(control);
			builder = new StringBuilder(getSide()).append(" now controls ").append(getLocation()).append('.');
		}
		final Chatter chatter = GameModule.getGameModule().getChatter();
		final Command chat = new Chatter.DisplayText(chatter, builder.toString());
		chat.execute();
		return chat.append(comm);
	}
	
	public Command adjustInfluence(final int change) {
		if (change == 0)
			return null;
		final Command comm;
		final StringBuilder builder;
		int inf = getInfluence();
		if (inf == 0 && change < 0) {
			builder = new StringBuilder(getSide()).append(" already has 0 influence in ").append(getLocation()).append('.');
			comm = null;
		} else if (change < 0) {
			inf = inf + change;
			inf = inf < 0 ? 0 : inf;
			comm = setInfluence(inf);
			builder = new StringBuilder(getSide()).append(" Influence in ").append(getLocation()).append(" reduced to ").append(inf).append('.');
		} else {
			comm = setInfluence(inf + change);
			builder = new StringBuilder(getSide()).append(" Influence in ").append(getLocation()).append(" increased to ").append(inf+change).append('.');
		}
		final Chatter chatter = GameModule.getGameModule().getChatter();
		final Command chat = new Chatter.DisplayText(chatter, builder.toString());
		chat.execute();
		return chat.append(comm);
	}
	
	// influence at the start of the round
	public int getAtStartInfluence() {
		return atStartInfluence;
	}

	// influence before changes
	public int getStartingInfluence() {
		return startingInfluence;
	}
	
	public boolean hasControl() {
		final int inf = getInfluence();
		if (inf == 0)
			return false;
		final Influence marker = getOpponentInfluenceMarker();
		if (marker == null) // only in the editor or China
			return inf >= getStability();
		final int opponentInfluence = marker.getInfluence();
		return inf >= opponentInfluence + getStability();
	}
	
	public int getStability() {
		return stability;
	}
	
	public String getRegion() {
		return region;
	}
	
	public boolean isBattleground() {
		return isBattleground;
	}
	
	public boolean isAdjacentToOpponentSuperpower() {
		final String opponentSuperpower = TSPlayerRoster.US.equals(getSide()) ? U_S_S_R : U_S_A;
		return getNeighbours(getLocation()).contains(opponentSuperpower);
	}
	
	public HelpFile getHelpFile() {
		return null;
	}

	public Rectangle boundingBox() {
		return piece.boundingBox();
	}

	public void draw(final Graphics g, final int x, final int y, final Component obs, final double zoom) {
		piece.draw(g, x, y, obs, zoom);
	}

	public String getName() {
		return piece.getName();
	}

	public Shape getShape() {
		return piece.getShape();
	}
	
	public boolean canRealign() {
		if (delegator == null)
			return false;
		return delegator.canRealign(this);
	}
	
	public boolean canCoup() {
		if (delegator == null)
			return false;
		return delegator.canCoup(this);		
	}

	@Override
	public String myGetState() {
		SequenceEncoder encoder = new SequenceEncoder(';');
		encoder.append(influence);
		encoder.append(atStartInfluence);
		encoder.append(startingInfluence);
		return encoder.getValue();
	}

	@Override
	public void mySetState(final String newState) {
		SequenceEncoder.Decoder decoder = new SequenceEncoder.Decoder(newState, ';');
		influence = decoder.nextInt(0);
		atStartInfluence = decoder.nextInt(0);
		startingInfluence = decoder.nextInt(0);
	}
	
	public String getDescription() {
		final StringBuilder builder = new StringBuilder();
		builder.append(getStability()).append(" Stability ");
		builder.append(isBattleground() ? "Battleground" : "Country");
		builder.append(" in ").append(getRegion());
		return builder.toString();
	}

	@Override
	public String myGetType() {
		final SequenceEncoder encoder = new SequenceEncoder(';');
		encoder.append(stability);
		encoder.append(region);
		encoder.append(isBattleground);
		return getID() + encoder.getValue();
	}

	public void mySetType(final String type) {
		final SequenceEncoder.Decoder decoder = new SequenceEncoder.Decoder(type, ';');
		decoder.nextToken();
		stability = decoder.nextInt(0);
		region = decoder.nextToken("nowhere");
		isBattleground = decoder.nextBoolean(false);
	}

	protected boolean canIncreaseInfluence() {
		if (delegator == null)
			return false;
		else
			return delegator.canIncreaseInfluence(this);
	}
	
	protected boolean canDecreaseInfluence() {
		if (delegator == null)
			return false;
		else
			return delegator.canDecreaseInfluence(this);
	}
	
	public static int getStartingInfluenceRemaining(final String who) {
		final String propName = TSPlayerRoster.US.equals(who) ? AMERICAN_STARTING_INFLUENCE : SOVIET_STARTING_INFLUENCE;
		final MutableProperty prop = Utilities.getGlobalProperty(propName);
		final String propertyValue = prop.getPropertyValue();
		if (propertyValue.isEmpty())
			return -1;
		else
			return Integer.valueOf(propertyValue);
	}
	
	public static int getStartingInfluencePlaced(final String who) {
		final int maxValue = TSPlayerRoster.US.equals(who) ? TOTAL_AMERICAN_STARTING_INFLUENCE : TOTAL_SOVIET_STARTING_INFLUENCE;
		return maxValue - Math.max(getStartingInfluenceRemaining(who), 0);
	}
	
	public static int getExtraInfluenceRemaining() {
		final String propertyValue = Utilities.getGlobalProperty(BONUS_INFLUENCE).getPropertyValue();
		if (propertyValue.isEmpty())
			return -1;
		else
			return Integer.valueOf(propertyValue);
	}
	
	public static int getExtraInfluencePlaced() {
		final String who = Utilities.getGlobalProperty(WHO_RECEIVES_BONUS_INFLUENCE).getPropertyValue();
		final Set<GamePiece> pieces = Utilities.findAllPiecesMatching(new PieceFilter() {			
			public boolean accept(GamePiece piece) {
				final Influence inf = getInfluenceMarker(piece);
				return inf != null 
						&& inf.getSide().equals(who) 
						&& inf.getInfluence() > inf.getStartingInfluence();
			}
		});
		int total = 0;
		for (GamePiece gp : pieces) {
			final Influence inf = getInfluenceMarker(gp);
			total += inf.getInfluence() - inf.getStartingInfluence();
		}
		return total - getStartingInfluencePlaced(who);
	}
	
	@Override
	protected KeyCommand[] myGetKeyCommands() {
		final KeyCommand[] commands = new KeyCommand[4];
		final GamePiece outermost = getOutermost(this);
		commands[0] = new KeyCommand(INCREASE_INFLUENCE, INCREASE_INFLUENCE_KEY, outermost, canIncreaseInfluence());
		commands[1] = new KeyCommand(DECREASE_INFLUENCE, DECREASE_INFLUENCE_KEY, outermost, canDecreaseInfluence());
		if (canRealign())
			commands[2] = new KeyCommand(REALIGN,REALIGN_KEY, outermost, true);
		else
			commands[2] = new KeyCommand(CANNOT_REALIGN,REALIGN_KEY, outermost, false);
		if (canCoup())
			commands[3] = new KeyCommand(COUP,COUP_KEY, outermost, true);
		else
			commands[3] = new KeyCommand(CANNOT_COUP,COUP_KEY, outermost, false);
		return commands;
	}

	private Command incrementInfluence(final int amount) {
		String propertyValue = Delegate.usedAs.getPropertyValue();			
		Command comm = delegator.incrementInfluence(this, amount);
		if (propertyValue == null || propertyValue.isEmpty())
			propertyValue = Delegate.usedAs.getPropertyValue();
		if (dialog.showingAvailable && propertyValue != null && !propertyValue.isEmpty())
			showHighlighters(propertyValue);
		return comm;
	}
	
	@Override
	public Command myKeyEvent(KeyStroke stroke) {
		if (stroke.equals(INCREASE_INFLUENCE_KEY) && canIncreaseInfluence())
			return incrementInfluence(+1);
		else if (stroke.equals(DECREASE_INFLUENCE_KEY) && canDecreaseInfluence())
			return incrementInfluence(-1);
		else if (stroke.equals(Influence.REALIGN_KEY) && canRealign())
			return delegator.realign(this);
		else if (stroke.equals(Influence.COUP_KEY) && canCoup())
			return delegator.coup(this);
		return null;
	}

	public int getRealignmentModifier(final StringBuilder adjacent, final StringBuilder in) {
		int modifier = 0;
		final Set<String> neigh = new HashSet<String>();
		for (final Influence marker : getNeighbours())
			if (marker.hasControl()) {
				++modifier;
				neigh.add(marker.getLocation());
			}
		if (neigh.size() == 0)
			Utilities.listAsString(adjacent.append(getSide()).append(" does not control any of"),
					getNeighbours(getLocation()), "or");
		else
			Utilities.listAsString(adjacent.append(getSide()).append(" controls"), neigh, "and");
		
		if (getInfluence() > getOpponentInfluence()) {
			++modifier;
			in.append(getSide()).append(" has more influence in ").append(getLocation()).append('.');
		}
		return modifier;
	}
	
	public Command realign(final String who) {
		final Chatter chatter = GameModule.getGameModule().getChatter();
		StringBuilder builder = new StringBuilder("* ").append(who).append(" attempts Realignment Roll in ").append(getLocation()).append('.');
		Command chat = new Chatter.DisplayText(chatter, builder.toString());
		final Influence amInf;
		final Influence suInf;
		if (TSPlayerRoster.US.equals(getSide())) {
			amInf = this;
			suInf = getOpponentInfluenceMarker();
		} else {
			amInf = getOpponentInfluenceMarker();
			suInf = this;
		}
		StringBuilder adjacent = new StringBuilder("");
		StringBuilder in = new StringBuilder("");
		int amModifier = amInf.getRealignmentModifier(adjacent, in);
		chat = chat.append(new Chatter.DisplayText(chatter, adjacent.toString()));
		if (in.length() > 0)
			chat = chat.append(new Chatter.DisplayText(chatter, in.toString()));		
		if (CardEvent.getCard(IranContraScandal.class).isEventInEffect()) {
			chat = chat.append(new Chatter.DisplayText(chatter, "Iran-Contra Scandal is in effect (-1)."));
			--amModifier;
		}
		builder = new StringBuilder("Modifier to US roll: ");
		if (amModifier >= 0)
			builder.append('+');
		builder.append(amModifier);
		chat = chat.append(new Chatter.DisplayText(chatter, builder.toString()));
		adjacent = new StringBuilder("");
		in = new StringBuilder("");		
		final int suModifier = suInf.getRealignmentModifier(adjacent, in);
		chat = chat.append(new Chatter.DisplayText(chatter, adjacent.toString()));
		if (in.length() > 0)
			chat = chat.append(new Chatter.DisplayText(chatter, in.toString()));		
		builder = new StringBuilder("Modifier to USSR roll: ");
		if (suModifier >= 0)
			builder.append('+');
		builder.append(suModifier);
		chat = chat.append(new Chatter.DisplayText(chatter, builder.toString()));
		chat.execute();
		Command comm = chat;
		final int[] result = {0,0};
		comm = comm.append(Utilities.rollDie(new String[] {TSPlayerRoster.US, TSPlayerRoster.USSR}, result));
		result[0] += amModifier;
		result[1] += suModifier;
		final int difference = Math.abs(result[0] - result[1]);
		if (difference == 0) {
			chat = new Chatter.DisplayText(chatter, "Draw: No Influence is removed.");
			chat.execute();
			comm = comm.append(chat);
		} else {
			if (result[0] > result[1])
				comm = comm.append(suInf.adjustInfluence(result[1]-result[0]));
			else
				comm = comm.append(amInf.adjustInfluence(result[0]-result[1]));
		}
		return comm;
	}
	
	public Command coup(final String who, final int ops, final boolean free) {
		final Chatter chatter = GameModule.getGameModule().getChatter();
		Command comm;
		final CubanMissileCrisis cmc = CardEvent.getCard(CubanMissileCrisis.class);
		if (cmc.isEventInEffect() && cmc.getTarget().equals(who)) {
			Command chat = new Chatter.DisplayText(chatter, new StringBuilder(cmc.getDescription()).append(" is still in effect.").toString());
			chat.execute();
			comm = chat;
			comm = comm.append(cmc.setGameOver(true));
			return comm;
		} 
		
		final YuriAndSamantha yuri = CardEvent.getCard(YuriAndSamantha.class);
		if (yuri.isEventInEffect() && TSPlayerRoster.US.equals(who)) {
			Command chat = new Chatter.DisplayText(chatter, new StringBuilder(yuri.getDescription()).append(" is still in effect.").toString());
			chat.execute();
			comm = chat;
			comm = comm.append(Utilities.adjustVps(-1));
			if (TSTurnTracker.isGameOver())
				return comm;
		} else
			comm = new NullCommand();
		
		StringBuilder builder = new StringBuilder("* ").append(who).append(" attempts Coup in ").append(getLocation()).append(" with ").append(ops).append(" Op");
		if (ops > 1)
			builder.append("s.");
		else
			builder.append(".");
		Command chat = new Chatter.DisplayText(chatter, builder.toString());
		final SaltNegotiations saltNegotiations = CardEvent.getCard(SaltNegotiations.class);
		int mod = ops;
		if (saltNegotiations.isEventInEffect()) {
			builder = new StringBuilder(saltNegotiations.getDescription()).append(": -1 die-roll modifier to all coups.");
			chat = chat.append(new Chatter.DisplayText(chatter, builder.toString()));
			--mod;
		}
		final LatinAmericanDeathSquads lads = CardEvent.getCard(LatinAmericanDeathSquads.class);
		if (lads.isEventInEffect() 
				&& (Influence.CENTRAL_AMERICA.equals(getRegion()) || SOUTH_AMERICA.equals(getRegion()))) {
			final int modifier = lads.getModifierFor(who);
			builder = new StringBuilder(lads.getDescription()).append(": ");
			if (modifier > 0)
				builder.append('+');
			builder.append(modifier).append(" die-roll modifier.");
			chat = chat.append(new Chatter.DisplayText(chatter, builder.toString()));
			mod += modifier;
		}
		chat.execute();
		comm = comm.append(chat);
		if (isBattleground()) { 
			final NuclearSubs nuclearSubs = CardEvent.getCard(NuclearSubs.class);
			if (!nuclearSubs.isEventInEffect() || TSPlayerRoster.USSR.equals(who))
				comm = comm.append(Utilities.adjustDefcon(-1));
			else {
				chat = new Chatter.DisplayText(chatter, new StringBuilder("DEFCON unaffected by coups because of ").append(nuclearSubs.getDescription()).toString());
				chat.execute();
				comm = comm.append(chat);
			}
		}
		if (TSTurnTracker.isGameOver())
			return comm;
		final int stability = getStability();
		final int doubleStability = 2*stability;
		final int[] result = {0};
		comm = comm.append(Utilities.rollDie(who, result));
		builder = new StringBuilder("").append(result[0]).append(" + ").append(ops).append(" Op");
		if (ops > 1)
			builder.append("s ");
		else
			builder.append(' ');
		if (mod < ops)
			builder.append("- ").append(ops-mod);
		else if (mod > ops)
			builder.append("+ ").append(mod-ops);
		builder.append(" - 2*").append(stability).append(" Stability = ").append(result[0]+mod-doubleStability);
		chat = new Chatter.DisplayText(chatter, builder.toString());
		chat.execute();
		comm = comm.append(chat);
		result[0] += mod;
		if (result[0] <= doubleStability) {
			chat = new Chatter.DisplayText(chatter, "Coup Attempt fails.");
			chat.execute();
			comm = comm.append(chat);
		} else {
			chat = new Chatter.DisplayText(chatter, "Coup Attempt is successful.");
			chat.execute();
			comm = comm.append(chat);
			int difference = result[0] - doubleStability;
			Influence opInf, myInf;
			if (who.equals(getSide())) {
				myInf = this;
				opInf = getOpponentInfluenceMarker();
			} else {
				myInf = getOpponentInfluenceMarker();
				opInf = this;
			}
			final int total = opInf.getInfluence();
			if (difference > total) {
				final int remainder = difference - total;
				comm = comm.append(opInf.removeAllInfluence());
				comm = comm.append(myInf.adjustInfluence(remainder));
			} else
				comm = comm.append(opInf.adjustInfluence(-difference));
		}
		if (!free)
			comm = comm.append(Utilities.adjustMilitaryOps(ops, who));
		return comm;
	}
	
	@Override
	public Object getProperty(Object key) {
		if (INFLUENCE.equals(key)) {
			return Integer.toString(influence);
		} else if (CONTROL.equals(key)) {
			if (getInfluence() == 0)
				return NO_PRESENCE_LEVEL;
			return hasControl() ? CONTROL_LEVEL : PRESENCE_LEVEL;
		} else
			return super.getProperty(key);
	}


	@Override
	public Object getLocalizedProperty(Object key) {
		if (INFLUENCE.equals(key)) {
			return Integer.toString(influence);
		} else if (CONTROL.equals(key)) {
			if (getInfluence() == 0)
				return NO_PRESENCE_LEVEL;
			return hasControl() ? CONTROL_LEVEL : PRESENCE_LEVEL;
		} else
			return super.getLocalizedProperty(key);
	}

	public static Influence getInfluenceMarker(GamePiece gp) {
		gp = getOutermost(gp);
		while (!(gp instanceof Influence) 
				&& gp instanceof Decorator 
				&& ((Decorator) gp).getInner() instanceof Decorator)
			gp = ((Decorator) gp).getInner();
		if (gp instanceof Influence)
			return (Influence) gp;
		else
			return null;
	}
	
	// get a marker associated with a nation's influence in a given country
	public static Influence getInfluenceMarker(final String at, final String side) {
		if (Influence.U_S_A.equals(at))
			return new Superpower(Influence.U_S_A, TSPlayerRoster.US.equals(side));
		if (Influence.U_S_S_R.equals(at))
			return new Superpower(Influence.U_S_S_R, TSPlayerRoster.USSR.equals(side));
		final GamePiece piece = Utilities.findPiece(new PieceFilter() {		
			public boolean accept(final GamePiece piece) {
				final Influence inf = getInfluenceMarker(piece);
				return inf != null && inf.getLocation().equals(at) && inf.getSide().equals(side);
			}
		});
		if (piece == null)
			throw new NullPointerException("at = " + at + "; side = " + side);
		return getInfluenceMarker(piece);
	}
	
	protected int getOpponentInfluence() {
		final Influence opponent = getOpponentInfluenceMarker();
		if (opponent == null)
			return 0;
		else
			return opponent.getInfluence();
	}

	public String getLocation() {
		if (location == null)
			location = (String) getOutermost(this).getProperty(BasicPiece.LOCATION_NAME);
		return location;
	}

	public String getSide() {
		if (side == null)
			side = (String) getOutermost(this).getProperty(SIDE);
		return side;
	}

	public static Command setAllStartingInfluence() {
		Command comm = new NullCommand();
		final Set<GamePiece> pieces = Utilities.findAllPiecesMatching(new PieceFilter() {			
			public boolean accept(final GamePiece piece) {
				final Influence inf = getInfluenceMarker(piece);
				if (inf == null)
					return false;
				return inf.getInfluence() != inf.getStartingInfluence();
			}
		});
		for (GamePiece gp : pieces) {
			final Influence inf = (Influence) gp;
			final ChangeTracker tracker = new ChangeTracker(inf);
			inf.startingInfluence = inf.getInfluence();
			GamePiece highlighter = gp;
			while (!(highlighter instanceof Embellishment) 
					|| !((Embellishment) highlighter).getLayerName().equals(HIGHLIGHTER))
				highlighter = ((Decorator) highlighter).getInner();
			((Embellishment) highlighter).setActive(false);
			comm = comm.append(tracker.getChangeCommand());
		}	
		return comm;
	}

	public static Command setAllAtStartInfluence() {
		Command comm = new NullCommand();
		final Set<GamePiece> pieces = Utilities.findAllPiecesMatching(new PieceFilter() {			
			public boolean accept(final GamePiece piece) {
				final Influence inf = getInfluenceMarker(piece);
				if (inf == null)
					return false;
				final int influence = inf.getInfluence();
				return influence != inf.getAtStartInfluence()
						|| influence != inf.getStartingInfluence();
			}
		});
		for (GamePiece gp : pieces) {
			final Influence marker = (Influence) gp;
			int inf = marker.getInfluence();
			final ChangeTracker tracker = new ChangeTracker(marker);
			marker.startingInfluence = inf;
			marker.atStartInfluence = inf;
			GamePiece highlighter = gp;
			while (!(highlighter instanceof Embellishment) 
					|| !((Embellishment) highlighter).getLayerName().equals(HIGHLIGHTER))
				highlighter = ((Decorator) highlighter).getInner();
			((Embellishment) highlighter).setActive(false);
			comm = comm.append(tracker.getChangeCommand());
		}	
		return comm;
	}

	public static Command revertToStartingInfluence() {
		Command comm = new NullCommand();
		final Set<GamePiece> pieces = Utilities.findAllPiecesMatching(new PieceFilter() {			
			public boolean accept(final GamePiece piece) {
				final Influence inf = getInfluenceMarker(piece);
				if (getInfluenceMarker(piece) == null)
					return false;
				return inf.getInfluence() != inf.getStartingInfluence();
			}
		});
		for (GamePiece gp : pieces) {
			final Influence inf = (Influence) gp;
			comm = comm.append(inf.setInfluence(inf.getStartingInfluence()));
		}
		return comm;		
	}
	
	public static Command revertToAtStartInfluence() {
		Command comm = new NullCommand();
		final Set<GamePiece> pieces = Utilities.findAllPiecesMatching(new PieceFilter() {			
			public boolean accept(final GamePiece piece) {
				final Influence inf = getInfluenceMarker(piece);
				if (inf == null)
					return false;
				final int influence = inf.getInfluence();
				return influence != inf.getAtStartInfluence()
						|| influence != inf.getStartingInfluence();
			}
		});
		for (GamePiece gp : pieces) {
			final Influence inf = (Influence) gp;
			final ChangeTracker tracker = new ChangeTracker(gp);
			inf.startingInfluence = inf.atStartInfluence;
			inf.setInfluence(inf.atStartInfluence);
			comm = comm.append(tracker.getChangeCommand());
		}
		return comm;
	}

	final public Set<Influence> getNeighbours() {
		final Set<String> neighbours = getNeighbours(getLocation());
		final Set<Influence> myNeighbours = new HashSet<Influence>(neighbours.size());
		for (final String c : neighbours)
			myNeighbours.add(getInfluenceMarker(c, getSide()));
		return myNeighbours;
	}
	
	public static Set<String> getNeighbours(final String c) {
		return neighbours.get(c);
	}

	public static boolean areNeighbours(final String c1, final String c2) {
		return neighbours.get(c1).contains(c2);
	}

	public static Set<String> getCountries() {
		return neighbours.keySet();
	}
	
	public static int getTotalInfluencePlaced(final String who) {
		final Set<GamePiece> markers = Utilities.findAllPiecesMatching(new PieceFilter() {			
			public boolean accept(GamePiece piece) {
				final Influence inf = getInfluenceMarker(piece);
				return inf != null 
						&& inf.getSide().equals(who)
						&& inf.getInfluence() > inf.getStartingInfluence();
			}
		});
		int total = 0;
		for (final GamePiece marker : markers) {
			final Influence inf = getInfluenceMarker(marker);
			total += inf.getInfluence() - inf.getStartingInfluence();
		}
		return total;
	}
	
	public boolean isInEasternEurope() {
		return EASTERN_EUROPE.contains(getLocation());
	}
	
	public boolean isInWesternEurope() {
		return WESTERN_EUROPE.contains(getLocation());
	}
	
	public boolean isInSouthEastAsia() {
		return SOUTH_EAST_ASIA.contains(getLocation());
	}
	
	public static boolean isOpsRemaining() {
		return !Utilities.getGlobalProperty(OPS_REMAINING).getPropertyValue().isEmpty();
	}

	public static JDialog getInfluenceDialog(final String title, final Delegate delegator) {
		return getInfluenceDialog(title, null, delegator);
	}
	
	public static JDialog getInfluenceDialog(final String title, 
			String message,
			final Delegate delegator) {
		if (message == null)
			message = "";
		disposeInluenceDialog();
		final JFrame frame = GameModule.getGameModule().getFrame();
		Influence.delegator = delegator;
		dialog = new InfluenceDialog(frame, title, message);
		dialog.setVisible(true);
		return dialog;
	}
	
	public static void disposeInluenceDialog() {
		if (dialog == null)
			return;
		dialog.dispose();
		dialog = null;	
	}
	
	public static InfluenceDialog getDialog() {
		return dialog;
	}

	public static void showHighlighters(final String propertyValue) {
		final boolean isPlaceInfluence = PLACE_INFLUENCE.equals(propertyValue) || REMOVE_INFLUENCE.equals(propertyValue);
		final boolean increase = delegator.increasing;
		final boolean isRealignment = REALIGNMENT.equals(propertyValue);
		final boolean isCoup = COUP.equals(propertyValue);
		final Set<GamePiece> pieces = Utilities.findAllPiecesMatching(new PieceFilter() {							
			public boolean accept(final GamePiece piece) {
				final Influence marker = getInfluenceMarker(piece);
				if (marker == null)
					return false;
				if (isPlaceInfluence && increase)
					return marker.canIncreaseInfluence();
				if (isPlaceInfluence && !increase)
					return marker.canDecreaseInfluence();
				if (isRealignment)
					return marker.canRealign();
				if (isCoup)
					return marker.canCoup();
				return false;
			}
		});
		resetAllAvailableHighlighters();
		for (final GamePiece gp : pieces)
			getInfluenceMarker(gp).setAvailableHighlighter(true);
		Map.getMapById(MAIN_MAP_NAME).getView().repaint();
	}
}
