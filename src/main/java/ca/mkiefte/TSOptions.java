package ca.mkiefte;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.Box;
import javax.swing.SwingUtilities;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.Configurable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.GameComponent;
import VASSAL.build.module.GameSetupStep;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.map.DrawPile;
import VASSAL.build.module.properties.MutableProperty;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.configure.BooleanConfigurer;
import VASSAL.configure.StringEnumConfigurer;
import VASSAL.counters.Deck;
import VASSAL.counters.Decorator;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceFilter;
import ca.mkiefte.cards.BearTrap;
import ca.mkiefte.cards.CampDavidAccords;
import ca.mkiefte.cards.CardEvent;
import ca.mkiefte.cards.DeGaulleLeadsFrance;
import ca.mkiefte.cards.FlowerPower;
import ca.mkiefte.cards.JohnPaulIIElectedPope;
import ca.mkiefte.cards.MarshallPlan;
import ca.mkiefte.cards.Nato;
import ca.mkiefte.cards.PanamaCanalReturned;
import ca.mkiefte.cards.UsJapanMutualDefensePact;
import ca.mkiefte.cards.WarsawPactFormed;
import ca.mkiefte.cards.WillyBrandt;

public class TSOptions extends AbstractConfigurable implements GameComponent, GameSetupStep {

	private static final String FINISHED_SETUP = "FinishedSetup";
	private Box box;
	private StringEnumConfigurer whoConfig;
	private StringEnumConfigurer howMuchConfig;
	private BooleanConfigurer chineseRevolutionConfig;
	private BooleanConfigurer optionalCardsConfig;
	private BooleanConfigurer lateWarConfig;
	
	@SuppressWarnings("rawtypes")
	private static Set<Class> EVENTS_IN_EFFECT = new HashSet<Class>(Arrays.asList(new Class[] {
			UsJapanMutualDefensePact.class,
			MarshallPlan.class,
			Nato.class,
			WarsawPactFormed.class,
			DeGaulleLeadsFrance.class,
			FlowerPower.class,
			WillyBrandt.class,
	}));
	
	@SuppressWarnings("rawtypes")
	private static Set<Class> EVENTS_STILL_IN_DECK = new HashSet<Class>(Arrays.asList(new Class[] {
		BearTrap.class,
		CampDavidAccords.class,
		JohnPaulIIElectedPope.class,
		PanamaCanalReturned.class
	}));
	
	private static class INF {
		int americanInfluence;
		int sovietInfluence;
		INF(int us, int ussr) {
			americanInfluence = us;
			sovietInfluence = ussr;
		}
		@Override
		public int hashCode() {
			return Integer.valueOf(americanInfluence+sovietInfluence<<3).hashCode();
		}		
	}
	private static int CONTROL = 99;
	
	private static Map<String,INF> START = new HashMap<String,INF>();
	
