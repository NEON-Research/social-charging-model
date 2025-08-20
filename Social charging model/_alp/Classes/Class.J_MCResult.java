/**
 * J_MCResult
 */	
public class J_MCResult {

	int iterations;
	int scenarioIndex;
	
	ArrayList<double[]> successRate_b1;
	ArrayList<double[]> successRate_b2;
	ArrayList<double[]> successRate_b3;
	
	ArrayList<double[]> avgProb_b1;
	ArrayList<double[]> avgProb_b2;
	ArrayList<double[]> avgProb_b3;
	
	ArrayList<double[]> leftWhileCharging_perDay;
	ArrayList<double[]> leftUncharged_perDay;
	ArrayList<double[]> outOfModelCharging_perDay;
    /**
     * Default constructor
     */
    public J_MCResult() {
    }
    
    // --- Getters ---
    public int getIterations() {
        return iterations;
    }
    
    public int getScenarioIndex() {
    	return scenarioIndex;
    }

    public ArrayList<double[]> getSuccessRate_b1() {
        return successRate_b1;
    }

    public ArrayList<double[]> getSuccessRate_b2() {
        return successRate_b2;
    }

    public ArrayList<double[]> getSuccessRate_b3() {
        return successRate_b3;
    }

    public ArrayList<double[]> getAvgProb_b1() {
        return avgProb_b1;
    }

    public ArrayList<double[]> getAvgProb_b2() {
        return avgProb_b2;
    }

    public ArrayList<double[]> getAvgProb_b3() {
        return avgProb_b3;
    }

    public ArrayList<double[]> getLeftWhileChargingPerDay() {
        return leftWhileCharging_perDay;
    }
    
    public ArrayList<double[]> getLeftUnchargedPerDay() {
        return leftUncharged_perDay;
    }

    public ArrayList<double[]> getOutOfModelChargingPerDay() {
        return outOfModelCharging_perDay;
    }

    // --- Setters ---
    public void setIterations(int iterations) {
        this.iterations = iterations;
    }
    
    public void setScenarioIndex(int scenIndex) {
    	this.scenarioIndex = scenIndex;
    }
    
    public void setSuccessRate_b1( ArrayList<double[]> uncertaintyBounds ) {
    	successRate_b1 = uncertaintyBounds;
    }
    
    public void setSuccessRate_b2( ArrayList<double[]> uncertaintyBounds ) {
    	successRate_b2 = uncertaintyBounds;
    }
    
    public void setSuccessRate_b3( ArrayList<double[]> uncertaintyBounds ) {
    	successRate_b3 = uncertaintyBounds;
    }
    
    public void setAvgProb_b1( ArrayList<double[]> uncertaintyBounds ) {
    	avgProb_b1 = uncertaintyBounds;
    }
    
    public void setAvgProb_b2( ArrayList<double[]> uncertaintyBounds ) {
    	avgProb_b2 = uncertaintyBounds;
    }
    
    public void setAvgProb_b3( ArrayList<double[]> uncertaintyBounds ) {
    	avgProb_b3 = uncertaintyBounds;
    }
    
    public void setLeftWhileChargingPerDay( ArrayList<double[]> uncertaintyBounds ) {
    	leftWhileCharging_perDay = uncertaintyBounds;
    }
    
    public void setLeftUnchargedPerDay( ArrayList<double[]> uncertaintyBounds ) {
    	leftUncharged_perDay = uncertaintyBounds;
    }
    
    public void setOutOfModelChargingPerDay( ArrayList<double[]> uncertaintyBounds ) {
    	outOfModelCharging_perDay = uncertaintyBounds;
    }

	@Override
	public String toString() {
		return super.toString();
	}

}