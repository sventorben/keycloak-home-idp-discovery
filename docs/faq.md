---
layout: default
title: FAQ
nav_order: 5
---

# Frequently asked questions
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

## Does it work with the legacy Wildfly-based Keycloak distro?

Maybe! There is even a high chance it will, since this extension does not make use of any Quarkus-related functionality.
For installation instructions, please refer to
an [older version of this readme](https://github.com/sventorben/keycloak-home-idp-discovery/blob/v16.0.0/README.md).

Please note that with the release of Keycloak 20.0.0 the Wildfly-based distro is no longer supported.
Hence, I dropped support for the Wildfly-based distro already. Though this library may still work with the Wildfly-based
distro, I will no longer put any efforts into keeping this extension compatible.

## Does it work with Keycloak / RedHat SSO version X.Y.Z?

If you are using Keycloak version `X` (e.g. `X.y.z`), version `X.b.c` of this extension should be compatible.
Keycloak SPIs are quite stable. So, there is a high chance this authenticator will work with other versions, too. Check
the details of
latest [compatibility test run](https://github.com/sventorben/keycloak-home-idp-discovery/actions/workflows/matrix.yml) for
an overview or simply give it a try.

Authenticator version `X.b.c` is compiled against Keycloak version `X.y.z`. For example, version `16.3.1` will be
compiled against Keycloak version `16.y.z`.

I do not guarantee what version `a.b` or `y.z` will be. Neither do I backport features to older version, nor maintain
any older versions of this authenticator. If you need the latest features or bugfixes for an older version, please fork
this project or update your Keycloak instance. I recommend doing the latter on regular basis anyways.

For RedHat SSO versions, please check the corresponding Keycloak
version [here](https://access.redhat.com/articles/2342881). Above rules apply ;)

## User does not get redirected, but only sees a password form instead.

Make sure that your users email is marked as verified. You can enable the `Email verified` flag per user or switch
on `Trust Email` in the advanced settings of the identity provider.

You can also allow redirecting users with unverified email addresses by switching
on `Forward users with unverified email` option in the authenticator config.

## User is not redirected to the correct identity provider. How to analyze the problem?

You may want to increase the log level to see more fine-grained information on how the authenticator discovered the home
identity provider.
Try to increase the log level to `DEBUG` or even `TRACE` level. Details can be found in the
official [Configuring logging](https://www.keycloak.org/server/logging) guide.
The log category for the authenticator is `de.sventorben.keycloak.authentication.hidpd`.
