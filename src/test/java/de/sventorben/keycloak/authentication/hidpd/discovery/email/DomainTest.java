package de.sventorben.keycloak.authentication.hidpd.discovery.email;

import de.sventorben.keycloak.authentication.hidpd.discovery.email.Domain;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class DomainTest {

    @ParameterizedTest
    @CsvSource(value = {
        "com, false",
        "example.com, false",
        "sub.example.com, true",
        "a.sub.example.com, true",
        "anotherexample.com, false"
    })
    void test(String candidate, boolean expected) {
        Domain domain = new Domain("example.com");
        Domain candidateDomain = new Domain(candidate);
        assertThat(candidateDomain.isSubDomainOf(domain)).isEqualTo(expected);
    }

}
