angular.module('synsormed.services.patient', [
    'synsormed.services.error',
    'synsormed.env'
])
.service('synsormed.services.patient.PatientService', [
    '$http',
    '$q',
    'synsormed.services.error.http',
    'synsormed.env.urlBase',
    function ($http, $q, HttpError, urlBase) {
        return {
            getProviderStatus: function (providerId) {
                var deferred = $q.defer();
                $http.get(urlBase + '/v1/rest/status/provider/' + providerId, {}, {timeout: 5000}).then(function (resp) {
                    deferred.resolve(resp.data);
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