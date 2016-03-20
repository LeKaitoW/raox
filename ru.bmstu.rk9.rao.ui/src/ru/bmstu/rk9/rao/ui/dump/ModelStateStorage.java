package ru.bmstu.rk9.rao.ui.dump;

import java.util.ArrayList;
import java.util.List;

import ru.bmstu.rk9.rao.lib.simulator.ModelState;

public class ModelStateStorage {
	private List<ModelState> stateList = new ArrayList<ModelState>();

	void addModelState(ModelState modelState) {
		stateList.add(modelState);
	}

	List<ModelState> getStateList() {
		return stateList;
	}
}
