package eu.artist.migration.ct.classifier;

public class BenchmarkWorkload extends Workload implements Comparable<BenchmarkWorkload> {
	private String name;
	private double distanceFromRequest;
	
	public BenchmarkWorkload(String name) {
		super();
		this.name = name;
		distanceFromRequest = 0;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public double getDistanceFromRequest() {
		return distanceFromRequest;
	}

	public void setDistanceFromRequest(double distanceFromRequest) {
		this.distanceFromRequest = distanceFromRequest;
	}
	
	@Override
	public String toString() {
		return "Workload: " + name + "\tDistance: " + distanceFromRequest;
	}
	
	public String getValuesToString() {
		StringBuilder builder = new StringBuilder(name);
		for (int i = 0; i < vector.length; i++) {
			builder.append("\t");
			builder.append(String.format("%.10f", vector[i]));
		}
		return builder.toString();
	}
	
	@Override
	public int compareTo(BenchmarkWorkload other) {
		if (this.getDistanceFromRequest() < other.getDistanceFromRequest()) {
			return -1;
		}
		else if (this.getDistanceFromRequest() == other.getDistanceFromRequest()) {
			return 0;
		}
		return 1;
	}
}
