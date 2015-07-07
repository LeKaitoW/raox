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
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * Our sample action implements workbench action delegate. The action proxy will
 * be created by the workbench and shown in the UI. When the user tries to use
 * the action, this delegate will be created and execution will be delegated to
 * it.
 * 
 * @see IWorkbenchWindowActionDelegate
 */
public class Game5Action implements IWorkbenchWindowActionDelegate {
	/**
	 * The constructor.
	 */
	public Game5Action() {
	}

	/**
	 * The action has been activated. The argument of the method represents the
	 * 'real' action sitting in the workbench UI.
	 * 
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IProject game5Project = root.getProject("new_game5_project");

			if (!game5Project.exists()) {
				try {
					game5Project.create(null);
					game5Project.open(null);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			} else if (!game5Project.isOpen()) {
				try {
					game5Project.open(null);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}

			IPath workspacePath = root.getLocation();
			IProjectDescription description;
			IJavaProject game5JavaProject;

			try {
				description = game5Project.getDescription();
				description.setNatureIds(new String[] { JavaCore.NATURE_ID });
				game5Project.setDescription(description, null);

				game5JavaProject = JavaCore.create(game5Project);
				IFolder sourceFolder = game5Project.getFolder("src-gen");
				sourceFolder.create(false, true, null);

				List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
				IExecutionEnvironmentsManager executionEnvironmentsManager = JavaRuntime
						.getExecutionEnvironmentsManager();
				IExecutionEnvironment[] executionEnvironments = executionEnvironmentsManager
						.getExecutionEnvironments();

				for (IExecutionEnvironment iExecutionEnvironment : executionEnvironments) {
					if ("JavaSE-1.8".equals(iExecutionEnvironment.getId())) {
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
						entries.toArray(new IClasspathEntry[entries.size()]),
						null);
			} catch (CoreException e2) {
				e2.printStackTrace();
			}

			IPath filePath = workspacePath.append(game5Project.getFullPath())
					.append("game5Model.rdo");

			File game5Model = new File(filePath.toString());
			try {
				game5Model.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			IFile game5File = game5Project.getFile("game5Model.rdo");
			InputStream is;
			try {
				is = new java.io.FileInputStream(filePath.toFile());
				game5File.create(is, true, null);
			} catch (FileNotFoundException | CoreException e) {
				e.printStackTrace();
			}

			IWorkbenchPage page = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();

			IDE.openEditor(page, game5File);

			PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage()
					.openEditor(new Game5EditorInput(), Game5View.ID);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Selection in the workbench has been changed. We can change the state of
	 * the 'real' action here if we want, but this can only happen after the
	 * delegate has been created.
	 * 
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system resources we previously
	 * allocated.
	 * 
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to be able to provide parent shell
	 * for the message dialog.
	 * 
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
	}
}