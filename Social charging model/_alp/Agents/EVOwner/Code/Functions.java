double f_findChargePoint()
{/*ALCODESTART::1746011747637*/
//Charge car if below threshold
if(main.v_chargePointAvailable > 0){
	f_chargeCar();
}


if(v_soc < 0){
	traceln("ERROR - soc in car " + this.getIndex() + " is below 0 at " + v_soc);
}
/*ALCODEEND*/}

double f_chargeCar()
{/*ALCODESTART::1746011766273*/
if(v_soc < 1){
	double timestepsPerHour = 60 / main.p_timestep_minutes;
	double charge_kWhperTimestep = main.p_chargingPower_kW / timestepsPerHour;
	v_electricityInBattery_kWh += charge_kWhperTimestep;
	//traceln("EV " + this.getIndex() + " is charging in status " + v_status + ", current charge " + charge_kWhperTimestep + " current bat cap "+ v_electricityInBattery_kWh + "kWh, cap " + v_batteryCapacity_kWh + "kwh and soc " + v_soc);
	if( v_electricityInBattery_kWh >= v_batteryCapacity_kWh){
		v_electricityInBattery_kWh = v_batteryCapacity_kWh;
		v_status = PARKED_CHARGE_POINT_IDLE;
		
		if(main.initializationMode == false){
			f_b1_movingVehicle();
		}
	}
}

v_soc = v_electricityInBattery_kWh / v_batteryCapacity_kWh;
/*ALCODEEND*/}

double f_updateSOC(double tripDistance_km)
{/*ALCODESTART::1746011780748*/
//Update SOC
double electricityConsumed_kWh = tripDistance_km;
v_electricityInBattery_kWh -= electricityConsumed_kWh;
v_soc = v_electricityInBattery_kWh / v_batteryCapacity_kWh;
if(v_soc < 0){
	double outOfModelCharging_soc = abs(v_soc) + 0.1; //Cars charge to 10% when doing fast charging outside of model
	v_outOfModelCharge_kWh += (outOfModelCharging_soc * v_batteryCapacity_kWh);
	v_electricityInBattery_kWh += v_outOfModelCharge_kWh;
	v_soc = v_electricityInBattery_kWh / v_batteryCapacity_kWh;
	//traceln("ERROR - soc in car " + this.getIndex() + " is below 0 at " + v_soc);
}



/*ALCODEEND*/}

double f_setParkingStatus()
{/*ALCODESTART::1747136105778*/
Status currentStatus = v_status;

boolean wantsToCharge = false;
boolean chargePointAvailable = false;
boolean foundCPThroughRequest = false;

//Charge car if below threshold
if( v_soc < v_socChargingThreshold){
	wantsToCharge = true;
}

//Check available charge points
if(main.v_chargePointAvailable > 0){
	chargePointAvailable = true;
}


//Update status
if(wantsToCharge){
	if(chargePointAvailable){
		v_status = PARKED_CHARGE_POINT_CHARGING;
		main.v_chargePointAvailable--;
		if(main.v_chargePointAvailable < 0){
			traceln("CP available = " + main.v_chargePointAvailable + " after regular charging");
		}
	}
	else if(wantsToCharge && !chargePointAvailable && main.initializationMode == false){
		//Behaviour 2: Request neighbor to move EV
		f_b2_requestMove();
	}
	else{
		v_status = PARKED_NON_CHARGE_POINT_CHARGING_REQUIRED;
	}
}
else {
	v_status = PARKED_NON_CHARGE_POINT_CHARGING_NOT_REQUIRED;
}

/*
if(currentStatus == PARKED_NON_CHARGE_POINT_CHARGING_REQUIRED && v_status == PARKED_CHARGE_POINT_CHARGING){
	traceln("EV " + this.getIndex() + " has moved from parking to charge point and started charging");
}
*/
/*ALCODEEND*/}

double f_b1_movingVehicle()
{/*ALCODESTART::1748950054520*/
boolean actBehavior = f_actBehavior(v_prob_b1);
f_moveVehicle(actBehavior);


/*ALCODEEND*/}

