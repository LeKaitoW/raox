package ru.bmstu.rk9.rao.lib.simulator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


import ru.bmstu.rk9.rao.lib.exception.RaoLibException;
import ru.bmstu.rk9.rao.lib.resource.ComparableResource;
import ru.bmstu.rk9.rao.lib.resource.Resource;
import ru.bmstu.rk9.rao.lib.resource.ResourceManager;

public class ModelState {
	public ModelState(Collection<Class<? extends Resource>> resourceClasses) {
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

	public Collection<ResourceManager<? extends Resource>> getResourceManagers() {

		Collection<ResourceManager<? extends Resource>> list = new ArrayList<>(resourceManagers.values());

		return list;
	}

	@SuppressWarnings("unchecked")
	public <T extends ComparableResource<T>> Collection<T> getAll(Class<T> cl) {
		return (Collection<T>) resourceManagers.get(cl).getAll();
	}

	@SuppressWarnings("unchecked")
	public <T extends ComparableResource<T>> Collection<T> getAccessible(Class<T> cl) {
		return (Collection<T>) resourceManagers.get(cl).getAccessible();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean checkEqual(ModelState other) {
		if (other == null)
			return false;

		if (other == this)
			return true;

		for (Class<?> resourceClass : resourceManagers.keySet()) {
			ResourceManager<?> resourceManager = resourceManagers.get(resourceClass);
			ResourceManager<?> resourceManagerOther = other.resourceManagers.get(resourceClass);

			if (!resourceManager.checkEqual((ResourceManager) resourceManagerOther))
				return false;
		}

		return true;
	}

	// FIXME stub
	public void deploy() {
	}

	// FIXME stub
	public ModelState copy() {
		return null;
	}

	private Map<Class<?>, ResourceManager<?>> resourceManagers = new HashMap<>();
}
