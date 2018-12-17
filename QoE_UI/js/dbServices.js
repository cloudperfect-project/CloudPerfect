/**
 * Created by alexandros on 20/9/2017.
 */


var dbConnect = angular.module('dbConnect', ['ngResource', 'ngRoute']);

// Some APIs expect a PUT request in the format URL/object/ID
// Here we are creating an 'update' method
dbConnect.factory('dbService', ['$resource', function($resource) {
    return $resource('http://147.102.19.75:27080/_connect',{}, 
        {
            'login': {
                method:'GET',
                isArray: false,
                params:{
                    id:'@id',
                    pass:'@pass'
                },
                url:"http://147.102.19.75:27080/qoe/users/_find?criteria={\"username\":\":id\", \"password\":\":pass\"}",
                contentType: 'application/x-www-form-urlencoded'
            },
            'GETSLAResults': {
                method:'GET',
                isArray: false,
                params:{
                    id:'@id'
                },
                url:"http://147.102.19.75:27080/3alib/SLA/_find?criteria={\"User\":\":id\"}",
                contentType: 'application/x-www-form-urlencoded'
            },
            'GETScaleResults': {
                method:'GET',
                isArray: false,
                params:{
                    id:'@id'
                },
                url:"http://147.102.19.75:27080/3alib/scaleSLA/_find?criteria={\"User\":\":id\"}",
                contentType: 'application/x-www-form-urlencoded'
            },
            'GETSLAAuditors': {
                method:'GET',
                isArray: false,
                params:{
                    id:'@id'
                },
                url:"http://147.102.19.75:27080/3alib/SLAAgreement/_find?criteria={\"id\":\":id\"}",
                contentType: 'application/x-www-form-urlencoded'
            },
            'GETScaleAuditors': {
                method:'GET',
                isArray: false,
                params:{
                    id:'@id'
                },
                url:"http://147.102.19.75:27080/3alib/scaleAgreement/_find?criteria={\"id\":\":id\"}",
                contentType: 'application/x-www-form-urlencoded'
            },
            'GETBENCHTableResults': {
                method:'GET',
                isArray: false,
                url:"http://147.102.19.75:27080/qoe/benchresults/_find",
                contentType: 'application/x-www-form-urlencoded'
            },
            'GETPrivateBENCHResults':{
                method:'GET',
                isArray: false,
                params:{
                    visibility:'private',
                    id:'@id'
                },
                url:"http://147.102.19.75:27080/benchmarking/results/_find?criteria={\"properties.visibility\":\":visibility\", \"properties.user\":\":id\"}&batch_size=10000",
                contentType: 'application/x-www-form-urlencoded'
            },
            'GETProviderSLA': {
                method:'GET',
                isArray: false,
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                url:"http://147.102.19.75:27080/3alib/providers/_find",
                contentType: 'application/x-www-form-urlencoded'
            },
            'GETBENCHLogs': {
                method:'GET',
                isArray: false,
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                url:"http://147.102.19.75:27080/benchmarking/_apexec/_find?batch_size=100000000",
                contentType: 'application/x-www-form-urlencoded'
            },
            'GETBENCHJobs': {
                method:'GET',
                isArray: false,
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                url:"http://147.102.19.75:27080/benchmarking/apjobs2/_find",
                contentType: 'application/x-www-form-urlencoded'
            },
            'GETBENCHCONF':{
                method:'POST',
                isArray: false,
                url:"http://147.102.19.75:8900/qoehelper/rest/CloudInfo",
                contentType: 'application/json'

            },
            'GETBenchTEST':{
                method:'GET',
                isArray: true,
                url:"http://147.102.19.75:8900/qoehelper/rest/benchmarks/LATEST",
                contentType: 'application/json'
            },
            'GETBENCHSchedules': {
                method:'GET',
                isArray: false,
                params:{
                    id:'@id'
                },
                url:"http://147.102.19.75:27080/benchmarking/scheduling/_find?criteria={\"username\":\":id\"}",
                contentType: 'application/x-www-form-urlencoded'
            },
            'dockerSecret': {
                method:'POST',
                isArray: false,
                url:"http://147.102.19.75:4550/secrets/create",
                contentType: 'application/json'
            },
            'dockerSecretDelete': {
                method:'DELETE',
                isArray: false,
                params:{
                    id:'@id'
                },
                url:"http://147.102.19.75:4550/secrets/:id",
                contentType: 'application/json'
            },
            'SLACreateService': {
                method:'POST',
                isArray: false,
                url:"http://147.102.19.75:4550/services/create",
                contentType: 'application/json'
            },
            'SLACreateCustomAgreement': {
                method:'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                contentType: 'application/x-www-form-urlencoded',
                isArray: false,
                url:"http://147.102.19.75:27080/3alib/SLAAgreement/_insert"

            },
            'scaleCreateCustomAgreement': {
                method:'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                contentType: 'application/x-www-form-urlencoded',
                isArray: false,
                url:"http://147.102.19.75:27080/3alib/scaleAgreement/_insert"

            },
            'BenchStore': {
                method:'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                contentType: 'application/x-www-form-urlencoded',
                isArray: false,
                url:"http://147.102.19.75:27080/benchmarking/scheduling/_insert"

            },
            'BenchTableStore': {
                method:'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                contentType: 'application/x-www-form-urlencoded',
                isArray: false,
                url:"http://147.102.19.75:27080/qoe/benchresults/_insert"

            },
            'schedulerUpdate': {
                method:'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                contentType: 'application/x-www-form-urlencoded',
                isArray: false,
                url:"http://147.102.19.75:27080/benchmarking/scheduling/_update"

            },
            'schedulerDelete':{
                method:'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                contentType: 'application/x-www-form-urlencoded',
                isArray: false,
                url:"http://147.102.19.75:27080/benchmarking/scheduling/_remove"

            },

            //the ip can change to docker.host if a script is running on the deployment
            'GETBenchGroups': {
                method:'POST',
                isArray: false,
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                url:"http://147.102.19.75:27080/benchmarking/results/_cmd",
                contentType: 'application/x-www-form-urlencoded'
            },
            'GETBenchResults': {
                method:'GET',
                isArray: false,
                params:{
                    provider:'@provider',
                    size:'@size',
                    workload:'@workload',
                    tool:'tool'
                },
                url:"http://147.102.19.75:27080/benchmarking/results/_find?criteria={\"provider.id\":\":provider\",\"provider.size\":\":size\",\"test.workload\":\":workload\",\"test.tool\":\":tool\"}",
                contentType: 'application/x-www-form-urlencoded'
            },
            'GETCostModel': {
                method:'GET',
                isArray: false,
                params:{
                    id:'@id'
                },
                url:"http://147.102.19.75:27080/qoe/costModels/_find?criteria={\"UserId\":\":id\"}",
                contentType: 'application/x-www-form-urlencoded'
            },
            'costModelUpdate': {
                method:'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                contentType: 'application/x-www-form-urlencoded',
                isArray: false,
                url:"http://147.102.19.75:27080/qoe/costModels/_update"

            }





        });
}]);