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
package eu.artist.cloud.auditors;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

/**
 * @author geo
 *
 */
public class TriggerAvailabilityCalculation {

	/**
	 * 
	 */
	public TriggerAvailabilityCalculation() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		//needs also to get provider and service name, given that a user (or in the central DB) may use services from multiple
		//providers
		//FIX! THIS SHOULD BE ALSO LOGGED DURING THE LOGGING PROCESS IN THE DB along with the userID


		//FIX NEEDS TO GET FROM PROP FILE PROVIDER NAME AND SERVICE AND HAVE SETTING LOGIC PER PROVIDER
		int quantumOfTime=60; //dependent on provider-check to see how to abstract better
		InputStream input = null;
		
		/*
		 * All the default limits of the providers (Need to be read from a file)
		 */
		
		ArrayList<Double> ULM_Limits = new ArrayList<>(Arrays.asList(99.99,99.0));
		ArrayList<Double> AWS_Limits = new ArrayList<>(Arrays.asList(99.99,99.0));
		ArrayList<Double> Cosmote_Limits = new ArrayList<>(Arrays.asList(99.9,99.0));

		try {
			Mongo mongoClient;
	    	mongoClient = new Mongo("147.102.19.75" , 80);
			
	    	DB db = mongoClient.getDB("3alib");
	    	DBCollection coll = db.getCollection("logs");
	    	DBCollection SlaAgreement = db.getCollection("SLAAgreement");

		//	Properties prop2 = new Properties();
		//	input = new FileInputStream("3alibconfig.properties");
		//	input = new FileInputStream("/run/secrets/"+args[0]+"_"+args[1]+"_"+args[2]+"_"+args[3]);
			// load a properties file
		//	prop2.load(input);

		//	String overall=prop2.getProperty("ProviderName")+"-"+prop2.getProperty("ServiceName");
	    	String overall = args[0];
			//calculate Availability
			AbstractedAvailabilityCalculator calc=new AbstractedAvailabilityCalculator();
			if (overall.equals("aws-ec2")){

				quantumOfTime=60;
				BasicDBObject filter1 = new BasicDBObject("Provider", "aws-ec2"); //will be changed with qoe user id		
	    		List distinctUsers = coll.distinct("User-Id",filter1);
	    		/*
	    		 * Do we want Custom SLAs for AWS also??
	    		 */
	    		for (int i = 0; i < distinctUsers.size(); i++) {
	    			System.out.println(distinctUsers.get(i).toString());
	    			calc.calculateAvailability(overall, distinctUsers.get(i).toString(), quantumOfTime,args[1],AWS_Limits);
	    		}
				//calc.calculateCloudSleuthAvailability(prop2.getProperty("databaseIP"));


			} 
			if (overall.equals("Cosmote-Compute")) {
			    
				quantumOfTime=Integer.parseInt(args[2]);
				OpenstackCosmoteCalculator a= new OpenstackCosmoteCalculator();
				BasicDBObject filter1 = new BasicDBObject("Provider", "Cosmote-Compute"); //will be changed with qoe user id		
	    		List distinctUsers = coll.distinct("User-Id",filter1);
	    		BasicDBObject query = new BasicDBObject();
    			query.put("Provider","Cosmote");
    			query.put("Service", "Compute");
    			List CustomAvailabilityUsers= SlaAgreement.distinct("id",query);
	    		for (int i = 0; i < distinctUsers.size(); i++) {
	    			
	    			if(CustomAvailabilityUsers.contains(distinctUsers.get(i).toString())) {
	    				
	    				query.put("id",distinctUsers.get(i).toString());
	    				DBObject next = SlaAgreement.find(query).next();
	    				a.calculateAvailability(overall, distinctUsers.get(i).toString(), quantumOfTime,args[1],new ArrayList<>(Arrays.asList( new Double(next.get("Soft_SLA").toString()), new Double(next.get("Hard_SLA").toString()))));
	    			
	    			}
	    			else {
	    				
	    				System.out.println(distinctUsers.get(i).toString());
	    				a.calculateAvailability(overall, distinctUsers.get(i).toString(), quantumOfTime,args[1],Cosmote_Limits);
	    			}
	    		}
			
			}
			if (overall.equals("ULM-Compute")) {
			
				quantumOfTime=Integer.parseInt(args[2]);
				OpenstackUlmCalculator a= new OpenstackUlmCalculator();
				BasicDBObject filter1 = new BasicDBObject("Provider", "ULM-Compute"); //will be changed with qoe user id		
	    		List distinctUsers = coll.distinct("User-Id",filter1);
	    		BasicDBObject query = new BasicDBObject();
    			query.put("Provider","ULM");
    			query.put("Service", "Compute");
    			List CustomAvailabilityUsers= SlaAgreement.distinct("id",query);
    			
	    		for (int i = 0; i < distinctUsers.size(); i++) {
	    			
	    			if(CustomAvailabilityUsers.contains(distinctUsers.get(i).toString())) {
	    				query.put("id",distinctUsers.get(i).toString());
	    				DBObject next = SlaAgreement.find(query).next();
	    				a.calculateAvailability(overall, distinctUsers.get(i).toString(), quantumOfTime,args[1],new ArrayList<>(Arrays.asList( new Double(next.get("Soft_SLA").toString()), new Double(next.get("Hard_SLA").toString()))));
	    			}
	    			else {
	    				System.out.println(distinctUsers.get(i).toString());
	    			
	    				a.calculateAvailability(overall, distinctUsers.get(i).toString(), quantumOfTime,args[1],ULM_Limits);
	    			}
	    		
	    		}
				
			}
			if (overall.equals("microsoft-azure")){//check if naming has effect-has dependency on jclouds
				//for azure it seems there is only support for blobstore

			//	quantumOfTime=60;
			//	calc.calculateAvailability(prop2.getProperty("databaseIP"), quantumOfTime);
				//calc.calculateCloudSleuthAvailability(prop2.getProperty("databaseIP"));


			}

			if (overall.equals("microsoft-azureblob")){//check if naming has effect-has dependency on jclouds
				//for azure it seems there is only support for blobstore

				quantumOfTime=3600;
				double errorrate=0.0;
				AbstractedAvailabilityCalculatorWithErrorRate blobcalc=new AbstractedAvailabilityCalculatorWithErrorRate();
		//		blobcalc.calculateAvailability(prop2.getProperty("databaseIP"),"azureblob", quantumOfTime,errorrate);
		//		blobcalc.calculateCloudSleuthAvailability(prop2.getProperty("databaseIP"), "azureblob");


			}

			if (overall.equals("google-compute-engine")){//check if naming has effect-has dependency on jclouds

				quantumOfTime=300;
			//	calc.calculateAvailability(prop2.getProperty("databaseIP"), quantumOfTime);
		//		calc.calculateCloudSleuthAvailability(prop2.getProperty("databaseIP"));
				

			}

			if (overall.contains("datastore")){
				quantumOfTime=300;
				double errorRate=0.1;
				String DBtable="datastore";
				AbstractedAvailabilityCalculatorWithErrorRate calcDatastore=new AbstractedAvailabilityCalculatorWithErrorRate();
		//		calcDatastore.calculateAvailability(prop2.getProperty("databaseIP"), "datastore", quantumOfTime, errorRate);
		//		calcDatastore.calculateCloudSleuthAvailability(prop2.getProperty("databaseIP"), "datastore");
			}
			
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

}
