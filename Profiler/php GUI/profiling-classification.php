<html>
<title>Profiling and Classification</title>
   <!-- CSS INCLUDE -->
    <style>.file-input-wrapper { overflow: hidden; position: relative; cursor: pointer; z-index: 1; }.file-input-wrapper input[type=file], .file-input-wrapper input[type=file]:focus, .file-input-wrapper input[type=file]:hover { position: absolute; top: 0; left: 0; cursor: pointer; opacity: 0; filter: alpha(opacity=0); z-index: 99; outline: 0; }.file-input-name { margin-left: 8px; }</style><link rel="stylesheet" type="text/css" id="theme" href="css/theme-blue.css">
    <!-- EOF CSS INCLUDE -->

<style type="text/css">.jqstooltip { position: absolute;left: 0px;top: 0px;visibility: hidden;background: rgb(0, 0, 0) transparent;background-color: rgba(0,0,0,0.6);filter:progid:DXImageTransform.Microsoft.gradient(startColorstr=#99000000, endColorstr=#99000000);-ms-filter: "progid:DXImageTransform.Microsoft.gradient(startColorstr=#99000000, endColorstr=#99000000)";color: white;font: 10px arial, san serif;text-align: left;white-space: nowrap;padding: 5px;border: 1px solid white;z-index: 10000;}.jqsfield { color: white;font: 10px arial, san serif;text-align: left;}</style></head>
<body ng-controller="mainController" class="ng-scope">



<new-login ng-show="!authenticated" ></new-login>

<div ng-show="authenticated"><!-- START main page CONTAINER -->

    <!-- START PAGE CONTAINER -->
    <div class="page-container" ng-init="nav=1">

        <!-- START PAGE SIDEBAR -->
        <div class="page-sidebar">
		
            <!-- START X-NAVIGATION -->
            <ul class="x-navigation"  style="width:100%">
                <li class="xn-logo" >
                    <a href="/profiler/profiling-classification.php"  style="height: 13%">Profiling and Classification Dashboard</a>
                    
                </li>
                <li class="xn-profile">
                    <a href="#" class="profile-mini">
                        <img  src="logo.png"  alt="logo">
                    </a>
                    <div class="profile">
                        <div class="profile-data">
                            <div class="profile-data-name ng-binding">COSMOTE</div>
                            <div class="profile-data-title">Admininstrator</div>
                            <div class="profile-controls">
                                <a href="" class="profile-control-left"><span class="myImg"></span></a>
                            </div>
                        </div>

                    </div>
                </li>
      

            </ul>
            <!-- END X-NAVIGATION -->
			
        </div>
        <!-- END PAGE SIDEBAR -->

        <!-- PAGE CONTENT -->
        <div class="page-content" style="height: 1104px;">

            <!-- START X-NAVIGATION VERTICAL -->
            <ul class="x-navigation x-navigation-horizontal x-navigation-panel">
                <!-- TOGGLE NAVIGATION -->
                <li class="xn-icon-button">
                    <a href="#" class="x-navigation-minimize"><span class="fa fa-dedent"></span></a>
                </li>
                <li class="xn-icon-button pull-right">
                    <a href="#" class="mb-control" data-box="#mb-signout"><span class="fa fa-sign-out"></span></a>
                </li>
                <!-- END TOGGLE NAVIGATION -->
            </ul>
            <!-- END X-NAVIGATION VERTICAL -->

           
   


<!-- PAGE CONTENT WRAPPER -->
<div class="page-content-wrap">

    <div class="row">
        <div class="col-md-12">

			<div class="panel panel-default" ng-init="slaF=''">
                <div class="panel-heading">
                    <h3 class="panel-title col-md-12">Available VM Instances  </h3>
                </div>
            <div class="panel-body">
			
<?php

function getPeriod($str, $vm_id) {
	
	foreach(preg_split("/((\r?\n)|(\r\n?))/", $str) as $line){
    // do stuff with $line
		if (preg_match('/\b' . $vm_id . '\b/',$line))
		return($line);
	} 
    return('');
}

function getTimes($cronjob) {
	
	$pieces = preg_split('/\s+/', $cronjob);

	$minutes = $pieces[0];
	$hours = $pieces[1];
	
	$times = '';
	foreach(preg_split("/,/", $hours) as $hour){
		if (strcmp($hour, "*") == 0) 
			return "every houy";
		if (strcmp($hour, "") == 0) 
			return "";
		foreach(preg_split("/,/", $minutes) as $minute){	
			$times .= ' | ' . $hour . ':' . $minute;
		}	
	} 	
	return $times; 
}

function getDays($cronjob) {
	
	$pieces = preg_split('/\s+/', $cronjob);
	return $pieces[2]; 
}

$output = shell_exec('crontab -l');
//echo $output;
?>

<table class="table table-striped" style="width: 62%">
<thead>
<tr>
<th>Instance Id</th><th>Instance Profiles</th><th> Instance Categories</th><th> Profiling Period loop  <a href="profiling-schedule.php"> [edit] </a></th>
</tr>
</thead>
<tbody>
<?php 

$connection = mysqli_connect("localhost", "root", "my-cl0udPdbswdmy", "profiler");

// Check connection
if (!$connection) {
	echo "failure in connection!";
    die("Connection failed: " . mysqli_connect_error());
} 	


$sql = "SELECT DISTINCT vm_id FROM CATEGORIES;"; // WHERE category='$user'

if($result = mysqli_query($connection, $sql)) { 
	if (mysqli_num_rows($result)>0){
		while ($row = mysqli_fetch_array($result)){
			$id = $row["vm_id"];
			$cronjob = getPeriod($output, $id);
			$days = getDays($cronjob);
			$times = getTimes($cronjob);
			echo "<tr  style=\"font-size: 13px;\">";
			echo "<td class=\"ng-binding\"  align=\"center\">" . $id . "</td><td class=\"ng-binding\"  align=\"center\"><a href=\"profiles.php?id=" . $id . "\">PROFILES</a></td><td class=\"ng-binding\"   align=\"center\"><a href=\"categories.php?id=" . $id . "\">CATEGORIES</a></td>
			<td class=\"ng-binding\" align=\"center\">" . "Days of the month: " . $days . "<BR>Scheduled profilings: " . $times . "</td>";
			echo "</tr>";
		}
	}	
	else {
		echo "no results!";
	}	
	
}



mysqli_close($connection);

?>
				
				        </tbody>
 </table>
                </div>
            </div>
			
        </div>
    </div>
</div>


</body>
</html>
