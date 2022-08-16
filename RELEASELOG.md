* [deps] Update Keycloak dependencies to 19.0.1
* [feat] Support custom user attribute to lookup email address. <br>This may be useful if you want to redirect users based on their User Principal Name (UPN) instead of their email address.

> ⚠️ **Compatibility issues**:
>
> Due to changes in the `KeycloakSession` this extension will no longer be backwards compatible with Keycloak versions < 19.x.
> For details please see https://www.keycloak.org/2022/07/keycloak-1900-released#_changes_in_keycloaksession

> ⚠️ **Production readiness**:
>
> This is a pre-release and not production ready, yet.
> Consider this to be a proof of concept for now.
>
>
