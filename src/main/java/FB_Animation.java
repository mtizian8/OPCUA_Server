public class FB_Animation {
    public int nPosX;
    public int nPosY;
    public boolean bVisible;
    public String sLabel = "";

    public boolean bWeiche1;
    public boolean bWeiche2;
    public boolean bSensor_Einlauf;
    public boolean bSensor_TypA;
    public boolean bSensor_TypB;
    public boolean bSensor_Ausschuss;
    public boolean bSensor_Auslauf;

    public boolean bProduktAErkannt;
    public boolean bProduktBErkannt;
    public boolean bAusschussErkannt;

    public boolean bAnkunft;
    public int nAnkunftTyp;

    private int nTyp;
    private int nPhase;

    private long tTaktMs;
    private boolean bSensorEinlaufLatched;
    private boolean bSensorTypALatched;
    private boolean bSensorTypBLatched;
    private boolean bSensorAusschussLatched;
    private boolean bSensorAuslaufLatched;

    private final R_TRIG rTrig = new R_TRIG();
    private final TON tonTick = new TON();

    public void update(
            boolean bBandLaeuft,
            boolean bNeuesProdukt,
            int nProduktTyp,
            int nGeschwindigkeit,
            boolean bAutomatikbetrieb,
            boolean bHand_Weiche1,
            boolean bHand_Weiche2
    ) {
        bAnkunft = false;

        rTrig.update(bNeuesProdukt);

        // Neues Produkt starten
        if (rTrig.Q && !bVisible) {
            nTyp = nProduktTyp;
            nPosX = 113;
            nPosY = 409;
            bVisible = true;
            nPhase = 1;
            bSensorEinlaufLatched = false;
            bSensorTypALatched = false;
            bSensorTypBLatched = false;
            bSensorAusschussLatched = false;
            bSensorAuslaufLatched = false;

            switch (nTyp) {
                case 1 -> sLabel = "A";
                case 2 -> sLabel = "B";
                case 3 -> sLabel = "C";
                default -> sLabel = "";
            }
        }

        tTaktMs = 220L - (nGeschwindigkeit * 20L);

        if (tTaktMs < 20L) {
            tTaktMs = 20L;
        }

        tonTick.update(
                bVisible && bBandLaeuft && !tonTick.Q,
                tTaktMs
        );

        updateSensorLatches();

        boolean bWeiche1Aktiv = bAutomatikbetrieb
                ? bVisible && nTyp == 1 && (bSensorTypALatched || nPhase == 2)
                : bHand_Weiche1;

        boolean bWeiche2Aktiv = bAutomatikbetrieb
                ? bVisible && nTyp == 2 && (bSensorTypBLatched || nPhase == 2)
                : bHand_Weiche2;

        if (tonTick.Q) {
            switch (nPhase) {
                case 1 -> {
                    // Horizontal fahren
                    nPosX = nPosX + 12;

                    if (bWeiche1Aktiv && nPosX >= 266) {
                        nPhase = 2;
                    }

                    else if (bWeiche2Aktiv && nPosX >= 494) {
                        nPhase = 2;
                    }

                    // Ausschuss: fährt bis rechts raus
                    else if (nTyp == 3 && nPosX >= 684) {
                        bVisible = false;
                        nPhase = 0;
                        bAnkunft = true;
                        nAnkunftTyp = 3;
                        bSensorAusschussLatched = true;
                    }
                }

                case 2 -> {
                    // Vertikal hochfahren
                    nPosY = nPosY - 12;

                    if (nPosY <= 140) {
                        bVisible = false;
                        nPhase = 0;
                        bAnkunft = true;
                        nAnkunftTyp = nTyp;
                        bSensorAuslaufLatched = true;
                    } else {
                        bAnkunft = false;
                    }
                }

                default -> {
                    // Keine aktive Phase
                }
            }
        }

        updateSensorLatches();

        bWeiche1 = bAutomatikbetrieb
                ? bVisible && nTyp == 1 && (bSensorTypALatched || nPhase == 2)
                : bHand_Weiche1;

        bWeiche2 = bAutomatikbetrieb
                ? bVisible && nTyp == 2 && (bSensorTypBLatched || nPhase == 2)
                : bHand_Weiche2;

        bProduktAErkannt = bVisible && bSensorTypALatched;
        bProduktBErkannt = bVisible && bSensorTypBLatched;
        bAusschussErkannt = bVisible && bSensorAusschussLatched;
        updateLiveSensors();
    }

    private void updateSensorLatches() {
        if (!bVisible) {
            return;
        }

        if (nPhase == 1 && nPosX >= 113) {
            bSensorEinlaufLatched = true;
        }

        if (nTyp == 1 && nPosX >= 220) {
            bSensorTypALatched = true;
        }

        if (nTyp == 2 && nPosX >= 445) {
            bSensorTypBLatched = true;
        }

        if (nTyp == 3 && nPosX >= 620) {
            bSensorAusschussLatched = true;
        }

        if (nPhase == 2 && nPosY <= 200) {
            bSensorAuslaufLatched = true;
        }
    }

    private void updateLiveSensors() {
        bSensor_Einlauf = bVisible && nPhase == 1 && isInRange(nPosX, 113, 137);
        bSensor_TypA = bVisible && nTyp == 1 && isInRange(nPosX, 220, 244);
        bSensor_TypB = bVisible && nTyp == 2 && isInRange(nPosX, 445, 469);
        bSensor_Ausschuss = bVisible && nTyp == 3 && isInRange(nPosX, 620, 644);
        bSensor_Auslauf = bVisible && nPhase == 2 && isInRange(nPosY, 176, 200);
    }

    private boolean isInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }

    public void reset() {
        nPosX = 0;
        nPosY = 0;
        bVisible = false;
        sLabel = "";
        bWeiche1 = false;
        bWeiche2 = false;
        bSensor_Einlauf = false;
        bSensor_TypA = false;
        bSensor_TypB = false;
        bSensor_Ausschuss = false;
        bSensor_Auslauf = false;
        bProduktAErkannt = false;
        bProduktBErkannt = false;
        bAusschussErkannt = false;
        bAnkunft = false;
        nAnkunftTyp = 0;
        nTyp = 0;
        nPhase = 0;
        tTaktMs = 0;
        bSensorEinlaufLatched = false;
        bSensorTypALatched = false;
        bSensorTypBLatched = false;
        bSensorAusschussLatched = false;
        bSensorAuslaufLatched = false;
        rTrig.reset();
        tonTick.reset();
    }
}
