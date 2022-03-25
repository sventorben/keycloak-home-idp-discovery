package de.sventorben.keycloak.authentication.hidpd;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import org.keycloak.models.Constants;
import org.keycloak.models.IdentityProviderModel;

final class IdentityProviderModelConfig {

	private static final String DOMAINS_ATTRIBUTE_KEY = "home.idp.discovery.domains";
	private static final String ISFALLBACK_ATTRIBUTE_KEY = "home.idp.discovery.isfallback";

	private final IdentityProviderModel identityProviderModel;

	IdentityProviderModelConfig(IdentityProviderModel identityProviderModel) {
		this.identityProviderModel = identityProviderModel;
	}

	boolean hasDomain(String domain) {
		return getDomains().anyMatch(domain::equalsIgnoreCase);
	}

	Stream<String> getDomains() {
		String domainsAttribute = identityProviderModel.getConfig().getOrDefault(DOMAINS_ATTRIBUTE_KEY, "");
		return Arrays.stream(Constants.CFG_DELIMITER_PATTERN.split(domainsAttribute));
	}

	void setDomains(Collection<String> domains) {
		String domainsAttributeValue = String.join(Constants.CFG_DELIMITER, domains);
		identityProviderModel.getConfig().put(DOMAINS_ATTRIBUTE_KEY, domainsAttributeValue);
	}

	void setIsFallback(boolean isfallback) {
		identityProviderModel.getConfig().put(ISFALLBACK_ATTRIBUTE_KEY, Boolean.toString(isfallback));
	}

	boolean isFallback() {
		return Boolean.parseBoolean(identityProviderModel.getConfig().getOrDefault(ISFALLBACK_ATTRIBUTE_KEY, "false"));
	}
}
