module.controller('HomeIdpDiscoveryConfigCtrl', function($scope, realm, homeIdpDiscoveryConfig, Notifications, $route, Dialog) {

    $scope.realm = realm;
    $scope.homeIdpDiscoveryConfig = homeIdpDiscoveryConfig

    $scope.page = 0;

    $scope.query = {
        realm: realm.realm,
        max: 20,
        first: 0
    };

    $scope.searchQuery = function() {
        $scope.searchLoaded = false;

        $scope.details = HomeIdpDiscoveryConfig.query($scope.query, function() {
            $scope.searchLoaded = true;
            $scope.lastSearch = $scope.query.search;
        });
    };

    $scope.removeBeer = function(beer) {
        Dialog.confirmDelete(beer.name, 'beer', function() {
            beer.$remove({
                realm : realm.realm,
                beerId : beer.id
            }, function() {
                $route.reload();
                Notifications.success("Beer has been deleted.");
            }, function() {
                Notifications.error("Beer couldn't be deleted");
            });
        });
    };

});
