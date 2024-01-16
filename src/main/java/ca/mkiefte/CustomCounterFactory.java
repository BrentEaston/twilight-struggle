package ca.mkiefte;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import VASSAL.build.GameModule;
import VASSAL.build.module.BasicCommandEncoder;
import VASSAL.counters.Decorator;
import VASSAL.counters.GamePiece;
import VASSAL.tools.ErrorDialog;
import ca.mkiefte.cards.*;

public final class CustomCounterFactory extends BasicCommandEncoder {
	
	private static Map<String,Class<?>> myDecoratorFactories = new HashMap<String,Class<?>>();
	
	static {
		myDecoratorFactories.put(LatinAmericanDeathSquads.ID, LatinAmericanDeathSquads.class);
		myDecoratorFactories.put(NuclearSubs.ID, NuclearSubs.class);
		myDecoratorFactories.put(CMCInfluence.ID, CMCInfluence.class);
		myDecoratorFactories.put(YuriAndSamantha.ID, YuriAndSamantha.class);
		myDecoratorFactories.put(IranContraScandal.ID, IranContraScandal.class);
		myDecoratorFactories.put(TheReformer.ID, TheReformer.class);
		myDecoratorFactories.put(NorthSeaOil.ID, NorthSeaOil.class);
		myDecoratorFactories.put(IranianHostageCrisis.ID, IranianHostageCrisis.class);
		myDecoratorFactories.put(SouthAmericaScoring.ID, SouthAmericaScoring.class);
		myDecoratorFactories.put(AfricaScoring.ID, AfricaScoring.class);
		myDecoratorFactories.put(JohnPaulIIElectedPope.ID, JohnPaulIIElectedPope.class);
		myDecoratorFactories.put(AwacsSaleToSaudis.ID, AwacsSaleToSaudis.class);
		myDecoratorFactories.put(PershingIIDeployed.ID, PershingIIDeployed.class);
		myDecoratorFactories.put(MarineBarracksBombing.ID, MarineBarracksBombing.class);
		myDecoratorFactories.put(LiberationTheology.ID, LiberationTheology.class);
		myDecoratorFactories.put(TheVoiceOfAmerica.ID, TheVoiceOfAmerica.class);
		myDecoratorFactories.put(SadatExpelsSoviets.ID, SadatExpelsSoviets.class);
		myDecoratorFactories.put(OASFounded.ID, OASFounded.class);
		myDecoratorFactories.put(PuppetGovernments.ID, PuppetGovernments.class);
		myDecoratorFactories.put(PanamaCanalReturned.ID, PanamaCanalReturned.class);
		myDecoratorFactories.put(ColonialRearGuards.ID, ColonialRearGuards.class);
		myDecoratorFactories.put(MuslimRevolution.ID, MuslimRevolution.class);
		myDecoratorFactories.put(Allende.ID, Allende.class);
		myDecoratorFactories.put(SouthAfricanUnrest.ID, SouthAfricanUnrest.class);
		myDecoratorFactories.put(PortugueseEmpireCrumbles.ID, PortugueseEmpireCrumbles.class);
		myDecoratorFactories.put(IranIraqWar.ID, IranIraqWar.class);
		myDecoratorFactories.put(LoneGunman.ID, LoneGunman.class);
		myDecoratorFactories.put(AnEvilEmpire.ID, AnEvilEmpire.class);
		myDecoratorFactories.put(FlowerPower.ID, FlowerPower.class);
		myDecoratorFactories.put(CentralAmericaScoring.ID, CentralAmericaScoring.class);
		myDecoratorFactories.put(TrumanDoctrine.ID, TrumanDoctrine.class);
		myDecoratorFactories.put(MiddleEastScoring.ID, MiddleEastScoring.class);
		myDecoratorFactories.put(DeStalinization.ID, DeStalinization.class);
		myDecoratorFactories.put(Decolonization.ID, Decolonization.class);
		myDecoratorFactories.put(EastEuropeanUnrest.ID, EastEuropeanUnrest.class);
		myDecoratorFactories.put(SuezCrisis.ID, SuezCrisis.class);
		myDecoratorFactories.put(UsJapanMutualDefensePact.ID, UsJapanMutualDefensePact.class);
		myDecoratorFactories.put(CiaCreated.ID, CiaCreated.class);
		myDecoratorFactories.put(IndoPakistaniWar.ID, IndoPakistaniWar.class);
		myDecoratorFactories.put(MarshallPlan.ID, MarshallPlan.class);
		myDecoratorFactories.put(IndependentReds.ID, IndependentReds.class);
		myDecoratorFactories.put(DeGaulleLeadsFrance.ID, DeGaulleLeadsFrance.class);
		myDecoratorFactories.put(WarsawPactFormed.ID, WarsawPactFormed.class);
		myDecoratorFactories.put(Comecon.ID, Comecon.class);
		myDecoratorFactories.put(RomanianAbdication.ID, RomanianAbdication.class);
		myDecoratorFactories.put(Fidel.ID, Fidel.class);
		myDecoratorFactories.put(SocialistGovernments.ID, SocialistGovernments.class);
		myDecoratorFactories.put(China.ID, China.class);
		myDecoratorFactories.put(Influence.ID, Influence.class);
		myDecoratorFactories.put(ConditionalLabeler.ID, ConditionalLabeler.class);
		myDecoratorFactories.put(TheChinaCard.ID, TheChinaCard.class);
		myDecoratorFactories.put(CardEvent.ID, CardEvent.class);
		myDecoratorFactories.put(ScoringCard.ID, ScoringCard.class);
		myDecoratorFactories.put(AsiaScoring.ID, AsiaScoring.class);
		myDecoratorFactories.put(EuropeScoring.ID, EuropeScoring.class);
		myDecoratorFactories.put(ShuttleDiplomacyRegion.ID, ShuttleDiplomacyRegion.class);
		myDecoratorFactories.put(DuckAndCover.ID, DuckAndCover.class);
		myDecoratorFactories.put(FiveYearPlan.ID, FiveYearPlan.class);
		myDecoratorFactories.put(VietnamRevolts.ID, VietnamRevolts.class);
		myDecoratorFactories.put(Blockade.ID, Blockade.class);
		myDecoratorFactories.put(KoreanWar.ID, KoreanWar.class);
		myDecoratorFactories.put(ArabIsraeliWar.ID, ArabIsraeliWar.class);
		myDecoratorFactories.put(Nasser.ID, Nasser.class);
		myDecoratorFactories.put(CapturedNaziScientist.ID, CapturedNaziScientist.class);
		myDecoratorFactories.put(OlympicGames.ID, OlympicGames.class);
		myDecoratorFactories.put(Nato.ID, Nato.class);
		myDecoratorFactories.put(BiWar.ID, BiWar.class);
		myDecoratorFactories.put(RevealHandEvent.ID, RevealHandEvent.class);
		myDecoratorFactories.put(UnIntervention.ID, UnIntervention.class);
		myDecoratorFactories.put(NuclearTestBan.ID, NuclearTestBan.class);
		myDecoratorFactories.put(Defectors.ID, Defectors.class);
		myDecoratorFactories.put(TheCambridgeFive.ID, TheCambridgeFive.class);
		myDecoratorFactories.put(SpecialRelationship.ID, SpecialRelationship.class);
		myDecoratorFactories.put(Norad.ID, Norad.class);
		myDecoratorFactories.put(BrushWar.ID, BrushWar.class);
		myDecoratorFactories.put(SouthEastAsiaScoring.ID, SouthEastAsiaScoring.class);
		myDecoratorFactories.put(ArmsRace.ID, ArmsRace.class);
		myDecoratorFactories.put(CubanMissileCrisis.ID, CubanMissileCrisis.class);
		myDecoratorFactories.put(Quagmire.ID, Quagmire.class);
		myDecoratorFactories.put(SaltNegotiations.ID, SaltNegotiations.class);
		myDecoratorFactories.put(BearTrap.ID, BearTrap.class);
		myDecoratorFactories.put(Summit.ID, Summit.class);
		myDecoratorFactories.put(HowILearnedToStopWorrying.ID, HowILearnedToStopWorrying.class);
		myDecoratorFactories.put(Junta.ID, Junta.class);
		myDecoratorFactories.put(KitchenDebates.ID, KitchenDebates.class);
		myDecoratorFactories.put(MissileEnvy.ID, MissileEnvy.class);
		myDecoratorFactories.put(WeWillBuryYou.ID, WeWillBuryYou.class);
		myDecoratorFactories.put(WillyBrandt.ID, WillyBrandt.class);
		myDecoratorFactories.put(AbmTreaty.ID, AbmTreaty.class);
		myDecoratorFactories.put(CulturalRevolution.ID, CulturalRevolution.class);
		myDecoratorFactories.put(U2Incident.ID, U2Incident.class);
		myDecoratorFactories.put(Opec.ID, Opec.class);
		myDecoratorFactories.put(CampDavidAccords.ID, CampDavidAccords.class);
		myDecoratorFactories.put(GrainSalesToSoviets.ID, GrainSalesToSoviets.class);
		myDecoratorFactories.put(NixonPlaysTheChinaCard.ID, NixonPlaysTheChinaCard.class);
		myDecoratorFactories.put(ShuttleDiplomacy.ID, ShuttleDiplomacy.class);
		myDecoratorFactories.put(UssuriRiverSkirmish.ID, UssuriRiverSkirmish.class);
		myDecoratorFactories.put(AskNotWhatYourCountryCanDoForYou.ID, AskNotWhatYourCountryCanDoForYou.class);
		myDecoratorFactories.put(AllianceForProgress.ID, AllianceForProgress.class);
		myDecoratorFactories.put(OneSmallStep.ID, OneSmallStep.class);
		myDecoratorFactories.put(Che.ID, Che.class);
		myDecoratorFactories.put(OurManInTehran.ID, OurManInTehran.class);
		myDecoratorFactories.put(TheIronLady.ID, TheIronLady.class);
		myDecoratorFactories.put(ReaganBombsLibya.ID, ReaganBombsLibya.class);
		myDecoratorFactories.put(StarWars.ID, StarWars.class);
		myDecoratorFactories.put(SovietsShootDownKal007.ID, SovietsShootDownKal007.class);
		myDecoratorFactories.put(OrtegaElectedInNicaragua.ID, OrtegaElectedInNicaragua.class);
		myDecoratorFactories.put(Terrorism.ID, Terrorism.class);
		myDecoratorFactories.put(Chernobyl.ID, Chernobyl.class);
		myDecoratorFactories.put(LatinAmericanDebtCrisis.ID, LatinAmericanDebtCrisis.class);
		myDecoratorFactories.put(TearDownThisWall.ID, TearDownThisWall.class);
		myDecoratorFactories.put(AldrichAmesRemix.ID, AldrichAmesRemix.class);
		myDecoratorFactories.put(RedScarePurge.ID, RedScarePurge.class);
		myDecoratorFactories.put(FormosanResolution.ID, FormosanResolution.class);
		myDecoratorFactories.put(Wargames.ID, Wargames.class);
		myDecoratorFactories.put(Solidarity.ID, Solidarity.class);
		myDecoratorFactories.put(Glasnost.ID, Glasnost.class);
		myDecoratorFactories.put(Containment.ID, Containment.class);
		myDecoratorFactories.put(BrezhnevDoctrine.ID, BrezhnevDoctrine.class);
	}

	@Override
	public Decorator createDecorator(final String type, final GamePiece inner) {
	    Decorator d = null;
	    String prefix = type.substring(0, type.indexOf(';')+1);
	    if (prefix.length() == 0)
	      prefix = type;
	    final Class<?> f = myDecoratorFactories.get(prefix);
	    if (f != null) {
	    	try {
	    		final Constructor<?> constructor = f.getConstructor(String.class, GamePiece.class);
				d = (Decorator) constructor.newInstance(type, inner);
			} catch (Exception e) {
				ErrorDialog.bug(e);
			}
	    	return d;
	    } 
	    
	    d = super.createDecorator(type,inner);
	    if (!d.getType().startsWith(prefix)) {
	    	JOptionPane.showMessageDialog(GameModule.getGameModule().getFrame(), 
	    			"You are attempting to synchronize with an incompatible module version.\nVASSAL will now exit to protect the ongoing game.", 
	    			"Incompatible Module Versions", 
	    			JOptionPane.ERROR_MESSAGE);
	    	System.exit(0);
	    }
	    return d;
	}
}