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

//text_nextTrip.setText("Next trip = " + v_nextTrip.toString());

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
    	if(v_status == PARKED_CHARGE_POINT_IDLE){
    		f_leaveChargePoint();
    	}
    	else if(v_status == PARKED_CHARGE_POINT_CHARGING ){
    		f_leaveChargePoint();
    		count_leftWhileCharging++;
    	}
    	else if( v_status == PARKED_NON_CHARGE_POINT_CHARGING_NOT_REQUIRED){
    		count_leftUncharged++;
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
    	//((EVOwner) this).f_setParkingStatus();
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

//text_nextTrip.setText("Next trip = " + v_nextTrip.toString());
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

double f_cleanUp()
{/*ALCODESTART::1754493803320*/
c_tripSchedule.clear();

this.deleteSelf();
/*ALCODEEND*/}

double f_leaveChargePoint()
{/*ALCODESTART::1754909149408*/
v_chargePoint.release();
v_chargePoint = null;
/*ALCODEEND*/}

double f_goOnTrip(double timestepStart,double minutesPerTimestep)
{/*ALCODESTART::1754916892774*/
//Start trip in timestep?
int departureTime = (int) v_nextTrip.getDepartureTime();
boolean departureInTimestep = f_isInCurrentTimestep(departureTime, timestepStart, minutesPerTimestep);

//Start if in timestep
if( departureInTimestep ){
	//for EVs
    if (v_type == EV) {
    	//Leave CP
    	if(v_chargePoint != null){
    		f_leaveChargePoint();
    	}
    	//Count unfulfilled charging sessions
    	if(v_status == PARKED_CHARGE_POINT_CHARGING ){
    		count_leftWhileCharging++;
    	}
    	else if( v_status == PARKED_NON_CHARGE_POINT_CHARGING_REQUIRED){
    		count_leftUncharged++;
    	}
    }
    v_status = ON_TRIP;
    main.countTripDepartures++;
}


if(v_status == ON_TRIP && v_chargePoint != null){
	traceln("ERROR, has not dropped CP but on trip");
}
/*ALCODEEND*/}

double f_arriveFromTrip(double timestepStart,double minutesPerTimestep)
{/*ALCODESTART::1754918819469*/
//Finish trip
if(v_status == ON_TRIP){
	int arrivalTime = (int) v_nextTrip.getArrivalTime();
	boolean arrivalInTimestep = f_isInCurrentTimestep(arrivalTime, timestepStart, minutesPerTimestep);
	if (arrivalInTimestep) {
	    v_km_driven += v_nextTrip.getDistance_km();
	
	    //for EVs
	    if (v_type == EV) {
	    	//this.f_updateChargingStatus(v_nextTrip); // Cast and call the function
	        ((EVOwner) this).f_updateSOC(v_nextTrip.getDistance_km()); // Cast and call the function
	        v_status = ARRIVING;
	    }
	    else {
	    	v_status = PARKED_NON_CHARGE_POINT_CHARGING_NOT_REQUIRED;
	    }
	    
		//New next trip
	    f_updateNextTrip();
	    main.countTripArrivals++;
	}
}

/*ALCODEEND*/}

