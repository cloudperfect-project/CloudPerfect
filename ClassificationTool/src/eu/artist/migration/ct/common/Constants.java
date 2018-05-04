package eu.artist.migration.ct.common;


public class Constants {
	public static final int DEFAULT_PIDSTAT_VECTOR_SIZE = 15;
	public static final int DEFAULT_TSHARK_VECTOR_SIZE = 4;
	public static final int DEFAULT_TRAINING_VECTOR_SIZE = DEFAULT_PIDSTAT_VECTOR_SIZE + DEFAULT_TSHARK_VECTOR_SIZE * 2;
	public static final String DEFAULT_CONFIGURATION_FILE = "conf.ini";
	
	public static final String [] BENCHMARKS_WORKLOADS = {"cfd-100iter",
"filebench-varmail",
"filebench-webproxy",
"filebench-webserver",
"filebench-videoserver",
"filebench-fileserver",
"dacapo-ft-avrora",
"dacapo-ft-eclipse",
"dacapo-ft-fop",
"dacapo-ft-h2",
"dacapo-ft-jython",
"dacapo-ft-pmd",
"dacapo-ft-tomcat",
"dacapo-ft-xalan",
"ycsb-mysql-workloada",
"ycsb-mysql-workloadb",
"ycsb-mysql-workloadc",
"ycsb-mysql-workloadd",
"ycsb-mysql-workloade",
"ycsb-mysql-workloadf" };

}
