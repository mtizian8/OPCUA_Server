import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateBuilder;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.UUID;

public class OpcUaKeyStoreLoader {
    private static final String SERVER_ALIAS = "server";
    private static final char[] PASSWORD = "password".toCharArray();

    private X509Certificate[] serverCertificateChain;
    private KeyPair serverKeyPair;

    public OpcUaKeyStoreLoader load(Path baseDir) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        File serverKeyStore = baseDir.resolve("opcua-hmi-server.pfx").toFile();

        if (!serverKeyStore.exists()) {
            keyStore.load(null, PASSWORD);

            KeyPair keyPair = SelfSignedCertificateGenerator.generateRsaKeyPair(2048);
            String applicationUri = "urn:opcua-hmi:server:" + UUID.randomUUID();

            SelfSignedCertificateBuilder builder = new SelfSignedCertificateBuilder(keyPair)
                    .setCommonName("OPCUA HMI Simulation Server")
                    .setOrganization("OPCUA_HMI")
                    .setOrganizationalUnit("simulation")
                    .setLocalityName("local")
                    .setStateName("local")
                    .setCountryCode("DE")
                    .setApplicationUri(applicationUri);

            builder.addDnsName("localhost");
            builder.addIpAddress("127.0.0.1");

            String computerName = System.getenv("COMPUTERNAME");
            if (computerName != null && !computerName.isBlank()) {
                builder.addDnsName(computerName);
            }

            X509Certificate certificate = builder.build();
            keyStore.setKeyEntry(SERVER_ALIAS, keyPair.getPrivate(), PASSWORD, new X509Certificate[]{certificate});
            keyStore.store(new FileOutputStream(serverKeyStore), PASSWORD);
        } else {
            keyStore.load(new FileInputStream(serverKeyStore), PASSWORD);
        }

        Key serverPrivateKey = keyStore.getKey(SERVER_ALIAS, PASSWORD);
        if (serverPrivateKey instanceof PrivateKey) {
            X509Certificate serverCertificate = (X509Certificate) keyStore.getCertificate(SERVER_ALIAS);
            serverCertificateChain = Arrays.stream(keyStore.getCertificateChain(SERVER_ALIAS))
                    .map(X509Certificate.class::cast)
                    .toArray(X509Certificate[]::new);
            PublicKey serverPublicKey = serverCertificate.getPublicKey();
            serverKeyPair = new KeyPair(serverPublicKey, (PrivateKey) serverPrivateKey);
        }

        return this;
    }

    public X509Certificate[] getServerCertificateChain() {
        return serverCertificateChain;
    }

    public KeyPair getServerKeyPair() {
        return serverKeyPair;
    }
}
