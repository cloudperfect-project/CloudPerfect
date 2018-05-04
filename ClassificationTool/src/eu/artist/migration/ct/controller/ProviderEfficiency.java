package eu.artist.migration.ct.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.artist.migration.ct.common.IniReader;

public class ProviderEfficiency {
	private String workloadId;
	private double minPrice =100000.0;
	private double maxPrice =0.0;
	private double minPerformance = 10000000000.0;
	private double maxPerformance = 0.0;
	
	
	public ProviderEfficiency(String workloadId) {
		this.workloadId = workloadId;
	}
	

/*FOT: Commented out old code calling local SQL
 * 
 	private void setMinMaxPrices(Connection connection) throws SQLException {
		Statement statement = connection.createStatement();
		try {
			ResultSet resultSet = statement.executeQuery("SELECT MIN(Price) min, MAX(Price) max FROM Price");
			resultSet.next();
			minPrice = resultSet.getDouble("min");
			maxPrice = resultSet.getDouble("max");
		}
		finally {
			statement.close();
		}
	}
	
	private void setMinMaxPerformance(Connection connection, WorkloadInfo wInfo) throws SQLException {
		Statement statement = connection.createStatement();
		try {
			ResultSet resultSet = statement.executeQuery("SELECT MIN(aveg) min, MAX(aveg) max " +
														 "FROM " +
														 "( " +
														 	"SELECT AVG(" + wInfo.getMetricName() +") aveg " +
														 	"FROM " + wInfo.getTableName() + " " +
														 	"WHERE Workload = '" + wInfo.getWorkloadName() + "' " +
														 	"GROUP BY CloudProvider, InstanceType " +
														 ") AS memTable;");
			resultSet.next();
			minPerformance = resultSet.getDouble("min");
			maxPerformance = resultSet.getDouble("max");
		}
		finally {
			statement.close();
		}
	}
	*/
	
	private String getMostEfficientProvider(WorkloadInfo wInfo, int number)  {
		
		//FOT New method code: requesting benchmark scores for specific category found from QoE MongoDB
		IniReader reader = IniReader.getInstance();
		  try {
			  	String benchmark = wInfo.getTableName();
			  	String workload = wInfo.getWorkloadName();
				String QoE_RESTconnectionString = "http://" + reader.getDbHost() +":" + reader.getDbPort() + "/" + reader.getDbName() + "/results/_find?criteria={\"tool\":\""+benchmark+"\",\"workload\":\""+workload+"\"}";
				//System.out.println("call -->"+QoE_RESTconnectionString);
			    URL url = new URL(QoE_RESTconnectionString);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				
				conn.setDoOutput(true);
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Accept", "application/json");
				conn.setConnectTimeout(2000);
				if (conn.getResponseCode() != 200) {
					//System.out.println("-->"+conn.getResponseMessage());
					throw new RuntimeException("Connection with QoE database failed!! HTTP error code : "+ conn.getResponseCode());
					
				}

				BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));

				String output="";
				String line;
				while ((line= br.readLine()) != null) 
					output=output+line;
				br.close();
				conn.disconnect();

				String performanceMetric = reader.getPerformanceMetric();
				if ((performanceMetric==null) ||  (performanceMetric==""))
					performanceMetric=wInfo.getMetricName();
	
				//FOT: parsed object getting all providers' scores. Must find the best one
				JSONObject obj = new JSONObject(output);
				JSONArray results = obj.getJSONArray("results");
				ServiceOffer[] serviceOffers = new ServiceOffer[results.length()];

				for (int i=0; i<results.length(); i++) {
					JSONObject service = (JSONObject)results.get(i);
					JSONObject provider_record = (JSONObject)service.get("provider");
					String provider  = ""+provider_record.getString("id");
					String size = provider_record.getString("size");

					String provider_size = provider+" "+size;
									
					JSONObject metrics = service.getJSONObject("metrics");
					
					//Changed this if score is a table of (value, unit)
					//double score  = Double.parseDouble((String) metrics.get(performanceMetric));
					JSONObject metric;
					try {
						metric  = metrics.getJSONObject(performanceMetric);
					} catch(org.json.JSONException e) {
						return "Null";
					}	
					double score  = Double.parseDouble(""+ metric.get("value"));

					double price = Double.parseDouble(getPriceFromQoEdb(reader.getDbHost(), reader.getDbPort(), reader.getDbName(), provider, size));
					//System.out.println("Service: "+provider_size+" , score:"+score+", price:"+price);
					
					serviceOffers[i] = new ServiceOffer(provider_size, score, price);
					if (minPrice>price)
						minPrice = price;
					if (maxPrice<price)
						maxPrice = price;
					if (minPerformance>score)
						minPerformance = score;
					if (maxPerformance<score)
						maxPerformance = score;
				}
				
