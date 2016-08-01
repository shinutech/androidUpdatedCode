angular.module('synsormed.services.monitor', ['LocalStorageModule'])
.service('synsormed.services.monitor.MonitorMeasurementService',[
  '$http',
  '$q',
  'synsormed.env.urlBase',
  function($http, $q, urlBase){
    return {
      getMonitors : function(providerId)
      {
        var deferred = $q.defer();
        $http.get(urlBase + '/v1/rest/provider/' + providerId + '/monitor').then(function (resp) {
            deferred.resolve(resp.data);
        }).catch(deferred.reject);
        return deferred.promise;
      },
      getMeasurementsForMonitor : function(id){
        var deferred = $q.defer();
        $http.get(urlBase + '/v1/rest/monitor/' + id + '/measurements',{timeout:5000}).then(function (resp) {
            deferred.resolve(resp.data);
        }).catch(deferred.reject);
        return deferred.promise;
      },
      setOauthDataForMeasurement : function(monitorId,monitorMeasurementId,data,oauthUpdateOnly){
        var deferred = $q.defer();
        oauthUpdateOnly = oauthUpdateOnly || false;
        $http.put(urlBase + '/v1/rest/monitor/' + monitorId + '/measurements/' + monitorMeasurementId + '?oauthUpdateOnly=' + oauthUpdateOnly, data, {timeout:15000}).then(function (resp) {
            deferred.resolve(true);
        }).catch(deferred.reject);
        return deferred.promise;
      }
    };
}])
.service('synsormed.services.monitor.MonitorServicesService',[
  '$http',
  '$q',
  'synsormed.env.urlBase',
  function($http, $q, urlBase){
    return {
      getServicesForMonitor : function(id, measurementId){
        var deferred = $q.defer();
        $http.get(urlBase + '/v1/rest/monitor/' + id + '/services?measurementId=' + measurementId, {timeout: 5000})
        .then(function (resp) {
            deferred.resolve(resp.data);
        }).catch(deferred.reject);
        return deferred.promise;
    },
    getConnectedService: function(monitorId){
                var deferred = $q.defer();
                $http.get(urlBase + '/v1/rest/monitor/' + monitorId + '/services/connected', {timeout: 10000}).then(function (resp) {
                    deferred.resolve(resp.data);
                }).catch(deferred.reject);
                return deferred.promise;
        },
    unlinkOauthToken: function(monitorId, oauthId){
                var deferred = $q.defer();
                $http.delete(urlBase + '/v1//rest/monitor/' + monitorId + '/token/' + oauthId).then(function (resp) {
                    deferred.resolve(resp.data);
                }).catch(deferred.reject);
                return deferred.promise;
            }
        };
}])
.service('synsormed.services.monitor.MonitorAppointmentService',[
  '$http',
  '$q',
  'synsormed.env.urlBase',
  function($http, $q, urlBase){
    return {
      setAppointment : function(monitorId,date){
        var deferred = $q.defer();
        $http.post(urlBase + '/v1/rest/monitor/' + monitorId + '/appointment/confirm', { date : date }, {timeout:15000}).then(function (resp) {
            deferred.resolve(resp.data);
        }).catch(deferred.reject);
        return deferred.promise;
      }
    }
  }
])
.service('synsormed.services.monitor.MonitorDocumentService',[
    '$http',
    '$q',
    'synsormed.env.urlBase',
    'localStorageService',
    function($http, $q, urlBase, localStorageService){
       return {
           getDocuments : function(monitorId){
               var deferred = $q.defer();
               $http.get(urlBase + '/v1/rest/monitor/'+ monitorId +'/documents/', {timeout:15000})
               .then(function(resp){
                   deferred.resolve(resp.data);
               }).catch(deferred.reject);
               return deferred.promise;
           },
           getToken : function(monitorId, file, diseasesId){
              var deferred = $q.defer();
               $http.get(urlBase + '/v1/file/token?monitorId='+monitorId+'&fileName='+file+'&diseasesId='+diseasesId, {timeout:15000})
               .then(function(resp){
                   deferred.resolve(resp.data);
               }).catch(deferred.reject);
               return deferred.promise;
           },
           getFile : function(token, fileName){
               var deferred = $q.defer();
               if(!token || !fileName) return defer.reject('No token found');
               handleDocumentWithURL(
                   function() {
                       deferred.resolve(true);
                       console.log('success');
                   },
                   function(error) {
                       deferred.reject(error);
                   },
                   encodeURI(urlBase + '/v1/file/'+token+'/'+fileName)
               );
               return deferred.promise;
           },
           markRead : function(monitorId, diseasesId, filename){
               var deferred = $q.defer();
               $http.put(urlBase + '/v1/rest/monitor/'+ monitorId +'/documents/', {diseasesId: diseasesId, fileName: filename }, {timeout:15000})
               .then(function(resp){
                   deferred.resolve(resp.data);
               }).catch(deferred.reject);
               return deferred.promise;
           }
       }
    }
]);
