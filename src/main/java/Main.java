
import org.fusesource.jansi.AnsiConsole;

public class Main {
    public static void main(String[] args) throws Exception {
        AnsiConsole.systemInstall();

        GVL GVL = new GVL();
        PLC_PRG plc_prg = new PLC_PRG(GVL);
        OpcUaSimulationServer opcUaServer = new OpcUaSimulationServer();
        opcUaServer.startup().get();

        GVL.g_nGeschwindigkeit = 5;

        System.out.println("OPC UA Server laeuft auf: opc.tcp://localhost:4840/OPCUA_HMI");
        System.out.print("\033[2J");
        long lastConsoleUpdateMs = 0;

        while (true) {
            opcUaServer.readCommandsInto(GVL);
            plc_prg.cycle();
            opcUaServer.writeStateFrom(GVL);

            long nowMs = System.currentTimeMillis();
            if (nowMs - lastConsoleUpdateMs >= 100) {
                System.out.print("\033[H");
                System.out.print(formatBlockStatus(GVL));
                System.out.flush();
                lastConsoleUpdateMs = nowMs;
            }

            Thread.sleep(20);
        }
    }

    private static String formatBlockStatus(GVL GVL) {
        return String.format("""
                ========== ANLAGENSTATUS ==========
                Band laeuft:        %-5s
                Band stop:          %-5s
                Automatikbetrieb:   %-5s
                Geschwindigkeit:    %-5d

                Produkt aktiv:      %-5s
                Produkt Typ:        %-5d
                Produkt A erkannt:  %-5s
                Produkt B erkannt:  %-5s
                Ausschuss erkannt:  %-5s

                Block sichtbar:     %-5s
                Block Label:        %-5s
                Block X/Y:          %d / %d

                Weiche 1:           %-5s
                Weiche 2:           %-5s

                Sensor Einlauf:     %-5s
                Sensor Typ A:       %-5s
                Sensor Typ B:       %-5s
                Sensor Ausschuss:   %-5s
                Sensor Auslauf:     %-5s

                Lampe Betrieb:      %-5s
                Lampe Band steht:   %-5s
                Lampe Produkt A:    %-5s
                Lampe Produkt B:    %-5s
                Lampe Ausschuss:    %-5s
                Lampe Weiche 1:     %-5s
                Lampe Weiche 2:     %-5s

                Zaehler A:          %-5d
                Zaehler B:          %-5d
                Zaehler Ausschuss:  %-5d
                Zaehler Gesamt:     %-5d
                ===================================
                """,
                GVL.g_bBandLaeuft,
                GVL.g_bBandStop,
                GVL.g_bAutomatikbetrieb,
                GVL.g_nGeschwindigkeit,
                GVL.g_bProduktAktiv,
                GVL.g_nProduktTyp,
                GVL.g_bProduktAErkannt,
                GVL.g_bProduktBErkannt,
                GVL.g_bAusschussErkannt,
                GVL.g_bBlockVisible,
                GVL.g_sBlockLabel,
                GVL.g_nBlockPosX,
                GVL.g_nBlockPosY,
                GVL.g_bWeiche1,
                GVL.g_bWeiche2,
                GVL.g_bSensor_Einlauf,
                GVL.g_bSensor_TypA,
                GVL.g_bSensor_TypB,
                GVL.g_bSensor_Ausschuss,
                GVL.g_bSensor_Auslauf,
                GVL.g_bLampe_Betrieb,
                GVL.g_bLampe_BandSteht,
                GVL.g_bLampe_ProduktA,
                GVL.g_bLampe_ProduktB,
                GVL.g_bLampe_Ausschuss,
                GVL.g_bLampe_Weiche1,
                GVL.g_bLampe_Weiche2,
                GVL.g_nZaehler_A,
                GVL.g_nZaehler_B,
                GVL.g_nZaehler_Aus,
                GVL.g_nZaehler_Gesamt);
    }
}
