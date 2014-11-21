package ru.bmstu.rk9.rdo.ui;

import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.console.IConsoleConstants;

import ru.bmstu.rk9.rdo.ui.contributions.RDOConsoleView;
import ru.bmstu.rk9.rdo.ui.contributions.RDOTraceView;
import ru.bmstu.rk9.rdo.ui.contributions.RDOStatusView;

public class RDOPerspective implements IPerspectiveFactory
{
	private IPageLayout factory;

	public RDOPerspective()
	{
		super();
	}

	@Override
	public void createInitialLayout(IPageLayout factory)
	{
		this.factory = factory;
		
		addAssociatedViews();
	}

	private void addAssociatedViews()
	{
		IFolderLayout topLeft =
				factory.createFolder(
					"topLeft", //NON-NLS-1
					IPageLayout.LEFT,
					0.2f,
					factory.getEditorArea());
			topLeft.addView(IPageLayout.ID_PROJECT_EXPLORER);

		IFolderLayout bottomLeft =
				factory.createFolder(
					"bottomLeft", //NON-NLS-1
					IPageLayout.BOTTOM,
					0.5f,
					"topLeft"); //NON-NLS-1
			bottomLeft.addView(IPageLayout.ID_OUTLINE);

			
		IFolderLayout bottom =
				factory.createFolder(
					"bottom", //NON-NLS-1
					IPageLayout.BOTTOM,
					0.7f,
					factory.getEditorArea());
			bottom.addView(RDOConsoleView.ID);
			bottom.addView(RDOTraceView.ID);
			bottom.addView(IPageLayout.ID_PROBLEM_VIEW);
			bottom.addView(IConsoleConstants.ID_CONSOLE_VIEW);

			IFolderLayout bottomRight =
					factory.createFolder(
						"bottomRight", //NON-NLS-1
						IPageLayout.RIGHT,
						0.75f,
						"bottom"); //NON-NLS-1
				bottomRight.addView(RDOStatusView.ID);
	}
}
