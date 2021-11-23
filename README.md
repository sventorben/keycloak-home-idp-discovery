# Keycloak: Home IdP Discovery

This is a simple Keycloak authenticator to redirect users to their home identity provider during login.

![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/sventorben/keycloak-home-idp-discovery?sort=semver)
![Keycloak Dependency Version](https://img.shields.io/badge/Keycloak-15.0.2-blue)
![Keycloak.X ready](https://img.shields.io/badge/%E2%AD%90%20Keycloak.X%20%E2%AD%90-ready%20-blue)
![GitHub Release Date](https://img.shields.io/github/release-date-pre/sventorben/keycloak-home-idp-discovery)
![Github Last Commit](https://img.shields.io/github/last-commit/sventorben/keycloak-home-idp-discovery)

![CI build](https://github.com/sventorben/keycloak-home-idp-discovery/actions/workflows/buildAndTest.yml/badge.svg)
![open issues](https://img.shields.io/github/issues/sventorben/keycloak-home-idp-discovery)

## What is it good for?

TODO

## How does it work?

TODO


## How to install?

Download a release (*.jar file) that works with your Keycloak version from the [list of releases](https://github.com/sventorben/keycloak-home-idp-discovery/releases).
Follow the below instructions depending on your distribution and runtime environment.

### Wildfly-based distro

Create a Wildfly module and deploy it to your Keycloak instance. For details please refer to the [official documentation](https://www.keycloak.org/docs/latest/server_development/#register-a-provider-using-modules).

For convenience, here is a `module.xml` file.
```xml
<?xml version="1.0" encoding="UTF-8"?>
<module xmlns="urn:jboss:module:1.3" name="keycloak-home-idp-discovery">
    <resources>
        <resource-root path="keycloak-home-idp-discovery.jar"/>
    </resources>
    <dependencies>
        <module name="org.keycloak.keycloak-services"/>
    </dependencies>
</module>
```

### Container image (Docker)

For Docker-based setups mount or copy the jar to `/opt/jboss/keycloak/providers`. You may want to check [docker-compose.yml](docker-compose.yml) as an example.

### Maven/Gradle

Packages are being released to GitHub Packages. You find the coordinates [here](https://github.com/sventorben/keycloak-home-idp-discovery/packages/779937/versions)!

It may happen that I remove older packages without prior notice, because the storage is limited on the free tier.


## How to configure?

TODO

## Frequently asked questions

### Does it (already) work with Keycloak.X?
On October 28th 2021 the Keycloak project [announced](https://www.keycloak.org/2021/10/keycloak-x-update) the roadmap for the new Quarkus-based Keycloak-X distribution.
According to this Keycloak 16 will be the last preview of the Quarkus distribution. As of December 2021, Keycloak 17 will make the Quarkus distribution fully supported the WildFly distribution will be deprecated.
Support for the Wildfly distribution will be removed by mid 2022.

Therefore, I will focus all further development of this library towards the Quarkus-based Keycloak.X distribution.
Once the Wildfly support will be removed from the Keycloak project, I will remove all support for Wildfly here as well.

Don't worry, I will ensure this library stays compatible with the Wildfly distribution as well as with Keycloak.X until then.

### Does it work with Keycloak version X.Y.Z?

If you are using Keycloak version `X` (e.g. `X.y.z`), version `X.b.c` should be compatible.
Keycloak SPIs are quite stable. So, there is a high chance this authenticator will work with other versions, too. Check the details of latest [build results](https://github.com/sventorben/keycloak-home-idp-discovery/actions/workflows/buildAndTest.yml) for an overview or simply give it a try.

Authenticator version `X.b.c` is compiled against Keycloak version `X.y.z`. For example, version `12.3.1` will be compiled against Keycloak version `12.y.z`.

I do not guarantee what version `a.b` or `y.z` will be. Neither do I backport features to older version, nor maintain any older versions of this authenticator. If you need the latest features or bugfixes for an older version, please fork this project or update your Keycloak instance. I recommend doing the latter on regular basis anyways.
