package de.sventorben.keycloak.authentication.hidpd.ui;

import de.sventorben.keycloak.authentication.hidpd.discovery.email.Domain;
import de.sventorben.keycloak.authentication.hidpd.discovery.email.IdentityProviderModelConfig;
import org.jboss.logging.Logger;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.RepresentationToModel;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.keycloak.services.ui.extend.UiTabProvider;
import org.keycloak.util.JsonSerialization;

import java.util.Optional;
import java.util.stream.Collectors;

public final class ConfigSyncForUiEventListener implements EventListenerProvider {

    private static final Logger LOG = Logger.getLogger(ConfigSyncForUiEventListener.class);

    private KeycloakSession keycloakSession;

    ConfigSyncForUiEventListener(KeycloakSession keycloakSession) {

        this.keycloakSession = keycloakSession;
    }

    @Override
    public void onEvent(Event event) {

    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {
        if (adminEvent.getResourceType() == ResourceType.IDENTITY_PROVIDER ) {
            try {
                IdentityProviderRepresentation identityProviderRepresentation = JsonSerialization.readValue(adminEvent.getRepresentation(), IdentityProviderRepresentation.class);
                RealmModel realmModel = keycloakSession.realms().getRealm(adminEvent.getRealmId());
                switch (adminEvent.getOperationType()) {
                    case CREATE -> handleCreate(realmModel, identityProviderRepresentation);
                    case UPDATE -> handleUpdate(realmModel, identityProviderRepresentation);
                    case DELETE -> handleDelete(realmModel, adminEvent.getResourcePath());
                }
            } catch (Exception e) {
                LOG.errorf(e, "Home IdP Discovery could not sync config changes on %s idp %s in realm %s", adminEvent.getOperationType().name().toLowerCase(), adminEvent.getResourcePath(), adminEvent.getRealmName());
            }
        }
    }

    private void handleDelete(RealmModel realmModel, String resourcePath) {
        if (resourcePath != null && !resourcePath.isBlank() && resourcePath.contains("/")) {
            String identityProviderId = resourcePath.substring(resourcePath.lastIndexOf("/")).trim();
            Optional<ComponentModel> componentModel = searchComponentModel(realmModel, identityProviderId);
            componentModel.ifPresent(realmModel::removeComponent);
        }
    }

    private void handleUpdate(RealmModel realmModel, IdentityProviderRepresentation identityProviderRepresentation) {
        IdentityProviderModelConfig identityProviderModelConfig = new IdentityProviderModelConfig(RepresentationToModel.toModel(realmModel, identityProviderRepresentation, keycloakSession));
        Optional<ComponentModel> componentModel = searchComponentModel(realmModel, identityProviderRepresentation.getProviderId());
        if (componentModel.isEmpty()) {
            handleCreate(realmModel, identityProviderRepresentation);
        } else {
            MultivaluedHashMap<String, String> config = componentModel.get().getConfig();
            config.put("domains", identityProviderModelConfig.getDomains().map(Domain::getRawValue).collect(Collectors.toList()));
            config.putSingle("matchSubDomains", String.valueOf(identityProviderModelConfig.shouldMatchSubDomains()));

        }
    }

    private static Optional<ComponentModel> searchComponentModel(RealmModel realmModel, String identityProviderId) {
        return realmModel.getComponentsStream(realmModel.getId(), UiTabProvider.class.getName())
            .filter(it -> DomainTabFactory.PROVIDER_ID.equals(it.getProviderId()))
            .filter(it -> it.getConfig().getFirstOrDefault("providerId", "").equals(identityProviderId))
            .findFirst();
    }

    private void handleCreate(RealmModel realmModel, IdentityProviderRepresentation identityProviderRepresentation) {
        IdentityProviderModelConfig identityProviderModelConfig = new IdentityProviderModelConfig(RepresentationToModel.toModel(realmModel, identityProviderRepresentation, keycloakSession));

        ComponentModel componentModel = new ComponentModel();
        componentModel.setProviderId(DomainTabFactory.PROVIDER_ID);
        componentModel.setProviderType(UiTabProvider.class.getName());
        componentModel.setParentId(realmModel.getId());

        MultivaluedHashMap<String, String> config = new MultivaluedHashMap<>();
        config.putSingle("tab", "domains");
        config.putSingle("providerId", identityProviderRepresentation.getProviderId());
        config.put("domains", identityProviderModelConfig.getDomains().map(Domain::getRawValue).collect(Collectors.toList()));
        config.putSingle("alias", identityProviderRepresentation.getAlias());
        config.putSingle("realm", realmModel.getId());

        componentModel.setConfig(config);
        realmModel.addComponentModel(componentModel);
    }

    @Override
    public void close() {

    }

}
