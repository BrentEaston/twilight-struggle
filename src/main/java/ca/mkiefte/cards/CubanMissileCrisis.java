package ca.mkiefte.cards;

import static ca.mkiefte.Utilities.getGlobalProperty;

import java.util.Arrays;
import java.util.Iterator;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import VASSAL.tools.SequenceEncoder;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.Utilities;

public final class CubanMissileCrisis extends CardEvent {
	public static final String ID = "cubanmissilecrisis;";
	public static final String DESCRIPTION = "Cuban Missile Crisis*";
	private String target;
	private boolean gameOver;

	public CubanMissileCrisis() {
		this(ID, null);
	}

	public CubanMissileCrisis(final String type, final GamePiece inner) {
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
		final int defcon = Integer.valueOf(getGlobalProperty(Utilities.DEFCON).getPropertyValue());
		comm = comm.append(Utilities.adjustDefcon(2 - defcon));
		final String opponent = TSPlayerRoster.US.equals(getOwner()) ? TSPlayerRoster.USSR : TSPlayerRoster.US;			
		final ChangeTracker tracker = new ChangeTracker(this);
		target = opponent;
		eventInEffect = true;
		comm = comm.append(tracker.getChangeCommand());
		final Chatter chatter = GameModule.getGameModule().getChatter();
		StringBuilder builder = new StringBuilder("Any coups by ").append(opponent).append(" this Turn will result in Global Thermonuclear War.");
		Command chat = new Chatter.DisplayText(chatter, builder.toString());
		if (TSPlayerRoster.US.equals(who))
			chat = chat.append(new Chatter.DisplayText(chatter, "Remove 2 Influence from Cuba to cancel this event."));
		else
			chat = chat.append(new Chatter.DisplayText(chatter, "Remove 2 Influence from eitehr Turkey or W. Germany to cancel this event."));
		final String[] countries = getCountries();
		builder = new StringBuilder("Remove 2 Influence from ");
		if (countries.length > 1) {
			final Iterator<String> iter = Arrays.asList(countries).iterator();
			String s = iter.next();
			do {
				builder.append(s);
				if (iter.hasNext()) {
					s = iter.next();
					if (iter.hasNext())
						builder.append(", ");
					else
						builder.append(" or ");
				} else
					break;
			} while (true);
			builder.append(" to cancel ").append(getDescription()).append('.');
		}
		chat.execute();
		
		return comm;
	}
	
	private String[] getCountries() {
		final String[] countries;
		if (TSPlayerRoster.US.equals(target))
			countries = new String[] {Influence.W_GERMANY, Influence.TURKEY};
		else
			countries = new String[] {Influence.CUBA};
		return countries;
	}

	public String getTarget() {
		return target;
	}
	
	@Override
	public boolean isEventInEffect() {
		return super.isEventInEffect() && isEventPlayedThisTurn();
	}

	@Override
	public String myGetState() {
		final SequenceEncoder encoder = new SequenceEncoder(super.myGetState(), '+');
		encoder.append(target);
		encoder.append(gameOver);
		return encoder.getValue();
	}

	@Override
	public void mySetState(final String newState) {
		final SequenceEncoder.Decoder decoder = new SequenceEncoder.Decoder(newState, '+');
		super.mySetState(decoder.nextToken());
		target = decoder.nextToken(null);
		gameOver = decoder.nextBoolean(false);
	}

	public boolean isGameOver() {
		return gameOver;
	}

	public Command setGameOver(final boolean b) {
		if (b == gameOver)
			return null;
		final ChangeTracker tracker = new ChangeTracker(this);
		gameOver = b;
		Command comm = tracker.getChangeCommand();
		if (gameOver) {
			final Chatter chatter = GameModule.getGameModule().getChatter();
			Command chat = new Chatter.DisplayText(chatter, "!!! Global Thermonuclear War !!!");
			final StringBuilder builder = new StringBuilder(target).append(" has lost the game.");
			chat = chat.append(new Chatter.DisplayText(chatter, builder.toString()));
			chat.execute();
			comm = comm.append(chat);
		}
		return comm;
	}
}
