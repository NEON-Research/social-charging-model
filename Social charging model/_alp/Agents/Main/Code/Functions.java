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

//Get nb of trips in database
int nbOfInputTrips = (int) selectFirstValue(int.class,
	"SELECT COUNT (*) FROM vehicle_trips;"
);
//Create ICEs and EVs
f_createICECarOwners(nbOfInputTrips);
f_createEVOwners(nbOfInputTrips);

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

double f_createICECarOwners(int nbOfInputTrips)
{/*ALCODESTART::1745941028755*/
int ICECars = roundToInt(p_cars*(1-p_shareEVs));
for(int i = 0; i < ICECars; i++){
	CarOwner x = add_ICECarOwners();
	x.v_type = ICE;
    f_initializeTrips(nbOfInputTrips, x);
    c_carOwners.add(x);
}

/*ALCODEEND*/}

double f_initializeTrips(int nbOfInputTrips,CarOwner carOwner)
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
	if(p_hasProsocialChargingBehaviour){
		f_prosocialChargingBehaviour();
	}
	
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
	if(p_hasProsocialChargingBehaviour){
		f_prosocialChargingBehaviour();
	}

	v_timestep++;
    v_hourOfDay = (v_timestep * p_timestep_minutes / 60.0) % 24;
}

traceln("Finished initial week for division car location");
/*ALCODEEND*/}

