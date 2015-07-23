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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Game5ProjectConfigurator {

	private static IProject game5Project;
	private static IFile modelIFile;
	private static IPath modelIPath;
	private static IFile configIFile;
	private static IPath configIPath;
	private static File modelFile;
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
		modelIPath = workspacePath.append(game5Project.getFullPath()).append(
				modelName);
		modelFile = new File(modelIPath.toString());
		try {
			modelFile.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		modelIFile = game5Project.getFile(modelName);
		try {
			inputStream = Game5ProjectConfigurator.class.getClassLoader()
					.getResourceAsStream(modelTemplatePath);
			modelIFile.create(inputStream, true, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}

		parseConfig();
	}

	public static final void createConfigFile() {

		final String configName = ModelNameView.getName() + "_config.json";
		configIPath = workspacePath.append(game5Project.getFullPath().append(
				configName));
		final File configFile = new File(configIPath.toString());

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
		return modelIFile;
	}

	public static final IPath getModelFilePath() {
		return modelIPath;
	}

	public static final IFile getConfigFile() {
		return configIFile;
	}

	public static final IPath getConfigFilePath() {
		return configIPath;
	}

	public static final IProject getProject() {
		return game5Project;
	}

	@SuppressWarnings("restriction")
	public static final void addHeuristicCode() throws IOException {

		inputStream = Game5ProjectConfigurator.class.getClassLoader()
				.getResourceAsStream(modelTemplatePath);
		OutputStream outputStream = null;
		final File game5Model = new File(modelIPath.toString());

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

	public static void parseConfig() {
		JSONParser parser = new JSONParser();
		try {
			JSONObject object = (JSONObject) parser
					.parse(new FileReader(workspacePath.append(
							configIFile.getFullPath()).toString()));
			OutputStream outputStream = new FileOutputStream(modelFile, true);
			PrintStream printStream = new PrintStream(outputStream, true,
					StandardCharsets.UTF_8.name());
			JSONArray places = (JSONArray) object.get("places");

			printStream
					.print(String.format(
							"resource Фишка1 = Фишка.create(1, %1$s);\n"
									+ "resource Фишка2 = Фишка.create(2, %2$s);\n"
									+ "resource Фишка3 = Фишка.create(3, %3$s);\n"
									+ "resource Фишка4 = Фишка.create(4, %4$s);\n"
									+ "resource Фишка5 = Фишка.create(5, %5$s);\n"
									+ "resource Дырка = Дырка_t.create(%6$s);\n"
									+ "\n"
									+ "search Расстановка_фишек {\n"
									+ "	set init() {\n"
									+ "		setCondition(exist(Фишка: Фишка.Номер != Фишка.Местоположение));\n"
									+ "		setTerminateCondition(forAll(Фишка: Фишка.Номер == Фишка.Местоположение));\n"
									+ "		compareTops(%7$s);\n"
									+ "		evaluateBy(%8$s);\n"
									+ "	}\n"
									+ "	activity Перемещение_вправо checks Перемещение_фишки(Место_дырки.справа,  1).setValue%9$s(%10$s);\n"
									+ "	activity Перемещение_влево checks Перемещение_фишки(Место_дырки.слева,  -1).setValue%11$s(%12$s);\n"
									+ "	activity Перемещение_вверх checks Перемещение_фишки(Место_дырки.сверху,  -3).setValue%13$s(%14$s);\n"
									+ "	activity Перемещение_вниз checks Перемещение_фишки(Место_дырки.снизу,  3).setValue%15$s(%16$s);\n"
									+ "}\n", places.get(0), places.get(1),
							places.get(2), places.get(3), places.get(4),
							places.get(5), object.get("compare"),
							object.get("heuristic"),
							object.get("computeRight"),
							object.get("costRight"), object.get("computeLeft"),
							object.get("costLeft"), object.get("computeUp"),
							object.get("costUp"), object.get("computeDown"),
							object.get("costDown")));
			printStream.close();

		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}
}
