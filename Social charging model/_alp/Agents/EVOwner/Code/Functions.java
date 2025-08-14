double f_chargeCar()
{/*ALCODESTART::1746011766273*/
if(v_chargePoint != null && v_status == PARKED_CHARGE_POINT_CHARGING){	
	// Charging step
    if (v_electricityInBattery_kWh < v_batteryCapacity_kWh) {
        double timestepsPerHour = 60.0 / main.p_timestep_minutes;
        double charge_kWhperTimestep = main.p_chargingPower_kW / timestepsPerHour;

        // How much space is left in the battery?
        double needed_kWh = v_batteryCapacity_kWh - v_electricityInBattery_kWh;

        // Charge only what fits
        double actualCharge = Math.min(charge_kWhperTimestep, needed_kWh);
        v_electricityInBattery_kWh += actualCharge;
        v_totalElectricityCharged_kWh += actualCharge;
    }

    // Always update SOC
    v_soc = v_electricityInBattery_kWh / v_batteryCapacity_kWh;

    // Check for full battery *after* charging
    if (v_electricityInBattery_kWh >= v_batteryCapacity_kWh) {
        v_electricityInBattery_kWh = v_batteryCapacity_kWh;
        v_status = PARKED_CHARGE_POINT_IDLE;
		
		//Execute by after charging
		f_b1_movingVehicle();
    }
}
/*ALCODEEND*/}

double f_updateSOC(double tripDistance_km)
{/*ALCODESTART::1746011780748*/
//Update SOC
double electricityConsumed_kWh = tripDistance_km * v_elecCons_kWhperKm;
v_electricityInBattery_kWh -= electricityConsumed_kWh;

if(v_electricityInBattery_kWh < 0){
	double outOfModelCharging_kWh = abs(v_electricityInBattery_kWh) + v_batteryCapacity_kWh*0.1; //Cars charge to 10% when doing fast charging outside of model
	v_outOfModelCharge_kWh += outOfModelCharging_kWh;
	v_totalElectricityCharged_kWh += outOfModelCharging_kWh;
	v_electricityInBattery_kWh += outOfModelCharging_kWh;
	
	double soc = v_electricityInBattery_kWh / v_batteryCapacity_kWh;
	if (Math.abs(soc - 0.10) > 0.005) {  // ±0.5% tolerance
		traceln("ERROR: soc = " + roundToInt(v_electricityInBattery_kWh / v_batteryCapacity_kWh * 100) + "% after out of model charging, with OoMC = " + outOfModelCharging_kWh + "kWh, elec in bat = " + v_electricityInBattery_kWh + " bat cap " + v_batteryCapacity_kWh);
	}
}

v_soc = v_electricityInBattery_kWh / v_batteryCapacity_kWh;
if(v_soc < v_socChargingThreshold){
	count_chargingRequired++;
}


/*ALCODEEND*/}

double f_setChargingStatus()
{/*ALCODESTART::1747136105778*/
if(v_status == ARRIVING){
	boolean wantsToCharge = v_soc < v_socChargingThreshold;
	Status currentStatus = v_status;
	//boolean foundCPThroughRequest = false;
	
	if(wantsToCharge){
		// Already charging
		if(v_chargePoint != null){
			v_status = PARKED_CHARGE_POINT_CHARGING;
		}
		// Not charging, trying to get CP
		else if( f_tryAcquireChargePoint()){
			v_status = PARKED_CHARGE_POINT_CHARGING;
			count_chargingSessions++;
		}
		//Behaviour 2: Request neighbor to move EV
		else {
			f_b2_requestMove();
		}
	}
	else {
		//If somehow still holding CP
		if( v_chargePoint != null) {
			traceln("Holding CP which should not happen with status " + v_status);
			f_leaveChargePoint();
		}
		v_status = PARKED_NON_CHARGE_POINT_CHARGING_NOT_REQUIRED;
	}
	
	if( v_chargePoint != null && v_chargePoint.getCurrentEV() != this){
		traceln("At EV " + this.getIndex() + " connected charge point is occupied by EV " + v_chargePoint.isOccupiedBy());
	}
	/*
	if(currentStatus == PARKED_NON_CHARGE_POINT_CHARGING_REQUIRED && v_status == PARKED_CHARGE_POINT_CHARGING){
		traceln("EV " + this.getIndex() + " has moved from parking to charge point and started charging");
	}
	*/
	if(v_status == ARRIVING){
		traceln("EV " + this.getIndex() + " with SOC " + v_soc + " and WtC " + wantsToCharge + " and CP " + v_chargePoint + " status is still arriving while end of set charging status");
	}
	
	//For behavior recheck CP
	if( v_status == PARKED_NON_CHARGE_POINT_CHARGING_REQUIRED ){
		lastCheckDayPart = main.f_getPartOfDay();
		lastCheckDay = main.v_day;
		firstCheckDone = false;
	}
}
/*ALCODEEND*/}

