public class GVL {
    // Steuerung
    public boolean g_bStart;
    public boolean g_bStop;
    public boolean g_bBandLaeuft;
    public boolean g_bBandStop;
    public boolean g_bZaehlerReset;
    public boolean g_bSystemReset;
    public boolean g_bAutomatikbetrieb = true;
    public boolean g_bHand_Weiche1;
    public boolean g_bHand_Weiche2;

    // Aktuelles Produkt
    public int g_nProduktTyp;          // 0=keins, 1=TypA, 2=TypB, 3=Ausschuss
    public boolean g_bProduktAktiv;    // Produkt gerade unterwegs

    // Weichen-Status
    public boolean g_bWeiche1;
    public boolean g_bWeiche2;

    // Sensoren
    public boolean g_bSensor_Einlauf;
    public boolean g_bSensor_TypA;
    public boolean g_bSensor_TypB;
    public boolean g_bSensor_Ausschuss;
    public boolean g_bSensor_Auslauf;

    // Erkennung
    public boolean g_bProduktAErkannt;
    public boolean g_bProduktBErkannt;
    public boolean g_bAusschussErkannt;

    // Lampen
    public boolean g_bLampe_Betrieb;
    public boolean g_bLampe_BandSteht;
    public boolean g_bLampe_ProduktA;
    public boolean g_bLampe_ProduktB;
    public boolean g_bLampe_Ausschuss;
    public boolean g_bLampe_Weiche1;
    public boolean g_bLampe_Weiche2;
    public boolean g_bLampe_Automatikbetrieb;
    public boolean g_bLampe_Handbetrieb;

    // Zähler
    public int g_nZaehler_A;
    public int g_nZaehler_B;
    public int g_nZaehler_Aus;
    public int g_nZaehler_Gesamt;

    // Animation
    public int g_nBlockPosX;
    public int g_nBlockPosY;
    public boolean g_bBlockVisible;
    public String g_sBlockLabel = "";
    public int g_nGeschwindigkeit;
}
