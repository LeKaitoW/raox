package ru.bmstu.rk9.rdo.lib;

import java.util.Collection;
import java.util.Collections;

import java.util.Iterator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class ResourceManager<T extends Resource & ResourceComparison<T>>
{
	private SortedMap<Integer, T> listResources;

	private Map<String, T> permanent;
	private Map<Integer, T> temporary;

	private Integer resourceNumber;

	public int getNextNumber()
	{
		return resourceNumber;
	}

	public void addResource(T res)
	{
		String name = res.getName();
		Integer number = res.getNumber();

		if(number.equals(resourceNumber))
			resourceNumber++;
		else if(number > resourceNumber)
			return;

		listResources.put(number, res);

		if(name != null)
			permanent.put(name, res);
		else
			temporary.put(number, res);
	}

	public void eraseResource(T res)
	{
		String name = res.getName();
		Integer number = res.getNumber();

		if(name != null)
			permanent.remove(name);
		else
			temporary.remove(number);

		listResources.remove(number);
	}

	public T getResource(String name)
	{
		return permanent.get(name);
	}

	public T getResource(int number)
	{
		return listResources.get(number);
	}

	public Collection<T> getAll()
	{
		return Collections.unmodifiableCollection(listResources.values());
	}

	public Collection<T> getTemporary()
	{
		return Collections.unmodifiableCollection(temporary.values());
	}

	public ResourceManager()
	{
		this.permanent = new ConcurrentHashMap<String, T>();
		this.temporary = new ConcurrentHashMap<Integer, T>();
		this.listResources = new ConcurrentSkipListMap<Integer, T>();
		this.resourceNumber = 0;
	}

	private ResourceManager(ResourceManager<T> source)
	{
		this.permanent = new ConcurrentHashMap<String, T>(source.permanent);
		this.temporary = new ConcurrentHashMap<Integer, T>(source.temporary);
		this.listResources = new ConcurrentSkipListMap<Integer, T>(source.listResources);
		this.resourceNumber = source.resourceNumber;
	}

	public ResourceManager<T> copy()
	{
		return new ResourceManager<T>(this);
	}

	public boolean checkEqual(ResourceManager<T> other)
	{
		if(this.listResources.size() != other.listResources.size())
			return false;

		Iterator<T> itThis = this.listResources.values().iterator();
		Iterator<T> itOther = other.listResources.values().iterator();

		for(int i = 0; i < this.listResources.size(); i++)
		{
			T resThis = itThis.next();
			T resOther = itOther.next();

			if(resThis != resOther && !resThis.checkEqual(resOther))
				return false;
		}
		return true;
	}
}
