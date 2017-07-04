package eu.artist.migration.ct.ui;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;


import eu.artist.migration.ct.controller.ClassificationController;
import eu.artist.migration.ct.controller.NormalizationType;
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
		else if (args[0].equals("NoGUI")){
			String trainingFile = profilerPath+"BenchmarkProfiles-trainingFile.txt";
			String tSharkFile = profilerPath+"TShark.txt";
			String pidStatFile = profilerPath+"Pidstat.txt";
			NormalizationType norma = NormalizationType.RangeNormalization;
			SimilarityMeasure simi = SimilarityMeasure.EuclideanDistance;
			ClassificationController controller = new ClassificationController(pidStatFile, tSharkFile, trainingFile,
					simi, norma);
			try {
				System.out.println("Category: "+controller.calculateClassification());
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		else
			System.out.println("ERROR: Did not provide the correct argument to Classifier");
	}

}
