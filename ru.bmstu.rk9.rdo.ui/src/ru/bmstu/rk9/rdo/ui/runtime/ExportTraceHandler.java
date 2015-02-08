package ru.bmstu.rk9.rdo.ui.runtime;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

import ru.bmstu.rk9.rdo.lib.LegacyTracer;
import ru.bmstu.rk9.rdo.lib.Simulator;
import ru.bmstu.rk9.rdo.lib.Tracer.TraceOutput;

//TODO export to location chosen by user

public class ExportTraceHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (!ready())
			return null;

		String type = event
				.getParameter("ru.bmstu.rk9.rdo.ui.runtime.exportTraceType");
		switch (type) {
		case "Regular":
			exportTraceRegular();
			break;
		case "Legacy":
			exportTraceLegacy();
			break;
		}

		return null;
	}

	public final static void exportTraceRegular() {
		if (!Simulator.isInitialized() || !ready())
			return;

		ArrayList<TraceOutput> output = Simulator.getTracer().getTraceList();

		exportToFile(output, ".trc");
	}

	private static LegacyTracer legacyTracer = null;

	public final static void exportTraceLegacy() {
		if (!Simulator.isInitialized() || !ready())
			return;

		if (legacyTracer == null) {
			legacyTracer = new LegacyTracer();
			legacyTracer.parseNewEntries();
		}

		ArrayList<TraceOutput> output = legacyTracer.getTraceList();
		exportToFile(output, ".trc.legacy");
	}

	private final static void exportToFile(ArrayList<TraceOutput> output,
			String suffix) {
		IPath workspacePath = ResourcesPlugin.getWorkspace().getRoot()
				.getLocation();
		IPath filePath = workspacePath.append(currentProject.getFullPath()
				.append(currentModel.getName().substring(0,
						currentModel.getName().lastIndexOf('.'))
						+ suffix));

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(filePath.toString(), "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}

		for (TraceOutput item : output) {
			writer.println(item.content());
		}
		writer.close();
	}

	private final static boolean ready() {
		return (currentProject != null && currentModel != null);
	}

	public final static void reset() {
		currentProject = null;
		currentModel = null;
		legacyTracer = null;
	}

	private static IProject currentProject = null;

	public final static void setCurrentProject(IProject project) {
		currentProject = project;
	}

	private static IFile currentModel = null;

	public final static void setCurrentModel(IFile model) {
		currentModel = model;
	}
}
