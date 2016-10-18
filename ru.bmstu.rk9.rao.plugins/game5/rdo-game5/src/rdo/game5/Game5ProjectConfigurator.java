package rdo.game5;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.xtext.ui.XtextProjectHelper;
import org.osgi.framework.Bundle;
import org.osgi.service.prefs.BackingStoreException;

public class Game5ProjectConfigurator {
	public static final String modelPath = "game_5.rao";
	public static final String configPath = "game_5.json";
	public static final String heuristicsPath = "heuristics.rao";

	public static final String modelTemplatePath = "/model_template/game_5.rao.template";
	public static final String configTemplatePath = "/model_template/config.json";

	enum ProjectWizardStatus {
		SUCCESS, UNDEFINED_ERROR
	}

	private static IProject game5Project;
	private static final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

	public static final ProjectWizardStatus initializeProject(String projectName) {
		final IServiceLocator serviceLocator = PlatformUI.getWorkbench();
		final IProgressMonitor iProgressMonitor = (IProgressMonitor) serviceLocator.getService(IProgressMonitor.class);
		game5Project = root.getProject(projectName);

		try {
			game5Project.create(iProgressMonitor);
			game5Project.open(iProgressMonitor);
			configureProject();

			createFile(modelPath);

			createFile(heuristicsPath);
			openFile(heuristicsPath, new ByteArrayInputStream("".getBytes()),
					PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(heuristicsPath).getId());

			createFile(configPath);
			openFile(configPath,
					Game5ProjectConfigurator.class.getClassLoader().getResourceAsStream(configTemplatePath),
					Game5View.ID);

			return ProjectWizardStatus.SUCCESS;
		} catch (CoreException | IOException | BackingStoreException e) {
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error",
					"Failed to create project:\n" + e.getMessage());
			e.printStackTrace();
			return ProjectWizardStatus.UNDEFINED_ERROR;
		}
	}

	private static final void configureProject() throws CoreException, BackingStoreException, IOException {
		IProjectDescription description;
		IJavaProject game5JavaProject;
		ProjectScope projectScope;
		IEclipsePreferences projectNode;

		projectScope = new ProjectScope(game5Project);
		projectNode = projectScope.getNode("org.eclipse.core.resources");
		projectNode.node("encoding").put("<project>", "UTF-8");
		projectNode.flush();

		description = game5Project.getDescription();
		description.setNatureIds(new String[] { JavaCore.NATURE_ID, XtextProjectHelper.NATURE_ID });
		game5Project.setDescription(description, null);

		game5JavaProject = JavaCore.create(game5Project);
		final IFolder sourceFolder = game5Project.getFolder("src-gen");
		sourceFolder.create(false, true, null);

		final List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
		final IExecutionEnvironmentsManager executionEnvironmentsManager = JavaRuntime
				.getExecutionEnvironmentsManager();
		final IExecutionEnvironment[] executionEnvironments = executionEnvironmentsManager.getExecutionEnvironments();

		final String JSEVersion = "JavaSE-1.8";
		for (IExecutionEnvironment iExecutionEnvironment : executionEnvironments) {
			if (iExecutionEnvironment.getId().equals(JSEVersion)) {
				entries.add(JavaCore.newContainerEntry(JavaRuntime.newJREContainerPath(iExecutionEnvironment)));
				break;
			}
		}
		final IPath sourceFolderPath = sourceFolder.getFullPath();
		final IClasspathEntry srcEntry = JavaCore.newSourceEntry(sourceFolderPath);
		entries.add(srcEntry);

		Bundle lib = Platform.getBundle("ru.bmstu.rk9.rao.lib");
		File libPath = FileLocator.getBundleFile(lib);
		IPath libPathBinary;
		if (libPath.isDirectory())
			libPathBinary = new Path(libPath.getAbsolutePath() + "/bin/");
		else
			libPathBinary = new Path(libPath.getAbsolutePath());
		IPath sourcePath = new Path(libPath.getAbsolutePath());
		IClasspathEntry libEntry = JavaCore.newLibraryEntry(libPathBinary, sourcePath, null);
		entries.add(libEntry);

		Bundle xbaseLib = Platform.getBundle("org.eclipse.xtext.xbase.lib");
		File xbaseLibPath = FileLocator.getBundleFile(xbaseLib);
		IPath xbaseLibPathBinary = new Path(xbaseLibPath.getAbsolutePath());
		IClasspathEntry xbaseLibEntry = JavaCore.newLibraryEntry(xbaseLibPathBinary, null, null);
		entries.add(xbaseLibEntry);

		game5JavaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);
	}

	private static void createFile(String name) throws IOException, CoreException {
		IPath path = root.getLocation().append(game5Project.getFullPath()).append(name);
		File file = new File(path.toString());
		file.createNewFile();
	}

	private static void openFile(String name, InputStream inputStream, String editorID)
			throws IOException, CoreException {
		final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IFile file = game5Project.getFile(name);
		file.create(inputStream, true, null);
		page.openEditor(new FileEditorInput(file), editorID);
	}
}
