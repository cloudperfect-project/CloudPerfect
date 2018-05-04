#!/usr/bin/env bash 

echo -n "Please provide an existing folder as the installation path/workspace (Linux full path)> ";
read path;
echo "Profiler installation path: $path";
#comment next command and uncomment previous...
#path='/home/fot/Desktop/workspace';

echo -n "Please specify the installation mode (Type 'p' for IaaS Provider and 'a' for Cloud Adopter)> ";
read mode;
echo "Tool installation mode: $mode";

#Create profiler folder and download jar 
mkdir  -p  $path/profiler;
sudo chmod 777 $path/profiler;
wget https://github.com/cloudperfect-project/CloudPerfect/raw/master/files/ProfilingTool-fot.jar -O $path/profiler/latest-profiler.jar;

	
#Download benchmark commands list (adopter mode) or benchmark names list for benchmarking suite (provider mode)
if [ "$mode" = "a" ]; then

wget https://github.com/cloudperfect-project/CloudPerfect/raw/master/files/benchmark_workloads.txt -O $path/profiler/benchmark_workloads.txt;

else
wget https://github.com/cloudperfect-project/CloudPerfect/raw/master/files/benchmark_workloads_list.txt -O $path/profiler/benchmark_workloads.txt;

fi

sudo chmod 777 $path/profiler/benchmark_workloads.txt;


#Create classifier folder and download jar 
mkdir  -p  $path/classifier;
sudo chmod 777 $path/classifier;
wget https://github.com/cloudperfect-project/CloudPerfect/raw/master/files/ClassificationTool.jar -O $path/classifier/ClassificationTool.jar;

#Create tmp folder needed for tshark process
mkdir  -p  $path/profiler/tmp;
sudo chmod 777 $path/profiler/tmp;

#Create run script for classifier
echo "#!/usr/bin/env bash

path=\$(dirname \"\$0\")
cd \$path/

NoGUI=\"\$1\"
ProviderMode=\"\$2\"
java -jar ClassificationTool.jar \$NoGUI \$ProviderMode" > $path/classifier/classifier-run.sh;

#Create run script for profiler
echo "#!/usr/bin/env bash

Arg1=\"\$1\"
Arg2=\"\$2\"
Arg3=\"\$3\"
Arg4=\"\$4\"

if [ -n \"\$Arg1\" -a  -n \"\$Arg2\" -a  -n \"\$Arg3\" ]; then
	cd $path/profiler/
	java -jar latest-profiler.jar \$Arg1 \$Arg2 \$Arg3 \$Arg4

	if [ \"\$Arg2\" = \"application\" ]; then
		sh $path/classifier/classifier-run.sh NoGUI \$Arg4
	fi

else

passwd='nopass';
choice='C';
while [ \"\$choice\" != \"a\" -a  \"\$choice\" != \"b\" ]
do
	echo -n \"Choose your profiling target. Type [a] for application or [b] for benchmarks> \";
	read choice;
done

if [ \"\$choice\" = \"a\" ]; then
	target=\"application\"
	info=\"application-info.txt\"
else
	target=\"benchmarks\"
	info=\"benchmark-info.txt\"
fi

echo -n \"Please type [f] if you want to use the respective configuration txt file, else press enter (GUI will appear)> \";
read file;
if [ \"\$file\" != \"f\" ]; then
	info=\"\"
else
       echo \"Provide the root password:\"
       stty_orig=\`stty -g\` # save original terminal setting.
       stty -echo          # turn-off echoing.
       read passwd         # read the password
       stty \$stty_orig;     #restore echoing
fi
cd $path/profiler/

java -jar latest-profiler.jar \$passwd \$target \$info

if [ \"\$choice\" = \"a\" ]; then
	sh $path/classifier/classifier-run.sh NoGUI
fi

fi" > $path/profiler/profiler-run.sh;

sudo chmod 777 $path/profiler/profiler-run.sh;

	
#Create conf.ini file in classifier folder and template info configuration files
if [ "$mode" = "a" ]; then

#Download and install tshark & sysstat applications
command -v tshark >/dev/null 2>&1 || { sudo apt-get install tshark; }
command -v sysstat >/dev/null 2>&1 || { sudo apt-get install sysstat; }

echo "
[classification]
engine=knn

[database]
host=147.102.19.75
port=27080
user=results
pass=cloud
db=benchmarking

[service_efficiency]
price_weight=0.5
performance_weight=0.5
performance_metric=duration" > $path/classifier/conf.ini;

echo "$path/profiler
Replace_With_VM_Process_ID,Replace_with_profiling_duration(seconds)
Replace_With_Host_IP,Replace_With_Host_Interface" > $path/profiler/application-info.txt;
sudo chmod 777 $path/profiler/application-info.txt;

echo "$path/profiler
Replace_With_VM_Process_ID
Replace_With_Host_IP,Replace_With_Host_Interface
Replace_With_Benchmark_VM_IP,Replace_With_Benchmark_VM_Username,$path/profiler/benchmark_workloads.txt,Replace_With_Benchmark_VM_UserPassword,Replace_With_Benchmark_VM_RootPassword" > $path/profiler/benchmark-info.txt;
sudo chmod 777 $path/profiler/benchmark-info.txt;


else
echo "
[classification]
engine=knn

[database]
host=localhost
port=3306
user=root	
pass=cloud
db=profiler" > $path/classifier/conf.ini;

echo "$path/profiler
Replace_With_APPLICATION_VM_ID,300
Replace_With_Ceilometer_Host_IP,Replace_With_Ceilometer_Auth_token" > $path/profiler/application-provider-info.txt;
sudo chmod 777 $path/profiler/application-provider-info.txt;

echo "$path/profiler
Replace_With_Benchmark_Suite_URL
Replace_With_Benchmark_Suite_Provider_ID,Replace_With_NeededVM_Type_Size,$path/profiler/benchmark_workloads.txt
Replace_With_Ceilometer_Host_IP,Replace_With_Ceilometer_Auth_token" >  > $path/profiler/benchmark-provider-info.txt;
sudo chmod 777 $path/profiler/benchmark-provider-info.txt;

fi



echo "Profiler successfully installed at: $path/profiler
You can run via the profiler-run.sh script (Please read the documentation for input arguments).";
