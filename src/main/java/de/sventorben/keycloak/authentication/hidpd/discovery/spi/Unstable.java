package de.sventorben.keycloak.authentication.hidpd.discovery.spi;

import java.lang.annotation.*;

/**
 * Marks an interface, class or method as unstable, indicating that the SPI is subject to
 * potential significant changes or removal in future releases. This annotation serves
 * as a warning that the SPI is not yet finalized, and changes may occur which could
 * affect consumers of this API.
 *
 * <p>Usage of an API marked with this annotation should be limited to experimental or
 * non-production code as there is no guarantee of backward compatibility or ongoing
 * support. Implementors and consumers should be prepared for possible alterations and
 * required adaptations in their code when upgrades occur.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@interface Unstable {
}
