package ru.bmstu.rk9.rao.ui.build;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.builder.EclipseOutputConfigurationProvider;
import org.eclipse.xtext.builder.EclipseResourceFileSystemAccess2;
import org.eclipse.xtext.generator.OutputConfiguration;
import org.eclipse.xtext.ui.resource.IResourceSetProvider;

import ru.bmstu.rk9.rao.IMultipleResourceGenerator;

public class ModelBuilder {
	public static URI getURI(IResource res) {
		return URI.createPlatformResourceURI(res.getProject().getName() + "/"
				+ res.getProjectRelativePath(), true);
	}

	public static ArrayList<IResource> getAllRaoFilesInProject(IProject project) {
		ArrayList<IResource> allRaoFiles = new ArrayList<IResource>();
		IPath path = project.getLocation();
		recursiveFindRaoFiles(allRaoFiles, path, ResourcesPlugin.getWorkspace()
				.getRoot());
		return allRaoFiles;
	}

	private static void recursiveFindRaoFiles(ArrayList<IResource> allRaoFiles,
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
		if (file != null)
			return file.getProject();
		else
			return null;
	}

	public static IMarker[] calculateCompilationErrorMarkers(IProject project) {
		ArrayList<IMarker> result = new ArrayList<IMarker>();
		IMarker[] markers = null;

		try {
			markers = project.findMarkers(null, true, IResource.DEPTH_INFINITE);
			for (IMarker marker : markers) {
				Integer severityType;
				severityType = (Integer) marker.getAttribute(IMarker.SEVERITY);
				if (severityType != null
						&& severityType.intValue() == IMarker.SEVERITY_ERROR
						&& marker.getType().startsWith("ru.bmstu.rk9.rao"))
					result.add(marker);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return result.toArray(new IMarker[result.size()]);
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
					ArrayList<IClasspathEntry> projectClassPathList = new ArrayList<IClasspathEntry>(
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
		Job job = new Job("Building Rao model") {
			protected IStatus run(IProgressMonitor monitor) {
				IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
				final IProject project = getProject(activeEditor);

				if (project != null) {
					checkProjectClassPath(project, monitor);

					IJobManager jobMan = Job.getJobManager();
					try {
						for (Job j : jobMan.find(project.getName()))
							j.join();
					} catch (Exception e) {
						e.printStackTrace();
					}

					final ArrayList<IResource> projectFiles = ModelBuilder
							.getAllRaoFilesInProject(project);
					IFolder srcGenFolder = project.getFolder("src-gen");
					if (!srcGenFolder.exists()) {
						try {
							srcGenFolder.create(true, true,
									new NullProgressMonitor());
						} catch (CoreException e) {
							return new Status(Status.ERROR,
									"ru.bmstu.rk9.rao.ui", "Build failed", e);
						}
					}

					fsa.setOutputPath(srcGenFolder.getFullPath().toString());

					fsa.setMonitor(monitor);
					fsa.setProject(project);

					HashMap<String, OutputConfiguration> outputConfigurations = new HashMap<String, OutputConfiguration>();

					for (OutputConfiguration oc : ocp
							.getOutputConfigurations(project))
						outputConfigurations.put(oc.getName(), oc);

					fsa.setOutputConfigurations(outputConfigurations);

					final ResourceSet resourceSet = resourceSetProvider
							.get(project);

					boolean projectHasErrors = false;

					for (IResource res : projectFiles) {
						Resource loadedResource = resourceSet.getResource(
								getURI(res), true);
						if (!loadedResource.getErrors().isEmpty())
							projectHasErrors = true;
					}

					if (calculateCompilationErrorMarkers(project).length > 0)
						projectHasErrors = true;

					if (projectHasErrors) {
						try {
							srcGenFolder
									.delete(true, new NullProgressMonitor());
						} catch (CoreException e) {
							e.printStackTrace();
						}
						return new Status(Status.ERROR, "ru.bmstu.rk9.rao.ui",
								"Model has errors");
					}

					generator.doGenerate(resourceSet, fsa);

					try {
						project.build(
								IncrementalProjectBuilder.INCREMENTAL_BUILD,
								monitor);
					} catch (CoreException e) {
						e.printStackTrace();
					}
				} else
					return new Status(
							Status.ERROR,
							"ru.bmstu.rk9.rao.ui",
							"File '"
									+ activeEditor.getTitle()
									+ "' is not a part of any project in workspace.");

				return Status.OK_STATUS;
			}
		};

		job.setPriority(Job.BUILD);
		return job;
	}
}
