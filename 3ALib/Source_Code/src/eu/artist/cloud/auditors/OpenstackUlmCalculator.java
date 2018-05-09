package eu.artist.cloud.auditors;

import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Set;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class OpenstackUlmCalculator {
	public double calculateAvailability(String overall,String Quser, int DefinedQuantumofTimeInSeconds,String RunOption,ArrayList<Double> SlaLimits) {// (Date thisDate,
		// String userID,){

		double result = 0;

		//is this correct here?if the entire AWS is down, then it will add all 
		//intervals of all VMs--FIX
		double OVERALL_MONTH_INTERVAL_SECONDS=0;
		
		//Take the SLA Limits Custom or no.
		double Soft_SLA = SlaLimits.get(0);
		double Hard_SLA = SlaLimits.get(1);

		// DB interface part
		Mongo mongoClient;
		try {
			mongoClient = new Mongo("147.102.19.75" , 80);
			DB db = mongoClient.getDB("3alib");
		//	System.out.println("Host address:" + databaseIP);
			Set<String> colls = db.getCollectionNames();
			DBCollection coll = db.getCollection("logs");
			DBCollection coll_sla = db.getCollection("SLA");
			// get date and time of interest
			// preparation only once, usage in query for each distinct template
			// ID

			Calendar cal = Calendar.getInstance();
			
			Date dateFrom=null;
			
			Date dateTo=null;
			
			if(RunOption.equals("daily")) {
				dateTo=cal.getTime();
				cal.add(Calendar.DATE, -1);
				dateFrom=cal.getTime();
				coll_sla = db.getCollection("SLAStats");
			}
			
			else if(RunOption.equals("hourly")) {
					//Insert Logic
				dateTo=cal.getTime();
				
			//	System.out.println(dateTo);
				cal.add(Calendar.HOUR, -1);
				dateFrom=cal.getTime();
				
			
			}
			else if(RunOption.equals("monthly")) {
				 
				    dateTo=cal.getTime();
				//	System.out.println(dateTo);
				   // cal.set(2017, 11, 1);
					cal.add(Calendar.MONTH, -1);
					dateFrom=cal.getTime();
			}
			
			else {
				System.out.println("Invalid Interval Request");
				System.exit(0);
			//Insert Logic
			
			
			}
		/*	
			Scanner reader = new Scanner(System.in);
			System.out.println("Enter start year");

			int startYear = reader.nextInt();

			System.out.println("Enter start month");

			int startMonth = reader.nextInt()-1;//needs -1 since month numbering begins from 0

			System.out.println("Enter start day of month");

			int startDay = reader.nextInt();

			System.out.println("Enter stop year");

			int stopYear = reader.nextInt();

			System.out.println("Enter stop month");
			int stopMonth = reader.nextInt()-1;//needs +1 since month numbering begins from 0

			System.out.println("Enter stop day of month");

			int stopDay = reader.nextInt();
			
			
		*/
			
			//String date1=Year-Month instead of date we could use this
			
		

			System.out.println("Date beginning:" + dateFrom.toString());
			System.out.println("Date ending:" + dateTo.toString());
			ObjectId from = new ObjectId(dateFrom);
			ObjectId to = new ObjectId(dateTo);
			// we need a query to get the distinct templateIDs logged
			// in the DB
			// then based on this iteration we can get the actual values

			//filter by regions
			
			//get distinct region IDs
			BasicDBObject filter1 = new BasicDBObject("Provider", overall).append("User-Id",Quser); //will be changed with qoe user id
			
			
			//distinct("imageId") Distinct Data center
			
			 String index="-1";
				// query based on date to filter needed month

				BasicDBObject query = new BasicDBObject("_id", new BasicDBObject(
						"$gte", from).append("$lt", to)).append("Provider", overall).append("User-Id",Quser);

				DBCursor cursor = coll.find(query);
				System.out.println("THECOUNTIS__>"+cursor.count());
				cursor.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);
				cursor.batchSize(100);
				//here the code to extract actual stats for a given template ID


				//abstraction should be achieved also in the status messages
				//inside MongoDB add the field "reachability" should be REACHABLE OR UNREACHABLE
				//the insertion should be performed with an abstracted method at the auditors side

				try {
					long startID = 0;
					long stopID = 0;
					long diffSeconds = 0;
					
					long interarrivalStopID=0;
					long interarrivalStartID=0;
					long thisInterarrival=0;
					String filename;
					
					double PREDEFINED_LOGICAL_SAMPLE_INTERVAL_IN_SECONDS=180;//interval in which we would logically
					//have at least one sample if the daemon is running
					//We could just store the non active nodes as reachable to fix this
					
					DBObject thisObject=cursor.next();
					System.out.println("First object:"+thisObject.toString());
					int cursorCount=cursor.count();
					System.out.println("Cursor count:"+cursor.count());
					DBObject previousObject=thisObject;
					int k=0;
					filename="interarrivalTimes"+".txt";
					System.out.println("Storing stats in file:"+filename);
					
					while (k<(cursorCount+1)) {
						
						
						
						if (k>2){
							interarrivalStopID=((ObjectId)thisObject.get("_id")).getTime();
							interarrivalStartID=((ObjectId)previousObject.get("_id")).getTime();
							thisInterarrival=interarrivalStopID-interarrivalStartID;
							
							try {
								FileWriter fw = new FileWriter(filename,true); //the true will append the new data
							    fw.write(thisInterarrival+"\n");//appends the string to the file
							    fw.close();
							} catch (IOException e) {
							    e.printStackTrace();
							}
						}
						//System.out.println("Times:"+((ObjectId)thisObject.get("_id")).getTime());
						//thisObject=cursor.next();
						//System.out.println("Filtered objects:" + thisObject.get("_id"));

						if ((k%1000)==0){
							System.out.println("Progress:"+k+" from "+cursorCount+" overall records");
						}
						// index is a flag to indicate if the unreachable sample is the first of a sequence
						//of unreachable samples
						//if -1 it is the first sample in the series
						if (((thisObject.get("reachability")).equals("UNREACHABLE"))&&index.equals("-1")){ //if it is the first unavailable sample
							//index= this db index
							//ObjectId thisBSON= (ObjectId) thisObject.get("_id");

							System.out.println("Changing index to 1...");
							startID=((ObjectId)thisObject.get("_id")).getTime();//this line's id
							System.out.println("StartID is: "+startID);
							index="1";	
						}


						if (((thisObject.get("reachability")).equals("UNREACHABLE"))&&(!(index.equals("-1")))){
							//necessary for the case that two consecutive unavailable samples are too far apart (defined in variable 
							//PREDEFINED_LOGICAL_SAMPLE_INTERVAL_IN_SECONDS) in time, indicative that the image id
							//was not in the valid list during this time. Thus this interval should not count in the overall unavailable period

							long gapstopID=((ObjectId)thisObject.get("_id")).getTime();
							long gapstartID=((ObjectId)previousObject.get("_id")).getTime();
							//System.out.println("StopID is: "+stopID);
							long GapdiffSeconds = (gapstopID-gapstartID) / 1000; // 60;
							if (GapdiffSeconds>PREDEFINED_LOGICAL_SAMPLE_INTERVAL_IN_SECONDS){
								System.out.println("Found gap...");

								stopID=((ObjectId)previousObject.get("_id")).getTime();//this line's id to end interval
								System.out.println("StopID is previous: "+stopID);
								diffSeconds = (stopID-startID) / 1000; // 60;
								//System.out.println("Unavailable Interval is:"+diffSeconds);
								if (diffSeconds>DefinedQuantumofTimeInSeconds){

									//needs global variable

									OVERALL_MONTH_INTERVAL_SECONDS=OVERALL_MONTH_INTERVAL_SECONDS+diffSeconds;
									System.out.println("Overall month interval in seconds now:"+OVERALL_MONTH_INTERVAL_SECONDS);
								}


								//we must not initialize the index to -1 at this point!!!!!!!we must just reset the startID to point to the current
								//point
								startID=((ObjectId)thisObject.get("_id")).getTime();//this line's id

							}else{
								//standard logic to cover generic case of consecutive unavailable samples


							}

						}

						if (((((thisObject.get("reachability")).equals("REACHABLE"))||(!(cursor.hasNext()))))&&(!(index.equals("-1")))){
							//calculate interval from index to (this index-1)
							if (!(cursor.hasNext())){
								System.out.println("FINAL ELEMENT REACHED");
							}
							//we always get the previous sample, assuming that in the meantime
							//up to the available sample
							//it was available
							stopID=((ObjectId)previousObject.get("_id")).getTime();
							/*
							long gapstopID=((ObjectId)thisObject.get("_id")).getTime();
							long gapstartID=((ObjectId)previousObject.get("_id")).getTime();
							//System.out.println("StopID is: "+stopID);
							long GapdiffSeconds = (gapstopID-gapstartID) / 1000; // 60;
							if (GapdiffSeconds>PREDEFINED_LOGICAL_SAMPLE_INTERVAL_IN_SECONDS){
								System.out.println("Found gap...");

								stopID=((ObjectId)previousObject.get("_id")).getTime();//this line's id to end interval
								System.out.println("StopID is previous: "+stopID);
							}else
							{stopID=((ObjectId)previousObject.get("_id")).getTime();//this line's id to end interval
							System.out.println("StopID is: "+stopID);}
							*/
							diffSeconds = (stopID-startID) / 1000; // 60;
							//System.out.println("final segment Unavailable Interval is:"+diffSeconds);
							//if interval>Quantum add intervall to OVERALL MONTH interval

							if (diffSeconds>DefinedQuantumofTimeInSeconds){

								//needs global variable

								OVERALL_MONTH_INTERVAL_SECONDS=OVERALL_MONTH_INTERVAL_SECONDS+diffSeconds;
								System.out.println("Overall month interval in seconds now:"+OVERALL_MONTH_INTERVAL_SECONDS);
							}

							//set index=-1
							System.out.println("Resetting index to -1...");
							index="-1";
						}
						
						if ((cursor.hasNext())){
						previousObject=thisObject;
						thisObject=cursor.next();
						}
						k++;
					}//end of while for resultset analysis

					System.out.println("Final Overall month unavailable interval in seconds now:"+OVERALL_MONTH_INTERVAL_SECONDS);
					
					//part for measuring percentage of availability based on provider definition
					double OverallUnavailableIntervalInMinutes= OVERALL_MONTH_INTERVAL_SECONDS/60;
					System.out.println("OverallUnavailableIntervalInMinutes:"+OverallUnavailableIntervalInMinutes);
					double OverallIntervalInSeconds = (dateTo.getTime()-dateFrom.getTime()) / 1000; 
					//System.out.println("OVERALLINTERVAL IN SECONDS:"+OverallIntervalInSeconds);
					double OverallIntervalInMinutes= OverallIntervalInSeconds/60;
					
					//System.out.println("OVERALLINTERVAL IN Minutes:"+OverallIntervalInMinutes);
					double finalAvailabilityPercentage=100.0*((OverallIntervalInMinutes-OverallUnavailableIntervalInMinutes)/OverallIntervalInMinutes);
					System.out.println("Final percentage of availability based on provider definition in the given interval:"+finalAvailabilityPercentage);

					BasicDBObject obj = new BasicDBObject();
					String Agg_status="Ok";
					double Violation=0.0;
					if((finalAvailabilityPercentage <Soft_SLA)) {
					    	
					    Violation=(Soft_SLA-finalAvailabilityPercentage);
					    	
					    if((finalAvailabilityPercentage >= Hard_SLA)) {
					    		Agg_status ="Soft";
					    	}
					    else {
					    		Agg_status ="Hard";
					    	}
					    	
					    }	
				    	cal.setTime(dateFrom);
				    	int m=(cal.get(Calendar.MONTH)) +1;
				    	int y=(cal.get(Calendar.YEAR));
				        obj.put("User",Quser);
				        obj.put("Provider",overall.split("-")[0]);
				        obj.put("Service",overall.split("-")[1]);
				        obj.put("Data-Center","ULM-DC");
				        obj.put("SLA", finalAvailabilityPercentage);
				        obj.put("Date",dateFrom); 
				        obj.put("Month", m);
				        obj.put("Year", y);
				        obj.put("type",RunOption);
				        obj.put("Agreement-Status",Agg_status);
				        obj.put("Violation-Percentage",Violation);
				        coll_sla.insert(obj);
					
				}
				
				catch (NoSuchElementException e2){

					System.out.println("No available data for this period...");

				} catch (Exception e1){

					e1.printStackTrace();

				} finally {
					cursor.close();
				}
			

			//end of for for distinct template IDs
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("No available data for this period...");
		}


		return result;

	}
}
