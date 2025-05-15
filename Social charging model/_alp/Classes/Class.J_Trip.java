/**
 * J_Trip
 */	
public class J_Trip {
	private double departureTime;
	private double arrivalTime;
	private double distance_km;
    /**
     * Default constructor
     */
    public J_Trip(double dep, double arr, double dist) {
    	this.departureTime = dep;
    	this.arrivalTime = arr;
    	this.distance_km = dist;
    }

    
    /** Setters
     * 
     */
    
    /** Getters
     * 
     */
    public double getDepartureTime() { return departureTime; }
    public double getArrivalTime() { return arrivalTime; }
    public double getDistance_km() { return distance_km; }
    
    @Override
    public String toString() {
        return "J_Trip{" +
               "departureTime=" + departureTime +
               ", arrivalTime=" + arrivalTime +
               ", distance_km=" + distance_km +
               '}';
    }
}