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
import ru.bmstu.rk9.rao.ui.execution.BuildUtil.BundleType;

public class BuildJobProvider {
	private final EclipseResourceFileSystemAccess2 fsa;
	private final IResourceSetProvider resourceSetProvider;
	private final EclipseOutputConfigurationProvider outputConfigurationProvider;
	private final DefaultResourceUIValidatorExtension validatorExtension;
	private final IEditorPart activeEditor;
	private final IWorkbenchWindow activeWorkbenchWindow;

	private static IProject recentProject = null;
	private final String pluginId = RaoActivatorExtension.getInstance().getBundle().getSymbolicName();

	public BuildJobProvider(final IEditorPart activeEditor, final IWorkbenchWindow activeWorkbenchWindow,
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

	public IProject getBuiltProject() {
		return recentProject;
	}

	public static final IProject getProjectToBuild(final IWorkbenchWindow activeWorkbenchWindow,
			final IEditorPart activeEditor) {
		if (activeWorkbenchWindow == null)
			return null;

		if (activeEditor != null) {
			IProject project = BuildUtil.getProject(activeEditor);
			if (project == null)
				return null;

			return project;
		}

		IProject[] projectsInWorkspace = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		IProject projectToBuild = null;

		if (projectsInWorkspace.length == 1 && projectsInWorkspace[0].isAccessible()) {
			projectToBuild = projectsInWorkspace[0];
		} else if (recentProject != null && recentProject.isAccessible()) {
			projectToBuild = recentProject;
		}

		return projectToBuild;
	}

	public final IProject getProject() {
		return recentProject;
	}

	private final IProject selectNewProject() {
		IProject projectToBuild = getProjectToBuild(activeWorkbenchWindow, activeEditor);
		if (projectToBuild == null)
			return null;

		recentProject = projectToBuild;

		return recentProject;
	}

	public final Job createPreprocessJob() {
		Job preprocessJob = new Job("Building Rao model") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				final Display display = PlatformUI.getWorkbench().getDisplay();
				if (selectNewProject() == null) {
					return new Status(IStatus.ERROR, pluginId,
							BuildUtil.createErrorMessage("failed to select project to build"));
				}

				ISaveableFilter filter = new ISaveableFilter() {
					@Override
					public boolean select(Saveable saveable, IWorkbenchPart[] containingParts) {
						if (!saveable.getName().endsWith(".rao"))
							return false;

						if (containingParts.length < 1)
							return false;

						IWorkbenchPart part = containingParts[0];
						if (!(part instanceof XtextEditor))
							return false;

						XtextEditor editor = (XtextEditor) part;
						if (editor.getResource().getProject().equals(recentProject))
							return true;

						return false;
					}
				};

				display.syncExec(() -> PlatformUI.getWorkbench().saveAll(activeWorkbenchWindow, activeWorkbenchWindow,
						filter, true));

				final List<IResource> raoFiles = BuildUtil.getAllRaoFilesInProject(recentProject);
				if (raoFiles.isEmpty()) {
					return new Status(IStatus.ERROR, pluginId,
							BuildUtil.createErrorMessage("project contains no rao files"));
				}

				IFolder srcGenFolder = recentProject.getFolder("src-gen");
				String srcGenErrorMessage = BuildUtil.checkSrcGen(recentProject, srcGenFolder, monitor, false);
				if (srcGenErrorMessage != null)
					return new Status(IStatus.ERROR, pluginId, BuildUtil.createErrorMessage(srcGenErrorMessage));

				final ResourceSet resourceSet = resourceSetProvider.get(recentProject);

				boolean projectHasErrors = false;

				for (IResource resource : raoFiles) {
					Resource loadedResource = resourceSet.getResource(BuildUtil.getURI(resource), true);
					if (!loadedResource.getErrors().isEmpty()) {
						projectHasErrors = true;
						break;
					}

					try {
						validatorExtension.updateValidationMarkers((IFile) resource, loadedResource, CheckMode.ALL,
								monitor);
						IMarker[] markers = resource.findMarkers(IMarker.PROBLEM, true, 0);
						for (IMarker marker : markers) {
							int severity = marker.getAttribute(IMarker.SEVERITY, Integer.MAX_VALUE);
							if (severity == IMarker.SEVERITY_ERROR) {
								projectHasErrors = true;
								break;
							}
						}
					} catch (CoreException e) {
						e.printStackTrace();
						return new Status(IStatus.ERROR, pluginId,
								BuildUtil.createErrorMessage("internal error while calculating model error markers"),
								e);
					}
				}

				if (projectHasErrors)
					return new Status(IStatus.ERROR, pluginId, BuildUtil.createErrorMessage("model has errors"));

				return Status.OK_STATUS;
			}
		};

		preprocessJob.setPriority(Job.BUILD);
		return preprocessJob;
	}

	public final Job createRebuildJob() {
		Job rebuildJob = new Job("Building Rao model") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				String libErrorMessage = BuildUtil.checkLib(recentProject, monitor, BundleType.RAO_LIB);
				if (libErrorMessage != null)
					return new Status(IStatus.ERROR, pluginId, BuildUtil.createErrorMessage(libErrorMessage));

				libErrorMessage = BuildUtil.checkLib(recentProject, monitor, BundleType.XBASE_LIB);
				if (libErrorMessage != null)
					return new Status(IStatus.ERROR, pluginId, BuildUtil.createErrorMessage(libErrorMessage));

				IFolder srcGenFolder = recentProject.getFolder("src-gen");
				String srcGenErrorMessage = BuildUtil.checkSrcGen(recentProject, srcGenFolder, monitor, true);
				if (srcGenErrorMessage != null)
					return new Status(IStatus.ERROR, pluginId, BuildUtil.createErrorMessage(srcGenErrorMessage));

				fsa.setOutputPath(srcGenFolder.getFullPath().toString());
				fsa.setMonitor(monitor);
				fsa.setProject(recentProject);

				Map<String, OutputConfiguration> outputConfigurations = new HashMap<String, OutputConfiguration>();

				for (OutputConfiguration oc : outputConfigurationProvider.getOutputConfigurations(recentProject))
					outputConfigurations.put(oc.getName(), oc);

				fsa.setOutputConfigurations(outputConfigurations);

				// TODO: Invoke inferrer manually
				// generator.doGenerate(resourceSet, fsa);

				try {
					recentProject.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
				} catch (CoreException e) {
					e.printStackTrace();
					return new Status(IStatus.ERROR, pluginId, BuildUtil.createErrorMessage("could not build project"),
							e);
				}

				try {
					IMarker[] markers = recentProject.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true,
							IResource.DEPTH_INFINITE);

					if (markers.length > 0) {
						String errorsDetails = "Project contains errors:";
						for (IMarker marker : markers) {
							errorsDetails += "\nfile " + marker.getResource().getName() + " at line "
									+ MarkerUtilities.getLineNumber(marker) + ": " + MarkerUtilities.getMessage(marker);
						}
						return new Status(IStatus.ERROR, pluginId, errorsDetails);
					}
				} catch (CoreException e) {
					return new Status(IStatus.ERROR, pluginId, BuildUtil
							.createErrorMessage("internal error whule calculating generated files error markers"), e);
				}

				return Status.OK_STATUS;
			}
		};

		rebuildJob.setPriority(Job.BUILD);
		return rebuildJob;
	}
}
