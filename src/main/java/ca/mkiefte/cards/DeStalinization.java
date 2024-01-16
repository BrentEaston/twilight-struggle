package ca.mkiefte.cards;
import javax.swing.JOptionPane;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceFilter;
import VASSAL.tools.SequenceEncoder;
import ca.mkiefte.Influence;
import ca.mkiefte.Influence.Delegate;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.Utilities;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class DeStalinization extends ChangeInfluence {
	public static final String ID = "destalinization;";
	public static final String DESCRIPTION = "De-Stalinization*";
	private int removed;
	private static final int INFLUENCE = 4;

	public DeStalinization() {
		this(ID, null);
	}

	public DeStalinization(final String type, final GamePiece inner) {
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
	protected int nInfluence() {
		return 0;
	}

	@Override
	protected Command autoPlace() {
		final Chatter chatter = GameModule.getGameModule().getChatter();
		if (null == Utilities.findPiece(new PieceFilter() {			
			public boolean accept(final GamePiece piece) {
				final Influence marker = Influence.getInfluenceMarker(piece);
				return marker != null && TSPlayerRoster.USSR.equals(marker.getSide()) && marker.getInfluence() > 0;
			}
		})) {
			Command chat = new Chatter.DisplayText(chatter, "There is no USSR Influence on the board.");
			chat.execute();
			return chat;
		}
		
		if (null == Utilities.findPiece(new PieceFilter() {
			public boolean accept(final GamePiece piece) {
				final Influence marker = Influence.getInfluenceMarker(piece);
				return marker != null && TSPlayerRoster.USSR.equals(marker.getSide()) && passesFilter(marker);
			}
		})) {
			Command chat = new Chatter.DisplayText(chatter, 
					"There are no countries that are not controlled by the US."); // like, seriously, dude.
			chat.execute();
			return chat;
		}
		
		return null;
	}

	@Override
	protected String getMessage() {
		return "Relocate up to 4 Influence to non-US controlled countries,\n"
				+ "no more than 2 per country. Decrease USSR influence first\n"
				+ "in order to place elsewhere.";
	}
	
	private int getRemoved() {
		return removed;
	}

	@Override
	protected Delegate getDelegate() {
		return new Influence.Delegate(this) {
			@Override
			public boolean canDecreaseInfluence(final Influence marker) {
				return (TSPlayerRoster.USSR.equals(marker.getSide()) 
						&& getRemoved() < INFLUENCE 
						&& marker.getInfluence() > 0)
						|| super.canDecreaseInfluence(marker);
			}

			@Override
			public boolean isFinished() {
				if (!super.isFinished())
					return false;
				if (getRemoved() < INFLUENCE) {
					final StringBuilder builder = new StringBuilder("You have only relocated ").append(removed).append(" Influence.\n"
							+ "Are you sure you are finished?");
					return JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(
							GameModule.getGameModule().getFrame(),
							builder.toString(),
							getDescription(),
							JOptionPane.YES_NO_OPTION, 
							JOptionPane.WARNING_MESSAGE);
				} else
					return true;
			}

			@Override
			public Command incrementInfluence(final Influence marker, final int amount) {
				Command comm = super.incrementInfluence(marker, amount);
				if (amount > 0 && marker.getInfluence() <= marker.getStartingInfluence()
						|| amount < 0 && marker.getInfluence() < marker.getStartingInfluence()) {
					final ChangeTracker tracker = new ChangeTracker(DeStalinization.this);
					removed -= amount;
					comm = comm.append(tracker.getChangeCommand());
				}
				return comm;
			}

			@Override
			public boolean canIncreaseInfluence(final Influence marker) {
				return super.canIncreaseInfluence(marker) && passesFilter(marker);
			}
		};
	}

	@Override
	protected boolean passesFilter(final Influence marker) {
		return !marker.getOpponentInfluenceMarker().hasControl();
	}

	@Override
	public String myGetState() {
		final SequenceEncoder encoder = new SequenceEncoder(super.myGetState(), '+');
		encoder.append(removed);
		return encoder.getValue();
	}

	@Override
	public void mySetState(final String newState) {
		final SequenceEncoder.Decoder decoder = new SequenceEncoder.Decoder(newState, '+');
		super.mySetState(decoder.nextToken());
		removed = decoder.nextInt(0);
	}

	@Override
	protected void myClearState() {
		super.myClearState();
		removed = 0;
	}

	@Override
	protected Command setup() {
		Command comm = super.setup();
		if (comm == null)
			return Utilities.getGlobalProperty(Influence.OPS_REMAINING).setPropertyValue(Integer.toString(nInfluence()));
		else
			return comm;
	}
}
