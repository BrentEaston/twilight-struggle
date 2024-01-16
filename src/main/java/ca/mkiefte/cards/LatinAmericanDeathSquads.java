package ca.mkiefte.cards;
import ca.mkiefte.TSPlayerRoster;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import VASSAL.tools.SequenceEncoder;


/**
 * Containment.
 * @author Michael Kiefte
 */
public final class LatinAmericanDeathSquads extends CardEvent {
	public static final String ID = "latinamericandeathsquads;";
	public static final String DESCRIPTION = "Latin American Death Squads";
	private int americanModifier;
	private int sovietModifier;

	public LatinAmericanDeathSquads() {
		this(ID, null);
	}

	public LatinAmericanDeathSquads(final String type, final GamePiece inner) {
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
	protected Command myPlayEvent(final String who) {
		Command comm = super.myPlayEvent(who);
		final boolean newTurn = !isEventPlayedThisTurn();
		final String opponent = TSPlayerRoster.US.equals(who) ? TSPlayerRoster.USSR : TSPlayerRoster.US;
		final Chatter chatter = GameModule.getGameModule().getChatter();
		final ChangeTracker change = new ChangeTracker(this);
		if (newTurn) {
			americanModifier = 0;
			sovietModifier = 0;
		}
		if (TSPlayerRoster.USSR.equals(opponent)) {
			--sovietModifier;
			++americanModifier;
		} else {
			--americanModifier;
			++sovietModifier;
		}
		comm = comm.append(change.getChangeCommand());
		final StringBuilder builder = new StringBuilder("USSR coups are ");
		if (sovietModifier > 0)
			builder.append('+');
		builder.append(sovietModifier);
		builder.append(" and US coups are ");
		if (americanModifier > 0)
			builder.append('+');
		builder.append(americanModifier).append(" for the remainder of the turn.");
		final Command chat = new Chatter.DisplayText(chatter, builder.toString());
		chat.execute();
		comm = comm.append(chat);
		return comm;
	}

	@Override
	public String myGetState() {
		final SequenceEncoder encoder = new SequenceEncoder(super.myGetState(), '+');
		encoder.append(americanModifier);
		encoder.append(sovietModifier);
		return encoder.getValue();
	}

	@Override
	public void mySetState(final String newState) {
		final SequenceEncoder.Decoder decoder = new SequenceEncoder.Decoder(newState, '+');
		super.mySetState(decoder.nextToken());
		americanModifier = decoder.nextInt(0);
		sovietModifier = decoder.nextInt(0);
	}

	@Override
	public boolean isEventInEffect() {
		return isEventPlayedThisTurn();
	}

	public int getModifierFor(final String who) {
		return TSPlayerRoster.US.equals(who) ? americanModifier : sovietModifier;
	}
}
