package ru.bmstu.rk9.rdo.lib;

import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

public class Tracer
{
	private static BufferedWriter trc;

	public static void startTrace()
	{
		try
		{
			trc = new BufferedWriter(new FileWriter("log.txt"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void append(String entry)
	{
		try
		{
			trc.write(entry + "\n");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void stopTrace()
	{
		try
		{
			trc.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
