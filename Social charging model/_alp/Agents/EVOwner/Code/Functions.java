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

//Charge car if below threshold
if( v_soc < v_socChargingThreshold){
	wantsToCharge = true;
}

//Check available charge points
if(main.v_chargePointAvailable > 0){
	chargePointAvailable = true;
}

//Update status
if(wantsToCharge && chargePointAvailable){
	v_status = PARKED_CHARGE_POINT_CHARGING;
	main.v_chargePointAvailable--;
}
else if(wantsToCharge){
	v_status = PARKED_NON_CHARGE_POINT_CHARGING_REQUIRED;
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

double f_requestToMoveAVehicleFromChargePoint()
{/*ALCODESTART::1748950054381*/

/*ALCODEEND*/}

double f_notifyChargePointAvailable()
{/*ALCODESTART::1748950054518*/

/*ALCODEEND*/}

double f_movingVehicleFromChargePoint()
{/*ALCODESTART::1748950054520*/

/*ALCODEEND*/}

