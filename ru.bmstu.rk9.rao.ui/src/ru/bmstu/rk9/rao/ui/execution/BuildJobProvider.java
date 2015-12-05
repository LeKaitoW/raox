package ru.bmstu.rk9.rao.ui.execution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISaveableFilter;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.Saveable;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.eclipse.xtext.builder.EclipseOutputConfigurationProvider;
import org.eclipse.xtext.builder.EclipseResourceFileSystemAccess2;
import org.eclipse.xtext.generator.OutputConfiguration;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.resource.IResourceSetProvider;
import org.eclipse.xtext.ui.validation.DefaultResourceUIValidatorExtension;
import org.eclipse.xtext.validation.CheckMode;

import ru.bmstu.rk9.rao.ui.RaoActivatorExtension;

public class BuildJobProvider {
	private final EclipseResourceFileSystemAccess2 fsa;
	private final IResourceSetProvider resourceSetProvider;
	private final EclipseOutputConfigurationProvider outputConfigurationProvider;
	private final DefaultResourceUIValidatorExtension validatorExtension;
	private final IEditorPart activeEditor;
	private final IWorkbenchWindow activeWorkbenchWindow;

	private static IProject recentProject = null;
	private final String pluginId = RaoActivatorExtension.getInstance()
			.getBundle().getSymbolicName();

	public BuildJobProvider(final IEditorPart activeEditor,
			final IWorkbenchWindow activeWorkbenchWindow,
			final EclipseResourceFileSystemAccess2 fsa,
			final IResourceSetProvider resourceSetProvider,
			final EclipseOutputConfigurationProvider ocp,
			final DefaultResourceUIValidatorExtension validatorExtension) {
		this.activeEditor = activeEditor;
		this.activeWorkbenchWindow = activeWorkbenchWindow;
		this.fsa = fsa;
		this.resourceSetProvider = resourceSetProvider;
		this.outputConfigurationProvider = ocp;
		this.validatorExtension = validatorExtension;
	}

	public IProject getBuiltProject() {
		return recentProject;
	}

	public static final IProject getProjectToBuild(
			final IWorkbenchWindow activeWorkbenchWindow,
			final IEditorPart activeEditor) {
		if (activeWorkbenchWindow == null)
			return null;

		if (activeEditor != null) {
			IProject project = BuildUtil.getProject(activeEditor);
			if (project == null)
				return null;

			return project;
		}

		IProject[] projectsInWorkspace = ResourcesPlugin.getWorkspace()
				.getRoot().getProjects();
		IProject projectToBuild = null;

		if (projectsInWorkspace.length == 1
				&& projectsInWorkspace[0].isAccessible()) {
			projectToBuild = projectsInWorkspace[0];
		} else if (recentProject != null && recentProject.isAccessible()) {
			projectToBuild = recentProject;
		}

		return projectToBuild;
	}

