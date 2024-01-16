package ca.mkiefte.cards;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.command.NullCommand;
import VASSAL.counters.GamePiece;
import VASSAL.tools.SequenceEncoder;
import ca.mkiefte.Influence;
import ca.mkiefte.TSPlayerRoster;

public final class AsiaScoring extends ShuttleDiplomacyRegion {
	public final static String ID = "asiascoring;";
	public final static String DESCRIPTION = "Asia Scoring";
		
	public static final int PRESENCE_VPS = 3;
	public static final int DOMINATION_VPS = 7;
	public static final int CONTROL_VPS = 9;
	public AsiaScoring() {
		this(ID, null);
	}

	public AsiaScoring(final String type, final GamePiece inner) {
		super(type, inner);
	}

	private boolean isTaiwanBattleground() {
		return CardEvent.getCard(FormosanResolution.class).isEventInEffect()
				&& Influence.getInfluenceMarker(Influence.TAIWAN, TSPlayerRoster.US).hasControl();
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
	public Command calculateAndReportVps() {
		Command chat = new NullCommand();
		if (isTaiwanBattleground()) {
			final Chatter chatter = GameModule.getGameModule().getChatter();
			chat = new Chatter.DisplayText(chatter, "Taiwan treated as a battleground country.");
			chat.execute();
		}
		return chat.append(super.calculateAndReportVps());
	}

	@Override
	public String getRegion() {
		return Influence.ASIA;
	}

	@Override
	protected int getVpsForPresence() {
		return PRESENCE_VPS;
	}

	@Override
	protected int getVpsForDomination() {
		return DOMINATION_VPS;
	}

	@Override
	protected int getVpsForControl() {
		return CONTROL_VPS;
	}

	@Override
	protected int getNBattlegrounds(final String who, final boolean scoring) {
		int n = super.getNBattlegrounds(who, scoring);
		if (scoring 
				&& TSPlayerRoster.US.equals(who) 
				&& isTaiwanBattleground())
			++n;

		return n;
	}

	@Override
	protected int getTotalBattlegrounds(final boolean scoring) {
		int n = super.getTotalBattlegrounds(scoring);
		if (scoring && isTaiwanBattleground())
			++n;

		return n;
	}

	@Override
	public String myGetState() {
		final SequenceEncoder encoder = new SequenceEncoder(super.myGetState(), '+');
		encoder.append(gameOver);
		return encoder.getValue();
	}

	@Override
	public void mySetState(final String newState) {
		final SequenceEncoder.Decoder decoder = new SequenceEncoder.Decoder(newState, '+');
		super.mySetState(decoder.nextToken());
		gameOver = decoder.nextBoolean(false);
	}
}
