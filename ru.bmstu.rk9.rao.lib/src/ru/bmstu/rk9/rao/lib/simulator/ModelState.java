package ru.bmstu.rk9.rao.lib.simulator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ru.bmstu.rk9.rao.lib.exception.RaoLibException;
import ru.bmstu.rk9.rao.lib.resource.ComparableResource;
import ru.bmstu.rk9.rao.lib.resource.Resource;
import ru.bmstu.rk9.rao.lib.resource.ResourceManager;

public class ModelState {
	public ModelState(Collection<Class<?>> resourceClasses) {
		for (Class<?> resourceClass : resourceClasses) {
			if (!ComparableResource.class.isAssignableFrom(resourceClass))
				throw new RaoLibException(
						"Attempting to initialize model state with invalid resource type " + resourceClass);

			resourceManagers.put(resourceClass, new ResourceManager<>());
		}
	}

	private ModelState() {
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

	@SuppressWarnings("unchecked")
	public <T extends ComparableResource<T>> Collection<T> getAll(Class<T> cl) {
		return (Collection<T>) resourceManagers.get(cl).getAll();
	}

	@SuppressWarnings("unchecked")
	public <T extends ComparableResource<T>> Collection<T> getAccessible(Class<T> cl) {
		return (Collection<T>) resourceManagers.get(cl).getAccessible();
	}

	@SuppressWarnings("unchecked")
	public <T extends ComparableResource<T>> T getResource(Class<T> cl, String name) {
		return (T) resourceManagers.get(cl).getResource(name);
	}

	@SuppressWarnings("unchecked")
	public <T extends ComparableResource<T>> T getResource(Class<T> cl, int number) {
		return (T) resourceManagers.get(cl).getResource(number);
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

	public final List<Resource> getAllResources() {
		List<Resource> resources = new ArrayList<>();
		for (ResourceManager<?> resourceManager : resourceManagers.values()) {
			resources.addAll(resourceManager.getAll());
		}
		return resources;
	}

	public void deploy() {
		CurrentSimulator.setModelState(this);
	}

	@SuppressWarnings("unchecked")
	public <T extends ComparableResource<T>> T copyOnWrite(T resource) {
		return (T) ((ResourceManager<T>) resourceManagers.get(resource.getClass())).copyOnWrite((T) resource);
	}

	public ModelState deepCopy() {
		return copy(false);
	}

	public ModelState shallowCopy() {
		return copy(true);
	}

	private ModelState copy(boolean isShallow) {
		ModelState copy = new ModelState();
		for (Entry<Class<?>, ResourceManager<?>> entry : resourceManagers.entrySet()) {
			copy.resourceManagers.put(entry.getKey(),
					isShallow ? entry.getValue().shallowCopy() : entry.getValue().deepCopy());
		}

		return copy;
	}

	private Map<Class<?>, ResourceManager<?>> resourceManagers = new HashMap<>();
}
