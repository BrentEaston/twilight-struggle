package ca.mkiefte;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.Timer;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.GameComponent;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.tools.SequenceEncoder;
import VASSAL.tools.imageop.Op;
import ca.mkiefte.TSTurnTracker.CurrentState;

public class TSTimer extends AbstractConfigurable implements CommandEncoder, GameComponent, ActionListener {
	
	private static final int MILISECONDS_PER_MINUTE = 1000*60;
	private static final int MILISECONDS_PER_HOUR = MILISECONDS_PER_MINUTE*60;
	public static final String COMMAND_PREFIX = "UPDATE_TIMERS:";

	private static Timer timer;
	private long startTime = -1;
	
	private JButton timerButton;
	private String image;
	private String timerName;
	private long elapsedTime;
	private long verifiedTime;
	private boolean ticking;

	public TSTimer() {
	}

	public class UpdateTimerCommand extends Command {
		private String who;
		private String name;
		private long elapsed;
		private long verified;
		
		// who = who is reporting; which player
		// name = which timer
		public UpdateTimerCommand(String who, String name, long elapsed, long verified) {
			this.who = who;
			this.name = name;
			this.elapsed = elapsed;
			this.verified = verified;
		}

		// everything that comes from another computer is verified unless its verified
		// time is earlier than this one
		@Override
		protected void executeCommand() {
			if (!name.equals(timerName)) // this shouldn't happen
				return;
			final String me = TSPlayerRoster.getMySide();
			if (who.equals(me)) {
				// my computer reporting back: restore
				// don't update elapsed time. It could have run on before saving
				elapsedTime = verified;
				verifiedTime = verified;
			} else if (name.equals(me)) { // someone else's computer is reporting my time
				if (verified > elapsedTime) { 
					// later than what we thought due to synchronization
					verifiedTime = verified;
					elapsedTime = verified;
				} else if (verified > 0L) { 
					// getting back the time I originally reported (successful connection!)
					verifiedTime = verified;
				}
				if (!timer.isRunning())
					timer.start();
			} else if (who.equals(name)) {
				// someone else's computer reporting back their own time
				elapsedTime = elapsed;
				verifiedTime = elapsed;  // we assume this is correct
				// send it back for verification
				if (me != null)
					GameModule.getGameModule().sendAndLog(new UpdateTimerCommand(me, getName(), elapsedTime, verifiedTime));
				if (!timer.isRunning())
					timer.start();
			} else
				return;
			
			setTimerButton();
		}

		@Override
		protected Command myUndoCommand() {
			return null;
		}

		@Override
		public boolean isLoggable() {
			return false;
		}
	}
	
	private void initTimerButtion() {
		timerButton = new JButton("0:00:00", new ImageIcon(Op.load(image).getImage()));
	}

	public void updateDisplay() {
		if (startTime == -1) {
			startTime = System.currentTimeMillis();
			return;
		}
		
		long currentTime = System.currentTimeMillis();
		long elapsed = currentTime - startTime;
		startTime = currentTime;
		
		if (ticking) {
			elapsedTime += elapsed;
			setTimerButton();
		}
	}

	protected void setTimerButton() {
		long time = elapsedTime;
		int hours = (int) (time / MILISECONDS_PER_HOUR);
		time %= MILISECONDS_PER_HOUR;
		int minutes = (int) (time / MILISECONDS_PER_MINUTE);
		int seconds = (int) (time % MILISECONDS_PER_MINUTE)/1000;
		timerButton.setText(String.format("%d:%02d:%02d", hours, minutes, seconds));
	}

	public String getName() {
		return timerName;
	}

	public void addTo(Buildable parent) {
		initTimerButtion();
		final GameModule gameModule = GameModule.getGameModule();
		gameModule.getToolBar().add(getComponent());
		gameModule.addCommandEncoder(this);
		gameModule.getGameState().addGameComponent(this);
		TSTurnTracker.getInstance().addTimer(this);
	}

	public void removeFrom(Buildable parent) {
		final GameModule gameModule = GameModule.getGameModule();
		gameModule.getToolBar().remove(getComponent());
		gameModule.removeCommandEncoder(this);
		gameModule.getGameState().removeGameComponent(this);
		timer.removeActionListener(this);
	}

	private JButton getComponent() {
		return timerButton;
	}

	@Override
	public String[] getAttributeNames() {
		return new String[] {"Name", "Image"};
	}

	@Override
	public void setAttribute(String key, Object value) {
		if ("Name".equals(key))
			timerName = (String) value;
		else if ("Image".equals(key))
			image = (String) value;
	}

	@Override
	public String getAttributeValueString(String key) {
		if ("Name".equals(key))
			return timerName;
		else if ("Image".equals(key))
			return image;
		else
			return null;
	}

	public HelpFile getHelpFile() {
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Class[] getAllowableConfigureComponents() {
		return new Class[0];
	}

	@Override
	public String[] getAttributeDescriptions() {
		return new String[] {"Player name associated with timer", "Image used as icon"};
	}

	@Override
	public Class<?>[] getAttributeTypes() {
		return new Class[] {String.class, String.class};
	}

	public Command decode(final String command) {
		if (command.startsWith(COMMAND_PREFIX)) {
			final SequenceEncoder.Decoder decoder = new SequenceEncoder.Decoder(command, ':');
			decoder.nextToken();
			final String who = decoder.nextToken();
			final String name = decoder.nextToken();
			if (!getName().equals(name))
				return null;
			final long elapsed = decoder.nextLong(0L);
			final long verified = decoder.nextLong(0L);
			return new UpdateTimerCommand(who, name, elapsed, verified);
		} else
			return null;
	}

	public String encode(final Command c) {
		if (c instanceof UpdateTimerCommand) {
			final UpdateTimerCommand comm = (UpdateTimerCommand) c;
			final SequenceEncoder encoder = new SequenceEncoder(':');
			encoder.append(comm.who);
			encoder.append(comm.name);
			encoder.append(comm.elapsed);
			encoder.append(comm.verified);
			return COMMAND_PREFIX + encoder.getValue();
		}
		else
			return null;
	}

	public void setup(final boolean gameStarting) {
		if (timer == null)
			timer = new Timer(1000, this);
		else if (gameStarting) {
			timer.stop();
			timer.addActionListener(this);
		}
	}

	public Command getRestoreCommand() {
		return new UpdateTimerCommand(TSPlayerRoster.getMySide(), getName(), elapsedTime, verifiedTime);
	}

	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == timer)
			updateDisplay();
	}

	public Command updateState() {
		final CurrentState state = TSTurnTracker.getCurrentState(getName());
		ticking = state != CurrentState.WAITING && state != CurrentState.GAME_OVER;
		final String mySide = TSPlayerRoster.getMySide();
		if (mySide != null)
			return new UpdateTimerCommand(mySide, getName(), elapsedTime, verifiedTime);
		else
			return null;
	}
}
