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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
//import java.text.DecimalFormat;
//import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.JFrame;

//import eu.artist.migration.ct.common.ProfilerConfigurationReader;
import eu.artist.migration.pt.cotroller.DbConnection;
import eu.artist.migration.pt.results.TSharkCapFiles;
import eu.artist.migration.pt.user.*;

public class ControllerUserApp extends Controller{
	
	private StartStopFrame startStop; 
	private String rootPass;
	private int runDuration;
	private String ceilometer_host;
	private String ceilometer_token;
	
	//Provider mode parameters
	private String VM_id;

	
	public static void main(String args[]){
		new ControllerUserApp();
	}
	
	public ControllerUserApp(){
		ph = new PidstatHandler();
		th = new TSharkHandler();

		getInfoFromUser();
		//FOT: changed overwrite to TRUE!
		ph.info.setOverwrite(true);
		th.info.setOverwrite(true);
		startStop = new StartStopFrame(this);
		startStop.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		startStop.setSize( 350, 250 ); // set frame size
		startStop.setVisible(true ); 
	}
	
	public ControllerUserApp(String pass, String file, boolean providerMode){
		
		if (providerMode) {
			getInfoFromProviderFile(file);
			Calendar gcal = new GregorianCalendar();
			gcal.add(Calendar.HOUR, -2);
			Date startTimestamp = gcal.getTime();

			
			try        
			{
			    Thread.sleep(runDuration*1000);
			} 
			catch(InterruptedException ex) 
			{
			    Thread.currentThread().interrupt();
			}
			
			gcal = new GregorianCalendar();
			gcal.add(Calendar.HOUR, -2);
			Date endTimestamp = gcal.getTime();

			try {		
				
			//!!!FOT: delete these 3 lines!!
			//SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
			//startTimestamp  = dateFormat.parse("2018-01-26 13:00:00.50");
			//endTimestamp  = dateFormat.parse("2018-01-26 13:05:00.52");

			  	int i = workspace.indexOf("profiler/");
			  	String dbConfFile = workspace.substring(0, i)+"classifier/conf.ini";

			  
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
			  	Connection connection = DbConnection.getConnection(dbConfFile);
			    String query = "REPLACE into PROFILES (vm_id, usr, system, cpu, cpu_util, cache_hits, cache_misses, VSZ, RSS, MEM, kB_rds, kB_wrs, d_rds, d_wrs, cpu_cycles, Src_packs, Src_avBytess, Src_avPacketSize, Src_avPacketss, Dest_packs, Dest_avBytess, Dest_avPacketSize, Dest_avPacketss)"
			    	        + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			    PreparedStatement preparedStmt = connection.prepareStatement(query);

	    	    preparedStmt.setString (1, VM_id);
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
			    connection.close();
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		else {
			rootPass=pass;
			ph = new PidstatHandler();
			th = new TSharkHandler();
			getInfoFromUser(file);
			//FOT: changed overwrite to TRUE!
			ph.info.setOverwrite(true);
			
			th.info.setOverwrite(true);
			
			
			//FOT: commented to start/stop without GUI
			//startStop = new StartStopFrame(this);
			//startStop.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
			//startStop.setSize( 350, 250 ); // set frame size
			//startStop.setVisible(true );
			start();
			try        
			{
			    Thread.sleep(runDuration*1000);
			} 
			catch(InterruptedException ex) 
			{
			    Thread.currentThread().interrupt();
			}
			stop();
			//FOT: end of addition
		}	
	}
	
	private void getInfoFromUser(){
		MonitorInputForm cf = new MonitorInputForm();
		cf.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		cf.setSize( 400, 450 ); // set frame size
		cf.setMinimumSize(new Dimension(400, 450));
		cf.setMaximumSize(new Dimension(400, 450));
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
		workspace=cf.getWorkspace();
		if (!workspace.substring(workspace.length()-1).equals("/")) workspace=workspace+"/";
		ph.setInfo(valuesP.get(0), valuesP.get(1), workspace, false);
		th.setInfo(workspace, valuesT.get(0), valuesT.get(1), valuesT.get(2), "tsharkSrc", "tsharkDst", true);
		cf.dispose();
	}
	
	private void getInfoFromUser(String file){
		try{
			BufferedReader bufferedReader;
			bufferedReader = new BufferedReader(new FileReader(file));
			
			String line;
			ArrayList<String> valuesP = new ArrayList<String>();
			ArrayList<String> valuesT = new ArrayList<String>();
			
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
				if (i!=2){
					bufferedReader.close();
					throw new Exception("Wrong number of arguments for Pidstat");
				}
				
				runDuration=Integer.parseInt(valuesP.get(1));
				valuesP.remove(1);
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
			
			//FOT changed isOverwrite input parameter to true from false
			ph.setInfo(valuesP.get(0), rootPass, workspace, true);
						
			th.setInfo(workspace,valuesT.get(0), valuesT.get(1), rootPass, "tsharkSrc", "tsharkDst", true);
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
	
	private void getInfoFromProviderFile(String file){
		try{
			BufferedReader bufferedReader;
			bufferedReader = new BufferedReader(new FileReader(file));
			
			String line;
			ArrayList<String> valuesP = new ArrayList<String>();
			ArrayList<String> valuesT = new ArrayList<String>();
			
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
				if (i!=2){
					bufferedReader.close();
					throw new Exception("Wrong number of arguments for VM id!");
				}
				VM_id=valuesP.get(0);
				runDuration=Integer.parseInt(valuesP.get(1));
				
				
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
				
				ceilometer_host = valuesT.get(0);
				ceilometer_token = valuesT.get(1);
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
	public void processCapFiles(){
		System.out.println("----WRITE cAP FILES!!");
		new TSharkCapFiles(th.info.getOutFileNameForSource(), th.info.getOutFileNameForDest(), workspace, th.info.getPass());
	//	SystemCommandExecutor commandExecutorSrc=new SystemCommandExecutor(Arrays.asList("sudo", "-S", "rm", th.info.getWorkspace()+"tmp/"+th.info.getOutFileNameForSource()+".cap"), th.info.getPass());
	//	commandExecutorSrc.start();
	//	SystemCommandExecutor commandExecutorDest= new SystemCommandExecutor(Arrays.asList("sudo", "-S", "rm", th.info.getWorkspace()+"tmp/"+th.info.getOutFileNameForDest()+".cap"), th.info.getPass());
	//	commandExecutorDest.start();

	}
	
	//FOT addition: command line Start/Stop
	public void start(){
		startPidstat();
		startTShark();
	}
	public void stop(){
		stopPidstat();
		stopTShark();
		processCapFiles();
	}
	//end of Addition
	
}
