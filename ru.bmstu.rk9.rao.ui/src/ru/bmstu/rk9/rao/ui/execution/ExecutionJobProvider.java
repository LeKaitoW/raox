package ru.bmstu.rk9.rao.ui.execution;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import ru.bmstu.rk9.rao.lib.animation.AnimationFrame;
import ru.bmstu.rk9.rao.lib.result.Result;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.lib.simulator.Simulator.SimulationStopCode;
import ru.bmstu.rk9.rao.ui.animation.AnimationView;
import ru.bmstu.rk9.rao.ui.console.ConsoleView;
import ru.bmstu.rk9.rao.ui.results.ResultsView;
import ru.bmstu.rk9.rao.ui.serialization.SerializationConfigView;
import ru.bmstu.rk9.rao.ui.simulation.StatusView;
import ru.bmstu.rk9.rao.ui.trace.ExportTraceHandler;

public class ExecutionJobProvider {
	private final IProject project;

	public ExecutionJobProvider(final IProject project) {
		this.project = project;
	}

	public final Job createExecutionJob() {
		final Job executionJob = new Job(project.getName() + " execution") {
			protected IStatus run(IProgressMonitor monitor) {
				URLClassLoader classLoader = null;
				final Display display = PlatformUI.getWorkbench().getDisplay();

				try {
					ConsoleView.clearConsoleText();

					URL model = new URL("file:///"
							+ ResourcesPlugin.getWorkspace().getRoot()
									.getLocation().toString() + "/"
							+ project.getName() + "/bin/");

					URL[] urls = new URL[] { model };

					classLoader = new URLClassLoader(urls,
							Simulator.class.getClassLoader());

					Class<?> modelClass = classLoader.loadClass("model");

					Method run = null;
					for (Method method : modelClass.getMethods()) {
						if (method.getName() == "startSimulation")
							run = method;
					}

					ExportTraceHandler.reset();
					ExportTraceHandler.setCurrentProject(project);

					SerializationConfigView.initNames();

					final List<AnimationFrame> frames = new ArrayList<AnimationFrame>();

					display.syncExec(() -> AnimationView.initialize(frames));

					final long startTime = System.currentTimeMillis();
					StatusView.setStartTime(startTime);

					ConsoleView.addLine("Started model " + project.getName());

					List<Result> results = new LinkedList<Result>();
					SimulationStopCode simulationResult = SimulationStopCode.SIMULATION_CONTINUES;
					//TODO pass results
					if (run != null)
						run.invoke(null);

					switch (simulationResult) {
					case TERMINATE_CONDITION:
						ConsoleView.addLine("Stopped by terminate condition");
						break;
					case USER_INTERRUPT:
						ConsoleView.addLine("Model terminated by user");
						break;
					case NO_MORE_EVENTS:
						ConsoleView.addLine("No more events");
						break;
					default:
						ConsoleView.addLine("Runtime error");
						break;
					}

					for (Result result : results)
						result.calculate();

					display.asyncExec(() -> ResultsView.setResults(results));

					ConsoleView.addLine("Time elapsed: "
							+ String.valueOf(System.currentTimeMillis()
									- startTime) + "ms");

					return Status.OK_STATUS;
				} catch (Exception e) {
					e.printStackTrace();
					ConsoleView.addLine("Execution error\n");
					ConsoleView.addLine("Call stack:");
					ConsoleView.printStackTrace(e);
					Simulator.notifyError();

					return new Status(Status.ERROR, "ru.bmstu.rk9.rao.ui",
							"Execution failed", e);
				} finally {
					// TODO deinitialize via notification instead
					display.syncExec(() -> AnimationView.deinitialize());
					if (classLoader != null) {
						try {
							classLoader.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			}

			@Override
			public boolean belongsTo(Object family) {
				return ("rao_model_run").equals(family);
			}
		};

		executionJob.setPriority(Job.LONG);
		return executionJob;
	}
}
