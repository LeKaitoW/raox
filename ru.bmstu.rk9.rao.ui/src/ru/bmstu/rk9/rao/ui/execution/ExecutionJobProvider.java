package ru.bmstu.rk9.rao.ui.execution;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import ru.bmstu.rk9.rao.lib.result.Result;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.SimulationStopCode;
import ru.bmstu.rk9.rao.ui.animation.AnimationView;
import ru.bmstu.rk9.rao.ui.console.ConsoleView;
import ru.bmstu.rk9.rao.ui.results.ResultsView;
import ru.bmstu.rk9.rao.ui.serialization.SerializationConfigView;
import ru.bmstu.rk9.rao.ui.simulation.StatusView;
import ru.bmstu.rk9.rao.ui.trace.ExportTraceHandler;

public class ExecutionJobProvider {
	public ExecutionJobProvider(final IProject project) {
		this.project = project;
	}

	private final IProject project;

	public final Job createExecutionJob() {
		final Job executionJob = new Job(project.getName() + " execution") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				final Display display = PlatformUI.getWorkbench().getDisplay();
				final ModelInternalsParser parser = new ModelInternalsParser(project);

				ConsoleView.clearConsoleText();

				try {
					parser.parse();
				} catch (Exception e) {
					e.printStackTrace();
					return new Status(IStatus.ERROR, "ru.bmstu.rk9.rao.ui", "Model parsing failed", e);
				} finally {
					parser.closeClassLoader();
				}

				// TODO mess directly below, generalize in some way?
				ExportTraceHandler.reset();
				ExportTraceHandler.setCurrentProject(project);
				SerializationConfigView.initNames();
				List<Result> results = new LinkedList<Result>();

				CurrentSimulator.set(new Simulator());

				try {
					CurrentSimulator.preinitialize(parser.getSimulatorPreinitializationInfo());
				} catch (Exception e) {
					e.printStackTrace();
					return new Status(IStatus.ERROR, "ru.bmstu.rk9.rao.ui", "Simulator preinitialization failed", e);
				}

				try {
					parser.postprocess();
				} catch (Exception e) {
					e.printStackTrace();
					return new Status(IStatus.ERROR, "ru.bmstu.rk9.rao.ui", "Model postprocessing failed", e);
				}

				display.syncExec(() -> AnimationView.initialize(parser.getAnimationFrames()));

				try {
					CurrentSimulator.initialize(parser.getSimulatorInitializationInfo());
				} catch (Exception e) {
					e.printStackTrace();
					return new Status(IStatus.ERROR, "ru.bmstu.rk9.rao.ui", "Simulator initialization failed", e);
				}

				final long startTime = System.currentTimeMillis();
				StatusView.setStartTime(startTime);
				ConsoleView.addLine("Started model " + project.getName());

				SimulationStopCode simulationResult;

				try {
					simulationResult = CurrentSimulator.run();
				} catch (Throwable e) {
					e.printStackTrace();
					ConsoleView.addLine("Execution error\n");
					ConsoleView.addLine("Call stack:");
					ConsoleView.printStackTrace(e);
					CurrentSimulator.notifyError();

					if (e instanceof Error)
						throw e;

					return new Status(IStatus.ERROR, "ru.bmstu.rk9.rao.ui", "Execution failed", e);
				} finally {
					display.syncExec(() -> AnimationView.deinitialize());
				}

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

				ConsoleView.addLine("Time elapsed: " + String.valueOf(System.currentTimeMillis() - startTime) + "ms");

				return Status.OK_STATUS;
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
