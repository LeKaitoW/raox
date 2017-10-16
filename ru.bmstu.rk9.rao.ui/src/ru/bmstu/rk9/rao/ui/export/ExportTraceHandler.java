package ru.bmstu.rk9.rao.ui.export;

import java.io.PrintWriter;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import ru.bmstu.rk9.rao.lib.database.Database.Entry;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;
import ru.bmstu.rk9.rao.ui.trace.LegacyTracer;
import ru.bmstu.rk9.rao.ui.trace.Tracer;
import ru.bmstu.rk9.rao.ui.trace.Tracer.TraceOutput;

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
		exportTrace(type);

		return null;
	}

	public final static void exportTrace(ExportType type) {
		switch (type) {
		case REGULAR:
			exportTraceRegular();
			break;
		case LEGACY:
			exportTraceLegacy();
			break;
		default:
			return;
		}

		try {
			ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
		}
	}

	private final static void exportTraceRegular() {
		if (!ready())
			return;

		Tracer tracer = new Tracer(CurrentSimulator.getStaticModelData());

		PrintWriter writer = ExportPrintWriter.initializeWriter(".trc");
		if (writer == null)
			return;

		for (Entry entry : CurrentSimulator.getDatabase().getAllEntries()) {
			TraceOutput output = tracer.parseSerializedData(entry);
			if (output != null)
				writer.println(output.content());
		}
		writer.close();
	}

	private static LegacyTracer legacyTracer = null;

	private final static void exportTraceLegacy() {
		if (!ready())
			return;

		if (legacyTracer == null) {
			legacyTracer = new LegacyTracer();
			legacyTracer.parseAllEntries();
		}

		List<TraceOutput> output = legacyTracer.getTraceList();

		PrintWriter writer = ExportPrintWriter.initializeWriter(".trc.legacy");
		if (writer == null)
			return;

		for (TraceOutput item : output) {
			writer.println(item.content());
		}
		writer.close();
	}

	private final static boolean ready() {
		return CurrentSimulator.isInitialized() && !CurrentSimulator.getDatabase().getAllEntries().isEmpty();
	}

	public final static void reset() {
		legacyTracer = null;
	}
}
