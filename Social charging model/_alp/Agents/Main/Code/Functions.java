double f_createEVOwners(int nbOfInputTrips)
{/*ALCODESTART::1745925425403*/
int EVs = roundToInt(p_cars*p_shareEVs);
for(int i = 0; i < EVs; i++){
	EVOwner x = add_EVOwners();
	x.v_type = EV;
    f_initializeTrips(nbOfInputTrips, x);
    
    int minBatteryCap_kWh = 50;
    int maxBatteryCap_kWh = 80;
    int meanBatteryCap_kWh = 65;
    int spread = 5;
    
    x.v_batteryCapacity_kWh = normal(minBatteryCap_kWh, maxBatteryCap_kWh, meanBatteryCap_kWh, spread);
    double randomShare = uniform(0.25, 1);
    x.v_electricityInBattery_kWh = x.v_batteryCapacity_kWh * randomShare;
    x.v_soc = x.v_electricityInBattery_kWh / x.v_batteryCapacity_kWh;
    c_carOwners.add(x);
}
/*ALCODEEND*/}

double f_initializeModel()
{/*ALCODESTART::1745928671433*/
v_hourOfDay = 0;

ef_spvars.readFile();
f_setArrays();

int evOwners = roundToInt(p_cars*p_shareEVs);
f_setHistogramArrays(evOwners);
f_getRegressionCoefficients();
//f_setModelEffects();
//f_setThresholds();

//Get nb of trips in database
nbOfInputTrips = (int) selectFirstValue(int.class,
	"SELECT COUNT (*) FROM vehicle_trips;"
);
//Create ICEs and EVs
f_createICECarOwners();
f_generateSyntehticPopulationEVs(evOwners);
f_normalizeFromLikert();

ef_spvars.close();
/*
if (sortedRealData != null) {
    for (List<Double> innerList : sortedRealData) {
        innerList.clear();  // Clear contents of inner list
    }
    //sortedRealData.clear();  // Then clear the outer list
}*/

f_setChargePoints();
f_simulateFirstWeekToGetInitialLocationCars();

/*ALCODEEND*/}

int f_initalizeMinuteOfWeek()
{/*ALCODESTART::1745929564814*/
int daySunday0 = getDayOfWeek();
int dayOfWeek = 0;
if(daySunday0 == 0){
	dayOfWeek = 6;
}
else {
	dayOfWeek = daySunday0 - 1;
}
int hourOfDay = getHourOfDay();
int minuteOfHour = getMinute();
v_minuteOfWeek = (dayOfWeek * 24 * 60 + hourOfDay * 60 + minuteOfHour);


/*ALCODEEND*/}

double f_countTotals()
{/*ALCODESTART::1745939185407*/
int EVs = EVOwners.size();

//COUNT CAR BEHAVIOR
double carsOnTrip = count(c_carOwners, x->x.v_status == ON_TRIP);
double ICECarsParkedNonCP = count(ICECarOwners, x->x.v_status == PARKED_NON_CHARGE_POINT_CHARGING_NOT_REQUIRED);
double EVsParkedNonCPChargingRequired = count(EVOwners, x->x.v_status == PARKED_NON_CHARGE_POINT_CHARGING_REQUIRED);
double EVsParkedNonCPChargingNotRequired = count(EVOwners, x->x.v_status == PARKED_NON_CHARGE_POINT_CHARGING_NOT_REQUIRED);
double EVsParkedAtCPCharging = count(EVOwners, x->x.v_status == PARKED_CHARGE_POINT_CHARGING);
double EVsParkedAtCPIdle = count(EVOwners, x->x.v_status == PARKED_CHARGE_POINT_IDLE);
double EVsArriving = count(EVOwners, x->x.v_status == ARRIVING);

if(EVsArriving > 0){
	traceln("Error: " + EVsArriving + " EVs arriving did not get a new stauts");
}

// Charge Points
int CPAvailable = count(c_chargePoints, x->x.isOccupied() == false);
int CPOccupied = c_chargePoints.size() - CPAvailable;

//SUCCES RATES and avg probabiliy
count_b1_successful = 0;
count_b2_successful = 0;
count_b3_successful = 0;
count_b1_notSuccessful = 0;
count_b2_notSuccessful = 0;
count_b3_notSuccessful = 0;
count_successfulRechecks = 0;
count_unsuccessfulRechecks = 0;
int count_b2_noIdleChargers = 0;

outOfModelCharge_kWh = 0.0;
count_leftWhileCharging = 0;
count_leftUncharged = 0;

double totalProb_b1 = 0.0;
double totalProb_b2 = 0.0;
double totalProb_b3 = 0.0;

double totalNorms = 0.0;
double totalTrust = 0;
double totalPSI = 0.0;

for (EVOwner x : EVOwners) {
    count_b1_successful      += x.count_b1_successful;
    count_b1_notSuccessful   += x.count_b1_notSuccessful;

    count_b2_successful      += x.count_b2_successful;
    count_b2_notSuccessful   += x.count_b2_notSuccessful;
    count_b2_noIdleChargers  += x.count_b2_noIdleChargers;

    count_b3_successful      += x.count_b3_successful;
    count_b3_notSuccessful   += x.count_b3_notSuccessful;
    
    count_successfulRechecks += x.count_successfulRechecks;
    count_unsuccessfulRechecks += x.count_unsuccessfulRechecks;
    
    totalProb_b1 += x.v_prob_b1;
	totalProb_b2 += x.v_prob_b2;
	totalProb_b3 += x.v_prob_b3;
	
	totalNorms += x.v_norms;
	totalTrust += x.v_trust;
	totalPSI += x.v_perc_social_interdep;
	
	outOfModelCharge_kWh += x.v_outOfModelCharge_kWh;
	count_leftWhileCharging += x.count_leftWhileCharging;
	count_leftUncharged += x.count_leftUncharged;
}


int total_b1 = count_b1_successful + count_b1_notSuccessful;
successRate_b1 = (total_b1 != 0) ? ((double) count_b1_successful / total_b1) : 0;

int total_b2 = count_b2_successful + count_b2_notSuccessful;
successRate_b2 = (total_b2 != 0) ? ((double) count_b2_successful / total_b2) : 0;

int total_b3 = count_b3_successful + count_b3_notSuccessful;
successRate_b3 = (total_b3 != 0) ? ((double) count_b3_successful / total_b3) : 0;

int total_rechecks = count_successfulRechecks + count_unsuccessfulRechecks;
successRate_rechecks = (total_rechecks != 0) ? ((double) count_successfulRechecks / total_rechecks) : 0;

//Added no idle chargers category
count_b2_notSuccessful -= count_b2_noIdleChargers;

//Probabilities
double avgProb_b1 = totalProb_b1 / EVs;
double avgProb_b2 = totalProb_b2 / EVs;
double avgProb_b3 = totalProb_b3 / EVs;

//Norms, trust, PSI
double avgNorms = totalNorms / EVs;
double avgTrust = totalTrust / EVs;
double avgPSI = totalPSI / EVs;

//SETTING ARRAYS
// Charge Points
ar_CPOccupied[v_timestep] = CPOccupied;
ar_CPAvailable[v_timestep] = CPAvailable;

// Cars & Parking
ar_carsOnTrip[v_timestep] = carsOnTrip;
ar_ICECarsParkedNonCP[v_timestep] = ICECarsParkedNonCP;
ar_EVsParkedNonCPChargingRequired[v_timestep] = EVsParkedNonCPChargingRequired;
ar_EVsParkedNonCPChargingNotRequired[v_timestep] = EVsParkedNonCPChargingNotRequired;
ar_EVsParkedAtCPCharging[v_timestep] = EVsParkedAtCPCharging;
ar_EVsParkedAtCPIdle[v_timestep] = EVsParkedAtCPIdle;


//Norms, trust, PSI
ar_avgNorms[v_timestep] = avgNorms;
ar_avgTrust[v_timestep] = avgTrust;
ar_avgPSI[v_timestep] = avgPSI;

//Success Rates
ar_successRate_b1[v_timestep] = successRate_b1;
ar_successRate_b2[v_timestep] = successRate_b2;
ar_successRate_b3[v_timestep] = successRate_b3;
ar_successRate_rechecks[v_timestep] = successRate_rechecks;

//Probabilities
ar_avgProbability_b1[v_timestep] = avgProb_b1;
ar_avgProbability_b2[v_timestep] = avgProb_b2;
ar_avgProbability_b3[v_timestep] = avgProb_b3;

//Success/Not success
ar_successful_b1[v_timestep] = count_b1_successful;
ar_successful_b2[v_timestep] = count_b2_successful;
ar_successful_b3[v_timestep] = count_b3_successful;
ar_unsuccessful_b1[v_timestep] = count_b1_notSuccessful;
ar_unsuccessful_b2[v_timestep] = count_b2_notSuccessful;
ar_unsuccessful_b3[v_timestep] = count_b3_notSuccessful;
ar_noIdleChargers_b2[v_timestep] = count_b2_noIdleChargers;








/*ALCODEEND*/}

