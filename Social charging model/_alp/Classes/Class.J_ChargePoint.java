/**
 * J_ChargePoint
 */	
public class J_ChargePoint {
	//private boolean occupied;
	private EVOwner currentEV = null;
    /**
     * Default constructor
     */
    public J_ChargePoint() {
    }
      
    public boolean isOccupied(){
    	return currentEV != null;
    }
    
    public EVOwner isOccupiedBy() {
    	return currentEV;
    }
    
    public void occupy(EVOwner EV) {
    	currentEV = EV;
    }
    
    public void release() {
    	currentEV = null;
    }
    
    public EVOwner getCurrentEV() {
    	return currentEV;
    }

	@Override
	public String toString() {
		return super.toString();
	}

}