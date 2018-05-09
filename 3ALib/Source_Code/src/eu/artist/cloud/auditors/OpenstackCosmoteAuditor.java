package eu.artist.cloud.auditors;

/*
 * Copyright (c) 2014 ICCS
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0
 * which
accompanies this distribution, and is available at
 *
http://www.apache.org/licenses/LICENSE-2.0.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *
George Kousiouris

 * Initially developed in the context of ARTIST EU project
www.artist-project.eu
 *//**
 * 
 */
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Set;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.NodeMetadata;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.ServerAddress;
import com.mongodb.Mongo;
import com.google.appengine.repackaged.org.apache.http.conn.HttpHostConnectException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class OpenstackCosmoteAuditor implements AuditingInterface {

	/* (non-Javadoc)
	 * @see eu.artist.cloud.auditors.AuditingInterface#checkApplicabilityConditions(java.lang.String)
	 */

	/*FIX!!
	 * We need to check provider's API if is available (together with 8.8.8.8), 
	 * since if no answer is taken then exceptions will be 
	 * thrown (similar to having no connectivity). In this case, given that we can not get info, this sample should
	 * be logged as unavailable since we can not decide due to provider's fault
	From Amazon EC2 SLA page (logging requirements):
	 To receive a Service Credit, you must submit a claim by opening a case in the AWS Support Center. To be eligible, the credit request must be received by us by the end of the second billing cycle after which the incident occurred and must include:

    the words “SLA Credit Request” in the subject line;
    the dates and times of each Unavailability incident that you are claiming;
    the affected EC2 instance IDs or the affected EBS volume IDs; and
    your request logs that document the errors and corroborate your claimed outage (any confidential or sensitive information in these logs should be removed or replaced with asterisks).
	 *
	 */
	/* (non-Javadoc) 
	 * @see eu.artist.cloud.auditors.AuditingInterface#checkApplicabilityConditions(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public  void checkApplicabilityConditions(String provider,String user,String apiKey, String databaseIP, String DBuser, String DBkey,String libUser) throws Exception {
		// TODO Auto-generated method stub
		while (2>1){
		//get list of servers-GENERIC
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
	    	HttpPost httppost = new HttpPost("http://10.0.16.11:5000/v3/auth/tokens");
	    
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
	    	JSONArray catalog=myObject.getJSONArray("catalog");
	    	
	    	JSONArray endpoints;
	        String final_url="";
	      	for (int k = 0; k < catalog.length(); k++) {
	      			
	      		JSONObject IterObject =  catalog.getJSONObject(k);
	      			
	      			if (IterObject.getString("name").equals("nova")) {
	      				
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
	    	String link=final_url.split(":")[2];
	    	final_url="http://10.0.16.11:"+link;
	    	//System.out.println("RRRRRRRRRRRRRR->"+final_url+"/servers/detail");
	    	//--------------------------------------------------------------------
	    
	    	HttpGet httppost1 = new HttpGet(final_url+"/servers/detail");
	    	httppost1.addHeader("X-Auth-Token", Token_ID);
	    	CloseableHttpClient httpclient1 = HttpClients.createDefault();
	    	ResponseHandler<String> handler1 = new BasicResponseHandler();
	    	CloseableHttpResponse res1 = httpclient1.execute(httppost1);
	    	String body1 = handler1.handleResponse(res1);
	    	//System.out.println("HERE COMES THE BODY -----<>-----"+body1);
	    	JSONObject myObject1=new JSONObject(body1);
	    	
	        //--------------Service available implementation-------------------------   	
	    	
	    	JSONArray arr=myObject1.getJSONArray("servers");
	    	JSONObject server=null;
	      	
	    	boolean Serversrunning=false;
	    	boolean has_floating=false;
	    	boolean available=false;
	    	
	    	for (int i = 0; i < arr.length(); i++) {
	    		 
	    		server= arr.getJSONObject(i);
	    		
	    		if (server.get("status").equals("ACTIVE")) {
	    			
	    			Serversrunning=true;
	    			if(checkReachability(server)) {
	    			    	available=true;
	    				    //break;
	    			}

	    			if(checkFloatingip(server)) {
	    				    has_floating=true;
	    			}
	    		}
	    	}
	    	
	    	if(Serversrunning && has_floating){
	    		System.out.println(Serversrunning);
	    		System.out.println(has_floating);
	    		loggedData.add(addReachabilityInfo(available,server,libUser,provider));
				AbstractedAvailabilityLogger Logger=new AbstractedAvailabilityLogger();
				try {
					Logger.logAvailability(loggedData,DBuser,DBkey, databaseIP);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("DB unavailable....");
				}
				
	    	}
	    	else {
	    		System.out.println("There are not Active nodes in this Account");
	    	}
	    	
	    	
	    	
	    	if(arr.length()==0) {
	    		System.out.println("There are no servers in this Account");
	    	}
	    //------------------------------------------------------------------------------
	    	
	    	
	    	
	    	
	    }
		    catch (Exception e) {
		    	    
		    	    e.printStackTrace();
	    			System.out.println("Check your connectivity....");
	    	}
	    	
		
	}//daemon while end
		//return validTemplateIDs;

	}	
	    //	System.out.println(lol1);
	    	
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
			


	public boolean checkReachability(JSONObject server){

		//for needed
                                                                                     //-------------Ti ip prepei na koitaw edw Cosmote eidikh periptwsh edw 
		boolean isReachable=false;
		String ip="";
		Boolean found_floating=false;
    	JSONObject addresses= server.getJSONObject("addresses");
    	Set<String> keys=addresses.keySet();
    	String network=(String) keys.toArray()[0];
    	JSONObject addr;
    	
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
    			//  isReachable=true; //What do we want here?
    		  }
    		   
    		  else {
    			String IPAddress =  ip;
	    	    //check reachability here? directly transform field and add to loggeddata
	    	    //String IPAddress=metadata.getHostname();//here or publicaddresses array
		
			

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
