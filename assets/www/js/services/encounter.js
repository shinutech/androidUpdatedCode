angular.module('synsormed.services.encounter', ['LocalStorageModule'])
    .provider('synsormed.services.encounter.EncounterService', [function () {
        this.$get = [
            '$http',
            '$q',
            'synsormed.env.urlBase',
            'synsormed.services.error.http',
            function ($http, $q, urlBase, HttpError) {
                return {
                    //markable events for call
                    EVENT : {
                      START : 'start',
                      READY : 'ready'
                    },
                    getToken: function (encounterId) {
                        return $http.get(urlBase + '/v1/rest/encounter/' + encounterId + '/token').then(function (resp) {
                            return resp.data;
                        }).catch(function (err) {
                            throw new HttpError({
                                code: err.status,
                                message: err.data
                            });
                        });
                    },
                    dropToken: function (encounterId) {
                        return $http.delete(urlBase + '/v1/rest/encounter/' + encounterId + '/token').then(function (resp) {
                            return resp.data;
                        }).catch(function (err) {
                            throw new HttpError({
                                code: err.status,
                                message: err.data
                            });
                        });
                    },
                    updateEncounter: function (encounterData) {
                        var deferred = $q.defer();
                        $http.put(urlBase + '/v1/rest/encounter/' + encounterData.id, encounterData).then(function (resp) {
                            deferred.resolve(resp.data);
                        }).catch(deferred.reject);
                        return deferred.promise;
                    },
                    getEncounter: function (id) {
                        var deferred = $q.defer();
                        $http.get(urlBase + '/v1/rest/encounter/' + id).then(function (resp) {
                            deferred.resolve(resp.data);
                        }).catch(deferred.reject);
                        return deferred.promise;
                    },
                    // save call duration for an encounter
                    logEncounterCallDuration : function(id,seconds){
                      var deferred = $q.defer();
                      $http.put(urlBase + '/v1/rest/encounter/' + id + '/call/duration', {
                        duration : seconds
                      }).then(function (resp) {
                          deferred.resolve(resp.data);
                      }).catch(deferred.reject);
                      return deferred.promise;
                    },
                    //get patient name
                    getCodeName : function(id){
                      var deferred = $q.defer();
                      $http.get(urlBase + '/v1/rest/encounter/' + id + '/code/name')
                           .then(function (resp) {
                               console.log(64,resp.data);
                                deferred.resolve(resp.data);
                            })
                           .catch(deferred.reject);
                      return deferred.promise;
                    },
                    //mark call events
                    markCallEvent : function(id,event){
                      var deferred = $q.defer();
                      $http.put(urlBase + '/v1/rest/encounter/' + id + '/call/mark/' + event,{})
                           .then(function (resp) {
                                deferred.resolve(resp.data);
                            })
                           .catch(deferred.reject);
                      return deferred.promise;
                    },
                    createEncounter : function(data){
                        var deferred = $q.defer();
                        $http.post(urlBase + '/v1/rest/encounter', data)
                             .then(function (resp) {
                                  deferred.resolve(resp.data);
                              })
                             .catch(deferred.reject);
                        return deferred.promise;
                    },
                    linkServices : function(id, data){
                       var deferred = $q.defer();
                       $http.put(urlBase + '/v1/rest/encounter/'+ id +'/link', data).then(function (resp) {
                            deferred.resolve(resp.data);
                       }).catch(deferred.reject);
                       return deferred.promise;
                    }
                };
            }];
    }]);
