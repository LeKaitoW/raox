package ru.bmstu.rk9.raox.plugin.game5;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.services.IServiceLocator;

import ru.bmstu.rk9.rao.ui.execution.BuildUtil;
import ru.bmstu.rk9.rao.ui.execution.BuildUtil.BuildUtilException;

public class Game5ProjectConfigurator {
	public static final String modelPath = "game_5.rao";
	public static final String configPath = "game_5.json";
	public static final String heuristicsPath = "heuristics.rao";

	public static final String modelTemplatePath = "/model_template/game_5.rao.template";
	public static final String configTemplatePath = "/model_template/config.json";

	enum ProjectWizardStatus {
		SUCCESS, UNDEFINED_ERROR
	}

	private static IProject project;
	private static final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

	public static final ProjectWizardStatus initializeProject(String projectName) {
		final IServiceLocator serviceLocator = PlatformUI.getWorkbench();
		final IProgressMonitor iProgressMonitor = serviceLocator.getService(IProgressMonitor.class);
		project = root.getProject(projectName);

		try {
			project.create(iProgressMonitor);
			project.open(iProgressMonitor);
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
		} catch (CoreException | IOException | BuildUtilException e) {
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error",
					"Failed to create project:\n" + e.getMessage());
			e.printStackTrace();
			return ProjectWizardStatus.UNDEFINED_ERROR;
		}
	}

	private static final void configureProject() throws BuildUtilException {
		BuildUtil.initializeProjectSetup(project);
	}

	private static void createFile(String name) throws IOException, CoreException {
		IPath path = root.getLocation().append(project.getFullPath()).append(name);
		File file = new File(path.toString());
		file.createNewFile();
	}

	private static void openFile(String name, InputStream inputStream, String editorID)
			throws IOException, CoreException {
		final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IFile file = project.getFile(name);
		file.create(inputStream, true, null);
		page.openEditor(new FileEditorInput(file), editorID);
	}
}
