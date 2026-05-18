import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig;
import org.eclipse.milo.opcua.sdk.server.identity.AnonymousIdentityValidator;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaRuntimeException;
import org.eclipse.milo.opcua.stack.core.security.DefaultCertificateManager;
import org.eclipse.milo.opcua.stack.core.security.DefaultTrustListManager;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.transport.TransportProfile;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.structured.BuildInfo;
import org.eclipse.milo.opcua.stack.core.util.CertificateUtil;
import org.eclipse.milo.opcua.stack.core.util.NonceUtil;
import org.eclipse.milo.opcua.stack.server.EndpointConfiguration;
import org.eclipse.milo.opcua.stack.server.security.DefaultServerCertificateValidator;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig.USER_TOKEN_POLICY_ANONYMOUS;

public class OpcUaSimulationServer {
    public static final int TCP_PORT = 4840;
    public static final String PATH = "/OPCUA_HMI";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private final OpcUaServer server;
    private final OpcUaSimulationNamespace namespace;

    public OpcUaSimulationServer() throws Exception {
        NonceUtil.blockUntilSecureRandomSeeded(10, TimeUnit.SECONDS);

        Path securityDir = Paths.get(System.getProperty("java.io.tmpdir"), "opcua-hmi", "security");
        Files.createDirectories(securityDir);
        File pkiDir = securityDir.resolve("pki").toFile();

        OpcUaKeyStoreLoader loader = new OpcUaKeyStoreLoader().load(securityDir);
        DefaultCertificateManager certificateManager = new DefaultCertificateManager(
                loader.getServerKeyPair(),
                loader.getServerCertificateChain()
        );
        DefaultTrustListManager trustListManager = new DefaultTrustListManager(pkiDir);
        DefaultServerCertificateValidator certificateValidator =
                new DefaultServerCertificateValidator(trustListManager);

        X509Certificate certificate = certificateManager.getCertificates()
                .stream()
                .findFirst()
                .orElseThrow(() -> new UaRuntimeException(StatusCodes.Bad_ConfigurationError, "no certificate found"));

        String applicationUri = CertificateUtil.getSanUri(certificate)
                .orElseThrow(() -> new UaRuntimeException(
                        StatusCodes.Bad_ConfigurationError,
                        "certificate is missing the application URI"));

        OpcUaServerConfig config = OpcUaServerConfig.builder()
                .setApplicationUri(applicationUri)
                .setApplicationName(LocalizedText.english("OPCUA HMI Simulation Server"))
                .setEndpoints(createEndpoints(certificate))
                .setBuildInfo(new BuildInfo(
                        "urn:opcua-hmi:server",
                        "OPCUA_HMI",
                        "OPCUA HMI Simulation Server",
                        OpcUaServer.SDK_VERSION,
                        "",
                        DateTime.now()
                ))
                .setCertificateManager(certificateManager)
                .setTrustListManager(trustListManager)
                .setCertificateValidator(certificateValidator)
                .setIdentityValidator(AnonymousIdentityValidator.INSTANCE)
                .setProductUri("urn:opcua-hmi:server")
                .build();

        server = new OpcUaServer(config);
        namespace = new OpcUaSimulationNamespace(server);
        namespace.startup();
    }

    public CompletableFuture<OpcUaServer> startup() {
        return server.startup();
    }

    public CompletableFuture<OpcUaServer> shutdown() {
        namespace.shutdown();
        return server.shutdown();
    }

    public void readCommandsInto(GVL gvl) {
        namespace.readCommandsInto(gvl);
    }

    public void writeStateFrom(GVL gvl) {
        namespace.writeStateFrom(gvl);
    }

    public void writeSystemInfo(long cycleTimeMs, long updateIntervalMs, boolean simulationRunning) {
        namespace.writeSystemInfo(cycleTimeMs, updateIntervalMs, simulationRunning);
    }

    private Set<EndpointConfiguration> createEndpoints(X509Certificate certificate) {
        EndpointConfiguration baseEndpoint = createEndpoint(certificate, "");
        EndpointConfiguration hmiEndpoint = createEndpoint(certificate, PATH);

        return Set.of(baseEndpoint, hmiEndpoint);
    }

    private EndpointConfiguration createEndpoint(X509Certificate certificate, String path) {
        return EndpointConfiguration.newBuilder()
                .setBindAddress("0.0.0.0")
                .setBindPort(TCP_PORT)
                .setHostname("localhost")
                .setPath(path)
                .setCertificate(certificate)
                .setSecurityPolicy(SecurityPolicy.None)
                .setSecurityMode(MessageSecurityMode.None)
                .setTransportProfile(TransportProfile.TCP_UASC_UABINARY)
                .addTokenPolicies(USER_TOKEN_POLICY_ANONYMOUS)
                .build();
    }
}
