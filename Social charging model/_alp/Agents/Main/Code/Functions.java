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


f_setArrays();

int evOwners = roundToInt(p_cars*p_shareEVs);
f_setHistogramArrays(evOwners);

//f_setModelEffects();
//f_setThresholds();

//Get nb of trips in database
nbOfInputTrips = (int) selectFirstValue(int.class,
	"SELECT COUNT (*) FROM vehicle_trips;"
);
//Create ICEs and EVs
f_createICECarOwners();
f_generateSyntehticPopulationEVs(evOwners);
/*
double trips = sum(EVOwners, x->x.c_tripSchedule.size());
double km = 0;
for(EVOwner ev : EVOwners){
	for(J_Trip trip : ev.c_tripSchedule){
		km += trip.getDistance_km();
	}
}
traceln("number of weekly trips in trip schedules " + trips + " and " + km + "km");
*/
//f_normalizeFromLikert();


/*
if (sortedRealData != null) {
    for (List<Double> innerList : sortedRealData) {
        innerList.clear();  // Clear contents of inner list
    }
    //sortedRealData.clear();  // Then clear the outer list
}*/

f_setChargePoints();
f_setInitialEVsToCP();
//f_simulateFirstWeekToGetInitialLocationCars();

//traceln(sum(EVOwners, x->x.tripIndex) + " total sum of trip index");
//traceln("has initialized model");
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
count_b2_noIdleChargers = 0;
count_b2_noMatchingRequests = 0;
count_b2_noProb = 0;

outOfModelCharge_kWh = 0.0;
count_leftWhileCharging = 0;
count_leftWhileChargingWithDelayedAccess = 0;
count_leftUncharged = 0;
count_requiredChargingSessions = 0;
count_chargingSessions = 0;

v_tripsFinished = 0;


double totalProb_b1 = 0.0;
double totalProb_b2 = 0.0;
double totalProb_b3 = 0.0;

double totalNorms = 0.0;
double totalRC = 0.0;
double totalPSI = 0.0;
double totalPCP = 0.0;

double totalNorm_b1 = 0.0;
double totalNorm_b2 = 0.0;
double totalNorm_b3 = 0.0;

for (EVOwner x : EVOwners) {
    count_b1_successful      += x.count_b1_successful;
    count_b1_notSuccessful   += x.count_b1_notSuccessful;

    count_b2_successful      += x.count_b2_successful;
    count_b2_notSuccessful   += x.count_b2_notSuccessful;
    count_b2_noIdleChargers  += x.count_b2_noIdleChargers;
    count_b2_noMatchingRequests += x.count_b2_noMatchingRequest;
    count_b2_noProb			 += x.count_b2_noProb;

    count_b3_successful      += x.count_b3_successful;
    count_b3_notSuccessful   += x.count_b3_notSuccessful;
    
    count_successfulRechecks += x.count_successfulRechecks;
    count_unsuccessfulRechecks += x.count_unsuccessfulRechecks;
    
    totalProb_b1 += x.v_prob_b1;
	totalProb_b2 += x.v_prob_b2;
	totalProb_b3 += x.v_prob_b3;
	
	totalNorms += x.v_norms;
	totalRC += x.v_reputational_concern;
	totalPSI += x.v_perc_social_interdep;
	totalPCP += x.v_perc_charging_pressure;
	
	totalNorm_b1 += x.v_norm_b1;
	totalNorm_b2 += x.v_norm_b2;
	totalNorm_b3 += x.v_norm_b3;
	
	outOfModelCharge_kWh += x.v_outOfModelCharge_kWh;
	count_leftWhileCharging += x.count_leftWhileCharging;
	count_leftWhileChargingWithDelayedAccess += x.count_leftWhileChargingWithDelayedAccess;
	count_leftUncharged += x.count_leftUncharged;
	
	count_requiredChargingSessions += x.count_chargingRequired;
	count_chargingSessions += x.count_chargingSessions;
	
	v_tripsFinished += x.v_tripFinished;
}

if (count_b2_noProb + count_b2_noIdleChargers + count_b2_noMatchingRequests != count_b2_notSuccessful) {
    traceln(
        "B2 counter mismatch at main, noProb=" + count_b2_noProb +
        ", noIdleChargers=" + count_b2_noIdleChargers +
        ", noMatchingRequest=" + count_b2_noMatchingRequests +
        ", notSuccessful=" + count_b2_notSuccessful +
        ", successful=" + count_b2_successful +
        ", total=" + (count_b2_successful + count_b2_notSuccessful)
    );
}


int total_b1 = count_b1_successful + count_b1_notSuccessful;
successRate_b1 = (total_b1 != 0) ? ((double) count_b1_successful / total_b1) : 0;

int total_b2 = count_b2_successful + count_b2_notSuccessful;
successRate_b2 = (total_b2 != 0) ? ((double) count_b2_successful / total_b2) : 0;

