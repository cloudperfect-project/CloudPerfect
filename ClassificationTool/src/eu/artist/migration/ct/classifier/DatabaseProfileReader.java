package eu.artist.migration.ct.classifier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import eu.artist.migration.ct.common.Constants;
import eu.artist.migration.ct.controller.DbConnection;

//FOT: new CLass for Provider mode!
public class DatabaseProfileReader  extends ProfileResultsReader {

	private int processVectorSize;
	private int networkVectorSize;
	private String vm_id;
	
	public DatabaseProfileReader() {
		processVectorSize = Constants.DEFAULT_PIDSTAT_VECTOR_SIZE;
		networkVectorSize = Constants.DEFAULT_TSHARK_VECTOR_SIZE;
		
	}
	
	public DatabaseProfileReader(String vm_id) {
		this();
		this.vm_id = vm_id;
	}
	

	public int getProcessVectorSize() {
		return processVectorSize;
	}
	
	public void setProcessVectorSize(int processVectorSize) {
		this.processVectorSize = processVectorSize;
	}
	
	public int getNetworkVectorSize() {
		return networkVectorSize;
	}
	
	public void setNetworkVectorSize(int networkVectorSize) {
		this.networkVectorSize = networkVectorSize;
	}

	public ApplicationWorkload getWorkloadFromDB(Connection connection) throws Exception {

		
		ApplicationWorkload workload = new ApplicationWorkload();		
		double[] applicationVector = new double[Constants.DEFAULT_TRAINING_VECTOR_SIZE];
		
		PreparedStatement preparedStatement = null;
		String selectSQL = "SELECT * FROM PROFILES WHERE vm_id = ?";

		try {
			connection = DbConnection.getConnection();
			preparedStatement = connection.prepareStatement(selectSQL);
			preparedStatement.setString(1, this.vm_id);
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next()) {

				applicationVector[0] = Double.parseDouble(rs.getString("usr"));
				applicationVector[1] = Double.parseDouble(rs.getString("system"));
				applicationVector[2] = Double.parseDouble(rs.getString("cpu"));
				applicationVector[3] = Double.parseDouble(rs.getString("cpu_util"));
				applicationVector[4] = Double.parseDouble(rs.getString("disk_alloc"));
				
				//FOT: added a zero value, so that vector is of the same form as in Adopter mode workload
				applicationVector[5] = 0.0f;
				
				applicationVector[6] = Double.parseDouble(rs.getString("t_MEM"));
				applicationVector[7] = Double.parseDouble(rs.getString("VSZ"));
				applicationVector[8] = Double.parseDouble(rs.getString("RSS"));
				applicationVector[9] = Double.parseDouble(rs.getString("MEM"));
				applicationVector[10] = Double.parseDouble(rs.getString("kB_rds"));
				applicationVector[11] = Double.parseDouble(rs.getString("kB_wrs"));
				applicationVector[12] = Double.parseDouble(rs.getString("d_rds"));
				applicationVector[13] = Double.parseDouble(rs.getString("d_wrs"));
				applicationVector[14] = Double.parseDouble(rs.getString("cpu_cycles"));
				applicationVector[15] = Double.parseDouble(rs.getString("Src_packs"));
				applicationVector[16] = Double.parseDouble(rs.getString("Src_avBytess"));
				applicationVector[17] = Double.parseDouble(rs.getString("Src_avPacketSize"));
				applicationVector[18] = Double.parseDouble(rs.getString("Src_avPacketss"));
				applicationVector[19] = Double.parseDouble(rs.getString("Dest_packs"));
				applicationVector[20] = Double.parseDouble(rs.getString("Dest_avBytess"));
				applicationVector[21] = Double.parseDouble(rs.getString("Dest_avPacketSize"));
				applicationVector[22] = Double.parseDouble(rs.getString("Dest_avPacketss"));
				
			}

		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		workload.setVector(applicationVector);
		return workload;
	}
	
	public ArrayList<BenchmarkWorkload> getWorkloadsFromDB(Connection connection) throws Exception {
		
		ArrayList<BenchmarkWorkload> workloads = new ArrayList<BenchmarkWorkload>(); 
		BenchmarkWorkload workload = null;
		
		PreparedStatement preparedStatement = null;
		String selectSQL = "SELECT * FROM PROFILES WHERE vm_id = ?";

		try {
			connection = DbConnection.getConnection();
			
			for (int i=0; i<Constants.BENCHMARKS_WORKLOADS.length; i++) {
				
				workload = new BenchmarkWorkload(Constants.BENCHMARKS_WORKLOADS[i]);
				double[] benchmarkVector = new double[Constants.DEFAULT_TRAINING_VECTOR_SIZE];

				preparedStatement = connection.prepareStatement(selectSQL);
				preparedStatement.setString(1, Constants.BENCHMARKS_WORKLOADS[i]);
				ResultSet rs = preparedStatement.executeQuery();
				
				while (rs.next()) {
	
					benchmarkVector[0] = Double.parseDouble(rs.getString("usr"));
					benchmarkVector[1] = Double.parseDouble(rs.getString("system"));
					benchmarkVector[2] = Double.parseDouble(rs.getString("cpu"));
					benchmarkVector[3] = Double.parseDouble(rs.getString("cpu_util"));
					benchmarkVector[4] = Double.parseDouble(rs.getString("cache_hits"));

					//FOT: added a zero value, so that vector is of the same form as in Adopter mode workload
					benchmarkVector[5] = 0.0f;
					
					benchmarkVector[6] = Double.parseDouble(rs.getString("cache_misses"));
					benchmarkVector[7] = Double.parseDouble(rs.getString("VSZ"));
					benchmarkVector[8] = Double.parseDouble(rs.getString("RSS"));
					benchmarkVector[9] = Double.parseDouble(rs.getString("MEM"));
					benchmarkVector[10] = Double.parseDouble(rs.getString("kB_rds"));
					benchmarkVector[11] = Double.parseDouble(rs.getString("kB_wrs"));
					benchmarkVector[12] = Double.parseDouble(rs.getString("d_rds"));
					benchmarkVector[13] = Double.parseDouble(rs.getString("d_wrs"));
					benchmarkVector[14] = Double.parseDouble(rs.getString("cpu_cycles"));
					benchmarkVector[15] = Double.parseDouble(rs.getString("Src_packs"));
					benchmarkVector[16] = Double.parseDouble(rs.getString("Src_avBytess"));
					benchmarkVector[17] = Double.parseDouble(rs.getString("Src_avPacketSize"));
					benchmarkVector[18] = Double.parseDouble(rs.getString("Src_avPacketss"));
					benchmarkVector[19] = Double.parseDouble(rs.getString("Dest_packs"));
					benchmarkVector[20] = Double.parseDouble(rs.getString("Dest_avBytess"));
					benchmarkVector[21] = Double.parseDouble(rs.getString("Dest_avPacketSize"));
					benchmarkVector[22] = Double.parseDouble(rs.getString("Dest_avPacketss"));
					
					workload.setVector(benchmarkVector);
					workloads.add(workload);
				}
			    
			}

			return workloads;
			
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return new ArrayList<BenchmarkWorkload>();

	}
	
}
