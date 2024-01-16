package ca.mkiefte.cards;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Set;

import VASSAL.command.Command;
import VASSAL.command.NullCommand;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceFilter;
import ca.mkiefte.Influence;
import ca.mkiefte.Influence.Delegate;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.Utilities;

public final class WarsawPactFormed extends ChangeInfluenceOptions {	
	public static String ID = "warsawpact;";
	public static String DESCRIPTION = "Warsaw Pact Formed*";
	
	private static final String REMOVE = "Remove US Influence";
	private static final String ADD = "Add USSR Influence";
	private static final String[] OPTIONS = {REMOVE, ADD};
	private static final int INFLUENCE_TO_ADD = 5;
	private static final int COUNTRIES_TO_REMOVE = 4;

	public WarsawPactFormed() {
		this(ID, null);
	}

	public WarsawPactFormed(final String type, final GamePiece inner) {
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

	protected boolean passesFilter(final Influence marker) {
		if (ADD.equals(option))
			return marker.isInEasternEurope()
					&& TSPlayerRoster.USSR.equals(marker.getSide());
		else // default is to remove US influence
			return TSPlayerRoster.US.equals(marker.getSide())
					&& marker.isInEasternEurope()
					&& marker.getInfluence() > 0;
	}
	
	@Override
	protected int nInfluence() {
		if (ADD.equals(option))
			return INFLUENCE_TO_ADD;
		else
			return COUNTRIES_TO_REMOVE;
	}

	@Override
	protected Command autoPlace() {
		if (REMOVE.equals(option)) {
			final Set<GamePiece> pieces = Utilities.findAllPiecesMatching(new PieceFilter() {			
				public boolean accept(final GamePiece piece) {
					final Influence marker = Influence.getInfluenceMarker(piece);
					return marker != null && passesFilter(marker);
				}
			});
			
			if (pieces.size() <= 4) {
				Command comm = new NullCommand();
				for (final GamePiece gp : pieces) {
					final Influence marker = Influence.getInfluenceMarker(gp);
					comm = comm.append(marker.removeAllInfluence());
				}
				return comm;
			}
		}
		
		return null;
	}

	@Override
	protected String getOptionsMessage() {
		return "Do you wish to remove all Influence from 4\n" +
				"US countries in Eastern Europe, or add\n" +
				"5 USSR Influence in Eastern\n" +
				"Europe, no more than 2 per country?\n";
	}
	
	@Override
	protected String getStringForOption(final String option) {
		if (option.equals(OPTIONS[0])) // Removes
			return "USSR to remove all Influence from 4 countries in Eastern Europe.";
		else
			return "USSR to add 5 Influence in Eastern Europe.";
	}
	
	@Override
	protected String getMessage() {
		if (ADD.equals(option))
			return "Add 5 USSR Influence in Eastern Europe no more than 2 per country.";
		else
			return "Remove all US Influence from 4 countries in Eastern Europe.";
	}

	@Override
	protected Delegate getDelegate() {
		if (ADD.equals(option)) {
			return new Influence.Delegate(this) {
				@Override
				public boolean canIncreaseInfluence(final Influence marker) {
					return super.canIncreaseInfluence(marker)
						&& passesFilter(marker)
						&& marker.getInfluence() < marker.getStartingInfluence() + 2;
				}
			};
		} else {
			return new Influence.Delegate(this, Influence.OPS_REMAINING, false) {

				@Override
				public boolean canDecreaseInfluence(final Influence marker) {
					return super.canDecreaseInfluence(marker)
						&& passesFilter(marker);
				}

				@Override
				public Command incrementInfluence(final Influence marker, final int amount) {
					Command comm = new NullCommand();
					final int remaining = Integer.valueOf(prop.getPropertyValue());
					if (amount < 0) {
						comm = comm.append(marker.setInfluence(0));
						comm = comm.append(prop.setPropertyValue(Integer.toString(remaining-1)));
					} else {
						comm = comm.append(marker.setInfluence(marker.getStartingInfluence()));
						comm = comm.append(prop.setPropertyValue(Integer.toString(remaining+1)));
					}
					return comm;
				}
			};
		}
	}

	@Override
	protected boolean isUnderlined() {
		return true;
	}

	@Override
	protected String[] getOptions() {
		final GamePiece piece = Utilities.findPiece(new PieceFilter() {			
			public boolean accept(final GamePiece piece) {
				final Influence marker = Influence.getInfluenceMarker(piece);
				return marker != null && passesFilter(marker);
			}
		});
		if (piece == null)
			return new String[] {ADD};
		else
			return OPTIONS;
	}

	@Override
	protected Rectangle getFocusPosition() {
		final Set<GamePiece> pieces = Utilities.findAllPiecesMatching(new PieceFilter() {		
			public boolean accept(final GamePiece piece) {
				final Influence marker = Influence.getInfluenceMarker(piece);
				return marker != null && marker.isInEasternEurope();
			}
		});
		Rectangle r = null;
		for (final GamePiece gp : pieces) {
			final Influence marker = Influence.getInfluenceMarker(gp);
			final Point pos = marker.getPosition();
			final Rectangle rect = marker.boundingBox();
			rect.translate(pos.x, pos.y);
			if (r == null)
				r = rect;
			else
				r.add(rect);
		}
		return r;
	}
}