/**
 * 
 */


package eu.artist.cloud.auditors;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.CommittedButStillApplyingException;

/**
 * @author geo
 *
 */
public class GAE_logger  {

	/**
	 * 
	 */
	public GAE_logger() {
		// TODO Auto-generated constructor stub
	}
	//code for getting high replication option
	//set highReplication boolean variable accordingly before attempt to call the Datastore API

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws DatastoreTimeoutException,DatastoreFailureException, Exception {
		// TODO Auto-generated method stub

		//need to add field names for DB?????FIX if yes, check entries

		//depends on client, could return strings 
		//INTERNAL_ERROR, TIMEOUT, BIGTABLE_ERROR, COMMITTED_BUT_STILL_APPLYING, TRY_ALTERNATE_BACKEND
		// or if it returns exceptions we assume this mapping
		//http://www.google.gr/url?sa=t&rct=j&q=&esrc=s&source=web&cd=5&cad=rja&uact=8&ved=0CDIQFjAE&url=http%3A%2F%2Fgoogleappengine.googlecode.com%2Fsvn-history%2Fr465%2Ftrunk%2Fjava%2Fsrc%2Fmain%2Fcom%2Fgoogle%2Fappengine%2Fapi%2Fdatastore%2FDatastoreApiHelper.java&ei=q9plVbP2G8ytsAGWsYK4DQ&usg=AFQjCNGVNAWMF4YD-eBekVdoUyVCXZL1nw&sig2=PoMKAy90HC650CvHGnOJYA&bvm=bv.93990622,d.bGg

		//TIMEOUT or BIGTABLE_ERROR -> DatastoreTimeoutException
		//INTERNAL_ERROR -> DatastoreFailureException
		//COMMITTED_BUT_STILL_APPLYING -> CommittedButStillApplyingException    (not found in the appengine-api-1.2.0 jar)
		//TRY_ALTERNATE_BACKEND ->  not found in any exception up to now

		String response="";

		//NEED TO CHECK WHETHER IT RETURNS A MESSAGE (E.G. THE LOCAL JAVA CLIENT
		//OR THROWS AN EXCEPTION

		//could put catch code in method to call from catch statement

		//could put multiple exceptions in one catch, but then we would not be able to distinguish
		//between different exceptions which may prove useful in the future
		boolean highReplication=true;

		//FIX code to get high replication setting-necessary for SLA preconditions

		
		while(true) {
			try {
				
				//EXISTING APPLICATION CODE HERE
				//db.put(entities);
				//PART OF EXISTING APPLICATION END
				submitLog("Success");	
				
			} catch (DatastoreTimeoutException e) {

				//log here failed sample
				if (highReplication){
					submitLog("DatastoreTimeoutException");
					
				}
			} 
			catch (DatastoreFailureException ex1) {

				//this is returned also in the null case (rare, probably safety clause), but if we would
				//like to be 100% accurate we should not take it under consideration
				if (highReplication){
					//log here failed sample
					submitLog("DatastoreFailureException");					
				}
			}
			catch (CommittedButStillApplyingException e1) {

				//log here failed sample
				if (highReplication){
					submitLog("CommittedButStillApplyingException");					
				}
			}
			catch (Exception anyOtherException) {

				//log here failed sample
				//SOS submitLog("Success"); should also be recorded (to count for the
				//overall calls) for all other types of exceptions that are not SLA related!!!!!!!!!!!!!!
				if (highReplication){
					submitLog("Success");					
				}
			} 
		}
	}

	public static void submitLog(String type){

		InputStream input = null;
		Properties prop2 = new Properties();
		try {
			input = new FileInputStream("3alibconfig.properties");

			// load a properties file
			prop2.load(input);
			String DBuser=prop2.getProperty("DBuser");
			String DBKey=prop2.getProperty("DBKey");
			String databaseIP=prop2.getProperty("databaseIP");
			String libuser=prop2.getProperty("3alibuser");
			String region=prop2.getProperty("Region");
			//we do not need a discrete logger, especially if we store all records in a
			//single table (supported by Mongo), or if we want separate collections,
			//then we should change the default log_samples collection in the abtracted logger
			//and create here an arraylist of the records to be included (adding user, service etc.)
			AbstractedAvailabilityLogger logger=new AbstractedAvailabilityLogger();
			ArrayList record=new ArrayList();
			record.add("{\"Type\":\""+type+"\""+",\"Region\":\""+region+"\",\"Libuser\":\""+libuser+"\"}");
			logger.logAvailability(record, DBuser, DBKey, databaseIP);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}




