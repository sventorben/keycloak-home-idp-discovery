module.factory('HomeIdpDiscoveryConfig', function($resource) {
    return $resource(authUrl + '/realms/:realm/home-idp-discovery/:idpAlias', {
        realm : '@realm',
        idpAlias: '@idpAlias'
    });
});

module.factory('HomeIdpDiscoveryConfigLoader', function(Loader, HomeIdpDiscoveryConfig, $route, $q) {
    return Loader.get(HomeIdpDiscoveryConfig, function() {
        return {
            realm : $route.current.params.realm,
            idpAlias : $route.current.params.alias
        };
    });
});
