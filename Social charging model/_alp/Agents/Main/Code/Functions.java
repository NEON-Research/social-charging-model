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
v_parkingPlacesAvailable = 100;
v_hourOfDay = 0;

ef_spvars.readFile();

f_getRegressionCoefficients();
f_setModelEffects();
//f_setThresholds();

//Get nb of trips in database
nbOfInputTrips = (int) selectFirstValue(int.class,
	"SELECT COUNT (*) FROM vehicle_trips;"
);
//Create ICEs and EVs
f_createICECarOwners();
f_generateSyntehticPopulationEVs();
f_normalizeFromLikert();

ef_spvars.close();
if (sortedRealData != null) {
    for (List<Double> innerList : sortedRealData) {
        innerList.clear();  // Clear contents of inner list
    }
    //sortedRealData.clear();  // Then clear the outer list
}

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
//COUNT CAR BEHAVIOR
double v_carsOnTrip = count(c_carOwners, x->x.v_status == ON_TRIP);
double v_ICECarsParkedNonCP = count(ICECarOwners, x->x.v_status == PARKED_NON_CHARGE_POINT_CHARGING_NOT_REQUIRED);
double v_EVsParkedNonCPChargingRequired = count(EVOwners, x->x.v_status == PARKED_NON_CHARGE_POINT_CHARGING_REQUIRED);
double v_EVsParkedNonCPChargingNotRequired = count(EVOwners, x->x.v_status == PARKED_NON_CHARGE_POINT_CHARGING_NOT_REQUIRED);
double v_EVsParkedAtCPCharging = count(EVOwners, x->x.v_status == PARKED_CHARGE_POINT_CHARGING);
double v_EVsParkedAtCPIdle = count(EVOwners, x->x.v_status == PARKED_CHARGE_POINT_IDLE);
double v_EVsArriving = count(EVOwners, x->x.v_status == ARRIVING);

if(v_EVsArriving > 0){
	traceln("Error: " + v_EVsArriving + " EVs arriving did not get a new stauts");
}
data_carsOnTrip.add(v_timestep, v_carsOnTrip);
data_ICECarsParkedNonCP.add(v_timestep, v_ICECarsParkedNonCP);
data_EVsParkedNonCPChargingRequired.add(v_timestep, v_EVsParkedNonCPChargingRequired);
data_EVsParkedNonCPChargingNotRequired.add(v_timestep, v_EVsParkedNonCPChargingNotRequired);
data_EVsParkedAtCPCharging.add(v_timestep, v_EVsParkedAtCPCharging);
data_EVsParkedAtCPIdle.add(v_timestep, v_EVsParkedAtCPIdle);

int cpAvailable = count(c_chargePoints, x->x.isOccupied() == false);
data_CPAvailable.add(v_timestep, cpAvailable);
data_CPOccupied.add(v_timestep, p_chargePoints - cpAvailable);
/*
if(v_chargePointAvailable < 0){
	traceln("Error: cp available = " + v_chargePointAvailable + " at timestep " + v_timestep);
}
if((p_chargePoints - v_chargePointAvailable) < 0){
	traceln("Error: cp occupied = " + (p_chargePoints - v_chargePointAvailable) + " at timestep " + v_timestep);
}*/

//SUCCES RATES and avg probabiliy
count_b1_successful = 0;
count_b2_successful = 0;
count_b3_successful = 0;
count_b1_notSuccessful = 0;
count_b2_notSuccessful = 0;
count_b3_notSuccessful = 0;
int count_b2_noIdleChargers = 0;

double totalProb_b1 = 0.0;
double totalProb_b2 = 0.0;
double totalProb_b3 = 0.0;

for (EVOwner x : EVOwners) {
    count_b1_successful      += x.count_b1_successful;
    count_b1_notSuccessful   += x.count_b1_notSuccessful;

    count_b2_successful      += x.count_b2_successful;
    count_b2_notSuccessful   += x.count_b2_notSuccessful;
    count_b2_noIdleChargers  += x.count_b2_noIdleChargers;

    count_b3_successful      += x.count_b3_successful;
    count_b3_notSuccessful   += x.count_b3_notSuccessful;
    
    totalProb_b1 += x.v_prob_b1;
	totalProb_b2 += x.v_prob_b2;
	totalProb_b3 += x.v_prob_b3;
}