				if (number==1) 
					return getBestProviderfromAvailableOffers(serviceOffers, reader.getPerformanceWeight(), reader.getPriceWeight(), performanceMetric);
				else {
					
					//!! NEED TO UPDATE THIS TO TAKE INTO ACCOUNT MORE ICON PARAMETERS (from conf.ini file)
					return getListOfBestProvidersfromAvailableOffers(serviceOffers, reader.getPerformanceWeight(), reader.getPriceWeight(), performanceMetric);
				}	
			 } catch (Exception e) {
				//System.out.println("Recommendation process failed, did not establish connection with QoE database. Exception got:\n");
				//e.printStackTrace();

			 }
		     return "Null";
		
			
	}
	
	public String getListOfBestProvidersfromAvailableOffers (ServiceOffer[] serviceOffers, double performanceWeight, double priceWeight, String performanceMetric){
		
		String bestOffersList = "NO RECOMMENDATION FOR THIS APP CATEGORY!";
		if ((serviceOffers==null) || (serviceOffers.length==0))
			return bestOffersList;

		for (int i =0; i<serviceOffers.length; i++) {
			
			double performance_relative_score;
			//relative score and total performance Depends whether minimum or maximum values are better
			if ((performanceMetric.equals("duration"))  || (performanceMetric.contains("latency"))) {
				if (serviceOffers[i].score>0.0)
					performance_relative_score=(minPerformance/serviceOffers[i].score)*performanceWeight ;
				else
					performance_relative_score=performanceWeight;
			}	
			else {
				if (maxPerformance>0.0)
					performance_relative_score=(serviceOffers[i].score/maxPerformance)*performanceWeight ;
				else
					performance_relative_score=performanceWeight;
			}		
			double price_relative_score=(minPrice/serviceOffers[i].price)*priceWeight ;
			
			serviceOffers[i].offerTotalScore = price_relative_score+performance_relative_score;
	
		}
		
		//FOT: sort table and return up to 5 best offers in a string separated by \n
		ServiceOffer[] sortedServiceOffers = bubbleSort(serviceOffers);
		bestOffersList ="";
		for (int i =0; i<sortedServiceOffers.length; i++) {
			bestOffersList += sortedServiceOffers[i].provider_size+". Mean "+performanceMetric+":"+sortedServiceOffers[i].score+", Monthly Price: "+sortedServiceOffers[i].price+"   totally scored="+sortedServiceOffers[i].offerTotalScore+"\n";
			if (i==4)
				break;
		}	
		
		return bestOffersList;
	}

    private ServiceOffer[] bubbleSort(ServiceOffer[] offers) {
        
        int n = offers.length;
        ServiceOffer temp = null;
       
        for(int i=0; i < n; i++){
                for(int j=1; j < (n-i); j++){
                        if(offers[j-1].offerTotalScore < offers[j].offerTotalScore){
                                temp = offers[j-1];
                                offers[j-1] = offers[j];
                                offers[j] = temp;
                        }
                }
        }
        
        return offers;
	}
	
	public String getBestProviderfromAvailableOffers (ServiceOffer[] serviceOffers, double performanceWeight, double priceWeight, String performanceMetric){
		
		String bestOffer = "NO RECOMMENDATION FOR THIS APP CATEGORY!";
		if ((serviceOffers==null) || (serviceOffers.length==0))
			return bestOffer;
		
		double offersBestTotalScore=0.0;

		for (int i =0; i<serviceOffers.length; i++) {
			
			double performance_relative_score;
			//relative score and total performance Depends whether minimum or maximum values are better
			if ((performanceMetric.equals("duration"))  || (performanceMetric.contains("latency"))) {
				if (serviceOffers[i].score>0.0)
					performance_relative_score=(minPerformance/serviceOffers[i].score)*performanceWeight ;
				else
					performance_relative_score=performanceWeight;
			}	
			else {
				if (maxPerformance>0.0)
					performance_relative_score=(serviceOffers[i].score/maxPerformance)*performanceWeight ;
				else
					performance_relative_score=performanceWeight;
			}		
			double price_relative_score=(minPrice/serviceOffers[i].price)*priceWeight ;
			
			serviceOffers[i].offerTotalScore = price_relative_score+performance_relative_score;
	
			//System.out.println("serviceOffer: "+serviceOffers[i].provider_size+" , offerTotalScore="+serviceOffers[i].offerTotalScore);
			if (serviceOffers[i].offerTotalScore>offersBestTotalScore) {
				offersBestTotalScore = serviceOffers[i].offerTotalScore;
				bestOffer = serviceOffers[i].provider_size;
//				System.out.println("offersBestTotalScore ="+offersBestTotalScore);
			}
		}

		return bestOffer;
	}
	
	public String getPriceFromQoEdb(String host, String port, String dbName, String provider, String size) {

		try {
			String QoE_RESTconnectionString = "http://" + host +":" + port + "/" + dbName + "/prices/_find?criteria={\"service_id\":\""+provider+"\",\"size\":\""+size+"\"}";

		    URL url = new URL(QoE_RESTconnectionString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			
			conn.setDoOutput(true);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
	
			if (conn.getResponseCode() != 200) {
				System.out.println("-->"+conn.getResponseMessage());
				throw new RuntimeException("Connection with QoE database failed!! HTTP error code : "+ conn.getResponseCode());
			}
	
			BufferedReader br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));
	
			String output="";
			String line;
			while ((line= br.readLine()) != null) 
				output=output+line;
			br.close();
			conn.disconnect();
			
			//FOT: parsed object to get specific provider's price
			JSONObject obj = new JSONObject(output);
			JSONArray results = obj.getJSONArray("results");
			if (results.length()>0) {
				JSONObject service = (JSONObject)results.get(0);
				String price  = ""+service.getString("monthly_price");
				return price;
			}
			//Just return a huge price for an unknown record
			else 
				return "10000.0";
		}
		catch (Exception e){
			e.printStackTrace();
			return "10000.0";
		}
		
	}
	
	public String calculateBestProvider(int number) throws Exception {
		WorkloadInfo wInfo = WorkloadInfo.findWorkload(workloadId);
		if (wInfo == null) {
			throw new Exception("Workload_id: " + workloadId + " is not a recognized workload_id. Please check your training file.");
		}
		
		//!!FOT: Commented, need to check how they make sense with QoE MongoDB
		//setMinMaxPrices(DbConnection.getConnection());
		//setMinMaxPerformance(DbConnection.getConnection(), wInfo);
		
		return getMostEfficientProvider(wInfo, number);
	}
	
	public String getWorkloadId() {
		return workloadId;
	}
	
}

