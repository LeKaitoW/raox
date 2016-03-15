package ru.bmstu.rk9.rao.lib.simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.bmstu.rk9.rao.lib.exception.RaoLibException;
import ru.bmstu.rk9.rao.lib.resource.ComparableResource;
import ru.bmstu.rk9.rao.lib.resource.Resource;
import ru.bmstu.rk9.rao.lib.resource.ResourceManager;

public class ModelState {
	public ModelState(List<Class<? extends Resource>> resourceClasses) {
		for (Class<? extends Resource> resourceClass : resourceClasses) {
			if (!ComparableResource.class.isAssignableFrom(resourceClass))
				throw new RaoLibException(
						"Attempting to initialize model state with invalid resource type " + resourceClass);

			resourceManagers.put(resourceClass, new ResourceManager<>());
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends ComparableResource<T>> void eraseResource(T resource) {
		ResourceManager<T> resourceManager = (ResourceManager<T>) resourceManagers.get(resource.getClass());
		if (resourceManager == null)
			throw new RaoLibException("Attempting to erase resource of non-existing type " + resource.getClass());

		resourceManager.eraseResource(resource);
	}

	@SuppressWarnings("unchecked")
	public <T extends ComparableResource<T>> void addResource(T resource) {
		ResourceManager<T> resourceManager = (ResourceManager<T>) resourceManagers.get(resource.getClass());
		if (resourceManager == null)
			throw new RaoLibException("Attempting to add resource of non-existing type " + resource.getClass());

		resourceManager.addResource(resource);
	}

	public List<ResourceManager<? extends Resource>> getResourceManagers() {

		List<ResourceManager<? extends Resource>> list = new ArrayList<>(resourceManagers.values());

		return list;
	}

	private Map<Class<? extends Resource>, ResourceManager<?>> resourceManagers = new HashMap<>();
}
