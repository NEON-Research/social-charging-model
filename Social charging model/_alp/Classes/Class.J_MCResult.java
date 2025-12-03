/**
 * J_MCResult
 */	

import java.lang.reflect.Field;
import java.util.ArrayList;

public class J_MCResult {

	private int iterations;
	private int scenarioIndex;
	private int simulationRun;
	
	private boolean b1;
	private boolean b2;
	private boolean b3;
	private boolean b4;
	private double EVsPerCP;
	
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
	ArrayList<double[]> chargingSessions_perWeek;
	ArrayList<double[]> requiredChargingSessions_perWeek;
	
	ArrayList<double[]> behaviour1_perWeek;
	ArrayList<double[]> behaviour2_perWeek;
	ArrayList<double[]> behaviour3_perWeek;
	ArrayList<double[]> successfulBehaviour1_perWeek;
	ArrayList<double[]> successfulBehaviour2_perWeek;
	ArrayList<double[]> successfulBehaviour3_perWeek;
	ArrayList<double[]> unsuccessfulBehaviour1_perWeek;
	ArrayList<double[]> unsuccessfulBehaviour2_perWeek;
	ArrayList<double[]> unsuccessfulBehaviour3_perWeek;
	ArrayList<double[]> kmDriven_perWeek;
	ArrayList<double[]> tripsPerWeek;
	
	ArrayList<double[]> pcp_perWeek;
	ArrayList<double[]> rc_perWeek;
	ArrayList<double[]> psi_perWeek;
	ArrayList<double[]> norm1_perWeek;
	ArrayList<double[]> norm2_perWeek;		
	ArrayList<double[]> norm3_perWeek;
	
	Map<String, double[]> chargingSatisfactionMap;
	Map<String, double[]> chargingSessionsMap;
	Map<String, double[]> requiredChargingSessionsMap;
	Map<String, double[]> tripsMap;
	Map<String, double[]> kmDrivenMap;
	
	int window = 14;
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
    
    public int getSimulationRun() {
    	return simulationRun;
    }
    
    public boolean getB1() {
    	return b1;
    }
    
    public boolean getB2() {
    	return b2;
    }

    public boolean getB3() {
    	return b3;
    }

    public boolean getB4() {
    	return b4;
    }

