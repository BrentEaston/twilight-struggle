package ca.mkiefte;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.GlobalOptions;
import VASSAL.build.module.PlayerRoster;
import VASSAL.command.Command;
import VASSAL.configure.StringEnumConfigurer;
import VASSAL.i18n.Resources;

public class TSPlayerRoster extends PlayerRoster {

	private static final String RANDOM_ASSIGNMENT = "RandomAssignment";
	private static final String CHOOSE_RANDOMLY = "Choose Randomly";
	// side names
	
	public static final String US = "US";
	public static final String USSR = "USSR";
	public static final String BOTH = "Both";

	public TSPlayerRoster() {
		super();
	}

	@Override
	public void finish() {
	    String newSide = untranslateSide(sideConfig.getValueString());
	    if (newSide != null) {
	    	if (CHOOSE_RANDOMLY.equals(newSide)) {
	    		final List<String> sides = new ArrayList<String>(Arrays.asList(sideConfig.getValidValues()));
	    		sides.remove(translatedObserver);
	    		sides.remove(CHOOSE_RANDOMLY);
	    		newSide = sides.get(GameModule.getGameModule().getRNG().nextInt(sides.size()));
	    		Utilities.getGlobalProperty(RANDOM_ASSIGNMENT).setPropertyValue(Utilities.TRUE);
	    	}	    		
	      final Add a = new Add(this, GameModule.getUserId(), GlobalOptions.getInstance().getPlayerId(), newSide);
	      a.execute();
	      GameModule.getGameModule().getServer().sendToOthers(a);
	    }
	    retireButton.setVisible(getMySide() != null);
	}

	@Override
	public Component getControls() {
	    ArrayList<String> availableSides = new ArrayList<String>(sides);
	    ArrayList<String> alreadyTaken = new ArrayList<String>();

	    for (PlayerInfo p : players) {
	      alreadyTaken.add(p.getSide());
	    }

	    availableSides.removeAll(alreadyTaken);
	    if (availableSides.size() > 1)
		    availableSides.add(CHOOSE_RANDOMLY);
	    availableSides.add(0, translatedObserver);
	    sideConfig = new StringEnumConfigurer(null,
	      Resources.getString("PlayerRoster.join_game_as"), //$NON-NLS-1$
	      availableSides.toArray(new String[availableSides.size()]));
	    sideConfig.setValue(translatedObserver);
	    return sideConfig.getControls();
	}

	@Override
	public String getConfigureName() {
		return "Player Side Randomizer (Twilight Struggle)";
	}

	@Override
	public void setup(boolean gameStarting) {
		super.setup(gameStarting);
		if (gameStarting && (TSPlayerRoster.US.equals(getMySide()) || TSPlayerRoster.USSR.equals(getMySide()))) {
			final Chatter chatter = GameModule.getGameModule().getChatter();
			Command chat = new Chatter.DisplayText(chatter, new StringBuilder("You are playing ").append(getMySide()).append(".").toString());
			if (Utilities.TRUE.equals(Utilities.getGlobalProperty(RANDOM_ASSIGNMENT).getPropertyValue()))
				chat = new Chatter.DisplayText(chatter, "Sides were assigned randomly.").append(chat);
			chat.execute();
		}
	}

}