	static {
		START.put(Influence.U_K, new INF(CONTROL,0));			START.put(Influence.ITALY, new INF(CONTROL, 0));		START.put(Influence.BENELUX, new INF(CONTROL, 0));	
		START.put(Influence.DENMARK, new INF(CONTROL,0));		START.put(Influence.NORWAY, new INF(CONTROL, 0));		START.put(Influence.W_GERMANY, new INF(5, 1));
		START.put(Influence.ISRAEL, new INF(CONTROL, 0));		START.put(Influence.IRAN, new INF(CONTROL, 0));		START.put(Influence.PAKISTAN, new INF(CONTROL, 0));	START.put(Influence.TURKEY, new INF(CONTROL, 0));
		START.put(Influence.ZAIRE, new INF(CONTROL, 0));		START.put(Influence.SOMALIA, new INF(CONTROL, 0));	START.put(Influence.KENYA, new INF(CONTROL, 0));		START.put(Influence.NIGERIA, new INF(CONTROL, 0));
		START.put(Influence.JAPAN, new INF(CONTROL, 0));		START.put(Influence.S_KOREA, new INF(CONTROL, 0));	START.put(Influence.TAIWAN, new INF(CONTROL, 0));		START.put(Influence.PHILIPPINES, new INF(3, 1));
		START.put(Influence.THAILAND, new INF(CONTROL, 0));	START.put(Influence.INDONESIA, new INF(CONTROL, 0));	START.put(Influence.AUSTRALIA, new INF(CONTROL, 0));	START.put(Influence.MALAYSIA, new INF(3, 1));
		START.put(Influence.NICARAGUA, new INF(CONTROL, 0));	START.put(Influence.PANAMA, new INF(CONTROL, 0));		START.put(Influence.HAITI, new INF(CONTROL, 0));		START.put(Influence.HONDURAS, new INF(CONTROL, 0));
		START.put(Influence.VENEZUELA, new INF(CONTROL, 0));	START.put(Influence.CHILE, new INF(CONTROL, 0));		START.put(Influence.ARGENTINA, new INF(CONTROL, 0));	START.put(Influence.COLOMBIA, new INF(2, 1));
		START.put(Influence.DOMINICAN_REP, new INF(CONTROL, 0));
		START.put(Influence.E_GERMANY, new INF(0, CONTROL));	START.put(Influence.POLAND, new INF(0, CONTROL));		START.put(Influence.HUNGARY, new INF(0, CONTROL));	START.put(Influence.CZECHOSLOVAKIA, new INF(0, CONTROL));
		START.put(Influence.BULGARIA, new INF(0, CONTROL));	START.put(Influence.CUBA, new INF(0, CONTROL));		START.put(Influence.N_KOREA, new INF(0, CONTROL));
		START.put(Influence.IRAQ, new INF(0, CONTROL));		START.put(Influence.SYRIA, new INF(0, 3));			START.put(Influence.INDIA, new INF(0, CONTROL));		START.put(Influence.AFGHANISTAN, new INF(0, CONTROL));
		START.put(Influence.LIBYA, new INF(0, CONTROL));		START.put(Influence.ALGERIA, new INF(0, CONTROL));	START.put(Influence.ETHIOPIA, new INF(0, CONTROL));	START.put(Influence.ZIMBABWE, new INF(0, CONTROL));
		START.put(Influence.ANGOLA, new INF(1, CONTROL));		START.put(Influence.LAOS_CAMBODIA, new INF(0, 2));	START.put(Influence.VIETNAM, new INF(0, 5));
		START.put(Influence.SE_AFRICAN_STATES, new INF(0, 2));START.put(Influence.FRANCE, new INF(3, 1));			START.put(Influence.ROMANIA, new INF(1, 3));
		START.put(Influence.SPAIN_PORTUGAL, new INF(1, 0));	START.put(Influence.EGYPT, new INF(1, 0));			START.put(Influence.SOUTH_AFRICA, new INF(2, 1));
		START.put(Influence.JORDAN, new INF(2, 2));			START.put(Influence.BURMA, new INF(0, 1));			START.put(Influence.PERU, new INF(2, 1));
		START.put(Influence.FINLAND, new INF(1, 2));
		START.put(Influence.YUGOSLAVIA, new INF(1, 2));
		START.put(Influence.SAUDI_ARABIA, new INF(2, 0));
		START.put(Influence.CANADA, new INF(2, 0));
	}
	
	
	
	public TSOptions() {
	}

	public void addTo(Buildable parent) {
	    GameModule.getGameModule().getGameState().addGameComponent(this);
	    GameModule.getGameModule().getGameState().addGameSetupStep(this);
	}

	public boolean isFinished() {
		final String who = Utilities.getGlobalProperty(Influence.WHO_RECEIVES_BONUS_INFLUENCE).getPropertyValue();
		return who != null && !who.isEmpty();
	}

	public String getStepTitle() {
		return "Bonus Influence";
	}

