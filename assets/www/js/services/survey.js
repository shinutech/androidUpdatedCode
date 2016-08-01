angular.module('synsormed.services.survey', [
    'LocalStorageModule',
    'synsormed.services.user'
]).service('synsormed.services.survey.SurveyService', [
    '$http',
    '$q',
    'synsormed.services.user.UserService',
    'synsormed.env.urlBase',
    'synsormed.services.error.http',
    function ($http, $q, UserService, urlBase, HttpError) {
        return {
            saveSurveyAnswers: function (encounterId, answers) {
                var deferred = $q.defer();
                $http.post(urlBase + '/v1/rest/encounter/' + encounterId + '/answers', answers).then(function (resp) {
                    deferred.resolve(resp.data);
                }).catch(function (err) {
                    deferred.reject(new HttpError({
                        code: err.status,
                        message: err.data
                    }));
                });
                return deferred.promise;
            }
        }
    }
])