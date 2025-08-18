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
if(v_rapidRun = false){
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
	DataSet data_avgTrust = new DataSet(size);
	DataSet data_avgPSI = new DataSet(size);
	for(int i=0; i < m.ar_avgNorms.length; i++){
		data_avgNorms.add(i, m.ar_avgNorms[i]);
		data_avgTrust.add(i, m.ar_avgTrust[i]);
		data_avgPSI.add(i, m.ar_avgPSI[i]);
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
c_leftUnchargedPerDay.add(m.ar_leftUncharged);

/*ALCODEEND*/}

double f_setMCGraphs(J_MCResult results)
{/*ALCODESTART::1755183201656*/
//Create datasets
int size = results.getSuccessRate_b1().get(0).length;
int days = results.getLeftUnchargedPerDay().get(0).length;
DataSet data_successRate_b1 = new DataSet(size);
DataSet data_successRate_b2 = new DataSet(size);
DataSet data_successRate_b3 = new DataSet(size);
DataSet data_avgProbability_b1 = new DataSet(size);
DataSet data_avgProbability_b2 = new DataSet(size);
DataSet data_avgProbability_b3 = new DataSet(size);
DataSet data_outOfModelCharging = new DataSet(days);
DataSet data_leftWhileCharging = new DataSet(days);
DataSet data_leftUncharged = new DataSet(days);
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
	data_outOfModelCharging.add(i, results.getOutOfModelChargingPerDay().get(0)[i]);
	data_leftUncharged.add(i, results.getLeftUnchargedPerDay().get(0)[i]);	
	data_leftWhileCharging.add(i, results.getLeftWhileChargingPerDay().get(0)[i]);
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
pl_leftWhileCharging.setFixedHorizontalScale(0, days);
pl_leftUncharged.setFixedHorizontalScale(0, days);

pl_successRate1.addDataSet(data_successRate_b1, title, color, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_successRate2.addDataSet(data_successRate_b2, title, color, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_successRate3.addDataSet(data_successRate_b3, title, color, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_avgProb1.addDataSet(data_avgProbability_b1, title, color, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_avgProb2.addDataSet(data_avgProbability_b2, title, color, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_avgProb3.addDataSet(data_avgProbability_b3, title, color, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
	
pl_outOfModelCharge.addDataSet(data_outOfModelCharging, title, color, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_leftWhileCharging.addDataSet(data_leftWhileCharging, title, color, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_leftUncharged.addDataSet(data_leftUncharged, title, color, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
/*ALCODEEND*/}

DataSet f_writeMCToExcel()
{/*ALCODESTART::1755518313240*/
//Get excel file and indexes
excel_exportResults.readFile();

int scenarioIndex = simulationCount;
int sheetIndex = 1;
int sheetIndexPerDay = 2;

for(J_MCResult r : c_MCResults){
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

ArrayList<double[]> unceraintyBounds_SR_b1 = f_getUncertaintyBounds(c_succesRate_b1_MC);
ArrayList<double[]> unceraintyBounds_SR_b2 = f_getUncertaintyBounds(c_succesRate_b2_MC);
ArrayList<double[]> unceraintyBounds_SR_b3 = f_getUncertaintyBounds(c_succesRate_b3_MC);
ArrayList<double[]> unceraintyBounds_AP_b1 = f_getUncertaintyBounds(c_avgProb_b1_MC);
ArrayList<double[]> unceraintyBounds_AP_b2 = f_getUncertaintyBounds(c_avgProb_b2_MC);
ArrayList<double[]> unceraintyBounds_AP_b3 = f_getUncertaintyBounds(c_avgProb_b3_MC);
ArrayList<double[]> unceraintyBounds_OoMC = f_getUncertaintyBounds(c_outOfModelChargingPerDay);
ArrayList<double[]> unceraintyBounds_LWH = f_getUncertaintyBounds(c_leftWhileChargingPerDay);
ArrayList<double[]> unceraintyBounds_LUC = f_getUncertaintyBounds(c_leftUnchargedPerDay);

results.setSuccessRate_b1(unceraintyBounds_SR_b1);
results.setSuccessRate_b2(unceraintyBounds_SR_b2);
results.setSuccessRate_b3(unceraintyBounds_SR_b3);

results.setAvgProb_b1(unceraintyBounds_AP_b1);
results.setAvgProb_b2(unceraintyBounds_AP_b2);
results.setAvgProb_b3(unceraintyBounds_AP_b3);

results.setOutOfModelChargingPerDay(unceraintyBounds_OoMC);
results.setLeftWhileChargingPerDay(unceraintyBounds_LWH);
results.setLeftUnchargedPerDay(unceraintyBounds_LUC);

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
c_leftUnchargedPerDay.clear();

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

