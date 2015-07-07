package rdo.game5;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
import org.eclipse.xtext.ui.XtextProjectHelper;

public class Game5ProjectConfigurator {

	static IProject game5Project;
	static IWorkspaceRoot root;
	static IFile game5File;

	public static final void ProjectInitilazation() {

		root = ResourcesPlugin.getWorkspace().getRoot();
		game5Project = root.getProject("new_game5_project");
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

	public static final void ProjectConfiguration() {

		IProjectDescription description;
		IJavaProject game5JavaProject;

		try {
			description = game5Project.getDescription();
			description.setNatureIds(new String[] { JavaCore.NATURE_ID,
					XtextProjectHelper.NATURE_ID });
			game5Project.setDescription(description, null);

			game5JavaProject = JavaCore.create(game5Project);
			IFolder sourceFolder = game5Project.getFolder("src-gen");
			sourceFolder.create(false, true, null);

			List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
			IExecutionEnvironmentsManager executionEnvironmentsManager = JavaRuntime
					.getExecutionEnvironmentsManager();
			IExecutionEnvironment[] executionEnvironments = executionEnvironmentsManager
					.getExecutionEnvironments();

			String JSEVersion = "JavaSE-1.8";
			for (IExecutionEnvironment iExecutionEnvironment : executionEnvironments) {
				if (iExecutionEnvironment.getId().equals(JSEVersion)) {
					entries.add(JavaCore.newContainerEntry(JavaRuntime
							.newJREContainerPath(iExecutionEnvironment)));
					break;
				}
			}

			IPath sourceFolderPath = sourceFolder.getFullPath();
			IClasspathEntry srcEntry = JavaCore
					.newSourceEntry(sourceFolderPath);
			entries.add(srcEntry);
			game5JavaProject.setRawClasspath(
					entries.toArray(new IClasspathEntry[entries.size()]), null);
		} catch (CoreException e2) {
			e2.printStackTrace();
		}
	}

	public static void FileCreation() {

		IPath workspacePath = root.getLocation();
		String modelName = "game5Model.rdo";
		IPath filePath = workspacePath.append(game5Project.getFullPath())
				.append(modelName);

		File game5Model = new File(filePath.toString());
		try {
			game5Model.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		game5File = game5Project.getFile(modelName);
		InputStream is;
		try {
			is = new java.io.FileInputStream(filePath.toFile());
			game5File.create(is, true, null);
		} catch (FileNotFoundException | CoreException e) {
			e.printStackTrace();
		}
	}
}
