double f_initializeNextTrip(int minuteOfWeek)
{/*ALCODESTART::1745940705452*/
c_tripSchedule.sort(Comparator.comparing(J_Trip::getDepartureTime));

v_nextTrip = c_tripSchedule.get(0);
/*
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
/*/
v_status = PARKED_NON_CHARGE_POINT_CHARGING_NOT_REQUIRED;
/*ALCODEEND*/}

double f_updateICETripStatus(int timestepStartMinuteInWeek,int timestepEndMinuteInWeek)
{/*ALCODESTART::1745940705454*/
//Start trip in timestep?
int departureTime = (int) v_nextTrip.getDepartureTime();
boolean departureInTimestep = f_isInCurrentTimestep(departureTime, timestepStartMinuteInWeek, timestepEndMinuteInWeek);
//Start if in timestep
if( departureInTimestep ){
    v_status = ON_TRIP;
}

//Finish trip
int arrivalTime = (int) v_nextTrip.getArrivalTime();
boolean arrivalInTimestep = f_isInCurrentTimestep(arrivalTime, timestepStartMinuteInWeek, timestepEndMinuteInWeek);
if (arrivalInTimestep) {
    v_km_driven += v_nextTrip.getDistance_km();

	//New next trip
    f_updateNextTrip(timestepEndMinuteInWeek);
}

/*ALCODEEND*/}

double f_updateNextTrip(int timestepEndMinuteInWeek)
{/*ALCODESTART::1745940705456*/
int currentIndex = c_tripSchedule.indexOf(v_nextTrip);
int nextIndex = (currentIndex + 1) % c_tripSchedule.size();
v_nextTrip = c_tripSchedule.get(nextIndex);

if(v_nextTrip.getDepartureTime() < timestepEndMinuteInWeek && nextIndex != 0){
	f_goOnTrip();
	//traceln("trigger next trip in same time period");
}



//text_nextTrip.setText("Next trip = " + v_nextTrip.toString());
/*ALCODEEND*/}

double f_initializeStatus()
{/*ALCODESTART::1746001340956*/

/*ALCODEEND*/}

boolean f_departureInCurrentTimestep(int timestepStartMinuteInWeek,int timestepEndMinuteInWeek)
{/*ALCODESTART::1746027432257*/
//Start trip in timestep?
int departureTime = (int) v_nextTrip.getDepartureTime();
return departureTime >= timestepStartMinuteInWeek && departureTime < timestepEndMinuteInWeek;

/*ALCODEEND*/}

double f_cleanUp()
{/*ALCODESTART::1754493803320*/
v_nextTrip = null;
v_chargePoint = null;
c_tripSchedule.clear();

this.deleteSelf();
/*ALCODEEND*/}

double f_goOnTrip()
{/*ALCODESTART::1754916892774*/
//for EVs
if (this instanceof EVOwner) {
    //Count unfulfilled charging sessions
    if(v_status == PARKED_CHARGE_POINT_CHARGING) {
    	count_leftWhileCharging++;
    	if( v_delayedChargePointAccess ){
    		count_leftWhileChargingWithDelayedAccess++;
    	}
    }
    //Count missed charging sessions
    else if( v_status == PARKED_NON_CHARGE_POINT_CHARGING_REQUIRED){
    	count_leftUncharged++;
    	v_leftUnchargedStreak++;
    }
    //Leave CP
    if(v_chargePoint != null){
    	if(v_status != PARKED_CHARGE_POINT_CHARGING && v_status != PARKED_CHARGE_POINT_IDLE){
    		traceln("Error at EV " + this.getIndex() + " has v_chargePoint when going on trip but no corresponding status");
    	}
    	((EVOwner) this).f_leaveChargePoint(false);
    }
    if (b1_extended){
    	b1_extended = false;
    	main.count_extendedB1AlreadyOnTrip++;
    }
    
}
v_status = ON_TRIP;
main.countTripDepartures++;



if(v_status == ON_TRIP && v_chargePoint != null){
	traceln("ERROR, has not dropped CP but on trip");
}
/*ALCODEEND*/}

double f_arriveFromTrip(int timestepStartMinuteInWeek,int timestepEndMinuteInWeek)
{/*ALCODESTART::1754918819469*/
//Finish trip
if(v_status == ON_TRIP){
	if (f_arrivalInCurrentTimestep(timestepStartMinuteInWeek, timestepEndMinuteInWeek)) {
	    v_km_driven += v_nextTrip.getDistance_km();
	    v_tripFinished++;
	
	    //for EVs
	    if (v_type == EV) {
	    	//this.f_updateChargingStatus(v_nextTrip); // Cast and call the function
	        ((EVOwner) this).f_updateSOC(v_nextTrip.getDistance_km()); // Cast and call the function
	        v_status = ARRIVING;
	    }
	    else {
	    	v_status = PARKED_NON_CHARGE_POINT_CHARGING_NOT_REQUIRED;
	    }
	    main.countTripArrivals++;
		
		//New next trip
	    f_updateNextTrip(timestepEndMinuteInWeek);
	    
	}
}

/*ALCODEEND*/}

boolean f_arrivalInCurrentTimestep(int timestepStartMinuteInWeek,int timestepEndMinuteInWeek)
{/*ALCODESTART::1761218245825*/
//Start trip in timestep?
int arrivalTime = (int) v_nextTrip.getArrivalTime();
return arrivalTime >= timestepStartMinuteInWeek && arrivalTime < timestepEndMinuteInWeek;

/*ALCODEEND*/}

boolean f_isInCurrentTimestep(int tripMinuteOfWeek,int timestepStartMinuteInWeek,int timestepEndMinuteInWeek)
{/*ALCODESTART::1761218675468*/
//Start trip in timestep?
return tripMinuteOfWeek >= timestepStartMinuteInWeek && tripMinuteOfWeek < timestepEndMinuteInWeek;

/*ALCODEEND*/}

