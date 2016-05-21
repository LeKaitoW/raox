package ru.bmstu.rk9.rao.ui.trace;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

import ru.bmstu.rk9.rao.lib.database.Database.Entry;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;
import ru.bmstu.rk9.rao.ui.trace.Tracer.TraceOutput;

//TODO export to location chosen by user

public class ExportTraceHandler extends AbstractHandler {
	public static enum ExportType {
		REGULAR("Regular"), LEGACY("Legacy");

		ExportType(final String type) {
			this.type = type;
		}

		static final ExportType getByString(final String type) {
			for (final ExportType exportType : values()) {
				if (exportType.type.equals(type))
					return exportType;
			}
			throw new ExportTraceException("Unexpected export type: " + type);
		}

		public String getString() {
			return type;
		}

		final private String type;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (!ready())
			return null;

		ExportType type = ExportType.getByString(event.getParameter("ru.bmstu.rk9.rao.ui.runtime.exportTraceType"));
		switch (type) {
		case REGULAR:
			exportTraceRegular();
			break;
		case LEGACY:
			exportTraceLegacy();
			break;
		}

		return null;
	}

	public final static void exportTraceRegular() {
		if (!CurrentSimulator.isInitialized() || !ready())
			return;

		Tracer tracer = new Tracer(CurrentSimulator.getStaticModelData());

		PrintWriter writer = initializeWriter(".trc");
		for (Entry entry : CurrentSimulator.getDatabase().getAllEntries()) {
			TraceOutput output = tracer.parseSerializedData(entry);
			if (output != null)
				writer.println(output.content());
		}
		writer.close();
	}

	private static LegacyTracer legacyTracer = null;

	public final static void exportTraceLegacy() {
		if (!CurrentSimulator.isInitialized() || !ready())
			return;

		if (legacyTracer == null) {
			legacyTracer = new LegacyTracer();
			legacyTracer.parseAllEntries();
		}

		List<TraceOutput> output = legacyTracer.getTraceList();
		PrintWriter writer = initializeWriter(".trc.legacy");
		for (TraceOutput item : output) {
			writer.println(item.content());
		}
		writer.close();
	}

	private final static PrintWriter initializeWriter(String suffix) {
		IPath workspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		IPath filePath = workspacePath.append(currentProject.getFullPath()
				.append(currentProject.getName().substring(0, currentProject.getName().lastIndexOf('.')) + suffix));

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(filePath.toString(), "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}

		return writer;
	}

	private final static boolean ready() {
		return (currentProject != null);
	}

	public final static void reset() {
		currentProject = null;
		legacyTracer = null;
	}

	private static IProject currentProject = null;

	public final static void setCurrentProject(IProject project) {
		currentProject = project;
	}
}
