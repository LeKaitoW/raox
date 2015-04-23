package ru.bmstu.rk9.rdo.ui;

import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.console.IConsoleConstants;

import ru.bmstu.rk9.rdo.ui.contributions.RDOConsoleView;
import ru.bmstu.rk9.rdo.ui.contributions.RDOPlotView;
import ru.bmstu.rk9.rdo.ui.contributions.RDOResultsView;
import ru.bmstu.rk9.rdo.ui.contributions.RDOSerializationConfigView;
import ru.bmstu.rk9.rdo.ui.contributions.RDOSerializedObjectsView;
import ru.bmstu.rk9.rdo.ui.contributions.RDOTraceView;
import ru.bmstu.rk9.rdo.ui.contributions.RDOStatusView;

public class RDOPerspective implements IPerspectiveFactory {
	private IPageLayout factory;

	public RDOPerspective() {
		super();
	}

	@SuppressWarnings("restriction")
	@Override
	public void createInitialLayout(IPageLayout factory) {
		this.factory = factory;

		addAssociatedViews();

		// FIXME
		// editor area emits restricted access warning
		if (factory instanceof org.eclipse.ui.internal.e4.compatibility.ModeledPageLayout) {
			org.eclipse.ui.internal.e4.compatibility.ModeledPageLayout plotLayout = (org.eclipse.ui.internal.e4.compatibility.ModeledPageLayout) factory;
			plotLayout.stackView(RDOPlotView.ID + ":*",
					factory.getEditorArea(), false);
		}
	}

	private void addAssociatedViews() {
		IFolderLayout middleLeft = factory.createFolder("middleLeft",
				IPageLayout.LEFT, 0.2f, factory.getEditorArea());
		middleLeft.addView(RDOSerializedObjectsView.ID);

		IFolderLayout topLeft = factory.createFolder("topLeft", // NON-NLS-1
				IPageLayout.TOP, 0.6f, "middleLeft");
		topLeft.addView(IPageLayout.ID_PROJECT_EXPLORER);

		IFolderLayout bottomLeft = factory.createFolder("bottomLeft", // NON-NLS-1
				IPageLayout.BOTTOM, 0.6f, "topLeft"); // NON-NLS-1
		bottomLeft.addView(RDOSerializationConfigView.ID);

		IFolderLayout bottom = factory.createFolder("bottom", // NON-NLS-1
				IPageLayout.BOTTOM, 0.7f, factory.getEditorArea());
		bottom.addView(IPageLayout.ID_PROBLEM_VIEW);
		bottom.addView(IConsoleConstants.ID_CONSOLE_VIEW);
		bottom.addView(RDOConsoleView.ID);
		bottom.addView(RDOTraceView.ID);
		bottom.addView(RDOResultsView.ID);

		IFolderLayout bottomRight = factory.createFolder("bottomRight", // NON-NLS-1
				IPageLayout.RIGHT, 0.75f, "bottom"); // NON-NLS-1
		bottomRight.addView(RDOStatusView.ID);
		
	}
}
