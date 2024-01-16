package ca.mkiefte.cards;

import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import ca.mkiefte.Influence;
import ca.mkiefte.Influence.Delegate;
import ca.mkiefte.TSPlayerRoster;

public final class UssuriRiverSkirmish extends ChangeInfluence {
	private static final int INFLUENCE = 4;
	public static final String ID = "ussuririverskirmish;";
	public static final String DESCRIPTION = "Ussuri River Skirmish*";

	public UssuriRiverSkirmish() {
		this(ID, null);
	}

	public UssuriRiverSkirmish(String type, GamePiece inner) {
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
		final TheChinaCard chinaCard = getCard(TheChinaCard.class);
		if (TSPlayerRoster.USSR.equals(chinaCard.getOwner())) {
			Command comm = chinaCard.sendCardToHand(TSPlayerRoster.US);
			comm = comm.append(chinaCard.setAvailable(true));
			final Chatter chatter = GameModule.getGameModule().getChatter();
			final Command chat = new Chatter.DisplayText(chatter, "US claims The China Card face up and unavailable for play.");
			chat.execute();
			comm = comm.append(chat);
			return comm;
		} else
			return null;
	}

	@Override
	protected String getMessage() {
		return "Add 4 US Influence in Asia, no more than 2 per country.";
	}

	@Override
	protected Delegate getDelegate() {
		return new Delegate(this) {
			@Override
			public boolean canIncreaseInfluence(final Influence marker) {
				return super.canIncreaseInfluence(marker)
						&& passesFilter(marker)
						&& marker.getInfluence() < marker.getStartingInfluence() + 2;
			}		
		};
	}

	@Override
	protected boolean passesFilter(final Influence marker) {
		return TSPlayerRoster.US.equals(marker.getSide()) && Influence.ASIA.equals(marker.getRegion());
	}
}
