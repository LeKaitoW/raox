package ru.bmstu.rk9.rdo.lib;

import java.util.Collection;

import java.util.ArrayList;
import java.util.LinkedList;

import java.util.HashMap;

public class TemporaryResourceManager<T extends TemporaryResource & ResourceComparison<T>> extends PermanentResourceManager<T>
{
	private HashMap<Integer, T> temporary;

	private Integer currentLast;

	public int getNextNumber()
	{
		return currentLast;
	}

	@Override
	public void addResource(T res)
	{
		if (res.getName() != null)
			super.addResource(res);

		Integer number = res.getNumber();
		if (number != null && number == currentLast)
			temporary.put(currentLast++, res);
	}

	public void eraseResource(T res)
	{
		temporary.remove(res.getNumber());
	}

	@Override
	public Collection<T> getAll()
	{
		Collection<T> all = new LinkedList<T>(resources.values());
		all.addAll(temporary.values());
		return all;
	}

	public Collection<T> getTemporary()
	{
		return temporary.values();
	}

	public TemporaryResourceManager()
	{
		this.resources = new HashMap<String, T>();
		this.temporary = new HashMap<Integer, T>();
		this.currentLast = 0;
	}

	private TemporaryResourceManager(TemporaryResourceManager<T> source)
	{
		this.resources = new HashMap<String, T>(source.resources);
		this.temporary = new HashMap<Integer, T>(source.temporary);
		this.currentLast = source.currentLast;
	}

	@Override
	public TemporaryResourceManager<T> copy()
	{
		return new TemporaryResourceManager<T>(this);
	}

	public boolean checkEqual(TemporaryResourceManager<T> other)
	{
		if (!super.checkEqual(this))
			return false;

		if (temporary.size() != other.temporary.size())
			System.out.println("Runtime error: temporary resource set in manager was altered");

		for (int i = 0; i < temporary.size(); i++)
		{
			T resThis = temporary.get(i);
			T resOther = other.temporary.get(i);

			if (resThis != resOther && !resThis.checkEqual(resOther))
				return false;
		}
		return true;
	}
}
