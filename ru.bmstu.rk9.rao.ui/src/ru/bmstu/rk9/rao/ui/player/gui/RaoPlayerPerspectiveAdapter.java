package ru.bmstu.rk9.rao.ui.player.gui;

import org.eclipse.ui.PlatformUI;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

import org.eclipse.ui.services.ISourceProviderService;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.PerspectiveAdapter;

public class RaoPlayerPerspectiveAdapter extends PerspectiveAdapter {

	@Override
	public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspectiveDescriptor) {
		super.perspectiveActivated(page, perspectiveDescriptor);
		boolean RaoPlayerPerspective = false;
		if (perspectiveDescriptor.getId().equals("ru.bmstu.rk9.rao.ui.player.perspective")) {
			RaoPlayerPerspective = true;
		}

		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		ISourceProviderService service = window.getService(ISourceProviderService.class);
		RaoPlayerPerspectiveSourceProvider sourceProvider = (RaoPlayerPerspectiveSourceProvider) service
				.getSourceProvider(RaoPlayerPerspectiveSourceProvider.RaoPerspectiveKey);

		if (RaoPlayerPerspective)
			sourceProvider.perspectiveChanged(true);
		else
			sourceProvider.perspectiveChanged(false);
	}

	@Override
	public void perspectiveDeactivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
	}
}
