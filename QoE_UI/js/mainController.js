/**
 * Created by alexandros on 1/6/2017.
 */
var mainHandler = angular.module('mainHandler',['loginHandler','navigationHandler','dbConnect','chart.js','ui.bootstrap']);

mainHandler.controller('mainController', ['$scope','userprofile','dbService', function($scope,userprofile,dbService) {
    //table pagination

    $scope.viewby = 10;
    $scope.currentPage = 1;
    $scope.itemsPerPage = $scope.viewby;
    $scope.maxSize = 7; //Number of pager buttons to show

    $scope.setPage = function (pageNo) {
        $scope.currentPage = pageNo;
    };

    $scope.pageChanged = function() {
        console.log('Page changed to: ' + $scope.currentPage);
    };

    $scope.setItemsPerPage = function(num) {
        $scope.itemsPerPage = num;
        $scope.currentPage = 1; //reset to first page
    };

    //end of pagination
    //filters
    $scope.benchS="";
    //end of filters
    $scope.userN="";
    $scope.userP="";
    $scope.failLog=false;
    $scope.userprofile=userprofile;
    $scope.qoeUser="";
    $scope.qoeUserInfo={};
    $scope.authenticated=false;
    $scope.successSLALaunch=false;
    $scope.successBENCHLaunch=false;
    $scope.successBENCHUpdate=false;
    $scope.customAvailability=false;
    $scope.emailNotifications=false;
    $scope.extraParameters=false;
    $scope.extraParameters2=false;
    $scope.softAvailability="0";
    $scope.hardAvailability="0";
    $scope.benchTable=[];
    $scope.privateBenchResults=[];
    $scope.toolList=["cfd","filebench","dacapo-ft","ycsb-mysql"];
    $scope.benchmarkTests=[];
    $scope.visibilityOptions=["private","public", "organizational"]
    $scope.scheduleID="";
    $scope.benchResultForStorage=
    {
        provider:"",
            size:"",
        workload:"",
        tool:"",
        metrics:[]
    };

    $scope.changeTool= function(tool){
        $scope.listofWorkload=[];
        for(var t=0;t< $scope.benchmarkTests.length;t++){
            if(tool.id==$scope.benchmarkTests[t].id){
                $scope.listofWorkload=$scope.benchmarkTests[t].workloads;
            }
        }
    };

    $scope.bench={
        "tool":"",
        "workload":""};
    $scope.cloudDiscoveryParameters={
        "provider": "",
        "identity": "",
        "credentials": "",
        "optionalParameters": {
        }
    };
    $scope.vmConfigurationData={};
    $scope.populateCloudDiscoveryParameters= function(){

            $scope.cloudDiscoveryParameters.provider = $scope.BenchLaunch.provider.driver;
            $scope.cloudDiscoveryParameters.identity = $scope.BenchLaunch.provider.access_id;
            $scope.cloudDiscoveryParameters.credentials = $scope.BenchLaunch.provider.secret_key;
            if($scope.BenchLaunch.provider.auth_url!=""){
            $scope.cloudDiscoveryParameters.optionalParameters.authUrl = $scope.BenchLaunch.provider.auth_url;
            }
            if($scope.BenchLaunch.provider.region!=""){
            $scope.cloudDiscoveryParameters.optionalParameters.region = $scope.BenchLaunch.provider.region;
            }
            if($scope.BenchLaunch.provider.tenant!=""){
            $scope.cloudDiscoveryParameters.optionalParameters.project = $scope.BenchLaunch.provider.tenant;
            }
            if($scope.BenchLaunch.provider.userDomain!=""){
            $scope.cloudDiscoveryParameters.optionalParameters.userDomain = $scope.BenchLaunch.provider.userDomain;
             }
            if($scope.BenchLaunch.provider.projectDomain!=""){
            $scope.cloudDiscoveryParameters.optionalParameters.projectDomain = $scope.BenchLaunch.provider.projectDomain;
            }
            console.log($scope.cloudDiscoveryParameters);
            //send data
            $scope.vmConfigurationData=dbService.GETBENCHCONF($scope.cloudDiscoveryParameters,function (success) {console.log($scope.vmConfigurationData);});

    };
    $scope.BenchLaunch={
        "provider": {
            "class": "benchsuite.stdlib.provider.libcloud.LibcloudComputeProvider",
            "name": "",//list 1st phase
            "driver": "",//auto 1st phase
            "access_id": "",//put by user 1st phase
            "secret_key": "",//put by user 1st phase
            "region": "",//put by the user 1st phase optional
            "security_group": "",//security group 2nd phase!!!!
            "network": "",//network 2nd phase!!!
            "auth_url":"", // 1st phase put by the user
            "auth_version":"",//optional
            "tenant":"",//optional openstack project name 1st
            "userDomain": "",//optional
            "projectDomain": ""//optional
        },
        "vm": {
            "name":"",//2nd phase automatic
            "image": "",//image 2nd
            "size": "",//hardware profile 2nd
            "vm_user": "",//username optional 2nd phase
            "platform": "",//optional 2nd optional
            "key_name": "",//optional 2nd optional
            "ssh_private_key": ""//optional 2nd
        }
    };

    $scope.BenchScheduler={
        "id" : "",
        "provider_config_secret" : "",
        "username" : "",
        "active" : true,
        "properties": {
            "visibility": "",
            "user" : $scope.qoeUser,
            "organization": $scope.qoeUserInfo.organization,
        },
        "docker_additional_opts" : {
            "hosts" : {
                "cloudpcntlr" : "10.0.16.11"
            }
        },
        "tests" : [

        ],
        "tags" : [
            "scheduled"
        ],
        "interval" : {
            "hours":0,
            "days" : 0,
            "weeks":0

        }

    };
    $scope.SLALaunch={
        "providerName":"",
        "serviceName":"",
        "cloudUser":"",
        "APIKey":"",
        "softAvailability":"",
        "hardAvailability":""
    };

    $scope.benches=[];
    $scope.pushBench=function(){
        $scope.benches.push({tool:$scope.bench.tool.id,workload:$scope.bench.workload.id});
    };
    $scope.deleteBench=function(bench){
        var index = $scope.benches.indexOf(bench);
        $scope.benches.splice(index, 1);

    };
    // provider service combination
    $scope.listofServices=[];
    $scope.ListProviders= ["aws", "Microsoft","Google", "Cosmote","ULM", "Filab"];
    $scope.changeService= function(provider){
        switch(provider) {
            case "aws":
                $scope.listofServices=["ec2","ec3"];
                $scope.BenchLaunch.provider.driver="ec2";
                break;
            case "Microsoft":
                $scope.listofServices=["Azure Blob storage"];
                $scope.BenchLaunch.provider.driver="Azure Blob storage";
                break;
            case "Google":
                $scope.listofServices=["Compute Engine"];
                $scope.BenchLaunch.provider.driver="compute";
                break;
            case "Cosmote":
                $scope.listofServices=["Compute"];
                $scope.BenchLaunch.provider.driver="openstack";
                break;
            case "ULM":
                $scope.listofServices=["Compute"];
                $scope.BenchLaunch.provider.driver="openstack";
                break;
            case "Filab":
                $scope.listofServices=["Compute"];
                $scope.BenchLaunch.provider.driver="openstack";
                break;
            default:
                $scope.listofServices=["default"];
        }
    };
    //end of provider service combination
    $scope.SLALaunchData={
        "Name": "",
        "Data": ""
    };
    $scope.SLACreateServiceData=
        {
            "Name":"",
            "TaskTemplate":{
                "ContainerSpec":{
                    "Mounts":[
                    ],
                    "Image":"3alib_image:latest",
                    "Args":["java", "-jar", "3alibAuditoring.jar", "QoEUserID", "providerName", "serviceName", "auditor"],
                    "Secrets":[
                        {
                            "File":{
                                "GID":"0",
                                "Mode":292,
                                "Name":"QoEUserID_providerName_serviceName_containerType",
                                "UID":"0"
                            },
                            "SecretID":"utkbmiudrj16l52upg25dmoxn",
                            "SecretName":"QoEUserID_providerName_serviceName_containerType"
                        }
                    ],
                    "Env":[],
                    "Labels":{}
                }
            },
            "Mode":{
                "Replicated":{
                    "Replicas":1
                }
            },
            "EndpointSpec":{
                "Ports":[]
            },
            "Labels":{},
            "UpdateConfig":{"Parallelism":1, "Delay":0, "FailureAction":"continue"}
        };
    $scope.SLASecret="";

    $scope.createDate=function (date) {
        return new Date(date);

    };
    //get sla results for the user

    $scope.launchSLAResults=function() {
        $scope.slaResultsTable=[];
        $scope.SLAResults = dbService.GETSLAResults({id: $scope.qoeUser}, function (success) {
            $scope.SLAResults = $scope.SLAResults.results;
            for(var i=0 ; i< $scope.SLAResults.length; i++ ){
                $scope.slaResultsTable.push($scope.SLAResults[i]);
            }
            console.log($scope.SLAResults);
        });
    };

    $scope.createCustomSLA=function(){
        $scope.customSLAData='docs={' +
            '    "id" : "'+$scope.qoeUser+'",' +
            '    "Provider" : "'+$scope.SLALaunch.providerName+'",' +
            '    "Service" : "'+$scope.SLALaunch.serviceName+'",' +
            '    "mail_notification" : "'+$scope.emailNotifications+'",' +
            '    "custom" : "'+$scope.customAvailability+'",' +
            '    "Soft_SLA" : '+$scope.softAvailability+',' +
            '    "Hard_SLA" : '+$scope.hardAvailability+ '' +
            '}';
        dbService.SLACreateCustomAgreement($scope.customSLAData,function(success){
            console.log("custom sla created successfully")
        });

    };
    //new bench configuration
    $scope.BenchLaunchData={
        "Name": "",
        "Data": ""
    };
$scope.initiateBench2=function(){
    console.log("teeeeeeeeeeeeeeeeeest1111111111111111111111");
    console.log($scope.BenchLaunch);
    console.log("teeeeeeeeeeeeeeeeeest2222222222222222222222");
    console.log($scope.BenchScheduler);

};
    $scope.convertVMValues=function(){
        $scope.BenchLaunch.vm.image=$scope.BenchLaunch.vm.image.name;
        $scope.BenchLaunch.vm.size =$scope.BenchLaunch.vm.size.name;
        $scope.BenchLaunch.provider.network= $scope.BenchLaunch.provider.network.name
        $scope.BenchLaunch.provider.security_group=$scope.BenchLaunch.provider.security_group.name;

    };
    $scope.initiateBench = function (){
        $scope.BenchLaunch.vm.name=$scope.BenchLaunch.vm.platform+"_"+$scope.BenchLaunch.vm.size;
        $scope.encodedBenchlaunch=JSON.stringify($scope.BenchLaunch);
        console.log($scope.encodedBenchlaunch);
        $scope.encodedBenchlaunch=btoa(encodeURIComponent($scope.encodedBenchlaunch).replace(/%([0-9A-F]{2})/g, function toSolidBytes(match, p1) {return String.fromCharCode('0x' + p1);}));
        $scope.BenchLaunchData.Name=$scope.qoeUser+"__"+$scope.BenchLaunch.provider.name+"__"+$scope.BenchLaunch.vm.name;
        $scope.BenchLaunchData.Data=$scope.encodedBenchlaunch;
        $scope.BenchSecretCreate=dbService.dockerSecret($scope.BenchLaunchData,function (success) {
            console.log($scope.BenchSecretCreate.ID);
            $scope.storeScheduler($scope.BenchSecretCreate.ID);

        })
    };
    $scope.storeScheduler= function(secretid){
        $scope.BenchScheduler.id="Scheduler_"+ $scope.BenchLaunchData.Name;
        $scope.BenchScheduler.username=$scope.qoeUser;
        $scope.BenchScheduler.provider_config_secret=secretid;

        for (var k=0; k < $scope.benches.length; k++) {
            if ($scope.benches[k].workload!=null && $scope.benches[k].workload!=""){
                $scope.BenchScheduler.tests.push($scope.benches[k].tool+":"+$scope.benches[k].workload);
            }
            else {
                $scope.BenchScheduler.tests.push($scope.benches[k].tool)
            }
        }
        $scope.BenchSchedulerToString="docs="+JSON.stringify($scope.BenchScheduler);
        dbService.BenchStore($scope.BenchSchedulerToString, function (success) {
            console.log("scheduler created!") ;
            $scope.successBENCHLaunch=true;
        });

    };

    $scope.updateScheduler=function(){
        $scope.scheduleID.tests=[];
        for (var k=0; k < $scope.benches.length; k++) {
            if ($scope.benches[k].workload!=null && $scope.benches[k].workload!=""){
                $scope.scheduleID.tests.push($scope.benches[k].tool+":"+$scope.benches[k].workload);
            }
            else {
                $scope.scheduleID.tests.push($scope.benches[k].tool);
            }
        }
        delete $scope.scheduleID["_id"];
        delete $scope.scheduleID["$$hashKey"];
        $scope.scheduleIDidString='criteria={"id":"'+$scope.scheduleID.id+'"}';
        $scope.scheduleIDToString=$scope.scheduleIDidString+"&newobj="+JSON.stringify($scope.scheduleID);
        console.log($scope.scheduleIDToString);
        dbService.schedulerUpdate($scope.scheduleIDToString,function (success) {
            $scope.successBENCHUpdate=true;
        });
    };
    $scope.benchArrayConf = function(){
        $scope.benches=[];
        for (var t=0 ;t<$scope.scheduleID.tests.length; t++){
            if($scope.scheduleID.tests[t].indexOf(':') > -1){
                var res = $scope.scheduleID.tests[t].split(":");
                $scope.benches.push({"tool":res[0],"workload":res[1]});
            }
            else{
                $scope.benches.push({"tool":$scope.scheduleID.tests[t],"workload":""});
            }
        }
    };

    //end of new bench configuration
    $scope.loadSchedules=function (){
        $scope.BenchSchedules = dbService.GETBENCHSchedules({id: $scope.qoeUser}, function (success) {
            $scope.BenchSchedules  = $scope.BenchSchedules.results;
            console.log($scope.BenchSchedules);
        });

    };

    $scope.initiateLaunchSLA=function(){
            $scope.createCustomSLA();
        $scope.SLASecret="ProviderName="+$scope.SLALaunch.providerName+"\n"+
            "ServiceName="+$scope.SLALaunch.serviceName+"\n"+
            "user="+$scope.SLALaunch.cloudUser+"\n"+
            "APIkey="+$scope.SLALaunch.APIKey;
        // encode secret to base64
        $scope.SLASecret=btoa(encodeURIComponent($scope.SLASecret).replace(/%([0-9A-F]{2})/g, function toSolidBytes(match, p1) {return String.fromCharCode('0x' + p1);}));
        console.log($scope.SLASecret);
        $scope.SLALaunchData.Name=$scope.qoeUser+"_"+$scope.SLALaunch.providerName+"_"+$scope.SLALaunch.serviceName+"_"+"auditor";
        $scope.SLALaunchData.Data= $scope.SLASecret;

        $scope.secretCreate=dbService.dockerSecret($scope.SLALaunchData, function (success) {
            console.log($scope.secretCreate.ID);
            $scope.SLACreateServiceData.name=$scope.SLALaunchData.Name;
            $scope.SLACreateServiceData.TaskTemplate.ContainerSpec.Args[3]=$scope.qoeUser;
            $scope.SLACreateServiceData.TaskTemplate.ContainerSpec.Args[4]=$scope.SLALaunch.providerName;
            $scope.SLACreateServiceData.TaskTemplate.ContainerSpec.Args[5]=$scope.SLALaunch.serviceName;
            $scope.SLACreateServiceData.TaskTemplate.ContainerSpec.Secrets[0].File.Name=$scope.SLALaunchData.Name;
            $scope.SLACreateServiceData.TaskTemplate.ContainerSpec.Secrets[0].SecretID=$scope.secretCreate.ID;
            $scope.SLACreateServiceData.TaskTemplate.ContainerSpec.Secrets[0].SecretName=$scope.SLALaunchData.Name;
            $scope.createSLAContainer=dbService.SLACreateService($scope.SLACreateServiceData,function(success){
                console.log($scope.createSLAContainer);
                $scope.successSLALaunch=true ;

            });

        });
    };
    $scope.benchCategoriesGrouping= 'cmd={"group" : {"ns" : "results", "$reduce" : "function(curr,result){}", "key" : {"provider.id" : 1,"provider.size": 1, "tool": 1, "workload":1}, "initial" : {"total":0}}}';
    //get benchmarking results

    $scope.launchBenchResultsStored=function(){
        $scope.benchResults= dbService.GETBENCHTableResults(function(success){
            $scope.benchTable=$scope.benchResults.results[$scope.benchResults.results.length-1].table;
            $scope.totalItems = $scope.benchTable.length;
            console.log($scope.benchTable);

        })
    }
    $scope.launchBenchResults =function () {
        $scope.benchTable=[];
        $scope.benchResults = dbService.GETBenchGroups($scope.benchCategoriesGrouping, function (success) {
            $scope.benchResults = $scope.benchResults.retval;
            console.log($scope.benchResults);
            $scope.totalItems = $scope.benchResults.length;
            for (var j = 0; j < $scope.benchResults.length; j++) {
                if ($scope.benchResults[j]['provider.id'] != null && $scope.benchResults[j]['provider.size'] != null && $scope.benchResults[j].workload != null && $scope.benchResults[j].tool != null) {
                    dbService.GETBenchResults(
                        {
                            provider: $scope.benchResults[j]['provider.id'],
                            size: $scope.benchResults[j]['provider.size'],
                            workload: $scope.benchResults[j].workload,
                            tool: $scope.benchResults[j].tool
                        },
                        function (response) {
                            $scope.populateTable(response);

                        });
                }
            }
            console.log($scope.benchTable);
        });
    };
    //populate table
    $scope.tableInput={
        provider:"",
        size:"",
        workload:"",
        tool:"",
        metrics:[]};
    $scope.dataTablebench={table:[]};
    $scope.populateTable= function(benchR){
        for(var k=0;k< benchR.results.length;k++){

            if(benchR.results[k].metrics!= null && benchR.results[k].metrics.length!=0){
                var metrics = Object.keys(benchR.results[k].metrics);
                for (var a=0; a<metrics.length ;a++){
                    if(k==0){
                        $scope.tableInput.metrics.push({name:metrics[a],unit:benchR.results[k].metrics[metrics[a]].unit, values:[benchR.results[k].metrics[metrics[a]].value]})
                    }
                    else{
                        $scope.tableInput.metrics[a].values.push(benchR.results[k].metrics[metrics[a]].value);
                    }

                }
            }
        }
        $scope.benchTable.push( {
            provider:benchR.results[0].provider.id,
            size:benchR.results[0].provider.size,
            workload:benchR.results[0].workload,
            tool:benchR.results[0].tool,
            metrics:$scope.tableInput.metrics
        });
        $scope.benchResultForStorage.provider=benchR.results[0].provider.id;
        $scope.benchResultForStorage.size=benchR.results[0].provider.size;
        $scope.benchResultForStorage.workload=benchR.results[0].workload;
        $scope.benchResultForStorage.tool=benchR.results[0].tool;
        $scope.benchResultForStorage.metrics=$scope.tableInput.metrics;
        $scope.dataTablebench.table.push(angular.copy($scope.benchResultForStorage));
        $scope.tableInput.metrics=[];
    };

    $scope.storeBenchTable=function(){
            $scope.benchTableToString="docs="+JSON.stringify($scope.dataTablebench);
            console.log($scope.benchTableToString) ;
            dbService.BenchTableStore($scope.benchTableToString, function (success) {
                console.log("table stored!") ;
        });

    };
// calculate avg for bench
    $scope.calcAVG= function(data) {

        if (data.length != 0) {
            var sum = 0;
            for (var i = 0; i < data.length; i++) {
                sum += data[i]; //don't forget to add the base
            }
            return avg = sum / data.length;
        }
        else{
            return "invalid"
        }
    };

// calculate standar deviation bench
    $scope.calcDEV = function (data){

        var mean = $scope.calcAVG(data);
        var newSum = 0;
        if (data.length != 0) {
            for (var j = 0; j<data.length; j++){
                // put the calculation right in there
                newSum = newSum + ((data[j] - mean) * (data[j] - mean));
            }
            var squaredDiffMean = (newSum) / (data.length);
            var standardDev = (Math.sqrt(squaredDiffMean));

            return standardDev;
        }
        else{
            return "invalid"
        }

    };

//calculate performance of virtual cores avg
    $scope.calcPVCAVG = function(data){
        var pvca=0;

        if (data.length != 0) {
            var mean= $scope.calcAVG(data);
            for (var j = 0; j < data.length; j++) {
                pvca = pvca + Math.abs(data[j] - mean) / mean;

            }
            return (pvca / data.length) * 100 ;
        }
        else{
            return "invalid"
        }
    };
    $scope.calcPVCMAX = function(data){
        if (data.length != 0) {
            var mean= $scope.calcAVG(data);
            var sdata= data.sort(function(a,b){return b - a});
            return 100 *((sdata[0] - mean) / mean)
        }
        else{
            return "invalid"
        }
    };
    //end of bench results
    //get bench log files and next run
    $scope.getBenchLogs=function(){
        $scope.benchLogFiles= dbService.GETBENCHLogs(function(success){
            $scope.benchLogFiles=$scope.benchLogFiles.results;
            console.log($scope.benchLogFiles);
        });
        $scope.benchJobFiles= dbService.GETBENCHJobs(function(success){
            $scope.benchJobFiles=$scope.benchJobFiles.results;
            console.log($scope.benchJobFiles);
        });

    }
    //end
    //start getting private results
    $scope.GETPrivateBench=function(){
        $scope.privateBenchResults=dbService.GETPrivateBENCHResults({visibility:"private",id:$scope.qoeUser},function(success){
        });

    };
    //end private bench
    $scope.GETproviderSLA= function () {
        //variables for SLA Provider Ranking
        var boundaryPeriod;
        var runVSmonthTime;
        var availabilityLimit;
        var availabilityZone;
        var score=0;

        $scope.listofproviderSLA= dbService.GETProviderSLA(function(success){
            $scope.listofproviderSLA= $scope.listofproviderSLA.results;
            for (var i=0; i< $scope.listofproviderSLA.length; i++){
                boundaryPeriod=$scope.listofproviderSLA[i].underlyingMetrics[0].underlyingMetrics[0].underlyingMetrics[0].parameters[0].parameter;
                availabilityZone=$scope.listofproviderSLA[i].underlyingMetrics[0].underlyingMetrics[0].underlyingMetrics[0].rules[0].minlimit;
                runVSmonthTime=$scope.listofproviderSLA[i].underlyingMetrics[0].underlyingMetrics[0].underlyingMetrics[0].rules[1].minlimit;
                availabilityLimit=$scope.listofproviderSLA[i].parameters[0].parameter;

                score= $scope.normalize(boundaryPeriod,0,600) + (1- $scope.normalize(availabilityZone,1,3)) +runVSmonthTime + $scope.normalize(availabilityLimit,99,100);

                $scope.data[0].push(score);
            }
        });
        $scope.bp=1;
        $scope.az=1;
        $scope.rvm=1;
        $scope.al=1;
        $scope.recalculateRanking= function(){
            var boundaryPeriod;
            var runVSmonthTime;
            var availabilityLimit;
            var availabilityZone;
            var score=0;
            $scope.data[0]=[];
            for (var i=0; i< $scope.listofproviderSLA.length; i++){
                boundaryPeriod=$scope.listofproviderSLA[i].underlyingMetrics[0].underlyingMetrics[0].underlyingMetrics[0].parameters[0].parameter;
                availabilityZone=$scope.listofproviderSLA[i].underlyingMetrics[0].underlyingMetrics[0].underlyingMetrics[0].rules[0].minlimit;
                runVSmonthTime=$scope.listofproviderSLA[i].underlyingMetrics[0].underlyingMetrics[0].underlyingMetrics[0].rules[1].minlimit;
                availabilityLimit=$scope.listofproviderSLA[i].parameters[0].parameter;

                score= $scope.normalize(boundaryPeriod,0,600)*$scope.bp + (1- $scope.normalize(availabilityZone,1,3))*$scope.az +runVSmonthTime*$scope.rvm + $scope.normalize(availabilityLimit,99,100)*$scope.al;

                $scope.data[0].push(score);
            };
        };


    };
    $scope.normalize=function (val, min, max){
        // Shift to positive to avoid issues when crossing the 0 line
        return (val - min) / (max - min);
    };

    $scope.log_in= function(usern,userp){
        dbService.login({
                id:$scope.userN,
                pass:$scope.userP
            },
            function(response){
                if(response.results.length>0){
                    $scope.authenticated=true;
                    $scope.qoeUser= response.results[0].userID;
                    $scope.qoeUserInfo= response.results[0];
                    //$scope.launchBenchResults();
                    $scope.launchBenchResultsStored();
                    $scope.launchSLAResults();
                    $scope.GETproviderSLA();
                    $scope.loadSchedules();
                    $scope.getBenchLogs();
                    $scope.GETPrivateBench();
                    $scope.benchmarkTests=dbService.GETBenchTEST(function(success){console.log($scope.benchmarkTests)});

                }
                else{$scope.failLog=true};
            })
    };

//chart tests
    $scope.labels = ['Amazon',"Azure Compute","Google Compute"];
    $scope.series = ['Series A'];

    $scope.data = [
        []
    ];
// end of chart tests
}

]);
mainHandler.directive('onFinishRender',['$timeout', '$parse', function ($timeout, $parse) {
    return {
        restrict: 'A',
        link: function (scope, element, attr) {
            if (scope.$last === true) {
                $timeout(function () {
                    scope.$emit('ngRepeatFinished');
                    if(!!attr.onFinishRender){
                        $parse(attr.onFinishRender)(scope);
                    }
                });
            }
        }
    }
}]);