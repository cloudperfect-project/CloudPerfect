package eu.artist.migration.ct.ui;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;

import eu.artist.migration.ct.common.ProfilerConfigurationReader;
import eu.artist.migration.ct.controller.ClassificationController;
import eu.artist.migration.ct.controller.DbConnection;
import eu.artist.migration.ct.controller.NormalizationType;
import eu.artist.migration.ct.controller.ProviderEfficiency;
import eu.artist.migration.ct.controller.SimilarityMeasure;

public class Main {

	public static void main(String[] args) throws ClassNotFoundException {
		
		//FOT:  get path to put default files in form
		String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String decodedPath="";
		try {
			decodedPath = URLDecoder.decode(path, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int i = decodedPath.indexOf("classifier/");
		String profilerPath = decodedPath.substring(0, i)+"profiler/";
		//FOT: end of addition
		
		//FOT: Capture NoGUI option 
		if ((args==null) || (args.length==0)) {
			Class.forName("com.mysql.jdbc.Driver");
			MainForm.showForm(profilerPath);
		}
		else if ((args.length==1) && (args[0].equals("NoGUI"))){
			String trainingFile = profilerPath+"BenchmarkProfiles-trainingFile.txt";
			String tSharkFile = profilerPath+"TShark.txt";
			String pidStatFile = profilerPath+"Pidstat.txt";
			NormalizationType norma = NormalizationType.RangeNormalization;
			SimilarityMeasure simi = SimilarityMeasure.EuclideanDistance;
			ClassificationController controller = new ClassificationController(pidStatFile, tSharkFile, trainingFile,
					simi, norma);
			try {
				String Category = controller.calculateClassification();
				System.out.println("Classifier_Category: "+Category);
				System.out.println("Best_Provider: "+controller.calculateBestProvider(Category));

			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		//FOT: RUN IN PROVIDER MODE - save category in MariaDB, credentials provided in .ini file
		else if ((args.length==2) && (args[0].equals("NoGUI"))  && (args[1].equals("-p"))){

			String Vm_id = ProfilerConfigurationReader.getVMid(profilerPath+"application-provider-info.txt");
			System.out.println("VM-ID FOUND: "+Vm_id);
			
			try {
				NormalizationType norma = NormalizationType.RangeNormalization;
				SimilarityMeasure simi = SimilarityMeasure.EuclideanDistance;
				ClassificationController controller = new ClassificationController(Vm_id, simi, norma);
				
				Connection connection = DbConnection.getConnection();
				String Category =  controller.calculateClassification(connection);

				String query = "REPLACE into CATEGORIES (vm_id, category)"
			    	        + " values (?, ?)";
			    PreparedStatement preparedStmt = connection.prepareStatement(query);
	    	    preparedStmt.setString (1, Vm_id);
	    	    preparedStmt.setString (2, Category);
			    preparedStmt.execute();
			    connection.close();
			    
				System.out.println("Saved to local MariaDB--> Category: "+Category);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		//FOT: No need for Classification. Recommender functioned called in Adopter mode
		else if (args.length==2) {
			
			String Category = args[0];
			String recommendationFile = args[1]+"RankedOffers.txt";
			try {
				System.out.println("Calculating recommendation for Category: "+Category);
				ProviderEfficiency pe = new ProviderEfficiency(Category);
				String rankedOffersList =  pe.calculateBestProvider(5);
				//save to output file
				FileWriter  filewrt = new FileWriter(recommendationFile);
				BufferedWriter out = new BufferedWriter(filewrt);
				out.write(rankedOffersList);
				out.close();
				filewrt.close();

			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}


		else
			System.out.println("ERROR: Did not provide the correct argument(s) to Classifier");

	}

}