int total_b1 = count_b1_successful + count_b1_notSuccessful;
successRate_b1 = (total_b1 != 0) ? ((double) count_b1_successful / total_b1) : 0;

int total_b2 = count_b2_successful + count_b2_notSuccessful;
successRate_b2 = (total_b2 != 0) ? ((double) count_b2_successful / total_b2) : 0;

int total_b3 = count_b3_successful + count_b3_notSuccessful;
successRate_b3 = (total_b3 != 0) ? ((double) count_b3_successful / total_b3) : 0;



data_successRate_b1.add(v_timestep, successRate_b1);
data_successful_b1.add(v_timestep, count_b1_successful);
data_notSuccessful_b1.add(v_timestep, count_b1_notSuccessful);

data_successRate_b2.add(v_timestep, successRate_b2);
data_successful_b2.add(v_timestep, count_b2_successful);
//Added no idle chargers category
count_b2_notSuccessful -= count_b2_noIdleChargers;
data_notSuccessful_b2.add(v_timestep, count_b2_notSuccessful);
data_noIdleChargers_b2.add(v_timestep, count_b2_noIdleChargers);

data_successRate_b3.add(v_timestep, successRate_b3);
data_successful_b3.add(v_timestep, count_b3_successful);
data_notSuccessful_b3.add(v_timestep, count_b3_notSuccessful);

int EVs = EVOwners.size();
double avgProb_b1 = totalProb_b1 / EVs;
double avgProb_b2 = totalProb_b2 / EVs;
double avgProb_b3 = totalProb_b3 / EVs;

data_avgProbability_b1.add(v_timestep, avgProb_b1);
data_avgProbability_b2.add(v_timestep, avgProb_b2);
data_avgProbability_b3.add(v_timestep, avgProb_b3);
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
initializationMode = false;

//Trigger over timesteps
for(int i=0; i < nbOfTimesteps; i++){
	f_simulateTimestep();
}


f_endSimulationPeriod();
traceln("Simulation finished after " + v_timestep + " timesteps ");

	
/*ALCODEEND*/}

