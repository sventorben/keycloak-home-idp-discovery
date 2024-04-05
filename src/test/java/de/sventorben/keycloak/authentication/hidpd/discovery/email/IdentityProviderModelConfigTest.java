package de.sventorben.keycloak.authentication.hidpd.discovery.email;

import de.sventorben.keycloak.authentication.hidpd.discovery.email.Domain;
import de.sventorben.keycloak.authentication.hidpd.discovery.email.IdentityProviderModelConfig;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.keycloak.models.IdentityProviderModel;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class IdentityProviderModelConfigTest {

    private static final String DOMAINS_ATTRIBUTE_KEY = "home.idp.discovery.domains";
    private static final String SUBDOMAINS_ATTRIBUTE_KEY = "home.idp.discovery.matchSubdomains";

    @ParameterizedTest
    @CsvSource(value = {
        "null, null, null, null, false",
        "null, email, null, null, false",
        "null, null, null, '', false",
        "null, email, null, '', false",
        "null, null, null, example.net##example.org, false",
        "null, null, null, example.com##example.org, true",
        "null, email, null, example.net##example.org, false",
        "null, email, null, example.com##example.org, true",
        "email, email, null, example.net##example.org, false",
        "email, email, null, example.com##example.org, true",
        "email, email, example.net##example.org, example.com##example.org, false",
        "email, email, example.com##example.org, example.net##example.org, true",
        "email, email, example.net##example.org, null, false",
        "email, email, example.com##example.org, null, true",
        "email, email, example.com##example.org, , true",
        "email, email, '', example.com##example.org, false",
    }, nullValues = { "null" })
    void testSupportsDomain(String userAttributeName, String userAttributeNameQuery, String userAttributeDomains, String defaultDomains, boolean expected) {
        Map<String, String> config = new HashMap<>();
        IdentityProviderModel idp = new IdentityProviderModel();
        idp.setConfig(config);
        IdentityProviderModelConfig cut = new IdentityProviderModelConfig(idp);
        if (userAttributeName != null && userAttributeDomains != null) {
            config.put(DOMAINS_ATTRIBUTE_KEY + "." + userAttributeName, userAttributeDomains);
        }
        if (defaultDomains != null) {
            config.put(DOMAINS_ATTRIBUTE_KEY, defaultDomains);
        }

        boolean result = cut.supportsDomain(userAttributeNameQuery, new Domain("example.com"));

        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource(value = {
        "example.com,null,true",
        "example.com,email,true",
        "example.com,upn,true",
        "sub.example.com,null,false",
        "sub.example.com,email,true",
        "sub.example.com,upn,false",
    }, nullValues = { "null" })
    void testSupportsDomainWithSubdomain(String domain, String attribute, boolean expected) {
        Map<String, String> config = new HashMap<>();
        IdentityProviderModel idp = new IdentityProviderModel();
        idp.setConfig(config);
        IdentityProviderModelConfig cut = new IdentityProviderModelConfig(idp);
        config.put(SUBDOMAINS_ATTRIBUTE_KEY, "false");
        config.put(SUBDOMAINS_ATTRIBUTE_KEY + ".email", "true");
        // test UPN
        config.put(DOMAINS_ATTRIBUTE_KEY, "example.com##example.net");
        config.put(DOMAINS_ATTRIBUTE_KEY + ".email", "example.com##example.net");
        config.put(DOMAINS_ATTRIBUTE_KEY + ".upn", "example.com##example.net");

        boolean result = cut.supportsDomain(attribute, new Domain(domain));

        assertThat(result).isEqualTo(expected);
    }

}
