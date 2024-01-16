package ca.mkiefte;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import ca.mkiefte.cards.CardEvent;
import ca.mkiefte.cards.CubanMissileCrisis;
import VASSAL.build.GameModule;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import VASSAL.counters.KeyCommand;

public class CMCInfluence extends Influence {
	public static final String ID = "cmcinfluence;";
	public static final String DESCRIPTION = "Cuban Missile Crisis Influence";
	
	
	public CMCInfluence() {
		this(ID, null);
	}

	public CMCInfluence(final String type, final GamePiece inner) {
		super(type, inner);
	}
	
	public String getDescription() {
		return DESCRIPTION;
	}
	
	public String getID() {
		return ID;
	}

	private boolean canCancelCubanMissileCrisis() {
		final CubanMissileCrisis cmc = CardEvent.getCard(CubanMissileCrisis.class);
		final String target = cmc.getTarget();
		return cmc.isEventInEffect() 
				&& target.equals(getSide()) && getInfluence() >= 2
				&& TSPlayerRoster.getMySide().equals(target);
	}
	
	@Override
	public Command myKeyEvent(final KeyStroke stroke) {
		if (stroke.equals(DECREASE_INFLUENCE_KEY) && canCancelCubanMissileCrisis()) {
			final CubanMissileCrisis cmc = CardEvent.getCard(CubanMissileCrisis.class);
			final int n = JOptionPane.showConfirmDialog(GameModule.getGameModule().getFrame(), 
					"Do you wish to cancel Cuban Missile Crisis?",
					"Cuban Missile Crisis",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					cmc.asIcon());
			if (n == JOptionPane.YES_OPTION) {
				Command comm = cmc.cancelEvent();
				comm = comm.append(adjustInfluence(-2));
				return comm;
			} 
			else if (super.canDecreaseInfluence())
				return super.myKeyEvent(stroke);
			else
				return null;		
		} else
			return super.myKeyEvent(stroke);
	}

	@Override
	protected KeyCommand[] myGetKeyCommands() {
		KeyCommand[] commands = super.myGetKeyCommands();
		if (canCancelCubanMissileCrisis()) {
			final StringBuilder builder = new StringBuilder("Cancel ").append(CardEvent.getCard(CubanMissileCrisis.class).getDescription());
			if (canDecreaseInfluence())
				builder.append('/').append(DECREASE_INFLUENCE);
			for (int i = 0; i < commands.length; ++i) {
				if (commands[i].getName().equals(DECREASE_INFLUENCE)) {
					commands[i] = new KeyCommand(builder.toString(), DECREASE_INFLUENCE_KEY, this, true);
					break;
				}
			}
		}
		return commands;
	}
}
