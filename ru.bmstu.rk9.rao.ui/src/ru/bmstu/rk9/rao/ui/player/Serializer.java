package ru.bmstu.rk9.rao.ui.player;

import java.util.List;

import ru.bmstu.rk9.rao.lib.resource.Resource;
import ru.bmstu.rk9.rao.lib.resource.ResourceManager;
import ru.bmstu.rk9.rao.lib.simulator.ModelState;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class Serializer {

	public void dumpResoursestoJSON() {

		ModelState modelState = Simulator.getModelState();
		List<ResourceManager<? extends Resource>> listModelState = modelState.getResourceManagers();
		for (ResourceManager<? extends Resource> resourceManager : listModelState) {
			for (Resource resource : resourceManager.getAll()) {
				//System.out.println(resource.getName() + " " + resource.getTypeName());
			}
		}
	}
}