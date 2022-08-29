* [deps] Update Keycloak dependencies to 19.0.1
* [feat] Support custom user attribute to lookup email address. <br>This may be useful if you want to redirect users based on their User Principal Name (UPN) instead of their email address.
* [feat] Support different domains per user attribute. <br>This may be useful if you want to add multiple instances of the authenticator using different user attributes. For example, you may add an authenticator that is using email domains while another is using UPN domains.
* [refactor] Mark REST resources as deprecated. Resources will be removed in future versions.
