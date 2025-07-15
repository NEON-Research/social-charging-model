double f_initializeNextTrip(int minuteOfWeek)
{/*ALCODESTART::1745940705452*/
c_tripSchedule.sort(Comparator.comparing(J_Trip::getDepartureTime));

v_nextTrip = null;

for (J_Trip trip : c_tripSchedule) {
    if (trip.getDepartureTime() > minuteOfWeek) {
        v_nextTrip = trip;
        break;
    }
}

// If no future trip is found, wrap around to the first trip in the schedule
if (v_nextTrip == null && !c_tripSchedule.isEmpty()) {
    v_nextTrip = c_tripSchedule.get(0);
}

text_nextTrip.setText("Next trip = " + v_nextTrip.toString());

f_initializeStatus();
/*ALCODEEND*/}

double f_updateTripStatus(double timestepStart,double minutesPerTimestep)
{/*ALCODESTART::1745940705454*/
//Start trip in timestep?
int departureTime = (int) v_nextTrip.getDepartureTime();
boolean departureInTimestep = f_isInCurrentTimestep(departureTime, timestepStart, minutesPerTimestep);

//Start if in timestep
if( departureInTimestep ){
	//for EVs
    if (v_type == EV) {
    	if(v_status == PARKED_CHARGE_POINT_CHARGING || v_status == PARKED_CHARGE_POINT_IDLE){
    		main.v_chargePointAvailable++;
    	}
    }
    v_status = ON_TRIP;
}

//Finish trip
int arrivalTime = (int) v_nextTrip.getArrivalTime();
boolean arrivalInTimestep = f_isInCurrentTimestep(arrivalTime, timestepStart, minutesPerTimestep);
if (arrivalInTimestep) {
    v_km_driven += v_nextTrip.getDistance_km();

    //for EVs
    if (v_type == EV) {
    	//this.f_updateChargingStatus(v_nextTrip); // Cast and call the function
        ((EVOwner) this).f_updateSOC(v_nextTrip.getDistance_km()); // Cast and call the function
    	((EVOwner) this).f_setParkingStatus();
    }
    else {
    	v_status = PARKED_NON_CHARGE_POINT_CHARGING_NOT_REQUIRED;
    }

	//New next trip
    f_updateNextTrip();
}

/*ALCODEEND*/}

double f_updateNextTrip()
{/*ALCODESTART::1745940705456*/
int currentIndex = c_tripSchedule.indexOf(v_nextTrip);
int nextIndex = (currentIndex + 1) % c_tripSchedule.size();
v_nextTrip = c_tripSchedule.get(nextIndex);

text_nextTrip.setText("Next trip = " + v_nextTrip.toString());
/*ALCODEEND*/}

double f_initializeStatus()
{/*ALCODESTART::1746001340956*/
v_status = PARKED_NON_CHARGE_POINT_CHARGING_NOT_REQUIRED;

/*ALCODEEND*/}

boolean f_isInCurrentTimestep(int timeToCheck,double timestepStartMinuteOfWeek,double minutesPerTimestep)
{/*ALCODESTART::1746027432257*/
double timestepEndMinuteOfWeek = (timestepStartMinuteOfWeek + minutesPerTimestep) % 10080;

if (timestepStartMinuteOfWeek <= timestepEndMinuteOfWeek) {
	// Normal case: no wrap-around
	return timeToCheck >= timestepStartMinuteOfWeek && timeToCheck < timestepEndMinuteOfWeek;
} else {
	// Wrap-around case: timestep spans end of week
	return timeToCheck >= timestepStartMinuteOfWeek || timeToCheck < timestepEndMinuteOfWeek;
}

/*ALCODEEND*/}

