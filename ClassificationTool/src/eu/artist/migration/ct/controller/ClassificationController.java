package eu.artist.migration.ct.controller;

import java.sql.Connection;

import eu.artist.migration.ct.classifier.KnnClassifier;
import eu.artist.migration.ct.common.IniReader;

public class ClassificationController {
	private String pidstatFileName;
	private String tsharkFileName;
	private String trainingFileName;
	private SimilarityMeasure similarity;
	private NormalizationType normalization;
	private String vm_id;


	public ClassificationController(String pidstatFileName, String tsharkFileName, String trainingFileName, SimilarityMeasure similarity, NormalizationType normalization) {
		this.pidstatFileName = pidstatFileName;
		this.tsharkFileName = tsharkFileName;
		this.trainingFileName = trainingFileName;
		this.similarity = similarity;
		this.normalization = normalization;
	}
	
	//FOT: Provider mode
	public ClassificationController(String vm_id, SimilarityMeasure similarity, NormalizationType normalization) {
		this.vm_id = vm_id;
		this.similarity = similarity;
		this.normalization = normalization;
	}

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

	public String calculateClassification() throws Exception {
		IniReader reader = IniReader.getInstance();
		if (reader.getClassificationEngine().matches("knn")) {

			KnnClassifier classifier = new KnnClassifier(pidstatFileName, tsharkFileName, trainingFileName, similarity, normalization);
			return classifier.calculateClassification();
			
		}
		else {
			//Code for neuron networks engine
			return null;
		}
	}
	
	//FOT: if ran on Provider mode, needs SQL connection
	public String calculateClassification(Connection connection) throws Exception {
		IniReader reader = IniReader.getInstance();
		if (reader.getClassificationEngine().matches("knn")) {
			
			KnnClassifier classifier = new KnnClassifier(similarity, normalization);
			return classifier.calculateClassification(vm_id, connection);
		}
		else {
			//Code for neuron networks engine
			return null;
		}
	}

	public String calculateBestProvider(String workloadName) throws Exception {
		//setProviderPrices(getDbConnection());
		ProviderEfficiency pe = new ProviderEfficiency(workloadName);
		return pe.calculateBestProvider(1);
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
