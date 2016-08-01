angular.module('synsormed.controllers.provider.monitor', [
  'synsormed.services.user',
  'synsormed.services.monitor'
])
.controller('MonitorListController', [
  '$scope',
  'synsormed.services.user.UserService',
  'synsormed.services.monitor.MonitorMeasurementService',
  function($scope, UserService, MonitorService) {
    var user = UserService.getUser();
    $scope.orderOptions = [ {
      id: 0,
      name: 'All'
    },
    {
      id: 1,
      name: 'Missed'
    },
    {
      id: 2,
      name: 'Out Of Range'
    }
    ];
    $scope.sort = {};
    $scope.sort.sortOrder = $scope.orderOptions[0];

    $scope.reloadView = function() {
      $scope.$broadcast('scroll.refreshComplete');
      //We want to use our own wait spinner
      fetchMonitors();
    };

    $scope.monitorFilter = {};
    $scope.$watch('sort.sortOrder', function() {
      switch ($scope.sort.sortOrder.id) {
        case 0:
        $scope.monitorFilter = {};
        break;

        case 1:
        $scope.monitorFilter = { isMissed: true };
        break;

        case 2:
        $scope.monitorFilter = {isOutofBound: true };
        break;

        default:
        $scope.monitorFilter = {};
    }
  }, true);

    var fetchMonitors = function(){
      $scope.$emit('wait:start');
      MonitorService
      .getMonitors(user.id)
      .then(function(monitors){
        $scope.monitors = monitors;
        $scope.$emit('wait:stop');
      })
      .catch(function(error){
        $scope.$emit('wait:stop');
        console.log(error);
      });
  };
  fetchMonitors();
}]);
