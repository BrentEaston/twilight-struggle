package ca.mkiefte.cards;

import static ca.mkiefte.Utilities.adjustDefcon;
import static ca.mkiefte.Utilities.adjustVps;

import java.awt.Dialog.ModalityType;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.command.NullCommand;
import VASSAL.counters.GamePiece;
import VASSAL.tools.SequenceEncoder;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.TSTurnTracker;
import ca.mkiefte.Influence.Delegate;
import ca.mkiefte.Utilities;

public final class OlympicGames extends ConductOperations {
	
	private static final int INFLUENCE = 4;
	private static final String BOYCOTT = "Boycott";
	private static final String PARTICIPATE = "Participate";
	private static final String[] OPTIONS = {PARTICIPATE, BOYCOTT};
	private String option;
	
	public static String ID = "olympicgames;";
	public static String DESCRIPTION = "Olympic Games Event";

	public OlympicGames() {
		this(ID, null);
	}

	public OlympicGames(final String type, final GamePiece inner) {
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

	public Command updateState() {		
		Command comm = new NullCommand();

		if (option == null || option.isEmpty()) {
			final String sponsor = getOwner();
			final String opponent = TSPlayerRoster.US.equals(sponsor) ? TSPlayerRoster.USSR : TSPlayerRoster.US;
			final JOptionPane optionPane = new JOptionPane("Do you wish to participate or boycott?", 
					JOptionPane.QUESTION_MESSAGE, 
					JOptionPane.YES_NO_OPTION, 
					asIcon(), 
					OPTIONS, 
					OPTIONS[0]);
			final JDialog dialog = optionPane.createDialog(new StringBuilder(sponsor).append(" sponsors Olympics.").toString());
			dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
			dialog.setAlwaysOnTop(true);
			dialog.setVisible(true);
			final ChangeTracker tracker = new ChangeTracker(this);
			option = (String) optionPane.getValue();
			final Chatter chatter = GameModule.getGameModule().getChatter();
			Command chat;
			StringBuilder builder;
			if (option.equals(PARTICIPATE)) { // Participates
				final int[] dieRolls = new int[] {0,0};
				builder = new StringBuilder(opponent).append(" participates.");
				chat = new Chatter.DisplayText(chatter, builder.toString());
				builder = new StringBuilder(sponsor).append(" receives +2 die-roll bonus.");
				chat = chat.append(new Chatter.DisplayText(chatter, builder.toString()));
				chat.execute();
				comm = chat;
				final String[] sides = new String[] {TSPlayerRoster.US, TSPlayerRoster.USSR};
				while (dieRolls[0] == dieRolls[1]) {
					comm = comm.append(Utilities.rollDie(sides, dieRolls));
					if (TSPlayerRoster.US.equals(sponsor))
						dieRolls[0] += 2;
					else
						dieRolls[1] += 2;
					if (dieRolls[0] == dieRolls[1]) {
						chat = new Chatter.DisplayText(chatter, "- tie: roll again.");
						chat.execute();
						comm = comm.append(chat);
					}	
				}
				final int vps = dieRolls[0] > dieRolls[1] ? 2 : -2;
				comm = comm.append(adjustVps(vps));
				comm = comm.append(Utilities.getGlobalProperty(Influence.OPS_REMAINING).setPropertyValue(""));
				eventFinished = true;
				comm = comm.append(tracker.getChangeCommand());
				return comm;
			} else { // Boycotts
				comm = comm.append(tracker.getChangeCommand());
				builder = new StringBuilder(opponent).append(" boycotts the Olympic Games.");
				chat = new Chatter.DisplayText(chatter, builder.toString());
				chat.execute();
				comm = comm.append(chat);
				comm = comm.append(adjustDefcon(-1));
				if (TSTurnTracker.isGameOver())
					return comm;
				else
					return TSTurnTracker.updateState();
			}
		}
		
		return comm.append(super.updateState());
	}

	@Override
	protected int nInfluence() {
		return option == null || !option.equals(BOYCOTT) ? 0 : INFLUENCE;
	}

	@Override
	public String myGetState() {
		final SequenceEncoder encoder = new SequenceEncoder(super.myGetState(), '+');
		encoder.append(option);
		return encoder.getValue();
	}

	@Override
	public void mySetState(String newState) {
		final SequenceEncoder.Decoder decoder = new SequenceEncoder.Decoder(newState, '+');
		super.mySetState(decoder.nextToken());
		option = decoder.nextToken(null);
	}

	@Override
	protected void myClearState() {
		super.myClearState();
		option = null;
	}

	@Override
	public String activeSide() {
		if (BOYCOTT.equals(option))
			return super.activeSide();
		else
			return TSPlayerRoster.US.equals(getOwner()) ? TSPlayerRoster.USSR : TSPlayerRoster.US;
	}

	@Override
	public boolean isEventFinished() {
		if (BOYCOTT.equals(option))
			return super.isEventFinished();
		else
			return eventFinished;
	}

	@Override
	protected Delegate getDelegate() {
		return new Influence.ConductOperationsDelegate(this) {
			@Override
			public boolean isOptional() {
				return true;
			}			
		};

	}

	@Override
	public boolean canUndoEvent() {
		return false;
	}
}