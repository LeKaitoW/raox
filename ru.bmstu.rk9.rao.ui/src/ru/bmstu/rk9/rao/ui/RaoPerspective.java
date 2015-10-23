package ru.bmstu.rk9.rao.ui;

import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.console.IConsoleConstants;

import ru.bmstu.rk9.rao.ui.console.ConsoleView;
import ru.bmstu.rk9.rao.ui.plot.PlotView;
import ru.bmstu.rk9.rao.ui.results.ResultsView;
import ru.bmstu.rk9.rao.ui.serialization.SerializationConfigView;
import ru.bmstu.rk9.rao.ui.serialization.SerializedObjectsView;
import ru.bmstu.rk9.rao.ui.simulation.StatusView;
import ru.bmstu.rk9.rao.ui.trace.TraceView;

public class RaoPerspective implements IPerspectiveFactory {
	private IPageLayout factory;

	public RaoPerspective() {
		super();
	}

	@Override
	public void createInitialLayout(IPageLayout factory) {
		this.factory = factory;

		addAssociatedViews();
		setPlotLocation();
	}

	@SuppressWarnings("restriction")
	private final void setPlotLocation() {
		if (factory instanceof org.eclipse.ui.internal.e4.compatibility.ModeledPageLayout) {
			org.eclipse.ui.internal.e4.compatibility.ModeledPageLayout plotLayout = (org.eclipse.ui.internal.e4.compatibility.ModeledPageLayout) factory;
			plotLayout.stackView(PlotView.ID + ":*", factory.getEditorArea(),
					false);
		}
	}

	private void addAssociatedViews() {
		IFolderLayout middleLeft = factory.createFolder("middleLeft",
				IPageLayout.LEFT, 0.2f, factory.getEditorArea());
		middleLeft.addView(SerializedObjectsView.ID);

		IFolderLayout topLeft = factory.createFolder("topLeft",
				IPageLayout.TOP, 0.6f, "middleLeft");
		topLeft.addView(IPageLayout.ID_PROJECT_EXPLORER);

		IFolderLayout bottomLeft = factory.createFolder("bottomLeft",
				IPageLayout.BOTTOM, 0.6f, "topLeft");
		bottomLeft.addView(SerializationConfigView.ID);

		IFolderLayout bottom = factory.createFolder("bottom",
				IPageLayout.BOTTOM, 0.7f, factory.getEditorArea());
		bottom.addView(IPageLayout.ID_PROBLEM_VIEW);
		bottom.addView(IConsoleConstants.ID_CONSOLE_VIEW);
		bottom.addView(ConsoleView.ID);
		bottom.addView(TraceView.ID);
		bottom.addView(ResultsView.ID);
		bottom.addView(IPageLayout.ID_PROP_SHEET);

		IFolderLayout bottomRight = factory.createFolder("bottomRight",
				IPageLayout.RIGHT, 0.75f, "bottom");
		bottomRight.addView(StatusView.ID);

	}
}
