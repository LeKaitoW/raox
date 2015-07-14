package rdo.game5;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
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

public class Game5ProjectConfigurator {

	private static IProject game5Project;
	private static IWorkspaceRoot root;
	private static IFile game5File;
	private static IPath filePath;
	private static InputStream inputStream;
	private static final String modelTemplatePath = "/model_template/game_5.rao";

	public static final void initializeProject() {

		root = ResourcesPlugin.getWorkspace().getRoot();
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

	public static void createFile() {

		final IPath workspacePath = root.getLocation();
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
	}

	public static final IFile getFile() {
		return game5File;
	}

	public static final IPath getFilePath() {
		return filePath;
	}

	public static final IProject getProject() {
		return game5Project;
	}

	@SuppressWarnings("restriction")
	public static final void addHeuristicCode() throws IOException {

		inputStream = Game5ProjectConfigurator.class.getClassLoader()
				.getResourceAsStream(modelTemplatePath);
		OutputStream outputStream = null;
		final File game5Model = new File(Game5ProjectConfigurator.getFilePath()
				.toString());

		try {
			InputStreamReader inputStreamReader = new InputStreamReader(
					inputStream, "UTF-8");
			BufferedReader bufferedReader = new BufferedReader(
					inputStreamReader);
			outputStream = new FileOutputStream(game5Model);
			PrintStream printStream = new PrintStream(outputStream);

			String modelTemplateCode = bufferedReader.readLine();
			while (modelTemplateCode != null) {
				printStream.println(modelTemplateCode);
				modelTemplateCode = bufferedReader.readLine();
			}

			printStream.println();
			printStream.println(Game5View.getEditor().getEditablePart());
			printStream.flush();
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
