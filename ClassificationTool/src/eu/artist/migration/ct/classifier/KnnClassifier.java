package eu.artist.migration.ct.classifier;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import eu.artist.migration.ct.common.Constants;
import eu.artist.migration.ct.controller.DbConnection;
import eu.artist.migration.ct.controller.NormalizationType;
import eu.artist.migration.ct.controller.SimilarityMeasure;

public class KnnClassifier {
	
	private String pidstatFileName;
	private String tsharkFileName;
	private String trainingFileName;
	private int k;
	private SimilarityMeasure similarity;
	private NormalizationType normalization;
	
	
	public String getPidstatFileName() {
		return pidstatFileName;
	}
	
	public void setPidstatFileName(String pidstatFileName) {
		this.pidstatFileName = pidstatFileName;
	}
	
	public String getTsharkFileName() {
		return tsharkFileName;
	}
	
	public void setTsharkFileName(String tsharkFileName) {
		this.tsharkFileName = tsharkFileName;
	}
	
	public String getTrainingFileName() {
		return trainingFileName;
	}
	
	public void setTrainingFileName(String trainingFileName) {
		this.trainingFileName = trainingFileName;
	}
	
	public int getK() {
		return k;
	}
	
	public void setK(int k) {
		this.k = k;
	}
	
	public KnnClassifier() {
		k = 5;
	}
	
	public KnnClassifier(String pidstatFileName, String tsharkFileName, String trainingFileName, SimilarityMeasure similarity, NormalizationType normalization) {
		this();
		this.pidstatFileName = pidstatFileName;
		this.tsharkFileName = tsharkFileName;
		this.trainingFileName = trainingFileName;
		this.similarity = similarity;
		this.normalization = normalization;
	}
	
	//FOT: added method for provider mode
	public KnnClassifier(SimilarityMeasure similarity, NormalizationType normalization) {
		this();
		this.similarity = similarity;
		this.normalization = normalization;
	}
	
	public KnnClassifier(String pidstatFileName, String tsharkFileName, String trainingFileName, int vectorSize, int k, SimilarityMeasure similarity, NormalizationType normalization) {
		this(pidstatFileName, tsharkFileName, trainingFileName, similarity, normalization);
		this.k = k;
	}
	
	private double getEuclideanDistance(Workload a, Workload b) throws Exception {
		double sum = 0;
		if (a.getVector().length != b.getVector().length) {
			throw new Exception("Invalid vector sized in workloads!");
		}
		
		for (int i = 0; i < a.getVector().length; i++) {
			sum += Math.pow((a.getVector()[i] - b.getVector()[i]), 2);
		}
		return Math.sqrt(sum);
	}
	
	private double getCosineDistance(Workload a, Workload b) throws Exception {
		double arithmitis = 0.0;
		double paronomastis1 = 0.0;
		double paronomastis2 = 0.0;
		if (a.getVector().length != b.getVector().length) {
			throw new Exception("Invalid vector sized in workloads!");
		}
		
		for (int i = 0; i < a.getVector().length; i++) {
			arithmitis += a.getVector()[i] * b.getVector()[i];
			paronomastis1 += Math.pow(a.getVector()[i], 2);
			paronomastis2 += Math.pow(b.getVector()[i], 2);
		}
		
		paronomastis1 = Math.sqrt(paronomastis1);
		paronomastis2 = Math.sqrt(paronomastis2);
		
		return 1 - (arithmitis / (paronomastis1 * paronomastis2));
	}
	
	private double getBrayCurtisDistance(Workload a, Workload b) throws Exception {
		double arithmitis = 0.0;
		double paronomastis = 0.0;
		if (a.getVector().length != b.getVector().length) {
			throw new Exception("Invalid vector sized in workloads!");
		}
		
		for (int i = 0; i < a.getVector().length; i++) {
			arithmitis += Math.abs(a.getVector()[i] - b.getVector()[i]);
			paronomastis += Math.abs(a.getVector()[i] + b.getVector()[i]);
		}
		
		return arithmitis / paronomastis;
	}
	
