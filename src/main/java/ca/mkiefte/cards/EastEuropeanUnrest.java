package ca.mkiefte.cards;
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
import ca.mkiefte.TSTurnTracker;
import ca.mkiefte.Utilities;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class EastEuropeanUnrest extends ChangeInfluence {
	private static final int COUNTRIES = 3;
	public static final String ID = "easteuropeanunrest;";
	public static final String DESCRIPTION = "East European Unrest";

	public EastEuropeanUnrest() {
		this(ID, null);
	}

	public EastEuropeanUnrest(final String type, final GamePiece inner) {
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
		return COUNTRIES;
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
			final Chatter chatter = GameModule.getGameModule().getChatter();
			final Command chat = new Chatter.DisplayText(chatter, "There is no USSR Influence in Eastern Europe.");
			chat.execute();
			return chat;
		} else if (pieces.size() <= 3) {
			Command comm = new NullCommand();
			for (final GamePiece gp : pieces) {
				final Influence marker = Influence.getInfluenceMarker(gp);
				final int amount = TSTurnTracker.isLateWar() ? 2 : 1;
				comm = comm.append(marker.adjustInfluence(-amount));
			}
			return comm;
		} else
			return null;
	}

	@Override
	protected String getMessage() {
		final StringBuilder builder = new StringBuilder("Remove ");
		builder.append(TSTurnTracker.isLateWar() ? 2 : 1).append(" Influence from 3 countries in Eastern Europe.");
		return builder.toString();
	}

	@Override
	protected Delegate getDelegate() {
		return new Influence.Delegate(this, Influence.OPS_REMAINING, false) {

			@Override
			public boolean canDecreaseInfluence(final Influence marker) {
				return super.canDecreaseInfluence(marker)
						&& passesFilter(marker)
						&& marker.getInfluence() == marker.getStartingInfluence();
			}

			@Override
			public Command incrementInfluence(final Influence marker, final int amount) {
				if (!TSTurnTracker.isLateWar())
					return super.incrementInfluence(marker, amount);
				else if (amount < 0) {
					Command comm = marker.addInfluence(-2);
					final int value = Integer.valueOf(prop.getPropertyValue());
					comm = comm.append(prop.setPropertyValue(Integer.toString(value-1)));
					return comm;
				} else {
					Command comm = marker.setInfluence(marker.getStartingInfluence());
					final int value = Integer.valueOf(prop.getPropertyValue());
					comm = comm.append(prop.setPropertyValue(Integer.toString(value+1)));
					return comm;
				}
			}
		};
	}

	@Override
	protected boolean passesFilter(final Influence marker) {
		return TSPlayerRoster.USSR.equals(marker.getSide()) && marker.isInEasternEurope();
	}
}
