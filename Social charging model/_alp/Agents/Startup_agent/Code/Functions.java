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

m.learningRate_norms_b1 = learningRate_norms_b1;
m.learningRate_norms_b2 = learningRate_norms_b2;
m.learningRate_norms_b3 = learningRate_norms_b3;
m.learningRate_trust_b1 = learningRate_trust_b1;
m.learningRate_trust_b2 = learningRate_trust_b2;
m.learningRate_trust_b3 = learningRate_trust_b3;

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

double f_setGraphs()
{/*ALCODESTART::1755013947834*/
Main m = mains.get(0);
int size = m.v_timestep;

//SUCCES RATE	
DataSet data_successRate_b1 = new DataSet(size);
DataSet data_successRate_b2 = new DataSet(size);
DataSet data_successRate_b3 = new DataSet(size);
DataSet data_successRate_rechecks = new DataSet(size);
for(int i=0; i < m.ar_succesRate_b1.length; i++){
	data_successRate_b1.add(i, m.ar_succesRate_b1[i]);
	data_successRate_b2.add(i, m.ar_succesRate_b2[i]);
	data_successRate_b3.add(i, m.ar_succesRate_b3[i]);
	data_successRate_b3.add(i, m.ar_succesRate_rechecks[i]);
}
pl_succesRate.removeAll();
pl_succesRate.addDataSet(data_successRate_b1, "Behavior 1: move car", sandyBrown, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_succesRate.addDataSet(data_successRate_b2, "Behavior 2: request move", lightSeaGreen, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_succesRate.addDataSet(data_successRate_b3, "Behavior 3: notify neighbor", lightSlateBlue, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_succesRate.addDataSet(data_successRate_rechecks, "Rechecking CP availability", mediumOrchid, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);

//PROBABILITY
DataSet data_avgProbability_b1 = new DataSet(size);
DataSet data_avgProbability_b2 = new DataSet(size);
DataSet data_avgProbability_b3 = new DataSet(size);
for(int i=0; i < m.ar_avgProbability_b1.length; i++){
	data_avgProbability_b1.add(i, m.ar_avgProbability_b1[i]);
	data_avgProbability_b2.add(i, m.ar_avgProbability_b2[i]);
	data_avgProbability_b3.add(i, m.ar_avgProbability_b3[i]);
}
pl_probability.removeAll();
pl_probability.addDataSet(data_avgProbability_b1, "Behavior 1: move car", sandyBrown, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_probability.addDataSet(data_avgProbability_b2, "Behavior 2: request move", lightSeaGreen, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_probability.addDataSet(data_avgProbability_b3, "Behavior 3: notify neighbor", lightSlateBlue, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);

//AVG SOCIOPSYCHOLOGICAL VARIABLES
DataSet data_avgNorms = new DataSet(size);
DataSet data_avgTrust = new DataSet(size);
DataSet data_avgPSI = new DataSet(size);
for(int i=0; i < ar_avgNorms.length; i++){
	data_avgNorms.add(i, ar_avgNorms[i]);
	data_avgTrust.add(i, ar_avgTrust[i]);
	data_avgPSI.add(i, ar_avgPSI[i]);
}
pl_socioPsychologicalLearning.removeAll();
pl_socioPsychologicalLearning.addDataSet(data_avgNorms, "Average Norms", sandyBrown, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_socioPsychologicalLearning.addDataSet(data_avgTrust, "Average Trust", lightSeaGreen, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_socioPsychologicalLearning.addDataSet(data_avgPSI, "Average PSI", lightSlateBlue, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);

//OCCURANCE
DataSet data_successful_b1 = new DataSet(size);
DataSet data_successful_b2 = new DataSet(size);
DataSet data_successful_b3 = new DataSet(size);
DataSet data_unsuccessful_b1 = new DataSet(size);
DataSet data_unsuccessful_b2 = new DataSet(size);
DataSet data_unsuccessful_b3 = new DataSet(size);
for(int i=0; i < m.ar_successful_b1.length; i++){
	data_successful_b1.add(i, m.ar_successful_b1[i]);
	data_successful_b2.add(i, m.ar_successful_b2[i]);
	data_successful_b3.add(i, m.ar_successful_b3[i]);
	data_unsuccessful_b1.add(i, m.ar_unsuccessful_b1[i]);
	data_unsuccessful_b2.add(i, m.ar_unsuccessful_b2[i]);
	data_unsuccessful_b3.add(i, m.ar_unsuccessful_b3[i]);
}
pl_occurance.removeAll();
pl_occurance.addDataSet(data_successful_b1, "Behavior 1: move car successful", sandyBrown, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_occurance.addDataSet(data_unsuccessful_b1, "Behavior 1: move car unsuccessful", peru, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_occurance.addDataSet(data_successful_b2, "Behavior 2: request move successful", lightSeaGreen, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_occurance.addDataSet(data_unsuccessful_b2, "Behavior 2: request move unsuccessful", darkSeaGreen, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_occurance.addDataSet(data_successful_b3, "Behavior 3: notify neighbor successful", lightSlateBlue, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_occurance.addDataSet(data_unsuccessful_b3, "Behavior 3: notify neighbor unsuccessful", darkSlateBlue, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);

/*ALCODEEND*/}

