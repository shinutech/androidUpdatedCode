angular.module('synsormed.controllers', [
    'synsormed.controllers.login',
    'synsormed.controllers.patient.call',
    'synsormed.controllers.patient.agreement',
    'synsormed.controllers.provider.list',
    'synsormed.controllers.provider.createEncounter',
    'synsormed.controllers.provider.showEncounter',
    'synsormed.controllers.patient.pay',
    'synsormed.controllers.patient.connect',
    'synsormed.controllers.provider.notes',
    'synsormed.services.logging',
    'synsormed.controllers.provider.forgotPass',
    'synsormed.controllers.provider.survey',
    'synsormed.controllers.service',
    'synsormed.controllers.network',
    'synsormed.controllers.provider.monitor',
    'synsormed.controllers.monitor.agreement',
    'synsormed.controllers.monitor.read',
    'synsormed.controllers.monitor.appointment',
    'synsormed.controllers.monitor.document'
])
.controller('ApplicationController', [
    '$scope',
    '$rootScope',
    '$timeout',
    '$location',
    '$route',
    'synsormed.services.logging.time',
    'synsormed.services.video.Connector',
    function ($scope, $rootScope, $timeout, $location, $route, timeLogging, WeemoConnector) {
        $scope.notification = {
            type: 'error',
            message: '',
            show: false
        };

        $scope.alterations = {
            body: ''
        };

        var notificationTimeout = null;
        var pauseTime = null;

        document.addEventListener("pause", function () {
            //notify application is in background now
            $scope.$broadcast("app:pause");
            console.log("Pausing App");

            pauseTime = new Date();
            timeLogging.log('leaving app');
        }, false);

        document.addEventListener("resume", function () {
            //notify application is in foreground now
            $scope.$broadcast("app:resume");
            console.log("Resuming App");

            if(pauseTime && (new Date()).getTime() - pauseTime.getTime() >= 4 * 60 * 1000) {
                //App has timed out. Disconnect Video and go to login screen
                console.log("Resuming App");
                WeemoConnector.disconnect(true);
                $location.path('#/login');
            }
        });

        $rootScope.$on('$routeChangeError', function (err) {
            timeLogging.log('$routeChangeError');
        });


        var notificationId = null;

        /**
        * These should be collapsed into a less repetative factory
        */
        $rootScope.$on('notification', function (evt, message) {
            $scope.notification = message;
            $scope.notification.show = true;
            if(notificationId) {
                $timeout.cancel(notificationId);
            }
            notificationId = $timeout(function () {
                $scope.notification.show = false;
            }, 5000);
        });

        $rootScope.$on('service:error', function (evt, errorModel) {
            $scope.$emit('notification', {
                type: 'danger',
                message: errorModel.toString()
            });
        });

        $rootScope.$on('notification:error', function (evt, message) {
            $scope.$emit('notification', {
                type: 'danger',
                message: message
            });
        });

        $rootScope.$on('notification:information', function (evt, message) {
            $scope.$emit('notification', {
                type: 'info',
                message: message
            });
        });

        $rootScope.$on('notification:success', function (evt, message) {
            $scope.$emit('notification', {
                type: 'success',
                message: message
            });
        });

        $rootScope.$on('notification:warning', function (evt, message) {
            $scope.$emit('notification', {
                type: 'warning',
                message: message
            });
        });
    }]);
