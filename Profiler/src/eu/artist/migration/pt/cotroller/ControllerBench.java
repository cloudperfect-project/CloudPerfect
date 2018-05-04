/*
 * Copyright 2014 ICCS/NTUA
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Initially developed in the context of ARTIST EU project www.artist-project.eu
 *
 */

package eu.artist.migration.pt.cotroller;

import java.awt.Dimension;
import java.io.BufferedReader;
//import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.JFrame;

import eu.artist.migration.pt.executor.RESTCommandExecutor;
import eu.artist.migration.pt.executor.SecureSSHCommandExecutor;
import eu.artist.migration.pt.results.TSharkCapFiles;
import eu.artist.migration.pt.user.CompleteInputForm;

public class ControllerBench extends Controller{
	
	/*
	 * command: ssh command
	 * VMip: IP of the VM to connect to 
	 * VMpass: sudo password for the VM to connect to
	 * VMuser: user to get connected
	 * benchmark commands file: the file where benchmark commands are (absolute path)
	 * workloadCount: workload identifier for a specific benchmark
	 */
	private String command;
	private String VMip;
	private String VMpass;
	private String VMuser;
	private String VMpassForSudo;
	private String benchmarkCommandsFile;
	private int workloadCount;
	private String rootPass;
	private String benchmarkSuiteURL ;
	private String provider ;
	private String VM_type_size;
	private String VM_id ;
	private Date startTimestamp;
	private Date endTimestamp;
	private boolean providerMode=false;
	
	private String ceilometer_host;
	private String ceilometer_token;

		
	public static void main(String args[]){
		new ControllerBench();	
		}
	
	public ControllerBench(String pass, String file, boolean providerMode){
		//FOT
		this.providerMode = providerMode;
		rootPass=pass;
		workloadCount=0;
		if (providerMode)
			getProviderInfoFromFile(file);
		else {
			ph= new PidstatHandler();
			th= new TSharkHandler();
			getInfoFromUser(file);
			ph.info.setOverwrite(false);
			th.info.setOverwrite(true);
		}
		execute();
	}
	
	public ControllerBench(){
		ph= new PidstatHandler();
		th= new TSharkHandler();
		workloadCount=0;
		getInfoFromUser();
		ph.info.setOverwrite(false);
		th.info.setOverwrite(true);
		execute();		
	}
	