double f_moveVehicle(boolean actBehavior)
{/*ALCODESTART::1753175296086*/
//Move vehicle
if( actBehavior && main.v_parkingPlacesAvailable > 0 ){
	v_status = PARKED_NON_CHARGE_POINT_CHARGING_NOT_REQUIRED;
	main.v_chargePointAvailable++;
	count_b1_successful++;
}
else {
	count_b1_notSuccessful++;
}

int total_b1 = count_b1_successful + count_b1_notSuccessful;
successRate_b1 = (total_b1 != 0) ? ((double) count_b1_successful / total_b1) : 0;

//main.hs_data_utility_b1_all.add(utility_b1);
//main.hs_data_b1_all.add(b1);

//Social learning after interaction
f_socialLearning_b1();

//If moved, notify neighbor
f_b3_notifyNeighbor();

/*ALCODEEND*/}

double f_b2_requestMove()
{/*ALCODESTART::1753175364935*/
boolean actBehavior = f_actBehavior(v_prob_b2);
f_requestMove(actBehavior);




/*ALCODEEND*/}

double f_requestMove(boolean actBehavior)
{/*ALCODESTART::1753175364937*/
boolean succesfulMoveRequest = f_successfulMoveRequest(actBehavior);

if(succesfulMoveRequest){
	//Get moving EV and change status
	EVOwner movingEV = randomWhere(main.EVOwners, x->x.v_status == PARKED_CHARGE_POINT_IDLE);
	movingEV.v_status = PARKED_NON_CHARGE_POINT_CHARGING_NOT_REQUIRED;
	movingEV.count_fulfilledMoveRequest++;
			
	//Change status of this EV
	v_status = PARKED_CHARGE_POINT_CHARGING;
	count_b2_successful++;
}
else {
	count_b2_notSuccessful++;
}

int total_b2 = count_b2_successful + count_b2_notSuccessful;
successRate_b2 = (total_b2 != 0) ? ((double) count_b2_successful / total_b2) : 0;

//main.hs_data_utility_b2_all.add(utility_b2);
//main.hs_data_b2_all.add(b2);

//Social learning after interaction
f_socialLearning_b2();
/*ALCODEEND*/}

double f_b3_notifyNeighbor()
{/*ALCODESTART::1753175429703*/
boolean actBehavior = f_actBehavior(v_prob_b3);
f_notifyNeighbor(actBehavior);



/*ALCODEEND*/}

double f_notifyNeighbor(boolean actBehavior)
{/*ALCODESTART::1753175429706*/
if( actBehavior && main.v_EVsParkedNonCPChargingRequired > 0 ){
	//Get notified EV Owner
	EVOwner EVNotified = randomWhere(main.EVOwners, x->x.v_status == PARKED_NON_CHARGE_POINT_CHARGING_REQUIRED);
	EVNotified.v_status = PARKED_CHARGE_POINT_CHARGING;
	main.v_chargePointAvailable--;
	if(main.v_chargePointAvailable < 0){
		traceln("CP available = " + main.v_chargePointAvailable + " after b3");
	}
	count_b3_successful++;
}
else {
	count_b3_notSuccessful++;
}

int total_b3 = count_b3_successful + count_b3_notSuccessful;
successRate_b3 = (total_b3 != 0) ? ((double) count_b3_successful / total_b3) : 0;

//main.hs_data_utility_b3_all.add(utility_b3);
//main.hs_data_b3_all.add(b3);

//Social learning after interaction
f_socialLearning_b3();
/*ALCODEEND*/}