	private List<BenchmarkWorkload> calculate_knn(int k, ApplicationWorkload application, List<BenchmarkWorkload> workloads) throws Exception {
		double distance = 0.0;
		BenchmarkWorkload work;
		for (int i = 0; i < workloads.size(); i++) {
			work = workloads.get(i);
			switch (similarity) {
			case BrayCurtisDistance:
				distance = getBrayCurtisDistance(application, work);
				break;
			case CosineDistance:
				distance = getCosineDistance(application, work);
				break;
			case EuclideanDistance:
				distance = getEuclideanDistance(application, work);
				break;
			}
			work.setDistanceFromRequest(distance);
		}
		Collections.sort(workloads);
		
		//System.out.println("\n\n\n");
		//System.out.println("Classifier: Knn\tNormalization: " + normalization.name() + "\tSimilarity: " + similarity.name());
		//System.out.println("----------------- Results Knn Begin -----------------");
		//for (int i = 0; i < workloads.size(); i++) {
		//	System.out.println(i + "\t" + workloads.get(i));
		//}
		//System.out.println("-----------------  Results Knn End  -----------------");
		
		ArrayList<BenchmarkWorkload> result = new ArrayList<BenchmarkWorkload>(k);
		for (int i = 0; i < k; i++) {
			result.add(workloads.get(i));
		}
		return result;
	}
	
	private String getWorkloadClassification(ApplicationWorkload query, List<BenchmarkWorkload> nearest) {
		HashMap<String, Integer> histogram = new HashMap<String, Integer>();
		
		Integer value;
		int max = 0;
		for (BenchmarkWorkload w: nearest) {
			value = histogram.get(w.getName());
			if (value == null) {
				histogram.put(w.getName(), 1);
				if (1 > max) {
					max = 1;
				}
			}
			else {
				histogram.put(w.getName(), value + 1);
				if (value + 1 > max) {
					max = value + 1;
				}
			}
		}
		
		HashMap<String, Double> maxKeys = new HashMap<String, Double>();
		for (Entry<String, Integer> e: histogram.entrySet()) {
			if (e.getValue() == max) {
				maxKeys.put(e.getKey(), 0.0);
			}
		}
		
		if (maxKeys.size() == 1) {
			return maxKeys.keySet().toArray()[0].toString();
		}
		
		//We have a tie
		Double distanceSum;
		for (BenchmarkWorkload w: nearest) {
			distanceSum = maxKeys.get(w.getName());
			if (distanceSum != null) {
				maxKeys.put(w.getName(), distanceSum + w.getDistanceFromRequest());
			}
		}
		
		double min = Double.MAX_VALUE;
		String result = null;
		for (Entry<String, Double> e: maxKeys.entrySet()) {
			if (e.getValue() < min) {
				min = e.getValue();
				result = e.getKey();
			}
		}
		return result;
	}
	
	private double calculateMeanValue(ArrayList<BenchmarkWorkload> benchmarkWorkloads, ApplicationWorkload applicationWorkload, int x) {
		double sum = 0d;
		if (applicationWorkload != null) {
			sum = applicationWorkload.getVector()[x];
		}
		
		for (int i = 0; i < benchmarkWorkloads.size(); i++) {
			sum += benchmarkWorkloads.get(i).getVector()[x];
		}
		
		return sum / (benchmarkWorkloads.size() + 1);
	}
	
	private double calculateStandardDeviation(ArrayList<BenchmarkWorkload> benchmarkWorkloads, ApplicationWorkload applicationWorkload, int x) {
		double mean = calculateMeanValue(benchmarkWorkloads, applicationWorkload, x);
		
		double sum = 0d;
		if (applicationWorkload != null) {
			sum = Math.pow(applicationWorkload.getVector()[x] - mean, 2);
		}
		for (int i = 0; i < benchmarkWorkloads.size(); i++) {
			sum += Math.pow(benchmarkWorkloads.get(i).getVector()[x] - mean, 2);
		}
		
		sum = sum / (benchmarkWorkloads.size() + 1);
		return Math.sqrt(sum);
	}
	
