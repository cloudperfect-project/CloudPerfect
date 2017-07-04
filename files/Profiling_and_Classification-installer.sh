#!/usr/bin/env bash 

echo -n "Please provide an existing folder as the installation path/workspace (Linux full path)> ";
read path;
echo "Profiler installation path: $path";
#comment next command and uncomment previous...
#path='/home/fot/Desktop/workspace';

#Create profiler folder and download jar and benchmark commands
mkdir  -p  $path/profiler;
sudo chmod 777 $path/profiler;
wget https://github.com/cloudperfect-project/CloudPerfect/raw/master/files/ProfilingTool-fot.jar -O $path/profiler/latest-profiler.jar;
wget https://github.com/cloudperfect-project/CloudPerfect/raw/master/files/benchmark_workloads.txt -O $path/profiler/benchmark_workloads.txt;
sudo chmod 777 $path/profiler/benchmark_workloads.txt

#Create classifier folder and download jar 
mkdir  -p  $path/classifier;
sudo chmod 777 $path/classifier;
wget https://github.com/cloudperfect-project/CloudPerfect/raw/master/files/ClassificationTool.jar -O $path/classifier/ClassificationTool.jar;

#Create tmp folder needed for tshark process
mkdir  -p  $path/profiler/tmp;
sudo chmod 777 $path/profiler/tmp;

#Download and install tshark & sysstat applications
command -v tshark >/dev/null 2>&1 || { sudo apt-get install tshark; }
command -v sysstat >/dev/null 2>&1 || { sudo apt-get install sysstat; }


#Create run script for classifier
echo "#!/usr/bin/env bash

path=\$(dirname \"\$0\")
cd \$path/

NoGUI=\"\$1\"
if [ \"\$NoGUI\" != \"\" ]; then
	java -jar ClassificationTool.jar \$NoGUI
else
	java -jar ClassificationTool.jar
fi" > $path/classifier/classifier-run.sh;

#Create conf.ini file for classifier
echo "
[classification]
engine=knn

[database]
host=localhost
port=3306
user=root	
pass=1234
db=azure

[service_efficiency]
price_weight=0.5
performance_weight=0.5" > $path/classifier/conf.ini;

#Create run script for profiler
echo "#!/usr/bin/env bash

Arg1=\"\$1\"
Arg2=\"\$2\"
Arg3=\"\$3\"

if [ -n \"\$Arg1\" -a  -n \"\$Arg2\" -a  -n \"\$Arg3\" ]; then
	cd $path/profiler/
	java -jar latest-profiler.jar \$Arg1 \$Arg2 \$Arg3
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

#Create Profiler template info.txt files, for runtime input parameters
echo "$path/profiler
Replace_With_VM_Process_ID,Replace_with_profiling_duration(seconds)
Replace_With_Host_IP,Replace_With_Host_Interface" > $path/profiler/application-info.txt;
sudo chmod 777 $path/profiler/application-info.txt;
echo "$path/profiler
Replace_With_VM_Process_ID
Replace_With_Host_IP,Replace_With_Host_Interface
Replace_With_Benchmark_VM_IP,Replace_With_Benchmark_VM_Username,$path/profiler/benchmark_workloads.txt,Replace_With_Benchmark_VM_UserPassword,Replace_With_Benchmark_VM_RootPassword" > $path/profiler/benchmark-info.txt;
sudo chmod 777 $path/profiler/benchmark-info.txt;

echo "Profiler successfully installed at: $path/profiler
You can run via the profiler-run.sh script (Please read the documentation for input arguments).";
