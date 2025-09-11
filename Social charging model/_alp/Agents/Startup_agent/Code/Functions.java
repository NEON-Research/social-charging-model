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
}
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
}
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
int size = m.v_timestep;

c_succesRate_b1_MC.add(m.ar_successRate_b1);
c_succesRate_b2_MC.add(m.ar_successRate_b2);
c_succesRate_b3_MC.add(m.ar_successRate_b3);
c_avgProb_b1_MC.add(m.ar_avgProbability_b1);
c_avgProb_b2_MC.add(m.ar_avgProbability_b2);
c_avgProb_b3_MC.add(m.ar_avgProbability_b3);

c_outOfModelChargingPerDay.add(m.ar_outOfModelCharging);
c_leftWhileChargingPerDay.add(m.ar_leftWhileCharging);
c_leftWhileChargingWithDelayedAccessPerDay.add(m.ar_leftWhileChargingWithDelayedAccess);
c_leftUnchargedPerDay.add(m.ar_leftUncharged);
c_percSatisfiedChargingSessionsPerDay.add(m.ar_percSatisfiedChargingSessions);

c_chargingSessionsPerDay.add(m.ar_chargingSessions);
c_requiredChargingSessionsPerDay.add(m.ar_requiredChargingSessions);
/*ALCODEEND*/}

