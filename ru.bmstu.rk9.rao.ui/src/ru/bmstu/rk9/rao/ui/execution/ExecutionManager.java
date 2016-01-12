package ru.bmstu.rk9.rao.ui.execution;

import java.util.EnumSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.xtext.builder.EclipseOutputConfigurationProvider;
import org.eclipse.xtext.builder.EclipseResourceFileSystemAccess2;
import org.eclipse.xtext.ui.resource.IResourceSetProvider;
import org.eclipse.xtext.ui.validation.DefaultResourceUIValidatorExtension;

import ru.bmstu.rk9.rao.lib.notification.Notifier;
import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.lib.notification.Subscription.SubscriptionType;
import ru.bmstu.rk9.rao.ui.RaoActivatorExtension;
import ru.bmstu.rk9.rao.ui.console.ConsoleView;
import ru.bmstu.rk9.rao.ui.simulation.ModelExecutionSourceProvider;
import ru.bmstu.rk9.rao.ui.simulation.ModelExecutionSourceProvider.SimulationState;

public class ExecutionManager {
	private final EclipseResourceFileSystemAccess2 fsa;
	private final IResourceSetProvider resourceSetProvider;
	private final EclipseOutputConfigurationProvider outputConfigurationProvider;
	private final DefaultResourceUIValidatorExtension validatorExtension;
	private final IEditorPart activeEditor;
	private final IWorkbenchWindow activeWorkbenchWindow;

	private final String pluginId = RaoActivatorExtension.getInstance().getBundle().getSymbolicName();

	private enum ExecutionManagerState {
		BEFORE_RUN
	};

	private static final Notifier<ExecutionManagerState> executionManagerNotifier = new Notifier<>(
			ExecutionManagerState.class);

	public static final void registerBeforeRunSubcriber(Subscriber subscriber) {
		executionManagerNotifier.addSubscriber(subscriber, ExecutionManagerState.BEFORE_RUN,
				EnumSet.of(SubscriptionType.IGNORE_ACCUMULATED));
	}

	public ExecutionManager(final IEditorPart activeEditor, final IWorkbenchWindow activeWorkbenchWindow,
			final EclipseResourceFileSystemAccess2 fsa, final IResourceSetProvider resourceSetProvider,
			final EclipseOutputConfigurationProvider ocp,
			final DefaultResourceUIValidatorExtension validatorExtension) {
		this.activeEditor = activeEditor;
		this.activeWorkbenchWindow = activeWorkbenchWindow;
		this.fsa = fsa;
		this.resourceSetProvider = resourceSetProvider;
		this.outputConfigurationProvider = ocp;
		this.validatorExtension = validatorExtension;
	}

	public final void execute(boolean buildOnly) {
		Job executionJob = new Job("Building Rao model") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				ModelExecutionSourceProvider.setSimulationState(activeWorkbenchWindow,
						SimulationState.RUNNING.toString());
				try {
					BuildJobProvider modelBuilder = new BuildJobProvider(activeEditor, activeWorkbenchWindow, fsa,
							resourceSetProvider, outputConfigurationProvider, validatorExtension);
					final Job build = modelBuilder.createBuildJob();
					build.schedule();
					try {
						build.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
						return new Status(IStatus.ERROR, pluginId, "Internal error while finishing build");
					}

					if (build.getResult() != Status.OK_STATUS) {
						ModelExecutionSourceProvider.setSimulationState(activeWorkbenchWindow,
								SimulationState.STOPPED.toString());
						ConsoleView.addLine("Build failed");
						return new Status(IStatus.CANCEL, pluginId, "Execution cancelled");
					}

					if (buildOnly) {
						ModelExecutionSourceProvider.setSimulationState(activeWorkbenchWindow,
								SimulationState.STOPPED.toString());
						return Status.OK_STATUS;
					}

					executionManagerNotifier.notifySubscribers(ExecutionManagerState.BEFORE_RUN);

					final IProject project = modelBuilder.getBuiltProject();
					ExecutionJobProvider modelExecutioner = new ExecutionJobProvider(project);
					final Job runJob = modelExecutioner.createExecutionJob();
					runJob.schedule();
					try {
						runJob.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
						return new Status(IStatus.ERROR, pluginId, "Internal error while finishing execution");
					}

					if (runJob.getResult() != Status.OK_STATUS) {
						ConsoleView.addLine("Execution failed");
						return new Status(IStatus.CANCEL, pluginId, "Execution failed");
					}

					return Status.OK_STATUS;
				} finally {
					executionManagerNotifier.removeAllSubscribers(ExecutionManagerState.BEFORE_RUN);
					ModelExecutionSourceProvider.setSimulationState(activeWorkbenchWindow,
							SimulationState.STOPPED.toString());
				}
			}
		};

		executionJob.setPriority(Job.LONG);
		executionJob.schedule();
	}
}
