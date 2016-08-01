"use strict";
angular.module('synsormed.controllers.patient.connect', ['synsormed.services.user', 'synsormed.services.oauth'])
.controller('patientConnectController', [
    '$scope',
    '$location',
    'synsormed.services.user.UserService',
    'synsormed.services.oauth.OauthService',
    'synsormed.services.oauth.OauthDataManagementService',
    'synsormed.services.monitor.MonitorServicesService',
     function($scope, $location, UserService, OauthService, OauthDataManagementService, MonitorServicesService){
    $scope.encounter = false;
    $scope.user = UserService.getUser();

    $scope.unLink = function(service){
        $scope.$emit('wait:start');
        if($scope.user.isMonitor)
        {

            MonitorServicesService
            .unlinkOauthToken($scope.user.id, service.id)
            .then(function(data){
                OauthService.removeService(service.service_name)
                $scope.refreshList();
                $scope.$emit('wait:stop');
            });
        }
        else {
            OauthService.unlinkEncounter($scope.user.id)
                .then(function(){
                    OauthService.clearStoredService();
                    $scope.$emit('notification:success', 'Device Unlink Successful');
                    $scope.$emit('wait:stop');
                    $scope.connectedService = false;
                })
                .catch(function(err){
                    $scope.$emit('notification:error', 'Device Unlinking Failed');
                    $scope.$emit('wait:stop');
                });
        }
    };

    $scope.refreshList = function(){
        MonitorServicesService
        .getConnectedService($scope.user.id)
        .then(function(data){
            $scope.$emit("wait:stop");
            $scope.connectedService = data;
        })
        .catch(function(){
            $scope.connectedService = false;
        });
    };

    $scope.unLinkEncounter = function(){
        $scope.$emit('wait:start');
        OauthService
        .unlinkEncounter($scope.user.id)
        .then(function(data){
            OauthService.clearStoredService();
            $scope.$emit('notification:success', 'Device Unlink Successful');
            $scope.$emit('wait:stop');
            $location.path('/logout');
        })
        .catch(function(err){
            console.error(err);
            $scope.$emit('notification:error', 'Device Unlinking Failed');
            $scope.$emit('wait:stop');
        })
    };
    if($scope.user.isMonitor)
    {
        $scope.encounter = false;
        $scope.$emit("wait:start");
        MonitorServicesService
        .getConnectedService($scope.user.id)
        .then(function(data){
            $scope.$emit("wait:stop");
            $scope.connectedService = data;
        })
        .catch(function(){
            $scope.connectedService = false;
        });
    }
    else {
        $scope.encounter = true;
    }
}]);
