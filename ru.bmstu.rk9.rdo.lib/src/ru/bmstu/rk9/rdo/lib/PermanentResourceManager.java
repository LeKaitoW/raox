package ru.bmstu.rk9.rdo.lib;

import java.util.Iterator;

import java.util.ArrayList;

import java.util.HashMap;

public class PermanentResourceManager<T extends PermanentResource & ResourceComparison<T>>
{
	protected HashMap<String, T> resources;
	protected ArrayList<T> listResources;

	public void addResource(T res)
	{
		if (resources.get(res.getName()) != null)
			listResources.set(listResources.indexOf(resources.get(res.getName())), res);
		else
			listResources.add(res);

		resources.put(res.getName(), res);
	}

	public T getResource(String name)
	{
		return resources.get(name);
	}

	public java.util.Collection<T> getAll()
	{
		return listResources;
	}

	public PermanentResourceManager()
	{
		this.listResources = new ArrayList<T>();
		this.resources = new HashMap<String, T>();
	}

	private PermanentResourceManager(PermanentResourceManager<T> source)
	{
		this.listResources = new ArrayList<T>(source.listResources);
		this.resources = new HashMap<String, T>(source.resources);
	}

	public PermanentResourceManager<T> copy()
	{
		return new PermanentResourceManager<T>(this);
	}

	public boolean checkEqual(PermanentResourceManager<T> other)
	{
		if (resources.values().size() != other.resources.values().size())
			System.out.println("Runtime error: resource set in manager was altered");

		Iterator<T> itThis = resources.values().iterator();
		Iterator<T> itOther = other.resources.values().iterator();

		for (int i = 0; i < resources.values().size(); i++)
		{
			T resThis = itThis.next();
			T resOther = itOther.next();

			if (resThis != resOther && !resThis.checkEqual(resOther))
				return false;
		}
		return true;
	}
}