double f_createICECarOwners()
{/*ALCODESTART::1745941028755*/
int ICECars = roundToInt(p_cars*(1-p_shareEVs));
for(int i = 0; i < ICECars; i++){
	CarOwner x = add_ICECarOwners();
	x.v_type = ICE;
    f_initializeTrips(x);
    c_carOwners.add(x);
}

/*ALCODEEND*/}

double f_initializeTrips(CarOwner carOwner)
{/*ALCODESTART::1745941211927*/
//Get trip index based on nb of input trips
int tripIndex = uniform_discr(0, nbOfInputTrips - 1);

//Get result set of trip index
ResultSet rs = selectResultSet("SELECT * FROM vehicle_trips LIMIT 1 OFFSET " + tripIndex);
// Check if there is a row in the rs
if (rs.next()) {        
	// Retrieve the number of trips from the result
	int nbTripsPerWeek = rs.getInt("nb_trips");
	        
	// Loop over the trips and add them to the EVOwner's trip schedule
	for (int t = 1; t <= nbTripsPerWeek; t++) {
		double dep = rs.getDouble("departure" + t);
		double arr = rs.getDouble("arrival" + t);
		double dist = rs.getDouble("distance" + t);
	
		carOwner.c_tripSchedule.add(new J_Trip(dep, arr, dist));
	}
}
int minuteOfWeek = 0;
carOwner.f_initializeNextTrip(minuteOfWeek);

/*ALCODEEND*/}

double f_simulatePeriod(int nbOfTimesteps)
{/*ALCODESTART::1746025438383*/
v_timestep = 0;
v_hourOfDay = 0;
v_day = 0;
initializationMode = false;

//Trigger over timesteps
for(int i=0; i < nbOfTimesteps; i++){
	f_simulateTimestep();
}


f_endSimulationPeriod();
//traceln("Simulation finished after " + v_timestep + " timesteps ");

	
/*ALCODEEND*/}

