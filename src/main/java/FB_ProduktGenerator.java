public class FB_ProduktGenerator {
    public int nProduktTyp;
    public boolean bNeuesProdukt;

    private int nRandCounter;
    private final TON tonIntervall = new TON();

    public void update(boolean bBandLaeuft, boolean bBereit, long tIntervallMs) {
        // Jeden Zyklus hochzählen: 0..2 rollierend
        nRandCounter = nRandCounter + 1;
        if (nRandCounter > 2) {
            nRandCounter = 0;
        }

        // Self-Resetting TON
        tonIntervall.update(
                bBandLaeuft && bBereit && !tonIntervall.Q,
                tIntervallMs
        );

        bNeuesProdukt = tonIntervall.Q && bBereit;

        if (tonIntervall.Q) {
            nProduktTyp = nRandCounter + 1; // 1, 2 oder 3
        }
    }

    public void reset() {
        nProduktTyp = 0;
        bNeuesProdukt = false;
        nRandCounter = 0;
        tonIntervall.reset();
    }
}
