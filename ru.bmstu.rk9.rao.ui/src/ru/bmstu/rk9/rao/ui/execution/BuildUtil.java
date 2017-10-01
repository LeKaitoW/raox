package ru.bmstu.rk9.rao.ui.execution;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Entity;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.xtext.ui.XtextProjectHelper;
import org.osgi.framework.Bundle;
import org.osgi.service.prefs.BackingStoreException;

import ru.bmstu.rk9.rao.lib.persistence.QueryGenerator;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;

public class BuildUtil {

	private static final Logger logger = Logger.getLogger(BuildUtil.class);

	public enum BundleType {
		RAOX_LIB("ru.bmstu.rk9.rao.lib", "/bin/"), QUERYDSL_LIB("ru.bmstu.rk9.rao.querydsl",
				"/thirdparty/querydsl/lib/querydsl-jpa-4.1.3-apt-hibernate-one-jar.jar");

		BundleType(final String name, final String developmentPath) {
			this.name = name;
			this.developmentPath = developmentPath;
		}

		private final String name;
		private final String developmentPath;
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

	public static void initializeProjectSetup(IProject project) throws BuildUtilException {
		final ProjectScope projectScope = new ProjectScope(project);
		final IEclipsePreferences projectNode = projectScope.getNode("org.eclipse.core.resources");
		projectNode.node("encoding").put("<project>", "UTF-8");
		try {
			projectNode.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
			throw new BuildUtilException("Project encoding initialization failed" + ":\n" + e.getMessage());
		}

		try {
			final IProjectDescription description = project.getDescription();
			description.setNatureIds(new String[] { JavaCore.NATURE_ID, XtextProjectHelper.NATURE_ID });
			project.setDescription(description, null);
		} catch (CoreException e) {
			e.printStackTrace();
			throw new BuildUtilException("Project description failed" + ":\n" + e.getMessage());
		}

		final IFolder sourceFolder = project.getFolder("src-gen");
		try {
			sourceFolder.create(false, true, null);
		} catch (CoreException e) {
			e.printStackTrace();
			throw new BuildUtilException("Project source folder failed" + ":\n" + e.getMessage());
		}

		final List<IClasspathEntry> classpaths = new ArrayList<IClasspathEntry>();

		classpaths.add(JavaCore.newSourceEntry(sourceFolder.getFullPath()));
		classpaths.add(BuildUtil.getJavaCodeEntry(project));
		classpaths.add(BuildUtil.getJavaSeClasspathEntry());
		classpaths.add(BuildUtil.getXtendClasspathEntry());
		classpaths.add(BuildUtil.getRaoxClasspathEntry());
		classpaths.add(BuildUtil.getQuerydslClasspathEntry());

		final IJavaProject javaProject = JavaCore.create(project);
		try {
			javaProject.setRawClasspath(classpaths.toArray(new IClasspathEntry[classpaths.size()]), null);
		} catch (JavaModelException e) {
			e.printStackTrace();
			throw new BuildUtilException("Project set classpath failed" + ":\n" + e.getMessage());
		}
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

	private static IClasspathEntry getJavaSeClasspathEntry() throws BuildUtilException {
		final IExecutionEnvironmentsManager executionEnvironmentsManager = JavaRuntime
				.getExecutionEnvironmentsManager();
		final IExecutionEnvironment[] executionEnvironments = executionEnvironmentsManager.getExecutionEnvironments();
		final String JavaSeVersion = "JavaSE-1.8";
		for (IExecutionEnvironment executionEnvironment : executionEnvironments)
			if (executionEnvironment.getId().equals(JavaSeVersion))
				return JavaCore.newContainerEntry(JavaRuntime.newJREContainerPath(executionEnvironment));

		throw new BuildUtilException(JavaSeVersion + " not found");
	}

	private static IClasspathEntry getExternalEntry(BundleType bundleType) throws BuildUtilException {
		final Bundle bundle = Platform.getBundle(bundleType.name);
		try {
			final File libraryPath = FileLocator.getBundleFile(bundle);
			if (libraryPath == null)
				throw new BuildUtilException("Cannot locate bundle " + bundleType.name);

			switch (getMode(libraryPath.toString())) {
			case PRODUCTION:
				return JavaCore.newVariableEntry(new Path("ECLIPSE_HOME/dropins/" + bundleType.name + ".jar"), null,
						null);

			case DEVELOPMENT:
				final IPath libraryPathBinary = new Path(
						libraryPath.getAbsolutePath() + (libraryPath.isDirectory() ? bundleType.developmentPath : ""));
				final IPath sourcePath = new Path(libraryPath.getAbsolutePath());
				return JavaCore.newLibraryEntry(libraryPathBinary, sourcePath, null);

			default:
				throw new BuildUtilException("Undefined mode for bundle " + bundleType.name);
			}

		} catch (IOException e) {
			e.printStackTrace();
			throw new BuildUtilException("Classpath error for bundle " + bundleType.name + ":\n" + e.getMessage());
		}
	}

	private static IClasspathEntry getRaoxClasspathEntry() throws BuildUtilException {
		return getExternalEntry(BundleType.RAOX_LIB);
	}

	private static IClasspathEntry getQuerydslClasspathEntry() throws BuildUtilException {
		return getExternalEntry(BundleType.QUERYDSL_LIB);
	}

	private static IClasspathEntry getJavaCodeEntry(IProject project) throws BuildUtilException {
		final IFolder javaCodeFolder = project.getFolder("java");
		try {
			if (!javaCodeFolder.exists())
				javaCodeFolder.create(false, true, null);
		} catch (CoreException e) {
			e.printStackTrace();
			throw new BuildUtilException("Project java code folder creation failed" + ":\n" + e.getMessage());
		}
		return JavaCore.newSourceEntry(javaCodeFolder.getFullPath());
	}

	private static IClasspathEntry getXtendClasspathEntry() {
		return JavaCore.newContainerEntry(new Path("org.eclipse.xtend.XTEND_CONTAINER"));
	}

	static void updateClasspaths(IProject project, IProgressMonitor monitor) throws BuildUtilException {
		try {
			final IJavaProject javaProject = JavaCore.create(project);
			final List<IClasspathEntry> classpaths = new ArrayList<IClasspathEntry>(
					Arrays.asList(javaProject.getRawClasspath()));

			boolean updateXtendClasspath = false;
			boolean updateRaoxClasspath = false;
			// Данные элементы должны поддягиваться при ребилде, если их не было
			// ранее
			boolean updateQuerydslClasspath = true;
			boolean updateJavaCodeClasspath = true;
			Iterator<IClasspathEntry> it = classpaths.iterator();
			while (it.hasNext()) {
				final IClasspathEntry entry = it.next();
				final String path = entry.getPath().toString();
				if (path.contains("org.eclipse.xtext.xbase.lib")) {
					it.remove();
					updateXtendClasspath = true;
				} else if (path.contains(BundleType.RAOX_LIB.name)) {
					it.remove();
					updateRaoxClasspath = true;
				} else if (path.contains(BundleType.QUERYDSL_LIB.name)) {
					it.remove();
					updateQuerydslClasspath = true;
				} else if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE && path.contains("java")) {
					it.remove();
					updateJavaCodeClasspath = true;
				}
			}

			if (updateJavaCodeClasspath)
				classpaths.add(BuildUtil.getJavaCodeEntry(project));

			if (updateXtendClasspath)
				classpaths.add(getXtendClasspathEntry());

			if (updateRaoxClasspath)
				classpaths.add(getRaoxClasspathEntry());

			if (updateQuerydslClasspath)
				classpaths.add(getQuerydslClasspathEntry());

			javaProject.setRawClasspath(classpaths.toArray(new IClasspathEntry[classpaths.size()]), monitor);

		} catch (JavaModelException e) {
			e.printStackTrace();
			throw new BuildUtilException(
					"Internal error while checking library " + BundleType.RAOX_LIB.name + ":\n" + e.getMessage());
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

	public static String getProjectLocation(IProject project) throws MalformedURLException, CoreException {
		IProjectDescription description = project.getDescription();
		java.net.URI locationURI = description.getLocationURI();
		boolean useDefaultLocation = (locationURI == null);
		if (useDefaultLocation)
			return "file:///" + ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + "/"
					+ project.getName();
		else
			return locationURI.toURL().toString();
	}

	static void loadJavaClasses(IProject project, ClassLoader classLoader)
			throws URISyntaxException, CoreException, IOException, ClassNotFoundException {
		IPath root = project.getFolder("java").getLocation();
		List<IResource> resources = new ArrayList<IResource>();
		recursiveFindFiles(resources, root, ResourcesPlugin.getWorkspace().getRoot(), "java");

		for (IResource resource : resources) {
			String relative = resource.getLocation().makeRelativeTo(root).toString();
			String className = relative.replace(".java", "").replace('/', '.');
			Class.forName(className, true, classLoader);
			logger.debug("Loaded class " + className);
		}
	}

	static void loadQueryDslClasses(IProject project, ClassLoader classLoader)
			throws URISyntaxException, CoreException, IOException, ClassNotFoundException {
		IPath root = project.getFolder("java").getLocation();
		List<IResource> resources = new ArrayList<IResource>();
		recursiveFindFiles(resources, root, ResourcesPlugin.getWorkspace().getRoot(), "java");

		for (IResource resource : resources) {
			String relative = resource.getLocation().makeRelativeTo(root).toString();
			String className = relative.replace(".java", "").replace('/', '.');
			Class<?> entityClass = Class.forName(className, true, classLoader);
			logger.debug("Loaded class " + className);
			@SuppressWarnings("unchecked")
			Class<Entity> entityAnnotationClass = (Class<Entity>) Class.forName(Entity.class.getCanonicalName(), true,
					classLoader);
			if (!entityClass.isAnnotationPresent(entityAnnotationClass))
				continue;
			String queryClassName = className.replaceAll("\\.(?!.*\\.)", ".Q");
			Class.forName(queryClassName, true, classLoader);
			logger.debug("Loaded class " + queryClassName);
		}
	}

	static boolean generateQueryDslCode(IProject project, ClassLoader classLoader)
			throws URISyntaxException, CoreException, IOException, ClassNotFoundException {
		loadJavaClasses(project, classLoader);
		IPath root = project.getFolder("java").getLocation();
		IPath target = project.getFolder("src-gen").getLocation();
		QueryGenerator queryGenerator = new QueryGenerator(classLoader, target.toFile());

		List<IResource> resources = new ArrayList<IResource>();
		recursiveFindFiles(resources, root, ResourcesPlugin.getWorkspace().getRoot(), "java");

		boolean generatedAny = false;
		for (IResource resource : resources) {
			String relative = resource.getLocation().makeRelativeTo(root).toString();
			String className = relative.replace(".java", "").replace('/', '.');
			Class<?> entityClass = Class.forName(className, true, classLoader);
			@SuppressWarnings("unchecked")
			Class<Entity> entityAnnotationClass = (Class<Entity>) Class.forName(Entity.class.getCanonicalName(), true,
					classLoader);
			if (!entityClass.isAnnotationPresent(entityAnnotationClass))
				continue;
			queryGenerator.generate(entityClass);
			logger.debug("Generated Query class for " + className);
			generatedAny = true;
		}
		return generatedAny;
	}

	static URLClassLoader createClassLoader(IProject project) throws CoreException, MalformedURLException {
		String location = getProjectLocation(project);

		URL modelURL = new URL(location + "/bin/");

		URL[] urls = new URL[] { modelURL };
		return new URLClassLoader(urls, CurrentSimulator.class.getClassLoader());
	}
}