	public Component getControls() {
		if (box == null) {
			box = Box.createVerticalBox();
			box.add(Box.createVerticalGlue());
		    whoConfig = new StringEnumConfigurer(null,
		    	      "Who receives extra influnce:",
		    	      new String[] {TSPlayerRoster.USSR, TSPlayerRoster.US});
		    whoConfig.setValue(TSPlayerRoster.US);
			box.add(whoConfig.getControls());
			howMuchConfig = new StringEnumConfigurer(null,
					"How much extra influence:",
					new String[] {"0", "1", "2", "3", "4"});
			howMuchConfig.setValue("2");
			box.add(howMuchConfig.getControls());
			chineseRevolutionConfig = new BooleanConfigurer(null, "Chinese Civil War", false);
			box.add(chineseRevolutionConfig.getControls());
			optionalCardsConfig = new BooleanConfigurer(null, "Optional Cards", true);
			box.add(optionalCardsConfig.getControls());
			lateWarConfig = new BooleanConfigurer(null, "Late-War Scenario", false);
			lateWarConfig.addPropertyChangeListener(new PropertyChangeListener() {				
				public void propertyChange(final PropertyChangeEvent event) {
					final Boolean value = (Boolean) event.getNewValue();
					chineseRevolutionConfig.getControls().setEnabled(!value);
					whoConfig.setEnabled(!value);
					howMuchConfig.setEnabled(!value);
				}
			});
			box.add(lateWarConfig.getControls());
			box.add(Box.createVerticalGlue());
		}
		return box;
	}

	public void finish() {
		Command comm = Utilities.getGlobalProperty(Influence.WHO_RECEIVES_BONUS_INFLUENCE).setPropertyValue(whoConfig.getValueString());
		final MutableProperty globalProperty = Utilities.getGlobalProperty(Influence.BONUS_INFLUENCE);
		final String valueString = howMuchConfig.getValueString();
		if (Integer.valueOf(valueString) == 0 || lateWarConfig.getValueBoolean())
			comm = comm.append(globalProperty.setPropertyValue(""));
		else
			comm = comm.append(globalProperty.setPropertyValue(valueString));
		GameModule.getGameModule().getServer().sendToOthers(comm);
	}

	public void setup(final boolean gameStarting) {
		if (gameStarting) {
			SwingUtilities.invokeLater(new Runnable() {				
				public void run() {
					final MutableProperty finishedSetup = Utilities.getGlobalProperty(FINISHED_SETUP);
					if (Utilities.FALSE.equals(finishedSetup.getPropertyValue())) {
						Command comm = finishedSetup.setPropertyValue(Utilities.TRUE);
						final Chatter chatter = GameModule.getGameModule().getChatter();
						final String who = Utilities.getGlobalProperty(Influence.WHO_RECEIVES_BONUS_INFLUENCE).getPropertyValue();
						final String propertyValue = Utilities.getGlobalProperty(Influence.BONUS_INFLUENCE).getPropertyValue();
						if (!propertyValue.isEmpty()) {
							final int howMuch = Integer.valueOf(propertyValue);
							final StringBuilder message = new StringBuilder(who).append(" will receive ").append(howMuch).append(" extra influence after all other setup.");
							comm = new Chatter.DisplayText(chatter, message.toString());
							comm.execute();
						}
						if (!chineseRevolutionConfig.getValueBoolean() || isLateWarScenario()) {
							final Influence china = Influence.getInfluenceMarker(Influence.CHINA, TSPlayerRoster.USSR);
							final ChangeTracker tracker = new ChangeTracker(china);
							china.atStartInfluence = china.startingInfluence = china.getStability();
							comm = comm.append(tracker.getChangeCommand());
							comm = comm.append(china.setInfluence(china.getStability()));
						}
						if (optionalCardsConfig.getValueBoolean()) {
							Deck deck = CardEvent.getDeckNamed(CardEvent.REMOVED_FROM_PLAY_DECK_NAME);
							Iterator<GamePiece> iter = deck.getPiecesIterator();
							final DrawPile earlyWarDeck = DrawPile.findDrawPile(CardEvent.DRAW_DECK);
							final DrawPile midWarDeck = DrawPile.findDrawPile(CardEvent.MID_WAR_CARDS);
							final DrawPile lateWarDeck = DrawPile.findDrawPile(CardEvent.LATE_WAR_CARDS);
							while (iter.hasNext()) {
								final CardEvent event = CardEvent.getCard(iter.next());
								switch (event.getWar()) {
								case 0:
									comm = comm.append(earlyWarDeck.addToContents(Decorator.getOutermost(event)));
									break;
								case 1:
									comm = comm.append(midWarDeck.addToContents(Decorator.getOutermost(event)));
									break;
								case 2:
									comm = comm.append(lateWarDeck.addToContents(Decorator.getOutermost(event)));
									break;
								}
							}
						}
						
						if (isLateWarScenario())
							comm = comm.append(setupLateWar());
						final Set<GamePiece> cards = Utilities.findAllPiecesMatching(new PieceFilter() {					
							public boolean accept(final GamePiece piece) {
								return CardEvent.getCard(piece) != null;
							}
						});
						for (final GamePiece gp : cards)
							comm = comm.append(CardEvent.getCard(gp).setCurrentPosition());
						Influence.resetAllAvailableHighlighters();
						GameModule.getGameModule().sendAndLog(comm);
					}
				}
			});
		}
	}