    public double getEVsPerCP() {
    	return EVsPerCP;
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
    /*
    public ArrayList<double[]> getLeftWhileChargingPerDay() {
        return leftWhileCharging_perDay;
    }*/
    /*
    public ArrayList<double[]> getLeftUnchargedPerDay() {
        return leftUncharged_perDay;
    }*/
    /*
    public ArrayList<double[]> getOutOfModelChargingPerDay() {
        return outOfModelCharging_perDay;
    }*/
    
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
    /*
    public ArrayList<double[]> getPercSatisfiedChargingSessionsPerDay() {
        return percSatisfiedChargingSessions_perDay;
    }*/
    
    public ArrayList<double[]> getChargingSessionsRollingAvg() {
        return chargingSessions_rollingAvg;
    }
    /*
    public ArrayList<double[]> getChargingSessionsPerDay() {
        return chargingSessions_perDay;
    }*/
    
    public ArrayList<double[]> getRequiredChargingSessionsRollingAvg() {
        return requiredChargingSessions_rollingAvg;
    }
    /*
    public ArrayList<double[]> getRequiredChargingSessionsPerDay() {
        return requiredChargingSessions_perDay;
    }*/
    
    public ArrayList<double[]> getLeftWhileChargingPerWeek() {
        return leftWhileCharging_perWeek;
    }
    
    public ArrayList<double[]> getLeftUnchargedPerWeek() {
        return leftUncharged_perWeek;
    }

    public ArrayList<double[]> getOutOfModelChargingPerWeek() {
        return outOfModelCharging_perWeek;
    }
    
    public ArrayList<double[]> getPercSatisfiedChargingSessionsPerWeek() {
        return percSatisfiedChargingSessions_perWeek;
    }

    public ArrayList<double[]> getChargingSessionsPerWeek() {
        return chargingSessions_perWeek;
    }
    
    public ArrayList<double[]> getRequiredChargingSessionsPerWeek() {
        return requiredChargingSessions_perWeek;
    }
    
    public ArrayList<double[]> getPCP() {
        return pcp_perWeek;
    }
    
    public ArrayList<double[]> getRC() {
        return rc_perWeek;
    }
    
    public ArrayList<double[]> getPSI() {
        return psi_perWeek;
    }
    
    public ArrayList<double[]> getNorm1() {
        return norm1_perWeek;
    }
    
    public ArrayList<double[]> getNorm2() {
        return norm2_perWeek;
    }
    
    public ArrayList<double[]> getNorm3() {
        return norm3_perWeek;
    }
    
    public Map<String, double[]> getChargingSatisfactionMap() {
        return this.chargingSatisfactionMap;
    }

    public Map<String, double[]> getChargingSessionsMap() {
        return this.chargingSessionsMap;
    }

    public Map<String, double[]> getRequiredChargingSessionsMap() {
        return this.requiredChargingSessionsMap;
    }

    public Map<String, double[]> getTripsMap() {
        return this.tripsMap;
    }

    public Map<String, double[]> getKMDMap() {
        return this.kmDrivenMap;
    }
    
    
    
    
    // --- Setters ---
    public void setIterations(int iterations) {
        this.iterations = iterations;
    }
    
    public void setScenarioIndex(int scenIndex) {
    	this.scenarioIndex = scenIndex;
    }
    
    public void setSimulationRunt(int simRun) {
    	this.simulationRun = simRun;
    }
    
    public void setB1(boolean val) {
    	this.b1 = val;
    }
    
    public void setB2(boolean val) {
    	this.b2 = val;
    }

    public void setB3(boolean val) {
    	this.b3 = val;
    }
    
    public void setB4(boolean val) {
    	this.b4 = val;
    }
    
    public void setEVsPerCP(double val) {
    	this.EVsPerCP = val;
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
    
    public void setPCP( ArrayList<double[]> uncertaintyBounds ) {
    	pcp_perWeek = uncertaintyBounds;
    }
    
    public void setRC( ArrayList<double[]> uncertaintyBounds ) {
    	rc_perWeek = uncertaintyBounds;
    }
    
    public void setPSI( ArrayList<double[]> uncertaintyBounds ) {
    	psi_perWeek = uncertaintyBounds;
    }
    
    public void setNorm1( ArrayList<double[]> uncertaintyBounds ) {
    	norm1_perWeek = uncertaintyBounds;
    }
    public void setNorm2( ArrayList<double[]> uncertaintyBounds ) {
    	norm2_perWeek = uncertaintyBounds;
    }
    public void setNorm3( ArrayList<double[]> uncertaintyBounds ) {
    	norm3_perWeek = uncertaintyBounds;
    }
    
    public void setChargingSessionsPerWeek( ArrayList<double[]> uncertaintyBounds ) {
    	chargingSessions_perWeek = uncertaintyBounds;
    	//days = chargingSessions_perDay.get(0).length;
    	//chargingSessions_rollingAvg = getRollingAverage(window, days, uncertaintyBounds);
    	//chargingSessions_perWeek = getStatsPerWeek(uncertaintyBounds);
    }
    
    public void setRequiredChargingSessionsPerWeek( ArrayList<double[]> uncertaintyBounds ) {
    	requiredChargingSessions_perWeek = uncertaintyBounds;
    	//days = requiredChargingSessions_perDay.get(0).length;
    	//requiredChargingSessions_rollingAvg = getRollingAverage(window, days, uncertaintyBounds);
    	//requiredChargingSessions_perWeek = getStatsPerWeek(uncertaintyBounds);
    }
    
    public void setLeftWhileChargingPerWeek( ArrayList<double[]> uncertaintyBounds ) {
    	leftWhileCharging_perWeek = uncertaintyBounds;
    	//days = leftWhileCharging_perDay.get(0).length;
    	//leftWhileCharging_rollingAvg = getRollingAverage(window, days, uncertaintyBounds);
    	//leftWhileCharging_perWeek = getStatsPerWeek(uncertaintyBounds);
    	
    }
    
    public void setLeftWhileChargingWithDelayedAccessPerWeek( ArrayList<double[]> uncertaintyBounds ) {
    	leftWhileChargingWithDelayedAccess_perWeek = uncertaintyBounds;
    	//days = leftWhileChargingWithDelayedAccess_perDay.get(0).length;
    	//leftWhileChargingWithDelayedAccess_rollingAvg = getRollingAverage(window, days, uncertaintyBounds);
    	//leftWhileChargingWithDelayedAccess_perWeek = getStatsPerWeek(uncertaintyBounds);
    	
    }
    
    public void setLeftUnchargedPerWeek( ArrayList<double[]> uncertaintyBounds ) {
    	leftUncharged_perWeek = uncertaintyBounds;
    	//days = uncertaintyBounds.get(0).length;
    	//leftUncharged_rollingAvg = getRollingAverage(window, days, uncertaintyBounds);
    	//leftUncharged_perWeek = getStatsPerWeek(uncertaintyBounds);
    }
    
    public void setOutOfModelChargingPerWeek( ArrayList<double[]> uncertaintyBounds ) {
    	outOfModelCharging_perWeek = uncertaintyBounds;
    	//days = uncertaintyBounds.get(0).length;
    	//outOfModelCharging_rollingAvg = getRollingAverage(window, days, uncertaintyBounds);
    	//outOfModelCharging_perWeek = getStatsPerWeek(uncertaintyBounds);
    }
    
    public void setPercSatisfiedChargingSessionsPerWeek( ArrayList<double[]> uncertaintyBounds ) {
    	percSatisfiedChargingSessions_perWeek = uncertaintyBounds;
    	//days = percSatisfiedChargingSessions_perDay.get(0).length;
    	//percSatisfiedChargingSessions_rollingAvg = getRollingAverage(window, days, uncertaintyBounds);
    	//percSatisfiedChargingSessions_perWeek = getStatsPerWeek(uncertaintyBounds);
    	
    }
    
    public ArrayList<double[]> getBehaviour1PerWeek() {
        return behaviour1_perWeek;
    }

    public void setBehaviour1PerWeek(ArrayList<double[]> behaviour1_perWeek) {
        this.behaviour1_perWeek = behaviour1_perWeek;
    }

    public ArrayList<double[]> getBehaviour2PerWeek() {
        return behaviour2_perWeek;
    }

    public void setBehaviour2PerWeek(ArrayList<double[]> behaviour2_perWeek) {
        this.behaviour2_perWeek = behaviour2_perWeek;
    }

    public ArrayList<double[]> getBehaviour3PerWeek() {
        return behaviour3_perWeek;
    }

    public void setBehaviour3PerWeek(ArrayList<double[]> behaviour3_perWeek) {
        this.behaviour3_perWeek = behaviour3_perWeek;
    }

    public ArrayList<double[]> getSuccessfulBehaviour1PerWeek() {
        return successfulBehaviour1_perWeek;
    }

    public void setSuccessfulBehaviour1PerWeek(ArrayList<double[]> successfulBehaviour1_perWeek) {
        this.successfulBehaviour1_perWeek = successfulBehaviour1_perWeek;
    }

    public ArrayList<double[]> getSuccessfulBehaviour2PerWeek() {
        return successfulBehaviour2_perWeek;
    }

    public void setSuccessfulBehaviour2PerWeek(ArrayList<double[]> successfulBehaviour2_perWeek) {
        this.successfulBehaviour2_perWeek = successfulBehaviour2_perWeek;
    }

    public ArrayList<double[]> getSuccessfulBehaviour3PerWeek() {
        return successfulBehaviour3_perWeek;
    }

    public void setSuccessfulBehaviour3PerWeek(ArrayList<double[]> successfulBehaviour3_perWeek) {
        this.successfulBehaviour3_perWeek = successfulBehaviour3_perWeek;
    }
    
    public ArrayList<double[]> getUnsuccessfulBehaviour1PerWeek() {
        return unsuccessfulBehaviour1_perWeek;
    }

    public void setUnsuccessfulBehaviour1PerWeek(ArrayList<double[]> unsuccessfulBehaviour1_perWeek) {
        this.unsuccessfulBehaviour1_perWeek = unsuccessfulBehaviour1_perWeek;
    }

    public ArrayList<double[]> getUnsuccessfulBehaviour2PerWeek() {
        return unsuccessfulBehaviour2_perWeek;
    }

    public void setUnsuccessfulBehaviour2PerWeek(ArrayList<double[]> unsuccessfulBehaviour2_perWeek) {
        this.unsuccessfulBehaviour2_perWeek = unsuccessfulBehaviour2_perWeek;
    }

    public ArrayList<double[]> getUnsuccessfulBehaviour3PerWeek() {
        return unsuccessfulBehaviour3_perWeek;
    }

    public void setUnsuccessfulBehaviour3PerWeek(ArrayList<double[]> unsuccessfulBehaviour3_perWeek) {
        this.unsuccessfulBehaviour3_perWeek = unsuccessfulBehaviour3_perWeek;
    }

    public ArrayList<double[]> getKmDrivenPerWeek() {
        return kmDriven_perWeek;
    }

    public void setKmDrivenPerWeek(ArrayList<double[]> kmDriven_perWeek) {
        this.kmDriven_perWeek = kmDriven_perWeek;
    }
    
    public ArrayList<double[]> getTripsPerWeek() {
        return tripsPerWeek;
    }

    public void setTripsPerWeek(ArrayList<double[]> tripsPerWeek) {
        this.tripsPerWeek = tripsPerWeek;
    }
    
    public void setChargingSatisfactionMap(Map<String, double[]> resultsMap) {
    	this.chargingSatisfactionMap = resultsMap;
    }
    public void setChargingSessionsMap(Map<String, double[]> resultsMap) {
    	this.chargingSessionsMap = resultsMap;
    }
    public void setRequiredChargingSessionsMap(Map<String, double[]> resultsMap) {
    	this.requiredChargingSessionsMap = resultsMap;
    }
    public void setTripsMap(Map<String, double[]> resultsMap) {
    	this.tripsMap = resultsMap;
    }
    public void setKMDMap(Map<String, double[]> resultsMap) {
    	this.kmDrivenMap = resultsMap;
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
    /*
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
    				double weekAverage = sumWeek / 7;
    				weekStat[w] = weekAverage;
    				sumWeek = 0;
    				w++;
    			}
    		}
    		perWeekStats.add(weekStat);
    	}
    	return perWeekStats;
    }*/
    
    public void clear() {
        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.getType() == ArrayList.class) {
                field.setAccessible(true);
                try {
                    field.set(this, null);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    
    
	@Override
	public String toString() {
		return super.toString();
	}

}