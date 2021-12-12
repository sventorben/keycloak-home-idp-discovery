package de.sventorben.keycloak.authentication;

import dasniko.testcontainers.keycloak.KeycloakContainer;

import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleDescriptor.Version;
import java.util.ArrayList;
import java.util.List;

final class KeycloakXContainer extends KeycloakContainer {

    private String providerClassLocation;
    private final String version;

    KeycloakXContainer(String version) {
        super("quay.io/keycloak/keycloak-x:" + version);
        this.version = version;
    }

    @Override
    protected void configure() {
        super.configure();
        if (!"latest".equalsIgnoreCase(version) && Version.parse(version).compareTo(Version.parse("15.1")) < 0) {
            if (providerClassLocation != null) {
                createKeycloakExtensionDeployment("/opt/jboss/keycloak/providers", "providers.jar", providerClassLocation);
            }
            List<String> commandParts = new ArrayList<>();
            commandParts.add("--auto-config");
            commandParts.add("--http-enabled=true");
            this.setCommand(commandParts.toArray(new String[0]));
        }
    }

    @Override
    public KeycloakContainer withProviderClassesFrom(String classesLocation) {
        this.providerClassLocation = classesLocation;
        return super.withProviderClassesFrom(classesLocation);
    }

    @Override
    public KeycloakContainer withRealmImportFile(String importFile) {
        if (!importFile.startsWith("/")) {
            importFile = "/" + importFile;
        }
        return super.withRealmImportFile(importFile);
    }
}
