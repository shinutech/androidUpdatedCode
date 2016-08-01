angular.module('synsormed.controllers.monitor.agreement', [
        'synsormed.services.user'
    ])
    .controller('MonitorAgreementController', [
        '$scope',
        '$location',
        'synsormed.services.user.UserService',
        'synsormed.services.logging.time',
        function($scope, $location, UserService, TimeLogger) {
        TimeLogger.log("AgreementScreen");
        $scope.gotoLogon = function() {
            console.log("Trying to go to logon");
            $location.path('/login');
        };

        $scope.continue = function() {
            //console.log("Trying to go to cc");
            var user = UserService.getUser();
            user.termsAccepted = true;
            user.save().then(function () {
                $location.path('/monitor/read');
            }).catch(function (err) {
                $scope.$emit('service:error', err);
            });
        };

    }]);
