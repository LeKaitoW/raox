package ru.bmstu.rk9.rao.ui.build;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISaveableFilter;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.Saveable;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.eclipse.xtext.builder.EclipseOutputConfigurationProvider;
import org.eclipse.xtext.builder.EclipseResourceFileSystemAccess2;
import org.eclipse.xtext.generator.OutputConfiguration;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.resource.IResourceSetProvider;

import ru.bmstu.rk9.rao.IMultipleResourceGenerator;

public class ModelBuilder {
	public static URI getURI(IResource res) {
		return URI.createPlatformResourceURI(res.getProject().getName() + "/"
				+ res.getProjectRelativePath(), true);
	}

	public static List<IResource> getAllRaoFilesInProject(IProject project) {
		List<IResource> allRaoFiles = new ArrayList<IResource>();
		IPath path = project.getLocation();
		recursiveFindRaoFiles(allRaoFiles, path, ResourcesPlugin.getWorkspace()
				.getRoot());
		return allRaoFiles;
	}

	private static void recursiveFindRaoFiles(List<IResource> allRaoFiles,
			IPath path, IWorkspaceRoot workspaceRoot) {
		IContainer container = workspaceRoot.getContainerForLocation(path);
		try {
			IResource[] iResources;
			iResources = container.members();
			for (IResource iR : iResources) {
				if ("rao".equalsIgnoreCase(iR.getFileExtension()))
					allRaoFiles.add(iR);
				if (iR.getType() == IResource.FOLDER) {
					IPath tempPath = iR.getLocation();
					recursiveFindRaoFiles(allRaoFiles, tempPath, workspaceRoot);
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public static IProject getProject(IEditorPart activeEditor) {
		IFile file = (IFile) activeEditor.getEditorInput().getAdapter(
				IFile.class);
		if (file == null)
			return null;

		return file.getProject();
	}

	private static void checkProjectClassPath(IProject project,
			IProgressMonitor monitor) {
		Bundle lib = Platform.getBundle("ru.bmstu.rk9.rao.lib");
		File libPath = null;
		try {
			libPath = FileLocator.getBundleFile(lib);
			if (libPath != null) {
				IJavaProject jProject = JavaCore.create(project);

				IClasspathEntry[] projectClassPathArray = jProject
						.getRawClasspath();

				IPath libPathBinary;
				if (libPath.isDirectory())
					libPathBinary = new Path(libPath.getAbsolutePath()
							+ "/bin/");
				else
					libPathBinary = new Path(libPath.getAbsolutePath());

				if (projectClassPathArray.length > 2) {
					if (!projectClassPathArray[2].getPath().equals(
							libPathBinary)) {
						projectClassPathArray[2] = JavaCore.newLibraryEntry(
								libPathBinary, null, null);
						jProject.setRawClasspath(projectClassPathArray, monitor);
					}
				} else {
					List<IClasspathEntry> projectClassPathList = new ArrayList<IClasspathEntry>(
							Arrays.asList(projectClassPathArray));
					IClasspathEntry libEntry = JavaCore.newLibraryEntry(
							libPathBinary, null, null);
					projectClassPathList.add(libEntry);

					jProject.setRawClasspath(
							(IClasspathEntry[]) projectClassPathList
									.toArray(new IClasspathEntry[projectClassPathList
											.size()]), monitor);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static Job build(final ExecutionEvent event,
			final EclipseResourceFileSystemAccess2 fsa,
			final IResourceSetProvider resourceSetProvider,
			final EclipseOutputConfigurationProvider ocp,
			final IMultipleResourceGenerator generator) {
		Job buildJob = new Job("Building Rao model") {
			protected IStatus run(IProgressMonitor monitor) {
				IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
				if (activeEditor == null)
					return new Status(Status.ERROR, "ru.bmstu.rk9.rao.ui",
							"Build failed: no editor opened.");

				final IProject project = getProject(activeEditor);
				if (project == null)
					return new Status(
							Status.ERROR,
							"ru.bmstu.rk9.rao.ui",
							"Build failed: file '"
									+ activeEditor.getTitle()
									+ "' is not a part of any project in workspace.");

				final Display display = PlatformUI.getWorkbench().getDisplay();
				IWorkbenchWindow workbenchWindow = HandlerUtil
						.getActiveWorkbenchWindow(event);

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
						if (editor.getResource().getProject().equals(project))
							return true;

						return false;
					}
				};

				display.syncExec(() -> PlatformUI.getWorkbench().saveAll(
						workbenchWindow, workbenchWindow, filter, true));

				checkProjectClassPath(project, monitor);

				IJobManager jobManager = Job.getJobManager();
				try {
					for (Job projectJob : jobManager.find(project.getName()))
						projectJob.join();
				} catch (Exception e) {
					e.printStackTrace();
				}

				final List<IResource> raoFiles = ModelBuilder
						.getAllRaoFilesInProject(project);
				if (raoFiles.isEmpty()) {
					return new Status(Status.ERROR, "ru.bmstu.rk9.rao.ui",
							"Build failed: project contains no rao files");
				}

				IFolder srcGenFolder = project.getFolder("src-gen");
				if (srcGenFolder.exists()) {
					try {
						for (IResource resource : srcGenFolder.members(true)) {
							resource.delete(true, new NullProgressMonitor());
						}
					} catch (CoreException e) {
						e.printStackTrace();
						return new Status(
								Status.ERROR,
								"ru.bmstu.rk9.rao.ui",
								"Build failed: could not delete src-gen folder",
								e);
					}
				} else {
					try {
						srcGenFolder.create(true, true, new NullProgressMonitor());
					} catch (CoreException e) {
						e.printStackTrace();
						return new Status(Status.ERROR, "ru.bmstu.rk9.rao.ui",
								"Build failed: could not create src-gen folder", e);
					}
				}

				fsa.setOutputPath(srcGenFolder.getFullPath().toString());

				fsa.setMonitor(monitor);
				fsa.setProject(project);

				Map<String, OutputConfiguration> outputConfigurations = new HashMap<String, OutputConfiguration>();

				for (OutputConfiguration oc : ocp
						.getOutputConfigurations(project))
					outputConfigurations.put(oc.getName(), oc);

				fsa.setOutputConfigurations(outputConfigurations);

				final ResourceSet resourceSet = resourceSetProvider
						.get(project);

				boolean projectHasErrors = false;

				for (IResource resource : raoFiles) {
					Resource loadedResource = resourceSet.getResource(
							getURI(resource), true);
					if (!loadedResource.getErrors().isEmpty()) {
						projectHasErrors = true;
						break;
					}
				}

				if (projectHasErrors) {
					try {
						srcGenFolder.delete(true, new NullProgressMonitor());
					} catch (CoreException e) {
						e.printStackTrace();
					}
					return new Status(Status.ERROR, "ru.bmstu.rk9.rao.ui",
							"Build failed: model has errors");
				}

				generator.doGenerate(resourceSet, fsa);

				try {
					project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD,
							monitor);
				} catch (CoreException e) {
					e.printStackTrace();
					return new Status(Status.ERROR, "ru.bmstu.rk9.rao.ui",
							"Build failed: could not build project", e);
				}

				try {
					IMarker[] markers = project.findMarkers(
							IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true,
							IResource.DEPTH_INFINITE);

					if (markers.length > 0) {
						String errorsDetails = "Project contains errors:";
						for (IMarker marker : markers) {
							errorsDetails += "\n\nfile "
									+ marker.getResource().getName()
									+ " at line "
									+ MarkerUtilities.getLineNumber(marker)
									+ ": " + MarkerUtilities.getMessage(marker);
						}
						return new Status(Status.ERROR, "ru.bmstu.rk9.rao.ui",
								errorsDetails);
					}
				} catch (CoreException e) {
					return new Status(
							Status.ERROR,
							"ru.bmstu.rk9.rao.ui",
							"Build failed: internal error whule calculating error markers",
							e);
				}

				return Status.OK_STATUS;
			}
		};

		buildJob.setPriority(Job.BUILD);
		return buildJob;
	}
}
