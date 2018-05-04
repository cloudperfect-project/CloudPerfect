package eu.artist.migration.pt.executor;

import java.io.BufferedReader;

import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.json.JSONArray;
import org.json.JSONObject;



public class RESTCommandExecutor extends Thread
{
	  private String benchmark; 
	  private String workload;
	  private String benchmarkSuiteURL;
	  private String provider;
	  private String VM_type_size;
	  public String VM_id;
	  public Date startTimestamp;
	  public Date endTimestamp;
	  

	  
	  
	  public RESTCommandExecutor (String benchmark, String workload, String benchmarkSuiteURL, String provider, String VM_type_size) {
		  this.benchmark=benchmark;
		  this.workload= workload;
		  this.benchmarkSuiteURL = benchmarkSuiteURL;
		  this.provider = provider;
		  this.VM_type_size=VM_type_size;
		  
	  }
	  
	  public void run(){
		//Calling benchmark suite to create VM, install benchmark and execute workload!
		String SessionId = createSession();
		String ExecutionId = createExecution(SessionId);
		prepareExecution(ExecutionId);
		this.VM_id = getVMid(ExecutionId);
		runBenchmark(ExecutionId);	
		deleteSession(SessionId);

	  }
	  
	  private String getVMid (String ExecutionId) {
			  try {
				  	String vm_Id=null;

				    URL url = new URL(this.benchmarkSuiteURL+"executions/"+ExecutionId);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					
					conn.setDoOutput(true);
					conn.setRequestMethod("GET");
					conn.setRequestProperty("Content-Type", "application/json");

					if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
						throw new RuntimeException("Failed : HTTP error code : "
							+ conn.getResponseCode()+
							". Message: "+conn.getResponseMessage());
						
					}

					BufferedReader br = new BufferedReader(new InputStreamReader(
							(conn.getInputStream())));

					String line;
					String output="";
					while ((line = br.readLine()) != null) 
						output=output+"\n"+line;
					JSONObject obj = new JSONObject(output);
					JSONObject exec_env = obj.getJSONObject("exec_env");
					JSONArray vms = exec_env.getJSONArray("vms");
					if (vms.length()>0) {
						JSONObject vm_Entry = (JSONObject) vms.get(0);
						vm_Id =	vm_Entry.getString("id");
					}	
					br.close();
					conn.disconnect();
					return vm_Id;

				 } catch (Exception e) {

					e.printStackTrace();

				 }
			     return "ERROR!!!";
		}

	  
		public void deleteSession (String SessionId) {
			  try {
				  	System.out.println("Sleeping for 5sec and then terminate Instance");
				  	Thread.sleep(5000);
				  
				    URL url = new URL(this.benchmarkSuiteURL+"sessions/"+SessionId);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					
					conn.setDoOutput(true);
					conn.setRequestMethod("DELETE");
					conn.setRequestProperty("Content-Type", "application/json");


					OutputStream os = conn.getOutputStream();
					os.flush();
					
					if (conn.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT) {
						throw new RuntimeException("Failed : HTTP error code : "
							+ conn.getResponseCode()+
							". Message: "+conn.getResponseMessage());
						
					}
					
					System.out.println("CLOSED CONNECTION - Terminated Instance");
					conn.disconnect();
					

				 } catch (Exception e) {

					e.printStackTrace();

				 }
			     
		}
		
		public String prepareExecution (String ExecutionId) {
			  try {

				    URL url = new URL(this.benchmarkSuiteURL+"executions/"+ExecutionId+"/prepare");
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					
					conn.setDoOutput(true);
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Content-Type", "application/json");


					OutputStream os = conn.getOutputStream();
					os.flush();
					
					if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
						throw new RuntimeException("Failed : HTTP error code : "
							+ conn.getResponseCode()+
							". Message: "+conn.getResponseMessage());
						
					}
					
					BufferedReader br = new BufferedReader(new InputStreamReader(
							(conn.getInputStream())));

					String line;
					String output="";
					while ((line = br.readLine()) != null) 
						output=output+"\n"+line;
					System.out.println("prepare output: "+ output);
//					JSONObject obj = new JSONObject(output);
//					String executionId = obj.getString("id");

//					System.out.println("ExecutionId: "+ executionId);
					br.close();
					conn.disconnect();
					return output;

				 } catch (Exception e) {

					e.printStackTrace();

				 }
			     return "ERROR!!!";
		}

		public String runBenchmark (String ExecutionId) {
			  try {

				    URL url = new URL(this.benchmarkSuiteURL+"executions/"+ExecutionId+"/run");
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					
					conn.setDoOutput(true);
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Content-Type", "application/json");


					OutputStream os = conn.getOutputStream();
					os.flush();
					
					if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
						throw new RuntimeException("Failed : HTTP error code : "
							+ conn.getResponseCode()+
							". Message: "+conn.getResponseMessage());
						
					}
					BufferedReader br = new BufferedReader(new InputStreamReader(
							(conn.getInputStream())));

					String line;
					String output="";
					while ((line = br.readLine()) != null) 
						output=output+"\n"+line;
					System.out.println("run output: "+ output);

					JSONObject obj = new JSONObject(output);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
					startTimestamp = sdf.parse(obj.getString("started"));
					String duration = obj.getString("duration");
					Calendar gcal = new GregorianCalendar();
				    gcal.setTime(startTimestamp);
				    
				    //Correct hour for COSMOTE's calendar
				    gcal.add(Calendar.HOUR, -2);
				    startTimestamp = gcal.getTime();
				    
				    gcal.add(Calendar.SECOND, (int) Math.round(Double.parseDouble(duration)));
				    endTimestamp = gcal.getTime();

					System.out.println("Executed on VM with ID="+VM_id+" Start time="+startTimestamp+" end time="+endTimestamp);


					br.close();
					conn.disconnect();
					return output;

				 } catch (Exception e) {

					e.printStackTrace();

				 }
			     return "ERROR!!!";
		}
		public String createExecution (String SessionId) {
			  try {

				    URL url = new URL(this.benchmarkSuiteURL+"sessions/"+SessionId+"/executions/");
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					
					conn.setDoOutput(true);
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Content-Type", "application/json");
					
					

					String input = "{\"tool\":\""+benchmark+"\",\"workload\":\""+workload+"\"}";

					System.out.println("creating execution for: "+benchmark+" "+workload +" .\n json:\n"+input);
					
					OutputStream os = conn.getOutputStream();
					os.write(input.getBytes());
					os.flush();

					if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
						throw new RuntimeException("Failed : HTTP error code : "
							+ conn.getResponseCode()+
							". Message: "+conn.getResponseMessage());
						
					}

					BufferedReader br = new BufferedReader(new InputStreamReader(
							(conn.getInputStream())));

					String line;
					String output="";
					while ((line = br.readLine()) != null) 
						output=output+"\n"+line;
					JSONObject obj = new JSONObject(output);
					String executionId = obj.getString("id");

					System.out.println("ExecutionId: "+ executionId);
					br.close();
					conn.disconnect();
					return executionId;

				 } catch (Exception e) {

					e.printStackTrace();

				 }
			     return "ERROR!!!";
		}

		public String createSession () {
			  try {

				    URL url = new URL(this.benchmarkSuiteURL+"sessions/");
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					
					conn.setDoOutput(true);
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Content-Type", "application/json");

					String input = "{\"provider\":\""+provider+"\",\"service\":\""+VM_type_size+"\"}";

					OutputStream os = conn.getOutputStream();
					os.write(input.getBytes());
					os.flush();

					if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
						throw new RuntimeException("Failed : HTTP error code : "
							+ conn.getResponseCode());
					}

					BufferedReader br = new BufferedReader(new InputStreamReader(
							(conn.getInputStream())));

					String line;
					String output="";
					while ((line = br.readLine()) != null) 
						output=output+"\n"+line;
					
					JSONObject obj = new JSONObject(output);
					String sessionId = obj.getString("id");

					System.out.println("Output SessionId: "+ sessionId);
					br.close();
					conn.disconnect();
					return sessionId;

				 } catch (Exception e) {

					e.printStackTrace();

				 }
			  	return "ERROR!!!";
		}

}
