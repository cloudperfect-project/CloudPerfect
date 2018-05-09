package eu.artist.cloud.auditors;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;

public class AzureBlobLogParser {

	public AzureBlobLogParser() {
		// TODO Auto-generated constructor stub

		// Logs are given offline as a file, following logging process mentioned here:
		//https://msdn.microsoft.com/en-us/library/azure/dn782840.aspx

		//Log format is given here:
		//https://msdn.microsoft.com/en-us/library/azure/hh343259.aspx

		//Purpose of this class is to get these logs, identify if a sample falls under the
		//SLA timing constraints, as mentioned here
		//http://www.microsoft.com/en-us/download/confirmation.aspx?id=6656
		//and store them in the 3alib

	}
	public void parseLogs(String path, String libuser,String dbhost,String dbuser,String dbpass){
		
		BufferedReader br = null;
		 
		try {
 
			String sCurrentLine;
			String[] segmentedLine = null;
			String operationType;
			br = new BufferedReader(new FileReader(path));
			String conclusion=null;
			String azureTimestamp=null;
			
			//sample structure in 3alib to overcome lack of (e.g. json) format: 
			//<timestampofDb> (useless) <OperationType> <AzureTimestamp> 
			//<account>? <conclusion> 
			
			
			while ((sCurrentLine = br.readLine()) != null) {
				System.out.println(sCurrentLine);
				//parse and store to 3alib db
				segmentedLine=sCurrentLine.split(";");
				
				//according to the SLA,the processing time refers to the 
				//<server-latency-in-ms> field (position String[6])
				//different limits exist for different types of requests
				
				operationType=segmentedLine[2];
				//CHECK IF NEW LINE IS ACTUALLY IN THE END OF THE ROW FIX
				
				conclusion="Success";
				
				if (operationType.equals("CopyBlob")){
					//assumes in the same storage account
					if (Integer.parseInt(segmentedLine[6])>90000){
						//failure
						//add conclusion to sample
						conclusion="Failure";
						
						
					}else{
						//success
						//add conclusion to sample
						conclusion="Success";
						
					}
					
					
					
				}
				if ((operationType.equals("PutBlockList"))||(operationType.equals("GetBlockList"))){
					//assumes in the same storage account
					if (Integer.parseInt(segmentedLine[6])>60000){
						//failure
						//add conclusion to sample
						conclusion="Failure";
						
					}else{
						//success
						//add conclusion to sample
						conclusion="Success";
						
					}
					
					
					
				}
				
				//submit sample to db
				azureTimestamp=segmentedLine[1];
				ArrayList record=new ArrayList();
				record.add("{\"Type\":\""+operationType+"\""+",\"Conclusion\":\""+conclusion+"\",\"AzureTimestamp\":\""+azureTimestamp+"\",\"Libuser\":\""+libuser+"\"}");
				AbstractedAvailabilityLogger Logger=new AbstractedAvailabilityLogger();
				try {
					Logger.logAvailability(record,dbuser,dbpass, dbhost);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("DB unavailable....");
				}
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		

	}

}
