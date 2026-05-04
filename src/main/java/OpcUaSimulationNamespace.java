import org.eclipse.milo.opcua.sdk.core.AccessLevel;
import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.DataItem;
import org.eclipse.milo.opcua.sdk.server.api.ManagedNamespaceWithLifecycle;
import org.eclipse.milo.opcua.sdk.server.api.MonitoredItem;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OpcUaSimulationNamespace extends ManagedNamespaceWithLifecycle {
    public static final String NAMESPACE_URI = "urn:opcua-hmi:simulation";

    private final Map<String, UaVariableNode> nodes = new LinkedHashMap<>();

    public OpcUaSimulationNamespace(OpcUaServer server) {
        super(server, NAMESPACE_URI);
        getLifecycleManager().addStartupTask(this::createNodes);
    }

    public void readCommandsInto(GVL gvl) {
        gvl.g_bStart = readBoolean("Commands/Start");
        gvl.g_bStop = readBoolean("Commands/Stop");
        gvl.g_bZaehlerReset = readBoolean("Commands/ZaehlerReset");
        gvl.g_nGeschwindigkeit = clamp(readInt("Commands/Geschwindigkeit"), 0, 10);
        gvl.g_bAutomatikbetrieb = readBoolean("Commands/Automatikbetrieb");
        gvl.g_bHand_Weiche1 = readBoolean("Commands/Hand_Weiche1");
        gvl.g_bHand_Weiche2 = readBoolean("Commands/Hand_Weiche2");
    }

    public void writeStateFrom(GVL gvl) {
        write("Status/BandLaeuft", gvl.g_bBandLaeuft);
        write("Status/BandStop", gvl.g_bBandStop);
        write("Status/ProduktTyp", gvl.g_nProduktTyp);
        write("Status/ProduktAktiv", gvl.g_bProduktAktiv);
        write("Status/Weiche1", gvl.g_bWeiche1);
        write("Status/Weiche2", gvl.g_bWeiche2);
        write("Status/Automatikbetrieb", gvl.g_bAutomatikbetrieb);
        write("Status/ProduktAErkannt", gvl.g_bProduktAErkannt);
        write("Status/ProduktBErkannt", gvl.g_bProduktBErkannt);
        write("Status/AusschussErkannt", gvl.g_bAusschussErkannt);
        write("Sensors/Sensor_Einlauf", gvl.g_bSensor_Einlauf);
        write("Sensors/Sensor_TypA", gvl.g_bSensor_TypA);
        write("Sensors/Sensor_TypB", gvl.g_bSensor_TypB);
        write("Sensors/Sensor_Ausschuss", gvl.g_bSensor_Ausschuss);
        write("Sensors/Sensor_Auslauf", gvl.g_bSensor_Auslauf);
        write("Lamps/Lampe_Betrieb", gvl.g_bLampe_Betrieb);
        write("Lamps/Lampe_BandSteht", gvl.g_bLampe_BandSteht);
        write("Lamps/Lampe_ProduktA", gvl.g_bLampe_ProduktA);
        write("Lamps/Lampe_ProduktB", gvl.g_bLampe_ProduktB);
        write("Lamps/Lampe_Ausschuss", gvl.g_bLampe_Ausschuss);
        write("Lamps/Lampe_Weiche1", gvl.g_bLampe_Weiche1);
        write("Lamps/Lampe_Weiche2", gvl.g_bLampe_Weiche2);
        write("Counters/Zaehler_A", gvl.g_nZaehler_A);
        write("Counters/Zaehler_B", gvl.g_nZaehler_B);
        write("Counters/Zaehler_Aus", gvl.g_nZaehler_Aus);
        write("Counters/Zaehler_Gesamt", gvl.g_nZaehler_Gesamt);
        write("Animation/BlockPosX", gvl.g_nBlockPosX);
        write("Animation/BlockPosY", gvl.g_nBlockPosY);
        write("Animation/BlockVisible", gvl.g_bBlockVisible);
        write("Animation/BlockLabel", gvl.g_sBlockLabel);

        write("Commands/ZaehlerReset", false);
    }

    private void createNodes() {
        UaFolderNode root = folder("Simulation");
        getNodeManager().addNode(root);
        root.addReference(new Reference(
                root.getNodeId(),
                Identifiers.Organizes,
                Identifiers.ObjectsFolder.expanded(),
                false
        ));

        UaFolderNode commands = childFolder(root, "Commands");
        UaFolderNode status = childFolder(root, "Status");
        UaFolderNode sensors = childFolder(root, "Sensors");
        UaFolderNode lamps = childFolder(root, "Lamps");
        UaFolderNode counters = childFolder(root, "Counters");
        UaFolderNode animation = childFolder(root, "Animation");

        variable(commands, "Start", Identifiers.Boolean, false, AccessLevel.READ_WRITE);
        variable(commands, "Stop", Identifiers.Boolean, false, AccessLevel.READ_WRITE);
        variable(commands, "ZaehlerReset", Identifiers.Boolean, false, AccessLevel.READ_WRITE);
        variable(commands, "Geschwindigkeit", Identifiers.Int32, 5, AccessLevel.READ_WRITE);
        variable(commands, "Automatikbetrieb", Identifiers.Boolean, true, AccessLevel.READ_WRITE);
        variable(commands, "Hand_Weiche1", Identifiers.Boolean, false, AccessLevel.READ_WRITE);
        variable(commands, "Hand_Weiche2", Identifiers.Boolean, false, AccessLevel.READ_WRITE);

        variable(status, "BandLaeuft", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(status, "BandStop", Identifiers.Boolean, true, AccessLevel.READ_ONLY);
        variable(status, "ProduktTyp", Identifiers.Int32, 0, AccessLevel.READ_ONLY);
        variable(status, "ProduktAktiv", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(status, "Weiche1", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(status, "Weiche2", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(status, "Automatikbetrieb", Identifiers.Boolean, true, AccessLevel.READ_ONLY);
        variable(status, "ProduktAErkannt", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(status, "ProduktBErkannt", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(status, "AusschussErkannt", Identifiers.Boolean, false, AccessLevel.READ_ONLY);

        variable(sensors, "Sensor_Einlauf", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(sensors, "Sensor_TypA", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(sensors, "Sensor_TypB", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(sensors, "Sensor_Ausschuss", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(sensors, "Sensor_Auslauf", Identifiers.Boolean, false, AccessLevel.READ_ONLY);

        variable(lamps, "Lampe_Betrieb", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(lamps, "Lampe_BandSteht", Identifiers.Boolean, true, AccessLevel.READ_ONLY);
        variable(lamps, "Lampe_ProduktA", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(lamps, "Lampe_ProduktB", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(lamps, "Lampe_Ausschuss", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(lamps, "Lampe_Weiche1", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(lamps, "Lampe_Weiche2", Identifiers.Boolean, false, AccessLevel.READ_ONLY);

        variable(counters, "Zaehler_A", Identifiers.Int32, 0, AccessLevel.READ_ONLY);
        variable(counters, "Zaehler_B", Identifiers.Int32, 0, AccessLevel.READ_ONLY);
        variable(counters, "Zaehler_Aus", Identifiers.Int32, 0, AccessLevel.READ_ONLY);
        variable(counters, "Zaehler_Gesamt", Identifiers.Int32, 0, AccessLevel.READ_ONLY);

        variable(animation, "BlockPosX", Identifiers.Int32, 0, AccessLevel.READ_ONLY);
        variable(animation, "BlockPosY", Identifiers.Int32, 0, AccessLevel.READ_ONLY);
        variable(animation, "BlockVisible", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(animation, "BlockLabel", Identifiers.String, "", AccessLevel.READ_ONLY);
    }

    private UaFolderNode childFolder(UaFolderNode parent, String name) {
        UaFolderNode folder = folder("Simulation/" + name);
        getNodeManager().addNode(folder);
        parent.addOrganizes(folder);
        return folder;
    }

    private UaFolderNode folder(String path) {
        String displayName = path.substring(path.lastIndexOf('/') + 1);
        return new UaFolderNode(
                getNodeContext(),
                newNodeId(path),
                newQualifiedName(displayName),
                LocalizedText.english(displayName)
        );
    }

    private void variable(UaFolderNode folder, String name, NodeId dataType, Object initialValue, Set<AccessLevel> accessLevel) {
        String path = folder.getNodeId().getIdentifier() + "/" + name;
        UaVariableNode node = UaVariableNode.builder(getNodeContext())
                .setNodeId(newNodeId(path))
                .setAccessLevel(accessLevel)
                .setUserAccessLevel(accessLevel)
                .setBrowseName(newQualifiedName(name))
                .setDisplayName(LocalizedText.english(name))
                .setDataType(dataType)
                .setTypeDefinition(Identifiers.BaseDataVariableType)
                .build();

        node.setValue(new DataValue(new Variant(initialValue)));
        getNodeManager().addNode(node);
        folder.addOrganizes(node);
        nodes.put(path.replace("Simulation/", ""), node);
    }

    private boolean readBoolean(String path) {
        Object value = value(path);
        return value instanceof Boolean && (Boolean) value;
    }

    private int readInt(String path) {
        Object value = value(path);
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    private Object value(String path) {
        UaVariableNode node = nodes.get(path);
        if (node == null || node.getValue() == null || node.getValue().getValue() == null) {
            return null;
        }
        return node.getValue().getValue().getValue();
    }

    private void write(String path, Object value) {
        UaVariableNode node = nodes.get(path);
        if (node != null) {
            node.setValue(new DataValue(new Variant(value)));
        }
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public void onDataItemsCreated(List<DataItem> dataItems) {
    }

    @Override
    public void onDataItemsModified(List<DataItem> dataItems) {
    }

    @Override
    public void onDataItemsDeleted(List<DataItem> dataItems) {
    }

    @Override
    public void onMonitoringModeChanged(List<MonitoredItem> monitoredItems) {
    }
}
