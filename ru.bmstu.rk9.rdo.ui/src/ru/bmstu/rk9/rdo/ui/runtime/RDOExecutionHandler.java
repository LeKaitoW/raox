package ru.bmstu.rk9.rdo.ui.runtime;

import java.io.PrintStream;

import java.lang.reflect.Method;

import java.net.URL;
import java.net.URLClassLoader;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import org.eclipse.ui.handlers.HandlerUtil;

import org.eclipse.xtext.builder.EclipseOutputConfigurationProvider;
import org.eclipse.xtext.builder.EclipseResourceFileSystemAccess2;

import org.eclipse.xtext.ui.resource.IResourceSetProvider;

import ru.bmstu.rk9.rdo.IMultipleResourceGenerator;

import com.google.inject.Inject;
import com.google.inject.Provider;


public class RDOExecutionHandler extends AbstractHandler
{
	@Inject
	private IMultipleResourceGenerator generator;
 
	@Inject
	private Provider<EclipseResourceFileSystemAccess2> fileAccessProvider;

	@Inject
	private IResourceSetProvider resourceSetProvider;

	@Inject
	private EclipseOutputConfigurationProvider outputConfigurationProvider;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		final Job build = ModelBuilder.build
		(
			event,
			fileAccessProvider.get(),
			resourceSetProvider,
			outputConfigurationProvider,
			generator
		);
		build.schedule();

		final IProject project = ModelBuilder.getProject(HandlerUtil.getActiveEditor(event));

		Job run = new Job(project.getName() + " execution")
		{
			protected IStatus run(IProgressMonitor monitor) 
			{			
				MessageConsole console = new MessageConsole("System Output", null);
				ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { console });
				ConsolePlugin.getDefault().getConsoleManager().showConsoleView(console);
				MessageConsoleStream stream = console.newMessageStream();

				System.setOut(new PrintStream(stream));
				System.setErr(new PrintStream(stream));

				String name = this.getName();
				this.setName(name + " (waiting for build to complete)");

				try
				{
					build.join();
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}

				this.setName(name);

				if (build.getResult() != Status.OK_STATUS)
					this.cancel();

				try
				{
					URL url = new URL("file://" + ResourcesPlugin.getWorkspace().
							getRoot().getLocation().toString() + "/" + project.getName() + "/bin/");
					URL[] urls = new URL[]{url};

					ClassLoader cl = new URLClassLoader(urls);

					Class<?> cls = cl.loadClass("rdo_model.MainClass");

					Method main = null;

					find_main:
					for (Method method : cls.getMethods())
						if (method.getName() == "main")
						{
							main = method;
							break find_main;
						}

					if (main != null)
					{
						main.invoke(null, (Object)null);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				return Status.OK_STATUS;
			}
		};
		run.setPriority(Job.LONG);
		run.schedule();

		return null;
	}
}
