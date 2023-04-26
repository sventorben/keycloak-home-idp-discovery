# Keycloak: Home IdP Discovery

This is a simple Keycloak authenticator to redirect users to their home identity provider during login.

![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/sventorben/keycloak-home-idp-discovery?sort=semver)
![Keycloak Dependency Version](https://img.shields.io/badge/Keycloak-21.1.1-blue)
![GitHub Release Date](https://img.shields.io/github/release-date-pre/sventorben/keycloak-home-idp-discovery)
![Github Last Commit](https://img.shields.io/github/last-commit/sventorben/keycloak-home-idp-discovery)

![CI build](https://github.com/sventorben/keycloak-home-idp-discovery/actions/workflows/buildAndTest.yml/badge.svg)
![open issues](https://img.shields.io/github/issues/sventorben/keycloak-home-idp-discovery)

## What is it good for?

When a federated user wants to login via Keycloak, Keycloak will present a username/password form and a list of configured identity providers to the user. The user needs to choose an identity provider to get redirected.
This authenticator allows to skip the step of selecting an identity provider.

## How does it work?

If this authenticator gets configured as part of a browser based login flow, Keycloak will present a username form (without password form and without list of configured identity providers).
A user can then enter an email address. Keycloak will then choose an identity provider based on the domain part of the provided email address and forward the user to the chosen provider.

The identity provider will be chosen by the following preference:
1. If the `forwardToLinkedIdp` config option is enabled
   1. Use the first linked identity provider with matching domain
   2. Use the first linked identity provider
   3. Use non-linked identity provider with matching domain
2. If the `forwadToLinkedIdp` config option is disabled
   1. Use the first identity provider with matching domain

Only enabled and not link-only identity providers will be considered.

## How to install?

Download a release (*.jar file) that works with your Keycloak version from the [list of releases](https://github.com/sventorben/keycloak-home-idp-discovery/releases).

### Server
Copy the jar to the `providers` folder and execute the following command:

```shell
${kc.home.dir}/bin/kc.sh build
```

### Container image (Docker)
For Docker-based setups mount or copy the jar to `/opt/keycloak/providers`.

You may want to check [docker-compose.yml](docker-compose.yml) as an example.

### Maven/Gradle

Packages are being released to GitHub Packages. You find the coordinates [here](https://github.com/sventorben/keycloak-home-idp-discovery/packages/1112199/versions)!

It may happen that I remove older packages without prior notice, because the storage is limited on the free tier.


## How to configure?

### Add Authenticator Execution

* Navigate to `Authentication`
* Create a custom `Basic` flow
* Click `Add execution`
* Select `Home IdP Discovery` and add the execution
* Set execution as required or alternative as desired
* Bind your newly created flow as desired - either as a default for the whole realm or on a per-client basis.

See the image below for an example:

![Example flow](docs/images/flow.JPG)

### Configuration options

To configure click settings/gear icon (&#9881;)

![Authenticator configuration](docs/images/authenticator-config.jpg)

| Option                        | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
|-------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| User attribute                | The user attribute used to lookup the user's email address.<br><br>If set to `email` (default) the authenticator will use the default email property. In this case the authenticator will only forward the user if the email has been verified. For any other attribute, the authenticator will not validate if the email has been verified. <br><br> A common use case is to store a User Principal Name (UPN) in a custom attribute and forward users based on the UPN instead instead of their email address.                                                                                                                                                                                                                            |
| Bypass login page             | If switched on, users will be forwarded to their home IdP without the need to reenter/confirm their email address on the login page iff email address is provided as an OICD [`login_hint` parameter](https://openid.net/specs/openid-connect-core-1_0.html#AuthRequest) or SAML `subject/nameID`.<br><br> If switched off, users are only redirected after submitting/confirming their email address on the login page. (default)<br> <br> *Note: This will take SAML `ForceAuthn` and OIDC [`prompt=login&#124;consent&#124;select_account`](https://openid.net/specs/openid-connect-core-1_0.html#AuthRequest) parameters into account. If one of these parameters is present, the login page will not be bypassed even if switched on.* |
| Forward to linked IdP         | If switched on, federated users (with already linked IdPs) will be forwarded to a linked IdP even if no IdP has been configured for the user's email address. Federated users can also use their local username for login instead of their email address.<br><br> If switched off, users will only be forwarded to IdPs with matching email domains. (default)                                                                                                                                                                                                                                                                                                                                                                              |
| Forward to first matched IdP  | If switched on, users will be forwarded to the first IdP that matches the email domain (default), even if multiply IdPs may match.<br><br>If switched off, user will be shown all IdPs that match the email domain to choose one, iff multiple match.<br>The user will only be able to choose from IdPs that match the email domain. Please note that also IdPs that have [`Hide on Login Page`](https://www.keycloak.org/docs/latest/server_admin/#_general-idp-config) switched on will be shown.<br>If only one IdP matches, behavior is the same as if switched on.                                                                                                                                                                     |

### Configure email domains

Email domains can be configured per identity provider. Currently, this can only be achieved via [Identity Providers REST API](https://www.keycloak.org/docs-api/19.0/rest-api/index.html#_identity_providers_resource). Make sure to post the full body, as you may receive from a `GET` request to the same endpoint, plus the `home.idp.discovery.domains` configuration.

```
PUT /{realm}/identity-provider/instances/{alias}
{
  ...
  "config": {
    "home.idp.discovery.domains": "example.com##example.net",
    ...
  },
  ...
}
```

Note that domains need to be separated by two hashtags (`##`).

You can also use the [Admin CLI (kcadm)](https://www.keycloak.org/docs/latest/server_admin/#identity-provider-operations):
```shell
kcadm.sh update identity-provider/instances/{alias} -s 'config."home.idp.discovery.domains"="example.com##example.net"'
```

#### Multiple authenticator instances
If you use multiple authenticator instances each using a different user attribute, you can specify different domains per user attribute as well.
For this to work, simply add a config key `home.idp.discovery.domains.<attribute_name>` where `<attribute_name>` is the name of the attribute you are using.

For example, when using a custom user attribute named `upn`, add a key named `home.idp.discovery.domains.upn`.
The authenticator will try to look up the specific key `home.idp.discovery.domains.<attribute_name>` first and fallback to `home.idp.discovery.domains` if the specific key does not exist.

```
PUT /{realm}/identity-provider/instances/{alias}
{
  ...
  "config": {
    "home.idp.discovery.domains": "example.com##example.net",
    "home.idp.discovery.domains.upn": "enterprise.local",
    "home.idp.discovery.domains.email": "example.org",
    ...
  },
  ...
}
```

In the example above, the following domains will be effective when using the configured attribute name:

| configured attribute name | effective domains        |
|---------------------------|--------------------------|
| email                     | example.org              |
| upn                       | enterprise.local         |
| notconfigured             | example.com, example.net |

Please note that the lookup is case-insensitive, so `email` will be the same as `Email` or `EMAIL`.

## Themes

The authenticator supports internationalization and you can add additional languages or locales as needed.

Please see the [Server Developer guide](https://www.keycloak.org/docs/latest/server_development/#messages) for detailed information.

### Customized messages for select login options dialog
When you configured this authenticator as an alternative to other authenticators, Keycloak may show a link "Try Another Way" during login as shown below:

![Sign in to your account dialog with "try another way" link](docs/images/sing-in-to-your-account.jpg)

When clicking that link, the user can select the login method based on configured alternative authenticators.

![Select login method dialog](docs/images/select-login-method.jpg)

You can change the title and help text for this authenticator by adding the following messages to your custom theme:

```properties
home-idp-discovery-display-name=Home identity provider
home-idp-discovery-help-text=Sign in via your home identity provider which will be automatically determined based on your provided email address.
```

### Customized messages for selecting an IdP during login
If multiple IdPs match the email domain of the user, the user may be presented with a dialog to choose an identity provider (see config option `Forward to first matched IdP`).

![Select IdP dialog](docs/images/select-idp.jpg)

You can change the title by adding the following messages to your custom theme:

```properties
home-idp-discovery-identity-provider-login-label=Select your home identity provider
```

## Frequently asked questions

### Does it work with the legacy Wildfly-based Keycloak distro?
Maybe! There is even a high chance it will, since this extension does not make use of any Quarkus-related functionality.
For installation instructions, please refer to an [older version of this readme](https://github.com/sventorben/keycloak-home-idp-discovery/blob/v16.0.0/README.md).

Please note that with the release of Keycloak 20.0.0 the Wildfly-based distro is no longer supported.
Hence, I dropped support for the Wildfly-based distro already. Though this library may still work with the Wildfly-based distro, I will no longer put any efforts into keeping this extension compatible.

### Does it work with Keycloak / RedHat SSO version X.Y.Z?

If you are using Keycloak version `X` (e.g. `X.y.z`), version `X.b.c` should be compatible.
Keycloak SPIs are quite stable. So, there is a high chance this authenticator will work with other versions, too. Check the details of latest [build results](https://github.com/sventorben/keycloak-home-idp-discovery/actions/workflows/buildAndTest.yml) for an overview or simply give it a try.

Authenticator version `X.b.c` is compiled against Keycloak version `X.y.z`. For example, version `16.3.1` will be compiled against Keycloak version `16.y.z`.

I do not guarantee what version `a.b` or `y.z` will be. Neither do I backport features to older version, nor maintain any older versions of this authenticator. If you need the latest features or bugfixes for an older version, please fork this project or update your Keycloak instance. I recommend doing the latter on regular basis anyways.

For RedHat SSO versions, please check the corresponding Keycloak version [here](https://access.redhat.com/articles/2342881). Above rules apply ;)

### User does not get redirected, but only sees a password form instead.
Make sure that your users email is marked as verified. You can enable the `Email verified` flag per user or switch on `Trust Email` in the advanced settings of the identity provider.

### User is not redirected to the correct identity provider. How to analyze the problem?
You may want to increase the log level to see more fine-grained information on how the authenticator discovered the home identity provider.
Try to increase the log level to `DEBUG` or even `TRACE` level. Details can be found in the official [Configuring logging](https://www.keycloak.org/server/logging) guide.
The log category for the authenticator is `de.sventorben.keycloak.authentication.hidpd`.
