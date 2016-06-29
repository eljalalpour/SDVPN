// js for SDVPN app custom view
(function () {
    'use strict';

    // injected refs
    var $log, $scope, wss, ks;

    // constant
    var hostEvent = "hostEvent";
    var addRule = "addRule";

    function addKeyBindings() {
        var map = {
            space: [dropRule, 'Add drop rule'],

            _helpFormat: [
                ['space']
            ]
        };

        ks.keyBindings(map);
    }

    function dropRule() {
        $log.log($scope.srcHost);
        $log.log($scope.dstHost);
        var src_host = $scope.srcHost;
        var dst_host = $scope.dstHost;
        wss.sendEvent("dropRule", {
            h1: src_host,
            h2: dst_host
        });
    }

    function hostEventCb(host) {
        $scope.hosts.push(host);
        $scope.$apply()
    }


    var app = angular.module('ovSDVPN', []);
    app.controller('OvSDVPNCtrl',
        ['$log', '$scope', 'WebSocketService', 'KeyService',
            function (_$log_, _$scope_, _wss_, _ks_) {
                $log = _$log_;
                $scope = _$scope_;
                wss = _wss_;
                ks = _ks_;

                var handlers = {};
                $scope.hosts = [];

                // data response handler
                handlers[hostEvent] = hostEventCb;
                wss.bindHandlers(handlers);

                addKeyBindings();

                $scope.dropRule = dropRule;

                // cleanup
                $scope.$on('$destroy', function () {
                    wss.unbindHandlers(handlers);
                    ks.unbindKeys();
                    $log.log('OvSDVPNCtrl has been destroyed');
                });

                $log.log('OvSDVPNCtrl has been created');
            }]);

}());
