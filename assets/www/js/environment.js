angular.module('synsormed.env', [])
    .value('synsormed.env.weemo.appId', 'ukjegju45xsp')

    .value('synsormed.env.urlBase', 'http://api-staging.synsormed.com') /* mobile app */
    //.value('synsormed.env.urlBase', 'http://api-test-synsormed.herokuapp.com') /* mobile app test server */

    //.value('synsormed.env.urlBase', 'http://10.0.2.2:9000') /* emulator */
    //.value('synsormed.env.urlBase', 'http://127.0.0.1:9000') /* local */

    //in milliseconds, refresh grade of network in all application , 0 network cost
    .value('synsormed.env.network.scan.check',1000) //in milliseconds

    //in milliseconds, time after which network will be scanned to refresh grade , high network cost
    .value('synsormed.env.network.scan.interval',20000); // in milliseconds
