package ru.bmstu.rk9.rao.ui.build;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.URI;
import org.eclipse.ui.IEditorPart;

public class BuildUtil {
	public static URI getURI(IResource res) {
		return URI.createPlatformResourceURI(res.getProject().getName() + "/"
				+ res.getProjectRelativePath(), true);
	}

	public static List<IResource> getAllRaoFilesInProject(IProject project) {
		List<IResource> allRaoFiles = new ArrayList<IResource>();
		IPath path = project.getLocation();
		recursiveFindRaoFiles(allRaoFiles, path, ResourcesPlugin.getWorkspace()
				.getRoot());
		return allRaoFiles;
	}

	private static void recursiveFindRaoFiles(List<IResource> allRaoFiles,
			IPath path, IWorkspaceRoot workspaceRoot) {
		IContainer container = workspaceRoot.getContainerForLocation(path);
		try {
			IResource[] iResources;
			iResources = container.members();
			for (IResource iR : iResources) {
				if ("rao".equalsIgnoreCase(iR.getFileExtension()))
					allRaoFiles.add(iR);
				if (iR.getType() == IResource.FOLDER) {
					IPath tempPath = iR.getLocation();
					recursiveFindRaoFiles(allRaoFiles, tempPath, workspaceRoot);
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public static IProject getProject(IEditorPart activeEditor) {
		IFile file = (IFile) activeEditor.getEditorInput().getAdapter(
				IFile.class);
		if (file == null)
			return null;

		return file.getProject();
	}

}
