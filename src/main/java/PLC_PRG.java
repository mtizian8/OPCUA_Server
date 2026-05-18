public class PLC_PRG {
    private final GVL GVL;

    private final FB_Band fbBand = new FB_Band();
    private final FB_ProduktGenerator fbGenerator = new FB_ProduktGenerator();
    private final FB_Animation fbAnimation = new FB_Animation();
    private final FB_Sortierer fbSortierer = new FB_Sortierer();

    public PLC_PRG(GVL GVL) {
        this.GVL = GVL;
    }

    public void cycle() {
        if (GVL.g_bSystemReset) {
            reset();
            return;
        }

        // 1. Band
        fbBand.update(
                GVL.g_bStart,
                GVL.g_bStop
        );

        GVL.g_bBandLaeuft = fbBand.bLaeuft;
        GVL.g_bBandStop = !fbBand.bLaeuft;

        // 2. Generator
        long tIntervallMs =
                5200L - (GVL.g_nGeschwindigkeit * 500L);

        if (tIntervallMs < 200L) {
            tIntervallMs = 200L;
        }

        fbGenerator.update(
                GVL.g_bBandLaeuft,
                !fbAnimation.bVisible,
                tIntervallMs
        );

        GVL.g_nProduktTyp = fbGenerator.nProduktTyp;
        GVL.g_bProduktAktiv = fbGenerator.bNeuesProdukt;

        // 3. Animation
        fbAnimation.update(
                GVL.g_bBandLaeuft,
                fbGenerator.bNeuesProdukt,
                fbGenerator.nProduktTyp,
                GVL.g_nGeschwindigkeit,
                GVL.g_bAutomatikbetrieb,
                GVL.g_bHand_Weiche1,
                GVL.g_bHand_Weiche2
        );

        GVL.g_nBlockPosX = fbAnimation.nPosX;
        GVL.g_nBlockPosY = fbAnimation.nPosY;
        GVL.g_bBlockVisible = fbAnimation.bVisible;
        GVL.g_bProduktAktiv = fbAnimation.bVisible;
        GVL.g_sBlockLabel = fbAnimation.sLabel;
        GVL.g_bWeiche1 = fbAnimation.bWeiche1;
        GVL.g_bWeiche2 = fbAnimation.bWeiche2;
        GVL.g_bSensor_Einlauf = fbAnimation.bSensor_Einlauf;
        GVL.g_bSensor_TypA = fbAnimation.bSensor_TypA;
        GVL.g_bSensor_TypB = fbAnimation.bSensor_TypB;
        GVL.g_bSensor_Ausschuss = fbAnimation.bSensor_Ausschuss;
        GVL.g_bSensor_Auslauf = fbAnimation.bSensor_Auslauf;
        GVL.g_bProduktAErkannt = fbAnimation.bProduktAErkannt;
        GVL.g_bProduktBErkannt = fbAnimation.bProduktBErkannt;
        GVL.g_bAusschussErkannt = fbAnimation.bAusschussErkannt;
        GVL.g_bLampe_Betrieb = GVL.g_bBandLaeuft;
        GVL.g_bLampe_BandSteht = !GVL.g_bBandLaeuft;
        GVL.g_bLampe_ProduktA = GVL.g_bProduktAErkannt;
        GVL.g_bLampe_ProduktB = GVL.g_bProduktBErkannt;
        GVL.g_bLampe_Ausschuss = GVL.g_bAusschussErkannt;
        GVL.g_bLampe_Weiche1 = GVL.g_bWeiche1;
        GVL.g_bLampe_Weiche2 = GVL.g_bWeiche2;
        GVL.g_bLampe_Automatikbetrieb = GVL.g_bAutomatikbetrieb;
        GVL.g_bLampe_Handbetrieb = !GVL.g_bAutomatikbetrieb;

        // 4. Sortierer
        fbSortierer.update(
                fbAnimation.bAnkunft,
                fbAnimation.nAnkunftTyp,
                GVL.g_bZaehlerReset
        );

        GVL.g_nZaehler_A = fbSortierer.nZaehler_A;
        GVL.g_nZaehler_B = fbSortierer.nZaehler_B;
        GVL.g_nZaehler_Aus = fbSortierer.nZaehler_Aus;
        GVL.g_nZaehler_Gesamt = fbSortierer.nZaehler_Gesamt;
    }

    private void reset() {
        fbBand.reset();
        fbGenerator.reset();
        fbAnimation.reset();
        fbSortierer.reset();

        GVL.g_bStart = false;
        GVL.g_bStop = false;
        GVL.g_bBandLaeuft = false;
        GVL.g_bBandStop = true;
        GVL.g_bZaehlerReset = false;
        GVL.g_bSystemReset = false;
        GVL.g_bAutomatikbetrieb = true;
        GVL.g_bHand_Weiche1 = false;
        GVL.g_bHand_Weiche2 = false;

        GVL.g_nProduktTyp = 0;
        GVL.g_bProduktAktiv = false;
        GVL.g_bWeiche1 = false;
        GVL.g_bWeiche2 = false;
        GVL.g_bSensor_Einlauf = false;
        GVL.g_bSensor_TypA = false;
        GVL.g_bSensor_TypB = false;
        GVL.g_bSensor_Ausschuss = false;
        GVL.g_bSensor_Auslauf = false;
        GVL.g_bProduktAErkannt = false;
        GVL.g_bProduktBErkannt = false;
        GVL.g_bAusschussErkannt = false;

        GVL.g_bLampe_Betrieb = false;
        GVL.g_bLampe_BandSteht = true;
        GVL.g_bLampe_ProduktA = false;
        GVL.g_bLampe_ProduktB = false;
        GVL.g_bLampe_Ausschuss = false;
        GVL.g_bLampe_Weiche1 = false;
        GVL.g_bLampe_Weiche2 = false;
        GVL.g_bLampe_Automatikbetrieb = true;
        GVL.g_bLampe_Handbetrieb = false;

        GVL.g_nZaehler_A = 0;
        GVL.g_nZaehler_B = 0;
        GVL.g_nZaehler_Aus = 0;
        GVL.g_nZaehler_Gesamt = 0;
        GVL.g_nBlockPosX = 0;
        GVL.g_nBlockPosY = 0;
        GVL.g_bBlockVisible = false;
        GVL.g_sBlockLabel = "";
        GVL.g_nGeschwindigkeit = 5;
    }
}
