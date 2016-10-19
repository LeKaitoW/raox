package ru.bmstu.rk9.rao.ui.execution;

import java.io.File;
import java.io.IOException;
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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;
import org.eclipse.ui.IEditorPart;
import org.osgi.framework.Bundle;

public class BuildUtil {

	public enum BundleType {
		RAOX_LIB("ru.bmstu.rk9.rao.lib");

		BundleType(final String name) {
			this.name = name;
		}

		private final String name;
	}

	public enum Mode {
		PRODUCTION, DEVELOPMENT;
	}

	public static URI getURI(IResource resource) {
		return URI.createPlatformResourceURI(resource.getProject().getName() + "/" + resource.getProjectRelativePath(),
				true);
	}

	public static List<IResource> getAllFilesInProject(IProject project, String fileExtension) {
		List<IResource> allRaoFiles = new ArrayList<IResource>();
		if (!project.isAccessible())
			return allRaoFiles;
		IPath path = project.getLocation();
		recursiveFindFiles(allRaoFiles, path, ResourcesPlugin.getWorkspace().getRoot(), fileExtension);
		return allRaoFiles;
	}

	private static void recursiveFindFiles(List<IResource> allRaoFiles, IPath path, IWorkspaceRoot workspaceRoot,
			String fileExtension) {
		IContainer container = workspaceRoot.getContainerForLocation(path);
		try {
			IResource[] iResources;
			iResources = container.members();
			for (IResource resource : iResources) {
				if (fileExtension.equalsIgnoreCase(resource.getFileExtension()))
					allRaoFiles.add(resource);
				if (resource.getType() == IResource.FOLDER) {
					IPath tempPath = resource.getLocation();
					recursiveFindFiles(allRaoFiles, tempPath, workspaceRoot, fileExtension);
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

	static Mode getMode(final String libraryPath) {
		return libraryPath.contains("dropins") ? Mode.PRODUCTION : Mode.DEVELOPMENT;
	}

	@SuppressWarnings("serial")
	public static class BuildUtilException extends Exception {
		public BuildUtilException(final String message) {
			super(message);
		}
	}

	public static IClasspathEntry getJavaSeClasspathEntry() throws BuildUtilException {
		final IExecutionEnvironmentsManager executionEnvironmentsManager = JavaRuntime
				.getExecutionEnvironmentsManager();
		final IExecutionEnvironment[] executionEnvironments = executionEnvironmentsManager.getExecutionEnvironments();
		final String JavaSeVersion = "JavaSE-1.8";
		for (IExecutionEnvironment executionEnvironment : executionEnvironments)
			if (executionEnvironment.getId().equals(JavaSeVersion))
				return JavaCore.newContainerEntry(JavaRuntime.newJREContainerPath(executionEnvironment));

		throw new BuildUtilException(JavaSeVersion + " not found");
	}

	public static IClasspathEntry getRaoxClasspathEntry() throws BuildUtilException {
		final Bundle bundle = Platform.getBundle(BundleType.RAOX_LIB.name);
		try {
			final File libraryPath = FileLocator.getBundleFile(bundle);
			if (libraryPath == null)
				throw new BuildUtilException("Cannot locate bundle " + BundleType.RAOX_LIB.name);

			switch (getMode(libraryPath.toString())) {
			case PRODUCTION:
				return JavaCore.newVariableEntry(new Path("ECLIPSE_HOME/dropins/" + BundleType.RAOX_LIB.name + ".jar"),
						null, null);

			case DEVELOPMENT:
				final IPath libraryPathBinary = new Path(
						libraryPath.getAbsolutePath() + (libraryPath.isDirectory() ? "/bin/" : ""));
				final IPath sourcePath = new Path(libraryPath.getAbsolutePath());
				return JavaCore.newLibraryEntry(libraryPathBinary, sourcePath, null);

			default:
				throw new BuildUtilException("Undefined mode for bundle " + BundleType.RAOX_LIB.name);
			}

		} catch (IOException e) {
			e.printStackTrace();
			throw new BuildUtilException(
					"Classpath error for bundle " + BundleType.RAOX_LIB.name + ":\n" + e.getMessage());
		}
	}

	public static IClasspathEntry getXtendClasspathEntry() {
		return JavaCore.newContainerEntry(new Path("org.eclipse.xtend.XTEND_CONTAINER"));
	}

	static String setupRaoxClasspath(IProject project, IProgressMonitor monitor) {
		try {
			final IClasspathEntry raoxClasspathEntry = getRaoxClasspathEntry();
			if (getMode(raoxClasspathEntry.getPath().toString()) == Mode.PRODUCTION)
				return null;

			final IJavaProject jProject = JavaCore.create(project);
			final IClasspathEntry[] projectClassPathArray = jProject.getRawClasspath();
			final List<IClasspathEntry> projectClassPathList = new ArrayList<IClasspathEntry>(
					Arrays.asList(projectClassPathArray));

			for (IClasspathEntry classpathEntry : projectClassPathList) {
				if (classpathEntry.getPath().equals(raoxClasspathEntry.getPath())) {
					projectClassPathList.remove(classpathEntry);
					break;
				}
			}

			projectClassPathList.add(raoxClasspathEntry);
			jProject.setRawClasspath(projectClassPathList.toArray(new IClasspathEntry[projectClassPathList.size()]),
					monitor);
			return null;

		} catch (BuildUtilException e) {
			e.printStackTrace();
			return e.getMessage();
		} catch (JavaModelException e) {
			e.printStackTrace();
			return "Internal error while checking library " + BundleType.RAOX_LIB.name + ":\n" + e.getMessage();
		}
	}

	private static void addSrcGenToClassPath(IProject project, IFolder srcGenFolder, IProgressMonitor monitor)
			throws JavaModelException {
		IJavaProject jProject = JavaCore.create(project);

		IClasspathEntry[] projectClassPathArray;
		projectClassPathArray = jProject.getRawClasspath();
		List<IClasspathEntry> projectClassPathList = new ArrayList<IClasspathEntry>(
				Arrays.asList(projectClassPathArray));

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
	}

	static String checkSrcGen(IProject project, IFolder srcGenFolder, IProgressMonitor monitor, boolean clean) {
		try {
			if (!srcGenFolder.exists()) {
				srcGenFolder.create(true, true, new NullProgressMonitor());
			} else if (clean) {
				for (IResource resource : srcGenFolder.members(true))
					resource.delete(true, new NullProgressMonitor());
			}

			addSrcGenToClassPath(project, srcGenFolder, monitor);
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