double f_b1_movingVehicle()
{/*ALCODESTART::1748950054520*/
//check if within daytime and selected in scenario
if(main.p_b1_moveCar && main.v_withinSocialChargingTimes){
	//act within probability
	boolean actBehavior = f_actBehavior(v_prob_b1);
	///act
	f_moveVehicle(actBehavior);
	
	//Social learning after interaction
	f_socialLearning_b1();
}

/*ALCODEEND*/}

double f_moveVehicle(boolean actBehavior)
{/*ALCODESTART::1753175296086*/
//Move vehicle
if( actBehavior ) {// && main.v_parkingPlacesAvailable > 0 ){
	f_leaveChargePoint();
	v_status = PARKED_NON_CHARGE_POINT_CHARGING_NOT_REQUIRED;
	count_b1_successful++;
	
	//If moved, notify neighbor
	f_b3_notifyNeighbor();
}
else {
	count_b1_notSuccessful++;
}

int total_b1 = count_b1_successful + count_b1_notSuccessful;
successRate_b1 = (total_b1 != 0) ? ((double) count_b1_successful / total_b1) : 0;

//main.hs_data_utility_b1_all.add(utility_b1);
//main.hs_data_b1_all.add(b1);



/*ALCODEEND*/}

double f_b2_requestMove()
{/*ALCODESTART::1753175364935*/
//check if within daytime and selected in scenario
if(main.p_b2_requestMove && main.v_withinSocialChargingTimes){
	//act within probability
	boolean actBehavior = f_actBehavior(v_prob_b2);
	///act
	f_requestMove(actBehavior);
	
	//Social learning after interaction
	f_socialLearning_b2();
}
//No CP available
else {
	v_status = PARKED_NON_CHARGE_POINT_CHARGING_REQUIRED;
}


/*ALCODEEND*/}

double f_requestMove(boolean actBehavior)
{/*ALCODESTART::1753175364937*/
//Get moving EV if available
EVOwner movingEV = f_successfulMoveRequest(actBehavior);

if(movingEV != null){
	movingEV.v_status = PARKED_NON_CHARGE_POINT_CHARGING_NOT_REQUIRED;
	J_ChargePoint freedCP = movingEV.v_chargePoint;
	movingEV.f_leaveChargePoint();
	movingEV.count_fulfilledMoveRequest++;
			
	//Change status of this EV
	v_status = PARKED_CHARGE_POINT_CHARGING;
	v_chargePoint = freedCP;
	v_chargePoint.occupy(this);
	
	count_chargingSessions++;
	count_b2_successful++;
}
else {
	v_status = PARKED_NON_CHARGE_POINT_CHARGING_REQUIRED;
	count_b2_notSuccessful++;
}

int total_b2 = count_b2_successful + count_b2_notSuccessful;
successRate_b2 = (total_b2 != 0) ? ((double) count_b2_successful / total_b2) : 0;

//main.hs_data_utility_b2_all.add(utility_b2);
//main.hs_data_b2_all.add(b2);

/*ALCODEEND*/}

double f_b3_notifyNeighbor()
{/*ALCODESTART::1753175429703*/
//check if within daytime and selected in scenario
if(main.p_b3_notifyNeighbor && main.v_withinSocialChargingTimes){
	//act within probability
	boolean actBehavior = f_actBehavior(v_prob_b3);
	///act
	f_notifyNeighbor(actBehavior);
	
	//Social learning after interaction
	f_socialLearning_b3();
}

/*ALCODEEND*/}

