package eu.artist.migration.ct.controller;

//FOT: Class created for CloudPerfect project
public class ServiceOffer {
	
	public String provider_size;
	public double score;
	public double price;
	public double offerTotalScore;
	
	public ServiceOffer(String provider_size, double score, double price){
		this.provider_size = provider_size;
		this.score=score;
		this.price = price;
	}


}
