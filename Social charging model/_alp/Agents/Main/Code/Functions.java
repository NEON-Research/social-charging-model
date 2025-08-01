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
v_chargePointAvailable = p_chargePoints;
v_hourOfDay = 0;

f_getVariableWeights();

//Get nb of trips in database
nbOfInputTrips = (int) selectFirstValue(int.class,
	"SELECT COUNT (*) FROM vehicle_trips;"
);
//Create ICEs and EVs
f_createICECarOwners();
f_generateSyntehticPopulationEVs();

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

double f_triggerTrips()
{/*ALCODESTART::1745930872659*/
for(CarOwner x : c_carOwners){
	//Update minute of week to match database
	double minutesPerTimestep = p_timestep_minutes;
	double timestepStartMinuteOfWeek = (v_timestep * minutesPerTimestep) % (7 * 24 * 60);
	
	//Update trip status 
	x.f_updateTripStatus(timestepStartMinuteOfWeek, minutesPerTimestep);
}
/*ALCODEEND*/}

double f_countTotals()
{/*ALCODESTART::1745939185407*/
v_carsOnTrip = count(c_carOwners, x->x.v_status == ON_TRIP);
v_ICECarsParkedNonCP = count(ICECarOwners, x->x.v_status == PARKED_NON_CHARGE_POINT_CHARGING_NOT_REQUIRED);
v_EVsParkedNonCPChargingRequired = count(EVOwners, x->x.v_status == PARKED_NON_CHARGE_POINT_CHARGING_REQUIRED);
v_EVsParkedNonCPChargingNotRequired = count(EVOwners, x->x.v_status == PARKED_NON_CHARGE_POINT_CHARGING_NOT_REQUIRED);
v_EVsParkedAtCPCharging = count(c_carOwners, x->x.v_status == PARKED_CHARGE_POINT_CHARGING);
v_EVsParkedAtCPIdle = count(c_carOwners, x->x.v_status == PARKED_CHARGE_POINT_IDLE);

data_carsOnTrip.add(v_timestep, v_carsOnTrip);
data_ICECarsParkedNonCP.add(v_timestep, v_ICECarsParkedNonCP);
data_EVsParkedNonCPChargingRequired.add(v_timestep, v_EVsParkedNonCPChargingRequired);
data_EVsParkedNonCPChargingNotRequired.add(v_timestep, v_EVsParkedNonCPChargingNotRequired);
data_EVsParkedAtCPCharging.add(v_timestep, v_EVsParkedAtCPCharging);
data_EVsParkedAtCPIdle.add(v_timestep, v_EVsParkedAtCPIdle);

data_CPAvailable.add(v_timestep, v_chargePointAvailable);
data_CPOccupied.add(v_timestep, p_chargePoints - v_chargePointAvailable);

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

//Trigger over timesteps
for(int i=0; i < nbOfTimesteps; i++){
	
	//1. Trigger trips	
	f_triggerTrips();
		
	//2. Update charging
	f_chargeCars();
	
	//3. Check if CP have become available for waiting EVs
	if(p_checkCPAvailability){
		f_checkAvailableChargePoints();
	}
	
	//4. prosocial charging behaviour
	/*
	if(p_hasProsocialChargingBehaviour){
		for(EVOwner x : EVOwners){
			x.f_prosocialChargingBehaviour();
		}
	}
	*/
	//4. Count totals
	f_countTotals();
		
	v_timestep++;
    v_hourOfDay = (v_timestep * p_timestep_minutes / 60.0) % 24;
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
/*ALCODEEND*/}

double f_endSimulationPeriod()
{/*ALCODESTART::1746090357452*/
f_updateChart();
/*ALCODEEND*/}

double f_writeResultsToExcel(ExcelFile excel_exportResults)
{/*ALCODESTART::1746096423531*/
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

traceln("Finished writing results to excel!");
/*ALCODEEND*/}

double f_checkAvailableChargePoints()
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

double f_chargeCars()
{/*ALCODESTART::1747135360798*/
for(EVOwner x : EVOwners){
	if(x.v_status == PARKED_CHARGE_POINT_CHARGING){
		x.f_chargeCar();
	}
}
/*ALCODEEND*/}

double f_simulateFirstWeekToGetInitialLocationCars()
{/*ALCODESTART::1748942230602*/
v_timestep = 0;
int minutesPerWeek = 7 * 24 * 60;
double timestepsInWeek = (double) minutesPerWeek / p_timestep_minutes;

//Trigger over timesteps
for(int i=0; i < timestepsInWeek; i++){
	
	//1. Trigger trips	
	f_triggerTrips();
		
	//2. Update charging
	f_chargeCars();
	
	//3. Check if CP have become available for waiting EVs
	if(p_checkCPAvailability){
		f_checkAvailableChargePoints();
	}
	
	//4. prosocial charging behaviour
	/*
	if(p_hasProsocialChargingBehaviour){
		f_prosocialChargingBehaviour();
	}
	*/
	v_timestep++;
    v_hourOfDay = (v_timestep * p_timestep_minutes / 60.0) % 24;
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
f_histogramsPopData();

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

int col_norms = 1;
int col_trust = 2;
int col_rc = 3;
int col_psi = 4;

List<Double> norms = new ArrayList<>();
List<Double> trust = new ArrayList<>();
List<Double> rc = new ArrayList<>();
List<Double> psi = new ArrayList<>();

for(int rowIndex = 2; rowIndex < size + 1; rowIndex++){
	norms.add(ef_spvars.getCellNumericValue(sheetName, rowIndex, col_norms));
	trust.add(ef_spvars.getCellNumericValue(sheetName, rowIndex, col_trust));
	rc.add(ef_spvars.getCellNumericValue(sheetName, rowIndex, col_rc));
	psi.add(ef_spvars.getCellNumericValue(sheetName, rowIndex, col_psi));
}


Collections.sort(norms);
Collections.sort(trust);
Collections.sort(rc);
Collections.sort(psi);

sortedRealData = Arrays.asList(norms, trust, rc, psi);



//Add histograms to check
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
}

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
hs_psi_pop.updateData();;
/*ALCODEEND*/}

double f_mediationResultsQuery(String behavior,String var1,String var2)
{/*ALCODESTART::1752592809828*/
return (double) selectFirstValue(double.class,
	"SELECT value FROM mediation_results WHERE " + 
		"var_1 = ? AND " + 
		"behavior = ? LIMIT 1;",
		var1,
		behavior
);
/*ALCODEEND*/}

double f_getVariableWeights()
{/*ALCODESTART::1752592809830*/
//Behavior 2: Requesting to move
String behavior = "b1_move_vehicle";
String var1 = "norms";
String var2 = "perceived_social_interdependence";

String key = behavior+var1+var2;
double value = f_mediationResultsQuery(behavior, var1, var2);
c_variableWeights.put(key, value);
b1_norms_psi = value;

var2 = "trust";
key = behavior+var1+var2;
value = f_mediationResultsQuery(behavior, var1, var2);
c_variableWeights.put(key, value);
b1_trust_psi = value;

var2 = "reputational_concern";
key = behavior+var1+var2;
value = f_mediationResultsQuery(behavior, var1, var2);
c_variableWeights.put(key, value);
b1_rc_psi = value;

var1 = "perceived_social_interdependence";
var2 = "b1_move_vehicle";
key = behavior+var1+var2;
value = f_mediationResultsQuery(behavior, var1, var2);
c_variableWeights.put(key, value);
b1_psi_b1 = value;

//Behavior 2: Requesting to move
behavior = "b2_request_move";
var1 = "norms";
var2 = "perceived_social_interdependence";

key = behavior+var1+var2;
value = f_mediationResultsQuery(behavior, var1, var2);
c_variableWeights.put(key, value);
b2_norms_psi = value;

var2 = "trust";
key = behavior+var1+var2;
value = f_mediationResultsQuery(behavior, var1, var2);
c_variableWeights.put(key, value);
b2_trust_psi = value;

var2 = "reputational_concern";
key = behavior+var1+var2;
value = f_mediationResultsQuery(behavior, var1, var2);
c_variableWeights.put(key, value);
b2_rc_psi = value;

var1 = "perceived_social_interdependence";
var2 = "b2_request_move";
key = behavior+var1+var2;
value = f_mediationResultsQuery(behavior, var1, var2);
c_variableWeights.put(key, value);
b2_psi_b2 = value;

var1 = "trust";
var2 = "b2_request_move";
key = behavior+var1+var2;
value = f_mediationResultsQuery(behavior, var1, var2);
c_variableWeights.put(key, value);
b2_trust_b2 = value;

//Behavior 3: Notify neighbour
behavior = "b3_notify_neighbour";
var1 = "norms";
var2 = "perceived_social_interdependence";

key = behavior+var1+var2;
value = f_mediationResultsQuery(behavior, var1, var2);
c_variableWeights.put(key, value);
b3_norms_psi = value;

var2 = "trust";
key = behavior+var1+var2;
value = f_mediationResultsQuery(behavior, var1, var2);
c_variableWeights.put(key, value);
b3_trust_psi = value;

var2 = "reputational_concern";
key = behavior+var1+var2;
value = f_mediationResultsQuery(behavior, var1, var2);
c_variableWeights.put(key, value);
b3_rc_psi = value;

var1 = "perceived_social_interdependence";
var2 = "b3_notify_neighbour";
key = behavior+var1+var2;
value = f_mediationResultsQuery(behavior, var1, var2);
c_variableWeights.put(key, value);
b3_psi_b3 = value;

var1 = "trust";
var2 = "b3_notify_neighbour";
key = behavior+var1+var2;
value = f_mediationResultsQuery(behavior, var1, var2);
c_variableWeights.put(key, value);
b3_trust_b3 = value;

for (String key2 : c_variableWeights.keySet()) {
    traceln(key2);
}

/*ALCODEEND*/}

