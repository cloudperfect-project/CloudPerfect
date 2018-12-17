var navigationHandler = angular.module('navigationHandler',[]);

navigationHandler.directive('slaResults', function(){
    return{
        restrict:'E',
        templateUrl:'sla-results.html'
    }
});
navigationHandler.directive('slaOperations', function(){
    return{
        restrict:'E',
        templateUrl:'sla-operations.html'
    }
});
navigationHandler.directive('slaAuditor', function(){
    return{
        restrict:'E',
        templateUrl:'sla-auditor.html'
    }
});
navigationHandler.directive('slaMetrics', function(){
    return{
        restrict:'E',
        templateUrl:'sla-metrics.html'
    }
});
navigationHandler.directive('slaMonitor', function(){
    return{
        restrict:'E',
        templateUrl:'sla-monitor.html'
    }
});
navigationHandler.directive('benchResults', function(){
    return{
        restrict:'E',
        templateUrl:'bench-results.html'
    }
});
navigationHandler.directive('benchPrivateResults', function(){
    return{
        restrict:'E',
        templateUrl:'bench-private-results.html'
    }
});
navigationHandler.directive('benchOperations', function(){
    return{
        restrict:'E',
        templateUrl:'bench-operations.html'
    }
});
navigationHandler.directive('benchQueries', function(){
    return{
        restrict:'E',
        templateUrl:'bench-queries.html'
    }
});
navigationHandler.directive('benchMetrics', function(){
    return{
        restrict:'E',
        templateUrl:'bench-metrics.html'
    }
});
navigationHandler.directive('benchReport', function(){
    return{
        restrict:'E',
        templateUrl:'bench-report.html'
    }
});
navigationHandler.directive('costModel', function(){
    return{
        restrict:'E',
        templateUrl:'cost-model.html'
    }
});
navigationHandler.directive('scaleAuditor', function(){
    return{
        restrict:'E',
        templateUrl:'sla-scale.html'
    }
});
navigationHandler.directive('scaleResults', function(){
    return{
        restrict:'E',
        templateUrl:'scale-results.html'
    }
});