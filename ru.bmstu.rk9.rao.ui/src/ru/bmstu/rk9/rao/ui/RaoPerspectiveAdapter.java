package ru.bmstu.rk9.rao.ui;

import org.eclipse.ui.PlatformUI;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

import org.eclipse.ui.services.ISourceProviderService;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.PerspectiveAdapter;

public class RaoPerspectiveAdapter extends PerspectiveAdapter {

	@Override
	public void perspectiveActivated(IWorkbenchPage page,
			IPerspectiveDescriptor perspectiveDescriptor) {
		super.perspectiveActivated(page, perspectiveDescriptor);
		boolean RaoPerspective = false;
		if (perspectiveDescriptor.getId().equals(
				"ru.bmstu.rk9.rao.ui.perspective")) {
			RaoPerspective = true;
		}

		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		ISourceProviderService service = (ISourceProviderService) window
				.getService(ISourceProviderService.class);
		RaoPerspectiveSourceProvider sourceProvider = (RaoPerspectiveSourceProvider) service
				.getSourceProvider(RaoPerspectiveSourceProvider.RaoPerspectiveKey);

		if (RaoPerspective)
			sourceProvider.perspectiveChanged(true);
		else
			sourceProvider.perspectiveChanged(false);
	}

	public void perspectiveDeactivated(IWorkbenchPage page,
			IPerspectiveDescriptor perspective) {
	}
}