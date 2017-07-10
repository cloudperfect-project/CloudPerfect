/**
 * Created by alexandros on 1/6/2017.
 */
var mainHandler = angular.module('mainHandler',['loginHandler']);

mainHandler.controller('mainController', ['$scope','userprofile','$rootScope', function($scope,userprofile,$rootScope) {
    $scope.userprofile=userprofile;

    $scope.log_in= function(){
        $scope.userprofile.setUser("test2");
         console.log("in");
        $scope.user=userprofile.getUser();
    }


     console.log($scope.userprofile.getUser());
    //user credential form initialization

}]);