	public final Job createBuildJob() {
		Job buildJob = new Job("Building Rao model") {
			protected IStatus run(IProgressMonitor monitor) {
				final Display display = PlatformUI.getWorkbench().getDisplay();
				IProject projectToBuild = getProjectToBuild(
						activeWorkbenchWindow, activeEditor);
				if (projectToBuild == null) {
					return new Status(
							Status.ERROR,
							pluginId,
							BuildUtil
									.createErrorMessage("failed to select project to build"));
				}
				recentProject = projectToBuild;

				ISaveableFilter filter = new ISaveableFilter() {
					@Override
					public boolean select(Saveable saveable,
							IWorkbenchPart[] containingParts) {
						if (!saveable.getName().endsWith(".rao"))
							return false;

						if (containingParts.length < 1)
							return false;

						IWorkbenchPart part = containingParts[0];
						if (!(part instanceof XtextEditor))
							return false;

						XtextEditor editor = (XtextEditor) part;
						if (editor.getResource().getProject()
								.equals(recentProject))
							return true;

						return false;
					}
				};

				display.syncExec(() -> PlatformUI.getWorkbench().saveAll(
						activeWorkbenchWindow, activeWorkbenchWindow, filter,
						true));

				String libErrorMessage = BuildUtil.checkRaoLib(recentProject,
						monitor);
				if (libErrorMessage != null)
					return new Status(Status.ERROR, pluginId,
							BuildUtil.createErrorMessage(libErrorMessage));

				final List<IResource> raoFiles = BuildUtil
						.getAllRaoFilesInProject(recentProject);
				if (raoFiles.isEmpty()) {
					return new Status(
							Status.ERROR,
							pluginId,
							BuildUtil
									.createErrorMessage("project contains no rao files"));
				}

				IFolder srcGenFolder = recentProject.getFolder("src-gen");
				String srcGenErrorMessage = BuildUtil.checkSrcGen(
						recentProject, srcGenFolder, monitor);
				if (srcGenErrorMessage != null)
					return new Status(Status.ERROR, pluginId,
							BuildUtil.createErrorMessage(srcGenErrorMessage));

				final ResourceSet resourceSet = resourceSetProvider
						.get(recentProject);

				boolean projectHasErrors = false;

				for (IResource resource : raoFiles) {
					Resource loadedResource = resourceSet.getResource(
							BuildUtil.getURI(resource), true);
					if (!loadedResource.getErrors().isEmpty()) {
						projectHasErrors = true;
						break;
					}

					try {
						validatorExtension.updateValidationMarkers(
								(IFile) resource, loadedResource,
								CheckMode.ALL, monitor);
						IMarker[] markers = resource.findMarkers(
								IMarker.PROBLEM, true, 0);
						if (markers.length > 0) {
							projectHasErrors = true;
							break;
						}
					} catch (CoreException e) {
						e.printStackTrace();
						return new Status(
								Status.ERROR,
								pluginId,
								BuildUtil
										.createErrorMessage("internal error whule calculating model error markers"),
								e);
					}
				}

				if (projectHasErrors) {
					try {
						srcGenFolder.delete(true, new NullProgressMonitor());
					} catch (CoreException e) {
						e.printStackTrace();
					}
					return new Status(Status.ERROR, pluginId,
							BuildUtil.createErrorMessage("model has errors"));
				}

				fsa.setOutputPath(srcGenFolder.getFullPath().toString());

				fsa.setMonitor(monitor);
				fsa.setProject(recentProject);

				Map<String, OutputConfiguration> outputConfigurations = new HashMap<String, OutputConfiguration>();

				for (OutputConfiguration oc : outputConfigurationProvider
						.getOutputConfigurations(recentProject))
					outputConfigurations.put(oc.getName(), oc);

				fsa.setOutputConfigurations(outputConfigurations);

//				TODO: Invoke inferrer manually
//				generator.doGenerate(resourceSet, fsa);

				try {
					recentProject.build(
							IncrementalProjectBuilder.INCREMENTAL_BUILD,
							monitor);
				} catch (CoreException e) {
					e.printStackTrace();
					return new Status(
							Status.ERROR,
							pluginId,
							BuildUtil
									.createErrorMessage("could not build project"),
							e);
				}

				try {
					IMarker[] markers = recentProject.findMarkers(
							IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true,
							IResource.DEPTH_INFINITE);

					if (markers.length > 0) {
						String errorsDetails = "Project contains errors:";
						for (IMarker marker : markers) {
							errorsDetails += "\nfile "
									+ marker.getResource().getName()
									+ " at line "
									+ MarkerUtilities.getLineNumber(marker)
									+ ": " + MarkerUtilities.getMessage(marker);
						}
						return new Status(Status.ERROR, pluginId, errorsDetails);
					}
				} catch (CoreException e) {
					return new Status(
							Status.ERROR,
							pluginId,
							BuildUtil
									.createErrorMessage("internal error whule calculating generated files error markers"),
							e);
				}

				return Status.OK_STATUS;
			}
		};

		buildJob.setPriority(Job.BUILD);
		return buildJob;
	}
}
