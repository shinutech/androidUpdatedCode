angular.module('synsormed.controllers.login', [
        'synsormed.services.authentication',
        'synsormed.services.user',
        'synsormed.services.oauth'
    ]).
    controller('loginViewController', [
        '$scope',
        '$location',
        '$interval',
        'synsormed.services.user.UserService',
        'synsormed.services.video.Connector',
        'synsormed.services.logging.time',
        '$modal',
        'media',
        function($scope,$location, $interval, UserService, videoConnector, TimeLogger, $modal, Media) {
            UserService.clearUser();
            UserService.clearUserData();
            
            
            $scope.initVideo = function(){
			
				if(!window.videoPlugin) {
        			console.error("!!!!!!!!! Video SERVICE NOT ACTIVE !!!!!!!!!");
    			}
									   
				
				videoConnector.initVideoSystem();
				console.log("Clicked init button");
				$scope.status = "Clicked";
			
			}
            
            

            TimeLogger.log("LoginScreen");
            try {
                videoConnector.removeCall();
            } catch (e) {/* ignore */}
            videoConnector.disconnect(true);

            if(!$scope.hasHighBandwidth){
              $location.path('/network');
            }

            $scope.year = new Date().getFullYear();

            $scope.showTutorial = function () {
                var modalInstance = $modal.open({
                    windowClass: 'howItWorksModal',
                    templateUrl: 'js/features/login/howItWorks.html',
                    controller: 'howItWorksController'
                });
            };

            $scope.showLogin = function (which) {
                $location.path('/login/' + which);
            };

            //about page
            $scope.showAbout = function () {
                var modalInstance = $modal.open({
                    windowClass: 'aboutModal',
                    templateUrl: 'js/features/login/about.html',
                    controller: 'aboutModalController'
                });
            };
        }
    ])
    .controller('patientLoginViewController', [
        '$scope',
        'synsormed.services.authentication.Login',
        '$location',
        '$modal',
        'synsormed.services.oauth.OauthDataManagementService',
        'localStorageService',
        function ($scope, LoginService, $location, $modal, OauthDataManagementService, localStorageService){

            //if there is code in the memory get it
            $scope.code = localStorageService.get('synsormed:patient:code');
            $scope.rememberMe = !!localStorageService.get('synsormed:patient:code');

            $scope.connectedService = false;
            $scope.canShare = OauthDataManagementService.canSendData();

            //get the connected service name
            OauthDataManagementService
            .sharingString()
            .then(function(serviceName){
                $scope.connectedService = serviceName;
            })
            .catch(function(err){
              console.log(err);
              $scope.connectedService = false;
            });

            $scope.needHelp = function () {
                $modal.open({
        			windowClass: 'helpModal',
                    templateUrl: 'js/features/login/help.html',
                    controller: function ($scope, $modalInstance) {
                        $scope.close = function () {
                            $modalInstance.close();
                        }
                    }
                });
            };

            $scope.focusInput = function () {
                //This is a hack due to the UI
                $('input:first').focus();
            };

            $scope.submitForm = function () {
                $scope.$broadcast('validate');

                $scope.form.$setDirty();

                if($scope.code && $scope.code.search(/[^a-zA-Z0-9]/) != -1) {
                    $scope.$broadcast('setInvalid:code', 'characters');
                } else {
                    $scope.$broadcast('setValid:code', 'characters');
                }

                $scope.showLoginError = false;
                if($scope.form.$valid) {
                    attemptLogin();
                } else {
                    console.log($scope.form);
                    //This is more or less a hack due to the UI
                    if($scope.form.$error.required && $scope.form.$error.required.length > 0) {
                        $scope.$emit('notification:error', "Code is required");
                    }
                    if($scope.form.$error.characters && $scope.form.$error.characters.length > 0) {
                        $scope.$emit('notification:error', "Invalid characters entered. Only A-Z and 0-9 allowed.");
                    }
                }
            }

            function attemptLogin() {

                $scope.$emit('wait:start');
                LoginService.patientAuth($scope.code).then(function (user) {

                    if($scope.rememberMe){
                        localStorageService.set('synsormed:patient:code', $scope.code);
                    } else {
                        localStorageService.remove('synsormed:patient:code');
                    }

                    user.save(true); //Saving locally, skipping remote save

                    console.log(user);

                    //check if user is monitor type
                    if(user.isMonitor){

                      //if terms not accepted show them this screen
                      if(!user.termsAccepted) {
                          $location.path('/monitor/terms');
                      } else if (user.appointmentMeta) {
                          $location.path('/monitor/appointment');
                      } else {
                          $location.path('/monitor/read');
                      }

                    } else {

                      //Has the user already agreed to the TOU?
                      if(!user.termsAccepted) {
                          $location.path('/patient/terms');
                      } else if(!user.paid && user.fee > 0) {
                          $location.path('/patient/pay');
                      } else {
                          $location.path('/patient/call');
                      }

                    }

                }).catch(function (err) {
                    if(err.code != 401) {
                        $scope.$emit('service:error', err);
                    } else {
                        $scope.$emit('notification:error', "An invalid code was entered");
                    }
                }).finally(function () {
                    $scope.$emit('wait:stop');
                });
            }

            //show sensor data modal
            $scope.showSensorPopup = function(){
              $modal.open({
                   windowClass: 'sensorModal',
                   templateUrl: 'js/features/login/sensors.html',
                   controller: 'sensorModalController'
               });
            };

        }
    ])
    .controller('providerLoginViewController', [
        '$scope',
        'synsormed.services.authentication.Login',
        '$location',
        'localStorageService',
        'synsormed.services.user.UserService',
        '$modal',
        function ($scope, LoginService, $location, localStorageService, UserService,$modal) {

            $scope.rememberMe = !!localStorageService.get('savedUsername');
            $scope.email = localStorageService.get('savedUsername');

            $scope.needHelp = function () {
                $modal.open({
          			     windowClass: 'helpModal',
                     templateUrl: 'js/features/login/help.html',
                     controller: function ($scope, $modalInstance) {
                         $scope.close = function () {
                             $modalInstance.close();
                         }
                     }
                 });
            };

            $scope.$watch('password', function () {
                $scope.showLoginError = false;
            });

            $scope.submitForm = function () {

                $scope.$broadcast('validate');
                $scope.form.$setDirty();

                $scope.showLoginError = false;
                if($scope.form.$valid) {
                    $scope.$emit('wait:start');
                    if($scope.rememberMe) {
                        localStorageService.set('savedUsername', $scope.email);
                    } else {
                        localStorageService.remove('savedUsername');
                    }
                    LoginService.providerAuth($scope.email, $scope.password).then(function (user) {
                        console.log(user);
                        //if(user.role.)
                        user.save(true);
                        //Has the user already agreed to the TOU?
                        $location.path('/provider/list');
                    }).catch(function (err) {
                        if(err.code != 401) {
                            $scope.$emit('service:error', err);
                        } else {
                            $scope.$emit('notification:error', 'Username/password combination not found.');
                        }
                    }).finally(function () {
                        $scope.$emit('wait:stop');
                    });
                }
            };
        }
    ]).controller('howItWorksController', [
        '$scope', '$modalInstance', '$timeout', 'synsormed.services.logging.time',
        function ($scope, $modalInstance, $timeout, TimeLogger) {
            TimeLogger.log("TutorialScreen");
            $scope.close = function () {
                $modalInstance.dismiss();
            }
            $timeout(function () {

                console.log($(".carousel-inner").height())
                $(".carousel-inner .image").height($(".carousel-inner").height())
            }, 100)
        }
    ]).controller('sensorModalController',[
      '$scope','$modalInstance','$location',
      function($scope, $modalInstance, $location){
        $scope.close = function () {
            $modalInstance.dismiss();
        };

        // go to services
        $scope.serviceLogin = function(){
            $modalInstance.dismiss();
            $location.path('/service/list');
        };

      }
    ])
    .controller('aboutModalController',[
      '$scope','$modalInstance',
      function($scope, $modalInstance){
        $scope.year = new Date().getFullYear();

        // this will need plugin https://github.com/whiteoctober/cordova-plugin-app-version.git
        cordova.getAppVersion(function (version) {
            $scope.version = version;
        });

        $scope.close = function () {
            $modalInstance.dismiss();
        };
      }
    ]);
