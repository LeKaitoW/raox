package rdo.game5;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.xtext.ui.XtextProjectHelper;

public class Game5ProjectConfigurator {

	private static IProject game5Project;
	private static final IWorkspaceRoot root = ResourcesPlugin.getWorkspace()
			.getRoot();

	public static final void initializeProject() {

		game5Project = root.getProject(ModelNameView.getName());
		try {
			if (!game5Project.exists()) {
				game5Project.create(null);
				game5Project.open(null);
			} else if (!game5Project.isOpen())
				game5Project.open(null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		configureProject();
	}

	private static final void configureProject() {

		IProjectDescription description;
		IJavaProject game5JavaProject;

		try {
			description = game5Project.getDescription();
			description.setNatureIds(new String[] { JavaCore.NATURE_ID,
					XtextProjectHelper.NATURE_ID });
			game5Project.setDescription(description, null);

			game5JavaProject = JavaCore.create(game5Project);
			final IFolder sourceFolder = game5Project.getFolder("src-gen");
			sourceFolder.create(false, true, null);

			final List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
			final IExecutionEnvironmentsManager executionEnvironmentsManager = JavaRuntime
					.getExecutionEnvironmentsManager();
			final IExecutionEnvironment[] executionEnvironments = executionEnvironmentsManager
					.getExecutionEnvironments();

			final String JSEVersion = "JavaSE-1.8";
			for (IExecutionEnvironment iExecutionEnvironment : executionEnvironments) {
				if (iExecutionEnvironment.getId().equals(JSEVersion)) {
					entries.add(JavaCore.newContainerEntry(JavaRuntime
							.newJREContainerPath(iExecutionEnvironment)));
					break;
				}
			}

			final IPath sourceFolderPath = sourceFolder.getFullPath();
			final IClasspathEntry srcEntry = JavaCore
					.newSourceEntry(sourceFolderPath);
			entries.add(srcEntry);
			game5JavaProject.setRawClasspath(
					entries.toArray(new IClasspathEntry[entries.size()]), null);
		} catch (CoreException e2) {
			e2.printStackTrace();
		}
		final IFile configIFile = createConfigFile();
		createModelFile(configIFile);
	}

	@SuppressWarnings("resource")
	private static void createModelFile(IFile configIFile) {

		final String modelName = "game5.rao";
		IPath modelIPath = root.getLocation()
				.append(game5Project.getFullPath()).append(modelName);
		File modelFile = new File(modelIPath.toString());
		IFile modelIFile = game5Project.getFile(modelName);
		final String modelTemplatePath = "/model_template/game_5.rao";
		try {
			modelFile.createNewFile();
			InputStream inputStream = Game5ProjectConfigurator.class
					.getClassLoader().getResourceAsStream(modelTemplatePath);
			modelIFile.create(inputStream, true, null);
			OutputStream outputStream = new FileOutputStream(configIFile
					.getLocation().removeLastSegments(1).append("/game5.rao")
					.toString(), true);
			PrintStream printStream = new PrintStream(outputStream, true,
					StandardCharsets.UTF_8.name());
			final String configuration = ConfigurationParser
					.parseConfig(configIFile);
			printStream.print(configuration);
		} catch (CoreException | IOException e) {
			e.printStackTrace();
		}
	}

	private static final IFile createConfigFile() {

		final String configName = "game5.json";
		IPath configIPath = root.getLocation().append(
				game5Project.getFullPath().append(configName));
		final File configFile = new File(configIPath.toString());
		final String configFilePath = "/model_template/config.json";
		final IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		IFile configIFile = null; // exception pls?
		try {
			configFile.createNewFile();
			configIFile = game5Project.getFile(configName);
			InputStream inputStream = Game5ProjectConfigurator.class
					.getClassLoader().getResourceAsStream(configFilePath);
			configIFile.create(inputStream, true, null);
			page.openEditor(new FileEditorInput(configIFile), Game5View.ID);
		} catch (CoreException | IOException e) {
			e.printStackTrace();
		}
		return configIFile;
	}
}
