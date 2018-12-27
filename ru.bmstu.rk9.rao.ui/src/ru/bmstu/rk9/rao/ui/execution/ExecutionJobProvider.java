package ru.bmstu.rk9.rao.ui.execution;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.xtext.ui.resource.IResourceSetProvider;
import org.eclipse.xtext.xbase.typesystem.IBatchTypeResolver;

import ru.bmstu.rk9.rao.lib.runtime.Experiments;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.SimulationStopCode;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.ui.animation.AnimationView;
import ru.bmstu.rk9.rao.ui.console.ConsoleView;
import ru.bmstu.rk9.rao.ui.export.ExportTraceHandler;
import ru.bmstu.rk9.rao.ui.gef.process.ProcessParsingException;
import ru.bmstu.rk9.rao.ui.results.ResultsView;
import ru.bmstu.rk9.rao.ui.serialization.SerializationConfigView;
import ru.bmstu.rk9.rao.ui.simulation.StatusView;

@SuppressWarnings("restriction")
public class ExecutionJobProvider {
	public ExecutionJobProvider(final IProject project, IResourceSetProvider resourceSetProvider,
			IBatchTypeResolver typeResolver) {
		this.project = project;
		this.resourceSetProvider = resourceSetProvider;
		this.typeResolver = typeResolver;
	}

	private final IResourceSetProvider resourceSetProvider;
	private final IProject project;
	private final IBatchTypeResolver typeResolver;

	public final Job createExecutionJob() {
		final Job executionJob = new Job(project.getName() + " execution") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				final Display display = PlatformUI.getWorkbench().getDisplay();
				final ModelInternalsParser parser = new ModelInternalsParser(project, resourceSetProvider,
						typeResolver);

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
				SerializationConfigView.initNames();

				CurrentSimulator.set(new Simulator());

				try {
					CurrentSimulator.preinitialize(parser.getSimulatorPreinitializationInfo());
				} catch (Exception e) {
					e.printStackTrace();
					return new Status(IStatus.ERROR, "ru.bmstu.rk9.rao.ui", "Simulator preinitialization failed", e);
				}

				try {
					parser.postprocess();
				} catch (ProcessParsingException e) {
					return new Status(IStatus.ERROR, "ru.bmstu.rk9.rao.ui", "invalid block parameter", e);
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

				Runnable start = () -> {
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

						IStatus status = new Status(IStatus.ERROR, "ru.bmstu.rk9.rao.ui", "Execution failed", e);
						throw new ExecutionException(status);
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

					display.asyncExec(() -> ResultsView.update());

					ConsoleView
							.addLine("Time elapsed: " + String.valueOf(System.currentTimeMillis() - startTime) + "ms");

				};
				Experiments.setStart(start);
				try {
					CurrentSimulator.runExperiments();
					return Status.OK_STATUS;
				} catch (ExecutionException e) {
					return e.getStatus();
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
