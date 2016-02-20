package ru.bmstu.rk9.rao.lib.simulator;

import java.util.ArrayList;
import java.util.List;

public class ModelStateStorage {
	private List<ModelState> stateList = new ArrayList<ModelState>();
	
	void addModelState(ModelState modelState){
		System.out.println("Adding modelState to list");
		stateList.add(modelState);		
	}
	
	List<ModelState> getStateList (){
		
		System.out.println("State list out");
		return stateList;
		
	}
}
