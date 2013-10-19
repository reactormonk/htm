'use strict';

angular.module('htm', ['common.playRoutes', 'common.filters', 'htm.services', 'ui.bootstrap']).
controller('BattleCtrl', ['$rootScope', '$scope', '$timeout', '$modal', '$location', '$filter', 'playRoutes', 'appService', BattleCtrl]).
controller('PoolsCtrl', ['$rootScope', '$scope', '$timeout', '$location', 'playRoutes', 'appService', PoolsCtrl]).config(
		[ '$routeProvider', function($routeProvider) {
			$routeProvider.when('/arenas', {
				templateUrl : 'static/battle/templates/arenas.html',
				controller : 'PoolsCtrl'
			}).when('/fight', {
				templateUrl : 'static/battle/templates/fight.html',
				controller : 'BattleCtrl'
			}).otherwise({
				redirectTo : '/arenas'
			});
		} ]);