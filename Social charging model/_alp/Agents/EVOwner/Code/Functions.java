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
        
        if(v_delayedChargePointAccess == true){
        	main.totalElecChargedAfterB3 += actualCharge;
        }
    }

    // Always update SOC
    v_soc = v_electricityInBattery_kWh / v_batteryCapacity_kWh;

    // Check for full battery *after* charging
    if (v_electricityInBattery_kWh >= v_batteryCapacity_kWh) {
        v_electricityInBattery_kWh = v_batteryCapacity_kWh;
        v_status = PARKED_CHARGE_POINT_IDLE;
		
		//Execute by after charging
		if(main.v_withinSocialChargingTimes){
			f_b1_movingVehicle();
		}
		else {
			b1_extended = true;
			main.count_b1ExtendedTriggered++;
		}
    }
}
/*ALCODEEND*/}

double f_updateSOC(double tripDistance_km)
{/*ALCODESTART::1746011780748*/
//Update SOC
double electricityConsumed_kWh = tripDistance_km * v_elecCons_kWhperKm;
v_electricityInBattery_kWh -= electricityConsumed_kWh;

if(v_electricityInBattery_kWh < 0){ //if on-route below 0 soc
	double chargeToPercentage = 0.1; //If regular fast charging on route required to 0.1, if often out of model charging because shortage in CPs charge in other neighbrohood to 100
	if( v_leftUnchargedStreak >= 3 ){
		chargeToPercentage = 1;
		v_leftUnchargedStreak = 0;
		main.countOOMCTo100++;
		//traceln("out of model charge to 100% for EV " + this.getIndex());
	}
	
	double outOfModelCharging_kWh = abs(v_electricityInBattery_kWh) + v_batteryCapacity_kWh * chargeToPercentage;
	v_outOfModelCharge_kWh += outOfModelCharging_kWh;
	v_totalElectricityCharged_kWh += outOfModelCharging_kWh;
	v_electricityInBattery_kWh += outOfModelCharging_kWh;
	
	double soc = v_electricityInBattery_kWh / v_batteryCapacity_kWh;
	
	if (!(Math.abs(soc - 0.10) <= 0.005 || Math.abs(soc - 1) <= 0.005)) {  // ±0.5% tolerance
		traceln("ERROR: soc = " + roundToInt(v_electricityInBattery_kWh / v_batteryCapacity_kWh * 100) + "% after out of model charging, with OoMC = " + outOfModelCharging_kWh + "kWh, elec in bat = " + v_electricityInBattery_kWh + " bat cap " + v_batteryCapacity_kWh);
	}
}

v_soc = v_electricityInBattery_kWh / v_batteryCapacity_kWh;



/*ALCODEEND*/}

