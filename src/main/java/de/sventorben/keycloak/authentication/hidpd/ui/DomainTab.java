package de.sventorben.keycloak.authentication.hidpd.ui;

import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.ui.extend.UiTabProvider;

import java.util.List;

public final class DomainTab implements UiTabProvider {
    @Override
    public String getHelpText() {
        return null;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return null;
    }

    @Override
    public void close() {

    }
}
