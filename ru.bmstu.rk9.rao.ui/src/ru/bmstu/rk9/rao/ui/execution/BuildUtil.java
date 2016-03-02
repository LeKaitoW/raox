package ru.bmstu.rk9.rao.ui.execution;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.IEditorPart;
import org.osgi.framework.Bundle;

public class BuildUtil {

	public enum BundleType {
		RAO_LIB("ru.bmstu.rk9.rao.lib"), XBASE_LIB("org.eclipse.xtext.xbase.lib");

		BundleType(String bundleName) {
			this.name = bundleName;
		}

		private final String name;
	}

	public static URI getURI(IResource resource) {
		return URI.createPlatformResourceURI(resource.getProject().getName() + "/" + resource.getProjectRelativePath(),
				true);
	}

	public static List<IResource> getAllRaoFilesInProject(IProject project) {
		List<IResource> allRaoFiles = new ArrayList<IResource>();
		if (!project.isAccessible())
			return allRaoFiles;
		IPath path = project.getLocation();
		recursiveFindRaoFiles(allRaoFiles, path, ResourcesPlugin.getWorkspace().getRoot());
		return allRaoFiles;
	}

	private static void recursiveFindRaoFiles(List<IResource> allRaoFiles, IPath path, IWorkspaceRoot workspaceRoot) {
		IContainer container = workspaceRoot.getContainerForLocation(path);
		try {
			IResource[] iResources;
			iResources = container.members();
			for (IResource resource : iResources) {
				if ("rao".equalsIgnoreCase(resource.getFileExtension()))
					allRaoFiles.add(resource);
				if (resource.getType() == IResource.FOLDER) {
					IPath tempPath = resource.getLocation();
					recursiveFindRaoFiles(allRaoFiles, tempPath, workspaceRoot);
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	static IProject getProject(IEditorPart activeEditor) {
		IFile file = activeEditor.getEditorInput().getAdapter(IFile.class);
		if (file == null)
			return null;

		return file.getProject();
	}

	static String checkLib(IProject project, IProgressMonitor monitor, BundleType bundle) {
		String bundleName = bundle.name;
		Bundle lib = Platform.getBundle(bundleName);
		try {
			File libPath = FileLocator.getBundleFile(lib);
			if (libPath == null)
				return "cannot locate bundle " + bundleName;

			IJavaProject jProject = JavaCore.create(project);

			IClasspathEntry[] projectClassPathArray = jProject.getRawClasspath();

			IPath libPathBinary;
			if (libPath.isDirectory())
				libPathBinary = new Path(libPath.getAbsolutePath() + "/bin/");
			else
				libPathBinary = new Path(libPath.getAbsolutePath());
			IPath sourcePath = new Path(libPath.getAbsolutePath());

			List<IClasspathEntry> projectClassPathList = new ArrayList<IClasspathEntry>(
					Arrays.asList(projectClassPathArray));

			for (IClasspathEntry classpathEntry : projectClassPathList) {
				if (classpathEntry.getPath().equals(libPathBinary)) {
					projectClassPathList.remove(classpathEntry);
					break;
				}
			}

			jProject.setRawClasspath(projectClassPathList.toArray(new IClasspathEntry[projectClassPathList.size()]),
					monitor);

			IClasspathEntry libEntry = JavaCore.newLibraryEntry(libPathBinary, sourcePath, null);
			projectClassPathList.add(libEntry);

			jProject.setRawClasspath(projectClassPathList.toArray(new IClasspathEntry[projectClassPathList.size()]),
					monitor);
		} catch (Exception e) {
			e.printStackTrace();
			return "internal error while checking lib:\n" + e.getMessage();
		}

		return null;
	}

	static String checkSrcGen(IProject project, IFolder srcGenFolder, IProgressMonitor monitor) {
		IJavaProject jProject = JavaCore.create(project);
		try {
			IClasspathEntry[] projectClassPathArray;
			projectClassPathArray = jProject.getRawClasspath();
			List<IClasspathEntry> projectClassPathList = new ArrayList<IClasspathEntry>(
					Arrays.asList(projectClassPathArray));

			if (srcGenFolder.exists()) {
				for (IResource resource : srcGenFolder.members(true))
					resource.delete(true, new NullProgressMonitor());
			} else {
				srcGenFolder.create(true, true, new NullProgressMonitor());
			}

			boolean srcGenInClasspath = false;
			for (IClasspathEntry classpathEntry : projectClassPathArray) {
				if (classpathEntry.getPath().equals(srcGenFolder.getFullPath())) {
					srcGenInClasspath = true;
					break;
				}
			}

			if (!srcGenInClasspath) {
				IClasspathEntry libEntry = JavaCore.newSourceEntry(srcGenFolder.getFullPath(), null, null);
				projectClassPathList.add(libEntry);

				jProject.setRawClasspath(projectClassPathList.toArray(new IClasspathEntry[projectClassPathList.size()]),
						monitor);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "internal error while checking src-gen:\n" + e.getMessage();
		}

		return null;
	}

	static String createErrorMessage(String message) {
		return "Build failed: " + message;
	}
}
