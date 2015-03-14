package ru.bmstu.rk9.rdo.ui;

import org.eclipse.ui.PlatformUI;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

import org.eclipse.ui.services.ISourceProviderService;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.PerspectiveAdapter;

public class RDOPerspectiveAdapter extends PerspectiveAdapter {

	@Override
	public void perspectiveActivated(IWorkbenchPage page,
			IPerspectiveDescriptor perspectiveDescriptor) {
		super.perspectiveActivated(page, perspectiveDescriptor);
		boolean RDOPerspective = false;
		if (perspectiveDescriptor.getId().equals(
				"ru.bmstu.rk9.rdo.ui.perspective")) {
			RDOPerspective = true;
		}

		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		ISourceProviderService service = (ISourceProviderService) window
				.getService(ISourceProviderService.class);
		RDOPerspectiveSourceProvider sourceProvider = (RDOPerspectiveSourceProvider) service
				.getSourceProvider(RDOPerspectiveSourceProvider.RDOPerspectiveKey);

		if (RDOPerspective)
			sourceProvider.perspectiveChanged(true);
		else
			sourceProvider.perspectiveChanged(false);
	}

	public void perspectiveDeactivated(IWorkbenchPage page,
			IPerspectiveDescriptor perspective) {
	}
}