double f_notifyNeighbor(boolean actBehavior)
{/*ALCODESTART::1753175429706*/
int EVsAwaitingCP = count(main.EVOwners, x->x.v_status == PARKED_NON_CHARGE_POINT_CHARGING_REQUIRED);

if( actBehavior && EVsAwaitingCP > 0 ){
	//Get notified EV Owner
	EVOwner EVNotified = randomWhere(main.EVOwners, x->x.v_status == PARKED_NON_CHARGE_POINT_CHARGING_REQUIRED);
	EVNotified.v_status = PARKED_CHARGE_POINT_CHARGING;
	count_b3_successful++;
}
else {
	count_b3_notSuccessful++;
}

int total_b3 = count_b3_successful + count_b3_notSuccessful;
successRate_b3 = (total_b3 != 0) ? ((double) count_b3_successful / total_b3) : 0;

//main.hs_data_utility_b3_all.add(utility_b3);
//main.hs_data_b3_all.add(b3);
/*ALCODEEND*/}

boolean f_actBehavior(double probabilityBehavior)
{/*ALCODESTART::1754388708163*/
double rand = uniform();
if( rand < probabilityBehavior){
	return true;
}
else {
	return false;
}
/*ALCODEEND*/}

double f_socialLearning_b1()
{/*ALCODESTART::1754388925560*/
//Update social learning after b1 interaction (successful or unsuccessful)
double observedFraction = main.successRate_b1;

double norms_t0 = v_norms;
double trust_t0 = v_trust;
double psi_t0 = v_perc_social_interdep;
double prob_b1_t0 = v_prob_b1;

double norms_t1 = norms_t0 + main.learningRate_norms_b1 * (1 - norms_t0);
double trust_t1 = trust_t0 + main.learningRate_trust_b1 * (1 - trust_t0);
//double psi_t1 = psi_t0 + main.regCoef_norms_psi * (norms_t1 - norms_t0) + main.regCoef_trust_psi * (trust_t1 - trust_t0);
//double prob_b1_t1 = prob_b1_t0 + main.regCoef_psi_b1 * (psi_t1 - psi_t0);

double psi_t1 = psi_t0 + (norms_t1 - norms_t0);// + main.regCoef_trust_psi /10 * (trust_t1 - trust_t0);
double prob_b1_t1 = prob_b1_t0 + main.regCoef_psi_b1 * 5 * (psi_t1 - psi_t0);


v_norms = norms_t1;
v_trust = trust_t1;
v_perc_social_interdep = psi_t1;
v_prob_b1 = prob_b1_t1;

/*ALCODEEND*/}

EVOwner f_successfulMoveRequest(boolean actBehavior)
{/*ALCODESTART::1754391554916*/
/**
 * Attempts to find an EV to move based on actBehavior probability and availability.
 * Returns the EV to move if successful, or null otherwise.
 */
if (!actBehavior) {
	return null;  // Behavior not active, no move
}

// Check if any EV with idle CP available
int idleCount = count(main.EVOwners, x -> x.v_status == PARKED_CHARGE_POINT_IDLE);
if (idleCount == 0) {
	count_b2_noIdleChargers++;
	return null;
}

// Calculate sigmoid-based probability
double scale = 10.0; // Steepness
double x_shift = 0.5; // Center
double sigmoid = 1.0 / (1.0 + Math.exp(-scale * (main.successRate_b2 - x_shift)));
double probability = 0.2 + 0.4 * sigmoid;
double rand = uniform();

if (rand >= probability) {
	count_b2_noMatchingRequest++;
	return null;
}

// Passed probability check: select random EV with idle CP to move
EVOwner selected = randomWhere(main.EVOwners, x->x.v_status == PARKED_CHARGE_POINT_IDLE);

if (selected == null) {
	// Defensive: no candidates despite count check earlier
	traceln("Error no matching EV owner despite checl");
	count_b2_noIdleChargers++;
	return null;
}

return selected;


/*
boolean act = false;
if( actBehavior ){
	if(count(main.EVOwners, x->x.v_status == PARKED_CHARGE_POINT_IDLE) > 0){
		
		
		double scale = 10.0; // Steepness of the S
		double x_shift = 0.5; // Center point of the S
		double sigmoid = 1.0 / (1.0 + Math.exp(-scale * (main.successRate_b2 - x_shift))); // Output in (0,1)
		double probability = 0.2 + 0.4 * sigmoid;
		double rand = uniform();
		
		//traceln("in b2 prob = " + probability + " with succes rate " + main.successRate_b2);
		
		if( rand < probability ){
			act = true;
		}
		else {
			count_b2_noMatchingRequest++;
		}
	}
	else{
		//traceln("in act behavior 2 but without idle chargers");
		count_b2_noIdleChargers++;
	}
}
	

return act;
*/

//S-shape curve y between 0.2 and 0.6, x between 0 - 1 based on successRate
/*ALCODEEND*/}

