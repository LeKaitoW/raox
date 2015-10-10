package ru.bmstu.rk9.rao.ui.execution;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.builder.EclipseOutputConfigurationProvider;
import org.eclipse.xtext.builder.EclipseResourceFileSystemAccess2;
import org.eclipse.xtext.ui.resource.IResourceSetProvider;
import org.eclipse.xtext.ui.validation.DefaultResourceUIValidatorExtension;

import ru.bmstu.rk9.rao.IMultipleResourceGenerator;
import ru.bmstu.rk9.rao.ui.RaoActivatorExtension;
import ru.bmstu.rk9.rao.ui.console.ConsoleView;
import ru.bmstu.rk9.rao.ui.simulation.ModelExecutionSourceProvider;

public class ExecutionManager {
	private final IMultipleResourceGenerator generator;
	private final EclipseResourceFileSystemAccess2 fsa;
	private final IResourceSetProvider resourceSetProvider;
	private final EclipseOutputConfigurationProvider outputConfigurationProvider;
	private final DefaultResourceUIValidatorExtension validatorExtension;
	private final ExecutionEvent event;

	private final String pluginId = RaoActivatorExtension.getInstance()
			.getBundle().getSymbolicName();

	public ExecutionManager(final ExecutionEvent event,
			final EclipseResourceFileSystemAccess2 fsa,
			final IResourceSetProvider resourceSetProvider,
			final EclipseOutputConfigurationProvider ocp,
			final IMultipleResourceGenerator generator,
			final DefaultResourceUIValidatorExtension validatorExtension) {
		this.event = event;
		this.fsa = fsa;
		this.resourceSetProvider = resourceSetProvider;
		this.outputConfigurationProvider = ocp;
		this.generator = generator;
		this.validatorExtension = validatorExtension;
	}

	public final void execute(boolean buildOnly) {
		Job executionJob = new Job("Building Rao model") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				IWorkbenchWindow activeWorkbenchWindow = HandlerUtil
						.getActiveWorkbenchWindow(event);

				BuildJobProvider modelBuilder = new BuildJobProvider(event,
						fsa, resourceSetProvider, outputConfigurationProvider,
						generator, validatorExtension);
				final Job build = modelBuilder.createBuildJob();
				build.schedule();
				try {
					build.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
					ModelExecutionSourceProvider.setRunningState(
							activeWorkbenchWindow, false);
					return new Status(Status.ERROR, pluginId,
							"Internal error while finishing build");
				}

				if (build.getResult() != Status.OK_STATUS) {
					ModelExecutionSourceProvider.setRunningState(
							activeWorkbenchWindow, false);
					ConsoleView.addLine("Build failed");
					return new Status(Status.CANCEL, pluginId,
							"Execution cancelled");
				}

				if (buildOnly) {
					ModelExecutionSourceProvider.setRunningState(
							activeWorkbenchWindow, false);
					return Status.OK_STATUS;
				}

				final IProject project = modelBuilder.getBuiltProject();
				ExecutionJobProvider modelExecutioner = new ExecutionJobProvider(
						project);
				final Job runJob = modelExecutioner.createExecutionJob();
				runJob.schedule();
				try {
					runJob.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
					return new Status(Status.ERROR, pluginId,
							"Internal error while finishing execution");
				} finally {
					ModelExecutionSourceProvider.setRunningState(
							activeWorkbenchWindow, false);
				}

				if (runJob.getResult() != Status.OK_STATUS) {
					ConsoleView.addLine("Execution failed");
					return new Status(Status.CANCEL, pluginId,
							"Execution failed");
				}

				return Status.OK_STATUS;
			}
		};

		executionJob.setPriority(Job.LONG);
		executionJob.schedule();
	}
}
