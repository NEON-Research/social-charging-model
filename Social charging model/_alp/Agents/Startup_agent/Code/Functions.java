double f_initializeModel()
{/*ALCODESTART::1746025123736*/
Main m = add_mains(v_cars,
	v_parkingPlaces,
	v_chargePoints,
	v_shareEVs,
	v_EVEnergyConsumption_kWhperKm,
	v_chargingPower_kW,
	v_timestep_minutes,
	v_numberOfTimesteps,
	v_checkCPAvailability,
	v_prosocialBehaviour,
	v_cpInteraction
	);

m.threshold_b1 = threshold_b1;
m.threshold_b2 = threshold_b2;
m.threshold_b3 = threshold_b3;
m.f_simulatePeriod(m.p_nbOfTimesteps);
m.viewArea.navigateTo();
/*ALCODEEND*/}

double f_setDefaultValues()
{/*ALCODESTART::1746086906741*/
v_cars = 100;
v_shareEVs = 0.50;
v_chargePoints = 11;
v_parkingPlaces = 100;

v_chargingPower_kW = 11;
v_EVEnergyConsumption_kWhperKm = 0.178;

v_timestep_minutes = 15;
v_simulationPeriod = "week";
v_periods = 1;

eb_cars.setText(v_cars);
eb_shareElectric.setText(v_shareEVs);
eb_chargePoints.setText(v_chargePoints);
eb_parkingPlaces.setText(v_parkingPlaces);
cb_simulationPeriod.setValue(v_simulationPeriod);
eb_nbOfPeriods.setText(v_periods, true);

cb_checkCPAvailability.setSelected(true, true);
cb_prosocialBehaviour.setSelected(false, true);
cb_cpInteraction.setSelected(false, true);

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

