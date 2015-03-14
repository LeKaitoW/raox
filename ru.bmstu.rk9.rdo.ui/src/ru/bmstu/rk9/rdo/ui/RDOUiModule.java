package ru.bmstu.rk9.rdo.ui;

import org.eclipse.swt.widgets.Display;

import org.eclipse.ui.IWorkbenchWindow;

import org.eclipse.ui.PlatformUI;

import org.eclipse.ui.plugin.AbstractUIPlugin;

public class RDOUiModule extends ru.bmstu.rk9.rdo.ui.AbstractRDOUiModule {
	private void registerPerspectiveAdapter() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow();
				RDOPerspectiveAdapter wsPerspectiveListener = new RDOPerspectiveAdapter();
				workbenchWindow.addPerspectiveListener(wsPerspectiveListener);

				wsPerspectiveListener.perspectiveActivated(PlatformUI
						.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage(), PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage()
						.getPerspective());
			}
		});
	}

	public RDOUiModule(AbstractUIPlugin plugin) {
		super(plugin);
		registerPerspectiveAdapter();
	}
}