	private void normalizeWorkloadsByDeviation(ArrayList<BenchmarkWorkload> benchmarkWorkloads, ApplicationWorkload applicationWorkload) {
		double mean = 0d, deviation = 0d;
		
		for (int i = 0; i < Constants.DEFAULT_TRAINING_VECTOR_SIZE; i++) {
			if (applicationWorkload != null) {
				mean = calculateMeanValue(benchmarkWorkloads, applicationWorkload, i);
				deviation = calculateStandardDeviation(benchmarkWorkloads, applicationWorkload, i);
			}
			
			if (deviation != 0) {
				if (applicationWorkload != null) {
					applicationWorkload.getVector()[i] = (applicationWorkload.getVector()[i] - mean) / deviation;
				}
				for (int j = 0; j < benchmarkWorkloads.size(); j++) {
					benchmarkWorkloads.get(j).getVector()[i] = (benchmarkWorkloads.get(j).getVector()[i] - mean) / deviation;
				}
			}
		}
	}
	
	private void normalizeWorkloadsByDeviationAbs(ArrayList<BenchmarkWorkload> benchmarkWorkloads, ApplicationWorkload applicationWorkload) {
		double mean = 0d, deviation = 0d;
		
		for (int i = 0; i < Constants.DEFAULT_TRAINING_VECTOR_SIZE; i++) {
			if (applicationWorkload != null) {
				mean = calculateMeanValue(benchmarkWorkloads, applicationWorkload, i);
				deviation = calculateStandardDeviation(benchmarkWorkloads, applicationWorkload, i);
			}
			
			if (deviation != 0) {
				if (applicationWorkload != null) {
					applicationWorkload.getVector()[i] = Math.abs(applicationWorkload.getVector()[i] - mean) / deviation;
				}
				for (int j = 0; j < benchmarkWorkloads.size(); j++) {
					benchmarkWorkloads.get(j).getVector()[i] = Math.abs(benchmarkWorkloads.get(j).getVector()[i] - mean) / deviation;
				}
			}
		}
	}
	
	private void normalizeWorkloadsByDeviationPow(ArrayList<BenchmarkWorkload> benchmarkWorkloads, ApplicationWorkload applicationWorkload) {
		double mean = 0d, deviation = 0d;
		
		for (int i = 0; i < Constants.DEFAULT_TRAINING_VECTOR_SIZE; i++) {
			if (applicationWorkload != null) {
				mean = calculateMeanValue(benchmarkWorkloads, applicationWorkload, i);
				deviation = calculateStandardDeviation(benchmarkWorkloads, applicationWorkload, i);
			}
			
			if (deviation != 0) {
				if (applicationWorkload != null) {
					applicationWorkload.getVector()[i] = Math.pow(applicationWorkload.getVector()[i] - mean, 2) / deviation;
				}
				for (int j = 0; j < benchmarkWorkloads.size(); j++) {
					benchmarkWorkloads.get(j).getVector()[i] = Math.pow(benchmarkWorkloads.get(j).getVector()[i] - mean, 2) / deviation;
				}
			}
		}
	}
	
	private void normalizeWorkloads(ArrayList<BenchmarkWorkload> benchmarkWorkloads, ApplicationWorkload applicationWorkload) {
		double min = Double.MAX_VALUE, max = 0d, val;
		
		for (int i = 0; i < Constants.DEFAULT_TRAINING_VECTOR_SIZE; i++) {
			if (applicationWorkload != null) {
				min = applicationWorkload.getVector()[i];
				max = applicationWorkload.getVector()[i];
			}
			
			//min = Double.MAX_VALUE;
			//max = Double.MIN_VALUE;
			for (int j = 0; j < benchmarkWorkloads.size(); j++) {
				val = benchmarkWorkloads.get(j).getVector()[i];
				if (val < min) {
					min = val;
				}
				if (val > max) {
					max = val;
				}
			}
			
			if (max != min) {
				if (applicationWorkload != null) {
					applicationWorkload.getVector()[i] = (applicationWorkload.getVector()[i] - min) / (max - min);
				}
				for (int j = 0; j < benchmarkWorkloads.size(); j++) {
					benchmarkWorkloads.get(j).getVector()[i] = (benchmarkWorkloads.get(j).getVector()[i] - min) / (max - min);
				}
			}
		}
	}
	
