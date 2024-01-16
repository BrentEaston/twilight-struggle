package ca.mkiefte.cards;

import java.util.Arrays;
import java.util.HashSet;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceFilter;
import VASSAL.tools.SequenceEncoder;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;
import ca.mkiefte.Utilities;

public final class Che extends ChangeInfluence {
	public static final String ID = "che;";
	public static final String DESCRIPTION = "Che";
	private static String firstCoupLocation;
	
	private static HashSet<String> REGIONS = new HashSet<String>(Arrays.asList(new String[] {
			Influence.CENTRAL_AMERICA,
			Influence.SOUTH_AMERICA,
			Influence.AFRICA
	}));
	
	public String getFirstCoupLocation() {
		return firstCoupLocation;
	}

	public Che() {
		this(ID, null);
	}

	public Che(final String type, final GamePiece inner) {
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
	public String myGetState() {
		final SequenceEncoder se = new SequenceEncoder(super.myGetState(), '#');
		se.append(firstCoupLocation);
		return se.getValue();
	}

	@Override
	public void mySetState(final String newState) {
		final SequenceEncoder.Decoder se = new SequenceEncoder.Decoder(newState, '#');
		super.mySetState(se.nextToken());
		firstCoupLocation = se.nextToken(null);
	}

	@Override
	protected int nInfluence() {
		return getOps();
	}

	@Override
	protected Command autoPlace() {
		final GamePiece piece = Utilities.findPiece(new PieceFilter() {			
			public boolean accept(final GamePiece piece) {
				final Influence marker = Influence.getInfluenceMarker(piece);
				return marker != null && passesFilter(marker) && marker.getInfluence() > 0;
			}
		});
		
		if (piece == null) {
			final StringBuilder builder = new StringBuilder("US has no Influence in non-Battleground countries in");
			Utilities.listAsString(builder, REGIONS, "or");
			final Command chat = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), 
					 builder.toString());
			chat.execute();
			return chat;
		} else
			return null;
	}

	@Override
	protected String getMessage() {
		final StringBuilder builder = new StringBuilder("Make a Coup attempt against any non-battleground country\n"
				+ "in");
		Utilities.listAsString(builder, REGIONS, "or");
		builder.deleteCharAt(builder.length()-1);
		if (firstCoupLocation != null && !firstCoupLocation.isEmpty()) {
			builder.deleteCharAt(builder.length()-1);
			builder.append(" except ").append(firstCoupLocation).append('.');
		}
		return builder.toString();
	}

	@Override
	protected Influence.Delegate getDelegate() {
		return new Influence.Delegate(this) {
			@Override
			public boolean canCoup() {
				return true;
			}

			@Override
			public boolean canChangeInfluence() {
				return false;
			}

			@Override
			public boolean canCoup(final Influence marker) {
				if (firstCoupLocation != null && !firstCoupLocation.isEmpty() && firstCoupLocation.equals(marker.getLocation()))
					return false;
				else
					return super.canCoup(marker) && passesFilter(marker);
			}

			@Override
			public Command coup(final Influence marker) {
				Command comm;
				if (firstCoupLocation == null || firstCoupLocation.isEmpty()) {
					comm = marker.coup(who, getInfluenceAvailableFor(marker), freeCoup());
					if (comm == null || comm.isNull())
						return comm;
					final Chatter chatter = GameModule.getGameModule().getChatter();
					final Command chat;
					if (marker.getInfluence() == marker.getStartingInfluence()) {
						chat = new Chatter.DisplayText(chatter, "USSR may not make a second Coup attempt.");
						comm = comm.append(setFinished(true));
					} else {
						chat = new Chatter.DisplayText(chatter, "USSR may make a second Coup attempt.");
						final ChangeTracker tracker = new ChangeTracker(Che.this);
						firstCoupLocation = marker.getLocation();
						comm = comm.append(tracker.getChangeCommand());
					}
					chat.execute();
					comm = comm.append(chat);
					comm = comm.append(Influence.getDialog().finish());
				} else {
					final ChangeTracker tracker = new ChangeTracker(Che.this);
					firstCoupLocation = null;
					comm = tracker.getChangeCommand();
					comm = comm.append(super.coup(marker));
				}
				return comm;
			}

			@Override
			public Command finish() {
				if (firstCoupLocation != null && !firstCoupLocation.isEmpty()) {
					event = null;
					return super.finish().append(setup());
				} else
					return super.finish();
			}
		};
	}

	@Override
	protected boolean passesFilter(final Influence marker) {
		if (firstCoupLocation != null && !firstCoupLocation.isEmpty() && firstCoupLocation.equals(marker.getLocation()))
			return false;
		final String region = marker.getRegion();
		if (!TSPlayerRoster.US.equals(marker.getSide()) || marker.isBattleground())
			return false;
		for (final String r : REGIONS)
			if (region.equals(r))
				return true;
		return false;
	}

	@Override
	public Command updateState() {
		if (firstCoupLocation != null && !firstCoupLocation.isEmpty())
			return setup().append(super.updateState());
		else
			return super.updateState();
	}

	@Override
	protected void myClearState() {
		super.myClearState();
		firstCoupLocation = null;
	}
}