int total_b3 = count_b3_successful + count_b3_notSuccessful;
successRate_b3 = (total_b3 != 0) ? ((double) count_b3_successful / total_b3) : 0;

int total_rechecks = count_successfulRechecks + count_unsuccessfulRechecks;
successRate_rechecks = (total_rechecks != 0) ? ((double) count_successfulRechecks / total_rechecks) : 0;

//Probabilities
avgProb_b1 = totalProb_b1 / EVs;
avgProb_b2 = totalProb_b2 / EVs;
avgProb_b3 = totalProb_b3 / EVs;

//Norms, trust, PSI
double avgNorms = totalNorms / EVs;
double avgRC = totalRC / EVs;
double avgPSI = totalPSI / EVs;
v_avgPCP = totalPCP / EVs;

v_avgNorm_b1 = totalNorm_b1 / EVs;
v_avgNorm_b2 = totalNorm_b2 / EVs;
v_avgNorm_b3 = totalNorm_b3 / EVs;

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

//Once per day
if(v_timestep % 96 == 0){
//Norms, trust, PSI
	ar_avgNorms[v_day] = f_convertStandardizedToProb(avgNorms, mean_norms, sd_norms, true);
	ar_avgRC[v_day] = f_convertStandardizedToProb(avgRC, mean_rc, sd_rc, true);
	ar_avgPSI[v_day] = f_convertStandardizedToProb(avgPSI, mean_psi, sd_psi, true);
	ar_avgPCP[v_day] = f_convertStandardizedToProb(v_avgPCP, mean_pcp, sd_pcp, true);
	
	ar_avgNorm_b1[v_day] = f_convertStandardizedToProb(v_avgNorm_b1, mean_b1, sd_b1, true);
	ar_avgNorm_b2[v_day] = f_convertStandardizedToProb(v_avgNorm_b2, mean_b2, sd_b2, false);
	ar_avgNorm_b3[v_day] = f_convertStandardizedToProb(v_avgNorm_b3, mean_b3, sd_b3, false);
	
	//Success Rates
	ar_successRate_b1[v_day] = successRate_b1;
	ar_successRate_b2[v_day] = successRate_b2;
	ar_successRate_b3[v_day] = successRate_b3;
	ar_successRate_rechecks[v_day] = successRate_rechecks;
	
	//Probabilities
	ar_avgProbability_b1[v_day] = avgProb_b1;
	ar_avgProbability_b2[v_day] = avgProb_b2;
	ar_avgProbability_b3[v_day] = avgProb_b3;
	
	//Success/Not success
	/*
	ar_successful_b1[v_day] = count_b1_successful;
	ar_successful_b2[v_day] = count_b2_successful;
	ar_successful_b3[v_day] = count_b3_successful;
	ar_unsuccessful_b1[v_day] = count_b1_notSuccessful;
	ar_unsuccessful_b2[v_day] = count_b2_notSuccessful;
	ar_unsuccessful_b3[v_day] = count_b3_notSuccessful; 
	ar_unsuccesfulDueToProb_b2[v_day] = count_b2_noProb;
	ar_noIdleChargers_b2[v_day] = count_b2_noIdleChargers;
	ar_noMatchingRequests_b2[v_day] = count_b2_noMatchingRequests;*/
}
//if(ar_unsuccesfulDueToProb_b2[v_timestep] < 0){ traceln(ar_unsuccesfulDueToProb_b2[v_timestep] + " unssuccesful due to prob");}







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
v_week = 0;
initializationMode = false;

//traceln("Start simulation period");

//Trigger over timesteps
for(int i=0; i < nbOfTimesteps; i++){
	f_simulateTimestep();
}


