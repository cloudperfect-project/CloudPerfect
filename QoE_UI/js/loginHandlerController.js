var loginHandler = angular.module('loginHandler',[])
// service to instantiate user profile data
loginHandler.service('userprofile', function () {

   this.setUser = function(info) {
        this.userProfile=info
    };

   this.getUser = function(){
        return this.userProfile;
    };

    })
loginHandler.directive('newLogin', function(){
  return{
      restrict:'E',
      templateUrl:'new-login.html'
  };
});



