package ru.bmstu.rk9.rdo.ui.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.emf.common.util.URI;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import org.eclipse.xtext.builder.EclipseOutputConfigurationProvider;
import org.eclipse.xtext.builder.EclipseResourceFileSystemAccess2;

import org.eclipse.xtext.generator.OutputConfiguration;

import org.eclipse.xtext.ui.resource.IResourceSetProvider;

import ru.bmstu.rk9.rdo.IMultipleResourceGenerator;


public class ModelBuilder
{
	public static URI getURI(IResource res)
	{
		return URI.createPlatformResourceURI(res.getProject().getName()
			+ "/" + res.getProjectRelativePath(), true);
	}
	
	public static ArrayList<IResource> getAllRDOFilesInProject(IProject project)
	{
		ArrayList<IResource> allRDOFiles = new ArrayList<IResource>();
		IPath path = project.getLocation();
		recursiveFindRDOFiles(allRDOFiles,path,ResourcesPlugin.getWorkspace().getRoot());
		return allRDOFiles;
	}
	
	private static void recursiveFindRDOFiles(ArrayList<IResource> allRDOFiles,IPath path, IWorkspaceRoot workspaceRoot)
	{
		IContainer container = workspaceRoot.getContainerForLocation(path);
		try
		{
			IResource[] iResources;
			iResources = container.members();
			for (IResource iR : iResources)
			{
				if ("rdo".equalsIgnoreCase(iR.getFileExtension()))
					allRDOFiles.add(iR);
				if (iR.getType() == IResource.FOLDER)
				{
					IPath tempPath = iR.getLocation();
					recursiveFindRDOFiles(allRDOFiles, tempPath, workspaceRoot);
				}
			}
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}
	}
	
	static IProject getProject(IEditorPart activeEditor)
	{
		IFile file = (IFile) activeEditor.getEditorInput().getAdapter(IFile.class);
		if (file != null)
			return file.getProject();
		else
			return null;
	}

	public static IMarker[] calculateCompilationErrorMarkers(IProject project)
	{
		ArrayList <IMarker> result = new ArrayList <IMarker>();
		IMarker[] markers = null;

		try
		{
			markers = project.findMarkers(null, true, IResource.DEPTH_INFINITE);
			for (IMarker marker: markers)
		    {
		        Integer severityType;
				severityType = (Integer) marker.getAttribute(IMarker.SEVERITY);
				if (severityType != null && severityType.intValue() == IMarker.SEVERITY_ERROR &&
						marker.getType().startsWith("ru.bmstu.rk9.rdo"))
					result.add(marker);
			}
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}

	    return result.toArray(new IMarker[result.size()]);
	}

	static Job build
	(
		final ExecutionEvent event,
		final EclipseResourceFileSystemAccess2 fsa,
		final IResourceSetProvider resourceSetProvider,
		final EclipseOutputConfigurationProvider ocp,
		final IMultipleResourceGenerator generator
	)
	{
		Job job = new Job("Building RDO model")
		{
			protected IStatus run(IProgressMonitor monitor)
			{
				IJobManager jobMan = Job.getJobManager();

				try
				{
					jobMan.join(ResourcesPlugin.FAMILY_AUTO_REFRESH, monitor);
					jobMan.join(ResourcesPlugin.FAMILY_AUTO_BUILD, monitor);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
				final IProject project = getProject(activeEditor);
								
		 		final List<IResource> projectFiles = ModelBuilder.getAllRDOFilesInProject(project);
				if (project != null)
				{
					IFolder srcGenFolder = project.getFolder("src-gen");
					if (!srcGenFolder.exists())
					{
						try
						{
							srcGenFolder.create(true, true,	new NullProgressMonitor());
						}
						catch (CoreException e)
						{
							return new Status(Status.ERROR, "ru.bmstu.rk9.rdo.ui", "Build failed", e);
						}
					}

					fsa.setOutputPath(srcGenFolder.getFullPath().toString());

					fsa.setMonitor(monitor);
					fsa.setProject(project);

		 			HashMap<String, OutputConfiguration> outputConfigurations =
		 				new HashMap<String,OutputConfiguration>();

		 			for(OutputConfiguration oc : ocp.getOutputConfigurations(project))
		 				outputConfigurations.put(oc.getName(), oc);

		 			fsa.setOutputConfigurations(outputConfigurations);
		 			
		 			final ResourceSet resourceSet = resourceSetProvider.get(project);
		 			
		 			boolean projectHasErrors = false;
		 			
		 			for (IResource res : projectFiles)
		 			{
		 				Resource loadedResource = resourceSet.getResource(getURI(res), true);
						if (loadedResource.getErrors().size() > 0)
							projectHasErrors = true;
		 			}

		 			if (calculateCompilationErrorMarkers(project).length > 0)
		 				projectHasErrors = true;
		 			
		 			if (projectHasErrors)
		 			{
		 				try
		 				{
							srcGenFolder.delete(true, new NullProgressMonitor());
						}
		 				catch (CoreException e)
		 				{
							e.printStackTrace();
						}
		 				return new Status(Status.ERROR, "ru.bmstu.rk9.rdo.ui", "Model has errors");
		 			}

					generator.doGenerate(resourceSet, fsa);

					try
					{
						project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
					}
					catch (CoreException e)
					{
						e.printStackTrace();
					}
				 }
				 return Status.OK_STATUS;
			}
		};

		job.setPriority(Job.BUILD);
		return job;		
	}
}
