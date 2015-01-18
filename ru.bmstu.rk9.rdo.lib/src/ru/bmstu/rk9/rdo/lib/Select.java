package ru.bmstu.rk9.rdo.lib;

import java.util.Collection;

public class Select
{
	public static interface Checker<T>
	{
		public boolean check(T res);
	}

	public static <T> boolean Exist(Collection<T> resources, Checker<T> checker)
	{
		for(T res : resources)
			if(checker.check(res))
				return true;
		return false;
	}

	public static <T> boolean Not_Exist(Collection<T> resources, Checker<T> checker)
	{
		for(T res : resources)
			if(checker.check(res))
				return false;
		return true;
	}

	public static <T> boolean For_All(Collection<T> resources, Checker<T> checker)
	{
		for(T res : resources)
			if(!checker.check(res))
				return false;
		return true;
	}

	public static <T> boolean Not_For_All(Collection<T> resources, Checker<T> checker)
	{
		for(T res : resources)
			if(!checker.check(res))
				return true;
		return false;
	}

	public static <T> boolean Empty(Collection<T> resources, Checker<T> checker)
	{
		return Not_Exist(resources, checker);
	}

	public static <T> int Size(Collection<T> resources, Checker<T> checker)
	{
		int count = 0;
		for(T res : resources)
			if(checker.check(res))
				count++;
		return count;
	}
}
