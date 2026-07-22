package de.sventorben.keycloak.authentication.hidpd.ui;

import de.sventorben.keycloak.authentication.hidpd.discovery.email.IdentityProviderModelConfig;
import org.keycloak.Config;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.ui.extend.UiTabProviderFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DomainTabFactory implements UiTabProviderFactory {

    static final String PROVIDER_ID = "hidpd-ui-domain-tab";

    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel model) throws ComponentValidationException {
        MultivaluedHashMap<String, String> config = model.getConfig();
        String alias = config.getFirst("alias");

        IdentityProviderModel idp = session.identityProviders().getByAlias(alias);
        if (idp == null) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void onCreate(KeycloakSession session, RealmModel realm, ComponentModel model) {
        updateIdentityProviderModel(session, model);
    }

    @Override
    public void onUpdate(KeycloakSession session, RealmModel realm, ComponentModel oldModel, ComponentModel newModel) {
        updateIdentityProviderModel(session, newModel);
    }


    private static void updateIdentityProviderModel(KeycloakSession session, ComponentModel model) {
        MultivaluedHashMap<String, String> config = model.getConfig();
        String alias = config.getFirst("alias");

        IdentityProviderModel idp = session.identityProviders().getByAlias(alias);
        if (idp == null) {
            throw new IllegalArgumentException();
        }

        IdentityProviderModelConfig idpConfig = new IdentityProviderModelConfig(idp);
        idpConfig.setDomains(config.getOrDefault("domains", config.getOrDefault("domains", Collections.emptyList())));
        String matchSubDomains = config.getFirstOrDefault("matchSubDomains", config.getFirstOrDefault("matchSubDomains", "false"));
        idpConfig.setMatchDomain(Boolean.parseBoolean(matchSubDomains));
    }

    @Override
    public Object create(KeycloakSession session, ComponentModel model) {
        return new DomainTab();
    }

    @Override
    public Map<String, Object> getTypeMetadata() {
        return UiTabProviderFactory.super.getTypeMetadata();
    }

    @Override
    public String getPath() {
        return "/:realm/identity-providers/:providerId/:alias/:tab?";
    }

    @Override
    public Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        params.put("tab", "domains");
        return params;
    }

    @Override
    public String getHelpText() {
        return null;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return IdentityProviderModelConfigProperties.CONFIG_PROPERTIES;
    }

    @Override
    public Object getConfig() {
        return null;
    }

    @Override
    public void init(Config.Scope scope) {

    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