double f_updateChart()
{/*ALCODESTART::1746089953946*/
ch_countEVs.removeAll();
ch_countEVs.addDataSet(data_carsOnTrip, "Cars on trip", coral);
ch_countEVs.addDataSet(data_ICECarsParkedNonCP, "ICE cars parked", darkKhaki);
ch_countEVs.addDataSet(data_EVsParkedNonCPChargingNotRequired, "EVs parked not at charge point charging not required", limeGreen);
ch_countEVs.addDataSet(data_EVsParkedNonCPChargingRequired, "EVs parked not at charge point charging required", slateGray);
ch_countEVs.addDataSet(data_EVsParkedAtCPCharging, "EVs parked at charge point and charging", darkMagenta);
ch_countEVs.addDataSet(data_EVsParkedAtCPIdle, "EVs parked at charge point and idle", orange);

ch_countCPs.removeAll();
ch_countCPs.addDataSet(data_CPAvailable, "Charge points available", limeGreen);
ch_countCPs.addDataSet(data_CPOccupied, "Charge points occupied", coral);

//Appearance app1 = new Appearance(blue, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);

pl_succesRate.removeAll();
pl_succesRate.addDataSet(data_successRate_b1, "Behavior 1: move car", sandyBrown, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_succesRate.addDataSet(data_successRate_b2, "Behavior 2: request move", lightSeaGreen, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_succesRate.addDataSet(data_successRate_b3, "Behavior 3: notify neighbor", lightSlateBlue, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);

pl_probability.removeAll();
pl_probability.addDataSet(data_avgProbability_b1, "Behavior 1: move car", sandyBrown, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_probability.addDataSet(data_avgProbability_b2, "Behavior 2: request move", lightSeaGreen, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_probability.addDataSet(data_avgProbability_b3, "Behavior 3: notify neighbor", lightSlateBlue, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);

pl_interactionPerDay.removeAll();
pl_interactionPerDay.addDataSet(data_interactionsPerDay_b1, "Behavior 1: move car", sandyBrown, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_interactionPerDay.addDataSet(data_interactionsPerDay_b2, "Behavior 2: request move", lightSeaGreen, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_interactionPerDay.addDataSet(data_interactionsPerDay_b3, "Behavior 3: notify neighbor", lightSlateBlue, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);


ch_b1.removeAll();
ch_b1.addDataSet(data_notSuccessful_b1, "Non successful interactions", red);
ch_b1.addDataSet(data_successful_b1, "Successful interactions", green);

ch_b2.removeAll();
ch_b2.addDataSet(data_notSuccessful_b2, "Non successful interactions", red);
ch_b2.addDataSet(data_noIdleChargers_b2, "No idle chargers", orange);
ch_b2.addDataSet(data_successful_b2, "Successful interactions", green);

ch_b3.removeAll();
ch_b3.addDataSet(data_notSuccessful_b3, "Non successful interactions", red);
ch_b3.addDataSet(data_successful_b3, "Successful interactions", green);

hs_utility_b1_all.updateData();
hs_utility_b2_all.updateData();
hs_utility_b3_all.updateData();
hs_b1_all.updateData();
hs_b2_all.updateData();
hs_b3_all.updateData();
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

traceln("Total charging sessions = " + countTotalChargingSessions + ", is avg per EV per day " + avgChargingSessionPerEVPerDay);
traceln("Unfinished charging = " + roundToInt((double) countUnfinishedCharging / countTotalChargingSessions * 100) + "% of charging sessions");
traceln("Left without charging = " + roundToInt((double) countLeftUncharged / countTotalRequiredCharging * 100) + "% of required charging sessions");
traceln("Out of model charging = " + roundToInt((double) outOfModelCharging/totalCharging * 100) + "% of total charging");
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

traceln("Finished initial week for division car location");
/*ALCODEEND*/}

double f_correlatedVariablesGenerator()
{/*ALCODESTART::1752581980940*/
//Get mean behavioral drivers
double mean_WtP = selectFrom(behavioral_variables)
	.where(behavioral_variables.variable.eq("willingness_to_participate"))
	.firstResult(behavioral_variables.mean_norm);
	
double mean_environmentalConcern = selectFrom(behavioral_variables)
	.where(behavioral_variables.variable.eq("environmental_concern"))
	.firstResult(behavioral_variables.mean_norm);
	
double mean_renewablesAttitude = selectFrom(behavioral_variables)
	.where(behavioral_variables.variable.eq("renewables_attitude"))
	.firstResult(behavioral_variables.mean_norm);
	
double mean_financialAttitude = selectFrom(behavioral_variables)
	.where(behavioral_variables.variable.eq("financial_attitude"))
	.firstResult(behavioral_variables.mean_norm);
/*	
double mean_attitude = selectFrom(behavioral_variables)
	.where(behavioral_variables.variable.eq("attitude"))
	.firstResult(behavioral_variables.mean_norm);
*/
double mean_awarenessEC = selectFrom(behavioral_variables)
	.where(behavioral_variables.variable.eq("awareness_EC"))
	.firstResult(behavioral_variables.mean_norm);

double mean_timeAvailability = selectFrom(behavioral_variables)
	.where(behavioral_variables.variable.eq("time_available"))
	.firstResult(behavioral_variables.mean_norm);

//Get sd behavioral drivers
double sd_WtP = selectFrom(behavioral_variables)
	.where(behavioral_variables.variable.eq("willingness_to_participate"))
	.firstResult(behavioral_variables.sd_norm);
	
double sd_environmentalConcern = selectFrom(behavioral_variables)
	.where(behavioral_variables.variable.eq("environmental_concern"))
	.firstResult(behavioral_variables.sd_norm);
	
double sd_renewablesAttitude = selectFrom(behavioral_variables)
	.where(behavioral_variables.variable.eq("renewables_attitude"))
	.firstResult(behavioral_variables.sd_norm);
	
double sd_financialAttitude = selectFrom(behavioral_variables)
	.where(behavioral_variables.variable.eq("financial_attitude"))
	.firstResult(behavioral_variables.sd_norm);
/*	
double sd_attitude = selectFrom(behavioral_variables)
	.where(behavioral_variables.variable.eq("attitude"))
	.firstResult(behavioral_variables.sd_norm);
*/	
double sd_awarenessEC = selectFrom(behavioral_variables)
	.where(behavioral_variables.variable.eq("awareness_EC"))
	.firstResult(behavioral_variables.sd_norm);

double sd_timeAvailability = selectFrom(behavioral_variables)
	.where(behavioral_variables.variable.eq("time_available"))
	.firstResult(behavioral_variables.sd_norm);


double mean1 = mean_environmentalConcern;
double mean2 = mean_awarenessEC;
double mean3 = mean_financialAttitude;
double mean4 = mean_timeAvailability;
double mean5 = mean_renewablesAttitude;

double sd1 = sd_environmentalConcern;
double sd2 = sd_awarenessEC;
double sd3 = sd_financialAttitude;
double sd4 = sd_timeAvailability;
double sd5 = sd_renewablesAttitude;

double rho12 = 0.4;
double rho13 = 0.23;	//conr
double rho14 = 0.4;		//conr
double rho15 = 0.4;		//conr
double rho23 = 0.4;
double rho24 = 0.4;
double rho25 = 0.4;
double rho34 = 0.4;
double rho35 = 0.26;	//conr
double rho45 = 0.4;

// Step 1: Covariance matrix
double[][] covarianceMatrix = {
		{sd1 * sd1, rho12 * sd1 * sd2, rho13 * sd1 * sd3, rho14 * sd1 * sd4, rho15 * sd1 * sd5},
		{rho12 * sd1 * sd2, sd2 * sd2, rho23 * sd2 * sd3, rho24 * sd2 * sd4, rho25 * sd2 * sd5},
		{rho13 * sd1 * sd3, rho23 * sd2 * sd3, sd3 * sd3, rho34 * sd3 * sd4, rho35 * sd3 * sd5},
		{rho14 * sd1 * sd4, rho24 * sd2 * sd4, rho34 * sd3 * sd4, sd4 * sd4, rho45 * sd4 * sd5},
		{rho15 * sd1 * sd5, rho25 * sd2 * sd5, rho35 * sd3 * sd5, rho45 * sd4 * sd5, sd5 * sd5},
};

// Step 2: Cholesky decomposition
//RealMatrix matrix2 = new Array2DRowRealMatrix(covarianceMatrix);
RealMatrix matrix = MatrixUtils.createRealMatrix(covarianceMatrix);
CholeskyDecomposition choleskyDecomposition = new CholeskyDecomposition(matrix);
RealMatrix L = choleskyDecomposition.getL();

for(J_Household x : c_households){
	f_setCorrelatedVariablesHousehold(x, L, mean1, mean2, mean3, mean4, mean5);
}

//To normalize values
double min_aoc = min(c_households, x->x.awarenessOfConsequence);
double min_att = min(c_households, x->x.attitude);
double min_ta = min(c_households, x->x.timeAvailability);
double min_pbc = min(c_households, x->x.pbc);

double max_aoc = max(c_households, x->x.awarenessOfConsequence);
double max_att = max(c_households, x->x.attitude);
double max_ta = max(c_households, x->x.timeAvailability);
double max_pbc = max(c_households, x->x.pbc);

double diff_aoc = max_aoc - min_aoc;
double diff_att = max_att - min_att;
double diff_ta = max_ta - min_ta;
double diff_pbc = max_pbc - min_pbc;
for(J_Household x : c_households){
	x.awarenessOfConsequence = (x.awarenessOfConsequence - min_aoc) / diff_aoc;
	x.attitude = (x.attitude - min_att) / diff_att;
	x.timeAvailability = (x.timeAvailability - min_ta) / diff_ta;
	x.pbc = (x.pbc - min_pbc) / diff_pbc;
	/*
	if(x.id < 100){
		traceln("Correlated input params " + x.awarenessOfConsequence + " AoC " + x.attitude + " att. " + x.timeAvailability + " ta " + x.pbc + " pbc");
	}*/
}

/*ALCODEEND*/}

double f_setCorrelatedVariablesHousehold(J_Household household,RealMatrix L,double mean1,double mean2,double mean3,double mean4,double mean5)
{/*ALCODESTART::1752581980942*/
// Step 3: Generate independent standard normal variables
/*
double z1 = normal(0,1);
double z2 = normal(0,1);
double z3 = normal(0,1);
double z4 = normal(0,1);
double z5 = normal(0,1);
*/
Random random = new Random();  // Ensure this generates different values each time
double z1 = random.nextGaussian();
double z2 = random.nextGaussian();
double z3 = random.nextGaussian();
double z4 = random.nextGaussian();
double z5 = random.nextGaussian();

// Step 4: Apply Cholesky transformation
double Y1 = L.getEntry(0, 0) * z1;
double Y2 = L.getEntry(1, 0) * z1 + L.getEntry(1, 1) * z2;
double Y3 = L.getEntry(2, 0) * z1 + L.getEntry(2, 1) * z2 + L.getEntry(2, 2) * z3;
double Y4 = L.getEntry(3, 0) * z1 + L.getEntry(3, 1) * z2 + L.getEntry(3, 2) * z3 + L.getEntry(3, 3) * z4; 
double Y5 = L.getEntry(4, 0) * z1 + L.getEntry(4, 1) * z2 + L.getEntry(4, 2) * z3 + L.getEntry(4, 3) * z4 + L.getEntry(4, 4) * z5; 

// Step 5: Add means to get the final correlated variables
double X1 = Y1 + mean1;  // Adding mean of 10 to Y1
double X2 = Y2 + mean2;  // Adding mean of 20 to Y2
double X3 = Y3 + mean3;  // Adding mean of 30 to Y3
double X4 = Y4 + mean4;
double X5 = Y5 + mean5;

double[] correlatedInputParams = {X1, X2, X3, X4, X5};
/*
if(household.householdIndex < 100){
	//traceln("Correlated input params " + X1 + " ec " + X2 + " aew " + X3 + " fa " + X4 + " ta " + X5 + " ra ");
}
*/
household.f_initializeCharacteristics(correlatedInputParams);
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

double f_generateSyntehticPopulationEVs()
{/*ALCODESTART::1752581980946*/
//1. get correlation matrix
f_getCorrelationMatrixFromExcel();

//2. get sorter real values from data
f_getSortedSocialPsychologicalData();

//3. generate syntethic agents
int EVs = roundToInt(p_cars*p_shareEVs);
f_generateSyntheticAgents(EVs, sortedRealData);

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

double f_getSortedSocialPsychologicalData()
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

List<Double> norms = new ArrayList<>();
List<Double> trust = new ArrayList<>();
List<Double> rc = new ArrayList<>();
List<Double> psi = new ArrayList<>();
List<Double> b1 = new ArrayList<>();
List<Double> b2 = new ArrayList<>();
List<Double> b3 = new ArrayList<>();

for(int rowIndex = 2; rowIndex < size + 1; rowIndex++){
	norms.add(ef_spvars.getCellNumericValue(sheetName, rowIndex, col_norms));
	trust.add(ef_spvars.getCellNumericValue(sheetName, rowIndex, col_trust));
	rc.add(ef_spvars.getCellNumericValue(sheetName, rowIndex, col_rc));
	psi.add(ef_spvars.getCellNumericValue(sheetName, rowIndex, col_psi));
	b1.add(ef_spvars.getCellNumericValue(sheetName, rowIndex, col_b1));
	b2.add(ef_spvars.getCellNumericValue(sheetName, rowIndex, col_b2));
	b3.add(ef_spvars.getCellNumericValue(sheetName, rowIndex, col_b3));
}


Collections.sort(norms);
Collections.sort(trust);
Collections.sort(rc);
Collections.sort(psi);
Collections.sort(b1);
Collections.sort(b2);
Collections.sort(b3);

sortedRealData = Arrays.asList(norms, trust, rc, psi, b1, b2, b3);



//Add histograms to check
if(startup_agent.v_rapidRun == false){
	for(double value : norms){
		hs_data_norms_data.add(value);
	}
	for(double value : trust){
		hs_data_trust_data.add(value);
	}
	for(double value : rc){
		hs_data_rc_data.add(value);
	}
	for(double value : psi){
		hs_data_psi_data.add(value);
	}
	for(double value : b1){
		hs_data_b1_data.add(value);
	}
	for(double value : b2){
		hs_data_b2_data.add(value);
	}
	for(double value : b3){
		hs_data_b3_data.add(value);
	}
}
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
x.v_perceived_social_interdependence = agentAttributes[3];
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
for(EVOwner x : EVOwners){
	hs_data_norms_pop.add(x.v_norms);
	hs_data_trust_pop.add(x.v_trust);
	hs_data_rc_pop.add(x.v_reputational_concern);
	hs_data_psi_pop.add(x.v_perceived_social_interdependence);

	hs_data_b1_pop.add(x.v_prob_b1);
	hs_data_b2_pop.add(x.v_prob_b2);
	hs_data_b3_pop.add(x.v_prob_b3);
}

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
*/
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
c_variableWeights.put(key, value);
regCoef_norms_psi = value;

var1 = "trust";
key = var1+var2;
value = f_mediationResultsQuery(var1, var2);
c_variableWeights.put(key, value);
regCoef_trust_psi = value;

var1 = "reputational_concern";
key = var1+var2;
value = f_mediationResultsQuery(var1, var2);
c_variableWeights.put(key, value);
regCoef_rc_psi = value;

//PSI to behavior
var1 = "perceived_social_interdependence";
var2 = "b1_move_vehicle";
key = var1+var2;
value = f_mediationResultsQuery(var1, var2);
c_variableWeights.put(key, value);
regCoef_psi_b1 = value;

var2 = "b2_request_move";
key = var1+var2;
value = f_mediationResultsQuery(var1, var2);
c_variableWeights.put(key, value);
regCoef_psi_b2 = value;

var2 = "b3_notify_neighbor";
key = var1+var2;
value = f_mediationResultsQuery(var1, var2);
c_variableWeights.put(key, value);
regCoef_psi_b3 = value;

//Trust to behavior
var1 = "trust";
var2 = "b2_request_move";
key = var1+var2;
value = f_mediationResultsQuery(var1, var2);
c_variableWeights.put(key, value);
regCoef_trust_b2 = value;

var2 = "b3_notify_neighbor";
key = var1+var2;
value = f_mediationResultsQuery(var1, var2);
c_variableWeights.put(key, value);
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
	x.v_perceived_social_interdependence = (x.v_perceived_social_interdependence - 1)/6;
	x.v_prob_b1 = (x.v_prob_b1 - 1)/6;
	x.v_prob_b2 = (x.v_prob_b2 - 1)/6;
	x.v_prob_b3 = (x.v_prob_b3 - 1)/6;
}

for(EVOwner x : EVOwners){
	hs_data_norms_pop1.add(x.v_norms);
	hs_data_trust_pop1.add(x.v_trust);
	hs_data_rc_pop1.add(x.v_reputational_concern);
	hs_data_psi_pop1.add(x.v_perceived_social_interdependence);
}

hs_norms_pop1.updateData();
hs_trust_pop1.updateData();
hs_rc_pop1.updateData();
hs_psi_pop1.updateData();
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
int day = data_interactionsPerDay_b1.size() + 1;

double interactions_b1 = 0.0;
double interactions_b2 = 0.0;
double interactions_b3 = 0.0;


if(day == 1){
	interactions_b1 = count_b1_successful + count_b1_notSuccessful;
	interactions_b2 = count_b2_successful + count_b2_notSuccessful;
	interactions_b3 = count_b3_successful + count_b3_notSuccessful;
}


else{
	//Get previous total
	double sum_prev_b1 = 0.0;
	double sum_prev_b2 = 0.0;
	double sum_prev_b3 = 0.0;
	
	for (int i = 0; i < day - 1; i++) {
	    sum_prev_b1 += data_interactionsPerDay_b1.getY(i);
	    sum_prev_b2 += data_interactionsPerDay_b2.getY(i);
	    sum_prev_b3 += data_interactionsPerDay_b3.getY(i);
	}	

	interactions_b1 = count_b1_successful + count_b1_notSuccessful - sum_prev_b1;
	interactions_b2 = count_b2_successful + count_b2_notSuccessful - sum_prev_b2;
	interactions_b3 = count_b3_successful + count_b3_notSuccessful - sum_prev_b3;
}

data_interactionsPerDay_b1.add(day, interactions_b1);
data_interactionsPerDay_b2.add(day, interactions_b2);
data_interactionsPerDay_b3.add(day, interactions_b3);
/*ALCODEEND*/}

double f_cleanUp()
{/*ALCODESTART::1754493563613*/
c_carOwners.clear();
c_chargePoints.clear();
c_EVsParkedAtCPCharging.clear();
c_EVsParkedAtCPIdle.clear();
c_EVsParkedNonCPChargingNotRequired.clear();
c_EVsParkedNonCPChargingRequired.clear();
c_variableWeights.clear();

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

data_notSuccessful_b1.reset();
data_notSuccessful_b2.reset();
data_notSuccessful_b3.reset();
data_successful_b1.reset();
data_successful_b2.reset();
data_successful_b3.reset();
data_successRate_b1.reset();
data_successRate_b2.reset();
data_successRate_b3.reset();

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

data_notSuccessful_b1 = null;
data_notSuccessful_b2 = null;
data_notSuccessful_b3 = null;
data_successful_b1 = null;
data_successful_b2 = null;
data_successful_b3 = null;
data_successRate_b1 = null;
data_successRate_b2 = null;
data_successRate_b3 = null;

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

pl_interactionPerDay.removeAll();
pl_probability.removeAll();
pl_succesRate.removeAll();
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

traceln("Memory should be cleaned up");

this.deleteSelf();
/*ALCODEEND*/}

double f_endRun()
{/*ALCODESTART::1754550396944*/
//f_writeResultsToExcel(startup_agent.excel_exportResults);
startup_agent.simulationCount++;
if(startup_agent.simulationCount == 1){
	startup_agent.fileChooser_exportResults.setEnabled(true);
}
if(startup_agent.v_rapidRun == false){
	startup_agent.viewArea.navigateTo();
}
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
/*
//3. Release CP if fully charged (b2)
for(EVOwner ev : EVOwners){
	ev.f_realseChargePoint();
}*/

//4. Try and aquire if waiting for charge point
/*
for (EVOwner ev : EVOwners) {
	ev.tryAcquireChargePoint();
}*/

//5. Other vehicles	
/*for(CarOwner ice : ICECarOwners){
	ice.f_updateTripStatus(timestepStartMinuteOfWeek, minutesPerTimestep);
}*/

//6. Count totals
if(!initializationMode){
	f_countTotals();
	if(v_timestep == 0){
		f_setHSUtilStart();
	}
	if(v_timestep == p_nbOfTimesteps - 1){
		f_setHSUtilEnd();
	}
}
f_checkCPA();

//7. Increment timestep
double prevHourOfDay = v_hourOfDay;
v_timestep++;
v_hourOfDay = (v_timestep * p_timestep_minutes / 60.0) % 24;

if(!initializationMode){
	// Check if day ended (hour wrapped around)
	if (v_hourOfDay < prevHourOfDay) {
    	f_countBehavioursPerDay();
	}
}


/*ALCODEEND*/}