double f_socialLearning_b2()
{/*ALCODESTART::1754404097901*/
//Update social learning after b2 interaction (successful or unsuccessful)
double norms_t0 = v_norms;
double trust_t0 = v_trust;
double psi_t0 = v_perc_social_interdep;
double prob_b2_t0 = v_prob_b2;

double norms_t1 = norms_t0 + main.learningRate_norms_b2 * (1 - norms_t0);
double trust_t1 = trust_t0 + main.learningRate_trust_b2 * (1 - trust_t0);
double psi_t1 = psi_t0 + main.regCoef_norms_psi * (norms_t1 - norms_t0) + main.regCoef_trust_psi * (trust_t1 - trust_t0);

double prob_b2_t1 = prob_b2_t0 + main.regCoef_psi_b2 * (psi_t1 - psi_t0);

v_norms = norms_t1;
v_trust = trust_t1;
v_perc_social_interdep = psi_t1;
v_prob_b2 = prob_b2_t1;

/*ALCODEEND*/}

double f_socialLearning_b3()
{/*ALCODESTART::1754404103304*/
//Update social learning after b3 interaction (successful or unsuccessful)
double norms_t0 = v_norms;
double trust_t0 = v_trust;
double psi_t0 = v_perc_social_interdep;
double prob_b3_t0 = v_prob_b3;

double norms_t1 = norms_t0 + main.learningRate_norms_b3 * (1 - norms_t0);
double trust_t1 = trust_t0 + main.learningRate_trust_b3 * (1 - trust_t0);
double psi_t1 = psi_t0 + main.regCoef_norms_psi * (norms_t1 - norms_t0) + main.regCoef_trust_psi * (trust_t1 - trust_t0);

double prob_b3_t1 = prob_b3_t0 + main.regCoef_psi_b3 * (psi_t1 - psi_t0);

v_norms = norms_t1;
v_trust = trust_t1;
v_perc_social_interdep = psi_t1;
v_prob_b3 = prob_b3_t1;

/*ALCODEEND*/}

double f_cleanUp()
{/*ALCODESTART::1754493835269*/
v_nextTrip = null;
v_chargePoint = null;
c_tripSchedule.clear();

this.deleteSelf();
/*ALCODEEND*/}

boolean f_tryAcquireChargePoint()
{/*ALCODESTART::1754908555295*/
for (J_ChargePoint cp : main.c_chargePoints) {
    if (!cp.isOccupied()) {
        cp.occupy(this);
        v_chargePoint = cp;
        return true; //Succesfully acquired a CP
    }
}
return false;
/*ALCODEEND*/}

double f_recheckChargePoints()
{/*ALCODESTART::1754997016641*/
if(main.v_recheckCPAvailability && main.v_withinSocialChargingTimes){
	//Get EVs waiting for CP
	if(v_status == PARKED_NON_CHARGE_POINT_CHARGING_REQUIRED){
		
		int currentPartOfDay = main.f_getPartOfDay();
		int currentDay = main.v_day;
		
		//Day of arrival, check next part of day
		if( !firstCheckDone && currentPartOfDay != lastCheckDayPart){	
			lastCheckDay = currentDay;
			lastCheckDayPart = currentPartOfDay;
			firstCheckDone = true;
			f_tryRecheck();
		}
		//Next days: once per preselected random part of day
		else if( firstCheckDone && currentDay != lastCheckDay){
			//If no chosen random part yet
			if(plannedPartOfDay == -1){
				plannedPartOfDay = (int) uniform(0,3);
			}
		
			if(plannedPartOfDay == currentPartOfDay){		
				lastCheckDay = currentDay;
				lastCheckDayPart = currentPartOfDay;
				plannedPartOfDay = -1; //reset
				f_tryRecheck();
			}
		}
        else if (!firstCheckDone && currentDay != lastCheckDay) {
            traceln("WARNING: First day check skipped unexpectedly");
        }
	}
}
			

/*ALCODEEND*/}

double f_tryRecheck()
{/*ALCODESTART::1754999113828*/
//Check CP availability
if( f_tryAcquireChargePoint()){
	v_status = PARKED_CHARGE_POINT_CHARGING;
	count_chargingSessions++;
	count_successfulRechecks++;
}
else {
	count_unsuccessfulRechecks++;
}
/*ALCODEEND*/}

