package ru.bmstu.rk9.rao.ui;

import org.eclipse.swt.widgets.Display;

import org.eclipse.ui.IWorkbenchWindow;

import org.eclipse.ui.PlatformUI;

import org.eclipse.ui.plugin.AbstractUIPlugin;

public class RaoUiModule extends ru.bmstu.rk9.rao.ui.AbstractRaoUiModule {
	private void registerPerspectiveAdapter() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				RaoPerspectiveAdapter wsPerspectiveListener = new RaoPerspectiveAdapter();
				workbenchWindow.addPerspectiveListener(wsPerspectiveListener);

				wsPerspectiveListener.perspectiveActivated(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getPerspective());
			}
		});
	}

	public RaoUiModule(AbstractUIPlugin plugin) {
		super(plugin);
		registerPerspectiveAdapter();
	}
}
