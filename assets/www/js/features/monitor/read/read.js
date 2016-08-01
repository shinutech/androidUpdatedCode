angular.module('synsormed.controllers.monitor.read',[
    'synsormed.services.user',
    'synsormed.services.oauth',
    'synsormed.services.monitor'
])
.controller('MonitorReadController',[
    '$scope',
    '$modal',
    '$location',
    '$window',
    'synsormed.services.user.UserService',
    'synsormed.services.user.UserModel',
    'synsormed.services.oauth.OauthService',
    'synsormed.services.monitor.MonitorMeasurementService',
    'synsormed.services.monitor.MonitorServicesService',
    'synsormed.services.healthkit.HealthkitService',
    'synsormed.services.oauth.OauthDataManagementService',
    '$q',
    'localStorageService',
    'synsormed.services.monitor.MonitorDocumentService',
    function($scope, $modal, $location, $window, UserService, UserModel, OauthService, MonitorMeasurementService, MonitorServicesService, HealthkitService, OauthDataManagementService, $q, localStorageService, MonitorDocumentService){
        $scope.services = [];
        $scope.measurements = [];
        $scope.monitor = UserService.getUser();

        $scope.healthData = {
            data : null,
            service : null
        };

        $scope.$emit("wait:start");
        MonitorDocumentService
        .getDocuments($scope.monitor.id)
        .then(function(documents){
            $scope.$emit("wait:stop");
            $scope.documents = [];
            documents.forEach(function(document){
                $scope.documents = $scope.documents.concat(document.files);
            });
        })
        .catch(function(err){
            console.log(err);
            $scope.$emit("wait:stop");
            $scope.$emit('notification:error', 'Server Error');
        });




        $scope.healthkitAvailable = false;
        //check if Healthkit is available or not on start up
        HealthkitService
        .checkAvailable()
        .then(function(){
            $scope.healthkitAvailable = true;
        })
        .catch(function(err){
            $scope.healthkitAvailable = false;
        })

        //watch to update autoFetch of monitor
        $scope.$watch('monitor.autoFetch', function(newVal, oldVal){
            if (newVal !== oldVal) {
                $scope.$emit("wait:start");
                $scope.monitor.autoFetch = newVal;
                $scope.monitor.save(false).then(function(user){
                    $scope.$emit('notification:success', 'Auto Fetch Updated');
                    $scope.$emit("wait:stop");
                }).catch(function(err){
                    console.error(err);
                    $scope.$emit('notification:error', 'Server Error');
                });
            }
        });
        $scope.refreshList = function(){
            //get all the measurements
            $scope.$emit("wait:start");

            MonitorMeasurementService
            .getMeasurementsForMonitor($scope.monitor.id)
            .then(function(measurements){
                $scope.measurements = measurements;
                var oauthAvailable = true;
                $scope.measurements.forEach(function(measurement){
                    if(!measurement.oauthAvailable)
                    {
                        oauthAvailable = false;
                        return false;
                    }
                });
                $scope.oauthAvailable = oauthAvailable ? true : false;
                $scope.$emit("wait:stop");
            })
            .catch(function(e){
                $scope.measurements = [];
                $scope.$emit("notification:error", "Unable to load measurements");
                $scope.$emit("wait:stop");
            });
        }
        $scope.helpModal = function(){
            var modalInstance = $modal.open({
                templateUrl: 'js/features/monitor/read/autoFetchHelp.html',
                controller: 'HelpModalController'
            });
        };

        $scope.goToDocumentsPage = function(){
            $location.path('/monitor/documents');
        };

        $scope.servicesList = function(measurementMap){
            $scope.$emit('wait:start');
            var modalInstance = $modal.open({
                templateUrl: 'js/features/monitor/read/servicelist.html',
                controller: 'serviceListModalController',
                resolve: {
                    monitor: function () {
                        return $scope.monitor;
                    },
                    measurementMap: function(){
                        return measurementMap;
                    }

                }});

                //reload the monitor list
                modalInstance.result
                .then(function(oauthData){

                    if(!oauthData){
                        $scope.$emit("notification", {
                            type: 'danger',
                            message: "Synsormed Connect Failed"
                        });
                        return;
                    }
                    else if(typeof oauthData == 'object'){
                        //update this data with monitor
                        MonitorMeasurementService
                        .setOauthDataForMeasurement($scope.monitor.id, measurementMap.id, oauthData)
                        .then(function(data){
                            $scope.$emit("notification", {
                                type: 'success',
                                message: "Synsormed Connected"
                            });
                            $scope.$emit('wait:stop');
                            $scope.refreshList();
                        })
                        .catch(function(err){
                            if(err.status == 422)
                            {
                                $scope.$emit("notification", {
                                    type: 'danger',
                                    message: err.data
                                });
                            }
                            $scope.$emit('wait:stop');
                            $scope.refreshList();
                        });
                    }

                    else if(oauthData == true){
                        $scope.$emit("notification", {
                            type: 'success',
                            message: "Synsormed Connected"
                        });
                    }
                });

                modalInstance.result.finally(function(){
                    $scope.$emit('wait:stop');
                    $scope.refreshList();
                });
            };


            //update healthkit data
            var updateHealthkitData = function(monitor, measurements){

                var promises = [];
                var uploadDataToServer = function(measurementMap){
                    return HealthkitService
                    .performAuth()
                    .then(function(data) {
                       if(data === true) {
                         return HealthkitService.readData().then(function(readings){
                            return MonitorMeasurementService
                                    .setOauthDataForMeasurement(monitor.id, measurementMap.id,{
                                        'service_name': 'healthkit',
                                        'oauth_data': readings
                                    }, true);
                          });
                       } else {
                          return false;
                       }
                   });
               };

                measurements
                .forEach(function(measurement){
                    if(measurement.serviceName != null && measurement.serviceName.toLowerCase() == 'healthkit'){
                       promises.push(uploadDataToServer(measurement));
                    }
                });

                return $q.all(promises);
            };


            // update healthkit data
            $scope.syncHealthKitData = function(monitor, measurements, autoSync){
                monitor = monitor ? monitor : $scope.monitor;
                measurements = measurements ? measurements : $scope.measurements;
                if(!monitor || !measurements || !autoSync) return;

                $scope.syncing = true;
                updateHealthkitData(monitor, measurements)
                .then(function(data){
                    var showSuccessMsg = data.every(function(val){ return !!val });
                    var showPermissionErrMsg = data.every(function(val){ return !val });

                    if(data.length && showSuccessMsg){
                       $scope.$emit('notification:success', 'Healthkit data uploaded');
                    }
                    if(data.length && showPermissionErrMsg){
                       $scope.$emit('notification:error', 'Healthkit permission denied');
                    }
                    $scope.refreshList();
                    $scope.syncing = false;
                })
                .catch(function(){
                    $scope.$emit('notification:error', 'Healthkit data syncing failed');
                    $scope.syncing = false;
                });
            };

            $scope.showSyncBar = function(){
                if(!$scope.measurements) return false;
                return $scope.measurements.some(function(measurement){
                    return (measurement.serviceName != null && measurement.serviceName.toLowerCase() == 'healthkit');
                });
            };

            //get all the measurements
            $scope.$emit("wait:start");

            MonitorMeasurementService
            .getMeasurementsForMonitor($scope.monitor.id)
            .then(function(measurements){
                if(!measurements.length){
                    $scope.$emit("wait:stop");
                    return;
                }
                $scope.measurements = measurements;

                var autoSync = !localStorageService.get('stopAutoSyncHealthkitData');
                $scope.syncHealthKitData($scope.monitor, $scope.measurements, autoSync);
                localStorageService.set('stopAutoSyncHealthkitData', true);

                var oauthAvailable = true;
                $scope.measurements
                .forEach(function(measurement){
                    if(!measurement.oauthAvailable)
                    {
                        oauthAvailable = false;
                        return false;
                    }
                });
                if(oauthAvailable){
                    $scope.oauthAvailable = true;
                }
                else{
                    $scope.oauthAvailable = false;
                }
                $scope.$emit("wait:stop");
            })
            .catch(function(e){
                $scope.measurements = [];
                $scope.$emit("notification:error", "Unable to load measurements");
                $scope.$emit("wait:stop");
            });

            //get all the services
            MonitorServicesService
            .getServicesForMonitor($scope.monitor.id)
            .then(function(services){
                $scope.services = services;
                $scope.$emit("wait:stop");
            })
            .catch(function(e){
                $scope.services = [];
                $scope.$emit("notification:error", "Unable to find any services");
                $scope.$emit("wait:stop");
            });
        }])
        .controller('HelpModalController',['$scope', '$modalInstance', function($scope, $modalInstance){
            $scope.ok = function(){
                $modalInstance.close();
            };
        }])
        .controller('serviceListModalController',
        ['$scope',
        '$window',
        'monitor',
        'measurementMap',
        '$modalInstance',
        'synsormed.services.monitor.MonitorServicesService',
        'synsormed.services.oauth.OauthService',
        'synsormed.services.healthkit.HealthkitService',
        'synsormed.services.monitor.MonitorMeasurementService',
        function($scope, $window, monitor, measurementMap, $modalInstance, MonitorServicesService, OauthService, HealthkitService, MonitorMeasurementService){

            $scope.healthData = {
                data : null,
                service : null
            };

            $scope.ok = function(){
                $modalInstance.dismiss();
            };

            HealthkitService
            .checkAvailable()
            .then(function(data){
               $scope.healthkitAvailable = true;
            })
            .catch(function(err){
               $scope.healthkitAvailable = false;
            })

            $scope.checkAvailable = function(service){
                return (service && service.name.toLowerCase() == 'healthkit' && !$scope.healthkitAvailable) ? false : true;
            };

            MonitorServicesService
            .getServicesForMonitor(monitor.id, measurementMap.measurementId)
            .then(function(services){
                $scope.services = services;

                MonitorServicesService
                .getConnectedService(monitor.id)
                .then(function(serviceName){
                    $scope.$emit("wait:stop");
                    for(var i = 0; i < $scope.services.length; i++){
                        for(var j = 0; j < serviceName.length; j++){
                            if($scope.services[i].name == serviceName[j].service_name){
                                $scope.services[i].connected = serviceName[j];
                            }
                        }
                    }
                });
            })
            .catch(function(e){
                $scope.services = [];
                $scope.$emit("notification:error", "Unable to find any services");
                $scope.$emit("wait:stop");
            });

            $scope.startOauth = function(service){
                var serviceData = $scope.services[service];
                $scope.isConnected = service.connected;

                if(!$scope.isConnected){
                    var oauthData = OauthService.getStoredService();
                    var selectedService = service;

                    if(!_.isEmpty(oauthData)){
                        //check if already connected
                        if(!!oauthData.name && !!oauthData.data && oauthData.name == selectedService.name){
                            $scope.healthData.data = oauthData.data;
                            $scope.healthData.service = oauthData.name;
                            $scope.$emit('notification:success', 'Synsormed Connected');
                            return;
                        }
                    }

                    //if service is healthkit
                    if(selectedService.name === 'healthkit'){
                        HealthkitService
                        .performAuth()
                        .then(function(data){
                            if(data === true)
                            {
                                $scope.$emit('wait:start');
                                HealthkitService.readData()
                                .then(function(data){
                                    $scope.$emit("wait:stop");
                                    $scope.healthData.data = data;
                                    $scope.healthData.service = 'healthkit';
                                    $modalInstance.close({'service_name': 'healthkit', 'oauth_data': data});
                                    $scope.$emit('notification:success', 'Synsormed Connected');
                                })
                                .finally(function(){
                                   $scope.$emit("wait:stop");
                                   $scope.refreshList();
                                });
                            }
                            else {
                                $modalInstance.close(false);
                            }

                        })
                        .catch(function(e){
                            console.log(e);
                            $scope.$emit('notification:error', 'Healthkit Connect Failed');
                        });
                        return;
                    }
                    // need plugin https://github.com/apache/cordova-plugin-inappbrowser.git to work
                    var ref = $window.open(selectedService.apiUrl + '?monitorId=' + monitor.id +'&measurementMapId=' +measurementMap.id, '_blank', 'location=no,enableviewportscale=yes');

                    var metaContent = "width=device-width, initial-scale=1.0, maximum-scale=1.0";

                    var scriptMetaInsert = 'var meta = document.querySelector("meta[name=viewport]");' +
                    'if(meta){' +
                    'meta.parentNode.removeChild(meta);' +
                    '}' +
                    'meta = document.createElement("meta");' +
                    'meta.setAttribute("name","viewport");' +
                    'meta.setAttribute("content","' + metaContent + '");'

                    ref.addEventListener('loadstop', function(event) {
                        ref.executeScript({ code: scriptMetaInsert }, function(value){
                            if(OauthService.hasUrl(event.url, selectedService.callback)) {
                                    $scope.$emit('notification:success', 'Synsormed Connect Success');
                                    $modalInstance.close(true);
                                    ref.close();
                                    MonitorMeasurementService
                                     .setOauthDataForMeasurement(monitor.id, measurementMap.id, {'service_name': service.name})
                                     .then(function(data){
                                         $scope.$emit("wait:stop");
                                         $scope.$emit("notification", {
                                             type: 'success',
                                             message: "Synsormed Connected"
                                         });
                                         $modalInstance.close(true);
                                     }).finally(function(){
                                         $scope.$emit("wait:stop");
                                         $scope.refreshList();
                                     });
                            }
                        });
                    });
                }
                else {

                    $scope.$emit('wait:start');
                    MonitorMeasurementService
                     .setOauthDataForMeasurement(monitor.id, measurementMap.id, {'service_name': service.name})
                     .then(function(data){
                         $scope.$emit("wait:stop");
                         $scope.$emit("notification", {
                             type: 'success',
                             message: "Synsormed Connected"
                         });
                     })
                     .finally(function(){
                         $scope.$emit("wait:stop");
                         $modalInstance.close(true);
                         $scope.refreshList();
                     });
                }
            };

        }]);
