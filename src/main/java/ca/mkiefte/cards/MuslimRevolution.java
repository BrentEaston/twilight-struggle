package ca.mkiefte.cards;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.command.NullCommand;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceFilter;
import ca.mkiefte.Influence;
import ca.mkiefte.Influence.Delegate;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.Utilities;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class MuslimRevolution extends ChangeInfluence {
	private static final int N_COUNTRIES = 2;
	public static final String ID = "muslimrevolution;";
	public static final String DESCRIPTION = "Muslim Revolution";
	
	private static final Set<String> COUNTRIES = new HashSet<String>(Arrays.asList(new String[] {
		Influence.SUDAN, 
		Influence.IRAN, 
		Influence.IRAQ, 
		Influence.EGYPT, 
		Influence.LIBYA, 
		Influence.SAUDI_ARABIA, 
		Influence.SYRIA, 
		Influence.JORDAN
	}));

	public MuslimRevolution() {
		this(ID, null);
	}

	public MuslimRevolution(final String type, final GamePiece inner) {
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
		return N_COUNTRIES;
	}

	@Override
	protected Command autoPlace() {
		final Set<GamePiece> pieces = Utilities.findAllPiecesMatching(new PieceFilter() {		
			public boolean accept(final GamePiece piece) {
				final Influence marker = Influence.getInfluenceMarker(piece);
				return marker != null && passesFilter(marker) && marker.getInfluence() > 0;
			}
		});
		
		if (pieces.size() == 0) {
			final StringBuilder builder = new StringBuilder("There is no US Influence in any of");
			Utilities.listAsString(builder, COUNTRIES, "or");
			final Chatter chatter = GameModule.getGameModule().getChatter();
			final Command chat = new Chatter.DisplayText(chatter, builder.toString());
			chat.execute();
			return chat;
		} else if (pieces.size() <= 2) {
			Command comm = new NullCommand();
			for (final GamePiece gp : pieces) {
				final Influence marker = Influence.getInfluenceMarker(gp);
				comm = comm.append(marker.removeAllInfluence());
			}
			return comm;
		} else
			return null;
	}

	@Override
	protected String getMessage() {
		final StringBuilder builder = new StringBuilder("Remove all US Influence from 2 of:");
		Utilities.listAsString(builder, COUNTRIES, "and");
		return builder.toString();
	}

	@Override
	protected Delegate getDelegate() {
		return new Influence.Delegate(this, Influence.OPS_REMAINING, false) {
			@Override
			public boolean canDecreaseInfluence(final Influence marker) {
				return super.canDecreaseInfluence(marker) 
						&& passesFilter(marker);
			}

			@Override
			public Command incrementInfluence(final Influence marker, int amount) {
				if (amount < 0) 
					amount = -marker.getInfluence();
				else
					amount = marker.getStartingInfluence();
				return super.incrementInfluence(marker, amount);
			}

			@Override
			protected Command changeRemainingInfluence(final Influence marker, int amount) {
				if (amount < 0)
					amount = -1;
				else
					amount = +1;
				return super.changeRemainingInfluence(marker, amount);
			}
		};
	}

	@Override
	protected boolean passesFilter(final Influence marker) {
		return COUNTRIES.contains(marker.getLocation())
				&& TSPlayerRoster.US.equals(marker.getSide());
	}

	@Override
	public boolean isEventPlayable(final String who) {
		return super.isEventPlayable(who) && !getCard(AwacsSaleToSaudis.class).isEventInEffect();
	}
}
