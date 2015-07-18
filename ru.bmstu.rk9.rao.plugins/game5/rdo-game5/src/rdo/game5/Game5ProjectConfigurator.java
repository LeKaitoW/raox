package rdo.game5;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
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
import org.eclipse.xtext.ui.XtextProjectHelper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Game5ProjectConfigurator {

	private static IProject game5Project;
	private static IFile game5File;
	private static IPath filePath;
	private static IFile configIFile;
	private static IPath configPath;
	private static InputStream inputStream;
	private static final String modelTemplatePath = "/model_template/game_5.rao";
	private static final String configFilePath = "/model_template/config.json";
	private static final IWorkspaceRoot root = ResourcesPlugin.getWorkspace()
			.getRoot();
	private static final IPath workspacePath = root.getLocation();

	public static final void initializeProject() {

		game5Project = root.getProject(ModelNameView.getName() + "_project");
		try {
			if (!game5Project.exists()) {
				game5Project.create(null);
				game5Project.open(null);
			} else if (!game5Project.isOpen())
				game5Project.open(null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public static final void configureProject() {

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
	}

	public static void createModelFile() {

		final String modelName = ModelNameView.getName() + ".rao";
		filePath = workspacePath.append(game5Project.getFullPath()).append(
				modelName);
		final File game5Model = new File(filePath.toString());
		try {
			game5Model.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		game5File = game5Project.getFile(modelName);
		try {
			inputStream = Game5ProjectConfigurator.class.getClassLoader()
					.getResourceAsStream(modelTemplatePath);
			game5File.create(inputStream, true, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}

		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(new FileReader(workspacePath.append(
					configIFile.getFullPath()).toString()));
			JSONObject object = (JSONObject) obj;
			// parse here
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}

	public static final void createConfigFile() {

		final String configName = ModelNameView.getName() + "_config.json";
		configPath = workspacePath.append(game5Project.getFullPath().append(
				configName));
		final File configFile = new File(configPath.toString());

		try {
			configFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		configIFile = game5Project.getFile(configName);
		try {
			inputStream = Game5ProjectConfigurator.class.getClassLoader()
					.getResourceAsStream(configFilePath);
			configIFile.create(inputStream, true, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public static final IFile getModelFile() {
		return game5File;
	}

	public static final IPath getModelFilePath() {
		return filePath;
	}

	public static final IFile getConfigFile() {
		return configIFile;
	}

	public static final IPath getConfigPath() {
		return configPath;
	}

	public static final IProject getProject() {
		return game5Project;
	}

	@SuppressWarnings("restriction")
	public static final void addHeuristicCode() throws IOException {

		inputStream = Game5ProjectConfigurator.class.getClassLoader()
				.getResourceAsStream(modelTemplatePath);
		OutputStream outputStream = null;
		final File game5Model = new File(filePath.toString());

		try {
			InputStreamReader inputStreamReader = new InputStreamReader(
					inputStream, StandardCharsets.UTF_8.name());
			BufferedReader bufferedReader = new BufferedReader(
					inputStreamReader);
			outputStream = new FileOutputStream(game5Model);
			PrintStream printStream = new PrintStream(outputStream, true,
					StandardCharsets.UTF_8.name());

			String modelTemplateCode = bufferedReader.readLine();
			while (modelTemplateCode != null) {
				printStream.println(modelTemplateCode);
				modelTemplateCode = bufferedReader.readLine();
			}

			printStream.println();
			printStream.println(Game5View.getEditor().getEditablePart());
			printStream.close();

			Game5ProjectConfigurator.getProject().refreshLocal(
					IResource.DEPTH_INFINITE, null);
		} catch (FileNotFoundException | CoreException e) {
			e.printStackTrace();
		} finally {
			inputStream.close();
			if (outputStream != null) {
				outputStream.flush();
				outputStream.close();
			}
		}
	}
}
