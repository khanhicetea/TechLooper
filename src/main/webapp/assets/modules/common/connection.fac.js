angular.module("Common").factory("connectionFactory", function (jsonValue, utils, $http, $q) {
  var socketUri = jsonValue.socketUri;
  var events = jsonValue.events;
  var callbacks = [];
  var scope;
  var subscriptions = {};
  var isConnecting = false;

  //var contextUrl = window.location.protocol + '//' + window.location.host + paths.join('/');
  var stompUrl = baseUrl + '/' + socketUri.sockjs;
  var stompClient = Stomp.over(new SockJS(stompUrl));
  stompClient.debug = function () {};

  // private functions
  var $$ = {
    /** must to call when controller initialized */
    initialize: function ($scope) {
      $$.clearCache();
      scope = $scope;
    },

    clearCache: function () {
      for (var uri in subscriptions) {
        if ($.type(subscriptions[uri]) !== "number") {
          subscriptions[uri].unsubscribe();
        }
      }
      callbacks.length = 0;
      subscriptions = {};
    },

    runCallbacks: function () {
      $.each(callbacks, function (index, callback) {
        callback.fn(callback.args);
      });
      callbacks.length = 0;
    },

    post: function(uri, json) {
      var deferred = $q.defer();
      setTimeout(function() {
        $http.post(uri, json)
          .success(function(resp) {
            deferred.resolve(resp);
          })
          .error(function(resp) {
            deferred.reject(resp);
          });
      }, 2000);
      return deferred.promise;
    }
  }

  var instance = {
    saveUserInfo: function(json) {
      return $$.post(jsonValue.httpUri.userSave, json);
    },

    findUserInfoByKey: function(json) {
      //if (!stompClient.connected) {
      //  callbacks.push({
      //    fn: instance.findUserInfoByKey,
      //    args: json
      //  });
      //  return instance.connectSocket();
      //}
      //
      //var subscription = stompClient.subscribe(socketUri.subscribeUserInfoByKey, function (response) {
      //  scope.$emit(events.userInfo, JSON.parse(response.body));
      //  subscription.unsubscribe();
      //});
      //stompClient.send(socketUri.getUserInfoByKey, {}, JSON.stringify(json));
      return $$.post(jsonValue.httpUri.user, json);
    },

    /* @subscription */
    analyticsSkill: function (analyticJson) {
      if (!stompClient.connected) {
        callbacks.push({
          fn: instance.analyticsSkill,
          args: analyticJson
        });
        return instance.connectSocket();
      }

      var uri = socketUri.subscribeAnalyticsSkill;
      //var subscription = subscriptions[uri];
      //if (subscription !== undefined) { return true; }

      var subscription = stompClient.subscribe(uri, function (response) {
        scope.$emit(events.analyticsSkill, JSON.parse(response.body));
        subscription.unsubscribe(); // TODO no need to support real-time now
        utils.sendNotification(jsonValue.notifications.gotData);
      });
      //subscriptions[uri] = subscription;

      stompClient.send(socketUri.analyticsSkill, {},
        JSON.stringify({term: analyticJson.term, histograms: analyticJson.histograms}));
    },

    /* @subscription */
    findJobs: function (json) {
      if (!stompClient.connected) {
        callbacks.push({
          fn: instance.findJobs,
          args: json
        });
        return instance.connectSocket();
      }

      var subscription = stompClient.subscribe(socketUri.subscribeJobsSearch, function (response) {
        scope.$emit(events.foundJobs, JSON.parse(response.body));
        subscription.unsubscribe();
      });
      stompClient.send(socketUri.sendJobsSearch, {}, JSON.stringify(json));
    },

    /* @subscription */
    registerTermsSubscription: function (terms) {
      $.each(terms, function (index, term) {
        instance.subscribeTerm(term);
      });
    },

    // {term: "JAVA", name: "java"}
    subscribeTerm: function (term) {
      var uri = socketUri.subscribeTerm + term.term;
      var subscription = subscriptions[uri];
      if (subscription !== undefined) {
        return true;
      }
      subscription = stompClient.subscribe(uri, function (response) {
        scope.$emit(events.term + term.term, {
          count: JSON.parse(response.body).count,
          term: term.term
        });
      });
      subscriptions[uri] = subscription;
    },

    connectSocket: function () {
      if (isConnecting) { return; }

      if (instance.isConnected()) {
        stompClient.disconnect();
      }
      stompClient = Stomp.over(new SockJS(stompUrl));
      stompClient.debug = function () {};

      stompClient.connect({}, function (frame) {
        isConnecting = false;
        $$.runCallbacks();
      }, function (errorFrame) {
        console.log("Erorr: " + errorFrame);
        isConnecting = false;
      });//onreceipt
      isConnecting = true;
    },

    /* @subscription */
    receiveTechnicalTerms: function () {
      if (!stompClient.connected) {
        callbacks.push({
          fn: instance.receiveTechnicalTerms,
          args: undefined
        });
        return instance.connectSocket();
      }
      var subscribeTerms = stompClient.subscribe(socketUri.subscribeTerms, function (response) {
        scope.$emit(events.terms, JSON.parse(response.body));
        instance.registerTermsSubscription(JSON.parse(response.body));
        subscribeTerms.unsubscribe();
        utils.sendNotification(jsonValue.notifications.gotData);
      });
      stompClient.send(socketUri.sendTerms);
    },

    isConnected: function () {
      if (stompClient !== undefined) {
        return stompClient.connected;
      }
      return false;
    },

    getStompClient: function () {
      return stompClient;
    },

    initialize: function() {}
  }
  utils.registerNotification(jsonValue.notifications.switchScope, $$.initialize);
  return instance;
});