double f_updateChart()
{/*ALCODESTART::1746089953946*/
// Count EVs chart
ch_countEVs.removeAll();
ch_countEVs.addDataSet(f_arrayToDataSet(ar_carsOnTrip), "Cars on trip", coral);
ch_countEVs.addDataSet(f_arrayToDataSet(ar_ICECarsParkedNonCP), "ICE cars parked", darkKhaki);
ch_countEVs.addDataSet(f_arrayToDataSet(ar_EVsParkedNonCPChargingNotRequired), "EVs parked not at charge point charging not required", limeGreen);
ch_countEVs.addDataSet(f_arrayToDataSet(ar_EVsParkedNonCPChargingRequired), "EVs parked not at charge point charging required", slateGray);
ch_countEVs.addDataSet(f_arrayToDataSet(ar_EVsParkedAtCPCharging), "EVs parked at charge point and charging", darkMagenta);
ch_countEVs.addDataSet(f_arrayToDataSet(ar_EVsParkedAtCPIdle), "EVs parked at charge point and idle", orange);

// Charge points chart
ch_countCPs.removeAll();
ch_countCPs.addDataSet(f_arrayToDataSet(ar_CPAvailable), "Charge points available", limeGreen);
ch_countCPs.addDataSet(f_arrayToDataSet(ar_CPOccupied), "Charge points occupied", coral);

// Success rate chart
pl_successRate.removeAll();
pl_successRate.addDataSet(f_arrayToDataSet(ar_successRate_b1), "Behavior 1: move car", sandyBrown, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_successRate.addDataSet(f_arrayToDataSet(ar_successRate_b2), "Behavior 2: request move", lightSeaGreen, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_successRate.addDataSet(f_arrayToDataSet(ar_successRate_b3), "Behavior 3: notify neighbor", lightSlateBlue, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_successRate.addDataSet(f_arrayToDataSet(ar_successRate_rechecks), "Rechecking CP availability", mediumOrchid, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);

// Probability chart
pl_probability.removeAll();
pl_probability.addDataSet(f_arrayToDataSet(ar_avgProbability_b1), "Behavior 1: move car", sandyBrown, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_probability.addDataSet(f_arrayToDataSet(ar_avgProbability_b2), "Behavior 2: request move", lightSeaGreen, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_probability.addDataSet(f_arrayToDataSet(ar_avgProbability_b3), "Behavior 3: notify neighbor", lightSlateBlue, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);

// Socio-psychological chart
pl_socioPsychologicalLearning.removeAll();
pl_socioPsychologicalLearning.addDataSet(f_arrayToDataSet(ar_avgNorms), "Average Norms", sandyBrown, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_socioPsychologicalLearning.addDataSet(f_arrayToDataSet(ar_avgTrust), "Average Trust", lightSeaGreen, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_socioPsychologicalLearning.addDataSet(f_arrayToDataSet(ar_avgPSI), "Average PSI", lightSlateBlue, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);

// Interactions per day chart
pl_interactionPerDay.removeAll();
pl_interactionPerDay.addDataSet(f_arrayToDataSet(ar_interactionsPerDay_b1), "Behavior 1: move car", sandyBrown, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_interactionPerDay.addDataSet(f_arrayToDataSet(ar_interactionsPerDay_b2), "Behavior 2: request move", lightSeaGreen, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_interactionPerDay.addDataSet(f_arrayToDataSet(ar_interactionsPerDay_b3), "Behavior 3: notify neighbor", lightSlateBlue, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_interactionPerDay.addDataSet(f_arrayToDataSet(ar_rechecksPerDay), "Rechecking CP availability", mediumOrchid, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);

// Behavior charts
ch_b1.removeAll();
ch_b1.addDataSet(f_arrayToDataSet(ar_unsuccessful_b1), "Non successful interactions", red);
ch_b1.addDataSet(f_arrayToDataSet(ar_successful_b1), "Successful interactions", green);

ch_b2.removeAll();
ch_b2.addDataSet(f_arrayToDataSet(ar_unsuccessful_b2), "Non successful interactions", red);
ch_b2.addDataSet(f_arrayToDataSet(ar_noIdleChargers_b2), "No idle chargers", orange);
ch_b2.addDataSet(f_arrayToDataSet(ar_successful_b2), "Successful interactions", green);

ch_b3.removeAll();
ch_b3.addDataSet(f_arrayToDataSet(ar_unsuccessful_b3), "Non successful interactions", red);
ch_b3.addDataSet(f_arrayToDataSet(ar_successful_b3), "Successful interactions", green);

f_setHistograms();
/*ALCODEEND*/}

double f_endSimulationPeriod()
{/*ALCODESTART::1746090357452*/
if(startup_agent.v_rapidRun == false){
	f_updateChart();
}
double outOfModelCharging = 0.0;
double totalCharging = 0.0;
int countUnfinishedCharging = 0;
int countLeftUncharged = 0;
int countTotalRequiredCharging = 0;
int countTotalChargingSessions = 0;

for(EVOwner x : EVOwners){
	outOfModelCharging += x.v_outOfModelCharge_kWh;
	totalCharging += x.v_totalElectricityCharged_kWh;
	countUnfinishedCharging += x.count_leftWhileCharging;
	countLeftUncharged += x.count_leftUncharged;
	countTotalRequiredCharging += x.count_chargingRequired;
	countTotalChargingSessions += x.count_chargingSessions;
}
double avgChargingSessionPerEVPerDay = roundToDecimal((double) countTotalChargingSessions / EVOwners.size() / p_days,3);

v_unfulfilledChargingSessions_perc = (double) (countLeftUncharged + countUnfinishedCharging) / countTotalChargingSessions;
v_outOfModelCharging_perc = outOfModelCharging/totalCharging;
/*
traceln("Total charging sessions = " + countTotalChargingSessions + ", is avg per EV per day " + avgChargingSessionPerEVPerDay);
traceln("Unfinished charging = " + roundToInt((double) countUnfinishedCharging / countTotalChargingSessions * 100) + "% of charging sessions");
traceln("Left without charging = " + roundToInt((double) countLeftUncharged / countTotalRequiredCharging * 100) + "% of required charging sessions");
traceln("Out of model charging = " + roundToInt((double) outOfModelCharging/totalCharging * 100) + "% of total charging");
*/
/*ALCODEEND*/}

double f_writeResultsToExcel(ExcelFile excel_exportResults)
{/*ALCODESTART::1746096423531*/
excel_exportResults.readFile();

//traceln("Start writing results to excel!");
int sheetIndex = 1;
int columnYear = 1;
int columnNGB = 2;
int columnNGBl = 3;
int columnHHP = 4;
int columnEHP = 5;
int columnDH = 6;

//Clear the sheet first

//Set column names
int row = 1;
excel_exportResults.setCellValue("simulation run", sheetIndex, row, columnYear);
excel_exportResults.setCellValue("chargingSatisfaction", sheetIndex, row, columnNGB);
/*
excel_exportResults.setCellValue("Gas block heating", sheetIndex, row, columnNGBl);
excel_exportResults.setCellValue("Hybrid heat pump", sheetIndex, row, columnHHP);
excel_exportResults.setCellValue("Electric heat pump", sheetIndex, row, columnEHP);
excel_exportResults.setCellValue("District heating", sheetIndex, row, columnDH);

//Get data
double startYear = v_startYear;
double endYear = v_startYear + data_naturalGasBoilers_nb.size();
traceln("Start year " + startYear + " end year " + endYear);
for (int i = 0; i < endYear - startYear + 1 ; i++) {

	//Time series
	excel_exportResults.setCellValue(startYear + i, sheetIndex, i+2, columnYear);
	excel_exportResults.setCellValue(data_naturalGasBoilers_nb.getY(i), sheetIndex, i+2, columnNGB);
	excel_exportResults.setCellValue(data_naturalGasBlock_nb.getY(i), sheetIndex, i+2, columnNGBl);
	excel_exportResults.setCellValue(data_hybridHeatPumps_nb.getY(i), sheetIndex, i+2, columnHHP);
	excel_exportResults.setCellValue(data_electricHeatPumps_nb.getY(i), sheetIndex, i+2, columnEHP);
	excel_exportResults.setCellValue(data_districtHeating_nb.getY(i), sheetIndex, i+2, columnDH);
}
*/
//Write file
excel_exportResults.writeFile();
excel_exportResults.close();

traceln("Finished writing results to excel!");
/*ALCODEEND*/}

double f_recheckAvailableChargePoints()
{/*ALCODESTART::1747135321123*/
int EVsAwaitingCharge = count(c_carOwners, x->x.v_status == PARKED_NON_CHARGE_POINT_CHARGING_REQUIRED);
boolean inMoveableTimePeriod = false;
if( v_hourOfDay >= 7 && v_hourOfDay <= 22){
	inMoveableTimePeriod = true;
}


if(v_chargePointAvailable > 0 && EVsAwaitingCharge > 0 && inMoveableTimePeriod){
	List<EVOwner> tempEVsAwaitingCharge = EVOwners.stream()
        .filter(x -> x.v_status == PARKED_NON_CHARGE_POINT_CHARGING_REQUIRED)
        .collect(Collectors.toList());
    
    while(v_chargePointAvailable > 0 && !tempEVsAwaitingCharge.isEmpty()){
    	EVOwner EV = randomWhere(tempEVsAwaitingCharge, x->x.v_status == PARKED_NON_CHARGE_POINT_CHARGING_REQUIRED);
		EV.f_setParkingStatus();
		//traceln("EV " + EV.getIndex() + " tried to find cp");
		tempEVsAwaitingCharge.remove(EV);
			
	}
}
/*ALCODEEND*/}

double f_simulateFirstWeekToGetInitialLocationCars()
{/*ALCODESTART::1748942230602*/
v_timestep = 0;
int minutesPerWeek = 7 * 24 * 60;
double timestepsInWeek = (double) minutesPerWeek / p_timestep_minutes;
initializationMode = true;

//Trigger over timesteps
for(int i=0; i < timestepsInWeek; i++){
	f_simulateTimestep();
}

f_clearCounts();

//traceln("Finished initial week for division car location");
/*ALCODEEND*/}

double f_generateSyntheticAgents(int numAgents,List<List<Double>>  sortedRealData)
{/*ALCODESTART::1752581980944*/
//Gaussian copula
int numVars = correlationMatrix.length;

// Cholesky decomposition
RealMatrix corr = MatrixUtils.createRealMatrix(correlationMatrix);
RealMatrix L = new CholeskyDecomposition(corr).getL();

List<double[]> synthethicAgents = new ArrayList<>();
NormalDistribution standardNormal = new NormalDistribution(0, 1);

for(int i= 0; i < numAgents; i++){
	//Step 1: Generate independent standard normals
	double[] z = new double[numVars];
	for (int j = 0; j < numVars; j++) {
		z[j] = standardNormal.sample();
	}
	
	//Step 2: Apply Cholesky to introduce correlation
	RealVector zVector = new ArrayRealVector(z);
	RealVector correlated = L.operate(zVector);
	
	//Step 3: Transform to uniforms, then to empirical values
	double[] agentAttributes = new double[numVars];
	for(int j= 0; j < numVars; j++){
		double u = standardNormal.cumulativeProbability(correlated.getEntry(j));
		agentAttributes[j] = f_inverseECDF(sortedRealData.get(j), u);
	}
	
	f_addEVOwner(agentAttributes);
	
}
/*ALCODEEND*/}

double f_generateSyntehticPopulationEVs(int evOwners)
{/*ALCODESTART::1752581980946*/
//1. get correlation matrix
f_getCorrelationMatrixFromExcel();

//2. get sorter real values from data
List<List<Double>> sortedRealData = new ArrayList<>();
sortedRealData = f_getSortedSocialPsychologicalData();

//3. generate syntethic agents
f_generateSyntheticAgents(evOwners, sortedRealData);

/*
// Optional: print a few samples
for (EVOwner x : EVOwners){
    if(x.getIndex() < 10){
    	System.out.printf("Agent %d: Norms=%.2f, Trust=%.2f, Rep=%.2f, Interdep=%.2f\n",
		x.getIndex(), x.v_norms, x.v_trust, x.v_reputationalConcern, x.v_perceived_social_interdependence);
	}
}
*/
if(startup_agent.v_rapidRun == false){
	f_histogramsPopData();
}
/*ALCODEEND*/}

double f_getCorrelationMatrixFromExcel()
{/*ALCODESTART::1752581980948*/

String sheetName = "correlation_matrix";
int matrixSize = ef_spvars.getLastRowNum(sheetName);

correlationMatrix = new double[matrixSize-1][matrixSize-1];

for(int rowIndex = 2; rowIndex < matrixSize + 1; rowIndex++){
	for(int columnIndex = 2; columnIndex < matrixSize + 1; columnIndex++){
		double value = ef_spvars.getCellNumericValue(sheetName, rowIndex, columnIndex);
		correlationMatrix[rowIndex-2][columnIndex-2] = value;
	}
}
	
/*ALCODEEND*/}

List<List<Double>> f_getSortedSocialPsychologicalData()
{/*ALCODESTART::1752585514702*/
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
int col_trust = 2;
int col_rc = 3;
int col_psi = 4;
int col_b1 = 5;
int col_b2 = 6;
int col_b3 = 7;
/*
List<Double> norms = new ArrayList<>();
List<Double> trust = new ArrayList<>();
List<Double> rc = new ArrayList<>();
List<Double> psi = new ArrayList<>();
List<Double> b1 = new ArrayList<>();
List<Double> b2 = new ArrayList<>();
List<Double> b3 = new ArrayList<>();
*/
for(int rowIndex = 2; rowIndex < size + 1; rowIndex++){
	c_norms.add(ef_spvars.getCellNumericValue(sheetName, rowIndex, col_norms));
	c_trust.add(ef_spvars.getCellNumericValue(sheetName, rowIndex, col_trust));
	c_rc.add(ef_spvars.getCellNumericValue(sheetName, rowIndex, col_rc));
	c_psi.add(ef_spvars.getCellNumericValue(sheetName, rowIndex, col_psi));
	c_b1.add(ef_spvars.getCellNumericValue(sheetName, rowIndex, col_b1));
	c_b2.add(ef_spvars.getCellNumericValue(sheetName, rowIndex, col_b2));
	c_b3.add(ef_spvars.getCellNumericValue(sheetName, rowIndex, col_b3));
}


Collections.sort(c_norms);
Collections.sort(c_trust);
Collections.sort(c_rc);
Collections.sort(c_psi);
Collections.sort(c_b1);
Collections.sort(c_b2);
Collections.sort(c_b3);

List<List<Double>> sortedRealData = new ArrayList<>();
return sortedRealData = Arrays.asList(c_norms, c_trust, c_rc, c_psi, c_b1, c_b2, c_b3);

/*ALCODEEND*/}

double f_inverseECDF(List<Double> sortedData,double u)
{/*ALCODESTART::1752586755791*/
// ECDF inverse: maps uniform u in [0,1] to data value
/*
int index = (int) Math.floor(u * sortedData.size());
index = Math.min(index, sortedData.size() - 1);
return sortedData.get(index);
*/
int n = sortedData.size();
    
// Clamp u to avoid exact 0 or 1
u = Math.max(0.0001, Math.min(0.9999, u));

double idx = u * (n - 1);
int lower = (int) Math.floor(idx);
int upper = (int) Math.ceil(idx);
    
if (upper == lower) return sortedData.get(lower); // Exact match
    
double weight = idx - lower;
return (1 - weight) * sortedData.get(lower) + weight * sortedData.get(upper);
/*ALCODEEND*/}

double f_addEVOwner(double[] agentAttributes)
{/*ALCODESTART::1752587249607*/
EVOwner x = add_EVOwners();

x.v_norms = agentAttributes[0];
x.v_trust = agentAttributes[1];
x.v_reputational_concern = agentAttributes[2];
x.v_perc_social_interdep = agentAttributes[3];
x.v_prob_b1 = agentAttributes[4];
x.v_prob_b2 = agentAttributes[5];
x.v_prob_b3 = agentAttributes[6];

x.v_type = EV;
f_initializeTrips(x);
    
int minBatteryCap_kWh = 50;
int maxBatteryCap_kWh = 80;
int meanBatteryCap_kWh = 65;
int spread = 5;
    
x.v_batteryCapacity_kWh = normal(minBatteryCap_kWh, maxBatteryCap_kWh, meanBatteryCap_kWh, spread);
double randomShare = uniform(0.25, 1);
x.v_electricityInBattery_kWh = x.v_batteryCapacity_kWh * randomShare;
x.v_soc = x.v_electricityInBattery_kWh / x.v_batteryCapacity_kWh;
c_carOwners.add(x);

/*ALCODEEND*/}

double f_histogramsPopData()
{/*ALCODESTART::1752589029155*/

int nIntervals = 20;
double initialIntervalWidth = 0.35;
boolean calcCDF = true;
boolean calcPercentiles = false;
double lowPercent = 10;
double highPercent = 10;

HistogramSmartData hs_data_norms_pop = new HistogramSmartData(nIntervals, initialIntervalWidth, calcCDF, calcPercentiles, lowPercent, highPercent);
HistogramSmartData hs_data_trust_pop = new HistogramSmartData(nIntervals, initialIntervalWidth, calcCDF, calcPercentiles, lowPercent, highPercent);
HistogramSmartData hs_data_rc_pop = new HistogramSmartData(nIntervals, initialIntervalWidth, calcCDF, calcPercentiles, lowPercent, highPercent);
HistogramSmartData hs_data_psi_pop = new HistogramSmartData(nIntervals, initialIntervalWidth, calcCDF, calcPercentiles, lowPercent, highPercent);
HistogramSmartData hs_data_b1_pop = new HistogramSmartData(nIntervals, initialIntervalWidth, calcCDF, calcPercentiles, lowPercent, highPercent);
HistogramSmartData hs_data_b2_pop = new HistogramSmartData(nIntervals, initialIntervalWidth, calcCDF, calcPercentiles, lowPercent, highPercent);
HistogramSmartData hs_data_b3_pop = new HistogramSmartData(nIntervals, initialIntervalWidth, calcCDF, calcPercentiles, lowPercent, highPercent);

for(EVOwner x : EVOwners){
	hs_data_norms_pop.add(x.v_norms);
	hs_data_trust_pop.add(x.v_trust);
	hs_data_rc_pop.add(x.v_reputational_concern);
	hs_data_psi_pop.add(x.v_perc_social_interdep);

	hs_data_b1_pop.add(x.v_prob_b1);
	hs_data_b2_pop.add(x.v_prob_b2);
	hs_data_b3_pop.add(x.v_prob_b3);
}

Color colorLowPercent = green;
Color colorHighPercent = green;
Color colorPDF = crimson;
Color colorCDF = green;
float lineWidthCDF = 1;
Color colorMean = green;

hs_norms_data.addHistogram(hs_data_norms_pop, "Norms pop", colorLowPercent, colorHighPercent, colorPDF, colorCDF, lineWidthCDF, colorMean);

/*
String sheetName = "frequency_list";
int size = ef_spvars.getLastRowNum(sheetName);

int col_norms = 1;
int col_trust = 2;
int col_rc = 3;
int col_pci = 4;

List<Double> norms = new ArrayList<>();
List<Double> trust = new ArrayList<>();
List<Double> rc = new ArrayList<>();
List<Double> pci = new ArrayList<>();

for(int rowIndex = 2; rowIndex < size + 1; rowIndex++){
	norms.add(ef_spvars.getCellNumericValue(sheetName, rowIndex, col_norms));
	trust.add(ef_spvars.getCellNumericValue(sheetName, rowIndex, col_trust));
	rc.add(ef_spvars.getCellNumericValue(sheetName, rowIndex, col_rc));
	pci.add(ef_spvars.getCellNumericValue(sheetName, rowIndex, col_pci));
}
*/
/*
hs_data_norms_data.update();
hs_data_trust_data.update();
hs_data_rc_data.update();
hs_data_psi_data.update();
hs_data_norms_pop.update();
hs_data_trust_pop.update();
hs_data_rc_pop.update();

hs_norms_data.updateData();
hs_trust_data.updateData();
hs_rc_data.updateData();
hs_psi_data.updateData();
hs_norms_pop.updateData();
hs_trust_pop.updateData();
hs_rc_pop.updateData();
hs_psi_pop.updateData();
hs_b1_data.updateData();
hs_b2_data.updateData();
hs_b3_data.updateData();
hs_b1_pop.updateData();
hs_b2_pop.updateData();
hs_b3_pop.updateData();
*/
/*ALCODEEND*/}

double f_mediationResultsQuery(String var1,String var2)
{/*ALCODESTART::1752592809828*/
double value = (double) selectFirstValue(double.class,
	"SELECT value FROM mediation_results WHERE " + 
		"var_1 = ? AND " +
		"var_2 = ? LIMIT 1;",
		var1,
		var2
);

//traceln("var1 = " + var1 + " var2 = " + var2 + " value = " + value);
return value;

/*ALCODEEND*/}

double f_getRegressionCoefficients()
{/*ALCODESTART::1752592809830*/
//Norms, trust and RC to PSI
String var1 = "norms";
String var2 = "perceived_social_interdependence";
String key = var1+var2;
double value = f_mediationResultsQuery(var1, var2);
//c_variableWeights.put(key, value);
regCoef_norms_psi = value;

var1 = "trust";
key = var1+var2;
value = f_mediationResultsQuery(var1, var2);
//c_variableWeights.put(key, value);
regCoef_trust_psi = value;

var1 = "reputational_concern";
key = var1+var2;
value = f_mediationResultsQuery(var1, var2);
//c_variableWeights.put(key, value);
regCoef_rc_psi = value;

//PSI to behavior
var1 = "perceived_social_interdependence";
var2 = "b1_move_vehicle";
key = var1+var2;
value = f_mediationResultsQuery(var1, var2);
//c_variableWeights.put(key, value);
regCoef_psi_b1 = value;

var2 = "b2_request_move";
key = var1+var2;
value = f_mediationResultsQuery(var1, var2);
//c_variableWeights.put(key, value);
regCoef_psi_b2 = value;

var2 = "b3_notify_neighbor";
key = var1+var2;
value = f_mediationResultsQuery(var1, var2);
//c_variableWeights.put(key, value);
regCoef_psi_b3 = value;

//Trust to behavior
var1 = "trust";
var2 = "b2_request_move";
key = var1+var2;
value = f_mediationResultsQuery(var1, var2);
//c_variableWeights.put(key, value);
regCoef_trust_b2 = value;

var2 = "b3_notify_neighbor";
key = var1+var2;
value = f_mediationResultsQuery(var1, var2);
//c_variableWeights.put(key, value);
regCoef_trust_b3 = value;

/*ALCODEEND*/}

double f_setModelEffects()
{/*ALCODESTART::1753176262070*/
//B1
String b = "b1_move_vehicle";
modelEffect_b1 = (double) selectFirstValue(double.class,
	"SELECT fit_r2 FROM behavior_data WHERE " + 
		"behavior = ? LIMIT 1;",
		b
);

//B2
b = "b2_request_move";
modelEffect_b2 = (double) selectFirstValue(double.class,
	"SELECT fit_r2 FROM behavior_data WHERE " + 
		"behavior = ? LIMIT 1;",
		b
);

//B3
b = "b3_notify_neighbor";
modelEffect_b3 = (double) selectFirstValue(double.class,
	"SELECT fit_r2 FROM behavior_data WHERE " + 
		"behavior = ? LIMIT 1;",
		b
);
/*
double modelEffectMultiplier_b1 = 6;
double modelEffectMultiplier_b2 = 6;
double modelEffectMultiplier_b3 = 6;
*/
double modelEffectMultiplier_b1 = 0;
double modelEffectMultiplier_b2 = 0;
double modelEffectMultiplier_b3 = 0;

modelEffect_b1 = modelEffect_b1 * modelEffectMultiplier_b1;
modelEffect_b2 = modelEffect_b2 * modelEffectMultiplier_b2;
modelEffect_b3 = modelEffect_b3 * modelEffectMultiplier_b3;
/*ALCODEEND*/}

double f_setThresholds()
{/*ALCODESTART::1753176527439*/
threshold_b1 = 0.5;
threshold_b2 = 0.5;
threshold_b3 = 0.5;

/*ALCODEEND*/}

double f_normalizeFromLikert()
{/*ALCODESTART::1753184211075*/
regCoef_trust_psi = regCoef_trust_psi/6;
regCoef_norms_psi = regCoef_norms_psi/6;
regCoef_rc_psi = regCoef_rc_psi/6;
regCoef_psi_b1 = regCoef_psi_b1/6;
regCoef_psi_b2 = regCoef_psi_b2/6;
regCoef_psi_b3 = regCoef_psi_b3/6;
regCoef_trust_b2 = regCoef_trust_b2/6;
regCoef_trust_b3 = regCoef_trust_b3/6;

for(EVOwner x : EVOwners){
	x.v_norms = (x.v_norms - 1)/6;
	x.v_trust = (x.v_trust - 1)/6;
	x.v_reputational_concern = (x.v_reputational_concern - 1)/6;
	x.v_perc_social_interdep = (x.v_perc_social_interdep - 1)/6;
	x.v_prob_b1 = (x.v_prob_b1 - 1)/6;
	x.v_prob_b2 = (x.v_prob_b2 - 1)/6;
	x.v_prob_b3 = (x.v_prob_b3 - 1)/6;
}
/*
for(EVOwner x : EVOwners){
	hs_data_norms_pop1.add(x.v_norms);
	hs_data_trust_pop1.add(x.v_trust);
	hs_data_rc_pop1.add(x.v_reputational_concern);
	hs_data_psi_pop1.add(x.v_perc_social_interdep);
}

hs_norms_pop1.updateData();
hs_trust_pop1.updateData();
hs_rc_pop1.updateData();
hs_psi_pop1.updateData();
*/
/*ALCODEEND*/}

double f_setHSUtilStart()
{/*ALCODESTART::1753188219685*/
for(EVOwner x : EVOwners){
	hs_data_utility_b1_start.add(x.utility_b1);
	hs_data_utility_b2_start.add(x.utility_b2);
	hs_data_utility_b3_start.add(x.utility_b3);
}
hs_utility_b1_start.updateData();
hs_utility_b2_start.updateData();
hs_utility_b3_start.updateData();
/*ALCODEEND*/}

double f_setHSUtilEnd()
{/*ALCODESTART::1753188241182*/
for(EVOwner x : EVOwners){
	hs_data_utility_b1_end.add(x.utility_b1);
	hs_data_utility_b2_end.add(x.utility_b2);
	hs_data_utility_b3_end.add(x.utility_b3);
	
	hs_data_b1_end.add(x.b1);
	hs_data_b2_end.add(x.b2);
	hs_data_b3_end.add(x.b3);
}
hs_utility_b1_end.updateData();
hs_utility_b2_end.updateData();
hs_utility_b3_end.updateData();
hs_b1_end.updateData();
hs_b2_end.updateData();
hs_b3_end.updateData();
/*ALCODEEND*/}

double f_countBehavioursPerDay()
{/*ALCODESTART::1754483086805*/
double interactions_b1 = 0.0;
double interactions_b2 = 0.0;
double interactions_b3 = 0.0;
double rechecking = 0.0;

double outOfModelCharge_kWhperDay = 0.0;
int count_leftWhileChargingDay = 0;
int count_leftUnchargedDay = 0;

int dayIndex = v_day-1;

if(v_day == 1){
	interactions_b1 = count_b1_successful + count_b1_notSuccessful;
	interactions_b2 = count_b2_successful + count_b2_notSuccessful;
	interactions_b3 = count_b3_successful + count_b3_notSuccessful;
	rechecking = count_successfulRechecks + count_unsuccessfulRechecks;
	
	outOfModelCharge_kWhperDay = outOfModelCharge_kWh;
	count_leftWhileChargingDay = count_leftWhileCharging;
	count_leftUnchargedDay = count_leftUncharged;
}


else{
	//Get previous total
	double sum_prev_b1 = 0.0;
	double sum_prev_b2 = 0.0;
	double sum_prev_b3 = 0.0;
	double sum_prev_rechecks = 0.0;
	
	double sum_prev_OoMC = 0.0;
	int sum_prev_LWC = 0;
	int sum_prev_LU = 0;
	
	for (int i = 0; i < dayIndex; i++) {
	    sum_prev_b1 += ar_interactionsPerDay_b1[i];
	    sum_prev_b2 += ar_interactionsPerDay_b2[i];
	    sum_prev_b3 += ar_interactionsPerDay_b3[i];
	    sum_prev_rechecks += ar_rechecksPerDay[i];
	    
	    sum_prev_OoMC += ar_outOfModelCharging[i];
	    sum_prev_LWC += ar_leftWhileCharging[i];
	    sum_prev_LU += ar_leftUncharged[i];
	}	

	interactions_b1 = count_b1_successful + count_b1_notSuccessful - sum_prev_b1;
	interactions_b2 = count_b2_successful + count_b2_notSuccessful - sum_prev_b2;
	interactions_b3 = count_b3_successful + count_b3_notSuccessful - sum_prev_b3;
	rechecking = count_successfulRechecks + count_unsuccessfulRechecks - sum_prev_rechecks;

	outOfModelCharge_kWhperDay = outOfModelCharge_kWh - sum_prev_OoMC;
	count_leftWhileChargingDay = count_leftWhileCharging - sum_prev_LWC; 
	count_leftUnchargedDay = count_leftUncharged - sum_prev_LU;
}

ar_interactionsPerDay_b1[dayIndex] = interactions_b1;
ar_interactionsPerDay_b2[dayIndex] = interactions_b2;
ar_interactionsPerDay_b3[dayIndex] = interactions_b3;
ar_rechecksPerDay[dayIndex] = rechecking;

ar_outOfModelCharging[dayIndex] = outOfModelCharge_kWhperDay;
ar_leftWhileCharging[dayIndex] = count_leftWhileChargingDay;
ar_leftUncharged[dayIndex] = count_leftUnchargedDay;


/*ALCODEEND*/}

double f_cleanUp()
{/*ALCODESTART::1754493563613*/
c_carOwners.clear();
c_chargePoints.clear();
c_norms.clear();
c_trust.clear();
c_rc.clear();
c_psi.clear();
c_b1.clear();
c_b2.clear();
c_b3.clear();

/*
data_avgProbability_b1.reset();
data_avgProbability_b2.reset();
data_avgProbability_b3.reset();
data_carsOnTrip.reset();

data_ICECarsParkedNonCP.reset();
data_EVsParkedAtCPCharging.reset();
data_EVsParkedAtCPIdle.reset();
data_EVsParkedNonCPChargingNotRequired.reset();
data_EVsParkedNonCPChargingRequired.reset();

data_CPOccupied.reset();
data_CPAvailable.reset();

data_interactionsPerDay_b1.reset();
data_interactionsPerDay_b2.reset();
data_interactionsPerDay_b3.reset();
data_rechecksPerDay.reset();

data_notSuccessful_b1.reset();
data_notSuccessful_b2.reset();
data_notSuccessful_b3.reset();
data_successful_b1.reset();
data_successful_b2.reset();
data_successful_b3.reset();
data_successRate_b1.reset();
data_successRate_b2.reset();
data_successRate_b3.reset();
data_successRate_rechecks.reset();

hs_data_b1_all.reset();
hs_data_b1_data.reset();
hs_data_b1_end.reset();
hs_data_b1_pop.reset();
hs_data_b1_start.reset();
hs_data_b2_all.reset();
hs_data_b2_data.reset();
hs_data_b2_end.reset();
hs_data_b2_pop.reset();
hs_data_b2_start.reset();
hs_data_b3_all.reset();
hs_data_b3_data.reset();
hs_data_b3_end.reset();
hs_data_b3_pop.reset();
hs_data_b3_start.reset();
hs_data_norms_data.reset();
hs_data_norms_pop.reset();
hs_data_norms_pop1.reset();
hs_data_trust_data.reset();
hs_data_trust_pop.reset();
hs_data_trust_pop1.reset();
hs_data_psi_data.reset();
hs_data_psi_pop.reset();
hs_data_psi_pop1.reset();
hs_data_rc_data.reset();
hs_data_rc_pop.reset();
hs_data_rc_pop1.reset();
hs_data_utility_b1_all.reset();
hs_data_utility_b1_end.reset();
hs_data_utility_b1_start.reset();
hs_data_utility_b2_all.reset();
hs_data_utility_b2_end.reset();
hs_data_utility_b2_start.reset();
hs_data_utility_b3_all.reset();
hs_data_utility_b3_end.reset();
hs_data_utility_b3_start.reset();
data_noIdleChargers_b2.reset();

data_avgProbability_b1 = null;
data_avgProbability_b2 = null;
data_avgProbability_b3 = null;
data_carsOnTrip = null;

data_ICECarsParkedNonCP = null;
data_EVsParkedAtCPCharging = null;
data_EVsParkedAtCPIdle = null;
data_EVsParkedNonCPChargingNotRequired = null;
data_EVsParkedNonCPChargingRequired = null;

data_CPOccupied = null;
data_CPAvailable = null;

data_interactionsPerDay_b1 = null;
data_interactionsPerDay_b2 = null;
data_interactionsPerDay_b3 = null;
data_rechecksPerDay = null;

data_notSuccessful_b1 = null;
data_notSuccessful_b2 = null;
data_notSuccessful_b3 = null;
data_successful_b1 = null;
data_successful_b2 = null;
data_successful_b3 = null;
data_successRate_b1 = null;
data_successRate_b2 = null;
data_successRate_b3 = null;
data_successRate_rechecks = null;

hs_data_b1_all = null;
hs_data_b1_data = null;
hs_data_b1_end = null;
hs_data_b1_pop = null;
hs_data_b1_start = null;
hs_data_b2_all = null;
hs_data_b2_data = null;
hs_data_b2_end = null;
hs_data_b2_pop = null;
hs_data_b2_start = null;
hs_data_b3_all = null;
hs_data_b3_data = null;
hs_data_b3_end = null;
hs_data_b3_pop = null;
hs_data_b3_start = null;
hs_data_norms_data = null;
hs_data_norms_pop = null;
hs_data_norms_pop1 = null;
hs_data_trust_data = null;
hs_data_trust_pop = null;
hs_data_trust_pop1 = null;
hs_data_psi_data = null;
hs_data_psi_pop = null;
hs_data_psi_pop1 = null;
hs_data_rc_data = null;
hs_data_rc_pop = null;
hs_data_rc_pop1 = null;
hs_data_utility_b1_all = null;
hs_data_utility_b1_end = null;
hs_data_utility_b1_start = null;
hs_data_utility_b2_all = null;
hs_data_utility_b2_end = null;
hs_data_utility_b2_start = null;
hs_data_utility_b3_all = null;
hs_data_utility_b3_end = null;
hs_data_utility_b3_start = null;
data_noIdleChargers_b2 = null;


hs_b1_all.removeAll();
hs_b1_data.removeAll();
hs_b1_end.removeAll();
hs_b1_pop.removeAll();
hs_b1_start.removeAll();
hs_b2_all.removeAll();
hs_b2_data.removeAll();
hs_b2_end.removeAll();
hs_b2_pop.removeAll();
hs_b2_start.removeAll();
hs_b3_all.removeAll();
hs_b3_data.removeAll();
hs_b3_end.removeAll();
hs_b3_pop.removeAll();
hs_norms_data.removeAll();
hs_norms_pop.removeAll();
hs_norms_pop1.removeAll();
hs_trust_data.removeAll();
hs_trust_pop.removeAll();
hs_trust_pop1.removeAll();
hs_rc_data.removeAll();
hs_rc_pop.removeAll();
hs_rc_pop1.removeAll();
hs_psi_data.removeAll();
hs_psi_pop.removeAll();
hs_psi_pop1.removeAll();
hs_utility_b1_all.removeAll();
hs_utility_b1_end.removeAll();
hs_utility_b1_start.removeAll();
hs_utility_b2_all.removeAll();
hs_utility_b2_end.removeAll();
hs_utility_b2_start.removeAll();
hs_utility_b3_all.removeAll();
hs_utility_b3_end.removeAll();
hs_utility_b3_start.removeAll();
*/

// Interactions & rechecks
ar_interactionsPerDay_b1 = null;
ar_interactionsPerDay_b2 = null;
ar_interactionsPerDay_b3 = null;
ar_rechecksPerDay = null;

// Cars on trip & parking states
ar_carsOnTrip = null;
ar_ICECarsParkedNonCP = null;
ar_EVsParkedNonCPChargingRequired = null;
ar_EVsParkedNonCPChargingNotRequired = null;
ar_EVsParkedAtCPCharging = null;
ar_EVsParkedAtCPIdle = null;

// Charge points
ar_CPOccupied = null;
ar_CPAvailable = null;

// Socio-psychological data
ar_avgNorms = null;
ar_avgTrust = null;
ar_avgPSI = null;

// Success rates
ar_successRate_b1 = null;
ar_successRate_b2 = null;
ar_successRate_b3 = null;
ar_successRate_rechecks = null;

// Probabilities
ar_avgProbability_b1 = null;
ar_avgProbability_b2 = null;
ar_avgProbability_b3 = null;

// Successful & unsuccessful counts
ar_successful_b1 = null;
ar_successful_b2 = null;
ar_successful_b3 = null;
ar_unsuccessful_b1 = null;
ar_unsuccessful_b2 = null;
ar_unsuccessful_b3 = null;

// Special cases
ar_noIdleChargers_b2 = null;

pl_interactionPerDay.removeAll();
pl_probability.removeAll();
pl_successRate.removeAll();
ch_b1.removeAll();
ch_b2.removeAll();
ch_b3.removeAll();
ch_countCPs.removeAll();
ch_countEVs.removeAll();

for (int i = ICECarOwners.size() - 1; i >= 0; i--) {
	ICECarOwners.get(i).f_cleanUp();
}
for (int i = EVOwners.size() - 1; i >= 0; i--) {
    EVOwners.get(i).f_cleanUp();
}

this.deleteSelf();
/*ALCODEEND*/}

boolean f_checkCPA()
{/*ALCODESTART::1754579781080*/

int occupiedCount = (int) c_chargePoints.stream().filter(cp -> cp.isOccupied()).count();
int pointerCount  = (int) EVOwners.stream().filter(ev -> ev.v_chargePoint != null).count();
if (occupiedCount != pointerCount) {
    traceln("CPA mismatch: CP occupied = " + occupiedCount + ", EV pointers = " + pointerCount);
}


/*ALCODEEND*/}

double f_setChargePoints()
{/*ALCODESTART::1754903323970*/
for(int i=0; i<p_chargePoints; i++){
	J_ChargePoint cp = new J_ChargePoint();
	c_chargePoints.add(cp);
}
/*ALCODEEND*/}

double f_simulateTimestep()
{/*ALCODESTART::1754912834653*/
//Update minute of week to match database
double minutesPerTimestep = p_timestep_minutes;
double timestepStartMinuteOfWeek = (v_timestep * minutesPerTimestep) % (7 * 24 * 60);

v_withinSocialChargingTimes = f_setWithinSocialChargingTime();

//1. Charging progress, add charge over this time step if it is connected
for(EVOwner ev : EVOwners){
	ev.f_chargeCar();
}

//2. Cars leaving on new trips and releasing charge points
for(EVOwner ev : EVOwners){
	ev.f_goOnTrip(timestepStartMinuteOfWeek, minutesPerTimestep);
}

//3. Arrive from trip
for(EVOwner ev : EVOwners){
	ev.f_arriveFromTrip(timestepStartMinuteOfWeek, minutesPerTimestep);
}

//4. Connect to charger if required and available
for(EVOwner ev : EVOwners){
	ev.f_setChargingStatus();
}

//5. Try and aquire if waiting for charge point
for (EVOwner ev : EVOwners) {
	ev.f_recheckChargePoints();
}

//5. Other vehicles	
for(CarOwner ice : ICECarOwners){
	ice.f_updateTripStatus(timestepStartMinuteOfWeek, minutesPerTimestep);
}

//6. Count totals
if(!initializationMode){
	f_countTotals();
	/*
	if(v_timestep == 0){
		f_setHSUtilStart();
	}
	if(v_timestep == p_nbOfTimesteps - 1){
		f_setHSUtilEnd();
	}*/
}
f_checkCPA();

//7. Increment timestep
double prevHourOfDay = v_hourOfDay;
v_timestep++;
v_hourOfDay = (v_timestep * p_timestep_minutes / 60.0) % 24;

if(!initializationMode){
	// Check if day ended (hour wrapped around)
	if (v_hourOfDay < prevHourOfDay) {
		v_day++;
    	f_countBehavioursPerDay();
	}
}


/*ALCODEEND*/}

boolean f_setWithinSocialChargingTime()
{/*ALCODESTART::1754991946385*/
if(v_socialChargingOnlyDaytime){
	if(v_hourOfDay >= v_socialChargingStartHour && v_hourOfDay < v_socialChargingEndHour){
		return true;
	}
	else {
		return false;
	}
}
else {
	return true; //Set to always true so has no effect if not within daytime
}
/*ALCODEEND*/}

int f_getPartOfDay()
{/*ALCODESTART::1754997751929*/
if (v_hourOfDay >= 6 && v_hourOfDay < 12) return 0; // Morning
if (v_hourOfDay >= 12 && v_hourOfDay < 18) return 1; // Afternoon
return 2; // Evening


/*ALCODEEND*/}

double f_clearCounts()
{/*ALCODESTART::1755005247461*/
for(EVOwner ev : EVOwners){
	ev.count_b1_notSuccessful = 0;
	ev.count_b1_successful = 0;
	ev.count_b2_noIdleChargers = 0;
	ev.count_b2_noMatchingRequest = 0;
	ev.count_b2_notSuccessful = 0;
	ev.count_b2_successful = 0;
	ev.count_b3_notSuccessful = 0;
	ev.count_b3_successful = 0;
	ev.count_chargingRequired = 0;
	ev.count_chargingSessions = 0;
	ev.count_chargingSessions = 0;
	ev.count_fulfilledMoveRequest = 0;
	ev.count_successfulRechecks = 0;
	ev.count_unsuccessfulRechecks = 0;
	ev.count_leftUncharged = 0;
	ev.count_leftWhileCharging = 0;
	ev.v_outOfModelCharge_kWh = 0.0;
	ev.v_totalElectricityCharged_kWh = 0.0;
	ev.v_km_driven = 0.0;
}
for(CarOwner ice : ICECarOwners){
	ice.v_km_driven = 0.0;
}
countTripDepartures = 0;
countTripArrivals = 0;
/*ALCODEEND*/}

double f_setArrays()
{/*ALCODESTART::1755074064798*/
// Interactions & rechecks
ar_interactionsPerDay_b1 = new double[p_days];
ar_interactionsPerDay_b2 = new double[p_days];
ar_interactionsPerDay_b3 = new double[p_days];
ar_rechecksPerDay = new double[p_days];

// Cars on trip & parking states
ar_carsOnTrip = new double[p_nbOfTimesteps];
ar_ICECarsParkedNonCP = new double[p_nbOfTimesteps];
ar_EVsParkedNonCPChargingRequired = new double[p_nbOfTimesteps];
ar_EVsParkedNonCPChargingNotRequired = new double[p_nbOfTimesteps];
ar_EVsParkedAtCPCharging = new double[p_nbOfTimesteps];
ar_EVsParkedAtCPIdle = new double[p_nbOfTimesteps];

// Charge points
ar_CPOccupied = new double[p_nbOfTimesteps];
ar_CPAvailable = new double[p_nbOfTimesteps];

// Socio-psychological data
ar_avgNorms = new double[p_nbOfTimesteps];
ar_avgTrust = new double[p_nbOfTimesteps];
ar_avgPSI = new double[p_nbOfTimesteps];

// Success rates
ar_successRate_b1 = new double[p_nbOfTimesteps];
ar_successRate_b2 = new double[p_nbOfTimesteps];
ar_successRate_b3 = new double[p_nbOfTimesteps];
ar_successRate_rechecks = new double[p_nbOfTimesteps];

// Probabilities
ar_avgProbability_b1 = new double[p_nbOfTimesteps];
ar_avgProbability_b2 = new double[p_nbOfTimesteps];
ar_avgProbability_b3 = new double[p_nbOfTimesteps];

// Successful & unsuccessful counts
ar_successful_b1 = new double[p_nbOfTimesteps];
ar_successful_b2 = new double[p_nbOfTimesteps];
ar_successful_b3 = new double[p_nbOfTimesteps];
ar_unsuccessful_b1 = new double[p_nbOfTimesteps];
ar_unsuccessful_b2 = new double[p_nbOfTimesteps];
ar_unsuccessful_b3 = new double[p_nbOfTimesteps];

// Special cases
ar_noIdleChargers_b2 = new double[p_nbOfTimesteps];

//out of model and uncharged
ar_outOfModelCharging = new double[p_days];
ar_leftWhileCharging = new double[p_days];
ar_leftUncharged = new double[p_days];

/*ALCODEEND*/}

DataSet f_arrayToDataSet(double[] arr)
{/*ALCODESTART::1755167089452*/
DataSet ds = new DataSet(arr.length);
for (int i = 0; i < arr.length; i++) {
	ds.add(i, arr[i]);
}
return ds;
/*ALCODEEND*/}

double f_setHistogramArrays(int nbOfAgents)
{/*ALCODESTART::1755167696732*/
//Histograms
double[] ar_hs_data_norms_pop1 = new double[nbOfAgents]; 
double[] ar_hs_data_trust_pop1 = new double[nbOfAgents];
double[] ar_hs_data_psi_pop1 = new double[nbOfAgents];

double[] ar_hs_data_utility_b1_start = new double[nbOfAgents];
double[] ar_hs_data_utility_b2_start = new double[nbOfAgents];
double[] ar_hs_data_utility_b3_start = new double[nbOfAgents];

double[] ar_hs_data_utility_b1_end = new double[nbOfAgents];
double[] ar_hs_data_utility_b2_end = new double[nbOfAgents];
double[] ar_hs_data_utility_b3_end = new double[nbOfAgents];

double[] ar_hs_data_utility_b1_all = new double[nbOfAgents];
double[] ar_hs_data_utility_b2_all = new double[nbOfAgents];
double[] ar_hs_data_utility_b3_all = new double[nbOfAgents];

double[] ar_hs_data_b1_start = new double[nbOfAgents];
double[] ar_hs_data_b2_start = new double[nbOfAgents];
double[] ar_hs_data_b3_start = new double[nbOfAgents];

double[] ar_hs_data_b1_end = new double[nbOfAgents];
double[] ar_hs_data_b2_end = new double[nbOfAgents];
double[] ar_hs_data_b3_end = new double[nbOfAgents];

double[] ar_hs_data_b1_all = new double[nbOfAgents];
double[] ar_hs_data_b2_all = new double[nbOfAgents];
double[] ar_hs_data_b3_all = new double[nbOfAgents];


/*ALCODEEND*/}

double f_setHistograms()
{/*ALCODESTART::1755168571342*/
int nIntervals = 20;
double initialIntervalWidth = 0.35;
boolean calcCDF = true;
boolean calcPercentiles = false;
double lowPercent = 10;
double highPercent = 10;

HistogramSmartData hs_data = new HistogramSmartData(nIntervals, initialIntervalWidth, calcCDF, calcPercentiles, lowPercent, highPercent);
for(double value : c_norms){
	hs_data.add(value);
}

/*
hs_utility_b1_all.updateData();
hs_utility_b2_all.updateData();
hs_utility_b3_all.updateData();
hs_b1_all.updateData();
hs_b2_all.updateData();
hs_b3_all.updateData();
*/
/*ALCODEEND*/}

