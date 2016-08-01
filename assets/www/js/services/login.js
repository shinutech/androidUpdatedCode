angular.module('synsormed.services.authentication', [
    'synsormed.services.error',
    'synsormed.env',
    'synsormed.services.oauth',
    'synsormed.services.healthkit'
])
    .service('synsormed.services.authentication.Login', [
        '$http',
        '$q',
        'synsormed.services.error.http',
        'localStorageService',
        'synsormed.env.urlBase',
        '$rootScope',
        'synsormed.services.user.UserModel',
        'synsormed.services.logging.time',
        'synsormed.services.oauth.OauthService',
        'synsormed.services.healthkit.HealthkitService',
        'synsormed.services.oauth.OauthDataManagementService',
        function ($http, $q, HttpError, localStorageService, urlBase, $rootScope, UserModel, TimeLogger, OauthService, HealthkitService, OauthDataManagementService) {

            //read various services and check if any data available to transmit for provider
            var getAvailableData = function(){
                var deferred = $q.defer();

                //var connectedService = OauthService.getStoredService();
                var connectedService = OauthService.getDefaultConnectedService();
                var callParams = {};

                if(!_.isEmpty(connectedService)){
                    callParams.service = connectedService.name;
                    callParams.data = connectedService.data;
                    deferred.resolve(callParams);

                }
                else {
                    //get if health data is available
                    HealthkitService
                    .checkAuth(null,function(result){
                        //if permissions are granted read the data
                        if(result === true) {
                            HealthkitService.readData()
                            .then(function(data){
                                callParams.service = 'healthkit';
                                callParams.data = data;
                                deferred.resolve(callParams);
                            })
                            .catch(function(err){
                                deferred.reject(err);
                            });
                        }
                        else{
                            deferred.resolve(callParams);
                        }

                    },function(err){
                        deferred.reject(err);
                    });
                }

                return deferred.promise;
            };

            return {
                getAvailableData: getAvailableData,
                //method will perform the patient auth with server
                patientAuth : function(code){
                  var callParams = { code : code };
                  var deferred = $q.defer();
                  $http.post(urlBase + '/v1/authenticate/encounter', callParams , {timeout: 5000}).then(function (resp) {
                      console.log(resp)
                      if(resp.data.csrfToken) {
                          localStorageService.set('x-csrf', resp.data.csrfToken);
                          $http.defaults.headers.common['x-csrf'] = '"' + resp.data.csrfToken + '"';
                      }
                      localStorageService.set('X-Session-Token', resp.headers('X-Session-Token'));
                      $http.defaults.headers.common['X-Session-Token'] = resp.headers('X-Session-Token');
                      deferred.resolve(new UserModel(resp.data));
                  }).catch(function (err) {
                      deferred.reject(new HttpError({
                          code: err.status,
                          message: err.data
                      }));
                  });
                  return deferred.promise;
                },

                providerAuth: function (email, password) {
                    var deferred = $q.defer();
                    $http.post(urlBase + '/v1/authenticate', {
                        username: email,
                        password: password
                    }, {timeout: 5000}).then(function (resp) {

                        if(resp.data.user.role == 'Provider')
                        {
                            if(resp.data.csrfToken) {
                                localStorageService.set('x-csrf', resp.data.csrfToken);
                                $http.defaults.headers.common['x-csrf'] = '"' + resp.data.csrfToken + '"';
                            }
                            localStorageService.set('X-Session-Token', resp.headers('X-Session-Token'));
                            $http.defaults.headers.common['X-Session-Token'] = resp.headers('X-Session-Token');
                            deferred.resolve(new UserModel(resp.data.user));
                        }
                        else {
                            deferred.reject(new Error('Provider account is required to use this section'));
                        }
                    }).catch(function (err) {
                        deferred.reject(new HttpError({
                            code: err.status,
                            message: err.data
                        }));
                    });
                    return deferred.promise;
                }
            };
        }
    ]);
