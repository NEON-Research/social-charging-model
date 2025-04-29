void e_activityTrigger()
{/*ALCODESTART::1745930528409*/
if(time() == 0){
	pauseSimulation();
}

f_triggerTrips();
f_countTotals();

//Update minute of week
v_minuteOfWeek = (v_minuteOfWeek + 1) % 10080;

/*ALCODEEND*/}

