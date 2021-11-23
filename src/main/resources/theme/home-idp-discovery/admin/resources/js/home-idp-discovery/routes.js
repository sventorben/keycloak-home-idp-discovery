module.config([ '$routeProvider', function($routeProvider) {
    $routeProvider
        .when('/realms/:realm/identity-provider-home-idp-discovery/:alias/home-idp-discovery', {
            templateUrl : resourceUrl + '/partials/home-idp-disovery-details.html',
            resolve : {
                realm : function(RealmLoader) {
                    return RealmLoader();
                },
                identityProvider : function(IdentityProviderLoader) {
                    return IdentityProviderLoader();
                },
                homeIdpDiscoveryConfig : function (HomeIdpDiscoveryConfigLoader) {
                    return HomeIdpDiscoveryConfigLoader()
                }
            },
            controller : 'HomeIdpDiscoveryConfigCtrl'
        })
} ]);
