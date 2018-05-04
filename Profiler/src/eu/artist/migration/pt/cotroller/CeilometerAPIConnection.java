package eu.artist.migration.pt.cotroller;

import java.io.BufferedReader;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;



public class CeilometerAPIConnection {
	
	public String CeilometerURL;
	public Date startTimestamp;
	public Date endTimestamp;
	public String VM_id;
	public String token;
	
	public CeilometerAPIConnection(String URL, Date startTimestamp, Date endTimestamp, String vm_id, String ceilometer_token) {
		
		CeilometerURL=URL;
		this.startTimestamp=startTimestamp;
		this.endTimestamp=endTimestamp;
		this.VM_id=vm_id;
		this.token = ceilometer_token;
	}
	
	public double getMeterValue(String meterName) {
		
		try {
			String stringUrl = CeilometerURL+meterName;
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
			
			//some values are not related to specific VM, but rather the whole compute host!
			if ((meterName.equals("compute.node.cpu.user.percent"))|| (meterName.equals("compute.node.cpu.kernel.percent")) )
				stringUrl=stringUrl+"?q.field=resource_id&q.op=eq&q.value=cloudpcmpt_cloudpcmpt";
			else if (meterName.contains("network"))
				stringUrl=stringUrl+"?q.field=resource_metadata.instance_id&q.op=eq&q.value="+VM_id;
			else if ((meterName.contains("hardware.memory.used")) || (meterName.contains("hardware.memory.total")))
				stringUrl=stringUrl+"?q.field=resource_id&q.op=eq&q.value=10.0.16.31";  //!!compute host IP, static for now!
			else	
				stringUrl=stringUrl+"?q.field=resource_id&q.op=eq&q.value="+VM_id;
			
			stringUrl=stringUrl+"&q.field=timestamp&q.op=le&q.value="+df.format(endTimestamp).replace(" ", "T");
			stringUrl=stringUrl+"&q.field=timestamp&q.op=gt&q.value="+df.format(startTimestamp).replace(" ", "T");
		
			URL url = new URL(stringUrl);
			
			//JUST DEBUGGING
			//if (meterName.equals("cpu"))
			//	System.out.println((stringUrl));
	
		    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		    connection.setRequestMethod("GET");
		    connection.setDoInput(true);
		    connection.setDoOutput(true);
		    connection.setRequestProperty("Content-Type", "application/json");
		    connection.setRequestProperty("Accept", "application/json");

		    connection.setRequestProperty("X-Auth-Token", token);

			
		    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new RuntimeException("Failed : HTTP error code : "
					+ connection.getResponseCode()+
					". Message: "+connection.getResponseMessage());
				
			}
			
			BufferedReader br = new BufferedReader(new InputStreamReader(
					(connection.getInputStream())));

			String line;
			String output="";
			while ((line = br.readLine()) != null) 
				output=output+"\n"+line;
		        
			br.close();
			
	
	//		System.out.println("@@@Output: from time:"+df.format(startTimestamp).replace(" ", "T")+" to time: "+df.format(endTimestamp).replace(" ", "T")+" == "+output);
			JSONArray json= new JSONArray(output);
			if (json.length()==0)
				return 0.0;
			JSONObject jsonObject = (JSONObject)json.get(0);
			double value = jsonObject.getDouble("counter_volume");
	//		System.out.println("###value: "+value);
			return value;
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
		return 0.0;
	}
	
}
