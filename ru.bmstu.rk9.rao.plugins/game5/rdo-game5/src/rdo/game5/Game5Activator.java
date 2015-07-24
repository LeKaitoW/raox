package rdo.game5;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Game5Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "rdo-game5";
	private static Game5Activator plugin;

	public Game5Activator() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		IWorkbench workBench = PlatformUI.getWorkbench();
		workBench.addWorkbenchListener(new IWorkbenchListener() {

			@Override
			public boolean preShutdown(IWorkbench workBench, boolean arg1) {
				workBench.getActiveWorkbenchWindow().getActivePage()
						.closeAllEditors(true);
				return true;
			}

			@Override
			public void postShutdown(IWorkbench arg0) {
			}
		});
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static Game5Activator getDefault() {
		return plugin;
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
