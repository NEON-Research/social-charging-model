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
if(v_rapidRun){
	m.f_endRun();
}
else {
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

Color color = randomFrom(c_colorPalette);
String title = "run " + simulationCount;


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

f_setGraphsPerRun(color, title);

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

/*ALCODEEND*/}

double f_setGraphsPerRun(Color color,String title)
{/*ALCODESTART::1755157664950*/
Main m = mains.get(0);

DataItem di1 = new DataItem();
di1.setValue(m.v_unfulfilledChargingSessions_perc);
DataItem di2 = new DataItem();
di2.setValue(m.v_outOfModelCharging_perc);

ch_percOutOfModelCharge.addDataItem(di1, title, color);
ch_percUnfulfilledChargingSessions.addDataItem(di2, title, color);

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
/*ALCODEEND*/}

double f_exportMCResults()
{/*ALCODESTART::1755183201656*/
int nTimePoints = c_succesRate_b1_MC.get(0).length;
int nRuns = c_succesRate_b1_MC.size();

double[] mean = new double[nTimePoints];
double[] lower = new double[nTimePoints];
double[] upper = new double[nTimePoints];

//Get excel file and indexes
excel_exportResults.readFile();
int colIndexScen = 1;
int colIndexTimestep = 2;
int colIndexMean = 3;
int colIndexLower = 4;
int colIndexUpper = 5;
int scenarioIndex = 1;
int sheetIndex = 1;
int rowIndex = excel_exportResults.getLastRowNum(sheetIndex) + 1;


for (int t = 0; t < nTimePoints; t++) {
	double[] valuesAtTimeT = new double[nRuns];
	for (int r = 0; r < nRuns; r++) {
		valuesAtTimeT[r] = c_succesRate_b1_MC.get(r)[t];
	}
	
	Arrays.sort(valuesAtTimeT);
	mean[t] = Arrays.stream(valuesAtTimeT).average().getAsDouble();
	lower[t] = valuesAtTimeT[(int)(0.05 * nRuns)];  // 5th percentile
	upper[t] = valuesAtTimeT[(int)(0.95 * nRuns)];  // 95th percentile
    
	excel_exportResults.setCellValue(scenarioIndex, sheetIndex, rowIndex, colIndexScen);
	excel_exportResults.setCellValue(r, sheetIndex, rowIndex, colIndexTimestep);
	excel_exportResults.setCellValue(mean, sheetIndex, rowIndex, colIndexMean);
	excel_exportResults.setCellValue(lower, sheetIndex, rowIndex, colIndexLower);
	excel_exportResults.setCellValue(upper, sheetIndex, rowIndex, colIndexUpper);
    rowIndex++;
}


excel_exportResults.writeFile();



/*ALCODEEND*/}