	//FOT: new method added for Provider mode
	public String calculateClassification(String vm_id, Connection connection) throws Exception {
		
		connection = DbConnection.getConnection();
		DatabaseProfileReader appReader  = new DatabaseProfileReader (vm_id);
		ApplicationWorkload applicationWorkload = appReader.getWorkloadFromDB(connection);
		DatabaseProfileReader benchReader = new DatabaseProfileReader();
		ArrayList<BenchmarkWorkload> benchmarkWorkloads = benchReader.getWorkloadsFromDB(connection);
		ApplicationWorkload tmp = applicationWorkload;

		//applicationWorkload = null;
		switch (normalization) {
		case DeviationNormalizationPow:
			normalizeWorkloadsByDeviationPow(benchmarkWorkloads, applicationWorkload);
			break;
		case DeviationNormalizationAbs:
			normalizeWorkloadsByDeviationAbs(benchmarkWorkloads, applicationWorkload);
			break;
		case DeviationNormalization:
			normalizeWorkloadsByDeviation(benchmarkWorkloads, applicationWorkload);
			break;
		case RangeNormalization:
			normalizeWorkloads(benchmarkWorkloads, applicationWorkload);
			break;
		default:
			break;
		}
		applicationWorkload = tmp;
		//System.out.println("Normalized values of Benchmark dataset:");
		//for (int i = 0; i < benchmarkWorkloads.size(); i++) {
		//	System.out.println(benchmarkWorkloads.get(i).getValuesToString());
		//}
		//System.out.println("\n\n\n");
		List<BenchmarkWorkload> knn = null;
		if (benchmarkWorkloads.size() < k) {
			knn = calculate_knn(benchmarkWorkloads.size(), applicationWorkload, benchmarkWorkloads);
		}
		else {
			knn = calculate_knn(k, applicationWorkload, benchmarkWorkloads);
		}
		
		
		String queryClassification = getWorkloadClassification(applicationWorkload, knn);
		if (queryClassification == null) {
			System.out.println("classification failed");
		}
		else {
			//System.out.println("To kontinotero workload einai to: " + queryClassification);
		}
		
		return queryClassification;
	}
	
	public String calculateClassification() throws Exception {
		ApplicationProfileReader appReader = new ApplicationProfileReader(pidstatFileName, tsharkFileName);
		ApplicationWorkload applicationWorkload = appReader.getWorkloadFromFiles();
		
		BenchmarkProfileReader benchReader = new BenchmarkProfileReader(trainingFileName);
		ArrayList<BenchmarkWorkload> benchmarkWorkloads = benchReader.getWorkloadsFromFile();
		ApplicationWorkload tmp = applicationWorkload;
		//applicationWorkload = null;
		switch (normalization) {
		case DeviationNormalizationPow:
			normalizeWorkloadsByDeviationPow(benchmarkWorkloads, applicationWorkload);
			break;
		case DeviationNormalizationAbs:
			normalizeWorkloadsByDeviationAbs(benchmarkWorkloads, applicationWorkload);
			break;
		case DeviationNormalization:
			normalizeWorkloadsByDeviation(benchmarkWorkloads, applicationWorkload);
			break;
		case RangeNormalization:
			normalizeWorkloads(benchmarkWorkloads, applicationWorkload);
			break;
		default:
			break;
		}
		applicationWorkload = tmp;
		//System.out.println("Normalized values of Benchmark dataset:");
		//for (int i = 0; i < benchmarkWorkloads.size(); i++) {
		//	System.out.println(benchmarkWorkloads.get(i).getValuesToString());
		//}
		//System.out.println("\n\n\n");
		List<BenchmarkWorkload> knn = null;
		if (benchmarkWorkloads.size() < k) {
			knn = calculate_knn(benchmarkWorkloads.size(), applicationWorkload, benchmarkWorkloads);
		}
		else {
			knn = calculate_knn(k, applicationWorkload, benchmarkWorkloads);
		}
		
		
		String queryClassification = getWorkloadClassification(applicationWorkload, knn);
		if (queryClassification == null) {
			System.out.println("classification failed");
		}
		else {
			//System.out.println("To kontinotero workload einai to: " + queryClassification);
		}
		
		return queryClassification;
	}

	public SimilarityMeasure getSimilarity() {
		return similarity;
	}

	public void setSimilarity(SimilarityMeasure similarity) {
		this.similarity = similarity;
	}

	public NormalizationType getNormalization() {
		return normalization;
	}

	public void setNormalization(NormalizationType normalization) {
		this.normalization = normalization;
	}
}