	public static boolean isLateWarScenario() {
		final BooleanConfigurer lateWar = getInstance().lateWarConfig;
		return lateWar != null && lateWar.getValueBoolean();
	}
	
	@SuppressWarnings("unchecked")
	protected Command setupLateWar() {
		synchronized(TSTurnTracker.getInstance()) {
			Command comm = TSTurnTracker.getInstance().setActionRound(8, 0, null);
			comm = comm.append(Utilities.setDefcon(4));
			comm = comm.append(Utilities.setSpaceRace(TSPlayerRoster.USSR, 4));
			comm = comm.append(Utilities.setSpaceRace(TSPlayerRoster.US, 6));
			comm = comm.append(Utilities.setVps(-4));
			for (@SuppressWarnings("rawtypes") Class cl : EVENTS_IN_EFFECT) {
				comm = comm.append(CardEvent.getCard(cl).setEventInEffect(true));
			}
			final Set<GamePiece> pieces = Utilities.findAllPiecesMatching(new PieceFilter() {	
				public boolean accept(final GamePiece piece) {
					final Influence marker = Influence.getInfluenceMarker(piece);
					return marker != null && marker.getInfluence() > 0;
				}
			});

			ChangeTracker tracker;

			for (final GamePiece gp : pieces) {
				final Influence marker = Influence.getInfluenceMarker(gp);
				tracker = new ChangeTracker(marker);
				marker.startingInfluence = 0;
				marker.influence = 0;
				comm = comm.append(tracker.getChangeCommand());
			}

			for (String country: START.keySet()) {
				final INF inf = START.get(country);
				if (inf.americanInfluence == CONTROL) {
					Influence marker = Influence.getInfluenceMarker(country, TSPlayerRoster.USSR);
					tracker = new ChangeTracker(marker);
					marker.startingInfluence = marker.atStartInfluence = inf.sovietInfluence;
					comm = comm.append(tracker.getChangeCommand());
					comm = comm.append(marker.setInfluence(inf.sovietInfluence));
					marker = marker.getOpponentInfluenceMarker();
					tracker = new ChangeTracker(marker);
					marker.startingInfluence = marker.atStartInfluence = inf.sovietInfluence + marker.getStability();
					comm = comm.append(tracker.getChangeCommand());
					comm = comm.append(marker.setInfluence(inf.sovietInfluence + marker.getStability()));
				} else if (inf.sovietInfluence == CONTROL) {
					Influence marker = Influence.getInfluenceMarker(country, TSPlayerRoster.US);
					tracker = new ChangeTracker(marker);
					marker.startingInfluence= marker.atStartInfluence = inf.americanInfluence;
					comm = comm.append(tracker.getChangeCommand());
					comm = comm.append(marker.setInfluence(inf.americanInfluence));
					marker = marker.getOpponentInfluenceMarker();
					marker.startingInfluence = marker.atStartInfluence = inf.americanInfluence + marker.getStability();
					comm = comm.append(tracker.getChangeCommand());
					comm = comm.append(marker.setInfluence(inf.americanInfluence + marker.getStability()));
				} else {
					Influence marker = Influence.getInfluenceMarker(country, TSPlayerRoster.US);
					tracker = new ChangeTracker(marker);
					marker.startingInfluence = marker.atStartInfluence = inf.americanInfluence;
					comm = comm.append(tracker.getChangeCommand());
					comm = comm.append(marker.setInfluence(inf.americanInfluence));
					marker = marker.getOpponentInfluenceMarker();
					tracker = new ChangeTracker(marker);
					marker.startingInfluence = marker.atStartInfluence = inf.sovietInfluence;
					comm = comm.append(tracker.getChangeCommand());
					comm = comm.append(marker.setInfluence(inf.sovietInfluence));
				}
			}
		
			Deck deck = CardEvent.getDeckNamed(CardEvent.DRAW_DECK);
			Iterator<GamePiece> iter = deck.getPiecesIterator();
			final DrawPile removed = DrawPile.findDrawPile(CardEvent.REMOVED_FROM_PLAY_DECK_NAME);
			while (iter.hasNext()) {
				final CardEvent event = CardEvent.getCard(iter.next());
				if (event.isRemoveFromPlay())
					comm = comm.append(removed.addToContents(Decorator.getOutermost(event)));
			}
			
			deck = CardEvent.getDeckNamed(CardEvent.MID_WAR_CARDS);
			iter = deck.getPiecesIterator();
			final DrawPile draw = DrawPile.findDrawPile(CardEvent.DRAW_DECK);
			while (iter.hasNext()) {
				final CardEvent event = CardEvent.getCard(iter.next());
				if (event.isRemoveFromPlay() && !EVENTS_STILL_IN_DECK.contains(event.getClass()))
					comm = comm.append(removed.addToContents(Decorator.getOutermost(event)));
				else
					comm = comm.append(draw.addToContents(Decorator.getOutermost(event)));
			}

			deck = CardEvent.getDeckNamed(TSTurnTracker.LATE_WAR_DECK_NAME);
			comm = comm.append(deck.sendToDeck());
			deck.getMap().repaint();

			return comm;
		}
	}

	public Command getRestoreCommand() {
		return null;
	}

	@Override
	public String[] getAttributeNames() {
		return new String[0];
	}

	@Override
	public void setAttribute(String key, Object value) {
	}

	@Override
	public String getAttributeValueString(String key) {
		return null;
	}

	public void removeFrom(Buildable parent) {
		GameModule.getGameModule().getGameState().removeGameComponent(this);
	}

	public HelpFile getHelpFile() {
		return null;
	}

	public Class<?>[] getAllowableConfigureComponents() {
	    return new Class<?>[0];
	}

	@Override
	public String[] getAttributeDescriptions() {
		return new String[0];
	}

	@Override
	public Class<?>[] getAttributeTypes() {
		return new Class<?>[0];
	}

	@Override
	public Configurable[] getConfigureComponents() {
		return new Configurable[0];
	}

	public static boolean isActive() {
		return getInstance() != null;
	}

	public static TSOptions getInstance() {
		for (TSOptions pr : GameModule.getGameModule().getComponentsOf(TSOptions.class))
			return pr;
		return null;
	}

	@Override
	public String getConfigureName() {
		return "Options Configurer (Twilight Struggle)";
	}
}
