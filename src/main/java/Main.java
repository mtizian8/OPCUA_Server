
import org.fusesource.jansi.AnsiConsole;

public class Main {
    public static void main(String[] args) throws Exception {
        AnsiConsole.systemInstall();

        GVL GVL = new GVL();
        PLC_PRG plc_prg = new PLC_PRG(GVL);
        OpcUaSimulationServer opcUaServer = new OpcUaSimulationServer();
        opcUaServer.startup().get();

        GVL.g_nGeschwindigkeit = 5;

        System.out.println("OPC UA server running at: opc.tcp://localhost:4840/OPCUA_HMI");
        System.out.print("\033[2J");
        long lastConsoleUpdateMs = 0;
        final long updateIntervalMs = 20L;

        while (true) {
            long cycleStartMs = System.currentTimeMillis();

            opcUaServer.readCommandsInto(GVL);
            plc_prg.cycle();
            opcUaServer.writeStateFrom(GVL);
            opcUaServer.writeSystemInfo(System.currentTimeMillis() - cycleStartMs, updateIntervalMs, true);

            long nowMs = System.currentTimeMillis();
            if (nowMs - lastConsoleUpdateMs >= 100) {
                System.out.print("\033[H");
                System.out.print(formatBlockStatus(GVL));
                System.out.flush();
                lastConsoleUpdateMs = nowMs;
            }

            Thread.sleep(updateIntervalMs);
        }
    }

    private static String formatBlockStatus(GVL GVL) {
        return String.format("""
                ========== SYSTEM STATUS ==========
                Conveyor running:   %-5s
                Conveyor stopped:   %-5s
                Automatic mode:     %-5s
                Speed:              %-5d

                Product active:     %-5s
                Product type:       %-5d
                Product A detected: %-5s
                Product B detected: %-5s
                Reject detected:    %-5s

                Block visible:      %-5s
                Block label:        %-5s
                Block X/Y:          %d / %d

                Diverter 1:         %-5s
                Diverter 2:         %-5s

                Sensor infeed:      %-5s
                Sensor product A:   %-5s
                Sensor product B:   %-5s
                Sensor reject:      %-5s
                Sensor outfeed:     %-5s

                Lamp operation:     %-5s
                Lamp stopped:       %-5s
                Lamp product A:     %-5s
                Lamp product B:     %-5s
                Lamp reject:        %-5s
                Lamp diverter 1:    %-5s
                Lamp diverter 2:    %-5s
                Lamp automatic:     %-5s
                Lamp manual:        %-5s

                Counter A:          %-5d
                Counter B:          %-5d
                Counter reject:     %-5d
                Counter total:      %-5d
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
                GVL.g_bLampe_Automatikbetrieb,
                GVL.g_bLampe_Handbetrieb,
                GVL.g_nZaehler_A,
                GVL.g_nZaehler_B,
                GVL.g_nZaehler_Aus,
                GVL.g_nZaehler_Gesamt);
    }
}
