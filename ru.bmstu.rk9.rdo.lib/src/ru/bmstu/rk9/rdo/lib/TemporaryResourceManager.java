package ru.bmstu.rk9.rdo.lib;

import java.util.Collection;

import java.util.ArrayList;
import java.util.LinkedList;

import java.util.HashMap;

public class TemporaryResourceManager<T extends TemporaryResource & ResourceComparison<T>> extends PermanentResourceManager<T>
{
	private ArrayList<T> temporary;

	private LinkedList<Integer> vacantList;
	private Integer currentLast;

	public int getNextNumber()
	{
		if (vacantList.size() > 0)
			return vacantList.poll();
		else
			return currentLast++;
	}

	@Override
	public void addResource(T res)
	{
		if (res.getName() != null)
			super.addResource(res);

		if (res.getNumber() != null)
			if (res.getNumber() == temporary.size())
				temporary.add(res);
			else
				temporary.set(res.getNumber(), res);
	}

	public void eraseResource(T res)
	{
		vacantList.add(res.getNumber());
		temporary.set(res.getNumber(), null);
	}

	@Override
	public Collection<T> getAll()
	{
		Collection<T> all = new LinkedList<T>(resources.values());
		all.addAll(temporary);
		return all;
	}

	public Collection<T> getTemporary()
	{
		return temporary;
	}

	public TemporaryResourceManager()
	{
		this.resources = new HashMap<String, T>();
		this.temporary = new ArrayList<T>();
		this.vacantList = new LinkedList<Integer>();
		this.currentLast = 0;
	}

	private TemporaryResourceManager(TemporaryResourceManager<T> source)
	{
		this.resources = new HashMap<String, T>(source.resources);
		this.temporary = new ArrayList<T>(source.temporary);
		this.vacantList = source.vacantList;
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
