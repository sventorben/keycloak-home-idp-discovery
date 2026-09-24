package de.sventorben.keycloak.authentication.hidpd.discovery.email;

import org.keycloak.models.Constants;
import org.keycloak.models.IdentityProviderModel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public final class IdentityProviderModelConfig {

    private static final String DOMAINS_ATTRIBUTE_KEY = "home.idp.discovery.domains";
    private static final String SUBDOMAINS_ATTRIBUTE_KEY = "home.idp.discovery.matchSubdomains";

    private final IdentityProviderModel identityProviderModel;

    public IdentityProviderModelConfig(IdentityProviderModel identityProviderModel) {
        this.identityProviderModel = identityProviderModel;
    }

    boolean supportsDomain(String userAttributeName, Domain domain) {
        boolean shouldMatchSubdomains = shouldMatchSubdomains(userAttributeName);
        return getDomains(userAttributeName).anyMatch(it ->
            it.equals(domain) ||
                (shouldMatchSubdomains && domain.isSubDomainOf(it)));
    }

    private boolean shouldMatchSubdomains(String userAttributeName) {
        String key = getSubdomainConfigKey(userAttributeName);
        return Boolean.parseBoolean(identityProviderModel.getConfig().getOrDefault(key, "false"));
    }

    private Stream<Domain> getDomains(String userAttributeName) {
        String key = getDomainConfigKey(userAttributeName);
        String domainsAttribute = identityProviderModel.getConfig().getOrDefault(key, "");
        return Arrays.stream(Constants.CFG_DELIMITER_PATTERN.split(domainsAttribute)).map(Domain::new);
    }

    private String getDomainConfigKey(String userAttributeName) {
        return getConfigKey(DOMAINS_ATTRIBUTE_KEY, userAttributeName);
    }

    private String getSubdomainConfigKey(String userAttributeName) {
        return getConfigKey(SUBDOMAINS_ATTRIBUTE_KEY, userAttributeName);
    }

    private String getConfigKey(String attributeKey, String userAttributeName) {
        String key = attributeKey;
        if (userAttributeName != null) {
            final String candidateKey = attributeKey + "." + userAttributeName;
            if (identityProviderModel.getConfig().containsKey(candidateKey)) {
                key = candidateKey;
            }
        }
        return key;
    }

    public void setDomains(List<String> domains) {
        String domainConfigKey = getDomainConfigKey(null);
        if (domains == null) domains = Collections.emptyList();
        identityProviderModel.getConfig().put(domainConfigKey, String.join(Constants.CFG_DELIMITER, domains));
    }

    public Stream<Domain> getDomains() {
        String key = getDomainConfigKey(null);
        String domainsAttribute = identityProviderModel.getConfig().getOrDefault(key, "");
        return Arrays.stream(Constants.CFG_DELIMITER_PATTERN.split(domainsAttribute)).map(Domain::new);
    }

    public void setMatchDomain(boolean match) {
        String subdomainConfigKey = getSubdomainConfigKey(null);
        identityProviderModel.getConfig().put(subdomainConfigKey, String.valueOf(match));
    }

    public boolean shouldMatchSubDomains() {
        return shouldMatchSubdomains(null);
    }
}
