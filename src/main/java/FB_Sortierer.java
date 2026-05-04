public class FB_Sortierer {
    public int nZaehler_A;
    public int nZaehler_B;
    public int nZaehler_Aus;
    public int nZaehler_Gesamt;

    public boolean bWeiche1;
    public boolean bWeiche2;

    private final R_TRIG rTrig = new R_TRIG();

    public void update(boolean bAnkunft, int nAnkunftTyp, boolean bReset) {
        rTrig.update(bAnkunft);

        if (bReset) {
            nZaehler_A = 0;
            nZaehler_B = 0;
            nZaehler_Aus = 0;
            nZaehler_Gesamt = 0;
            bWeiche1 = false;
            bWeiche2 = false;
        }

        else if (rTrig.Q) {
            bWeiche1 = false;
            bWeiche2 = false;

            switch (nAnkunftTyp) {
                case 1 -> nZaehler_A = nZaehler_A + 1;
                case 2 -> nZaehler_B = nZaehler_B + 1;
                case 3 -> nZaehler_Aus = nZaehler_Aus + 1;
                default -> {
                    // unbekannter Typ
                }
            }

            nZaehler_Gesamt =
                    nZaehler_A +
                            nZaehler_B +
                            nZaehler_Aus;
        }
    }
}