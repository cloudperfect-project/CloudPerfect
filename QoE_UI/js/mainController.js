/**
 * Created by alexandros on 1/6/2017.
 */
var mainHandler = angular.module('mainHandler',['loginHandler','navigationHandler','dbConnect','chart.js','ui.bootstrap']);

mainHandler.controller('mainController', ['$scope','$timeout','userprofile','dbService', function($scope,$timeout,userprofile,dbService) {
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
    $scope.successScaleLaunch=false;
    $scope.successBENCHLaunch=false;
    $scope.successBENCHUpdate=false;
    $scope.customAvailability=false;
    $scope.emailNotifications=false;
    $scope.extraParameters=false;
    $scope.extraParameters2=false;
    $scope.softAvailability="0";
    $scope.hardAvailability="0";
    $scope.downtime="0";
    $scope.customPeriod="monthly";
    $scope.slaperiod=["monthly","year","week"]
    $scope.benchTable=[];
    $scope.privateBenchResults=[];
    $scope.toolList=["cfd","filebench","dacapo-ft","ycsb-mysql"];
    $scope.benchmarkTests=[];
    $scope.visibilityOptions=["private","public", "organizational"]
    $scope.scheduleID="";
    $scope.providerF="";
    $scope.serviceF="";
    $scope.toolF={};
    $scope.workloadF={};
    $scope.finishRenderAnalytics=false;
    $scope.agrAnalytics=true;
    $scope.costModelCreated=false;
    $scope.proCost=false;
    $scope.slaResultsTable=[];
    $scope.scaleResultsTable=[];
    $scope.performanceCostFilters={
        "costPer":0,
        "provider":"",
        "cpu":"",
        "ram":"",
        "vm":0
    };
    $scope.numbersList=["1","2","3","4","5","6","8","10","12","16","18","24","32","64"];
    $scope.minuteList=["5","10","15","20","25","30"];
    $scope.benchResultForStorage=
    {
        provider:"",
            size:"",
        workload:"",
        tool:"",
        metrics:[]
    };
    $scope.newcostService={
        "size" : "",
        "costCPU" : "",
        "costVM" : "",
        "costMONTH" : "",
        "maxVM" : "",
        "CPUNum" : 0,
        "RAMSize" : 0
    };
    $scope.newCostProvider={
        "provider": "",
        "costInfo":[]

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
            "ssh_private_key": "",//optional 2nd
            "post_create_script":""//optional 2nd
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
    $scope.ScaleLaunch={
        "scale_group":"",
        "promised_Scale_Time":"",
        "promised_cpu_util":"",
        "promised_cpu_util_in":"",
        "max_instances":"",
        "min_instances":"",
        "minutes":"",
        "provider": "",
        "service":"",
        "cloudUser": "",
        "apiKey":""

};

    $scope.benches=[];
    $scope.pushBench=function(){
        $scope.benches.push({tool:$scope.bench.tool.id,workload:$scope.bench.workload.id});
    };
    $scope.deleteBench=function(bench){
        var index = $scope.benches.indexOf(bench);
        $scope.benches.splice(index, 1);

    };

    //cost model CRUD
    $scope.pushServiceCost=function(cost){
        cost.push($scope.newcostService);
        $scope.newcostService={
            "size" : "",
            "costCPU" : "",
            "costVM" : "",
            "costMONTH" : "",
            "maxVM" : "",
            "CPUNum" : 0,
            "RAMSize" : 0
        };
    };
    $scope.deleteServiceCost=function(cost,list){
        var index3 = list.indexOf(cost);
        list.splice(index3,1);
    };
    $scope.deleteCostProvider=function(provider){
        var index4 = $scope.costModelTable.indexOf(provider);

        $scope.costModelTable.splice(index4,1);
    };
    $scope.addCostProvider=function(){
        $scope.costModelTable.push($scope.newCostProvider);
        $scope.newCostProvider={
            "provider": "",
            "costInfo":[]

        }
    };
    $scope.costModelforStorage={
        "UserId" : "",
        "ProviderCostModels" : []
    };
    $scope.saveCostModel = function(){
        $scope.costModelforStorage.UserId=$scope.qoeUser;
        $scope.costModelforStorage.ProviderCostModels=$scope.costModelTable;
        for(var i=0;i< $scope.costModelforStorage.ProviderCostModels.length; i++){
            delete $scope.costModelforStorage.ProviderCostModels[i]["$$hashKey"];
            for (var k=0;k<$scope.costModelforStorage.ProviderCostModels[i].costInfo.length;k++){
                delete  $scope.costModelforStorage.ProviderCostModels[i].costInfo[k]["$$hashKey"];
            }
        };
        $scope.costidString='criteria={"UserId":"'+$scope.costModelforStorage.UserId+'"}';
        $scope.costidString=$scope.costidString+"&newobj="+JSON.stringify($scope.costModelforStorage);
        console.log($scope.costidString);
        dbService.costModelUpdate($scope.costidString,function (success) {
            console.log("cost model saved");
        });
    };

    //cost end
    //performance cost analysis
    $scope.refreshCostPerformanceFilter= function(){
        $scope.toolF="";
        $scope.workloadF="";
        $scope.performanceCostFilters={
            "costPer":0,
            "provider":"",
            "cpu":"",
            "ram":"",
            "vm":0
        };
    };
    $scope.filteredCostModel=[];
    $scope.getProviderBenchCategForCost=function(){
        $scope.proCost=true;
        console.log($scope.performanceCostFilters.cpu);
        console.log($scope.performanceCostFilters.ram);
        $scope.filteredCostModel=[];
        $scope.BenchResultsAnalytics=[];
        $scope.ProviderBenchList= dbService.GETBenchGroups($scope.benchProvidersGrouping, function (success) {
            $scope.ProviderBenchList= $scope.ProviderBenchList.retval;
            console.log($scope.ProviderBenchList);

            for (var o=0; o<$scope.ProviderBenchList.length; o++){
                for(var k=0; k<$scope.benchTable.length; k++) {
                    if ($scope.benchTable[k].provider == $scope.ProviderBenchList[o]['provider.id'] && $scope.benchTable[k].size == $scope.ProviderBenchList[o]['provider.size'] && $scope.benchTable[k].tool == $scope.toolF.id && $scope.benchTable[k].workload == $scope.workloadF.id) {
                        if($scope.performanceCostFilters.provider!=""&&$scope.performanceCostFilters.provider!=null) {
                            if($scope.performanceCostFilters.provider== $scope.ProviderBenchList[o]['provider.id']){
                            $scope.BenchResultsAnalytics.push($scope.benchTable[k]);
                            }
                        }else{$scope.BenchResultsAnalytics.push($scope.benchTable[k]);}
                    }
                }
            }
            console.log($scope.BenchResultsAnalytics);
            if(($scope.performanceCostFilters.cpu!=""||$scope.performanceCostFilters.ram!="")&&($scope.performanceCostFilters.cpu!=null||$scope.performanceCostFilters.ram!=null)||$scope.performanceCostFilters.vm!=0)
            {

               // $scope.HardwareFiltering();
                $scope.hardwareFilteringSplice();
            }
            else{
                $scope.filteredCostModel= angular.copy($scope.costModelTable);
                $scope.performCostAnalysis();
            }
        });
        console.log("provider benchmark filtering done!");

    };

    $scope.HardwareFiltering=function(){
        $scope.filteredCostModel=[];
        for(var k=0;k<$scope.costModelTable.length;k++){
            $scope.filteredCostModel.push({"provider":$scope.costModelTable[k].provider,"costInfo":[]});
            for(var z=0; z< $scope.costModelTable[k].costInfo.length; z++){
                if ($scope.performanceCostFilters.cpu==$scope.costModelTable[k].costInfo[z].CPUNum&& $scope.performanceCostFilters.ram==$scope.costModelTable[k].costInfo[z].RAMSize){
                    $scope.filteredCostModel[k].costInfo.push($scope.costModelTable[k].costInfo[z]);
                    //console.log($scope.filteredCostModel[k].costInfo);
               }
            }

        }
        console.log($scope.filteredCostModel);
        $scope.performCostAnalysis();

    };
    $scope.cpuflag=true;
    $scope.ramflag=true;

    $scope.hardwareFilteringSplice=function(){
        $scope.cpuflag=true;
        $scope.ramflag=true;
        $scope.filteredCostModel=angular.copy($scope.costModelTable);
        for(var k=0; k<$scope.costModelTable.length; k++){
            for(var z=0; z< $scope.costModelTable[k].costInfo.length; z++){
                if($scope.performanceCostFilters.cpu!=""&&$scope.performanceCostFilters.cpu!= null){
                    if($scope.performanceCostFilters.cpu!=$scope.costModelTable[k].costInfo[z].CPUNum){
                        var index = $scope.filteredCostModel[k].costInfo.indexOf($scope.filteredCostModel[k].costInfo[z]);
                        $scope.filteredCostModel[k].costInfo.splice(index, 1);
                        $scope.cpuflag=false;
                    }
                }
                if($scope.performanceCostFilters.ram!=""&&$scope.performanceCostFilters.ram!= null){
                    if($scope.performanceCostFilters.ram!=$scope.costModelTable[k].costInfo[z].RAMSize){
                        if($scope.cpuflag){
                        var index2 = $scope.filteredCostModel[k].costInfo.indexOf($scope.filteredCostModel[k].costInfo[z]);
                        $scope.filteredCostModel[k].costInfo.splice(index2, 1);
                            $scope.ramflag=false;
                        }
                    }
                }
                if($scope.performanceCostFilters.vm > $scope.costModelTable[k].costInfo[z].maxVM){
                    if($scope.cpuflag&&$scope.ramflag){
                        var index6 = $scope.filteredCostModel[k].costInfo.indexOf($scope.filteredCostModel[k].costInfo[z]);
                        $scope.filteredCostModel[k].costInfo.splice(index6, 1);
                    }
                }
                $scope.cpuflag=true;
                $scope.ramflag=true;

            }

        }
        console.log($scope.filteredCostModel);
        console.log("cost Model Filtering");
        $scope.performCostAnalysis();
    };
    $scope.PCseries = ["Monthly Profitability", "CPU Profitability","VM Profitability"];
    $scope.PCLabels=[];
    $scope.PCData=[[],[],[]];
    $scope.performCostAnalysis=function(){
        $scope.PCLabels=[];
        $scope.PCData=[[],[],[]];
        for(var p=0; p < $scope.BenchResultsAnalytics.length; p++){
            if($scope.BenchResultsAnalytics[0].metrics.length==1){
                for(var c=0; c< $scope.filteredCostModel.length;c++){
                    if($scope.BenchResultsAnalytics[p].provider==$scope.filteredCostModel[c].provider){
                        for(var c1=0;c1<$scope.filteredCostModel[c].costInfo.length; c1++){
                            if($scope.filteredCostModel[c].costInfo[c1].size==$scope.BenchResultsAnalytics[p].size){
                                $scope.PCData[0].push((1/$scope.calcAVG($scope.BenchResultsAnalytics[p].metrics[0].values)*100*(1-$scope.performanceCostFilters.costPer))+(1/$scope.filteredCostModel[c].costInfo[c1].costMONTH*100*($scope.performanceCostFilters.costPer)));
                                $scope.PCData[1].push((1/$scope.calcAVG($scope.BenchResultsAnalytics[p].metrics[0].values)*100*(1-$scope.performanceCostFilters.costPer))+(1/$scope.filteredCostModel[c].costInfo[c1].costCPU*100*($scope.performanceCostFilters.costPer)));
                                $scope.PCData[2].push((1/$scope.calcAVG($scope.BenchResultsAnalytics[p].metrics[0].values)*100*(1-$scope.performanceCostFilters.costPer))+(1/$scope.filteredCostModel[c].costInfo[c1].costVM*100*($scope.performanceCostFilters.costPer)));
                                $scope.PCLabels.push($scope.BenchResultsAnalytics[p].provider+"|"+$scope.BenchResultsAnalytics[p].size);
                            }
                        }
                    }
                }
            }
            if($scope.BenchResultsAnalytics[0].metrics.length>1){
                for(var cc=0; cc< $scope.filteredCostModel.length;cc++){
                    if($scope.BenchResultsAnalytics[p].provider==$scope.filteredCostModel[cc].provider){
                        for(var cc1=0;cc1<$scope.filteredCostModel[cc].costInfo.length; cc1++){
                            if($scope.filteredCostModel[cc].costInfo[cc1].size==$scope.BenchResultsAnalytics[p].size){
                                for(var cc2=0; cc2<$scope.BenchResultsAnalytics[p].metrics.length; cc2++){
                                    if($scope.BenchResultsAnalytics[p].metrics[cc2].name=="duration"||$scope.BenchResultsAnalytics[p].metrics[cc2].name=="Duration"){
                                        $scope.PCData[0].push((1/$scope.calcAVG($scope.BenchResultsAnalytics[p].metrics[cc2].values)*100*(1-$scope.performanceCostFilters.costPer))+(1/$scope.filteredCostModel[cc].costInfo[cc1].costMONTH*100*($scope.performanceCostFilters.costPer)));
                                        $scope.PCData[1].push((1/$scope.calcAVG($scope.BenchResultsAnalytics[p].metrics[cc2].values)*100*(1-$scope.performanceCostFilters.costPer))+(1/$scope.filteredCostModel[cc].costInfo[cc1].costCPU*100*($scope.performanceCostFilters.costPer)));
                                        $scope.PCData[2].push((1/$scope.calcAVG($scope.BenchResultsAnalytics[p].metrics[cc2].values)*100*(1-$scope.performanceCostFilters.costPer))+(1/$scope.filteredCostModel[cc].costInfo[cc1].costVM*100*($scope.performanceCostFilters.costPer)));
                                        $scope.PCLabels.push($scope.BenchResultsAnalytics[p].provider+"|"+$scope.BenchResultsAnalytics[p].size);
                                    }
                                }

                            }
                        }
                    }
                }
            }
            //get on complex benchmarks duration
            //end of complex benchmark duration
        }
        $timeout(function () {
                $scope.costModelCreated=true;
                $scope.proCost=false;
            }
            , 2000);

    };
    $scope.downloadCostAnalysis= function(){
        $scope.DataJson={
            "services":$scope.PCLabels,
            "Metrics":$scope.PCseries,
            "Data": $scope.PCData
        }
        var data = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify($scope.DataJson));
        var downloader = document.createElement('a');

        downloader.setAttribute('href', data);
        downloader.setAttribute('download', 'Cost_Analysis.json');
        downloader.click();


    };
    //end of performance cost analysis
    // provider service combination
    $scope.listofServices=[];
    $scope.ListProviders= ["AWS", "Microsoft","Google", "COSMOTE","ULM", "FIWARE"];
    $scope.ListProvidersSLA= ["aws", "Microsoft","Google", "Cosmote","ULM", "FIWARE"];
    $scope.changeService= function(provider){
        switch(provider) {
            case "AWS":
                $scope.listofServices=["ec2","ec3"];
                $scope.BenchLaunch.provider.driver="ec2";
                break;
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
            case "COSMOTE":
                $scope.listofServices=["Compute"];
                $scope.BenchLaunch.provider.driver="openstack";
                break;
            case "Cosmote":
                $scope.listofServices=["Compute"];
                $scope.BenchLaunch.provider.driver="openstack";
                break;
            case "ULM":
                $scope.listofServices=["Compute"];
                $scope.BenchLaunch.provider.driver="openstack";
                break;
            case "FIWARE":
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
                    "Args":["java", "-jar", "3alibAuditoring.jar", "QoEUserID", "providerName", "serviceName", "auditor","0"],
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
        $scope.scaleResultsTable=[];
        $scope.SLAResults = dbService.GETSLAResults({id: $scope.qoeUser}, function (success) {
            $scope.SLAResults = $scope.SLAResults.results;
            for(var i=0 ; i< $scope.SLAResults.length; i++ ){
                $scope.slaResultsTable.push($scope.SLAResults[i]);
            }
            console.log($scope.SLAResults);
        });
        $scope.scaleResults = dbService.GETScaleResults({id: $scope.qoeUser}, function (success) {
            $scope.scaleResults = $scope.scaleResults.results;
            for(var i=0 ; i< $scope.scaleResults.length; i++ ){
                $scope.scaleResultsTable.push($scope.scaleResults[i]);
            }
            console.log($scope.scaleResults);
        });
    };
    // get running sla auditors
    $scope.launchSLAAuditors=function() {
        $scope.slaAuditorsTable=[];
        $scope.scaleAuditorTable=[];
        $scope.SLAAuditors = dbService.GETSLAAuditors({id: $scope.qoeUser}, function (success) {
            $scope.SLAAuditors = $scope.SLAAuditors.results;
            for(var i=0 ; i< $scope.SLAAuditors.length; i++ ){
                $scope.slaAuditorsTable.push($scope.SLAAuditors[i]);
            }
            console.log($scope.SLAAuditors);
        });
        $scope.scaleAuditors = dbService.GETScaleAuditors({id: $scope.qoeUser}, function (success) {
            $scope.scaleAuditors = $scope.scaleAuditors.results;
            for(var i=0 ; i< $scope.scaleAuditors.length; i++ ){
                $scope.scaleAuditorTable.push($scope.scaleAuditors[i]);
            }
            console.log($scope.scaleAuditors);
        });
    };

    $scope.createCustomSLA=function(type){
        console.log(type);
        if(type=="availability") {
            $scope.customSLAData = 'docs={' +
                '    "id" : "' + $scope.qoeUser + '",' +
                '    "Provider" : "' + $scope.SLALaunch.providerName + '",' +
                '    "Service" : "' + $scope.SLALaunch.serviceName + '",' +
                '    "mail_notification" : "' + $scope.emailNotifications + '",' +
                '    "custom" : "' + $scope.customAvailability + '",' +
                '    "period" : "' + $scope.customPeriod + '",' +
                '    "Soft_SLA" : ' + $scope.softAvailability + ',' +
                '    "Hard_SLA" : ' + $scope.hardAvailability + ',' +
                '    "downtime" : ' + $scope.downtime + '' +
                '}';
            dbService.SLACreateCustomAgreement($scope.customSLAData, function (success) {
                console.log("custom sla created successfully")
            });
        }else{
            $scope.customSLAData = 'docs={' +
                '    "id" : "' + $scope.qoeUser + '",' +
                '    "Provider" : "' + $scope.ScaleLaunch.provider + '",' +
                '    "Service" : "' + $scope.ScaleLaunch.service  + '",' +
                '    "scale_group" : "' + $scope.ScaleLaunch.scale_group + '"' +
                '}';
            dbService.scaleCreateCustomAgreement($scope.customSLAData, function (success) {
                console.log("custom  scale sla created successfully")
            });

        }

    };
    //new bench configuration
    $scope.BenchLaunchData={
        "Name": "",
        "Data": ""
    };
$scope.initiateBench2=function(){
    console.log($scope.BenchLaunch);
    console.log($scope.BenchScheduler);

};
    $scope.convertVMValues=function(){
        $scope.BenchLaunch.vm.image=$scope.BenchLaunch.vm.image.name;
        $scope.BenchLaunch.vm.size =$scope.BenchLaunch.vm.size.name;
        $scope.BenchLaunch.provider.network= $scope.BenchLaunch.provider.network.name;
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
    $scope.deleteScheduler=function(){
        $scope.scheduleIDidString='criteria={"id":"'+$scope.scheduleID.id+'"}';
        dbService.schedulerDelete($scope.scheduleIDidString, function (success) {
            console.log("scheduler Deleted!");
            dbService.dockerSecretDelete({id:$scope.scheduleID.id},function (success) {
                console.log("docker secret deleted!")

            });
            $scope.scheduleID={};
            $scope.benches=[];
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
            $scope.createCustomSLA("availability");
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
    $scope.scaleSecret="";
    $scope.initiateLaunchScale=function(){
        console.log($scope.ScaleLaunch.service);

        $scope.scaleSecret="ProviderName="+$scope.ScaleLaunch.provider+"\n"+
            "ServiceName="+$scope.ScaleLaunch.service+"\n"+
            "user="+$scope.ScaleLaunch.cloudUser+"\n"+
            "promised_Scale_Time="+$scope.ScaleLaunch.promised_Scale_Time+"\n"+
            "promised_cpu_util="+$scope.ScaleLaunch.promised_cpu_util+"\n"+
            "max_instances="+$scope.ScaleLaunch.max_instances+"\n"+
            "minutes="+$scope.ScaleLaunch.minutes+"\n"+
            "scale_group="+$scope.ScaleLaunch.scale_group+"\n"+
            "APIkey="+$scope.ScaleLaunch.apiKey;
        // encode secret to base64
        $scope.scaleSecret=btoa(encodeURIComponent($scope.scaleSecret).replace(/%([0-9A-F]{2})/g, function toSolidBytes(match, p1) {return String.fromCharCode('0x' + p1);}));
        console.log($scope.scaleSecret);
        $scope.ScaleLaunch.scale_group=$scope.ScaleLaunch.scale_group.substring(0,5);
        $scope.SLALaunchData.Name=$scope.ScaleLaunch.scale_group+"_"+$scope.qoeUser+"_"+$scope.ScaleLaunch.provider+"_"+$scope.ScaleLaunch.service+"_"+"auditor";
        $scope.SLALaunchData.Data= $scope.scaleSecret;

        $scope.secretCreate=dbService.dockerSecret($scope.SLALaunchData, function (success) {
            console.log($scope.secretCreate.ID);
            $scope.SLACreateServiceData.name=$scope.SLALaunchData.Name;
            $scope.SLACreateServiceData.TaskTemplate.ContainerSpec.Args[3]=$scope.qoeUser;
            $scope.SLACreateServiceData.TaskTemplate.ContainerSpec.Args[4]=$scope.ScaleLaunch.provider;
            $scope.SLACreateServiceData.TaskTemplate.ContainerSpec.Args[5]=$scope.ScaleLaunch.service;
            $scope.SLACreateServiceData.TaskTemplate.ContainerSpec.Args[7]=$scope.ScaleLaunch.scale_group;
            $scope.SLACreateServiceData.TaskTemplate.ContainerSpec.Secrets[0].File.Name=$scope.SLALaunchData.Name;
            $scope.SLACreateServiceData.TaskTemplate.ContainerSpec.Secrets[0].SecretID=$scope.secretCreate.ID;
            $scope.SLACreateServiceData.TaskTemplate.ContainerSpec.Secrets[0].SecretName=$scope.SLALaunchData.Name;
            $scope.createSLAContainer=dbService.SLACreateService($scope.SLACreateServiceData,function(success){
                console.log($scope.createSLAContainer);
                $scope.successScaleLaunch=true ;
                $scope.createCustomSLA("scale");

            });

        });
    };
    //delete auditor
    $scope.deleteAuditor=function(type){

    };
    $scope.benchCategoriesGrouping= 'cmd={"group" : {"ns" : "results", "$reduce" : "function(curr,result){}", "key" : {"provider.id" : 1,"provider.size": 1, "test.tool": 1, "test.workload":1}, "initial" : {"total":0}}}';
    $scope.benchProvidersGrouping= 'cmd={"group" : {"ns" : "results", "$reduce" : "function(curr,result){}", "key" : {"provider.id" : 1,"provider.size": 1}, "initial" : {"total":0}}}';
    // get analytics
    $scope.BenchResultsAnalytics=[];
    $scope.getProviderBenchCategories=function(){
        $scope.finishRenderAnalytics=false;
        $scope.BenchResultsAnalytics=[];
       $scope.ProviderBenchList= dbService.GETBenchGroups($scope.benchProvidersGrouping, function (success) {
           $scope.ProviderBenchList= $scope.ProviderBenchList.retval;
           console.log($scope.ProviderBenchList);

           for (var o=0; o<$scope.ProviderBenchList.length; o++){
               for(var k=0; k<$scope.benchTable.length; k++) {
                   if ($scope.benchTable[k].provider == $scope.ProviderBenchList[o]['provider.id'] && $scope.benchTable[k].size == $scope.ProviderBenchList[o]['provider.size'] && $scope.benchTable[k].tool == $scope.toolF.id && $scope.benchTable[k].workload == $scope.workloadF.id) {
                       $scope.BenchResultsAnalytics.push($scope.benchTable[k]);
                   }
               }
           }
           console.log($scope.BenchResultsAnalytics);
           $scope.createAnalyticsBarCharts($scope.BenchResultsAnalytics);
       });

    };

    //function for creating analytics

    $scope.options = {
        legend: { display: true }
    };
    //data for average scores

    $scope.labelsAVG = [];
    $scope.seriesAVG = [];
    $scope.dataAVG = [];
    $scope.dataDEV=[];
    $scope.dataPVCAVG=[];
    $scope.dataPVCMAX=[];
// end of chart tests
    $scope.dataforavg=[];

    //end of average score Data
    $scope.createAnalyticsBarCharts=function(benchAnalytics){
        $scope.labelsAVG = [];
        $scope.seriesAVG = [];
        $scope.dataAVG = [];
        $scope.dataDEV=[];
        $scope.dataPVCAVG=[];
        $scope.dataPVCMAX=[];
// end of chart tests
        $scope.dataforavg=[];
        $scope.datafordev=[];
        $scope.dataforpvcavg=[];
        $scope.dataforpvcmax=[];
        if(benchAnalytics[0].metrics.length==1){
            $scope.dataAVG = [[]];
            $scope.dataDEV=[[]];
            $scope.dataPVCAVG=[[]];
            $scope.dataPVCMAX=[[]];
            $scope.seriesAVG.push(benchAnalytics[0].metrics[0].name);
            for(var ko=0;ko<benchAnalytics.length;ko++){
                $scope.labelsAVG.push(benchAnalytics[ko].provider+"|"+ benchAnalytics[ko].size);
                $scope.dataAVG[0].push($scope.calcAVG(benchAnalytics[ko].metrics[0].values));
                $scope.dataDEV[0].push($scope.calcDEV(benchAnalytics[ko].metrics[0].values));
                $scope.dataPVCAVG[0].push($scope.calcPVCAVG(benchAnalytics[ko].metrics[0].values));
                $scope.dataPVCMAX[0].push($scope.calcPVCMAX(benchAnalytics[ko].metrics[0].values));
            }
        }
        else{
            for(var kk=0;kk<benchAnalytics.length; kk++){
                $scope.labelsAVG.push(benchAnalytics[kk].provider+"|"+ benchAnalytics[kk].size);
                if(kk==0){
                    for(var kk2=0; kk2<benchAnalytics[0].metrics.length; kk2++){
                        $scope.seriesAVG.push(benchAnalytics[0].metrics[kk2].name);
                    }
                }
                for(var kk3=0; kk3<benchAnalytics[0].metrics.length; kk3++){
                    $scope.dataforavg.push($scope.calcAVG(benchAnalytics[kk].metrics[kk3].values));
                    $scope.datafordev.push($scope.calcDEV(benchAnalytics[kk].metrics[kk3].values));
                    $scope.dataforpvcavg.push($scope.calcPVCAVG(benchAnalytics[kk].metrics[kk3].values));
                    $scope.dataforpvcmax.push($scope.calcPVCMAX(benchAnalytics[kk].metrics[kk3].values));
                }
                $scope.dataAVG.push($scope.dataforavg);
                $scope.dataDEV.push($scope.datafordev);
                $scope.dataPVCAVG.push($scope.dataforpvcavg);
                $scope.dataPVCMAX.push($scope.dataforpvcmax);
                $scope.dataforavg=[];
                $scope.datafordev=[];
                $scope.dataforpvcavg=[];
                $scope.dataforpvcmax=[];
            }
        }

        $timeout(function () {
                $scope.finishRenderAnalytics=true;
                console.log($scope.dataAVG);
                console.log($scope.labelsAVG);
                console.log($scope.seriesAVG);
            }
            , 5000);




    };

//end of analytics
    // edit cost model
    $scope.getCostModel=function(){
      $scope.costModelTable= dbService.GETCostModel({id:$scope.qoeUser},function (success) {
          $scope.costModelTable=$scope.costModelTable.results[0].ProviderCostModels;
          console.log($scope.costModelTable);
      });

    };


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
                if ($scope.benchResults[j]['provider.id'] != null && $scope.benchResults[j]['provider.size'] != null && $scope.benchResults[j]['test.workload'] != null && $scope.benchResults[j]['test.tool'] != null) {
                    dbService.GETBenchResults(
                        {
                            provider: $scope.benchResults[j]['provider.id'],
                            size: $scope.benchResults[j]['provider.size'],
                            workload: $scope.benchResults[j]['test.workload'],
                            tool: $scope.benchResults[j]['test.tool']
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
            workload:benchR.results[0].test.workload,
            tool:benchR.results[0].test.tool,
            metrics:$scope.tableInput.metrics
        });
        $scope.benchResultForStorage.provider=benchR.results[0].provider.id;
        $scope.benchResultForStorage.size=benchR.results[0].provider.size;
        $scope.benchResultForStorage.workload=benchR.results[0].test.workload;
        $scope.benchResultForStorage.tool=benchR.results[0].test.tool;
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
            console.log($scope.privateBenchResults);
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
                    //$scope.launchBenchResults();
                    $scope.authenticated=true;
                    $scope.qoeUser= response.results[0].userID;
                    $scope.qoeUserInfo= response.results[0];
                    // service initialization
                    $scope.launchBenchResultsStored();
                   $scope.launchSLAResults();
                    $scope.GETproviderSLA();
                    $scope.loadSchedules();
                    $scope.getBenchLogs();
                   $scope.GETPrivateBench();
                   $scope.launchSLAAuditors();
                    $scope.getCostModel();
                    $scope.benchmarkTests=dbService.GETBenchTEST(function(success){console.log($scope.benchmarkTests)});

                }
                else{$scope.failLog=true};
            })
    };

//chart for provider SLA
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