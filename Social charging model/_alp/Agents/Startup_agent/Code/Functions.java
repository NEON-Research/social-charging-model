double f_initializeModel()
{/*ALCODESTART::1746025123736*/
int v_days = (int) floor(v_timestep_minutes * v_numberOfTimesteps / 24 / 60);
int v_chargePoints = roundToInt(v_cars * v_shareEVs / v_EVsPerCP);

Main m = add_mains(v_cars,
	v_parkingPlaces,
	v_chargePoints,
	v_shareEVs,
	v_EVEnergyConsumption_kWhperKm,
	v_chargingPower_kW,
	v_timestep_minutes,
	v_numberOfTimesteps,
	v_b1_moveCar,
	v_b2_requestMove,
	v_b3_notifyNeighbor,
	v_days
	);

m.v_socialChargingStartHour = v_socialChargingStartHour;
m.v_socialChargingEndHour = v_socialChargingEndHour;
m.v_socialChargingOnlyDaytime = v_socialChargingOnlyDaytime;
m.v_recheckCPAvailability = v_recheckCPAvailability;

m.f_simulatePeriod(m.p_nbOfTimesteps);
if(v_rapidRun == false){
	m.viewArea.navigateTo();
}
/*ALCODEEND*/}

double f_setDefaultValues()
{/*ALCODESTART::1746086906741*/
v_cars = 100;
v_shareEVs = 1;
v_EVsPerCP = 5;
v_parkingPlaces = 100;

v_socialChargingStartHour = 9;
v_socialChargingEndHour = 22;

v_chargingPower_kW = 11;
v_EVEnergyConsumption_kWhperKm = 0.178;

v_timestep_minutes = 15;
v_simulationPeriod = "week";
v_periods = 52;

eb_cars.setText(v_cars);
eb_shareElectric.setText(v_shareEVs);
eb_chargePoints.setText(v_EVsPerCP);
eb_parkingPlaces.setText(v_parkingPlaces);
cb_simulationPeriod.setValue(v_simulationPeriod);
eb_nbOfPeriods.setText(v_periods, true);

cb_b1.setSelected(true, true);
cb_b2.setSelected(true, true);
cb_b3.setSelected(true, true);
cb_socialChargingOnlyDaytime.setSelected(true, true);
cb_recheckCPAvailability.setSelected(true, true);

cb_scSartHour.setText(v_socialChargingStartHour);
cb_scEndHour.setText(v_socialChargingEndHour);

/*ALCODEEND*/}

double f_setNbOfTimesteps()
{/*ALCODESTART::1746087969109*/
int minutesPerPeriod = 0;
if(v_simulationPeriod.equals("day")){
	minutesPerPeriod = 60 * 24;
} else if(v_simulationPeriod.equals("week")){
	minutesPerPeriod = 60 * 24 * 7;
} else if(v_simulationPeriod.equals("year")){
	minutesPerPeriod = 60 * 24 * 365;
}

v_numberOfTimesteps = (int) (minutesPerPeriod * v_periods / v_timestep_minutes);
/*ALCODEEND*/}

