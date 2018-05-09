package eu.artist.cloud.auditors;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jclouds.compute.domain.NodeMetadata;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class OpenstackFilabAuditor {
	//get list of servers-GENERIC
	public  void checkApplicabilityConditions(String provider,String user,String apiKey, String databaseIP, String DBuser, String DBkey,String libUser) throws Exception{
	
		
	while (2>1){
	System.out.println("--------------------------------------------------------------");	
	System.out.println("Getting server list and details...");
	
	try {
		//-------------------------------------
    	JSONObject total = new JSONObject();
    	JSONObject auth = new JSONObject();
    	JSONObject tmp = new JSONObject();
    	JSONObject project_tmp = new JSONObject();
    	JSONObject project = new JSONObject();
    	JSONObject password = new JSONObject();
    	JSONObject identity = new JSONObject();
    	JSONObject userr = new JSONObject();
    	//--------------------------------------
    	
    	ArrayList <String> loggedData= new ArrayList();
    	CloseableHttpClient httpclient = HttpClients.createDefault();
    	HttpPost httppost = new HttpPost("http://cloud.lab.fiware.org:4730/v3/auth/tokens");
    
    	String[] passwords=new String[1];
    	passwords[0]="password";
    	String project_id="ae9fb293677e4d68b68cda6ed6db7a12";
    	String domain="default";
    	
    	
    	
    	//Create the Json used for authentication.
    	//-----------------------------------------------------------------
    	
    	userr.put("domain",new JSONObject().put("id",domain));
    	userr.put("name", user);
    	userr.put("password",apiKey);
    	
    	tmp.put("user", userr);
    	
    	project_tmp.put("id",project_id);
    	project.put("project", project_tmp);
    	
    	identity.put("methods",passwords);
    	identity.put("password", tmp);
    	
    	total.put("identity", identity);
    	//total.put("scope", project);
    	
    	auth.put("auth", total);
    	
    	//-----------------------------------------------------------------
    	
    	//System.out.println(auth);
    	httppost.addHeader("Content-Type","application/json");
    	httppost.setEntity(new StringEntity(auth.toString()));
    	//total.accumulate("scope", project);
    	
    	CloseableHttpResponse res = httpclient.execute(httppost);
    	
    	//Retrieve X-Auth-Token  from header response
    	String Token_ID = res.getFirstHeader("X-Subject-Token").getValue();
    	ResponseHandler<String> handler = new BasicResponseHandler();
    	String body = handler.handleResponse(res);
    	JSONObject myObject=new JSONObject(body);
    	//------------------------------------------------------------------
    	
    	
    	
    	//Parse response to get endpoint url(final_url).
    	//System.out.println("-------------------------->"+Token_ID);
    	myObject=myObject.getJSONObject("token");
/*   	    	JSONArray catalog=myObject.getJSONArray("catalog");
    	
    	
    	String final_url="";
    	for (int i = 0; i < catalog.length(); i++) {
    		JSONObject IterObject =  catalog.getJSONObject(i);
    		if (IterObject.getString("name").equals("nova")) {
    			
    			JSONObject obj=	(JSONObject) IterObject.getJSONArray("endpoints").get(1);
    			String url=obj.getString("url");
    			System.out.println(url);
    			String[] url_parts=url.split(":");
    			final_url=url_parts[0]+"://"+"10.0.16.11:"+url_parts[2];
    			
    		}
    		
    	}
    	
    	System.out.println("RRRRRRRRRRRRRR->"+final_url+"/servers/detail");
    	//--------------------------------------------------------------------
*/   	    	
	    JSONArray catalog=myObject.getJSONArray("catalog");
	    JSONArray endpoints;
        String final_url="";
    	for (int k = 0; k < catalog.length(); k++) {
    		JSONObject IterObject =  catalog.getJSONObject(k);
    		if (IterObject.getString("type").equals("compute")) {
    			endpoints=IterObject.getJSONArray("endpoints");
    			for(int a=0; a < endpoints.length(); a++) {
    				JSONObject endpoint = endpoints.getJSONObject(a);
    				if(endpoint.get("interface").equals("public")) {
    					final_url = endpoint.getString("url");
    				//	System.out.println(final_url);
    				}
    			}
    		
    		}
    		
    	}
    	
    	HttpGet httppost1 = new HttpGet(final_url+"/servers/detail");
    	httppost1.addHeader("X-Auth-Token", Token_ID);
    	CloseableHttpClient httpclient1 = HttpClients.createDefault();
    	ResponseHandler<String> handler1 = new BasicResponseHandler();
    	CloseableHttpResponse res1 = httpclient1.execute(httppost1);
    	String body1 = handler1.handleResponse(res1);
    	//System.out.println("HERE COMES THE BODY -----<>-----"+body1);
    	JSONObject myObject1=new JSONObject(body1);
    	//System.out.println(body1);
    	JSONArray arr=myObject1.getJSONArray("servers");
    	JSONObject addresses;
    	JSONObject server=null;
    	Set<String> keys;
    	JSONObject addr;
    	String network;
    	JSONArray ips;
    	String ip;
    	
//-------------SCAN SERVERS------------------------------------------------------------    
    	
    	boolean Serversrunning=false;
    	boolean available=false;
    	boolean  has_floating=false;
    	
    	for (int i = 0; i < arr.length(); i++) {
    		 
    		server= arr.getJSONObject(i);
    		
    		if (server.get("status").equals("ACTIVE")) {
    			
    			Serversrunning=true;
    			if(checkReachability(server)) {
    			    	available=true;	    
    			}
    			
    			if(checkFloatingip(server)) {
    				    has_floating=true;
    			}
    			
    		}
    	}
    	
    	if(Serversrunning && has_floating) {
    		System.out.println(Serversrunning);
    		System.out.println(has_floating);
    		loggedData.add(addReachabilityInfo(available,server,libUser,provider));
			AbstractedAvailabilityLogger Logger=new AbstractedAvailabilityLogger();
			
    	}
    	else {
    		System.out.println("There are not Active nodes in this Account");
    	}
    	
    	
    	
    	if(arr.length()==0) {
    		System.out.println("There are no servers in this Account");
    	}
    	
    	
//-------------------------------------------------------------------------------------
    	
    	
    	
    	//Set<String> keys=server2.keySet();
    	//String network=(String) keys.toArray()[0];
       
    	
    	
    	//------------SHOULD TAKE ONLY THOSE WITH :"status":"ACTIVE"------------------------
    	
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		System.out.println("Check your connectivity....");
	}
}
	}
	
	
	
	
	public boolean checkFloatingip(JSONObject thisserver) {
		 
		  Boolean found_floating=false;
          JSONObject addr;
		  JSONObject addresses= thisserver.getJSONObject("addresses");
		  Set<String> keys=addresses.keySet();
		  String network=(String) keys.toArray()[0];
		  JSONArray ips=addresses.getJSONArray(network);
		  for (int j = 0; j < ips.length(); j++) {
	    		addr = ips.getJSONObject(j);
	    	    if(addr.getString("OS-EXT-IPS:type").equals("floating")) {
	 
	    			 found_floating=true;
	    			 break;
	    	    }
	      }
		  return found_floating;
	}
	
	
	
	public boolean checkReachability(JSONObject thisserver){

		//for needed
		  boolean isReachable=false;
		  String ip="";
		  Boolean found_floating=false;
          JSONObject addr;
		  JSONObject addresses= thisserver.getJSONObject("addresses");
		  Set<String> keys=addresses.keySet();
		  String network=(String) keys.toArray()[0];
		  JSONArray ips=addresses.getJSONArray(network);
		  for (int j = 0; j < ips.length(); j++) {
	    		addr = ips.getJSONObject(j);
	    	    if(addr.getString("OS-EXT-IPS:type").equals("floating")) {
	    			 ip = (String) addr.get("addr");
	    			 found_floating=true;
	    		}
	      }
		  if(!found_floating) {
			  System.out.println("Can not access VM externally");
			  //isReachable=true; //What do we want here?
		  }
		  else {
			String IPAddress =  ip;

			//This can be abstracted more in a generic DecideReachability class
			PingReachabilityRuler thisPinger=new PingReachabilityRuler();
			//Necessary check for generic connectivity against 8.8.8.8 (Google DNS)
			//however we must cater for the case the VM is reachable but Google DNS is not
			PingReachabilityRuler generalConnectivityPinger=new PingReachabilityRuler();
			//needs to be in separate expressions since otherwise the logical calculation
			//skips the 8.8.8.8 comparison if thisReachable is unreachable (no need to calculate it based on the
			//expression since one of the two AND operators is already 0) and repings the IPAddress
			boolean thisReachable=thisPinger.decideReachability(IPAddress);
			boolean generalReachable=generalConnectivityPinger.decideReachability("8.8.8.8");
			isReachable=(((thisReachable)&&(generalReachable))||(thisReachable));
		  }
		
		/*
		try {
			System.in.read();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		return isReachable;

	}
	public String addReachabilityInfo(boolean isReachable,JSONObject thismetadata,String libuser,String provider ){
		String loggedRecord;
		//current implementation adds the reachability field and probably keeps the info of the last reachble node
		//this does not mean that only this node is reachable, we are interested only in the template ID actually
		//Suggest to remove all irrelevant info in the next versions (will also reduce required disk space for the DB
		if (isReachable){
			//add field REACHABLE
			//we can either change one existing field or add another for
			//reachability
			//needs coordination with other operations like availability calculation
			System.out.println("REACHABLE");
			thismetadata.put("reachability","REACHABLE");
			thismetadata.put("User-Id",libuser);
			thismetadata.put("Provider",provider);
			loggedRecord=thismetadata.toString();


		}else{
			//add field UNREACHABLE
			System.out.println("UNREACHABLE");
			thismetadata.put("reachability","UNREACHABLE");
			thismetadata.put("User-Id",libuser);
			thismetadata.put("Provider",provider);
			loggedRecord=thismetadata.toString();
		}
		System.out.println("LOG RECORD:"+loggedRecord);
		return loggedRecord;
	}
}
