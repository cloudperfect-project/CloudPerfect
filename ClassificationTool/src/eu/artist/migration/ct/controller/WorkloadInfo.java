package eu.artist.migration.ct.controller;

public class WorkloadInfo {
	private String workloadId;
	private String tableName;
	private String workloadName;
	private String metricName;
	
	public static final WorkloadInfo[] workloadInfos = new WorkloadInfo[] {
						 //workload_id				table_name		workloadName			metricName
		new WorkloadInfo("DaCapo-avrora",			"dacapo-ft",	"avrora",				"cpu_time"),
		new WorkloadInfo("DaCapo-eclipse",			"dacapo-ft",	"eclipse",				"cpu_time"),
		new WorkloadInfo("DaCapo-fop",				"dacapo-ft",	"fop",					"cpu_time"),
		new WorkloadInfo("DaCapo-h2",				"dacapo-ft",	"h2",					"cpu_time"),
		new WorkloadInfo("DaCapo-jython",			"dacapo-ft",	"jython",				"cpu_time"),
		new WorkloadInfo("DaCapo-pmd",				"dacapo-ft",	"pmd",					"cpu_time"),
		new WorkloadInfo("DaCapo-tomcat",			"dacapo-ft",	"tomcat",				"cpu_time"),
		new WorkloadInfo("DaCapo-xalam",			"dacapo-ft",	"xalan",				"cpu_time"),
		new WorkloadInfo("Filebench-fileserver",	"filebench",	"fileserver",			"latency"),
		new WorkloadInfo("Filebench-varmail",		"filebench",	"varmail",				"latency"),
		new WorkloadInfo("Filebench-webserver",		"filebench",	"webserver",			"latency"),
		new WorkloadInfo("Filebench-webproxy",		"filebench",	"webproxy",				"latency"),
		new WorkloadInfo("Filebench-videoserver",	"filebench",	"videoserver",			"latency"),
		new WorkloadInfo("YCSB-workloada", 			"ycsb-mysql",	"workloada",			"ops/s"),
		new WorkloadInfo("YCSB-workloadb", 			"ycsb-mysql", 	"workloadb",			"ops/s"),
		new WorkloadInfo("YCSB-workloadc", 			"ycsb-mysql",	"workloadc",			"ops/s"),
		new WorkloadInfo("YCSB-workloadd", 			"ycsb-mysql",	"workloadd",			"ops/s"),
		new WorkloadInfo("YCSB-workloade", 			"ycsb-mysql",	"workloade",			"ops/s"),
		new WorkloadInfo("YCSB-workloadf", 			"ycsb-mysql",	"workloadf",			"ops/s"),
		new WorkloadInfo("CFD-runCase",				"cfd",			"100iter",				"cpu_time"),
	};
	
	public static WorkloadInfo findWorkload(String workloadId) {
		for (WorkloadInfo workloadInfo: workloadInfos) {
			if (workloadInfo.getWorkloadId().matches(workloadId)) {
				return workloadInfo;
			}
		}
		
		return null;
	}
	
	public WorkloadInfo(String workloadId, String tableName,
			String workloadName, String metricName) {
		super();
		this.workloadId = workloadId;
		this.tableName = tableName;
		this.workloadName = workloadName;
		this.metricName = metricName;
	}

	public String getWorkloadId() {
		return workloadId;
	}

	public String getTableName() {
		return tableName;
	}

	public String getWorkloadName() {
		return workloadName;
	}

	public String getMetricName() {
		return metricName;
	}
}
