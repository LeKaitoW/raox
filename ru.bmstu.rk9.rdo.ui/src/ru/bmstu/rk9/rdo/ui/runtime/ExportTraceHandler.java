package ru.bmstu.rk9.rdo.ui.runtime;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import ru.bmstu.rk9.rdo.lib.Simulator;
import ru.bmstu.rk9.rdo.lib.Tracer.TraceOutput;

public class ExportTraceHandler extends AbstractHandler
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		if (!Simulator.isInitialized())
			return null;

		ArrayList<TraceOutput> output = Simulator.getTracer().getTraceList();

		//TODO doesn't seem a reliable way to get current project
		IProject proj =
			ResourcesPlugin.getWorkspace().getRoot().getProjects()[0];

		//TODO make a name similar to model name
		Path filePath = Paths.get(
			proj.getLocation().toPortableString(), "traceOutput.trc");

		PrintWriter writer = null;
		try
		{
			writer = new PrintWriter(filePath.toString(), "UTF-8");
		}
		catch (FileNotFoundException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
			return null;
		}

		for (TraceOutput item : output)
		{
			writer.println(item.content());
		}
		writer.close();

		return null;
	}
}
