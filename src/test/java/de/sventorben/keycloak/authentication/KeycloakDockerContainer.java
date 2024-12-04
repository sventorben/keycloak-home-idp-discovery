package de.sventorben.keycloak.authentication;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.images.ImagePullPolicy;
import org.testcontainers.images.PullPolicy;

import java.time.Duration;

class KeycloakDockerContainer {

    private static final Logger LOGGER = LoggerFactory.getLogger(HomeIdpDiscoveryIT.class);

    private static final String KEYCLOAK_ADMIN_PASS = "admin";
    private static final String KEYCLOAK_ADMIN_USER = "admin";
    private static final int KEYCLOAK_HTTP_PORT = 8080;
    private static final int KEYCLOAK_METRICS_HTTP_PORT = 9000;

    static KeycloakContainer create(Network network) {
        String fullImage = FullImageName.get();
        ImagePullPolicy pullPolicy = PullPolicy.defaultPolicy();
        if (FullImageName.isLatestVersion() || FullImageName.isNightlyVersion()) {
            pullPolicy = PullPolicy.alwaysPull();
        }
        KeycloakContainer container = new KeycloakContainer(fullImage);
        LOGGER.info("Running test with image: " + container.getDockerImageName());
        return container
            .withImagePullPolicy(pullPolicy)
            .withRealmImportFile("/test-realm.json")
            .withRealmImportFile("/idp-realm.json")
            .withProviderClassesFrom("target/classes")
            .withExposedPorts(KEYCLOAK_HTTP_PORT, KEYCLOAK_METRICS_HTTP_PORT)
            .withLogConsumer(new Slf4jLogConsumer(LOGGER).withSeparateOutputStreams())
            .withStartupTimeout(Duration.ofSeconds(90))
            .withNetwork(network)
            .withNetworkAliases("keycloak")
            .withAdminUsername(KEYCLOAK_ADMIN_USER)
            .withAdminPassword(KEYCLOAK_ADMIN_PASS);
    }

}
