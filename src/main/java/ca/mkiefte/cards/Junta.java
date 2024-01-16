package ca.mkiefte.cards;

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

public final class Junta extends ChangeInfluence {
	private static final int INFLUENCE = 2;
	public static final String ID = "junta;";
	public static final String DESCRIPTION = "Junta";
	private boolean influencePlaced;

	public Junta() {
		this(ID, null);
	}

	public Junta(final String type, final GamePiece inner) {
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
		return INFLUENCE;
	}

	@Override
	protected Command autoPlace() {
		if (!influencePlaced)
			return null;
		else {
			final String opponent = TSPlayerRoster.US.equals(getOwner()) ? TSPlayerRoster.USSR : TSPlayerRoster.US;
			final GamePiece gp = Utilities.findPiece(new PieceFilter() {			
				public boolean accept(final GamePiece piece) {
					final Influence marker = Influence.getInfluenceMarker(piece);
					return marker != null 
							&& marker.getInfluence() > 0 
							&& opponent.equals(marker.getSide()) 
							&& passesFilter(marker);
				}
			});
			if (gp == null) {
				final Chatter chatter = GameModule.getGameModule().getChatter();
				final StringBuilder builder = new StringBuilder("There are no ").append(opponent).append(" Influence in Central or South America to Coup or Realign.");
				final Command chat = new Chatter.DisplayText(chatter, builder.toString());
				chat.execute();
				return chat;
			} else
				return null;
		}
	}

	@Override
	protected String getMessage() {
		if (!influencePlaced)
			return "Place 2 Influence in any one Central or South American country.";
		else
			return "Make a free Coup attempt or Realignment rolls in Central or South America.";
	}

	@Override
	protected Delegate getDelegate() {
		if (!influencePlaced) {
			return new Influence.Delegate(this) {

				@Override
				public boolean canIncreaseInfluence(final Influence marker) {
					return super.canIncreaseInfluence(marker)
							&& passesFilter(marker);
				}

				@Override
				public Command incrementInfluence(final Influence marker, int amount) {
					if (amount > 0) {
						amount *= 2;
						return super.incrementInfluence(marker, amount);
					} else {
						Command comm = marker.setInfluence(marker.getStartingInfluence());
						comm = comm.append(changeRemainingInfluence(marker, +2));
						comm = comm.append(usedAs.setPropertyValue(""));
						return comm;
					}
				}

				@Override
				public Command finish() {
					Command comm = super.finish();
					final ChangeTracker tracker = new ChangeTracker(Junta.this);
					influencePlaced = true;
					eventFinished = false;
					comm = comm.append(tracker.getChangeCommand());
					comm = comm.append(setup());
					return comm;
				}

				@Override
				public boolean freeCoup() {
					return true;
				}
			};
		} else {
			return new Influence.Delegate(this) {
				@Override
				public boolean canChangeInfluence() {
					return false;
				}

				@Override
				protected boolean canCoupOrRealign(final Influence marker) {
					return super.canCoupOrRealign(marker) && passesFilter(marker);
				}

				@Override
				public boolean isOptional() {
					return true;
				}

				@Override
				public boolean freeCoup() {
					return true;
				}

				@Override
				public boolean canRealign() {
					return true;
				}

				@Override
				public boolean canCoup() {
					return true;
				}
			};
		}
	}

	@Override
	protected boolean passesFilter(final Influence marker) {
		final String region = marker.getRegion();
		return Influence.CENTRAL_AMERICA.equals(region)
				|| Influence.SOUTH_AMERICA.equals(region);
	}

	@Override
	public String myGetState() {
		final SequenceEncoder encoder = new SequenceEncoder(super.myGetState(), '+');
		encoder.append(influencePlaced);
		return encoder.getValue();
	}

	@Override
	public void mySetState(final String newState) {
		final SequenceEncoder.Decoder decoder = new SequenceEncoder.Decoder(newState, '+');
		super.mySetState(decoder.nextToken());
		influencePlaced = decoder.nextBoolean(false);
	}

	@Override
	protected void myClearState() {
		super.myClearState();
		influencePlaced = false;
	}
}
