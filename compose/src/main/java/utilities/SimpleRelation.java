package utilities;

public class SimpleRelation {
	
	private String relation;
	private double confidence;
	
	public SimpleRelation(String relation, double confidence) {
		super();
		this.relation = relation;
		this.confidence = confidence;
	}
	
	public SimpleRelation() {}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}
	
	
	
	

}