double f_setMCGraphs(J_MCResult results)
{/*ALCODESTART::1755183201656*/
//Create datasets
int size = results.getSuccessRate_b1().get(0).length;
int days = results.getLeftUnchargedPerDay().get(0).length;
int weeks = results.getLeftUnchargedPerWeek().get(0).length;
DataSet data_successRate_b1 = new DataSet(size);
DataSet data_successRate_b2 = new DataSet(size);
DataSet data_successRate_b3 = new DataSet(size);
DataSet data_avgProbability_b1 = new DataSet(size);
DataSet data_avgProbability_b2 = new DataSet(size);
DataSet data_avgProbability_b3 = new DataSet(size);
DataSet data_outOfModelCharging = new DataSet(days);
DataSet data_leftWhileCharging = new DataSet(days);
DataSet data_leftUncharged = new DataSet(days);
DataSet data_percSatisfiedChargingSessions = new DataSet(weeks);
DataSet data_chargingSessions = new DataSet(days);
DataSet data_requiredChargingSessions = new DataSet(days);
//DataSet data_successRate_rechecks = new DataSet(size);

for(int i = 0; i < size; i++){
	data_successRate_b1.add(i, results.getSuccessRate_b1().get(0)[i]);
	data_successRate_b2.add(i, results.getSuccessRate_b2().get(0)[i]);
	data_successRate_b3.add(i, results.getSuccessRate_b3().get(0)[i]);
	
	data_avgProbability_b1.add(i, results.getAvgProb_b1().get(0)[i]);
	data_avgProbability_b2.add(i, results.getAvgProb_b2().get(0)[i]);
	data_avgProbability_b3.add(i, results.getAvgProb_b3().get(0)[i]);
}

for(int i = 0; i < days; i++){	
	data_outOfModelCharging.add(i, results.getOutOfModelChargingRollingAvg().get(0)[i]);
	data_leftUncharged.add(i, results.getLeftUnchargedRollingAvg().get(0)[i]);	
	data_leftWhileCharging.add(i, results.getLeftWhileChargingRollingAvg().get(0)[i]);
	//data_percSatisfiedChargingSessions.add(i, results.getPercSatisfiedChargingSessionsRollingAvg().get(0)[i]);
	data_requiredChargingSessions.add(i, results.getRequiredChargingSessionsRollingAvg().get(0)[i]);
	data_chargingSessions.add(i, results.getChargingSessionsRollingAvg().get(0)[i]);
}  

for(int i = 0; i < weeks; i++){	
	data_percSatisfiedChargingSessions.add(i, results.getPercSatisfiedChargingSessionsPerWeek().get(0)[i]);
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

pl_outOfModelCharge.setFixedHorizontalScale(0, days);
pl_chargingSatisfaction.setFixedHorizontalScale(0, weeks);
pl_leftWhileCharging.setFixedHorizontalScale(0, days);
pl_leftUncharged.setFixedHorizontalScale(0, days);
pl_chargingSessions.setFixedHorizontalScale(0, days);
pl_requiredChargingSessions.setFixedHorizontalScale(0, days);

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
excel_exportResults.readFile();


int sheetIndex = 1;
int sheetIndexPerDay = 2;

for(J_MCResult r : c_MCResults){

	int scenarioIndex = r.getScenarioIndex();
	int rowIndex = f_getTrueLastRow(sheetIndex) + 1;
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
				
		excel_exportResults.setCellValue(scenarioIndex, sheetIndex, rowIndex, 1);
		excel_exportResults.setCellValue(t, sheetIndex, rowIndex, 2);
		
		excel_exportResults.setCellValue(meanSRB1, sheetIndex, rowIndex, 3);
		excel_exportResults.setCellValue(lowerSRB1, sheetIndex, rowIndex, 4);
		excel_exportResults.setCellValue(upperSRB1, sheetIndex, rowIndex, 5);
		
		excel_exportResults.setCellValue(meanSRB2, sheetIndex, rowIndex, 6);
		excel_exportResults.setCellValue(lowerSRB2, sheetIndex, rowIndex, 7);
		excel_exportResults.setCellValue(upperSRB2, sheetIndex, rowIndex, 8);
		
		excel_exportResults.setCellValue(meanSRB3, sheetIndex, rowIndex, 9);
		excel_exportResults.setCellValue(lowerSRB3, sheetIndex, rowIndex, 10);
		excel_exportResults.setCellValue(upperSRB3, sheetIndex, rowIndex, 11);
		
		excel_exportResults.setCellValue(meanAPB1, sheetIndex, rowIndex, 12);
		excel_exportResults.setCellValue(lowerAPB1, sheetIndex, rowIndex, 13);
		excel_exportResults.setCellValue(upperAPB1, sheetIndex, rowIndex, 14);
		
		excel_exportResults.setCellValue(meanAPB2, sheetIndex, rowIndex, 15);
		excel_exportResults.setCellValue(lowerAPB2, sheetIndex, rowIndex, 16);
		excel_exportResults.setCellValue(upperAPB2, sheetIndex, rowIndex, 17);
		
		excel_exportResults.setCellValue(meanAPB3, sheetIndex, rowIndex, 18);
		excel_exportResults.setCellValue(lowerAPB3, sheetIndex, rowIndex, 19);
		excel_exportResults.setCellValue(upperAPB3, sheetIndex, rowIndex, 20);
		rowIndex++;
	}
	
	int days = r.getOutOfModelChargingPerDay().get(0).length;
	rowIndex = f_getTrueLastRow(sheetIndexPerDay) + 1;
	for( int t = 0; t < days; t++ ){
		
		double meanOoMC = r.getOutOfModelChargingPerDay().get(0)[t];
		double lowerOoMC = r.getOutOfModelChargingPerDay().get(1)[t];
		double upperOoMC = r.getOutOfModelChargingPerDay().get(2)[t];
		
		double meanLWC = r.getLeftWhileChargingPerDay().get(0)[t];
		double lowerLWC = r.getLeftWhileChargingPerDay().get(1)[t];
		double upperLWC = r.getLeftWhileChargingPerDay().get(2)[t];
		
		double meanLUC = r.getLeftUnchargedPerDay().get(0)[t];
		double lowerLUC = r.getLeftUnchargedPerDay().get(1)[t];
		double upperLUC = r.getLeftUnchargedPerDay().get(2)[t];

		excel_exportResults.setCellValue(scenarioIndex, sheetIndexPerDay, rowIndex, 1);
		excel_exportResults.setCellValue(t, sheetIndexPerDay, rowIndex, 2);
	
		excel_exportResults.setCellValue(meanOoMC, sheetIndexPerDay, rowIndex, 3);
		excel_exportResults.setCellValue(lowerOoMC, sheetIndexPerDay, rowIndex, 4);
		excel_exportResults.setCellValue(upperOoMC, sheetIndexPerDay, rowIndex, 5);
		
		excel_exportResults.setCellValue(meanLWC, sheetIndexPerDay, rowIndex, 6);
		excel_exportResults.setCellValue(lowerLWC, sheetIndexPerDay, rowIndex, 7);
		excel_exportResults.setCellValue(upperLWC, sheetIndexPerDay, rowIndex, 8);
		
		excel_exportResults.setCellValue(meanLUC, sheetIndexPerDay, rowIndex, 9);
		excel_exportResults.setCellValue(lowerLUC, sheetIndexPerDay, rowIndex, 10);
		excel_exportResults.setCellValue(upperLUC, sheetIndexPerDay, rowIndex, 11);
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
	resultsList.get(0)[t] = Arrays.stream(valuesAtTimeT).average().getAsDouble();
	//lower
	resultsList.get(1)[t] = valuesAtTimeT[(int)(0.05 * nRuns)];  // 5th percentile
	//upper
	resultsList.get(2)[t] = valuesAtTimeT[(int)(0.95 * nRuns)];  // 95th percentile
	/*
	mean[t] = Arrays.stream(valuesAtTimeT).average().getAsDouble();
	lower[t] = valuesAtTimeT[(int)(0.05 * nRuns)];  // 5th percentile
	upper[t] = valuesAtTimeT[(int)(0.95 * nRuns)];  // 95th percentile
    */    
}

return resultsList;

/*ALCODEEND*/}

DataSet f_storeMCResults()
{/*ALCODESTART::1755521331481*/
J_MCResult results = new J_MCResult();

results.setScenarioIndex(simulationCount);

ArrayList<double[]> uncertaintyBounds_SR_b1 = f_getUncertaintyBounds(c_succesRate_b1_MC);
ArrayList<double[]> uncertaintyBounds_SR_b2 = f_getUncertaintyBounds(c_succesRate_b2_MC);
ArrayList<double[]> uncertaintyBounds_SR_b3 = f_getUncertaintyBounds(c_succesRate_b3_MC);
ArrayList<double[]> uncertaintyBounds_AP_b1 = f_getUncertaintyBounds(c_avgProb_b1_MC);
ArrayList<double[]> uncertaintyBounds_AP_b2 = f_getUncertaintyBounds(c_avgProb_b2_MC);
ArrayList<double[]> uncertaintyBounds_AP_b3 = f_getUncertaintyBounds(c_avgProb_b3_MC);
ArrayList<double[]> uncertaintyBounds_OoMC = f_getUncertaintyBounds(c_outOfModelChargingPerDay);
ArrayList<double[]> uncertaintyBounds_LWC = f_getUncertaintyBounds(c_leftWhileChargingPerDay);
ArrayList<double[]> uncertaintyBounds_LWCWDA = f_getUncertaintyBounds(c_leftWhileChargingWithDelayedAccessPerDay);
ArrayList<double[]> uncertaintyBounds_LUC = f_getUncertaintyBounds(c_leftUnchargedPerDay);
ArrayList<double[]> uncertaintyBounds_SCS = f_getUncertaintyBounds(c_percSatisfiedChargingSessionsPerDay);
ArrayList<double[]> uncertaintyBounds_CS = f_getUncertaintyBounds(c_chargingSessionsPerDay);
ArrayList<double[]> uncertaintyBounds_RCS = f_getUncertaintyBounds(c_requiredChargingSessionsPerDay);

results.setSuccessRate_b1(uncertaintyBounds_SR_b1);
results.setSuccessRate_b2(uncertaintyBounds_SR_b2);
results.setSuccessRate_b3(uncertaintyBounds_SR_b3);

results.setAvgProb_b1(uncertaintyBounds_AP_b1);
results.setAvgProb_b2(uncertaintyBounds_AP_b2);
results.setAvgProb_b3(uncertaintyBounds_AP_b3);

results.setOutOfModelChargingPerDay(uncertaintyBounds_OoMC);
results.setLeftWhileChargingPerDay(uncertaintyBounds_LWC);
results.setLeftWhileChargingWithDelayedAccessPerDay(uncertaintyBounds_LWCWDA);
results.setLeftUnchargedPerDay(uncertaintyBounds_LUC);
results.setPercSatisfiedChargingSessionsPerDay(uncertaintyBounds_SCS);

results.setChargingSessionsPerDay(uncertaintyBounds_CS);
results.setRequiredChargingSessionsPerDay(uncertaintyBounds_RCS);

c_MCResults.add(results);

f_setMCGraphs(results);
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
	traceln("Finished MC iteration " + iteration);
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
c_outOfModelChargingPerDay.clear();
c_leftWhileChargingPerDay.clear();
c_leftWhileChargingWithDelayedAccessPerDay.clear();
c_percSatisfiedChargingSessionsPerDay.clear();
c_leftUnchargedPerDay.clear();
c_chargingSessionsPerDay.clear();
c_requiredChargingSessionsPerDay.clear();
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

int f_getTrueLastRow(int sheetIndex)
{/*ALCODESTART::1755534050897*/
int lastRow = excel_exportResults.getLastRowNum(sheetIndex);
for (int r = lastRow; r >= 3; r--) {
	int lastCol = excel_exportResults.getLastCellNum(sheetIndex, r);
	for (int c = 1; c <= lastCol; c++) {
	
	traceln("row " + r + " col " + c + " sheet " + sheetIndex);
	
	if(excel_exportResults.getCellType(sheetIndex, r, c) == CellType.NUMERIC){
	
		Double val = excel_exportResults.getCellNumericValue(sheetIndex, r, c);
			if (val != null) {
				return r; // found last non-empty row
			}
		}
	else if(excel_exportResults.getCellType(sheetIndex, r, c) == CellType.STRING){
	
		String vals = excel_exportResults.getCellStringValue(sheetIndex, r, c);
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