	private void execute(){
		
		//FOT
		List<String>  benchmarkLabels=new ArrayList<String>();
		Connection connection = null;
		
		while (true){
			command = getBenchmarkExecutionCommand();
			if (command.equals("")) break;
			
			//ph.setResultHeading(Integer.toString(workloadCount));
			//th.setResultHeading(Integer.toString(workloadCount));
			
			//FOT: check provider-mode
			if (providerMode) {
				
				System.out.println("Running provider mode....");
				
				//FOT  --get correct benchmark and workload names from list--
				int index = command.lastIndexOf(':');
				String workload = command.substring(index+1, command.length());
				String benchmark = command.substring(0, index);
				String label = benchmark+"-"+workload;
				//end of --change headings--*/
				
				RESTCommandExecutor restExecutor = new RESTCommandExecutor(benchmark, workload, benchmarkSuiteURL, provider, VM_type_size);
				restExecutor.start();
				try{
					restExecutor.join();
					
				}catch (Exception e){
					
					e.printStackTrace();
				}
				
				VM_id = restExecutor.VM_id;
				startTimestamp = restExecutor.startTimestamp;
				endTimestamp = restExecutor.endTimestamp;
				
				//!!!!FOT: Request to Ceilometer with this.VM_id, startTimestamp, endTimestamp
				System.out.println(VM_id +startTimestamp +endTimestamp);
				
				try {
				  	int i = workspace.indexOf("profiler/");
				  	String dbConfFile = workspace.substring(0, i)+"classifier/conf.ini";

					//!!FOT: Ceilometer values retrieval

					  
				  	CeilometerAPIConnection ceilometer = new CeilometerAPIConnection("http://"+ceilometer_host+":8777/v2/meters/", startTimestamp, endTimestamp, VM_id, ceilometer_token);
				  	
				  	
					//!!FOT: Ceilometer values retrieval - correct meter names!!
				  	double usr = ceilometer.getMeterValue("compute.node.cpu.user.percent");
				  	double system = ceilometer.getMeterValue("compute.node.cpu.kernel.percent");
				  	double cpu = ceilometer.getMeterValue("cpu");
				  	double cpu_util = ceilometer.getMeterValue("cpu_util");
				  	
				  	//keep these???
				  	double cache_hits = ceilometer.getMeterValue("disk.allocation");
				  	double cache_misses = ceilometer.getMeterValue("memory");
				  	
				  	
				  	double VSZ = ceilometer.getMeterValue("memory.usage");
				  	double RSS = ceilometer.getMeterValue("memory.resident");
				  	
				  	double MEM;
				  	double MEM_used = ceilometer.getMeterValue("hardware.memory.used");
				  	double MEM_total = ceilometer.getMeterValue("hardware.memory.total");
				  	if (MEM_total>0.0)
				  		MEM = (MEM_used/MEM_total);
				  	else
				  		MEM=0.0;
				  	
				  	double kB_rds = ceilometer.getMeterValue("disk.read.bytes.rate");
				  	double kB_wrs = ceilometer.getMeterValue("disk.write.bytes.rate");;
				  	double d_rds = ceilometer.getMeterValue("disk.read.requests.rate");
				  	double d_wrs = ceilometer.getMeterValue("disk.write.requests.rate");
				  	
				  	//keep this???
				  	double cpu_cycles = ceilometer.getMeterValue("cpu.delta");
				  	
				  	double Src_packs = ceilometer.getMeterValue("network.outgoing.packets");
				  	double Src_avBytess = ceilometer.getMeterValue("network.outgoing.bytes.rate");
				  	double Src_avPacketSize;
				  	if (Src_packs>0.0)
				  		Src_avPacketSize= ceilometer.getMeterValue("network.outgoing.bytes")/Src_packs;
				  	else
				  		Src_avPacketSize=0.0;

				  	double Src_avPacketss  = ceilometer.getMeterValue("network.outgoing.packets.rate");
				  	double Dest_packs = ceilometer.getMeterValue("network.incoming.packets");
				  	double Dest_avBytess  = ceilometer.getMeterValue("network.incoming.bytes.rate");
				  	double Dest_avPacketSize;
				  	if (Dest_packs>0.0)
				  		Dest_avPacketSize= ceilometer.getMeterValue("network.outgoing.bytes")/Dest_packs;
				  	else
				  		Dest_avPacketSize=0.0;

				  	double Dest_avPacketss  = ceilometer.getMeterValue("network.incoming.packets.rate");
				  	
				  	System.out.println("Value retrieval ENDED");
				  	
				  	connection = DbConnection.getConnection(dbConfFile);
				  	String query = "REPLACE into PROFILES (vm_id, usr, system, cpu, cpu_util, cache_hits, cache_misses, VSZ, RSS, MEM, kB_rds, kB_wrs, d_rds, d_wrs, cpu_cycles, Src_packs, Src_avBytess, Src_avPacketSize, Src_avPacketss, Dest_packs, Dest_avBytess, Dest_avPacketSize, Dest_avPacketss)"
			    	        + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
				  	PreparedStatement preparedStmt = connection.prepareStatement(query);

		    	    preparedStmt.setString (1, label);
		    	    preparedStmt.setString (2, ""+usr);
		    	    preparedStmt.setString (3, ""+system);
		    	    preparedStmt.setString (4, ""+cpu);
		    	    preparedStmt.setString (5, ""+cpu_util);
		    	    preparedStmt.setString (6, ""+cache_hits);
		    	    preparedStmt.setString (7, ""+cache_misses);
		    	    preparedStmt.setString (8, ""+VSZ);
		    	    preparedStmt.setString (9, ""+RSS);
		    	    preparedStmt.setString (10, ""+MEM);
		    	    preparedStmt.setString (11, ""+kB_rds);
		    	    preparedStmt.setString (12, ""+kB_wrs);
		    	    preparedStmt.setString (13, ""+d_rds);
		    	    preparedStmt.setString (14, ""+d_wrs);
		    	    preparedStmt.setString (15, ""+cpu_cycles);
		    	    preparedStmt.setString (16, ""+Src_packs);
		    	    preparedStmt.setString (17, ""+Src_avBytess);
		    	    preparedStmt.setString (18, ""+Src_avPacketSize);
		    	    preparedStmt.setString (19, ""+Src_avPacketss);
		    	    preparedStmt.setString (20, ""+Dest_packs);
		    	    preparedStmt.setString (21, ""+Dest_avBytess);
		    	    preparedStmt.setString (22, ""+Dest_avPacketSize);
		    	    preparedStmt.setString (23, ""+Dest_avPacketss);
				    preparedStmt.execute();

				}
				catch (Exception ex) {
					ex.printStackTrace();
				}

				
			}
			else {
				
				//FOT  --change headings--
				int index1 = command.lastIndexOf('/');
				int index2 = command.lastIndexOf('.');
				String workload = command.substring(index1+1, index2);
				String label = "-"+workload;
				String prelabel = command.substring(0,index1);
				index1 = prelabel.lastIndexOf('/');
				index2 = prelabel.lastIndexOf('_');
				String benchmark = prelabel.substring(index1+1, index2);
				label = benchmark+label;
				//end of --change headings--*/
				
				ph.setResultHeading(label);
				th.setResultHeading(label);

			
				//FOT: Need to init MySQL + run Load scripts for all YCSB workloads first!!
				if (command.contains("YCSB")) {
					index1 = command.lastIndexOf('.');
					String initYCSBworkloadCommand = command.substring(0,index1)+"-init.sh";
					System.out.println("running initialization and load for YCSB benchmark workload: "+initYCSBworkloadCommand);
					SecureSSHCommandExecutor benchExecInit =new SecureSSHCommandExecutor(VMuser, VMip, VMpass, VMpassForSudo, initYCSBworkloadCommand);
					benchExecInit.start();
					try{
						benchExecInit.join();
					}catch (Exception e){
						e.printStackTrace();
					}
				}
				
				System.out.println("executing command: "+command);
				SecureSSHCommandExecutor benchExec =new SecureSSHCommandExecutor(VMuser, VMip, VMpass, VMpassForSudo, command);
				benchExec.start();
				startPidstat();
				startTShark();
				try{
					benchExec.join();
					
				}catch (Exception e){
					
					e.printStackTrace();
				}
				stopPidstat();
				stopTShark();
							
				//FOT
				benchmarkLabels.add(workloadCount, label);
			}
		
			workloadCount ++;
		}
		System.out.println("No more workloads to execute");

		if (providerMode) { 
			try {
				connection.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} 
		else{
			//FOT: If Adopter mode-> create training file for classifier
			System.out.println("Create training file for classifier....");
			
			FileReader fr = null;
	        FileWriter fw = null;
	        try {
	            fr = new FileReader(workspace+"Pidstat.txt");
	            fw = new FileWriter(workspace+"BenchmarkProfiles-trainingFile.txt");
	            int c = fr.read();
	            while(c!=-1) {
	                fw.write(c);
	                c = fr.read();
	            }
	            fr.close();
	            fw.close();
	        } catch(IOException e) {
	            e.printStackTrace();
	        }
	        //end of - -training file creation
	
			
			///new TSharkCapFiles(th.info.getOutFileNameForSource(), th.info.getOutFileNameForDest(), workloadCount, workspace, th.info.getPass());
	        //FOT
	        new TSharkCapFiles(th.info.getOutFileNameForSource(), th.info.getOutFileNameForDest(), benchmarkLabels, workspace, th.info.getPass());
	        
			/*for (int i=0; i<workloadCount;  i++){
				removeSystemCommandExecutor commandExecutorSrc=new SystemCommandExecutor(Arrays.asList("sudo", "-S", "rm", th.info.getWorkspace()+"tmp/"+th.info.getOutFileNameForSource()+Integer.toString(i)+".cap"), th.info.getPass());
				commandExecutorSrc.start();
				SystemCommandExecutor commandExecutorDest= new SystemCommandExecutor(Arrays.asList("sudo", "-S", "rm", th.info.getWorkspace()+"tmp/"+th.info.getOutFileNameForDest()+Integer.toString(i)+".cap"), th.info.getPass());
				commandExecutorDest.start();
	
			}*/
		}
	}
	

	
	private String getBenchmarkExecutionCommand(){
		String line="";	
		try{
			BufferedReader in = new BufferedReader(new FileReader(benchmarkCommandsFile));
			int i;
			for (i=0; i<=workloadCount; i++){
				line = in.readLine();
			}
			in.close(); 
			if (line==null) line="";
				
		}catch (Exception e){
			e.printStackTrace();
		}
			return line;
	}
	
	
	private void getInfoFromUser(){
		CompleteInputForm cf = new CompleteInputForm();
		cf.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		cf.setSize( 380, 700 ); // set frame size
		cf.setMinimumSize(new Dimension(380, 700));
		cf.setMaximumSize(new Dimension(380, 700));
		cf.setVisible( true );
		while(!cf.gotTheResults()){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		ArrayList<String> valuesP = cf.getPidstatPanel().getValues();
		ArrayList<String> valuesT = cf.getTsharkPanel().getValues();
		ArrayList<String> valuesB = cf.getBenchmarkPanel().getValues();
		
		workspace= cf.getWorkspace();
		if (!workspace.substring(workspace.length()-1).equals("/")) workspace=workspace+"/";
		ph.setInfo(valuesP.get(0), valuesP.get(1), workspace, false);
		th.setInfo(workspace,valuesT.get(0), valuesT.get(1), valuesT.get(2), "tsharkSrc", "tsharkDst", true);
		
		VMip=valuesB.get(0);
		VMuser = valuesB.get(1);
		benchmarkCommandsFile = valuesB.get(2);
		VMpass=valuesB.get(3);
		VMpassForSudo=valuesB.get(4);
		
		cf.dispose();
	}
	
	private void getInfoFromUser(String file){
		try{
			BufferedReader bufferedReader;
			bufferedReader = new BufferedReader(new FileReader(file));
			
			String line;
			ArrayList<String> valuesP = new ArrayList<String>();
			ArrayList<String> valuesT = new ArrayList<String>();
			ArrayList<String> valuesB = new ArrayList<String>();
			
			if ((line=bufferedReader.readLine())!=null){
				workspace=line;
				if (!workspace.substring(workspace.length()-1).equals("/")) workspace=workspace+"/";
			}
			else {
				bufferedReader.close();
				throw new Exception("Wrong number of arguments in file");
			}
			
			if ((line=bufferedReader.readLine())!=null){
				valuesP=new ArrayList<String>(Arrays.asList(line.split(",")));
				int i=valuesP.size();
				if (i!=1){
					bufferedReader.close();
					throw new Exception("Wrong number of arguments for Pidstat");
				}
			}else {
				bufferedReader.close();
				throw new Exception("Wrong number of arguments in file");
			}
			
			if ((line=bufferedReader.readLine())!=null){
				valuesT=new ArrayList<String>(Arrays.asList(line.split(",")));
				int i=valuesT.size();
				if (i!=2){
					bufferedReader.close();
					throw new Exception("Wrong number of arguments for TShark");
				}
			}else{
				bufferedReader.close();
				throw new Exception("Wrong number of arguments in file");
			}
			
			if ((line=bufferedReader.readLine())!=null){
				valuesB=new ArrayList<String>(Arrays.asList(line.split(",")));
				int i=valuesB.size();
				if (i!=5){
					bufferedReader.close();
					throw new Exception("Wrong number of arguments for Benchmarks");
				}
			}else{
				bufferedReader.close();
				throw new Exception("Wrong number of arguments in file");
			}
			
			ph.setInfo(valuesP.get(0), rootPass, workspace, false);
			th.setInfo(workspace,valuesT.get(0), valuesT.get(1), rootPass, "tsharkSrc", "tsharkDst", true);
			VMip=valuesB.get(0);
			VMuser = valuesB.get(1);
			benchmarkCommandsFile = valuesB.get(2);
			VMpass=valuesB.get(3);
			VMpassForSudo=valuesB.get(4);
			bufferedReader.close();
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}catch(Exception e){
			System.out.println(e);
			System.exit(0);
		}
	}
	
	private void getProviderInfoFromFile (String file){
		try{
			BufferedReader bufferedReader;
			bufferedReader = new BufferedReader(new FileReader(file));
			
			String line;
			
			
			ArrayList<String> valuesB = new ArrayList<String>();
			ArrayList<String> valuesC = new ArrayList<String>();
			
			if ((line=bufferedReader.readLine())!=null){
				workspace=line;
				if (!workspace.substring(workspace.length()-1).equals("/")) workspace=workspace+"/";
			}
			else {
				bufferedReader.close();
				throw new Exception("Wrong number of arguments in file");
			}
			
			if ((line=bufferedReader.readLine())!=null)
				benchmarkSuiteURL = line;
			else {
				bufferedReader.close();
				throw new Exception("Wrong number of arguments in file");
			}
			
			if ((line=bufferedReader.readLine())!=null){
				valuesB=new ArrayList<String>(Arrays.asList(line.split(",")));
				int i=valuesB.size();
				if (i!=3){
					bufferedReader.close();
					throw new Exception("Wrong number of arguments for Benchmarks creation through bench suite!");
				}
				else {
					provider=valuesB.get(0);
					VM_type_size = valuesB.get(1);
					benchmarkCommandsFile = valuesB.get(2);
				}
					
			}else{
				bufferedReader.close();
				throw new Exception("Wrong number of arguments in file");
			}
			
			if ((line=bufferedReader.readLine())!=null){
				valuesC=new ArrayList<String>(Arrays.asList(line.split(",")));
				int i=valuesC.size();
				if (i!=2){
					bufferedReader.close();
					throw new Exception("Wrong number of arguments for TShark");
				}
				
				ceilometer_host = valuesC.get(0);
				ceilometer_token = valuesC.get(1);
			}else{
				bufferedReader.close();
				throw new Exception("Wrong number of arguments in file");
			}
			
			
			bufferedReader.close();
			
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}catch(Exception e){
			System.out.println(e);
			System.exit(0);
		}
	}

}
