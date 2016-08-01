'use strict';

angular.module('synsormed.components.menu',['pageslide-directive'])
.controller('synsormed.components.menu.MenuController',[
  '$scope',
  '$location',
  function($scope,$location){
    $scope.isLoggedIn = false;
    $scope.isProvider = false;
    $scope.navigationVisible = false;
    $scope.toggle = function(){
      $scope.navigationVisible = !$scope.navigationVisible;
    };
    $scope.sync = function(){
        $location.path('/patient/connect');
    };
    $scope.logout = function(){
        $location.path('/logout');
    };
    $scope.getMonitors = function(){
        $location.path('/provider/monitor');
    };
    $scope.getVisits = function(){
        $location.path('/provider/list');
    };
}])
.directive('synsormedMenu',[function(){
  return {
   controller : 'synsormed.components.menu.MenuController',
   restrict : 'E',
   templateUrl : 'js/components/menu/menu.html',
   scope : {
     isLoggedIn : "@",
     isProvider : "@"
   }
 };
}]);
