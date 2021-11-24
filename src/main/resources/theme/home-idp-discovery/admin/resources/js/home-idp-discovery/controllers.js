module.controller('HomeIdpDiscoveryConfigCtrl', function($scope, realm, homeIdpDiscoveryConfig, identityProvider, Notifications, $route, Dialog) {

    $scope.realm = realm;
    $scope.homeIdpDiscoveryConfig = homeIdpDiscoveryConfig
    $scope.identityProvider = identityProvider

    $scope.searchQuery = function() {
        $scope.searchLoaded = false;

        $scope.details = HomeIdpDiscoveryConfig.query($scope.query, function() {
            $scope.searchLoaded = true;
            $scope.lastSearch = $scope.query.search;
        });
    };

});
