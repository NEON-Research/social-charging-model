/**
 * J_MCResult
 */	
public class J_SensitivityResult {

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
	ArrayList<double[]> leftWhileChargingWithDelayedAccess_perDay;
	ArrayList<double[]> percSatisfiedChargingSessions_perDay;
	ArrayList<double[]> chargingSessions_perDay;
	ArrayList<double[]> requiredChargingSessions_perDay;
			
	int window = 14;
	int days;
	
    /**
     * Default constructor
     */
    public J_SensitivityResult() {
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
    
    public void setChargingSessionsPerDay( ArrayList<double[]> uncertaintyBounds ) {
    	chargingSessions_perDay = uncertaintyBounds;
    	days = chargingSessions_perDay.get(0).length;
    }
    
    public void setRequiredChargingSessionsPerDay( ArrayList<double[]> uncertaintyBounds ) {
    	requiredChargingSessions_perDay = uncertaintyBounds;
    	days = requiredChargingSessions_perDay.get(0).length;
 
    }
    
    public void setLeftWhileChargingPerDay( ArrayList<double[]> uncertaintyBounds ) {
    	leftWhileCharging_perDay = uncertaintyBounds;
    	days = leftWhileCharging_perDay.get(0).length;   	
    }
    
    public void setLeftWhileChargingWithDelayedAccessPerDay( ArrayList<double[]> uncertaintyBounds ) {
    	leftWhileChargingWithDelayedAccess_perDay = uncertaintyBounds;
    	days = leftWhileChargingWithDelayedAccess_perDay.get(0).length;  	
    }
    
    public void setLeftUnchargedPerDay( ArrayList<double[]> uncertaintyBounds ) {
    	leftUncharged_perDay = uncertaintyBounds;
    	days = uncertaintyBounds.get(0).length;
    }
    
    public void setOutOfModelChargingPerDay( ArrayList<double[]> uncertaintyBounds ) {
    	outOfModelCharging_perDay = uncertaintyBounds;
    	days = uncertaintyBounds.get(0).length;
     }
    
    public void setPercSatisfiedChargingSessionsPerDay( ArrayList<double[]> uncertaintyBounds ) {
    	percSatisfiedChargingSessions_perDay = uncertaintyBounds;
    	days = percSatisfiedChargingSessions_perDay.get(0).length;
     	
    }
       
    
 

	@Override
	public String toString() {
		return super.toString();
	}

}