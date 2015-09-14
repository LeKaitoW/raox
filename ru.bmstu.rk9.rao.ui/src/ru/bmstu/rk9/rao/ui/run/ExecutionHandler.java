package ru.bmstu.rk9.rao.ui.run;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.services.ISourceProviderService;
import org.eclipse.xtext.builder.EclipseOutputConfigurationProvider;
import org.eclipse.xtext.builder.EclipseResourceFileSystemAccess2;
import org.eclipse.xtext.ui.resource.IResourceSetProvider;

import ru.bmstu.rk9.rao.IMultipleResourceGenerator;
import ru.bmstu.rk9.rao.lib.animation.AnimationFrame;
import ru.bmstu.rk9.rao.lib.result.Result;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.ui.animation.AnimationView;
import ru.bmstu.rk9.rao.ui.build.ModelBuilder;
import ru.bmstu.rk9.rao.ui.console.ConsoleView;
import ru.bmstu.rk9.rao.ui.results.ResultsView;
import ru.bmstu.rk9.rao.ui.serialization.SerializationConfigView;
import ru.bmstu.rk9.rao.ui.simulation.ModelExecutionSourceProvider;
import ru.bmstu.rk9.rao.ui.simulation.StatusView;
import ru.bmstu.rk9.rao.ui.trace.ExportTraceHandler;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class ExecutionHandler extends AbstractHandler {
	@Inject
	private IMultipleResourceGenerator generator;

	@Inject
	private Provider<EclipseResourceFileSystemAccess2> fileAccessProvider;

	@Inject
	private IResourceSetProvider resourceSetProvider;

	@Inject
	private EclipseOutputConfigurationProvider outputConfigurationProvider;

	private static boolean isRunning = false;

	public static boolean getRunningState() {
		return isRunning;
	}

	private static void setRunningState(Display display,
			final ModelExecutionSourceProvider sourceProvider, boolean newstate) {
		isRunning = newstate;
		display.syncExec(new Runnable() {
			public void run() {
				sourceProvider.updateRunningState();
			}
		});
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final Display display = PlatformUI.getWorkbench().getDisplay();
		ISourceProviderService sourceProviderService = (ISourceProviderService) HandlerUtil
				.getActiveWorkbenchWindow(event).getService(
						ISourceProviderService.class);
		final ModelExecutionSourceProvider sourceProvider = (ModelExecutionSourceProvider) sourceProviderService
				.getSourceProvider(ModelExecutionSourceProvider.ModelExecutionKey);

		setRunningState(display, sourceProvider, true);

		final Job build = ModelBuilder.build(event, fileAccessProvider.get(),
				resourceSetProvider, outputConfigurationProvider, generator);
		build.schedule();

		IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);

		if (activeEditor == null) {
			setRunningState(display, sourceProvider, false);
			return null;
		}

		final IProject project = ModelBuilder.getProject(activeEditor);

		if (project == null) {
			setRunningState(display, sourceProvider, false);
			return null;
		}

		final Job run = new Job(project.getName() + " execution") {
			protected IStatus run(IProgressMonitor monitor) {
				URLClassLoader classLoader = null;

				try {
					String name = this.getName();
					this.setName(name + " (waiting for execution to complete)");

					IJobManager jobMan = Job.getJobManager();

					for (Job job : jobMan.find("rao_model_run"))
						if (job != this)
							job.join();

					this.setName(name + " (waiting for build to complete)");

					build.join();

					this.setName(name);

					if (build.getResult() != Status.OK_STATUS) {
						setRunningState(display, sourceProvider, false);
						ConsoleView.addLine("Build failed");
						return new Status(Status.CANCEL, "ru.bmstu.rk9.rao.ui",
								"Execution cancelled");
					}

					ConsoleView.clearConsoleText();

					URL model = new URL("file:///"
							+ ResourcesPlugin.getWorkspace().getRoot()
									.getLocation().toString() + "/"
							+ project.getName() + "/bin/");

					URL[] urls = new URL[] { model };

					classLoader = new URLClassLoader(urls,
							Simulator.class.getClassLoader());

					Class<?> modelClass = classLoader
							.loadClass("rao_model.Embedded");

					Method simulation = null;
					Method initialization = null;
					for (Method method : modelClass.getMethods()) {
						if (method.getName() == "runSimulation")
							simulation = method;
						if (method.getName() == "initSimulation")
							initialization = method;
					}

					IFile modelFile = (IFile) HandlerUtil
							.getActiveEditor(event).getEditorInput()
							.getAdapter(IFile.class);

					ExportTraceHandler.reset();
					ExportTraceHandler.setCurrentProject(project);
					ExportTraceHandler.setCurrentModel(modelFile);

					SerializationConfigView.initNames();

					final List<AnimationFrame> frames = new ArrayList<AnimationFrame>();

					if (initialization != null)
						initialization.invoke(null, (Object) frames);

					display.syncExec(() -> AnimationView.initialize(frames));

					final long startTime = System.currentTimeMillis();
					StatusView.setStartTime(startTime);

					ConsoleView.addLine("Started model " + project.getName());

					List<Result> results = new LinkedList<Result>();
					int simulationResult = -1;
					if (simulation != null)
						simulationResult = (int) simulation.invoke(null,
								(Object) results);

					display.syncExec(() -> AnimationView.deinitialize());

					setRunningState(display, sourceProvider, false);

					switch (simulationResult) {
					case 1:
						ConsoleView.addLine("Stopped by terminate condition");
						break;
					case -1:
						ConsoleView.addLine("Model terminated by user");
						break;
					default:
						ConsoleView.addLine("No more events");
					}

					for (Result result : results)
						result.calculate();

					display.asyncExec(() -> ResultsView.setResults(results));

					ConsoleView.addLine("Time elapsed: "
							+ String.valueOf(System.currentTimeMillis()
									- startTime) + "ms");

					classLoader.close();

					return Status.OK_STATUS;
				} catch (Exception e) {
					e.printStackTrace();
					setRunningState(display, sourceProvider, false);
					ConsoleView.addLine("Execution error\n");
					ConsoleView.addLine("Call stack:");
					ConsoleView.printStackTrace(e);
					Simulator.notifyError();

					display.syncExec(() -> AnimationView.deinitialize());

					if (classLoader != null)
						try {
							classLoader.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}

					return new Status(Status.ERROR, "ru.bmstu.rk9.rao.ui",
							"Execution failed", e);
				}
			}

			@Override
			public boolean belongsTo(Object family) {
				return ("rao_model_run").equals(family);
			}
		};
		run.setPriority(Job.LONG);
		run.schedule();

		return null;
	}
}
