import org.eclipse.milo.opcua.sdk.core.AccessLevel;
import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.DataItem;
import org.eclipse.milo.opcua.sdk.server.api.ManagedNamespaceWithLifecycle;
import org.eclipse.milo.opcua.sdk.server.api.MonitoredItem;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class OpcUaSimulationNamespace extends ManagedNamespaceWithLifecycle {
    public static final String NAMESPACE_URI = "urn:opcua-hmi:simulation";

    private final Map<String, UaVariableNode> nodes = new LinkedHashMap<>();
    private final Map<Object, DataItem> monitoredDataItems = new ConcurrentHashMap<>();
    private final long startupTimeMs = System.currentTimeMillis();
    private final ZoneId serverZoneId = ZoneId.systemDefault();
    private final DateTimeFormatter localTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(serverZoneId);
    private boolean heartbeat;

    public OpcUaSimulationNamespace(OpcUaServer server) {
        super(server, NAMESPACE_URI);
        getLifecycleManager().addStartupTask(this::createNodes);
    }

    public void readCommandsInto(GVL gvl) {
        gvl.g_bStart = readBoolean("Commands/Start");
        gvl.g_bStop = readBoolean("Commands/Stop");
        gvl.g_bZaehlerReset = readBoolean("Commands/CounterReset");
        gvl.g_bSystemReset = readBoolean("Commands/SystemReset");
        gvl.g_nGeschwindigkeit = clamp(readInt("Commands/Speed"), 0, 10);
        write("Commands/Speed", gvl.g_nGeschwindigkeit);
        gvl.g_bAutomatikbetrieb = readBoolean("Commands/AutomaticMode");
        gvl.g_bHand_Weiche1 = readBoolean("Commands/ManualDiverter1");
        gvl.g_bHand_Weiche2 = readBoolean("Commands/ManualDiverter2");

        if (gvl.g_bSystemReset) {
            resetCommandNodes();
        }
    }

    public void writeStateFrom(GVL gvl) {
        write("Status/ConveyorRunning", gvl.g_bBandLaeuft);
        write("Status/ConveyorStopped", gvl.g_bBandStop);
        write("Status/ProductType", gvl.g_nProduktTyp);
        write("Status/ProductActive", gvl.g_bProduktAktiv);
        write("Status/Diverter1", gvl.g_bWeiche1);
        write("Status/Diverter2", gvl.g_bWeiche2);
        write("Status/AutomaticMode", gvl.g_bAutomatikbetrieb);
        write("Status/ManualMode", !gvl.g_bAutomatikbetrieb);
        write("Status/OperationMode", gvl.g_bAutomatikbetrieb ? 1 : 0);
        write("Status/ProductARecognized", gvl.g_bProduktAErkannt);
        write("Status/ProductBRecognized", gvl.g_bProduktBErkannt);
        write("Status/RejectRecognized", gvl.g_bAusschussErkannt);
        write("Status/Speed", gvl.g_nGeschwindigkeit);
        write("Sensors/Infeed", gvl.g_bSensor_Einlauf);
        write("Sensors/ProductA", gvl.g_bSensor_TypA);
        write("Sensors/ProductB", gvl.g_bSensor_TypB);
        write("Sensors/Reject", gvl.g_bSensor_Ausschuss);
        write("Sensors/Outfeed", gvl.g_bSensor_Auslauf);
        write("Lamps/Operation", gvl.g_bLampe_Betrieb);
        write("Lamps/ConveyorStopped", gvl.g_bLampe_BandSteht);
        write("Lamps/ProductA", gvl.g_bLampe_ProduktA);
        write("Lamps/ProductB", gvl.g_bLampe_ProduktB);
        write("Lamps/Reject", gvl.g_bLampe_Ausschuss);
        write("Lamps/Diverter1", gvl.g_bLampe_Weiche1);
        write("Lamps/Diverter2", gvl.g_bLampe_Weiche2);
        write("Lamps/AutomaticMode", gvl.g_bLampe_Automatikbetrieb);
        write("Lamps/ManualMode", gvl.g_bLampe_Handbetrieb);
        write("Counters/ProductA", gvl.g_nZaehler_A);
        write("Counters/ProductB", gvl.g_nZaehler_B);
        write("Counters/Reject", gvl.g_nZaehler_Aus);
        write("Counters/Total", gvl.g_nZaehler_Gesamt);
        write("Animation/BlockPosX", gvl.g_nBlockPosX);
        write("Animation/BlockPosY", gvl.g_nBlockPosY);
        write("Animation/BlockVisible", gvl.g_bBlockVisible);
        write("Animation/BlockLabel", gvl.g_sBlockLabel);

        write("Commands/CounterReset", false);
    }

    public void writeSystemInfo(long cycleTimeMs, long updateIntervalMs, boolean simulationRunning) {
        long nowMs = System.currentTimeMillis();
        Instant now = Instant.now();
        heartbeat = !heartbeat;

        write("System/ServerTime", localDateTime(now));
        write("System/ServerUtcTime", new DateTime(now));
        write("System/ServerLocalTime", localTimeFormatter.format(now));
        write("System/UptimeSeconds", (int) ((nowMs - startupTimeMs) / 1000L));
        write("System/CycleTimeMs", (int) cycleTimeMs);
        write("System/UpdateIntervalMs", (int) updateIntervalMs);
        write("System/Heartbeat", heartbeat);
        write("System/SimulationRunning", simulationRunning);
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
        UaFolderNode system = childFolder(root, "System");

        variable(commands, "Start", Identifiers.Boolean, false, AccessLevel.READ_WRITE);
        variable(commands, "Stop", Identifiers.Boolean, false, AccessLevel.READ_WRITE);
        variable(commands, "CounterReset", Identifiers.Boolean, false, AccessLevel.READ_WRITE);
        variable(commands, "SystemReset", Identifiers.Boolean, false, AccessLevel.READ_WRITE);
        variable(commands, "Speed", Identifiers.Int32, 5, AccessLevel.READ_WRITE);
        variable(commands, "AutomaticMode", Identifiers.Boolean, true, AccessLevel.READ_WRITE);
        variable(commands, "ManualDiverter1", Identifiers.Boolean, false, AccessLevel.READ_WRITE);
        variable(commands, "ManualDiverter2", Identifiers.Boolean, false, AccessLevel.READ_WRITE);

        variable(status, "ConveyorRunning", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(status, "ConveyorStopped", Identifiers.Boolean, true, AccessLevel.READ_ONLY);
        variable(status, "ProductType", Identifiers.Int32, 0, AccessLevel.READ_ONLY);
        variable(status, "ProductActive", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(status, "Diverter1", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(status, "Diverter2", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(status, "AutomaticMode", Identifiers.Boolean, true, AccessLevel.READ_ONLY);
        variable(status, "ManualMode", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(status, "OperationMode", Identifiers.Int32, 1, AccessLevel.READ_ONLY);
        variable(status, "ProductARecognized", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(status, "ProductBRecognized", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(status, "RejectRecognized", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(status, "Speed", Identifiers.Int32, 5, AccessLevel.READ_ONLY);

        variable(sensors, "Infeed", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(sensors, "ProductA", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(sensors, "ProductB", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(sensors, "Reject", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(sensors, "Outfeed", Identifiers.Boolean, false, AccessLevel.READ_ONLY);

        variable(lamps, "Operation", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(lamps, "ConveyorStopped", Identifiers.Boolean, true, AccessLevel.READ_ONLY);
        variable(lamps, "ProductA", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(lamps, "ProductB", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(lamps, "Reject", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(lamps, "Diverter1", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(lamps, "Diverter2", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(lamps, "AutomaticMode", Identifiers.Boolean, true, AccessLevel.READ_ONLY);
        variable(lamps, "ManualMode", Identifiers.Boolean, false, AccessLevel.READ_ONLY);

        variable(counters, "ProductA", Identifiers.Int32, 0, AccessLevel.READ_ONLY);
        variable(counters, "ProductB", Identifiers.Int32, 0, AccessLevel.READ_ONLY);
        variable(counters, "Reject", Identifiers.Int32, 0, AccessLevel.READ_ONLY);
        variable(counters, "Total", Identifiers.Int32, 0, AccessLevel.READ_ONLY);

        variable(animation, "BlockPosX", Identifiers.Int32, 0, AccessLevel.READ_ONLY);
        variable(animation, "BlockPosY", Identifiers.Int32, 0, AccessLevel.READ_ONLY);
        variable(animation, "BlockVisible", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(animation, "BlockLabel", Identifiers.String, "", AccessLevel.READ_ONLY);

        variable(system, "ServerTime", Identifiers.DateTime, localDateTime(Instant.now()), AccessLevel.READ_ONLY);
        variable(system, "ServerUtcTime", Identifiers.DateTime, DateTime.now(), AccessLevel.READ_ONLY);
        variable(system, "ServerLocalTime", Identifiers.String, localTimeFormatter.format(Instant.now()), AccessLevel.READ_ONLY);
        variable(system, "UptimeSeconds", Identifiers.Int32, 0, AccessLevel.READ_ONLY);
        variable(system, "CycleTimeMs", Identifiers.Int32, 0, AccessLevel.READ_ONLY);
        variable(system, "UpdateIntervalMs", Identifiers.Int32, 20, AccessLevel.READ_ONLY);
        variable(system, "Heartbeat", Identifiers.Boolean, false, AccessLevel.READ_ONLY);
        variable(system, "SimulationRunning", Identifiers.Boolean, true, AccessLevel.READ_ONLY);
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
            DataValue dataValue = new DataValue(new Variant(value));
            node.setValue(dataValue);
            sampleDataItemsForNode(node);
        }
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private DateTime localDateTime(Instant now) {
        return new DateTime(now.plusSeconds(serverZoneId.getRules().getOffset(now).getTotalSeconds()));
    }

    private void resetCommandNodes() {
        write("Commands/Start", false);
        write("Commands/Stop", false);
        write("Commands/CounterReset", false);
        write("Commands/SystemReset", false);
        write("Commands/Speed", 5);
        write("Commands/AutomaticMode", true);
        write("Commands/ManualDiverter1", false);
        write("Commands/ManualDiverter2", false);
    }

    @Override
    public void onDataItemsCreated(List<DataItem> dataItems) {
        dataItems.forEach(dataItem -> monitoredDataItems.put(dataItem.getId(), dataItem));
        sampleDataItems(dataItems);
    }

    @Override
    public void onDataItemsModified(List<DataItem> dataItems) {
        dataItems.forEach(dataItem -> monitoredDataItems.put(dataItem.getId(), dataItem));
        sampleDataItems(dataItems);
    }

    @Override
    public void onDataItemsDeleted(List<DataItem> dataItems) {
        dataItems.forEach(dataItem -> monitoredDataItems.remove(dataItem.getId()));
    }

    @Override
    public void onMonitoringModeChanged(List<MonitoredItem> monitoredItems) {
        sampleDataItems(monitoredItems.stream()
                .filter(DataItem.class::isInstance)
                .map(DataItem.class::cast)
                .toList());
    }

    private void sampleDataItems(List<DataItem> dataItems) {
        for (DataItem dataItem : dataItems) {
            sampleDataItem(dataItem);
        }
    }

    private void sampleDataItemsForNode(UaVariableNode node) {
        monitoredDataItems.values()
                .stream()
                .filter(DataItem::isSamplingEnabled)
                .filter(dataItem -> node.getNodeId().equals(dataItem.getReadValueId().getNodeId()))
                .forEach(this::sampleDataItem);
    }

    private void sampleDataItem(DataItem dataItem) {
        if (!AttributeId.Value.isEqual(dataItem.getReadValueId().getAttributeId())) {
            return;
        }

        UaVariableNode node = findNode(dataItem.getReadValueId().getNodeId());
        if (node != null) {
            dataItem.setValue(DataValue.derivedValue(node.getValue(), dataItem.getTimestampsToReturn()));
        }
    }

    private UaVariableNode findNode(NodeId nodeId) {
        return nodes.values()
                .stream()
                .filter(node -> node.getNodeId().equals(nodeId))
                .findFirst()
                .orElse(null);
    }
}
