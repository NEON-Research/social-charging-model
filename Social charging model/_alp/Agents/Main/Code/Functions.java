double f_createEVOwners(int nbOfInputTrips)
{/*ALCODESTART::1745925425403*/
int EVs = roundToInt(p_cars*p_shareEVs);
for(int i = 0; i < EVs; i++){
	EVOwner x = add_EVOwners();  
    f_initializeTrips(nbOfInputTrips, x);
}
/*ALCODEEND*/}

double f_initializeModel()
{/*ALCODESTART::1745928671433*/
f_initalizeMinuteOfWeek();

//Get nb of trips in database
int nbOfInputTrips = (int) selectFirstValue(int.class,
	"SELECT COUNT (*) FROM vehicle_trips;"
);
//Create ICEs and EVs
f_createICECarOwners(nbOfInputTrips);
f_createEVOwners(nbOfInputTrips);
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

traceln("Day of week = " + dayOfWeek + " and from function: " + getDayOfWeek());
	
/*ALCODEEND*/}

double f_triggerTrips()
{/*ALCODESTART::1745930872659*/
for(EVOwner x : EVOwners){
	x.f_updateTripStatus(v_minuteOfWeek);
}
/*ALCODEEND*/}

double f_countTotals()
{/*ALCODESTART::1745939185407*/
v_EVsOnTrip = count(EVOwners, x->x.v_status == ON_TRIP);
v_EVsParkedNonCP = count(EVOwners, x->x.v_status == PARKED_NON_CHARGE_POINT);
v_EVsParkedAtCPCharging = count(EVOwners, x->x.v_status == PARKED_CHARGE_POINT_CHARGING);
v_EVsParkedAtCPIdle = count(EVOwners, x->x.v_status == PARKED_CHARGE_POINT_IDLE);

ch_countEVs.updateData();
/*ALCODEEND*/}

double f_initializeParkingOccupation()
{/*ALCODESTART::1745940299890*/

/*ALCODEEND*/}

double f_createICECarOwners(int nbOfInputTrips)
{/*ALCODESTART::1745941028755*/
int ICECars = roundToInt(p_cars*(1-p_shareEVs));
for(int i = 0; i < ICECars; i++){
	CarOwner x = add_ICECarOwners();
    f_initializeTrips(nbOfInputTrips, x);
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
carOwner.f_initializeNextTrip(v_minuteOfWeek);

/*ALCODEEND*/}

