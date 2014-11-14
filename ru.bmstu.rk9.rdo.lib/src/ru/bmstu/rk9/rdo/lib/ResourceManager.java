package ru.bmstu.rk9.rdo.lib;


import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;


public class ResourceManager<T extends Resource & ResourceComparison<T>>
{
	private ArrayList<T> listResources;

	private HashMap<String, T> permanent;
	private HashMap<Integer, T> temporary;	

	private Integer resourceNumber;

	public int getNextNumber()
	{
		return resourceNumber;
	}

	public void addResource(T res)
	{
		String name = res.getName();
		Integer number = res.getNumber();

		if(number == resourceNumber)
			resourceNumber++;
		else
			return;

		if (name != null)
		{
			if (permanent.get(name) != null)
				listResources.set(number, res);
			else
				listResources.add(res);

			permanent.put(name, res);
		}
		else
		{
			temporary.put(number, res);
			listResources.add(res);
		}
	}

	public void eraseResource(T res)
	{
		String name = res.getName();
		Integer number = res.getNumber();

		if (name != null)
			permanent.remove(name);
		else
			temporary.remove(number);
		
		listResources.set(number, null);
	}

	public T getResource(String name)
	{
		return permanent.get(name);
	}

	public Collection<T> getAll()
	{
		return Collections.unmodifiableCollection(listResources);
	}

	public Collection<T> getTemporary()
	{
		return Collections.unmodifiableCollection(temporary.values());
	}

	public ResourceManager()
	{
		this.permanent = new HashMap<String, T>();
		this.temporary = new HashMap<Integer, T>();
		this.listResources = new ArrayList<T>();
		this.resourceNumber = 0;
	}

	private ResourceManager(ResourceManager<T> source)
	{
		this.permanent = new HashMap<String, T>(source.permanent);
		this.temporary = new HashMap<Integer, T>(source.temporary);
		this.listResources = new ArrayList<T>(source.listResources);
		this.resourceNumber = source.resourceNumber;
	}

	public ResourceManager<T> copy()
	{
		return new ResourceManager<T>(this);
	}

	public boolean checkEqual(ResourceManager<T> other)
	{
		if (this.listResources.size() != other.listResources.size())
			return false;

		Iterator<T> itThis = this.listResources.iterator();
		Iterator<T> itOther = other.listResources.iterator();

		for (int i = 0; i < this.listResources.size(); i++)
		{
			T resThis = itThis.next();
			T resOther = itOther.next();

			if (resThis != resOther && !resThis.checkEqual(resOther))
				return false;
		}
		return true;
	}
}
