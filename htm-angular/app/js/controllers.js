'use strict';
(function(){

	angular.module('htm.welcome', [])

		.controller('WelcomeCtrl', ['$scope', function($scope) {
		}])

	angular.module('htm.tournament', ['htm.api'])

		.controller('TournamentListCtrl', ['$scope', 'Tournament', function($scope, Tournament) {
			$scope.tournaments = Tournament.query();

			$scope.tournaments.$promise.catch(function(error){
				$scope.tournaments.error = error;
			});

			/* New tournament form is initially hidden */
			$scope.addNewTournamentVisible = false;

			$scope.isLoading = function(){
				return !$scope.tournaments.$resolved;
			}
			
			$scope.isError = function(){
				return $scope.tournaments.$resolved && $scope.tournaments.error;
			}

			$scope.isLoaded = function(){
				return $scope.tournaments.$resolved && angular.isUndefined($scope.tournaments.error);
			}
			
			$scope.showAddNewTournament = function(){
				$scope.addNewTournamentVisible = true;
			}

			$scope.hideAddNewTournament = function(){
				$scope.addNewTournamentVisible = false;
			}

			$scope._findTournamentWithSameId = function(newTournament){
				return _.find($scope.tournaments, function(existingTournament){ 
					return existingTournament.identifier === newTournament.identifier(); 
				});
			};

			$scope.newTournament = {
				name: '',
				_identifier: undefined,
				_memo: undefined,

				customIdentifier: false,
				customMemo: false,
				error: undefined,

				identifier: function(identifier){
					if(angular.isDefined(identifier)){
						this._identifier = identifier;
						this.customIdentifier = true;
					}

					// Reset identifier when not custom or undefined
					if(!this.customIdentifier  || !angular.isDefined(identifier)){
						this._identifier = this._defaultIdentifier();	
					}

					return this._identifier;
				},

				_defaultIdentifier: function() {
					var name = this.name || '';
					return name.toLowerCase().split(' ').join('-');	
				},

				memo: function(memo){
					if(angular.isDefined(memo)){
						this._memo = memo;
						this.customMemo = true;
					};

					// Reset memo when not custom or undefined
					if(!this.customMemo || !angular.isDefined(memo)){
						this._memo = this._defaultMemo();
					}
					return this._memo;
				},

				_defaultMemo: function() {
					var memo = '';
					var name = this.name || '';

					angular.forEach(name.toUpperCase().split(' '), function(s) {
						if(isNaN(s)){
							memo += s.charAt(0);
						} else {
							memo += s;
						}
					});
					return memo;
				},

				reset: function() {
					var defaultName = 'Basic Longsword Tournament';
					this.name = defaultName,
					this.customIdentifier = false;
					this._identifier = undefined;
					this.customMemo = false;
					this._memo = undefined;
					this.state = 'new';
					this.error = undefined;

					var i = 2;
					while($scope._findTournamentWithSameId(this)){
						this.name =  defaultName + " " + i++;
					}
				}
			};
			$scope.newTournament.reset();


			$scope.save = function() {
				if($scope._findTournamentWithSameId($scope.newTournament)){
					//TODO: Move this to validation directives
					return;
				} 
				var t = $scope.newTournament;
				var tournament = new Tournament({
					name: t.name,
					identifier: t.identifier(),
					memo: t.memo(),
					participants: []
				});

				$scope.newTournament.state = 'saving';
				tournament.$save().then(function(success){
					$scope.tournaments.push(tournament);
					$scope.newTournament.reset();
					$scope.hideAddNewTournament();
				},function(error){
					$scope.newTournament.error = error;
				});					
			};
		}])

		.controller('TournamentCtrl', ['$scope', '$routeParams', 'Tournament', function($scope, $routeParams, Tournament) {

		}])



	;

})();