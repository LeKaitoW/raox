package ru.bmstu.rk9.rao.ui.execution;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import ru.bmstu.rk9.rao.lib.animation.AnimationFrame;
import ru.bmstu.rk9.rao.lib.dpt.Logic;
import ru.bmstu.rk9.rao.lib.event.Event;
import ru.bmstu.rk9.rao.lib.json.JSONObject;
import ru.bmstu.rk9.rao.lib.naming.NamingHelper;
import ru.bmstu.rk9.rao.lib.process.Block;
import ru.bmstu.rk9.rao.lib.naming.RaoNameable;
import ru.bmstu.rk9.rao.lib.resource.ComparableResource;
import ru.bmstu.rk9.rao.lib.result.Result;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.lib.simulator.Simulator.SimulationStopCode;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorInitializationInfo;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorPreinitializationInfo;
import ru.bmstu.rk9.rao.ui.animation.AnimationView;
import ru.bmstu.rk9.rao.ui.console.ConsoleView;
import ru.bmstu.rk9.rao.ui.process.BlockConverter;
import ru.bmstu.rk9.rao.ui.process.ProcessEditor;
import ru.bmstu.rk9.rao.ui.process.model.Model;
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
			@SuppressWarnings("unchecked")
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				URLClassLoader classLoader = null;
				final Display display = PlatformUI.getWorkbench().getDisplay();

				try {
					ConsoleView.clearConsoleText();

					URL modelURL = new URL(
							"file:///" + ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + "/"
									+ project.getName() + "/bin/");

					URL[] urls = new URL[] { modelURL };

					classLoader = new URLClassLoader(urls, Simulator.class.getClassLoader());

					SimulatorPreinitializationInfo simulatorPreinitializationInfo = new SimulatorPreinitializationInfo();
					simulatorPreinitializationInfo.modelStructure.put("name", project.getName());

					SimulatorInitializationInfo simulatorInitializationInfo = new SimulatorInitializationInfo();
					List<Field> logicFields = new ArrayList<>();
					List<Field> nameAbleFields = new ArrayList<>();

					for (IResource raoFile : BuildUtil.getAllFilesInProject(project, "rao")) {
						String raoFileName = raoFile.getName();
						raoFileName = raoFileName.substring(0, raoFileName.length() - ".rao".length());
						String modelClassName = project.getName() + "." + raoFileName;
						Class<?> modelClass = Class.forName(modelClassName, false, classLoader);

						try {
							Class<?> init = Class.forName(modelClassName + "$init", false, classLoader);
							Constructor<?> initConstructor = init.getDeclaredConstructor();
							initConstructor.setAccessible(true);
							simulatorInitializationInfo.initList.add((Runnable) initConstructor.newInstance());
						} catch (ClassNotFoundException classException) {
						}

						try {
							Class<?> terminate = Class.forName(modelClassName + "$terminateCondition", false,
									classLoader);
							Constructor<?> terminateConstructor = terminate.getDeclaredConstructor();
							terminateConstructor.setAccessible(true);
							simulatorInitializationInfo.terminateConditions
									.add((Supplier<Boolean>) terminateConstructor.newInstance());
						} catch (ClassNotFoundException classException) {
						}

						for (Class<?> nestedModelClass : modelClass.getDeclaredClasses()) {
							if (Event.class.isAssignableFrom(nestedModelClass))
								simulatorPreinitializationInfo.modelStructure.getJSONArray("events")
										.put(new JSONObject().put("name",
												NamingHelper.fieldNameToFullName(nestedModelClass.getName())));

							if (ComparableResource.class.isAssignableFrom(nestedModelClass))
								simulatorPreinitializationInfo.resourceClasses.add(nestedModelClass);
						}

						for (Field field : modelClass.getDeclaredFields()) {
							if (RaoNameable.class.isAssignableFrom(field.getType()))
								nameAbleFields.add(field);

							if (Logic.class.equals(field.getType())) {
								logicFields.add(field);
								continue;
							}
						}
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

					Simulator.preinitialize(simulatorPreinitializationInfo);

					for (IResource graoFile : BuildUtil.getAllFilesInProject(project, "grao")) {
						Model model = ProcessEditor.readModelFromFile((IFile) graoFile);
						List<Block> blocks = BlockConverter.convertModelToBlocks(model);
						simulatorInitializationInfo.processBlocks.addAll(blocks);
					}

					for (Field field : nameAbleFields) {
						String name = field.getName();
						RaoNameable nameable = (RaoNameable) field.get(null);
						nameable.setName(name);
					}

					for (Field logicField : logicFields) {
						Logic logic = (Logic) logicField.get(null);
						simulatorInitializationInfo.decisionPoints.add(logic);
					}

					Simulator.initialize(simulatorInitializationInfo);
					simulationResult = Simulator.run();

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

					ConsoleView
							.addLine("Time elapsed: " + String.valueOf(System.currentTimeMillis() - startTime) + "ms");

					return Status.OK_STATUS;
				} catch (Exception e) {
					e.printStackTrace();
					ConsoleView.addLine("Execution error\n");
					ConsoleView.addLine("Call stack:");
					ConsoleView.printStackTrace(e);
					Simulator.notifyError();

					return new Status(IStatus.ERROR, "ru.bmstu.rk9.rao.ui", "Execution failed", e);
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