double f_setChargingStatus()
{/*ALCODESTART::1747136105778*/
if(v_status == ARRIVING){
	boolean wantsToCharge = v_soc < v_socChargingThreshold;
	Status currentStatus = v_status;
	//boolean foundCPThroughRequest = false;
	
	if(wantsToCharge){
		main.count_wantsToCharge++;
		count_chargingRequired++;
		// Already charging
		if(v_chargePoint != null){
			v_status = PARKED_CHARGE_POINT_CHARGING;
		}
		// Not charging, trying to get CP
		else if( f_tryAndAcquireChargePoint()){
			f_updatePerceivedChargePointPressure(false);
		}
		//Behaviour 2: Request neighbor to move EV
		else {
			f_b2_requestMove();
			//f_updatePerceivedChargePointPressure(true);
		}
	}
	else {
		//If somehow still holding CP
		if( v_chargePoint != null) {
			traceln("Holding CP which should not happen with status " + v_status);
			f_leaveChargePoint(false);
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
if(main.p_b1_moveCar){
	//act within probability
	boolean actBehavior = f_actBehavior(v_prob_b1);
	///act
	boolean behaviorSuccess = f_moveVehicle(actBehavior);
		
	//Social learning after interaction
	//f_socialLearning_b1();
	if(main.initializationMode == false){
		f_updateNorm_b1(behaviorSuccess);
	}
}
main.b1++;


/*ALCODEEND*/}

boolean f_moveVehicle(boolean actBehavior)
{/*ALCODESTART::1753175296086*/
boolean behaviorSuccess = false;
//Move vehicle
if( actBehavior ) {// && main.v_parkingPlacesAvailable > 0 ){
	f_leaveChargePoint(false);
	v_status = PARKED_NON_CHARGE_POINT_CHARGING_NOT_REQUIRED;
	count_b1_successful++;
	behaviorSuccess = true;
}
else {
	count_b1_notSuccessful++;
}

int total_b1 = count_b1_successful + count_b1_notSuccessful;
successRate_b1 = (total_b1 != 0) ? ((double) count_b1_successful / total_b1) : 0;

//main.hs_data_utility_b1_all.add(utility_b1);
//main.hs_data_b1_all.add(b1);
return behaviorSuccess;


/*ALCODEEND*/}

double f_b2_requestMove()
{/*ALCODESTART::1753175364935*/
//check if within daytime and selected in scenario
boolean behaviorSuccess = false;
if(main.p_b2_requestMove && main.v_withinSocialChargingTimes){
	//act within probability
	boolean actBehavior = f_actBehavior(v_prob_b2);
	///act
	behaviorSuccess = f_requestMove(actBehavior);
	//f_updatePerceivedChargePointPressure(false);
	
	//Social learning after interaction
	//f_socialLearning_b2();
	if(main.initializationMode == false){
		f_updateNorm_b2(behaviorSuccess);
	}
	main.b2++;
}
//No CP available
else {
	v_status = PARKED_NON_CHARGE_POINT_CHARGING_REQUIRED;
	count_noCPAvailable++;
}
boolean missedChargingSession = !behaviorSuccess;
f_updatePerceivedChargePointPressure(missedChargingSession);

if (count_b2_noProb + count_b2_noIdleChargers + count_b2_noMatchingRequest != count_b2_notSuccessful) {
    traceln(
        "B2 counter mismatch at EV " + this.getIndex() +
        ", noProb=" + count_b2_noProb +
        ", noIdleChargers=" + count_b2_noIdleChargers +
        ", noMatchingRequest=" + count_b2_noMatchingRequest +
        ", notSuccessful=" + count_b2_notSuccessful +
        ", successful=" + count_b2_successful +
        ", total=" + (count_b2_successful + count_b2_notSuccessful)
    );
}


/*ALCODEEND*/}

boolean f_requestMove(boolean actBehavior)
{/*ALCODESTART::1753175364937*/
//Get moving EV if available
EVOwner movingEV = f_successfulMoveRequest(actBehavior);
boolean behaviorSuccess = false;

if(movingEV != null){
	movingEV.v_status = PARKED_NON_CHARGE_POINT_CHARGING_NOT_REQUIRED;
	J_ChargePoint freedCP = movingEV.v_chargePoint;
	movingEV.f_leaveChargePoint(true);
	movingEV.count_fulfilledMoveRequest++;
	f_connectToCP(freedCP);
	behaviorSuccess = true;
}
else {
	v_status = PARKED_NON_CHARGE_POINT_CHARGING_REQUIRED;
}

int total_b2 = count_b2_successful + count_b2_notSuccessful;
successRate_b2 = (total_b2 != 0) ? ((double) count_b2_successful / total_b2) : 0;

//main.hs_data_utility_b2_all.add(utility_b2);
//main.hs_data_b2_all.add(b2);

return behaviorSuccess;
/*ALCODEEND*/}

double f_b3_notifyNeighbor(J_ChargePoint chargePoint)
{/*ALCODESTART::1753175429703*/
//check if within daytime and selected in scenario
if(main.p_b3_notifyNeighbor && main.v_withinSocialChargingTimes){
	//act within probability
	boolean actBehavior = f_actBehavior(v_prob_b3);
	///act
	boolean behaviorSuccess = f_notifyNeighbor(actBehavior, chargePoint);
	
	//Social learning after interaction
	//f_socialLearning_b3();
	if(main.initializationMode == false){
		f_updateNorm_b3(behaviorSuccess);
	}
	
	main.b3++;
}

main.b3Triggered++;
/*ALCODEEND*/}

boolean f_notifyNeighbor(boolean actBehavior,J_ChargePoint chargePoint)
{/*ALCODESTART::1753175429706*/
int EVsAwaitingCP = count(main.EVOwners, x->x.v_status == PARKED_NON_CHARGE_POINT_CHARGING_REQUIRED);
boolean behaviorSuccess = false;

if( actBehavior && EVsAwaitingCP > 0 ){
	//Get notified EV Owner
	EVOwner EVNotified = randomWhere(main.EVOwners, x->x.v_status == PARKED_NON_CHARGE_POINT_CHARGING_REQUIRED);
	
	EVNotified.f_connectToCP(chargePoint);
	EVNotified.v_delayedChargePointAccess = true;
	
	
	count_b3_successful++;
	behaviorSuccess = true;
	successfulB3 = true;
	if(chargePoint == null){
		traceln("CP at B3 is null");
	}
}
else {
	count_b3_notSuccessful++;
}

int total_b3 = count_b3_successful + count_b3_notSuccessful;
successRate_b3 = (total_b3 != 0) ? ((double) count_b3_successful / total_b3) : 0;

//main.hs_data_utility_b3_all.add(utility_b3);
//main.hs_data_b3_all.add(b3);

return behaviorSuccess;
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
double alpha = 0.5;
double beta = 0.5;

double globalSuccessRate = main.successRate_b1;
if( main.count_b1_notSuccessful + main.count_b1_successful == 0){
	globalSuccessRate = main.avgProb_b1;
}

double personalSuccessRate = successRate_b1;
if(count_b1_notSuccessful + count_b1_successful == 0){
	personalSuccessRate = main.avgProb_b1;
}
double perceivedSuccesRate = personalSuccessRate * alpha + globalSuccessRate * (1-alpha);

double chargePointPressure = v_perc_charging_pressure;

double norms_t0 = v_norm_b1;
double rc_t0 = v_reputational_concern;
double psi_t0 = v_perc_social_interdep;
double pcp_t0 = v_perc_charging_pressure; //perceived charge point pressure has prior!
double stand_prob_b1_t0 = v_stand_prob_b1;

//learning independent variables
double norms_t1 = f_learnNorms(globalSuccessRate, norms_t0); //double norms_t1 = norms_t0 + main.learningRate_norms_b1 * (perceivedSuccesRate - norms_t0);
//double pcp_t1 = pcp_t0 + main.learningRate_pcp_b1 * (chargePointPressure - pcp_t0); 
double rc_t1 = rc_t0; //no learning in RC

//update mediator
double psi_t1 = psi_t0 + main.regCoef_norms_psi_b1 * (norms_t1 - norms_t0) + main.regCoef_pcp_psi_b1 * (pcp_t1 - pcp_t0) + main.regCoef_rc_psi_b1 * (rc_t1 - rc_t0);

//update probability (standardized)
v_stand_prob_b1 = stand_prob_b1_t0 + main.regCoef_psi_b1 * (psi_t1 - psi_t0) + main.regCoef_rc_b1 * (rc_t1 - rc_t0);

v_norm_b1 = norms_t1;
v_perc_charging_pressure = pcp_t1;
v_reputational_concern = rc_t1;
v_perc_social_interdep = psi_t1;

//update probability (normalized)
v_prob_b1 = main.f_convertStandardizedToProb(v_stand_prob_b1, main.mean_b1, main.sd_b1, true);



/*ALCODEEND*/}

EVOwner f_successfulMoveRequest(boolean actBehavior)
{/*ALCODESTART::1754391554916*/
/**
 * Attempts to find an EV to move based on actBehavior probability and availability.
 * Returns the EV to move if successful, or null otherwise.
 */
if (!actBehavior) {
	count_b2_noProb++;
	count_b2_notSuccessful++;
	return null;  // Behavior not active, no move
}

// Check if any EV with idle CP available
int idleCount = count(main.EVOwners, x -> x.v_status == PARKED_CHARGE_POINT_IDLE);
if (idleCount == 0) {
	count_b2_noIdleChargers++;
	count_b2_notSuccessful++;
	return null;
}

// Calculate sigmoid-based probability
/*
double scale = 2; // Steepness
double x_shift = 0.6; // Center
double sigmoid = 1.0 / (1.0 + Math.exp(-scale * (main.successRate_b2 - x_shift)));
double probability = 0.2 + 0.6 * sigmoid;*/
double probability = 0.75;
double rand = uniform();

if (rand > probability) {
	count_b2_noMatchingRequest++;
	count_b2_notSuccessful++;
	return null;
}

// Passed probability check: select random EV with idle CP to move
EVOwner selected = randomWhere(main.EVOwners, x->x.v_status == PARKED_CHARGE_POINT_IDLE);

if (selected == null) {
	// Defensive: no candidates despite count check earlier
	traceln("Error no matching EV owner despite checl");
	count_b2_noIdleChargers++;
	count_b2_notSuccessful++;
	return null;
}

count_b2_successful++;
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
//Update social learning after b1 interaction (successful or unsuccessful)
double alpha = 0.5;
double beta = 0.5;

//Success rate
double globalSuccessRate = main.successRate_b2;
if( main.count_b2_notSuccessful + main.count_b2_successful == 0){
	globalSuccessRate = main.avgProb_b2;
}

double personalSuccessRate = successRate_b2;
if(count_b2_notSuccessful + count_b2_successful == 0){
	personalSuccessRate = main.avgProb_b2;
}
double perceivedSuccesRate = personalSuccessRate * alpha + globalSuccessRate * (1-alpha);

double chargePointPressure = v_perc_charging_pressure; 

double norms_t0 = v_norm_b2;
double rc_t0 = v_reputational_concern;
double psi_t0 = v_perc_social_interdep;
double pcp_t0 = v_perc_charging_pressure; //perceived charge point pressure has prior!
double stand_prob_b2_t0 = v_stand_prob_b2;

//learning independent variables
double norms_t1 = f_learnNorms(globalSuccessRate, norms_t0); //double norms_t1 = norms_t0 + main.learningRate_norms_b2 * (perceivedSuccesRate - norms_t0);
//double pcp_t1 = pcp_t0 + main.learningRate_pcp_b2 * (chargePointPressure - pcp_t0); 
double rc_t1 = rc_t0; //no learning in RC



//update mediator
double psi_t1 = psi_t0 + main.regCoef_norms_psi_b2b3 * (norms_t1 - norms_t0);

//update probability (standardized)
v_stand_prob_b2 = stand_prob_b2_t0 + main.regCoef_psi_b2 * (psi_t1 - psi_t0);

v_norm_b2 = norms_t1;
v_perc_charging_pressure = pcp_t1;
v_reputational_concern = rc_t1;
v_perc_social_interdep = psi_t1;

//update probability (normalized)
v_prob_b2 = main.f_convertStandardizedToProb(v_stand_prob_b2, main.mean_b2, main.sd_b2, false);

//System.out.printf("val_stand_b2=%.4f, unstandardized val_b2=%.4f, prob_b2=%.4f\n", 
 //   v_stand_prob_b2, main.mean_b2 + v_stand_prob_b2*main.sd_b2, v_prob_b2);


/*ALCODEEND*/}

double f_socialLearning_b3()
{/*ALCODESTART::1754404103304*/
//Update social learning after b1 interaction (successful or unsuccessful)
double alpha = 0.5;
double beta = 0.5;

double globalSuccessRate = main.successRate_b3;
if( main.count_b3_notSuccessful + main.count_b3_successful == 0){
	globalSuccessRate = main.avgProb_b3;
}

double personalSuccessRate = successRate_b3;
if(count_b3_notSuccessful + count_b3_successful == 0){
	personalSuccessRate = main.avgProb_b3;
}

double norms_t0 = v_norm_b3;
double rc_t0 = v_reputational_concern;
double psi_t0 = v_perc_social_interdep;
double pcp_t0 = v_perc_charging_pressure; //perceived charge point pressure has prior!
double stand_prob_b3_t0 = v_stand_prob_b3;

//learning independent variables
double norms_t1 = f_learnNorms(globalSuccessRate, norms_t0); //double norms_t1 = norms_t0 + main.learningRate_norms_b3 * (perceivedSuccesRate - norms_t0);
//double pcp_t1 = pcp_t0 + main.learningRate_pcp_b3 * (chargePointPressure - pcp_t0); 
double rc_t1 = rc_t0; //no learning in RC

//update mediator
double psi_t1 = psi_t0 + main.regCoef_norms_psi_b2b3 * (norms_t1 - norms_t0) + main.regCoef_pcp_psi_b2b3 * (pcp_t1 - pcp_t0) + main.regCoef_rc_psi_b2b3 * (rc_t1 - rc_t0);

//update probability (standardized)
v_stand_prob_b3 = stand_prob_b3_t0 + main.regCoef_psi_b3 * (psi_t1 - psi_t0) + main.regCoef_pcp_b3 * (pcp_t1 - pcp_t0);

v_norm_b3 = norms_t1;
v_perc_charging_pressure = pcp_t1;
v_reputational_concern = rc_t1;
v_perc_social_interdep = psi_t1;

//update probability (normalized)
v_prob_b3 = main.f_convertStandardizedToProb(v_stand_prob_b3, main.mean_b3, main.sd_b3, false);

//System.out.printf("val_stand b3=%.4f, unstandardized val b3=%.4f, prob b3=%.4f\n", 
    //v_stand_prob_b3, main.mean_b3 + v_stand_prob_b3*main.sd_b3, v_prob_b3);


/*ALCODEEND*/}

double f_cleanUp()
{/*ALCODESTART::1754493835269*/
v_nextTrip = null;
v_chargePoint = null;
c_tripSchedule.clear();

this.deleteSelf();
/*ALCODEEND*/}

boolean f_tryAndAcquireChargePoint()
{/*ALCODESTART::1754908555295*/
for (J_ChargePoint cp : main.c_chargePoints) {
    if (!cp.isOccupied()) {
        f_connectToCP(cp);
        //f_updatePerceivedChargePointPressure(false);
        return true; //Succesfully acquired a CP
    }
}
//f_updatePerceivedChargePointPressure(true);
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
boolean foundAvailableChargePoint = f_tryAndAcquireChargePoint();
if( foundAvailableChargePoint ){
	count_successfulRechecks++;
	v_delayedChargePointAccess = true;
}
else {
	count_unsuccessfulRechecks++;
}
/*ALCODEEND*/}

double f_learnNorms(double globalSuccessRate,double norms_t0)
{/*ALCODESTART::1756215755202*/
// ---------- Parameters you can tweak ----------
double s = 1.1;                // conformity exponent: >1 amplifies majorities (try 1.5..3.0)
double lr_base_up = 0.3;       // base learning speed when adoption > norms (fast rise)
double lr_base_down = 0.05;    // base learning speed when adoption < norms (slow decay)
//double time_decay_factor = 0.8; // optional: decay lr_up over time to stabilise (close to 1)
double tiny = 1e-12;

// ---------- Compute conformity / lagged adoption ----------
double freq = globalSuccessRate; // in your model this equals adoption
// Use a lag to avoid norms only mirroring instant adoption (initialize main.prev_global_adoption = -1.0)
double laggedFreq = (main.prev_global_adoption >= 0.0) ? main.prev_global_adoption : main.avgProb_b1;

// clamp laggedFreq to [0,1] to avoid NaN in pow
if(laggedFreq < 0.0) laggedFreq = 0.0;
if(laggedFreq > 1.0) laggedFreq = 1.0;

// conformity amplification (optional)
double numerator = Math.pow(laggedFreq, s);
double denominator = numerator + Math.pow(1.0 - laggedFreq, s) + tiny;
double conformitySignal = numerator / denominator; // in [0,1]

// define delta = how norms should change this tick
// positive if adoption high (reinforce norm), negative if low (norm decays)
double delta = conformitySignal * lr_base_up - (1.0 - conformitySignal) * lr_base_down;

// update norms directly, no fixed target
double norms_t1 = norms_t0 + delta;

// clamp to 3 standard deviations
double maxNormSD = 3.0;
if(norms_t1 < -maxNormSD) norms_t1 = -maxNormSD;
if(norms_t1 > maxNormSD) norms_t1 = maxNormSD;


// save lag
main.prev_global_adoption = freq;

// Assign back
if (Double.isNaN(norms_t1)) {
    traceln("Warning: norms_t1 is NaN at time " + time());
    norms_t1 = norms_t0;  // fallback to previous value, or set to 0
}

return norms_t1;

/*ALCODEEND*/}

double f_updatePerceivedChargePointPressure(boolean missedThisSession)
{/*ALCODESTART::1756980645328*/
f_updatePCP(missedThisSession);
/*hasUpdatedPCP = true;
double pcp_t0 = v_perc_charging_pressure;
double pcp_t1 = 0;
double maxPCP = main.mean_pcp + 3 * main.sd_pcp;

// EMA update
double missWeight = 0.2;   // strong jump
double successWeight = 0.01; //0.00000001; // tiny decay

if (missedThisSession) {
    pcp_t1 = pcp_t0 + missWeight * (maxPCP - pcp_t0);
} else {
    pcp_t1 = pcp_t0 - successWeight * pcp_t0;
}

// Optional: clip to [0,1] just in case
//pcp_t1 = Math.max(0.0, Math.min(1.0, pcp_t1));

v_perc_charging_pressure = pcp_t1;


//Update mediator
double psi_t0 = v_perc_social_interdep;
double psi_t1 = psi_t0 + main.regCoef_pcp_psi_b1 * (pcp_t1 - pcp_t0);
v_perc_social_interdep = psi_t1;

//Update probability (standardized)
v_stand_prob_b1 = v_stand_prob_b1 + main.regCoef_psi_b1 * (psi_t1 - psi_t0);
v_stand_prob_b2 = v_stand_prob_b2 + main.regCoef_psi_b2 * (psi_t1 - psi_t0) + main.regCoef_pcp_b2 * (pcp_t1 - pcp_t0);
v_stand_prob_b3 = v_stand_prob_b3 + main.regCoef_psi_b3 * (psi_t1 - psi_t0) + main.regCoef_pcp_b3 * (pcp_t1 - pcp_t0);

//update probability (normalized)
v_prob_b1 = main.f_convertStandardizedToProb(v_stand_prob_b1, main.mean_b1, main.sd_b1, true);
v_prob_b2 = main.f_convertStandardizedToProb(v_stand_prob_b2, main.mean_b2, main.sd_b2, false);
v_prob_b3 = main.f_convertStandardizedToProb(v_stand_prob_b3, main.mean_b3, main.sd_b3, false);
*/
/*ALCODEEND*/}

double f_updateNorm_b1(boolean experience)
{/*ALCODESTART::1757421134413*/
//store previous norm
double norms_t0 = v_norm_b1;

//learning independent variables
double norms_t1 = f_updateNorms(experience, v_norm_b1, main.v_avgNorm_b1, main.mean_b1, main.sd_b1, main.avgProb_b1, true);
v_norm_b1 = norms_t1;

//Update mediator
double psi_t0 = v_perc_social_interdep;
double psi_t1 = psi_t0 + main.regCoef_norms_psi_b1 * (norms_t1 - norms_t0);
v_perc_social_interdep = psi_t1;

//Update probability (standardized)
v_stand_prob_b1 += main.regCoef_psi_b1 * (psi_t1 - psi_t0);

//update probability (normalized)
v_prob_b1 = main.f_convertStandardizedToProb(v_stand_prob_b1, main.mean_b1, main.sd_b1, true);

/*ALCODEEND*/}

double f_updateNorm_b2(boolean experience)
{/*ALCODESTART::1757421637017*/
//store previous norm
double norms_t0 = v_norm_b2;

//learning independent variables
double norms_t1 = f_updateNorms(experience, v_norm_b2, main.v_avgNorm_b2, main.mean_b2, main.sd_b2, main.avgProb_b2, false);
v_norm_b2 = norms_t1;

//Update mediator
double psi_t0 = v_perc_social_interdep;
double psi_t1 = psi_t0 + main.regCoef_norms_psi_b2b3 * (norms_t1 - norms_t0);
v_perc_social_interdep = psi_t1;

//Update probability (standardized)
v_stand_prob_b2 += main.regCoef_psi_b2 * (psi_t1 - psi_t0);

//update probability (normalized)
v_prob_b2 = main.f_convertStandardizedToProb(v_stand_prob_b2, main.mean_b2, main.sd_b2, false);
/*ALCODEEND*/}

double f_updateNorm_b3(boolean experience)
{/*ALCODESTART::1757421640023*/
//store previous norm
double norms_t0 = v_norm_b3;

//learning independent variables
double norms_t1 = f_updateNorms(experience, v_norm_b3, main.v_avgNorm_b3, main.mean_b3, main.sd_b3, main.avgProb_b3, false);
v_norm_b3 = norms_t1;

//Update mediator
double psi_t0 = v_perc_social_interdep;
double psi_t1 = psi_t0 + main.regCoef_norms_psi_b2b3 * (norms_t1 - norms_t0);
v_perc_social_interdep = psi_t1;

//Update probability (standardized)
v_stand_prob_b3 += main.regCoef_psi_b3 * (psi_t1 - psi_t0);

//update probability (normalized)
v_prob_b3 = main.f_convertStandardizedToProb(v_stand_prob_b3, main.mean_b3, main.sd_b3, false);

/*ALCODEEND*/}

double f_EMA2(double value,double EMA_t0,double smoothing_factor)
{/*ALCODESTART::1757498633342*/

double EMA_t1 = (1 - smoothing_factor) * EMA_t0 + smoothing_factor * value;
return EMA_t1;
/*ALCODEEND*/}

double f_updatePCP(boolean experience)
{/*ALCODESTART::1757510078994*/
double EMA_t0 = v_perc_charging_pressure;
double pcp_t0 = v_perc_charging_pressure;
double global_avg = main.v_avgPCP;
double smoothingFactor = main.v_smoothingFactorPCP;
double sharePersonal = main.v_sharePersonal;
double mean_pcp = main.mean_pcp;
double sd_pcp = main.sd_pcp;

double avgPCP = EMA_t0 * sharePersonal + (1-sharePersonal) * global_avg;
double minVal_standardized = -1.61303;
double maxVal_standardized = 3.04025;
double experienceValue = experience 
      ? maxVal_standardized
      : minVal_standardized;


//Assymetric smoothing: strong update on rise, weak on decay
double effective_smoothing;

if(experience){
	effective_smoothing = smoothingFactor * 3.0; //failure -> fast rise
} else {
	effective_smoothing = smoothingFactor * 0.01; //success -> slow decay
}

effective_smoothing = Math.max(0.01, Math.min(1.0, effective_smoothing));

//Update EMA
double EMA_t1 = EMA_t0 + effective_smoothing * (experienceValue - EMA_t0);
double pcp_t1 = EMA_t1;
v_perc_charging_pressure = pcp_t1;

//Update mediator
double psi_t0 = v_perc_social_interdep;
double psi_t1 = psi_t0 + main.regCoef_pcp_psi_b1 * (pcp_t1 - pcp_t0);
v_perc_social_interdep = psi_t1;

//Update probability (standardized)
v_stand_prob_b1 = v_stand_prob_b1 + main.regCoef_psi_b1 * (psi_t1 - psi_t0);
v_stand_prob_b2 = v_stand_prob_b2 + main.regCoef_psi_b2 * (psi_t1 - psi_t0) + main.regCoef_pcp_b2 * (pcp_t1 - pcp_t0);
v_stand_prob_b3 = v_stand_prob_b3 + main.regCoef_psi_b3 * (psi_t1 - psi_t0) + main.regCoef_pcp_b3 * (pcp_t1 - pcp_t0);

//update probability (normalized)
v_prob_b1 = main.f_convertStandardizedToProb(v_stand_prob_b1, main.mean_b1, main.sd_b1, true);
v_prob_b2 = main.f_convertStandardizedToProb(v_stand_prob_b2, main.mean_b2, main.sd_b2, false);
v_prob_b3 = main.f_convertStandardizedToProb(v_stand_prob_b3, main.mean_b3, main.sd_b3, false);

/*ALCODEEND*/}

double f_updateEMA(double personal_value,double EMA_t0,double global_avg,double smoothingFactor,double sharePersonal)
{/*ALCODESTART::1757510487750*/
//Combine personal and global value
double combined_value = sharePersonal * personal_value + (1 - sharePersonal) * global_avg;

//Assymetric smoothing: strong update on rise, weak on decay
double effective_smoothing;
if(experience){
	effective_smoothing = smoothingFactor * 2.0; //failure -> fast rise
} else {
	effective_smoothing = smoothingFactor * 0.5; //success -> slow decay
}

if( effective_smoothing > 1){ effective_smoothing = 1;}

//Update EMA
double EMA_t1 = (1 - effective_smoothing) * EMA_t0 + effective_smoothing * combined_value;
return EMA_t1;

/*ALCODEEND*/}

double f_updateNorms(boolean experience,double personalNorm,double globalNorm,double mean_norm,double sd_norm,double globalBehaviorRate,boolean b1)
{/*ALCODESTART::1757511287794*/
double EMA_t0 = personalNorm; //EMA = exponential moving average, here applied with non linear smoothing factor

double smoothingFactor = main.v_smoothingFactorNorms;
double sharePersonal = 0.5;
double avgNorm = personalNorm * sharePersonal + (1-sharePersonal) * globalNorm;

//double experience_value = experience ? mean_norm + sd_norm * 3 : mean_norm - sd_norm * 3;

//Set behavior impact stronger if further from personal norm
//double norm = sharePersonal * personalNorm + (1 - sharePersonal) * globalNorm;
double norm_normalized = main.f_convertStandardizedToProb(avgNorm, mean_norm, sd_norm, b1);
double pTrue = Math.min(Math.max(norm_normalized, 0.01), 0.99); // clamp
double pFalse = 1.0 - pTrue;

// Weight by surprisal
double rawSurprisalWeight = experience 
      ? -Math.log(pTrue)   // rare "true"
      : -Math.log(pFalse); // rare "false"
      
double surprisalWeight = Math.min(1.0, rawSurprisalWeight / 4.6); //4.6 takes a 99% deviation as 1 and a 0% deviation as 0

//Emperical disitrbution min and max from mean
//For B1 max standardized val =  1.42664, min standardized val = -1.63056
//For B2 and 3 max standardized val = 3.65424, min standardized val = -0.2727

double minVal_standardized = 0.0;
double maxVal_standardized = 0.0;
if(b1){
	minVal_standardized = -1.63056;
	maxVal_standardized = 1.42664;
}
else {
	minVal_standardized = -0.2727;
	maxVal_standardized = 3.65424;
}	

double experienceValue = experience 
      ? maxVal_standardized
      : minVal_standardized;

/*
//Scale by distance from current norm
double distance = Math.abs(experienceValue - EMA_t0);
*/
//double effective_smoothing = smoothingFactor * (1 + distance) * weight;
//effective_smoothing = Math.max(0.01, Math.min(1.0, effective_smoothing));
//Update EMA
double EMA_t1 = EMA_t0 + smoothingFactor * (experienceValue - EMA_t0) * surprisalWeight;
/* For debugging
if(!b1){
	if(distance > main.maxDistance){
		main.maxDistance = distance;
	}
	if(distance < main.minDistance){
		main.minDistance = distance;
	}
	if(surprisalWeight > main.maxWeight){
		main.maxWeight = surprisalWeight;
	}
	if(smoothingFactor > main.maxEffectiveSmoothing){
		main.maxEffectiveSmoothing = smoothingFactor;
	}
	if(smoothingFactor < main.minEffectiveSmoothing){
		main.minEffectiveSmoothing = smoothingFactor;
	}
	
	if(experienceValue > main.maxExperienceValue){
		main.maxExperienceValue = experienceValue;
	}
	double deltaExpEMA = experienceValue - EMA_t0;
	if(deltaExpEMA > main.maxDeltaExpEMA){
		main.maxDeltaExpEMA = deltaExpEMA;
	}
	if(norm_normalized > main.norm_normalized){
		main.norm_normalized = norm_normalized;
	}
}*/

return EMA_t1;

/*ALCODEEND*/}

double f_updateNorm_b4()
{/*ALCODESTART::1757513724869*/
//Update social learning after b1 interaction (successful or unsuccessful)
double alpha = 0.5;
double beta = 0.5;

//Success rate
double globalSuccessRate = main.successRate_b3;
if( main.count_b3_notSuccessful + main.count_b3_successful == 0){
	globalSuccessRate = main.avgProb_b3;
}

double personalSuccessRate = successRate_b3;
if(count_b3_notSuccessful + count_b3_successful == 0){
	personalSuccessRate = main.avgProb_b3;
}
double perceivedSuccesRate = personalSuccessRate * alpha + globalSuccessRate * (1-alpha);

double norms_t0 = v_norm_b3;
//learning independent variables
double norms_t1 = f_learnNorms(globalSuccessRate, norms_t0); //double norms_t1 = norms_t0 + main.learningRate_norms_b2 * (perceivedSuccesRate - norms_t0);
v_norm_b3 = norms_t1;


//Update mediator
double psi_t0 = v_perc_social_interdep;
double psi_t1 = psi_t0 + main.regCoef_norms_psi_b1 * (norms_t1 - norms_t0);
v_perc_social_interdep = psi_t1;

//Update probability (standardized)
v_stand_prob_b3 = v_stand_prob_b3 + main.regCoef_psi_b3 * (psi_t1 - psi_t0);

//update probability (normalized)
v_prob_b3 = main.f_convertStandardizedToProb(v_stand_prob_b3, main.mean_b3, main.sd_b3, false);

/*ALCODEEND*/}

double f_updateNorm_b5()
{/*ALCODESTART::1757513954298*/
//Update social learning after b1 interaction (successful or unsuccessful)
double alpha = 0.5;
double beta = 0.5;

//Success rate
double globalSuccessRate = main.successRate_b2;
if( main.count_b2_notSuccessful + main.count_b2_successful == 0){
	globalSuccessRate = main.avgProb_b2;
}

double personalSuccessRate = successRate_b2;
if(count_b2_notSuccessful + count_b2_successful == 0){
	personalSuccessRate = main.avgProb_b2;
}
double perceivedSuccesRate = personalSuccessRate * alpha + globalSuccessRate * (1-alpha);

double norms_t0 = v_norm_b2;
//learning independent variables
double norms_t1 = f_learnNorms(globalSuccessRate, norms_t0); //double norms_t1 = norms_t0 + main.learningRate_norms_b2 * (perceivedSuccesRate - norms_t0);
v_norm_b2 = norms_t1;


//Update mediator
double psi_t0 = v_perc_social_interdep;
double psi_t1 = psi_t0 + main.regCoef_norms_psi_b1 * (norms_t1 - norms_t0);
v_perc_social_interdep = psi_t1;

//Update probability (standardized)
v_stand_prob_b2 = v_stand_prob_b2 + main.regCoef_psi_b2 * (psi_t1 - psi_t0);

//update probability (normalized)
v_prob_b2 = main.f_convertStandardizedToProb(v_stand_prob_b2, main.mean_b2, main.sd_b2, false);


/*ALCODEEND*/}

double f_updateNorm_b6()
{/*ALCODESTART::1757513954300*/
//Update social learning after b1 interaction (successful or unsuccessful)
double alpha = 0.5;
double beta = 0.5;

//Success rate
double globalSuccessRate = main.successRate_b3;
if( main.count_b3_notSuccessful + main.count_b3_successful == 0){
	globalSuccessRate = main.avgProb_b3;
}

double personalSuccessRate = successRate_b3;
if(count_b3_notSuccessful + count_b3_successful == 0){
	personalSuccessRate = main.avgProb_b3;
}
double perceivedSuccesRate = personalSuccessRate * alpha + globalSuccessRate * (1-alpha);

double norms_t0 = v_norm_b3;
//learning independent variables
double norms_t1 = f_learnNorms(globalSuccessRate, norms_t0); //double norms_t1 = norms_t0 + main.learningRate_norms_b2 * (perceivedSuccesRate - norms_t0);
v_norm_b3 = norms_t1;


//Update mediator
double psi_t0 = v_perc_social_interdep;
double psi_t1 = psi_t0 + main.regCoef_norms_psi_b1 * (norms_t1 - norms_t0);
v_perc_social_interdep = psi_t1;

//Update probability (standardized)
v_stand_prob_b3 = v_stand_prob_b3 + main.regCoef_psi_b3 * (psi_t1 - psi_t0);

//update probability (normalized)
v_prob_b3 = main.f_convertStandardizedToProb(v_stand_prob_b3, main.mean_b3, main.sd_b3, false);

/*ALCODEEND*/}

double f_extendedB1()
{/*ALCODESTART::1761730717512*/
if(main.v_withinSocialChargingTimes && b1_extended && v_status == PARKED_CHARGE_POINT_IDLE){
	main.count_extendedB1++;
	b1_extended = false;
	f_b1_movingVehicle();
}
/*ALCODEEND*/}

double f_leaveChargePoint(boolean triggeredByB2)
{/*ALCODESTART::1761824568212*/
v_chargePoint.release();
v_delayedChargePointAccess = false;

if(!triggeredByB2){
	f_b3_notifyNeighbor(v_chargePoint);
}
if(v_chargePoint == null){
	traceln("Error charge point at EV " + this.getIndex() + " is null at release");
}

v_chargePoint = null;

main.leftCP++;
/*ALCODEEND*/}

double f_connectToCP(J_ChargePoint chargePoint)
{/*ALCODESTART::1761904993266*/
v_status = PARKED_CHARGE_POINT_CHARGING;
v_chargePoint = chargePoint;
v_chargePoint.occupy(this);
count_chargingSessions++;

main.connectedToCP++;
/*ALCODEEND*/}

