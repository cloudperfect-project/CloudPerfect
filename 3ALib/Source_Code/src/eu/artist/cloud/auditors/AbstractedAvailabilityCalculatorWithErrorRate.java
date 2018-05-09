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
 */
package eu.artist.cloud.auditors;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

import org.bson.BSON;
import org.bson.BSONObject;
import org.bson.types.ObjectId;

/**
 * @author geo
 * 
 */
public class AbstractedAvailabilityCalculatorWithErrorRate {

	/**
	 * @param args
	 */

	public double calculateAvailability(String databaseIP, String databaseCollection, int DefinedQuantumofTimeInSeconds, double errorRate) {// (Date thisDate,
		// String userID,){

		double result = 0;

		//is this correct here?if the entire AWS is down, then it will add all 
		//intervals of all VMs--FIX
		double OVERALL_MONTH_INTERVAL_SECONDS=0;

		//

		// DB interface part
		Mongo mongoClient;
		try {
			mongoClient = new Mongo(databaseIP);
			DB db = mongoClient.getDB("3alib");
			System.out.println("Host address:" + databaseIP);
			Set<String> colls = db.getCollectionNames();
			DBCollection coll = db.getCollection(databaseCollection);

			// get date and time of interest
			// preparation only once, usage in query for each distinct template
			// ID

			Scanner reader = new Scanner(System.in);
			System.out.println("Enter start year");

			int startYear = reader.nextInt();

			System.out.println("Enter start month");

			int startMonth = reader.nextInt()-1;//needs +1 since month numbering begins from 0

			System.out.println("Enter start day of month");

			int startDay = reader.nextInt();

			System.out.println("Enter stop year");

			int stopYear = reader.nextInt();

			System.out.println("Enter stop month");
			int stopMonth = reader.nextInt()-1;//needs +1 since month numbering begins from 0

			System.out.println("Enter stop day of month");

			int stopDay = reader.nextInt();
			Date date = new Date();
			Calendar calendarFrom = Calendar.getInstance();
			calendarFrom.setTime(date);
			calendarFrom.set(startYear, startMonth, startDay, 0, 0, 0);

			Date dateFrom = calendarFrom.getTime();

			Calendar calendarTo = Calendar.getInstance();
			calendarTo.setTime(date);
			calendarTo.set(stopYear, stopMonth, stopDay, 23, 59, 59);

			Date dateTo = calendarTo.getTime();

			System.out.println("Date beginning:" + dateFrom.toString());
			System.out.println("Date ending:" + dateTo.toString());
			ObjectId from = new ObjectId(dateFrom);
			ObjectId to = new ObjectId(dateTo);
			// we need a query to get the distinct templateIDs logged
			// in the DB
			// then based on this iteration we can get the actual values

			//filter by regions

			//get distinct region IDs--commented line could be used for multi-user?
			//List distinctTemplates = coll.distinct("Region", new BasicDBObject("Libuser",new BasicDBObject("$eq","test")));//.distinct("Region");//distinct("imageId");
			List distinctTemplates = coll.distinct("Libuser");

			for (int i = 0; i < distinctTemplates.size(); i++) {

				String index="-1";

				System.out.println("Distinct Region IDs:"
						+ distinctTemplates.get(i).toString());


				// query based on date to filter needed month

				BasicDBObject query = new BasicDBObject("_id", new BasicDBObject(
						"$gte", from).append("$lte", to));//)
				//.append("Region", distinctTemplates.get(i).toString());

				DBCursor cursor = coll.find(query);
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

					//this is not needed for the GAE case, so we set it to something very high
					double PREDEFINED_LOGICAL_SAMPLE_INTERVAL_IN_SECONDS=10500;//interval in which we would logically
					//have at least one sample if the daemon is running

					DBObject thisObject=cursor.next();
					System.out.println("First object:"+thisObject.toString());
					int cursorCount=cursor.count();
					System.out.println("Cursor count:"+cursor.count());
					DBObject previousObject=thisObject;
					int k=0;
					filename="interarrivalTimes"+distinctTemplates.get(i).toString()+".txt";
					System.out.println("Storing stats in file:"+filename);

					//counters for determining error rate
					double overallCalls=0;
					double errorCalls=0;
					double thisError=0;

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
						if ((((thisObject.get("Type")).toString()).contains("Exception"))&&index.equals("-1")){ //if it is the first unavailable sample
							//index= this db index
							//ObjectId thisBSON= (ObjectId) thisObject.get("_id");

							System.out.println("Changing index to 1...");
							startID=((ObjectId)thisObject.get("_id")).getTime();//this line's id
							System.out.println("StartID is: "+startID);
							index="1";
							errorCalls=1;
							overallCalls=1;
						}


						if ((((thisObject.get("Type")).toString()).contains("Exception"))&&(!(index.equals("-1")))){

							errorCalls=errorCalls+1;
							overallCalls=overallCalls+1;


						}

						if ((((((thisObject.get("Type")).toString()).contains("Success"))||(!(cursor.hasNext()))))&&(!(index.equals("-1")))){
							//calculate interval from index to (this index-1)
						//if ((((((thisObject.get("Type")).toString()).contains("Success"))))&&(!(index.equals("-1")))){
							if (!(cursor.hasNext())){
								System.out.println("FINAL ELEMENT REACHED");
							}
							overallCalls=overallCalls+1;
							thisError=errorCalls/overallCalls;
							System.out.println("Error rate now:"+thisError);
							if (thisError<errorRate){
								index="-1";
								stopID=((ObjectId)previousObject.get("_id")).getTime();
								System.out.println("StopID:"+stopID);
								diffSeconds = (stopID-startID) / 1000; // 60;
								if (diffSeconds>DefinedQuantumofTimeInSeconds){

								OVERALL_MONTH_INTERVAL_SECONDS=OVERALL_MONTH_INTERVAL_SECONDS+diffSeconds;
								System.out.println("Overall month interval in seconds now:"+OVERALL_MONTH_INTERVAL_SECONDS);
								}
								errorCalls=0;
								overallCalls=0;

							}							
						}

						if ((cursor.hasNext())){
							previousObject=thisObject;
							thisObject=cursor.next();
						}else  {
							if (!(index.equals("-1"))){


								thisError=errorCalls/overallCalls;
								if (thisError>errorRate){
									System.out.println("In final step...");
									index="-1";
									stopID=((ObjectId)thisObject.get("_id")).getTime();
									System.out.println("StopID:"+stopID);
									diffSeconds = (stopID-startID) / 1000; // 60;
									System.out.println("Diffseconds:"+diffSeconds);
									if (diffSeconds>DefinedQuantumofTimeInSeconds){

									OVERALL_MONTH_INTERVAL_SECONDS=OVERALL_MONTH_INTERVAL_SECONDS+diffSeconds;
									System.out.println("Overall month interval in seconds now:"+OVERALL_MONTH_INTERVAL_SECONDS);
									}
									errorCalls=0;
									overallCalls=0;
								}
							}	
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
				}

				catch (NoSuchElementException e2){

					System.out.println("No available data for this period...");

				} catch (Exception e1){

					e1.printStackTrace();

				} finally {
					cursor.close();
				}


			}//end of for for distinct template IDs
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

	public double calculateCloudSleuthAvailability(String databaseIP, String databaseCollection) {


		double CloudSleuthAvailability=0;
		Mongo mongoClient;
		try {
			mongoClient = new Mongo(databaseIP);
			DB db = mongoClient.getDB("3alib");
			System.out.println("Host address:" + databaseIP);
			Set<String> colls = db.getCollectionNames();
			DBCollection coll = db.getCollection(databaseCollection);

			// get date and time of interest
			// preparation only once, usage in query for each distinct template
			// ID

			Scanner reader = new Scanner(System.in);
			System.out.println("Enter start year");

			int startYear = reader.nextInt();

			System.out.println("Enter start month");

			int startMonth = reader.nextInt()-1;//needs +1 since month numbering begins from 0

			System.out.println("Enter start day of month");

			int startDay = reader.nextInt();

			System.out.println("Enter stop year");

			int stopYear = reader.nextInt();

			System.out.println("Enter stop month");
			int stopMonth = reader.nextInt()-1;//needs +1 since month numbering begins from 0

			System.out.println("Enter stop day of month");

			int stopDay = reader.nextInt();
			Date date = new Date();
			Calendar calendarFrom = Calendar.getInstance();
			calendarFrom.setTime(date);
			calendarFrom.set(startYear, startMonth, startDay, 0, 0, 0);

			Date dateFrom = calendarFrom.getTime();

			Calendar calendarTo = Calendar.getInstance();
			calendarTo.setTime(date);
			calendarTo.set(stopYear, stopMonth, stopDay, 23, 59, 59);

			Date dateTo = calendarTo.getTime();

			System.out.println("Date beginning:" + dateFrom.toString());
			System.out.println("Date ending:" + dateTo.toString());
			ObjectId from = new ObjectId(dateFrom);
			ObjectId to = new ObjectId(dateTo);
			// we need a query to get the distinct templateIDs logged
			// in the DB
			// then based on this iteration we can get the actual values



			//get distinct region IDs
			List distinctTemplates = coll.distinct("Libuser");

			for (int i = 0; i < distinctTemplates.size(); i++) {
				System.out.println("Region ID:"
						+ distinctTemplates.get(i).toString());


				// query based on date to filter all samples for the needed month for that region

				BasicDBObject queryPerRegionOverall = new BasicDBObject("_id", new BasicDBObject(
						"$gte", from).append("$lte", to));
				//.append("Region", distinctTemplates.get(i).toString());

				DBCursor cursorOverall = coll.find(queryPerRegionOverall);
				cursorOverall.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);
				System.out.println("Overall records:"+cursorOverall.count());


				//easier in this case to count success samples 
				BasicDBObject queryAvailable = new BasicDBObject("_id", new BasicDBObject(
						"$gte", from).append("$lte", to))
				.append("Conclusion", "Success");

				DBCursor cursorAvailable = coll.find(queryAvailable);
				System.out.println("Available records:"+cursorAvailable.count());

				CloudSleuthAvailability=100*(((double)cursorAvailable.count())/cursorOverall.count());
				System.out.println("Cloudsleuth based availability:"+CloudSleuthAvailability);
			}

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (NoSuchElementException e2){

			System.out.println("No available data for this period...");

		} 
		catch (MongoException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("No available data for this period...");
		}
		return CloudSleuthAvailability;
	}

}