double f_setInitialUtility(double modelEffect)
{/*ALCODESTART::1753189354451*/
//Utility
f_setPerceivedSocialInterdependence();

double rand = uniform();
utility_b1 = v_perceived_social_interdependence * modelEffect + rand * (1-modelEffect);
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
double psi_t0 = v_perceived_social_interdependence;
double prob_b1_t0 = v_prob_b1;

double norms_t1 = norms_t0 + main.learningRate_norms_b1 * (observedFraction - norms_t0);
double trust_t1 = trust_t0 + main.learningRate_trust_b1 * (observedFraction - trust_t0);
double psi_t1 = psi_t0 + main.regCoef_norms_psi * (norms_t1 - norms_t0) + main.regCoef_trust_psi * (trust_t1 - trust_t0);

double prob_b1_t1 = prob_b1_t0 + main.regCoef_psi_b1 * (psi_t1 - psi_t0);

v_norms = norms_t1;
v_trust = trust_t1;
v_perceived_social_interdependence = psi_t1;
v_prob_b1 = prob_b1_t1;

/*ALCODEEND*/}

boolean f_successfulMoveRequest(boolean actBehavior)
{/*ALCODESTART::1754391554916*/
boolean act = false;
if( actBehavior ){
	if(main.v_EVsParkedAtCPIdle > 0){
		if(main.v_EVsParkedAtCPIdle != count(main.EVOwners, x->x.v_status == PARKED_CHARGE_POINT_IDLE)){
			traceln("ERROR: v_EVsparkedAtCPIdle = " + main.v_EVsParkedAtCPIdle + " and v_status == PARKED_CHARGE_POINT_IDLE = " + count(main.EVOwners, x->x.v_status == PARKED_CHARGE_POINT_IDLE));
		}
		//S-shape curve y between 0.2 and 0.6, x between 0 - 1 based on succesRate
		double scale = 10.0; // Steepness of the S
		double x_shift = 0.5; // Center point of the S
		double sigmoid = 1.0 / (1.0 + Math.exp(-scale * (main.successRate_b2 - x_shift))); // Output in (0,1)
		double probability = 0.2 + 0.4 * sigmoid;
		double rand = uniform();
		
		//traceln("in b2 prob = " + probability + " with succes rate " + main.successRate_b2);
		
		if( rand < probability ){
			act = true;
		}	
	}
}
	

return act;
/*ALCODEEND*/}

double f_socialLearning_b2()
{/*ALCODESTART::1754404097901*/
//Update social learning after b2 interaction (successful or unsuccessful)
double observedFraction = main.successRate_b2;

double norms_t0 = v_norms;
double trust_t0 = v_trust;
double psi_t0 = v_perceived_social_interdependence;
double prob_b2_t0 = v_prob_b2;

double norms_t1 = norms_t0 + main.learningRate_norms_b2 * (observedFraction - norms_t0);
double trust_t1 = trust_t0 + main.learningRate_trust_b2 * (observedFraction - trust_t0);
double psi_t1 = psi_t0 + main.regCoef_norms_psi * (norms_t1 - norms_t0) + main.regCoef_trust_psi * (trust_t1 - trust_t0);

double prob_b2_t1 = prob_b2_t0 + main.regCoef_psi_b2 * (psi_t1 - psi_t0);

v_norms = norms_t1;
v_trust = trust_t1;
v_perceived_social_interdependence = psi_t1;
v_prob_b2 = prob_b2_t1;

/*ALCODEEND*/}

double f_socialLearning_b3()
{/*ALCODESTART::1754404103304*/
//Update social learning after b3 interaction (successful or unsuccessful)
double observedFraction = main.successRate_b3;

double norms_t0 = v_norms;
double trust_t0 = v_trust;
double psi_t0 = v_perceived_social_interdependence;
double prob_b3_t0 = v_prob_b3;

double norms_t1 = norms_t0 + main.learningRate_norms_b3 * (observedFraction - norms_t0);
double trust_t1 = trust_t0 + main.learningRate_trust_b3 * (observedFraction - trust_t0);
double psi_t1 = psi_t0 + main.regCoef_norms_psi * (norms_t1 - norms_t0) + main.regCoef_trust_psi * (trust_t1 - trust_t0);

double prob_b3_t1 = prob_b3_t0 + main.regCoef_psi_b3 * (psi_t1 - psi_t0);

v_norms = norms_t1;
v_trust = trust_t1;
v_perceived_social_interdependence = psi_t1;
v_prob_b3 = prob_b3_t1;

/*ALCODEEND*/}

