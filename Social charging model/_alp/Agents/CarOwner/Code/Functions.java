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
/*ALCODEEND*/}

double f_updateTripStatus(int minuteOfWeek)
{/*ALCODESTART::1745940705454*/
if(v_nextTrip.getDepartureTime() == minuteOfWeek){
	v_status = ON_TRIP;
}
if(v_nextTrip.getArrivalTime() == minuteOfWeek){
	v_status = PARKED_NON_CHARGE_POINT;
	v_km_driven += v_nextTrip.getDistrance_km();
	
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

