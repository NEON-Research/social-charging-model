double f_initializeModel()
{/*ALCODESTART::1746025123736*/
int v_days = (int) floor(v_timestep_minutes * v_numberOfTimesteps / 24 / 60);
v_chargePoints = roundToInt(v_cars * v_shareEVs / v_EVsPerCP);

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
m.learningRate_pcp_b1 = learningRate_pcp_b1;
m.learningRate_pcp_b2 = learningRate_pcp_b2;
m.learningRate_pcp_b3 = learningRate_pcp_b3;
m.learningRate_trust_b2 = learningRate_trust_b2;
m.learningRate_trust_b3 = learningRate_trust_b3;

m.mean_norms = mean_norms;
m.mean_pcp = mean_pcp;
m.mean_rc = mean_rc;
m.mean_psi = mean_psi;
m.mean_b1 = mean_b1;
m.mean_b2 = mean_b2;
m.mean_b3 = mean_b3;

m.sd_norms = sd_norms;
m.sd_pcp = sd_pcp;
m.sd_rc = sd_rc;
m.sd_psi = sd_psi;
m.sd_b1 = sd_b1;
m.sd_b2 = sd_b2;
m.sd_b3 = sd_b3;

m.regCoef_norms_psi_b1 = regCoef_norms_psi_b1;
m.regCoef_norms_psi_b2b3 = regCoef_norms_psi_b2b3;
m.regCoef_pcp_b2 = regCoef_pcp_b2;
m.regCoef_pcp_b3 = regCoef_pcp_b3;
m.regCoef_pcp_psi_b1 = regCoef_pcp_psi_b1;
m.regCoef_pcp_psi_b2b3 = regCoef_pcp_psi_b2b3;
m.regCoef_psi_b1 = regCoef_psi_b1;
m.regCoef_psi_b2 = regCoef_psi_b2;
m.regCoef_psi_b3 = regCoef_psi_b3;
m.regCoef_rc_b1 = regCoef_rc_b1;
m.regCoef_rc_psi_b1 = regCoef_rc_psi_b1;
m.regCoef_rc_psi_b2b3 = regCoef_rc_psi_b2b3;

m.correlationMatrix = correlationMatrix;
m.sortedRealData = sortedRealData;

m.f_initializeModel();
m.f_simulatePeriod(m.p_nbOfTimesteps);
if(v_rapidRun == false){
	m.viewArea.navigateTo();
	//traceln("should have navigated to view area");
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

if( monteCarlo ){
	f_storeMCArrays();
}/*
else {
	Color color = randomFrom(c_colorPalette);
	String title = "run " + simulationCount;
	f_setRunText(color, title);
	
	
	//SUCCES RATE	
	DataSet data_successRate_b1 = new DataSet(size);
	DataSet data_successRate_b2 = new DataSet(size);
	DataSet data_successRate_b3 = new DataSet(size);
	DataSet data_successRate_rechecks = new DataSet(size);
	for(int i=0; i < m.ar_successRate_b1.length; i++){
		data_successRate_b1.add(i, m.ar_successRate_b1[i]);
		data_successRate_b2.add(i, m.ar_successRate_b2[i]);
		data_successRate_b3.add(i, m.ar_successRate_b3[i]);
		data_successRate_rechecks.add(i, m.ar_successRate_rechecks[i]);
	}
	
	//PROBABILITY
	DataSet data_avgProbability_b1 = new DataSet(size);
	DataSet data_avgProbability_b2 = new DataSet(size);
	DataSet data_avgProbability_b3 = new DataSet(size);
	for(int i=0; i < m.ar_avgProbability_b1.length; i++){
		data_avgProbability_b1.add(i, m.ar_avgProbability_b1[i]);
		data_avgProbability_b2.add(i, m.ar_avgProbability_b2[i]);
		data_avgProbability_b3.add(i, m.ar_avgProbability_b3[i]);
	}
	
	//AVG SOCIOPSYCHOLOGICAL VARIABLES
	DataSet data_avgNorms = new DataSet(size);
	DataSet data_avgRC = new DataSet(size);
	DataSet data_avgPSI = new DataSet(size);
	DataSet data_avgPCP = new DataSet(size);
	for(int i=0; i < m.ar_avgNorms.length; i++){
		data_avgNorms.add(i, m.ar_avgNorms[i]);
		data_avgRC.add(i, m.ar_avgRC[i]);
		data_avgPSI.add(i, m.ar_avgPSI[i]);
		data_avgPCP.add(i, m.ar_avgPCP[i]);
	}
		
	pl_successRate1.setFixedHorizontalScale(0, v_numberOfTimesteps);
	pl_successRate2.setFixedHorizontalScale(0, v_numberOfTimesteps);
	pl_successRate3.setFixedHorizontalScale(0, v_numberOfTimesteps);
	pl_avgProb1.setFixedHorizontalScale(0, v_numberOfTimesteps);
	pl_avgProb2.setFixedHorizontalScale(0, v_numberOfTimesteps);
	pl_avgProb3.setFixedHorizontalScale(0, v_numberOfTimesteps);
	
	pl_successRate1.addDataSet(data_successRate_b1, title, color, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
	pl_successRate2.addDataSet(data_successRate_b2, title, color, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
	pl_successRate3.addDataSet(data_successRate_b3, title, color, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
	pl_avgProb1.addDataSet(data_avgProbability_b1, title, color, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
	pl_avgProb2.addDataSet(data_avgProbability_b2, title, color, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
	pl_avgProb3.addDataSet(data_avgProbability_b3, title, color, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
		
	
	if( setDetailedGraphs ){
		//SET SCALE
		pl_probability.setFixedHorizontalScale(0, v_numberOfTimesteps);
		pl_successRate.setFixedHorizontalScale(0, v_numberOfTimesteps);
		pl_probability.setFixedHorizontalScale(0, v_numberOfTimesteps);
		pl_socioPsychologicalLearning.setFixedHorizontalScale(0, v_numberOfTimesteps);
		pl_occurance.setFixedHorizontalScale(0, v_numberOfTimesteps);
		//pl_unfulfilledChargingSession.setFixedHorizontalScale(0, v_numberOfTimesteps);
		
		//SUCCES RATE	
		pl_successRate.removeAll();
		pl_successRate.addDataSet(data_successRate_b1, "Behavior 1: move car", sandyBrown, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
		pl_successRate.addDataSet(data_successRate_b2, "Behavior 2: request move", lightSeaGreen, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
		pl_successRate.addDataSet(data_successRate_b3, "Behavior 3: notify neighbor", lightSlateBlue, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
		pl_successRate.addDataSet(data_successRate_rechecks, "Rechecking CP availability", mediumOrchid, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
			
		//PROBABILITY
		pl_probability.removeAll();
		pl_probability.addDataSet(data_avgProbability_b1, "Behavior 1: move car", sandyBrown, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
		pl_probability.addDataSet(data_avgProbability_b2, "Behavior 2: request move", lightSeaGreen, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
		pl_probability.addDataSet(data_avgProbability_b3, "Behavior 3: notify neighbor", lightSlateBlue, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
		
		//AVG SOCIOPSYCHOLOGICAL VARIABLES
		pl_socioPsychologicalLearning.removeAll();
		pl_socioPsychologicalLearning.addDataSet(data_avgNorms, "Average Norms", sandyBrown, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
		pl_socioPsychologicalLearning.addDataSet(data_avgRC, "Average RC", lightSeaGreen, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
		pl_socioPsychologicalLearning.addDataSet(data_avgPSI, "Average PSI", lightSlateBlue, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
		pl_socioPsychologicalLearning.addDataSet(data_avgPCP, "Average PCP", orchid, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
		
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
	}
}*/
/*ALCODEEND*/}

double f_setGraphsPerRun(Color color,String title)
{/*ALCODESTART::1755157664950*/
Main m = mains.get(0);

DataItem di1 = new DataItem();
di1.setValue(m.v_unfulfilledChargingSessions_perc);
DataItem di2 = new DataItem();
di2.setValue(m.v_outOfModelCharging_perc);

//ch_percOutOfModelCharge.addDataItem(di1, title, color);
//ch_percUnfulfilledChargingSessions.addDataItem(di2, title, color);

double xOval = 1500;
double yOvalStart = 550;
double yOval = yOvalStart + simulationCount * 25;
double rotationOval = 0;
Color lineColor = color;
Color fillColor = color;
double radiusX = 8;
double radiusY = 8;
double lineWidth = 0;
LineStyle lineStyle = LINE_STYLE_SOLID;    
    
ShapeOval oval = new ShapeOval(true, xOval, yOval, rotationOval, lineColor, fillColor, radiusX, radiusY, lineWidth, lineStyle);
presentation.add(oval);

ShapeDrawMode drawMode = presentation.getDrawMode();
double xText = xOval + 20;
double yText = yOvalStart - 10 + simulationCount * 25;
double z = 0;
double rotationText = 0;
String text = title;
Font font = new Font("SansSerif", Font.PLAIN, 11);
TextAlignment alignment = TextAlignment.ALIGNMENT_LEFT;

ShapeText textShape = new ShapeText(drawMode, true, xText, yText, z, rotationText, black, text, font, alignment);
presentation.add(textShape);
/*ALCODEEND*/}

double f_setColorPalette()
{/*ALCODESTART::1755180600314*/
// Adding 50 readable and matching c_colorPalette
c_colorPalette.add(new Color(0xFF5733));   // Orange Red
c_colorPalette.add(new Color(0xC70039));   // Red
c_colorPalette.add(new Color(0x900C3F));   // Dark Red
c_colorPalette.add(new Color(0x581845));   // Dark Purple
c_colorPalette.add(new Color(0xFFC300));   // Yellow
c_colorPalette.add(new Color(0xFF5733));   // Orange
c_colorPalette.add(new Color(0xDAF7A6));   // Light Green
c_colorPalette.add(new Color(0x33FF57));   // Green
c_colorPalette.add(new Color(0x28B463));   // Dark Green
c_colorPalette.add(new Color(0x239B56));   // Forest Green
c_colorPalette.add(new Color(0x1F618D));   // Navy Blue
c_colorPalette.add(new Color(0x2874A6));   // Steel Blue
c_colorPalette.add(new Color(0x5DADE2));   // Sky Blue
c_colorPalette.add(new Color(0x85C1E9));   // Light Blue
c_colorPalette.add(new Color(0x154360));   // Midnight Blue
c_colorPalette.add(new Color(0x76448A));   // Medium Purple
c_colorPalette.add(new Color(0x7D3C98));   // Plum
c_colorPalette.add(new Color(0x884EA0));   // Amethyst
c_colorPalette.add(new Color(0xA569BD));   // Orchid
c_colorPalette.add(new Color(0xE74C3C));   // Red Orange
c_colorPalette.add(new Color(0xF39C12));   // Sunflower
c_colorPalette.add(new Color(0xF1C40F));   // Golden Yellow
c_colorPalette.add(new Color(0xD35400));   // Pumpkin
c_colorPalette.add(new Color(0xBA4A00));   // Burnt Orange
c_colorPalette.add(new Color(0x1ABC9C));   // Turquoise
c_colorPalette.add(new Color(0x16A085));   // Teal
c_colorPalette.add(new Color(0xF7DC6F));   // Pastel Yellow
c_colorPalette.add(new Color(0xF4D03F));   // Bright Yellow
c_colorPalette.add(new Color(0x229954));   // Emerald Green
c_colorPalette.add(new Color(0x3498DB));   // Bright Blue
c_colorPalette.add(new Color(0x2ECC71));   // Light Green
c_colorPalette.add(new Color(0x48C9B0));   // Aquamarine
c_colorPalette.add(new Color(0x45B39D));   // Sea Green
c_colorPalette.add(new Color(0x138D75));   // Dark Teal
c_colorPalette.add(new Color(0x117864));   // Pine Green
c_colorPalette.add(new Color(0x1F618D));   // Dark Blue
c_colorPalette.add(new Color(0x5B2C6F));   // Royal Purple
c_colorPalette.add(new Color(0x6C3483));   // Deep Purple
c_colorPalette.add(new Color(0x7FB3D5));   // Pastel Blue
c_colorPalette.add(new Color(0x85929E));   // Gray Blue
c_colorPalette.add(new Color(0xD98880));   // Soft Red
c_colorPalette.add(new Color(0xCD6155));   // Coral Red
c_colorPalette.add(new Color(0xA93226));   // Crimson
c_colorPalette.add(new Color(0x873600));   // Mahogany
c_colorPalette.add(new Color(0xA04000));   // Sienna
c_colorPalette.add(new Color(0xB9770E));   // Bronze
c_colorPalette.add(new Color(0xAF601A));   // Rust
c_colorPalette.add(new Color(0xD4AC0D));   // Mustard Yellow
c_colorPalette.add(new Color(0xB7950B));   // Olive
c_colorPalette.add(new Color(0xD68910));   // Amber

/*ALCODEEND*/}

double f_storeMCArrays()
{/*ALCODESTART::1755183120871*/
Main m = mains.get(0);

c_succesRate_b1_MC.add(f_dailyToWeekly(m.ar_successRate_b1));
c_succesRate_b2_MC.add(f_dailyToWeekly(m.ar_successRate_b2));
c_succesRate_b3_MC.add(f_dailyToWeekly(m.ar_successRate_b3));
c_avgProb_b1_MC.add(f_dailyToWeekly(m.ar_avgProbability_b1));
c_avgProb_b2_MC.add(f_dailyToWeekly(m.ar_avgProbability_b2));
c_avgProb_b3_MC.add(f_dailyToWeekly(m.ar_avgProbability_b3));

c_PCP_MC.add(f_dailyToWeekly(m.ar_avgPCP));
c_RC_MC.add(f_dailyToWeekly(m.ar_avgRC));
c_PSI_MC.add(f_dailyToWeekly(m.ar_avgPSI));
c_norm1_MC.add(f_dailyToWeekly(m.ar_avgNorm_b1));
c_norm2_MC.add(f_dailyToWeekly(m.ar_avgNorm_b2));
c_norm3_MC.add(f_dailyToWeekly(m.ar_avgNorm_b3));

c_kmDrivenPerWeek.add(m.ar_kmDrivenPerWeek.clone());

c_b1_perWeek.add(m.ar_interactionsPerWeek_b1.clone());
c_b2_perWeek.add(m.ar_interactionsPerWeek_b2.clone());
c_b3_perWeek.add(m.ar_interactionsPerWeek_b3.clone());
c_b1Successful_perWeek.add(m.ar_successful_b1.clone());
c_b2Successful_perWeek.add(m.ar_successful_b2.clone());
c_b3Successful_perWeek.add(m.ar_successful_b3.clone());
c_b1Unsuccessful_perWeek.add(m.ar_unsuccessful_b1.clone());
c_b2Unsuccessful_perWeek.add(m.ar_unsuccessful_b2.clone());
c_b3Unsuccessful_perWeek.add(m.ar_unsuccessful_b3.clone());

/*
c_outOfModelChargingPerDay.add(m.ar_outOfModelCharging);
c_leftWhileChargingPerDay.add(m.ar_leftWhileCharging);
c_leftWhileChargingWithDelayedAccessPerDay.add(m.ar_leftWhileChargingWithDelayedAccess);
c_leftUnchargedPerDay.add(m.ar_leftUncharged);
c_percSatisfiedChargingSessionsPerDay.add(m.ar_percSatisfiedChargingSessions);

c_chargingSessionsPerDay.add(m.ar_chargingSessions);
c_requiredChargingSessionsPerDay.add(m.ar_requiredChargingSessions);
*/

c_outOfModelChargingPerWeek.add(m.ar_outOfModelCharging.clone());
c_leftWhileChargingPerWeek.add(m.ar_leftWhileCharging.clone());
c_leftWhileChargingWithDelayedAccessPerWeek.add(m.ar_leftWhileChargingWithDelayedAccess.clone());
c_leftUnchargedPerWeek.add(m.ar_leftUncharged.clone());
c_percSatisfiedChargingSessionsPerWeek.add(m.ar_percSatisfiedChargingSessions.clone());

c_chargingSessionsPerWeek.add(m.ar_chargingSessions.clone());
c_requiredChargingSessionsPerWeek.add(m.ar_requiredChargingSessions.clone());

c_tripsPerWeek.add(m.ar_tripsPerWeek.clone());
/*ALCODEEND*/}

double f_setMCGraphs(J_MCResult results)
{/*ALCODESTART::1755183201656*/
//Create datasets
int size = results.getSuccessRate_b1().get(0).length;
int weeks = results.getLeftUnchargedPerWeek().get(0).length;
DataSet data_successRate_b1 = new DataSet(size);
DataSet data_successRate_b2 = new DataSet(size);
DataSet data_successRate_b3 = new DataSet(size);
DataSet data_avgProbability_b1 = new DataSet(size);
DataSet data_avgProbability_b2 = new DataSet(size);
DataSet data_avgProbability_b3 = new DataSet(size);
DataSet data_outOfModelCharging = new DataSet(weeks);
DataSet data_leftWhileCharging = new DataSet(weeks);
DataSet data_leftUncharged = new DataSet(weeks);
DataSet data_percSatisfiedChargingSessions = new DataSet(weeks);
DataSet data_chargingSessions = new DataSet(weeks);
DataSet data_requiredChargingSessions = new DataSet(weeks);
//DataSet data_successRate_rechecks = new DataSet(size);

for(int i = 0; i < size; i++){
	data_successRate_b1.add(i, results.getSuccessRate_b1().get(0)[i]);
	data_successRate_b2.add(i, results.getSuccessRate_b2().get(0)[i]);
	data_successRate_b3.add(i, results.getSuccessRate_b3().get(0)[i]);
	
	data_avgProbability_b1.add(i, results.getAvgProb_b1().get(0)[i]);
	data_avgProbability_b2.add(i, results.getAvgProb_b2().get(0)[i]);
	data_avgProbability_b3.add(i, results.getAvgProb_b3().get(0)[i]);
}
/*
for(int i = 0; i < days; i++){	
	data_outOfModelCharging.add(i, results.getOutOfModelChargingRollingAvg().get(0)[i]);
	data_leftUncharged.add(i, results.getLeftUnchargedRollingAvg().get(0)[i]);	
	data_leftWhileCharging.add(i, results.getLeftWhileChargingRollingAvg().get(0)[i]);
	//data_percSatisfiedChargingSessions.add(i, results.getPercSatisfiedChargingSessionsRollingAvg().get(0)[i]);
	data_requiredChargingSessions.add(i, results.getRequiredChargingSessionsRollingAvg().get(0)[i]);
	data_chargingSessions.add(i, results.getChargingSessionsRollingAvg().get(0)[i]);
}  
*/
for(int i = 0; i < weeks; i++){	
	data_outOfModelCharging.add(i, results.getOutOfModelChargingPerWeek().get(0)[i]);
	data_leftUncharged.add(i, results.getLeftUnchargedPerWeek().get(0)[i]);	
	data_leftWhileCharging.add(i, results.getLeftWhileChargingPerWeek().get(0)[i]);
	data_percSatisfiedChargingSessions.add(i, results.getPercSatisfiedChargingSessionsPerWeek().get(0)[i]);
	data_requiredChargingSessions.add(i, results.getRequiredChargingSessionsPerWeek().get(0)[i]);
	data_chargingSessions.add(i, results.getChargingSessionsPerWeek().get(0)[i]);
}

Color color = randomFrom(c_colorPalette);
String title = "run " + simulationCount;
f_setRunText(color, title);

pl_successRate1.setFixedHorizontalScale(0, size);
pl_successRate2.setFixedHorizontalScale(0, size);
pl_successRate3.setFixedHorizontalScale(0, size);
pl_avgProb1.setFixedHorizontalScale(0, size);
pl_avgProb2.setFixedHorizontalScale(0, size);
pl_avgProb3.setFixedHorizontalScale(0, size);

pl_outOfModelCharge.setFixedHorizontalScale(0, weeks);
pl_chargingSatisfaction.setFixedHorizontalScale(0, weeks);
pl_leftWhileCharging.setFixedHorizontalScale(0, weeks);
pl_leftUncharged.setFixedHorizontalScale(0, weeks);
pl_chargingSessions.setFixedHorizontalScale(0, weeks);
pl_requiredChargingSessions.setFixedHorizontalScale(0, weeks);

pl_successRate1.addDataSet(data_successRate_b1, title, color, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_successRate2.addDataSet(data_successRate_b2, title, color, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_successRate3.addDataSet(data_successRate_b3, title, color, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_avgProb1.addDataSet(data_avgProbability_b1, title, color, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_avgProb2.addDataSet(data_avgProbability_b2, title, color, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_avgProb3.addDataSet(data_avgProbability_b3, title, color, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);

pl_chargingSatisfaction.addDataSet(data_percSatisfiedChargingSessions, title, color, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_outOfModelCharge.addDataSet(data_outOfModelCharging, title, color, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_leftWhileCharging.addDataSet(data_leftWhileCharging, title, color, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_leftUncharged.addDataSet(data_leftUncharged, title, color, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);

pl_chargingSessions.addDataSet(data_chargingSessions, title, color, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_requiredChargingSessions.addDataSet(data_requiredChargingSessions, title, color, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
/*ALCODEEND*/}

DataSet f_writeMCToExcel()
{/*ALCODESTART::1755518313240*/
//Get excel file and indexes
//excel_exportResults.readFile();


int sheetIndex = 1;
int sheetIndexPerWeek = 2;

for(J_MCResult r : c_MCResults){

	int scenarioIndex = r.getScenarioIndex();
	int rowIndex = f_getTrueLastRow(sheetIndex, excel_exportResults) + 1;
	int nTimePoints = r.getSuccessRate_b1().get(0).length;
	
	for( int t = 0; t < nTimePoints; t++ ){
		
		double meanSRB1 = r.getSuccessRate_b1().get(0)[t];
		double lowerSRB1 = r.getSuccessRate_b1().get(1)[t];
		double upperSRB1 = r.getSuccessRate_b1().get(2)[t];
		
		double meanSRB2 = r.getSuccessRate_b2().get(0)[t];
		double lowerSRB2 = r.getSuccessRate_b2().get(1)[t];
		double upperSRB2 = r.getSuccessRate_b2().get(2)[t];
		
		double meanSRB3 = r.getSuccessRate_b3().get(0)[t];
		double lowerSRB3 = r.getSuccessRate_b3().get(1)[t];
		double upperSRB3 = r.getSuccessRate_b3().get(2)[t];
		
		double meanAPB1 = r.getAvgProb_b1().get(0)[t];
		double lowerAPB1 = r.getAvgProb_b1().get(1)[t];
		double upperAPB1 = r.getAvgProb_b1().get(2)[t];
		
		double meanAPB2 = r.getAvgProb_b2().get(0)[t];
		double lowerAPB2 = r.getAvgProb_b2().get(1)[t];
		double upperAPB2 = r.getAvgProb_b2().get(2)[t];
		
		double meanAPB3 = r.getAvgProb_b3().get(0)[t];
		double lowerAPB3 = r.getAvgProb_b3().get(1)[t];
		double upperAPB3 = r.getAvgProb_b3().get(2)[t];
		
		int day = t / 96;
				
		excel_exportResults.setCellValue(scenarioIndex, sheetIndex, rowIndex, 1);
		excel_exportResults.setCellValue(t, sheetIndex, rowIndex, 2);
		excel_exportResults.setCellValue(day, sheetIndex, rowIndex, 3);
		
		excel_exportResults.setCellValue(meanSRB1, sheetIndex, rowIndex, 4);
		excel_exportResults.setCellValue(lowerSRB1, sheetIndex, rowIndex, 5);
		excel_exportResults.setCellValue(upperSRB1, sheetIndex, rowIndex, 6);
		
		excel_exportResults.setCellValue(meanSRB2, sheetIndex, rowIndex, 7);
		excel_exportResults.setCellValue(lowerSRB2, sheetIndex, rowIndex, 8);
		excel_exportResults.setCellValue(upperSRB2, sheetIndex, rowIndex, 9);
		
		excel_exportResults.setCellValue(meanSRB3, sheetIndex, rowIndex, 10);
		excel_exportResults.setCellValue(lowerSRB3, sheetIndex, rowIndex, 11);
		excel_exportResults.setCellValue(upperSRB3, sheetIndex, rowIndex, 12);
		
		excel_exportResults.setCellValue(meanAPB1, sheetIndex, rowIndex, 13);
		excel_exportResults.setCellValue(lowerAPB1, sheetIndex, rowIndex, 14);
		excel_exportResults.setCellValue(upperAPB1, sheetIndex, rowIndex, 15);
		
		excel_exportResults.setCellValue(meanAPB2, sheetIndex, rowIndex, 16);
		excel_exportResults.setCellValue(lowerAPB2, sheetIndex, rowIndex, 17);
		excel_exportResults.setCellValue(upperAPB2, sheetIndex, rowIndex, 18);
		
		excel_exportResults.setCellValue(meanAPB3, sheetIndex, rowIndex, 19);
		excel_exportResults.setCellValue(lowerAPB3, sheetIndex, rowIndex, 20);
		excel_exportResults.setCellValue(upperAPB3, sheetIndex, rowIndex, 21);
		rowIndex++;
	}
	
	int days = r.getOutOfModelChargingPerWeek().get(0).length;
	rowIndex = f_getTrueLastRow(sheetIndexPerWeek, excel_exportResults) + 1;
	for( int t = 0; t < days; t++ ){
		
		double meanOoMC = r.getOutOfModelChargingPerWeek().get(0)[t];
		double lowerOoMC = r.getOutOfModelChargingPerWeek().get(1)[t];
		double upperOoMC = r.getOutOfModelChargingPerWeek().get(2)[t];
		
		double meanLWC = r.getLeftWhileChargingPerWeek().get(0)[t];
		double lowerLWC = r.getLeftWhileChargingPerWeek().get(1)[t];
		double upperLWC = r.getLeftWhileChargingPerWeek().get(2)[t];
		
		double meanLUC = r.getLeftUnchargedPerWeek().get(0)[t];
		double lowerLUC = r.getLeftUnchargedPerWeek().get(1)[t];
		double upperLUC = r.getLeftUnchargedPerWeek().get(2)[t];
		
		double meanCS = r.getPercSatisfiedChargingSessionsPerWeek().get(0)[t];
		double lowerCS = r.getPercSatisfiedChargingSessionsPerWeek().get(1)[t];
		double upperCS = r.getPercSatisfiedChargingSessionsPerWeek().get(2)[t];
		
		double meanCSpD = r.getChargingSessionsPerWeek().get(0)[t];
		double lowerCSpD = r.getChargingSessionsPerWeek().get(1)[t];
		double upperCSpD = r.getChargingSessionsPerWeek().get(2)[t];
		
		double meanRCSpD = r.getRequiredChargingSessionsPerWeek().get(0)[t];
		double lowerRCSpD = r.getRequiredChargingSessionsPerWeek().get(1)[t];
		double upperRCSpD = r.getRequiredChargingSessionsPerWeek().get(2)[t];

		excel_exportResults.setCellValue(scenarioIndex, sheetIndexPerWeek, rowIndex, 1);
		excel_exportResults.setCellValue(t, sheetIndexPerWeek, rowIndex, 2);
	
		excel_exportResults.setCellValue(meanOoMC, sheetIndexPerWeek, rowIndex, 3);
		excel_exportResults.setCellValue(lowerOoMC, sheetIndexPerWeek, rowIndex, 4);
		excel_exportResults.setCellValue(upperOoMC, sheetIndexPerWeek, rowIndex, 5);
		
		excel_exportResults.setCellValue(meanLWC, sheetIndexPerWeek, rowIndex, 6);
		excel_exportResults.setCellValue(lowerLWC, sheetIndexPerWeek, rowIndex, 7);
		excel_exportResults.setCellValue(upperLWC, sheetIndexPerWeek, rowIndex, 8);
		
		excel_exportResults.setCellValue(meanLUC, sheetIndexPerWeek, rowIndex, 9);
		excel_exportResults.setCellValue(lowerLUC, sheetIndexPerWeek, rowIndex, 10);
		excel_exportResults.setCellValue(upperLUC, sheetIndexPerWeek, rowIndex, 11);
		
		excel_exportResults.setCellValue(meanCS, sheetIndexPerWeek, rowIndex, 12);
		excel_exportResults.setCellValue(lowerCS, sheetIndexPerWeek, rowIndex, 13);
		excel_exportResults.setCellValue(upperCS, sheetIndexPerWeek, rowIndex, 14);
		
		excel_exportResults.setCellValue(meanCSpD, sheetIndexPerWeek, rowIndex, 15);
		excel_exportResults.setCellValue(lowerCSpD, sheetIndexPerWeek, rowIndex, 16);
		excel_exportResults.setCellValue(upperCSpD, sheetIndexPerWeek, rowIndex, 17);
		
		excel_exportResults.setCellValue(meanRCSpD, sheetIndexPerWeek, rowIndex, 18);
		excel_exportResults.setCellValue(lowerRCSpD, sheetIndexPerWeek, rowIndex, 19);
		excel_exportResults.setCellValue(upperRCSpD, sheetIndexPerWeek, rowIndex, 20);
		rowIndex++;
	}
}

//Write file
excel_exportResults.writeFile();
/*ALCODEEND*/}

ArrayList<double[]> f_getUncertaintyBounds(ArrayList<double[]> resultsCollection)
{/*ALCODESTART::1755521234175*/
int nTimePoints = resultsCollection.get(0).length;
int nRuns = resultsCollection.size();

//double[] mean = new double[nTimePoints];
//double[] lower = new double[nTimePoints];
//double[] upper = new double[nTimePoints];

ArrayList<double[]> resultsList = new ArrayList<>();
//Mean
resultsList.add(new double[nTimePoints]);
//Lower
resultsList.add(new double[nTimePoints]);
//Upper
resultsList.add(new double[nTimePoints]);

for (int t = 0; t < nTimePoints; t++) {
	double[] valuesAtTimeT = new double[nRuns];
	for (int r = 0; r < nRuns; r++) {
		valuesAtTimeT[r] = resultsCollection.get(r)[t];
	}
	Arrays.sort(valuesAtTimeT);
	
	//mean
	double mean = Arrays.stream(valuesAtTimeT).average().orElse(Double.NaN);
	
	// Percentiles (linear interpolation)
    double lower = f_percentile(valuesAtTimeT, 0.05);
    double upper = f_percentile(valuesAtTimeT, 0.95);
    
    resultsList.get(0)[t] = mean;
    resultsList.get(1)[t] = lower;
    resultsList.get(2)[t] = upper;
}

return resultsList;

/*ALCODEEND*/}

DataSet f_storeMCResults()
{/*ALCODESTART::1755521331481*/
J_MCResult results = new J_MCResult();

results.setScenarioIndex(simulationCount);
results.setB1(v_b1_moveCar);
results.setB2(v_b2_requestMove);
results.setB3(v_b3_notifyNeighbor);
results.setB4(v_recheckCPAvailability);
results.setEVsPerCP(v_EVsPerCP);

ArrayList<double[]> uncertaintyBounds_SR_b1 = f_getUncertaintyBounds(c_succesRate_b1_MC);
ArrayList<double[]> uncertaintyBounds_SR_b2 = f_getUncertaintyBounds(c_succesRate_b2_MC);
ArrayList<double[]> uncertaintyBounds_SR_b3 = f_getUncertaintyBounds(c_succesRate_b3_MC);
ArrayList<double[]> uncertaintyBounds_AP_b1 = f_getUncertaintyBounds(c_avgProb_b1_MC);
ArrayList<double[]> uncertaintyBounds_AP_b2 = f_getUncertaintyBounds(c_avgProb_b2_MC);
ArrayList<double[]> uncertaintyBounds_AP_b3 = f_getUncertaintyBounds(c_avgProb_b3_MC);
ArrayList<double[]> uncertaintyBounds_OoMC = f_getUncertaintyBounds(c_outOfModelChargingPerWeek);
ArrayList<double[]> uncertaintyBounds_LWC = f_getUncertaintyBounds(c_leftWhileChargingPerWeek);
ArrayList<double[]> uncertaintyBounds_LWCWDA = f_getUncertaintyBounds(c_leftWhileChargingWithDelayedAccessPerWeek);
ArrayList<double[]> uncertaintyBounds_LUC = f_getUncertaintyBounds(c_leftUnchargedPerWeek);
ArrayList<double[]> uncertaintyBounds_SCS = f_getUncertaintyBounds(c_percSatisfiedChargingSessionsPerWeek);
ArrayList<double[]> uncertaintyBounds_CS = f_getUncertaintyBounds(c_chargingSessionsPerWeek);
ArrayList<double[]> uncertaintyBounds_RCS = f_getUncertaintyBounds(c_requiredChargingSessionsPerWeek);

ArrayList<double[]> uncertaintyBounds_b1 = f_getUncertaintyBounds(c_b1_perWeek);
ArrayList<double[]> uncertaintyBounds_b2 = f_getUncertaintyBounds(c_b2_perWeek);
ArrayList<double[]> uncertaintyBounds_b3 = f_getUncertaintyBounds(c_b3_perWeek);
ArrayList<double[]> uncertaintyBounds_b1s = f_getUncertaintyBounds(c_b1Successful_perWeek);
ArrayList<double[]> uncertaintyBounds_b2s = f_getUncertaintyBounds(c_b2Successful_perWeek);
ArrayList<double[]> uncertaintyBounds_b3s = f_getUncertaintyBounds(c_b3Successful_perWeek);
ArrayList<double[]> uncertaintyBounds_b1us = f_getUncertaintyBounds(c_b1Unsuccessful_perWeek);
ArrayList<double[]> uncertaintyBounds_b2us = f_getUncertaintyBounds(c_b2Unsuccessful_perWeek);
ArrayList<double[]> uncertaintyBounds_b3us = f_getUncertaintyBounds(c_b3Unsuccessful_perWeek);
ArrayList<double[]> uncertaintyBounds_kmd = f_getUncertaintyBounds(c_kmDrivenPerWeek);

ArrayList<double[]> uncertaintyBounds_trips = f_getUncertaintyBounds(c_tripsPerWeek);

ArrayList<double[]> uncertaintyBounds_pcp = f_getUncertaintyBounds(c_PCP_MC);
ArrayList<double[]> uncertaintyBounds_rc = f_getUncertaintyBounds(c_RC_MC);
ArrayList<double[]> uncertaintyBounds_psi = f_getUncertaintyBounds(c_PSI_MC);
ArrayList<double[]> uncertaintyBounds_norm1 = f_getUncertaintyBounds(c_norm1_MC);
ArrayList<double[]> uncertaintyBounds_norm2 = f_getUncertaintyBounds(c_norm2_MC);
ArrayList<double[]> uncertaintyBounds_norm3 = f_getUncertaintyBounds(c_norm3_MC);




//Charging satisfaction



results.setSuccessRate_b1(uncertaintyBounds_SR_b1);
results.setSuccessRate_b2(uncertaintyBounds_SR_b2);
results.setSuccessRate_b3(uncertaintyBounds_SR_b3);

results.setAvgProb_b1(uncertaintyBounds_AP_b1);
results.setAvgProb_b2(uncertaintyBounds_AP_b2);
results.setAvgProb_b3(uncertaintyBounds_AP_b3);

results.setOutOfModelChargingPerWeek(uncertaintyBounds_OoMC);
results.setLeftWhileChargingPerWeek(uncertaintyBounds_LWC);
results.setLeftWhileChargingWithDelayedAccessPerWeek(uncertaintyBounds_LWCWDA);
results.setLeftUnchargedPerWeek(uncertaintyBounds_LUC);
results.setPercSatisfiedChargingSessionsPerWeek(uncertaintyBounds_SCS);

results.setChargingSessionsPerWeek(uncertaintyBounds_CS);
results.setRequiredChargingSessionsPerWeek(uncertaintyBounds_RCS);

results.setBehaviour1PerWeek(uncertaintyBounds_b1);
results.setBehaviour2PerWeek(uncertaintyBounds_b2);
results.setBehaviour3PerWeek(uncertaintyBounds_b3);
results.setSuccessfulBehaviour1PerWeek(uncertaintyBounds_b1s);
results.setSuccessfulBehaviour2PerWeek(uncertaintyBounds_b2s);
results.setSuccessfulBehaviour3PerWeek(uncertaintyBounds_b3s);

results.setUnsuccessfulBehaviour1PerWeek(uncertaintyBounds_b1us);
results.setUnsuccessfulBehaviour2PerWeek(uncertaintyBounds_b2us);
results.setUnsuccessfulBehaviour3PerWeek(uncertaintyBounds_b3us);

results.setKmDrivenPerWeek(uncertaintyBounds_kmd);
results.setTripsPerWeek(uncertaintyBounds_trips);

results.setPCP(uncertaintyBounds_pcp);
results.setRC(uncertaintyBounds_rc);
results.setPSI(uncertaintyBounds_psi);
results.setNorm1(uncertaintyBounds_norm1);
results.setNorm2(uncertaintyBounds_norm2);
results.setNorm3(uncertaintyBounds_norm3);






c_MCResults.add(results);



//f_setMCGraphs(results);
/*ALCODEEND*/}

double f_setRunText(Color color,String title)
{/*ALCODESTART::1755529642761*/

double xOval = 400;
double yOvalStart = 500;
double yOval = yOvalStart + simulationCount * 25;
double rotationOval = 0;
Color lineColor = color;
Color fillColor = color;
double radiusX = 8;
double radiusY = 8;
double lineWidth = 0;
LineStyle lineStyle = LINE_STYLE_SOLID;    
    
ShapeOval oval = new ShapeOval(true, xOval, yOval, rotationOval, lineColor, fillColor, radiusX, radiusY, lineWidth, lineStyle);
presentation.add(oval);

ShapeDrawMode drawMode = presentation.getDrawMode();
double xText = xOval + 20;
double yText = yOvalStart - 10 + simulationCount * 25;
double z = 0;
double rotationText = 0;
String text = title;
Font font = new Font("SansSerif", Font.PLAIN, 11);
TextAlignment alignment = TextAlignment.ALIGNMENT_LEFT;

ShapeText textShape = new ShapeText(drawMode, true, xText, yText, z, rotationText, black, text, font, alignment);
presentation.add(textShape);
/*ALCODEEND*/}

double f_endRun()
{/*ALCODESTART::1755530416903*/
//f_writeResultsToExcel(startup_agent.excel_exportResults);
f_setGraphs();

if(monteCarlo){
	iteration++;
	//traceln("Finished MC iteration " + iteration);
}
else{
	simulationCount++;
}
if(v_rapidRun == false){
	viewArea.navigateTo();
}
/*ALCODEEND*/}

double f_clearResultsCollections()
{/*ALCODESTART::1755531687092*/
c_succesRate_b1_MC.clear();
c_succesRate_b2_MC.clear();
c_succesRate_b3_MC.clear();
c_avgProb_b1_MC.clear();
c_avgProb_b2_MC.clear();
c_avgProb_b3_MC.clear();
c_outOfModelChargingPerWeek.clear();
c_leftWhileChargingPerWeek.clear();
c_leftWhileChargingWithDelayedAccessPerWeek.clear();
c_percSatisfiedChargingSessionsPerWeek.clear();
c_leftUnchargedPerWeek.clear();
c_chargingSessionsPerWeek.clear();
c_requiredChargingSessionsPerWeek.clear();

c_b1_perWeek.clear();
c_b2_perWeek.clear();
c_b3_perWeek.clear();

c_b1Successful_perWeek.clear();
c_b2Successful_perWeek.clear();
c_b3Successful_perWeek.clear();
c_b1Unsuccessful_perWeek.clear();
c_b2Unsuccessful_perWeek.clear();
c_b3Unsuccessful_perWeek.clear();

c_kmDrivenPerWeek.clear();

for(J_MCResult rs : c_MCResults){
	rs.clear();
}
c_MCResults.clear();
/*ALCODEEND*/}

double f_excelClearSheets()
{/*ALCODESTART::1755532153792*/
excel_exportResults.readFile();

// Use actual sheet count
int nSheets = excel_exportResults.getNumberOfSheets();

for (int s = 1; s <= nSheets; s++) {
    int lastRow = excel_exportResults.getLastRowNum(s);
    if (lastRow <= 1) continue; // skip empty or header-only sheets

    for (int r = 2; r <= lastRow; r++) {
        int lastCol = excel_exportResults.getLastCellNum(s, r);
        if (lastCol == 0) continue;
		
        for (int c = 1; c <= lastCol; c++) {
            // Overwrite with null (true clear)
            excel_exportResults.setCellValue((Double)null, s, r, c);
        }
    }
}

excel_exportResults.writeFile();
excel_exportResults.close();

traceln("Results excel cleared");

/*ALCODEEND*/}

int f_getTrueLastRow(int sheetIndex,ExcelFile excel_file)
{/*ALCODESTART::1755534050897*/

int lastRow = excel_file.getLastRowNum(sheetIndex);
for (int r = lastRow; r >= 3; r--) {
	int lastCol = excel_file.getLastCellNum(sheetIndex, r);
	for (int c = 1; c <= lastCol; c++) {
	
	//traceln("row " + r + " col " + c + " sheet " + sheetIndex);
	
	if(excel_file.getCellType(sheetIndex, r, c) == CellType.NUMERIC){
	
		Double val = excel_file.getCellNumericValue(sheetIndex, r, c);
			if (val != null) {
				return r; // found last non-empty row
			}
		}
	else if(excel_file.getCellType(sheetIndex, r, c) == CellType.STRING){
	
		String vals = excel_file.getCellStringValue(sheetIndex, r, c);
			if (vals != null) {
				return r; // found last non-empty row
			}
		}
	}
}
return 1; // assume header only
/*ALCODEEND*/}

double f_getRollingAverage(int days)
{/*ALCODESTART::1755769439993*/
int window = 7;

// Temporary sums to build rolling averages
double sum_out = 0;
double sum_leftUncharged = 0;
double sum_leftWhileCharging = 0;

int size = (int) Math.round(days/window);
DataSet data_outOfModelCharging_rolling = new DataSet(size);
DataSet data_leftUncharged_rolling = new DataSet(size);
DataSet data_leftWhileCharging_rolling = new DataSet(size);

for (int i = 0; i < days; i++) {    
    double out = results.getOutOfModelChargingPerDay().get(0)[i];
    double uncharged = results.getLeftUnchargedPerDay().get(0)[i];
    double whileCharging = results.getLeftWhileChargingPerDay().get(0)[i];

    // --- Rolling average part ---
    sum_out += out;
    sum_leftUncharged += uncharged;
    sum_leftWhileCharging += whileCharging;

    // Once we have enough days in the window, subtract the trailing value
    if (i >= window) {
        sum_out -= results.getOutOfModelChargingPerDay().get(0)[i - window];
        sum_leftUncharged -= results.getLeftUnchargedPerDay().get(0)[i - window];
        sum_leftWhileCharging -= results.getLeftWhileChargingPerDay().get(0)[i - window];
    }

    // Only start adding rolling averages after first `window` days
    if (i >= window - 1) {
        double avg_out = sum_out / window;
        double avg_uncharged = sum_leftUncharged / window;
        double avg_whileCharging = sum_leftWhileCharging / window;

        data_outOfModelCharging_rolling.add(i, avg_out);
        data_leftUncharged_rolling.add(i, avg_uncharged);
        data_leftWhileCharging_rolling.add(i, avg_whileCharging);
    }
}

/*ALCODEEND*/}

double f_getMeanAndSD()
{/*ALCODESTART::1756909501612*/
String sheetName = "mean_and_sd";

int columnIndex = 2;
mean_norms = ef_spvars.getCellNumericValue(sheetName, 2, columnIndex);
mean_pcp = ef_spvars.getCellNumericValue(sheetName, 3, columnIndex);
mean_rc = ef_spvars.getCellNumericValue(sheetName, 4, columnIndex);

mean_psi = ef_spvars.getCellNumericValue(sheetName, 5, columnIndex);

mean_b1 = ef_spvars.getCellNumericValue(sheetName, 6, columnIndex);
mean_b2 = ef_spvars.getCellNumericValue(sheetName, 7, columnIndex);
mean_b3 = ef_spvars.getCellNumericValue(sheetName, 8, columnIndex);

columnIndex = 3;
sd_norms = ef_spvars.getCellNumericValue(sheetName, 2, columnIndex);
sd_pcp = ef_spvars.getCellNumericValue(sheetName, 3, columnIndex);
sd_rc = ef_spvars.getCellNumericValue(sheetName, 4, columnIndex);

sd_psi = ef_spvars.getCellNumericValue(sheetName, 5, columnIndex);

sd_b1 = ef_spvars.getCellNumericValue(sheetName, 6, columnIndex);
sd_b2 = ef_spvars.getCellNumericValue(sheetName, 7, columnIndex);
sd_b3 = ef_spvars.getCellNumericValue(sheetName, 8, columnIndex);

/*ALCODEEND*/}

double f_getRegressionCoefficients()
{/*ALCODESTART::1756910926316*/
//--B1--
ef_spvars.readFile();

if( regCoefFromData ){
	String var1 = "norms";
	String var2 = "perceived_social_interdependence";
	String var3 = "b1";
	double value = f_mediationResultsQuery(var1, var2, var3);
	regCoef_norms_psi_b1 = value;
	
	var1 = "perceived_charging_pressure";
	value = f_mediationResultsQuery(var1, var2, var3);
	regCoef_pcp_psi_b1 = value;
	
	var1 = "reputational_concern";
	value = f_mediationResultsQuery(var1, var2, var3);
	regCoef_rc_psi_b1 = value;
	
	var1 = "perceived_social_interdependence";
	var2 = "b1_move_vehicle";
	value = f_mediationResultsQuery(var1, var2, var3);
	regCoef_psi_b1 = value;
	
	var1 = "reputational_concern";
	var2 = "b1_move_vehicle";
	value = f_mediationResultsQuery(var1, var2, var3);
	regCoef_rc_b1 = value;
	
	//--B2B3--
	var1 = "norms";
	var2 = "perceived_social_interdependence";
	var3 = "b2/b3";
	value = f_mediationResultsQuery(var1, var2, var3);
	regCoef_norms_psi_b2b3 = value;
	
	var1 = "perceived_charging_pressure";
	value = f_mediationResultsQuery(var1, var2, var3);
	regCoef_pcp_psi_b2b3 = value;
	
	var1 = "reputational_concern";
	value = f_mediationResultsQuery(var1, var2, var3);
	regCoef_rc_psi_b2b3 = value;
	
	//--B2--
	var1 = "perceived_social_interdependence";
	var2 = "b2_request_move";
	var3 = "b2";
	value = f_mediationResultsQuery(var1, var2, var3);
	regCoef_psi_b2 = value;
	
	var1 = "perceived_charging_pressure";
	var2 = "b2_request_move";
	value = f_mediationResultsQuery(var1, var2, var3);
	regCoef_pcp_b2 = value;
	
	//--B3--
	var1 = "perceived_social_interdependence";
	var2 = "b3_notify_neighbor";
	var3 = "b3";
	value = f_mediationResultsQuery(var1, var2, var3);
	regCoef_psi_b3 = value;
	
	var1 = "perceived_charging_pressure";
	var2 = "b3_notify_neighbor";
	value = f_mediationResultsQuery(var1, var2, var3);
	regCoef_pcp_b3 = value;
}
else {
	regCoef_norms_psi_b1 = 0.3;
	regCoef_norms_psi_b2b3 = 0.3;
	regCoef_pcp_psi_b1 = 0.3;
	regCoef_pcp_psi_b2b3 = 0.3;
	regCoef_psi_b1 = 0.3;
	regCoef_psi_b2 = 0.3;
	regCoef_psi_b3 = 0.3;
	regCoef_rc_b1 = 0.3;
	regCoef_rc_psi_b1 = 0.3;
	regCoef_rc_psi_b2b3 = 0.3;
	regCoef_pcp_b2 = 0.3;
	regCoef_pcp_b3 = 0.3;
}
/*ALCODEEND*/}

double f_mediationResultsQuery(String var1,String var2,String var3)
{/*ALCODESTART::1756911459525*/
double value = (double) selectFirstValue(double.class,
	"SELECT value FROM mediation_results WHERE " + 
		"var_1 = ? AND " +
		"var_2 = ? AND " + 
		"behavior = ? LIMIT 1;",
    	var1,
    	var2,
    	var3
);

//traceln("var1 = " + var1 + " var2 = " + var2 + " value = " + value);
return value;

/*ALCODEEND*/}

double f_getCorrelationMatrixFromExcel()
{/*ALCODESTART::1756911500687*/

String sheetName = "correlation_matrix";
int matrixSize = ef_spvars.getLastRowNum(sheetName) - 1; //-1 as i do not need both b2 and b3 as their correlation = 1, they are the same

correlationMatrix = new double[matrixSize-1][matrixSize-1];

for(int rowIndex = 2; rowIndex < matrixSize + 1; rowIndex++){
	for(int columnIndex = 2; columnIndex < matrixSize + 1; columnIndex++){
		double value = ef_spvars.getCellNumericValue(sheetName, rowIndex, columnIndex);
		correlationMatrix[rowIndex-2][columnIndex-2] = value;
	}
}
	
/*ALCODEEND*/}

List<List<Double>> f_getSortedSocialPsychologicalData()
{/*ALCODESTART::1756911534006*/
String sheetName = "frequency_list";
int size = ef_spvars.getLastRowNum(sheetName);
/*
List<Object> headers = ef_spvars.getRow(sheetName, 0);

Map<String, Integer> columnIndexMap = new HashMap<>();

for (Cell cell : headerRow) {
    String header = cell.getStringCellValue().trim().toLowerCase();
    columnIndexMap.put(header, cell.getColumnIndex());
}

// Now extract your needed indexes
int col_norms = columnIndexMap.getOrDefault("norms", -1);
int col_trust = columnIndexMap.getOrDefault("trust", -1);
int col_rc    = columnIndexMap.getOrDefault("rc", -1);
int col_psi   = columnIndexMap.getOrDefault("psi", -1);
int col_b1    = columnIndexMap.getOrDefault("b1", -1);
int col_b2    = columnIndexMap.getOrDefault("b2", -1);
int col_b3    = columnIndexMap.getOrDefault("b3", -1);
*/

int col_norms = 1;
//int col_trust = 2;
int col_rc = 2;
int col_psi = 3;
int col_pcp = 4;
int col_b1 = 5;
int col_b2 = 6;
int col_b3 = 7;

ArrayList<Double> c_norms = new ArrayList<>();
//ArrayList<Double> c_trust = new ArrayList<>();
ArrayList<Double> c_rc = new ArrayList<>();
ArrayList<Double> c_psi = new ArrayList<>();
ArrayList<Double> c_pcp = new ArrayList<>();
ArrayList<Double> c_b1 = new ArrayList<>();
ArrayList<Double> c_b2 = new ArrayList<>();
ArrayList<Double> c_b3 = new ArrayList<>();

for(int rowIndex = 2; rowIndex < size + 1; rowIndex++){
	c_norms.add(ef_spvars.getCellNumericValue(sheetName, rowIndex, col_norms));
	//c_trust.add(ef_spvars.getCellNumericValue(sheetName, rowIndex, col_trust));
	c_rc.add(ef_spvars.getCellNumericValue(sheetName, rowIndex, col_rc));
	c_psi.add(ef_spvars.getCellNumericValue(sheetName, rowIndex, col_psi));
	c_pcp.add(ef_spvars.getCellNumericValue(sheetName, rowIndex, col_pcp));
	c_b1.add(ef_spvars.getCellNumericValue(sheetName, rowIndex, col_b1));
	c_b2.add(ef_spvars.getCellNumericValue(sheetName, rowIndex, col_b2));
	c_b3.add(ef_spvars.getCellNumericValue(sheetName, rowIndex, col_b3));
}


Collections.sort(c_norms);
//Collections.sort(c_trust);
Collections.sort(c_rc);
Collections.sort(c_psi);
Collections.sort(c_pcp);
Collections.sort(c_b1);
Collections.sort(c_b2);
Collections.sort(c_b3);

//List<List<Double>> sortedRealData = new ArrayList<>();
//return sortedRealData = Arrays.asList(c_norms, c_trust, c_rc, c_psi, c_pcp, c_b1, c_b2, c_b3);
sortedRealData = new ArrayList<>();
sortedRealData = Arrays.asList(c_norms, c_rc, c_psi, c_pcp, c_b1, c_b2, c_b3);
/*ALCODEEND*/}

DataSet f_writeSensitivityResultsToExcel()
{/*ALCODESTART::1760004140225*/
//Get excel file and indexes
//excel_exportResultsSensitivity.readFile();


int sheetIndex = 1;
int sheetIndexPerDay = 2;

int scenarioIndex = v_EVsPerCP;
int rowIndex = f_getTrueLastRow(sheetIndex, excel_exportResultsSensitivity) + 1;
int nTimePoints = c_succesRate_b1_MC.get(0).length;

for(int run = 0; run < iteration; run++){
	for(int i = 0; i< nTimePoints; i++){
		int day = i / 96;
		
		
		/*			
		excel_exportResultsSensitivity.setCellValue(scenarioIndex, sheetIndex, rowIndex, 1);
		excel_exportResultsSensitivity.setCellValue(i, sheetIndex, rowIndex, 2);
		excel_exportResultsSensitivity.setCellValue(day, sheetIndex, rowIndex, 3);
			
		excel_exportResultsSensitivity.setCellValue(c_succesRate_b1_MC.get(run)[i], sheetIndex, rowIndex, 4);
		excel_exportResultsSensitivity.setCellValue(c_succesRate_b2_MC.get(run)[i], sheetIndex, rowIndex, 5);
		excel_exportResultsSensitivity.setCellValue(c_succesRate_b3_MC.get(run)[i], sheetIndex, rowIndex, 6);
		/*
		excel_exportResultsSensitivity.setCellValue(meanSRB2, sheetIndex, rowIndex, 7);
		excel_exportResultsSensitivity.setCellValue(lowerSRB2, sheetIndex, rowIndex, 8);
		excel_exportResultsSensitivity.setCellValue(upperSRB2, sheetIndex, rowIndex, 9);
			
		excel_exportResults.setCellValue(meanSRB3, sheetIndex, rowIndex, 10);
		excel_exportResults.setCellValue(lowerSRB3, sheetIndex, rowIndex, 11);
		excel_exportResults.setCellValue(upperSRB3, sheetIndex, rowIndex, 12);
			
		excel_exportResults.setCellValue(meanAPB1, sheetIndex, rowIndex, 13);
		excel_exportResults.setCellValue(lowerAPB1, sheetIndex, rowIndex, 14);
		excel_exportResults.setCellValue(upperAPB1, sheetIndex, rowIndex, 15);
			
		excel_exportResults.setCellValue(meanAPB2, sheetIndex, rowIndex, 16);
		excel_exportResults.setCellValue(lowerAPB2, sheetIndex, rowIndex, 17);
		excel_exportResults.setCellValue(upperAPB2, sheetIndex, rowIndex, 18);
			
		excel_exportResults.setCellValue(meanAPB3, sheetIndex, rowIndex, 19);
		excel_exportResults.setCellValue(lowerAPB3, sheetIndex, rowIndex, 20);
		excel_exportResults.setCellValue(upperAPB3, sheetIndex, rowIndex, 21);
		*/
		//rowIndex++;
	}
	
	int days = c_outOfModelChargingPerWeek.get(0).length;
	//rowIndex = 2;
	rowIndex = f_getTrueLastRow(sheetIndexPerDay, excel_exportResultsSensitivity) + 1;
	for( int i = 0; i < days; i++ ){
			
		excel_exportResultsSensitivity.setCellValue(scenarioIndex, sheetIndexPerDay, rowIndex, 1);
		excel_exportResultsSensitivity.setCellValue(run, sheetIndexPerDay, rowIndex, 2);
		excel_exportResultsSensitivity.setCellValue(i, sheetIndexPerDay, rowIndex, 3);
			
		excel_exportResultsSensitivity.setCellValue(c_outOfModelChargingPerWeek.get(run)[i], sheetIndexPerDay, rowIndex, 4);
		excel_exportResultsSensitivity.setCellValue(c_leftWhileChargingPerWeek.get(run)[i], sheetIndexPerDay, rowIndex, 5);
		excel_exportResultsSensitivity.setCellValue(c_leftWhileChargingWithDelayedAccessPerWeek.get(run)[i], sheetIndexPerDay, rowIndex, 6);
		excel_exportResultsSensitivity.setCellValue(c_leftUnchargedPerWeek.get(run)[i], sheetIndexPerDay, rowIndex, 7);
		excel_exportResultsSensitivity.setCellValue(c_percSatisfiedChargingSessionsPerWeek.get(run)[i], sheetIndexPerDay, rowIndex, 8);
		excel_exportResultsSensitivity.setCellValue(c_chargingSessionsPerWeek.get(run)[i], sheetIndexPerDay, rowIndex, 9);	
		excel_exportResultsSensitivity.setCellValue(c_requiredChargingSessionsPerWeek.get(run)[i], sheetIndexPerDay, rowIndex, 10);
		
		rowIndex++;
		
	}
}

//Write file
excel_exportResultsSensitivity.writeFile();



/*ALCODEEND*/}

DataSet f_writeBehaviorScenariosToExcel()
{/*ALCODESTART::1760014548511*/
//Get excel file and indexes
//excel_exportResults.readFile();


int sheetIndex = 1;
int sheetIndexPerWeek = 2;

excel_exportResultsBehaviours.readFile();

for(J_MCResult r : c_MCResults){

	int scenarioIndex = r.getScenarioIndex();
	int rowIndex = f_getTrueLastRow(sheetIndex, excel_exportResultsBehaviours) + 1;
	int nTimePoints = r.getSuccessRate_b1().get(0).length;
	
	
	for( int t = 0; t < nTimePoints; t++ ){
		
		double meanSRB1 = r.getSuccessRate_b1().get(0)[t];
		double lowerSRB1 = r.getSuccessRate_b1().get(1)[t];
	}
	
	int weeks = r.getOutOfModelChargingPerWeek().get(0).length;
	rowIndex = f_getTrueLastRow(sheetIndexPerWeek, excel_exportResultsBehaviours) + 1;
	for( int t = 0; t < weeks; t++ ){
		
		double meanOoMC = r.getOutOfModelChargingPerWeek().get(0)[t];
		double lowerOoMC = r.getOutOfModelChargingPerWeek().get(1)[t];
		double upperOoMC = r.getOutOfModelChargingPerWeek().get(2)[t];
		
		double meanLWC = r.getLeftWhileChargingPerWeek().get(0)[t];
		double lowerLWC = r.getLeftWhileChargingPerWeek().get(1)[t];
		double upperLWC = r.getLeftWhileChargingPerWeek().get(2)[t];
		
		double meanLUC = r.getLeftUnchargedPerWeek().get(0)[t];
		double lowerLUC = r.getLeftUnchargedPerWeek().get(1)[t];
		double upperLUC = r.getLeftUnchargedPerWeek().get(2)[t];
		
		double meanCS = r.getPercSatisfiedChargingSessionsPerWeek().get(0)[t];
		double lowerCS = r.getPercSatisfiedChargingSessionsPerWeek().get(1)[t];
		double upperCS = r.getPercSatisfiedChargingSessionsPerWeek().get(2)[t];
		
		double meanCSpD = r.getChargingSessionsPerWeek().get(0)[t];
		double lowerCSpD = r.getChargingSessionsPerWeek().get(1)[t];
		double upperCSpD = r.getChargingSessionsPerWeek().get(2)[t];
		
		double meanRCSpD = r.getRequiredChargingSessionsPerWeek().get(0)[t];
		double lowerRCSpD = r.getRequiredChargingSessionsPerWeek().get(1)[t];
		double upperRCSpD = r.getRequiredChargingSessionsPerWeek().get(2)[t];
		
		double meanB1 = r.getBehaviour1PerWeek().get(0)[t];
		double lowerB1 = r.getBehaviour1PerWeek().get(1)[t];
		double upperB1 = r.getBehaviour1PerWeek().get(2)[t];
		
		double meanB2 = r.getBehaviour2PerWeek().get(0)[t];
		double lowerB2 = r.getBehaviour2PerWeek().get(1)[t];
		double upperB2 = r.getBehaviour2PerWeek().get(2)[t];
		
		double meanB3 = r.getBehaviour3PerWeek().get(0)[t];
		double lowerB3 = r.getBehaviour3PerWeek().get(1)[t];
		double upperB3 = r.getBehaviour3PerWeek().get(2)[t];
		
		double meansB1 = r.getSuccessfulBehaviour1PerWeek().get(0)[t];
		double lowersB1 = r.getSuccessfulBehaviour1PerWeek().get(1)[t];
		double uppersB1 = r.getSuccessfulBehaviour1PerWeek().get(2)[t];
		
		double meansB2 = r.getSuccessfulBehaviour2PerWeek().get(0)[t];
		double lowersB2 = r.getSuccessfulBehaviour2PerWeek().get(1)[t];
		double uppersB2 = r.getSuccessfulBehaviour2PerWeek().get(2)[t];
		
		double meansB3 = r.getSuccessfulBehaviour3PerWeek().get(0)[t];
		double lowersB3 = r.getSuccessfulBehaviour3PerWeek().get(1)[t];
		double uppersB3 = r.getSuccessfulBehaviour3PerWeek().get(2)[t];
		
		double meanusB1 = r.getUnsuccessfulBehaviour1PerWeek().get(0)[t];
		double lowerusB1 = r.getUnsuccessfulBehaviour1PerWeek().get(1)[t];
		double upperusB1 = r.getUnsuccessfulBehaviour1PerWeek().get(2)[t];
		
		double meanusB2 = r.getUnsuccessfulBehaviour2PerWeek().get(0)[t];
		double lowerusB2 = r.getUnsuccessfulBehaviour2PerWeek().get(1)[t];
		double upperusB2 = r.getUnsuccessfulBehaviour2PerWeek().get(2)[t];
		
		double meanusB3 = r.getUnsuccessfulBehaviour3PerWeek().get(0)[t];
		double lowerusB3 = r.getUnsuccessfulBehaviour3PerWeek().get(1)[t];
		double upperusB3 = r.getUnsuccessfulBehaviour3PerWeek().get(2)[t];
		
		double meankmd = r.getKmDrivenPerWeek().get(0)[t];
		double lowerkmd = r.getKmDrivenPerWeek().get(1)[t];
		double upperkmd = r.getKmDrivenPerWeek().get(2)[t];
		
		double meantrips = r.getTripsPerWeek().get(0)[t];
		double lowertrips = r.getTripsPerWeek().get(1)[t];
		double uppertrips = r.getTripsPerWeek().get(2)[t];
		
		double meanavgProbB1 = r.getAvgProb_b1().get(0)[t];
		double loweravgProbB1 = r.getAvgProb_b1().get(1)[t];
		double upperavgProbB1 = r.getAvgProb_b1().get(2)[t];
		
		double meanavgProbB2 = r.getAvgProb_b2().get(0)[t];
		double loweravgProbB2 = r.getAvgProb_b2().get(1)[t];
		double upperavgProbB2 = r.getAvgProb_b1().get(2)[t];
		
		double meanavgProbB3 = r.getAvgProb_b2().get(0)[t];
		double loweravgProbB3 = r.getAvgProb_b2().get(1)[t];
		double upperavgProbB3 = r.getAvgProb_b2().get(2)[t];
		
		double meanPCP = r.getPCP().get(0)[t];
		double lowerPCP = r.getPCP().get(1)[t];
		double upperPCP = r.getPCP().get(2)[t];
		
		double meanPSI = r.getPSI().get(0)[t];
		double lowerPSI = r.getPSI().get(1)[t];
		double upperPSI = r.getPSI().get(2)[t];
		
		double meanRC = r.getRC().get(0)[t];
		double lowerRC = r.getRC().get(1)[t];
		double upperRC = r.getRC().get(2)[t];
		
		double meanNorm1 = r.getNorm1().get(0)[t];
		double lowerNorm1 = r.getNorm1().get(1)[t];
		double upperNorm1 = r.getNorm1().get(2)[t];
		
		double meanNorm2 = r.getNorm2().get(0)[t];
		double lowerNorm2 = r.getNorm2().get(1)[t];
		double upperNorm2 = r.getNorm2().get(2)[t];
		
		double meanNorm3 = r.getNorm3().get(0)[t];
		double lowerNorm3 = r.getNorm3().get(1)[t];
		double upperNorm3 = r.getNorm3().get(2)[t];
				
		excel_exportResultsBehaviours.setCellValue(scenarioIndex, sheetIndexPerWeek, rowIndex, 1);
		excel_exportResultsBehaviours.setCellValue(t, sheetIndexPerWeek, rowIndex, 2);
	
		excel_exportResultsBehaviours.setCellValue(meanOoMC, sheetIndexPerWeek, rowIndex, 3);
		excel_exportResultsBehaviours.setCellValue(lowerOoMC, sheetIndexPerWeek, rowIndex, 4);
		excel_exportResultsBehaviours.setCellValue(upperOoMC, sheetIndexPerWeek, rowIndex, 5);
		
		excel_exportResultsBehaviours.setCellValue(meanLWC, sheetIndexPerWeek, rowIndex, 6);
		excel_exportResultsBehaviours.setCellValue(lowerLWC, sheetIndexPerWeek, rowIndex, 7);
		excel_exportResultsBehaviours.setCellValue(upperLWC, sheetIndexPerWeek, rowIndex, 8);
		
		excel_exportResultsBehaviours.setCellValue(meanLUC, sheetIndexPerWeek, rowIndex, 9);
		excel_exportResultsBehaviours.setCellValue(lowerLUC, sheetIndexPerWeek, rowIndex, 10);
		excel_exportResultsBehaviours.setCellValue(upperLUC, sheetIndexPerWeek, rowIndex, 11);
		
		excel_exportResultsBehaviours.setCellValue(meanCS, sheetIndexPerWeek, rowIndex, 12);
		excel_exportResultsBehaviours.setCellValue(lowerCS, sheetIndexPerWeek, rowIndex, 13);
		excel_exportResultsBehaviours.setCellValue(upperCS, sheetIndexPerWeek, rowIndex, 14);
		
		excel_exportResultsBehaviours.setCellValue(meanCSpD, sheetIndexPerWeek, rowIndex, 15);
		excel_exportResultsBehaviours.setCellValue(lowerCSpD, sheetIndexPerWeek, rowIndex, 16);
		excel_exportResultsBehaviours.setCellValue(upperCSpD, sheetIndexPerWeek, rowIndex, 17);
		
		excel_exportResultsBehaviours.setCellValue(meanRCSpD, sheetIndexPerWeek, rowIndex, 18);
		excel_exportResultsBehaviours.setCellValue(lowerRCSpD, sheetIndexPerWeek, rowIndex, 19);
		excel_exportResultsBehaviours.setCellValue(upperRCSpD, sheetIndexPerWeek, rowIndex, 20);
		
		excel_exportResultsBehaviours.setCellValue(meanB1, sheetIndexPerWeek, rowIndex, 21);
		excel_exportResultsBehaviours.setCellValue(lowerB1, sheetIndexPerWeek, rowIndex, 22);
		excel_exportResultsBehaviours.setCellValue(upperB1, sheetIndexPerWeek, rowIndex, 23);
		
		excel_exportResultsBehaviours.setCellValue(meanB2, sheetIndexPerWeek, rowIndex, 24);
		excel_exportResultsBehaviours.setCellValue(lowerB2, sheetIndexPerWeek, rowIndex, 25);
		excel_exportResultsBehaviours.setCellValue(upperB2, sheetIndexPerWeek, rowIndex, 26);
		
		excel_exportResultsBehaviours.setCellValue(meanB3, sheetIndexPerWeek, rowIndex, 27);
		excel_exportResultsBehaviours.setCellValue(lowerB3, sheetIndexPerWeek, rowIndex, 28);
		excel_exportResultsBehaviours.setCellValue(upperB3, sheetIndexPerWeek, rowIndex, 29);
		
		excel_exportResultsBehaviours.setCellValue(meansB1, sheetIndexPerWeek, rowIndex, 30);
		excel_exportResultsBehaviours.setCellValue(lowersB1, sheetIndexPerWeek, rowIndex, 31);
		excel_exportResultsBehaviours.setCellValue(uppersB1, sheetIndexPerWeek, rowIndex, 32);
		
		excel_exportResultsBehaviours.setCellValue(meansB2, sheetIndexPerWeek, rowIndex, 33);
		excel_exportResultsBehaviours.setCellValue(lowersB2, sheetIndexPerWeek, rowIndex, 34);
		excel_exportResultsBehaviours.setCellValue(uppersB2, sheetIndexPerWeek, rowIndex, 35);
		
		excel_exportResultsBehaviours.setCellValue(meansB3, sheetIndexPerWeek, rowIndex, 36);
		excel_exportResultsBehaviours.setCellValue(lowersB3, sheetIndexPerWeek, rowIndex, 37);
		excel_exportResultsBehaviours.setCellValue(uppersB3, sheetIndexPerWeek, rowIndex, 38);
		
		excel_exportResultsBehaviours.setCellValue(meanusB1, sheetIndexPerWeek, rowIndex, 39);
		excel_exportResultsBehaviours.setCellValue(lowerusB1, sheetIndexPerWeek, rowIndex, 40);
		excel_exportResultsBehaviours.setCellValue(upperusB1, sheetIndexPerWeek, rowIndex, 41);
		
		excel_exportResultsBehaviours.setCellValue(meanusB2, sheetIndexPerWeek, rowIndex, 42);
		excel_exportResultsBehaviours.setCellValue(lowerusB2, sheetIndexPerWeek, rowIndex, 43);
		excel_exportResultsBehaviours.setCellValue(upperusB2, sheetIndexPerWeek, rowIndex, 44);
		
		excel_exportResultsBehaviours.setCellValue(meanusB3, sheetIndexPerWeek, rowIndex, 45);
		excel_exportResultsBehaviours.setCellValue(lowerusB3, sheetIndexPerWeek, rowIndex, 46);
		excel_exportResultsBehaviours.setCellValue(upperusB3, sheetIndexPerWeek, rowIndex, 47);
		
		excel_exportResultsBehaviours.setCellValue(meankmd, sheetIndexPerWeek, rowIndex, 48);
		excel_exportResultsBehaviours.setCellValue(lowerkmd, sheetIndexPerWeek, rowIndex, 49);
		excel_exportResultsBehaviours.setCellValue(upperkmd, sheetIndexPerWeek, rowIndex, 50);
		
		excel_exportResultsBehaviours.setCellValue(meantrips, sheetIndexPerWeek, rowIndex, 51);
		excel_exportResultsBehaviours.setCellValue(lowertrips, sheetIndexPerWeek, rowIndex, 52);
		excel_exportResultsBehaviours.setCellValue(uppertrips, sheetIndexPerWeek, rowIndex, 53);
		
		excel_exportResultsBehaviours.setCellValue(meanavgProbB1, sheetIndexPerWeek, rowIndex, 54);
		excel_exportResultsBehaviours.setCellValue(loweravgProbB1, sheetIndexPerWeek, rowIndex, 55);
		excel_exportResultsBehaviours.setCellValue(upperavgProbB1, sheetIndexPerWeek, rowIndex, 56);
		
		excel_exportResultsBehaviours.setCellValue(meanavgProbB2, sheetIndexPerWeek, rowIndex, 57);
		excel_exportResultsBehaviours.setCellValue(loweravgProbB2, sheetIndexPerWeek, rowIndex, 58);
		excel_exportResultsBehaviours.setCellValue(upperavgProbB2, sheetIndexPerWeek, rowIndex, 59);
		
		excel_exportResultsBehaviours.setCellValue(meanavgProbB3, sheetIndexPerWeek, rowIndex, 60);
		excel_exportResultsBehaviours.setCellValue(loweravgProbB3, sheetIndexPerWeek, rowIndex, 61);
		excel_exportResultsBehaviours.setCellValue(upperavgProbB3, sheetIndexPerWeek, rowIndex, 62);
		
		excel_exportResultsBehaviours.setCellValue(meanPCP, sheetIndexPerWeek, rowIndex, 63);
		excel_exportResultsBehaviours.setCellValue(lowerPCP, sheetIndexPerWeek, rowIndex, 64);
		excel_exportResultsBehaviours.setCellValue(upperPCP, sheetIndexPerWeek, rowIndex, 65);
		
		excel_exportResultsBehaviours.setCellValue(meanPSI, sheetIndexPerWeek, rowIndex, 66);
		excel_exportResultsBehaviours.setCellValue(lowerPSI, sheetIndexPerWeek, rowIndex, 67);
		excel_exportResultsBehaviours.setCellValue(upperPSI, sheetIndexPerWeek, rowIndex, 68);
		
		excel_exportResultsBehaviours.setCellValue(meanRC, sheetIndexPerWeek, rowIndex, 69);
		excel_exportResultsBehaviours.setCellValue(lowerRC, sheetIndexPerWeek, rowIndex, 70);
		excel_exportResultsBehaviours.setCellValue(upperRC, sheetIndexPerWeek, rowIndex, 71);
		
		excel_exportResultsBehaviours.setCellValue(meanNorm1, sheetIndexPerWeek, rowIndex, 72);
		excel_exportResultsBehaviours.setCellValue(lowerNorm1, sheetIndexPerWeek, rowIndex, 73);
		excel_exportResultsBehaviours.setCellValue(upperNorm1, sheetIndexPerWeek, rowIndex, 74);
		
		excel_exportResultsBehaviours.setCellValue(meanNorm2, sheetIndexPerWeek, rowIndex, 75);
		excel_exportResultsBehaviours.setCellValue(lowerNorm2, sheetIndexPerWeek, rowIndex, 76);
		excel_exportResultsBehaviours.setCellValue(upperNorm2, sheetIndexPerWeek, rowIndex, 77);
		
		excel_exportResultsBehaviours.setCellValue(meanNorm3, sheetIndexPerWeek, rowIndex, 78);
		excel_exportResultsBehaviours.setCellValue(lowerNorm3, sheetIndexPerWeek, rowIndex, 79);
		excel_exportResultsBehaviours.setCellValue(upperNorm3, sheetIndexPerWeek, rowIndex, 80);
			
				
		excel_exportResultsBehaviours.setCellValue(r.getB1(), sheetIndexPerWeek, rowIndex, 81);
		excel_exportResultsBehaviours.setCellValue(r.getB2(), sheetIndexPerWeek, rowIndex, 82);
		excel_exportResultsBehaviours.setCellValue(r.getB3(), sheetIndexPerWeek, rowIndex, 83);
		excel_exportResultsBehaviours.setCellValue(r.getB4(), sheetIndexPerWeek, rowIndex, 84);
		excel_exportResultsBehaviours.setCellValue(r.getEVsPerCP(), sheetIndexPerWeek, rowIndex, 85);
		excel_exportResultsBehaviours.setCellValue(roundToInt(100/r.getEVsPerCP()), sheetIndexPerWeek, rowIndex, 86);
		
		rowIndex++;
	}	
}

//Write file
excel_exportResultsBehaviours.writeFile();
excel_exportResultsBehaviours.close();
/*ALCODEEND*/}

double[] f_arDayToWeek(double[] arrayDailyValues)
{/*ALCODESTART::1760695136979*/
int days = arrayDailyValues.length;
int weeks =  days / 7;
double[] arrayWeeklyValues = new double[weeks];

for(int i = 0; i < weeks; i++){
	double sumValue = 0;
	for(int j = 0; j < 7; j++ ){
		int index = j + i * 7;
		sumValue += arrayDailyValues[index];
		
		if( i == 8){
		traceln(arrayDailyValues[index] + " value for day at index " + index);
		}
	}
	arrayWeeklyValues[i] = sumValue;
	traceln(arrayWeeklyValues[i] + " value for week " + i);
}

return arrayWeeklyValues;
/*ALCODEEND*/}

double f_percentile(double[] sorted,double p)
{/*ALCODESTART::1760706139361*/
double rank = p * (sorted.length - 1);
int low = (int) Math.floor(rank);
int high = (int) Math.ceil(rank);
if (high == low) return sorted[low];
return sorted[low] + (rank - low) * (sorted[high] - sorted[low]);
/*ALCODEEND*/}

double[] f_dailyToWeekly(double[] dailyArray)
{/*ALCODESTART::1761056276108*/
int days = dailyArray.length;
int weeks = days / 7;

double[] weeklyArray = new double[weeks];
for(int i = 0; i<weeks; i++){
	int endOfWeekIndex = (i + 1) * 7 - 1;  // last day of the week
	weeklyArray[i] = dailyArray[endOfWeekIndex];
}

return weeklyArray;

/*ALCODEEND*/}

