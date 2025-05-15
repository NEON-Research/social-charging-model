void e_activityTrigger()
{/*ALCODESTART::1745930528409*/
if(time() == 0){
	pauseSimulation();
}

f_triggerTrips();

//Update charging
for(EVOwner x : EVOwners){
	if(x.v_status == PARKED_CHARGE_POINT_CHARGING){
		x.f_chargeCar();
	}
}

f_countTotals();

//Update minute of week
v_minuteOfWeek = (v_minuteOfWeek + 1) % 10080;

/*ALCODEEND*/}