f_endSimulationPeriod();
//traceln("End simulation period");
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
pl_socioPsychologicalLearning.addDataSet(f_arrayToDataSet(ar_avgRC), "Average RC", lightSeaGreen, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_socioPsychologicalLearning.addDataSet(f_arrayToDataSet(ar_avgPSI), "Average PSI", lightSlateBlue, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_socioPsychologicalLearning.addDataSet(f_arrayToDataSet(ar_avgPCP), "Average PCP", orchid, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_socioPsychologicalLearning.addDataSet(f_arrayToDataSet(ar_avgNorm_b1), "Average norm B1", indianRed, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_socioPsychologicalLearning.addDataSet(f_arrayToDataSet(ar_avgNorm_b2), "Average norm B2", khaki, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);
pl_socioPsychologicalLearning.addDataSet(f_arrayToDataSet(ar_avgNorm_b3), "Average norm B3", darkTurquoise, true, Chart.INTERPOLATION_LINEAR, 1.0, Chart.POINT_NONE);

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
ch_b2.addDataSet(f_arrayToDataSet(ar_unsuccesfulDueToProb_b2), "Non successful interactions", red);
ch_b2.addDataSet(f_arrayToDataSet(ar_noIdleChargers_b2), "No idle chargers", khaki);
ch_b2.addDataSet(f_arrayToDataSet(ar_noMatchingRequests_b2), "No matching requests", orange);
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
traceln("Total rcs = " + countTotalRequiredCharging + " total cs = " + countTotalChargingSessions);
traceln("Unfinished charging = " + roundToInt((double) countUnfinishedCharging / countTotalChargingSessions * 100) + "% of charging sessions");
traceln("Left without charging = " + roundToInt((double) countLeftUncharged / countTotalRequiredCharging * 100) + "% of required charging sessions");
traceln("Out of model charging = " + roundToInt((double) outOfModelCharging/totalCharging * 100) + "% of total charging");

traceln("Total km driven by EVs = " + sum(EVOwners, x->x.v_km_driven));
double sumTrips = 0;
for(int i=0; i < ar_tripsPerWeek.length; i++){
	sumTrips += ar_tripsPerWeek[i];
}
traceln("Total trips = " + sumTrips);*/

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
//f_getCorrelationMatrixFromExcel();
//traceln("has correlation matrix");

//2. get sorter real values from data
//List<List<Double>> sortedRealData = new ArrayList<>();
//sortedRealData = f_getSortedSocialPsychologicalData();
//traceln("has sorted reald data");

//3. generate syntethic agents
f_generateSyntheticAgents(evOwners, sortedRealData);
//traceln("has syntethic population");
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

avgProb_b1 = average(EVOwners, x->x.v_stand_prob_b1);
avgProb_b2 = average(EVOwners, x->x.v_stand_prob_b2);
avgProb_b3 = average(EVOwners, x->x.v_stand_prob_b3);
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
//x.v_trust = agentAttributes[1];
x.v_reputational_concern = agentAttributes[1];
x.v_perc_social_interdep = agentAttributes[2];
x.v_perc_charging_pressure = agentAttributes[3];
x.v_stand_prob_b1 = agentAttributes[4];
x.v_stand_prob_b2 = agentAttributes[5];
x.v_stand_prob_b3 = agentAttributes[5]; //prob_b3 = prob_b2
x.v_norm_b1 = agentAttributes[4];
x.v_norm_b2 = agentAttributes[5];
x.v_norm_b3 = agentAttributes[5];

x.v_prob_b1 = f_convertStandardizedToProb(x.v_stand_prob_b1, mean_b1, sd_b1, true);
x.v_prob_b2 = f_convertStandardizedToProb(x.v_stand_prob_b2, mean_b2, sd_b2, false);
x.v_prob_b3 = f_convertStandardizedToProb(x.v_stand_prob_b3, mean_b3, sd_b3, false);

x.v_type = EV;
f_initializeTrips(x);
    
int minBatteryCap_kWh = 50;
int maxBatteryCap_kWh = 80;
int meanBatteryCap_kWh = 65;
int spread = 5;
    
x.v_batteryCapacity_kWh = normal(minBatteryCap_kWh, maxBatteryCap_kWh, meanBatteryCap_kWh, spread);
//double randomShare = normal(0.25, 1, 0.7, 0.2); // normal distribution of energy in battery at start
double randomShare = uniform(0.25,1);
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

	hs_data_b1_pop.add(x.v_stand_prob_b1);
	hs_data_b2_pop.add(x.v_stand_prob_b2);
	hs_data_b3_pop.add(x.v_stand_prob_b3);
}

Color colorLowPercent = green;
Color colorHighPercent = green;
Color colorPDF = crimson;
Color colorCDF = green;
float lineWidthCDF = 1;
Color colorMean = green;

//hs_norms_data.addHistogram(hs_data_norms_pop, "Norms pop", colorLowPercent, colorHighPercent, colorPDF, colorCDF, lineWidthCDF, colorMean);
hs_b1_pop.addHistogram(hs_data_b1_pop, "b1 pop", colorLowPercent, colorHighPercent, colorPDF, colorCDF, lineWidthCDF, colorMean);
hs_b2_pop.addHistogram(hs_data_b2_pop, "b1 pop", colorLowPercent, colorHighPercent, colorPDF, colorCDF, lineWidthCDF, colorMean);
hs_b3_pop.addHistogram(hs_data_b3_pop, "b1 pop", colorLowPercent, colorHighPercent, colorPDF, colorCDF, lineWidthCDF, colorMean);
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
/*
if( regCoefFromData ){
	regCoef_norms_psi_b1 = regCoef_norms_psi_b1 / 6;
	regCoef_pcp_psi_b1 = regCoef_pcp_psi_b1 / 6;
	regCoef_rc_psi_b1 = regCoef_rc_psi_b1 / 6;
	
	regCoef_norms_psi_b2b3 = regCoef_norms_psi_b2b3 / 6;
	regCoef_pcp_psi_b2b3 = regCoef_pcp_psi_b2b3 / 6;
	regCoef_rc_psi_b2b3 = regCoef_rc_psi_b2b3 / 6;
}
*/
for(EVOwner x : EVOwners){
	x.v_norms = (x.v_norms - 1)/6;
	x.v_trust = (x.v_trust - 1)/6;
	x.v_reputational_concern = (x.v_reputational_concern - 1)/6;
	x.v_perc_social_interdep = (x.v_perc_social_interdep - 1)/6;
	x.v_perc_charging_pressure = (x.v_perc_charging_pressure - 1)/6;
	x.v_prob_b1 = (x.v_prob_b1 - 1)/6;
}

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
int count_leftWhileChargingWithDelayedAccessDay = 0;
int count_leftUnchargedDay = 0;
int count_requiredChargingSessionsDay = 0;
int count_chargingSessionsDay = 0;

int dayIndex = v_day-1;

if(v_day == 1){
	interactions_b1 = count_b1_successful + count_b1_notSuccessful;
	interactions_b2 = count_b2_successful + count_b2_notSuccessful;
	interactions_b3 = count_b3_successful + count_b3_notSuccessful;
	rechecking = count_successfulRechecks + count_unsuccessfulRechecks;
	
	outOfModelCharge_kWhperDay = outOfModelCharge_kWh;
	count_leftWhileChargingDay = count_leftWhileCharging;
	count_leftUnchargedDay = count_leftUncharged;
	count_leftWhileChargingWithDelayedAccessDay = count_leftWhileChargingWithDelayedAccess;
	count_requiredChargingSessionsDay = count_requiredChargingSessions;
	count_chargingSessionsDay = count_chargingSessions;
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
	int sum_prev_LWCWDA = 0;
	int sum_prev_RCD = 0;
	int sum_prev_CS = 0;
	
	for (int i = 0; i < dayIndex; i++) {
	    sum_prev_b1 += ar_interactionsPerDay_b1[i];
	    sum_prev_b2 += ar_interactionsPerDay_b2[i];
	    sum_prev_b3 += ar_interactionsPerDay_b3[i];
	    sum_prev_rechecks += ar_rechecksPerDay[i];
	    
	    sum_prev_OoMC += ar_outOfModelCharging[i];
	    sum_prev_LWC += ar_leftWhileCharging[i];
	    sum_prev_LU += ar_leftUncharged[i];
	    sum_prev_LWCWDA += ar_leftWhileChargingWithDelayedAccess[i];
	    sum_prev_RCD += ar_requiredChargingSessions[i];
	    sum_prev_CS += ar_chargingSessions[i];
	}	

	interactions_b1 = count_b1_successful + count_b1_notSuccessful - sum_prev_b1;
	interactions_b2 = count_b2_successful + count_b2_notSuccessful - sum_prev_b2;
	interactions_b3 = count_b3_successful + count_b3_notSuccessful - sum_prev_b3;
	rechecking = count_successfulRechecks + count_unsuccessfulRechecks - sum_prev_rechecks;

	outOfModelCharge_kWhperDay = outOfModelCharge_kWh - sum_prev_OoMC;
	count_leftWhileChargingDay = count_leftWhileCharging - sum_prev_LWC;
	count_leftWhileChargingWithDelayedAccessDay = count_leftWhileChargingWithDelayedAccess - sum_prev_LWCWDA;
	count_leftUnchargedDay = count_leftUncharged - sum_prev_LU;
	count_chargingSessionsDay = count_chargingSessions - sum_prev_CS;
	count_requiredChargingSessionsDay = count_requiredChargingSessions - sum_prev_RCD;
}

ar_interactionsPerDay_b1[dayIndex] = interactions_b1;
ar_interactionsPerDay_b2[dayIndex] = interactions_b2;
ar_interactionsPerDay_b3[dayIndex] = interactions_b3;
ar_rechecksPerDay[dayIndex] = rechecking;

ar_outOfModelCharging[dayIndex] = outOfModelCharge_kWhperDay;
ar_leftWhileCharging[dayIndex] = count_leftWhileChargingDay;
ar_leftWhileChargingWithDelayedAccess[dayIndex] = count_leftWhileChargingWithDelayedAccessDay;
ar_leftUncharged[dayIndex] = count_leftUnchargedDay;
ar_chargingSessions[dayIndex] = count_chargingSessionsDay;
ar_requiredChargingSessions[dayIndex] = count_requiredChargingSessionsDay;

//double percSatisfiedChargingSessions = (double) (count_leftUnchargedDay + count_leftWhileChargingWithDelayedAccessDay) / count_requiredChargingSessionsDay;
double percSatisfiedChargingSessions;

if (count_requiredChargingSessionsDay == 0) {
    percSatisfiedChargingSessions = 1.0;
} else {
    percSatisfiedChargingSessions = (double) count_chargingSessionsDay / count_requiredChargingSessionsDay;
}
ar_percSatisfiedChargingSessions[dayIndex] = percSatisfiedChargingSessions;
/*ALCODEEND*/}

double f_cleanUp()
{/*ALCODESTART::1754493563613*/
c_carOwners.clear();
c_chargePoints.clear();
/*
c_norms.clear();
c_trust.clear();
c_rc.clear();
c_psi.clear();
c_b1.clear();
c_b2.clear();
c_b3.clear();


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
ar_avgRC = null;
ar_avgPSI = null;
ar_avgPCP = null;

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
ar_noMatchingRequests_b2 = null;
ar_unsuccesfulDueToProb_b2 = null;

ar_kmDrivenPerWeek = null;
ar_avgNorm_b1 = null;
ar_avgNorm_b2 = null;
ar_avgNorm_b3 = null;

ar_interactionsPerWeek_b1 = null;
ar_interactionsPerWeek_b2 = null;
ar_interactionsPerWeek_b3 = null;
ar_rechecksPerWeek = null;

ar_outOfModelCharging = null;
ar_leftWhileCharging = null;
ar_leftUncharged = null;
ar_leftWhileChargingWithDelayedAccess = null;

ar_chargingSessions = null;
ar_requiredChargingSessions = null;
ar_percSatisfiedChargingSessions = null;

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
int timestepStartMinuteInWeek = (v_timestep * p_timestep_minutes) % (7 * 24 * 60); //range between 0-10080 minutes in aweek
int timestepEndMinuteInWeek = timestepStartMinuteInWeek + p_timestep_minutes;	


v_withinSocialChargingTimes = f_setWithinSocialChargingTime();

//1. Charging progress, add charge over this time step if it is connected
for(EVOwner ev : EVOwners){
	ev.f_chargeCar();
}

//2. Cars leaving on new trips and releasing charge points
for(EVOwner ev : EVOwners){
	if( ev.f_departureInCurrentTimestep(timestepStartMinuteInWeek, timestepEndMinuteInWeek)){
		ev.f_goOnTrip();
	}
}

//3. Arrive from trip
for(EVOwner ev : EVOwners){
	ev.f_arriveFromTrip(timestepStartMinuteInWeek, timestepEndMinuteInWeek);
}

//4. Connect to charger if required and available
for(EVOwner ev : EVOwners){
	ev.f_setChargingStatus();
}

//5. Extended b1 - when full in night
for (EVOwner ev : EVOwners){
	ev.f_extendedB1();
}

//5. Try and aquire if waiting for charge point
for (EVOwner ev : EVOwners) {
	ev.f_recheckChargePoints();
}

//5. Other vehicles	
for(CarOwner ice : ICECarOwners){
	ice.f_updateICETripStatus(timestepStartMinuteInWeek, timestepEndMinuteInWeek);
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
    	//f_countBehavioursPerDay();
    	if( v_day % 7 == 0 && v_day > 0){
			v_week++;
			f_countBehavioursPerWeek();
			
		}
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
	ev.count_b2_noProb = 0;
	ev.count_b2_successful = 0;
	ev.count_b3_notSuccessful = 0;
	ev.count_b3_successful = 0;
	ev.count_chargingRequired = 0;
	ev.count_chargingSessions = 0;
	ev.count_fulfilledMoveRequest = 0;
	ev.count_successfulRechecks = 0;
	ev.count_unsuccessfulRechecks = 0;
	ev.count_leftUncharged = 0;
	ev.count_leftWhileCharging = 0;
	ev.count_leftWhileChargingWithDelayedAccess = 0;
	ev.v_outOfModelCharge_kWh = 0.0;
	ev.v_totalElectricityCharged_kWh = 0.0;
	ev.v_km_driven = 0.0;
	ev.v_tripFinished = 0;

	ev.count_b2_noIdleChargers = 0;
	ev.count_b2_noMatchingRequest = 0;
	ev.count_b2_noProb = 0;
}
for(CarOwner ice : ICECarOwners){
	ice.v_km_driven = 0.0;
}
countTripDepartures = 0;
countTripArrivals = 0;
count_requiredChargingSessions = 0;
count_chargingSessions = 0;
count_wantsToCharge = 0;
b1 = 0;
b2 = 0;
b3 = 0;
count_extendedB1 = 0;
count_extendedB1AlreadyOnTrip = 0;
count_b1ExtendedTriggered = 0;
b3Triggered = 0;
leftCP = 0;
connectedToCP = 0;
/*ALCODEEND*/}

double f_setArrays()
{/*ALCODESTART::1755074064798*/
// Interactions & rechecks
ar_interactionsPerDay_b1 = new double[p_days];
ar_interactionsPerDay_b2 = new double[p_days];
ar_interactionsPerDay_b3 = new double[p_days];
ar_rechecksPerDay = new double[p_days];

int weeks = p_days / 7;
ar_interactionsPerWeek_b1 = new double[weeks];
ar_interactionsPerWeek_b2 = new double[weeks];
ar_interactionsPerWeek_b3 = new double[weeks];
ar_rechecksPerWeek = new double[weeks];

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
ar_avgNorms = new double[p_days];
ar_avgRC = new double[p_days];
ar_avgPSI = new double[p_days];
ar_avgPCP = new double[p_days];

ar_avgNorm_b1 = new double[p_days];
ar_avgNorm_b2 = new double[p_days];
ar_avgNorm_b3 = new double[p_days];

// Success rates
ar_successRate_b1 = new double[p_days];
ar_successRate_b2 = new double[p_days];
ar_successRate_b3 = new double[p_days];
ar_successRate_rechecks = new double[p_days];

// Probabilities
ar_avgProbability_b1 = new double[p_days];
ar_avgProbability_b2 = new double[p_days];
ar_avgProbability_b3 = new double[p_days];

// Successful & unsuccessful counts
ar_successful_b1 = new double[weeks];
ar_successful_b2 = new double[weeks];
ar_successful_b3 = new double[weeks];
ar_unsuccessful_b1 = new double[weeks];
ar_unsuccessful_b2 = new double[weeks];
ar_unsuccessful_b3 = new double[weeks];

// Special cases
ar_noIdleChargers_b2 = new double[weeks];
ar_unsuccesfulDueToProb_b2 = new double[weeks];
ar_noMatchingRequests_b2 = new double[weeks];

//out of model and uncharged
ar_outOfModelCharging = new double[weeks];
ar_leftWhileCharging = new double[weeks];
ar_leftUncharged = new double[weeks];
ar_leftWhileChargingWithDelayedAccess = new double[weeks];
ar_requiredChargingSessions = new double[weeks];
ar_percSatisfiedChargingSessions = new double[weeks];
ar_chargingSessions = new double[weeks];

ar_kmDrivenPerWeek = new double[weeks];
ar_tripsPerWeek = new double[weeks];
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
/*
HistogramSmartData hs_data = new HistogramSmartData(nIntervals, initialIntervalWidth, calcCDF, calcPercentiles, lowPercent, highPercent);
for(double value : c_norms){
	hs_data.add(value);
}


hs_utility_b1_all.updateData();
hs_utility_b2_all.updateData();
hs_utility_b3_all.updateData();
hs_b1_all.updateData();
hs_b2_all.updateData();
hs_b3_all.updateData();
*/
/*ALCODEEND*/}

double f_convertStandardizedToProb(double val_stand,double mean,double sd,boolean isLikert)
{/*ALCODESTART::1756807925486*/
double probability = 0.0;
double val_norm = 0.0;

double val = mean + val_stand * sd;
if(isLikert){
	val_norm = (val - 1) /6;
}
else {
	val_norm = val;
}

// clip to [0,1] to be safe
val_norm = Math.max(0.0, Math.min(1.0, val_norm));

probability = val_norm;
//if non linear translation using cdf
//probability = (cdf_normal(val_stand) - cdf_normal(-1)) / (cdf_normal(1) - cdf_normal(-1));

return probability;
/*ALCODEEND*/}

double f_countBehavioursPerWeek()
{/*ALCODESTART::1760698613153*/
double interactions_b1 = 0.0;
double interactions_b2 = 0.0;
double interactions_b3 = 0.0;
double rechecking = 0.0;

double outOfModelCharge_kWhperWeek = 0.0;
int count_leftWhileChargingWeek = 0;
int count_leftWhileChargingWithDelayedAccessWeek = 0;
int count_leftUnchargedWeek = 0;
int count_requiredChargingSessionsWeek = 0;
int count_chargingSessionsWeek = 0;
int count_tripsPerWeek = 0;

int count_b1PerWeek = 0;
int count_b2PerWeek = 0;
int count_b3PerWeek = 0;
double count_kmDrivenPerWeek = 0;

int count_b1_successful_perWeek = 0;
int count_b2_successful_perWeek = 0;
int count_b3_successful_perWeek = 0;
int count_b1_notSuccessful_perWeek = 0;
int count_b2_notSuccessful_perWeek = 0;
int count_b3_notSuccessful_perWeek = 0; 
int count_b2_noProb_perWeek = 0;
int count_b2_noIdleChargers_perWeek = 0;
int count_b2_noMatchingRequests_perWeek = 0;

int weekIndex = v_week-1;

//traceln("v_week = " + v_week + " weekIndex " + weekIndex + " day = " + v_day);

if(v_week == 1){
	interactions_b1 = count_b1_successful + count_b1_notSuccessful;
	interactions_b2 = count_b2_successful + count_b2_notSuccessful;
	interactions_b3 = count_b3_successful + count_b3_notSuccessful;
	rechecking = count_successfulRechecks + count_unsuccessfulRechecks;
	
	outOfModelCharge_kWhperWeek = outOfModelCharge_kWh;
	count_leftWhileChargingWeek = count_leftWhileCharging;
	count_leftUnchargedWeek = count_leftUncharged;
	count_leftWhileChargingWithDelayedAccessWeek = count_leftWhileChargingWithDelayedAccess;
	count_requiredChargingSessionsWeek = count_requiredChargingSessions;
	count_chargingSessionsWeek = count_chargingSessions;
	count_kmDrivenPerWeek = sum(EVOwners, x->x.v_km_driven);
	count_tripsPerWeek = v_tripsFinished;
	
	count_b1_successful_perWeek = count_b1_successful;
	count_b2_successful_perWeek = count_b2_successful;
	count_b3_successful_perWeek = count_b3_successful;
	count_b1_notSuccessful_perWeek = count_b1_notSuccessful;
	count_b2_notSuccessful_perWeek = count_b2_notSuccessful;
	count_b3_notSuccessful_perWeek = count_b3_notSuccessful; 
	count_b2_noProb_perWeek = count_b2_noProb;
	count_b2_noIdleChargers_perWeek = count_b2_noIdleChargers;
	count_b2_noMatchingRequests_perWeek = count_b2_noMatchingRequests;	
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
	int sum_prev_LWCWDA = 0;
	int sum_prev_RCD = 0;
	int sum_prev_CS = 0;
	double sum_prev_kmd = 0;
	
	int prev_count_b1_successful_perWeek = 0;
	int prev_count_b2_successful_perWeek = 0;
	int prev_count_b3_successful_perWeek = 0;
	int prev_count_b1_notSuccessful_perWeek = 0;
	int prev_count_b2_notSuccessful_perWeek = 0;
	int prev_count_b3_notSuccessful_perWeek = 0;
	int prev_count_b2_noProb_perWeek = 0;
	int prev_count_b2_noIdleChargers_perWeek = 0;
	int prev_count_b2_noMatchingRequests_perWeek = 0;
	
	int prev_tripsFinished = 0;
	
	
	for (int i = 0; i < weekIndex; i++) {
	    sum_prev_b1 += ar_interactionsPerWeek_b1[i];
	    sum_prev_b2 += ar_interactionsPerWeek_b2[i];
	    sum_prev_b3 += ar_interactionsPerWeek_b3[i];
	    sum_prev_rechecks += ar_rechecksPerWeek[i];
	    
	    sum_prev_OoMC += ar_outOfModelCharging[i];
	    sum_prev_LWC += ar_leftWhileCharging[i];
	    sum_prev_LU += ar_leftUncharged[i];
	    sum_prev_LWCWDA += ar_leftWhileChargingWithDelayedAccess[i];
	    sum_prev_RCD += ar_requiredChargingSessions[i];
	    sum_prev_CS += ar_chargingSessions[i];
	    sum_prev_kmd += ar_kmDrivenPerWeek[i];
	    
	    prev_count_b1_successful_perWeek += ar_successful_b1[i];
		prev_count_b2_successful_perWeek += ar_successful_b2[i];
		prev_count_b3_successful_perWeek += ar_successful_b3[i];
		prev_count_b1_notSuccessful_perWeek += ar_unsuccessful_b1[i];
		prev_count_b2_notSuccessful_perWeek += ar_unsuccessful_b2[i];
		prev_count_b3_notSuccessful_perWeek += ar_unsuccessful_b3[i];
		prev_count_b2_noProb_perWeek += ar_unsuccesfulDueToProb_b2[i];
		prev_count_b2_noIdleChargers_perWeek += ar_noIdleChargers_b2[i];
		prev_count_b2_noMatchingRequests_perWeek += ar_noMatchingRequests_b2[i];
		
		prev_tripsFinished += ar_tripsPerWeek[i];
	}	

	interactions_b1 = count_b1_successful + count_b1_notSuccessful - sum_prev_b1;
	interactions_b2 = count_b2_successful + count_b2_notSuccessful - sum_prev_b2;
	interactions_b3 = count_b3_successful + count_b3_notSuccessful - sum_prev_b3;
	rechecking = count_successfulRechecks + count_unsuccessfulRechecks - sum_prev_rechecks;

	outOfModelCharge_kWhperWeek = outOfModelCharge_kWh - sum_prev_OoMC;
	count_leftWhileChargingWeek = count_leftWhileCharging - sum_prev_LWC;
	count_leftWhileChargingWithDelayedAccessWeek = count_leftWhileChargingWithDelayedAccess - sum_prev_LWCWDA;
	count_leftUnchargedWeek = count_leftUncharged - sum_prev_LU;
	count_chargingSessionsWeek = count_chargingSessions - sum_prev_CS;
	count_requiredChargingSessionsWeek = count_requiredChargingSessions - sum_prev_RCD;
	count_kmDrivenPerWeek = sum(EVOwners, x->x.v_km_driven) - sum_prev_kmd;
	
	count_b1_successful_perWeek = count_b1_successful - prev_count_b1_successful_perWeek;
	count_b2_successful_perWeek = count_b2_successful - prev_count_b2_successful_perWeek;
	count_b3_successful_perWeek = count_b3_successful - prev_count_b3_successful_perWeek;
	count_b1_notSuccessful_perWeek = count_b1_notSuccessful - prev_count_b1_notSuccessful_perWeek;
	count_b2_notSuccessful_perWeek = count_b2_notSuccessful - prev_count_b2_notSuccessful_perWeek;
	count_b3_notSuccessful_perWeek = count_b3_notSuccessful - prev_count_b3_notSuccessful_perWeek; 
	count_b2_noProb_perWeek = count_b2_noProb - prev_count_b2_noProb_perWeek;
	count_b2_noIdleChargers_perWeek = count_b2_noIdleChargers - prev_count_b2_noIdleChargers_perWeek;
	count_b2_noMatchingRequests_perWeek = count_b2_noMatchingRequests - prev_count_b2_noMatchingRequests_perWeek;
	
	count_tripsPerWeek = v_tripsFinished - prev_tripsFinished;
	
	//traceln(outOfModelCharge_kWhperWeek + " oomc in week " + v_week + " with sum prev " + sum_prev_OoMC + " and total " + outOfModelCharge_kWh);
}

ar_interactionsPerWeek_b1[weekIndex] = interactions_b1;
ar_interactionsPerWeek_b2[weekIndex] = interactions_b2;
ar_interactionsPerWeek_b3[weekIndex] = interactions_b3;
ar_rechecksPerWeek[weekIndex] = rechecking;


ar_outOfModelCharging[weekIndex] = outOfModelCharge_kWhperWeek;
ar_leftWhileCharging[weekIndex] = count_leftWhileChargingWeek;
ar_leftWhileChargingWithDelayedAccess[weekIndex] = count_leftWhileChargingWithDelayedAccessWeek;
ar_leftUncharged[weekIndex] = count_leftUnchargedWeek;
ar_chargingSessions[weekIndex] = count_chargingSessionsWeek;
ar_requiredChargingSessions[weekIndex] = count_requiredChargingSessionsWeek;
ar_kmDrivenPerWeek[weekIndex] = count_kmDrivenPerWeek;

ar_successful_b1[weekIndex] = count_b1_successful_perWeek;
ar_successful_b2[weekIndex] = count_b2_successful_perWeek;
ar_successful_b3[weekIndex] = count_b3_successful_perWeek;
ar_unsuccessful_b1[weekIndex] = count_b1_notSuccessful_perWeek;
ar_unsuccessful_b2[weekIndex] = count_b2_notSuccessful_perWeek;
ar_unsuccessful_b3[weekIndex] = count_b3_notSuccessful_perWeek; 
ar_unsuccesfulDueToProb_b2[weekIndex] = count_b2_noProb_perWeek;
ar_noIdleChargers_b2[weekIndex] = count_b2_noIdleChargers_perWeek;
ar_noMatchingRequests_b2[weekIndex] = count_b2_noMatchingRequests_perWeek;
ar_tripsPerWeek[weekIndex] = count_tripsPerWeek;
//traceln(count_tripsPerWeek + " trips in week " + weekIndex);
//traceln(count_chargingSessionsWeek + " cs and " + count_requiredChargingSessionsWeek + " rcs in week " + weekIndex);
//double percSatisfiedChargingSessions = (double) (count_leftUnchargedWeek + count_leftWhileChargingWithDelayedAccessWeek) / count_requiredChargingSessionsWeek;
double percSatisfiedChargingSessions;

if (count_requiredChargingSessionsWeek == 0) {
    percSatisfiedChargingSessions = 1.0;
} else {
    percSatisfiedChargingSessions = (double) count_chargingSessionsWeek / count_requiredChargingSessionsWeek;
}
ar_percSatisfiedChargingSessions[weekIndex] = percSatisfiedChargingSessions;
/*ALCODEEND*/}

double f_setInitialEVsToCP()
{/*ALCODESTART::1761053835097*/
//EVOwners.sort(Comparator.comparingDouble(o -> o.v_soc));

List<EVOwner> listEVOwners = new ArrayList<>();
for(int i=0; i<EVOwners.size(); i++){
	listEVOwners.add(EVOwners.get(i));
}
listEVOwners.sort(Comparator.comparingDouble(o -> o.v_soc)); //

int chargePoints = c_chargePoints.size();
for(int i=0; i<chargePoints; i++){
	EVOwner ev = listEVOwners.get(i);
	J_ChargePoint cp = c_chargePoints.get(i);
	ev.v_status = PARKED_CHARGE_POINT_CHARGING;
	ev.v_chargePoint = cp;
	cp.occupy(ev);
}
/*ALCODEEND*/}

