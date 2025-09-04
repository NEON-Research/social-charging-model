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
	ArrayList<double[]> leftWhileChargingWithDelayedAccess_perDay;
	ArrayList<double[]> percSatisfiedChargingSessions_perDay;
	ArrayList<double[]> chargingSessions_perDay;
	ArrayList<double[]> requiredChargingSessions_perDay;
	
	ArrayList<double[]> leftWhileCharging_rollingAvg;
	ArrayList<double[]> leftUncharged_rollingAvg;
	ArrayList<double[]> outOfModelCharging_rollingAvg;
	ArrayList<double[]> leftWhileChargingWithDelayedAccess_rollingAvg;
	ArrayList<double[]> percSatisfiedChargingSessions_rollingAvg;
	ArrayList<double[]> chargingSessions_rollingAvg;
	ArrayList<double[]> requiredChargingSessions_rollingAvg;
	
	ArrayList<double[]> leftWhileCharging_perWeek;
	ArrayList<double[]> leftUncharged_perWeek;
	ArrayList<double[]> outOfModelCharging_perWeek;
	ArrayList<double[]> leftWhileChargingWithDelayedAccess_perWeek;
	ArrayList<double[]> percSatisfiedChargingSessions_perWeek;
		
	int window = 7;
	int days;
	
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
    
    public ArrayList<double[]> getLeftWhileChargingRollingAvg() {
        return leftWhileCharging_rollingAvg;
    }
    
    public ArrayList<double[]> getLeftUnchargedRollingAvg() {
        return leftUncharged_rollingAvg;
    }

    public ArrayList<double[]> getOutOfModelChargingRollingAvg() {
        return outOfModelCharging_rollingAvg;
    }
    
    public ArrayList<double[]> getPercSatisfiedChargingSessionsRollingAvg() {
        return percSatisfiedChargingSessions_rollingAvg;
    }
    
    public ArrayList<double[]> getChargingSessionsRollingAvg() {
        return chargingSessions_rollingAvg;
    }
    
    public ArrayList<double[]> getRequiredChargingSessionsRollingAvg() {
        return requiredChargingSessions_rollingAvg;
    }
    
    public ArrayList<double[]> getLeftWhileChargingPerWeek() {
        return leftWhileCharging_perWeek;
    }
    
    public ArrayList<double[]> getLeftUnchargedPerWeek() {
        return leftUncharged_perWeek;
    }

    public ArrayList<double[]> getOutOfModelChargingPerWeek() {
        return outOfModelCharging_perWeek;
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
    	chargingSessions_rollingAvg = getRollingAverage(window, days, uncertaintyBounds);
    	//leftWhileCharging_perWeek = getStatsPerWeek(uncertaintyBounds);
    }
    
    public void setRequiredChargingSessionsPerDay( ArrayList<double[]> uncertaintyBounds ) {
    	requiredChargingSessions_perDay = uncertaintyBounds;
    	days = requiredChargingSessions_perDay.get(0).length;
    	requiredChargingSessions_rollingAvg = getRollingAverage(window, days, uncertaintyBounds);
    	//leftWhileCharging_perWeek = getStatsPerWeek(uncertaintyBounds);
    }
    
    public void setLeftWhileChargingPerDay( ArrayList<double[]> uncertaintyBounds ) {
    	leftWhileCharging_perDay = uncertaintyBounds;
    	days = leftWhileCharging_perDay.get(0).length;
    	leftWhileCharging_rollingAvg = getRollingAverage(window, days, uncertaintyBounds);
    	leftWhileCharging_perWeek = getStatsPerWeek(uncertaintyBounds);
    	
    }
    
    public void setLeftWhileChargingWithDelayedAccessPerDay( ArrayList<double[]> uncertaintyBounds ) {
    	leftWhileChargingWithDelayedAccess_perDay = uncertaintyBounds;
    	days = leftWhileChargingWithDelayedAccess_perDay.get(0).length;
    	leftWhileChargingWithDelayedAccess_rollingAvg = getRollingAverage(window, days, uncertaintyBounds);
    	leftWhileChargingWithDelayedAccess_perWeek = getStatsPerWeek(uncertaintyBounds);
    	
    }
    
    public void setLeftUnchargedPerDay( ArrayList<double[]> uncertaintyBounds ) {
    	leftUncharged_perDay = uncertaintyBounds;
    	days = uncertaintyBounds.get(0).length;
    	leftUncharged_rollingAvg = getRollingAverage(window, days, uncertaintyBounds);
    	leftUncharged_perWeek = getStatsPerWeek(uncertaintyBounds);
    }
    
    public void setOutOfModelChargingPerDay( ArrayList<double[]> uncertaintyBounds ) {
    	outOfModelCharging_perDay = uncertaintyBounds;
    	days = uncertaintyBounds.get(0).length;
    	outOfModelCharging_rollingAvg = getRollingAverage(window, days, uncertaintyBounds);
    	outOfModelCharging_perWeek = getStatsPerWeek(uncertaintyBounds);
    }
    
    public void setPercSatisfiedChargingSessionsPerDay( ArrayList<double[]> uncertaintyBounds ) {
    	percSatisfiedChargingSessions_perDay = uncertaintyBounds;
    	days = percSatisfiedChargingSessions_perDay.get(0).length;
    	percSatisfiedChargingSessions_rollingAvg = getRollingAverage(window, days, uncertaintyBounds);
    	percSatisfiedChargingSessions_perWeek = getStatsPerWeek(uncertaintyBounds);
    	
    }
       
    
    public ArrayList<double[]> getRollingAverage(int window, int days, ArrayList<double[]> timeseriesStats) {
    	
    	ArrayList<double[]> rollingAvgStats = new ArrayList<double[]>();
    	for(double[] timeseries : timeseriesStats) {
    		
      	double sumValue = 0;
	    	double[] rollingAverage = new double[days];
	    	
	    	for(int i = 0; i < days; i++) {
	    		double value = timeseries[i];
	    		sumValue += value;
	    		
	    		if(i >= window) {
	    			sumValue -= timeseries[i - window];
	    		}
	    		if(i >= window - 1) {
	    			rollingAverage[i] = sumValue / window;
	    		}
	    		else {
	    			rollingAverage[i] = 0.0;
	    		}
	    	}
	    	
	    	rollingAvgStats.add(rollingAverage);
    	}
    	
    return rollingAvgStats;
    }
    
    public ArrayList<double[]> getStatsPerWeek( ArrayList<double[]> timeseriesStats) {
    	ArrayList<double[] >perWeekStats = new ArrayList<double[]>();
    	int days = timeseriesStats.get(0).length;
    	int weeks = (int) floor(days/7);
    	
    	for( double[] timeseries : timeseriesStats) {
    		double sumWeek = 0;
    		double[] weekStat = new double[weeks];
    		int w = 0;
    		
    		for(int i = 0; i < days; i++ ) {
    			sumWeek += timeseries[i];
    		
    			if((i + 1) % 7 == 0) {
    				weekStat[w] = sumWeek;
    				sumWeek = 0;
    				w++;
    			}
    		}
    		
    		perWeekStats.add(weekStat);
    	}
    	return perWeekStats;
    }
    

	@Override
	public String toString() {
		return super.toString();
	}

}