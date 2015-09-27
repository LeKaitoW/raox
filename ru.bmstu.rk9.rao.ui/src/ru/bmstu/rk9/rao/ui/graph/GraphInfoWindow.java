package ru.bmstu.rk9.rao.ui.graph;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class GraphInfoWindow extends Shell {
	GraphInfoWindow(Display display) {
		setText("Graph Info");
		setLayout(new FillLayout());

		windowArea = new Composite(this, SWT.FILL);
		RowLayout windowAreaLayout = new RowLayout(numberOfAreas);
		windowAreaLayout.fill = true;
		windowAreaLayout.marginLeft = 5;
		windowAreaLayout.marginRight = 5;
		windowArea.setLayout(windowAreaLayout);

		infoArea = new Composite(windowArea, SWT.FILL);
		RowLayout infoAreaLayout = new RowLayout(SWT.VERTICAL);
		infoAreaLayout.spacing = 5;
		infoArea.setLayout(infoAreaLayout);

		graphInfoGroup = new Group(infoArea, SWT.NONE);
		graphInfoGroup.setText("Graph info");
		graphInfoGroup.setLayout(new FormLayout());

		Group selectedCellInfoGroup = new Group(graphInfoGroup, SWT.NONE);
		selectedCellInfoGroup.setText("Selected cell");
		FormData cellInfoGroupData = new FormData();
		cellInfoGroupData.left = new FormAttachment(0, 0);
		cellInfoGroupData.top = new FormAttachment(0, 5);
		selectedCellInfoGroup.setLayoutData(cellInfoGroupData);

		FillLayout cellInfoLayout = new FillLayout();
		cellInfoLayout.marginHeight = 2;
		cellInfoLayout.marginWidth = 4;
		selectedCellInfoGroup.setLayout(cellInfoLayout);
		cellInfoLabel = new Label(selectedCellInfoGroup, SWT.NONE);

		Group commonInfoGroup = new Group(graphInfoGroup, SWT.NONE);
		commonInfoGroup.setText("Common");
		FormData graphInfoGroupData = new FormData();
		graphInfoGroupData.left = new FormAttachment(selectedCellInfoGroup, 5);
		graphInfoGroupData.top = new FormAttachment(0, 5);
		commonInfoGroup.setLayoutData(graphInfoGroupData);

		FillLayout graphInfoLayout = new FillLayout();
		graphInfoLayout.marginHeight = 2;
		graphInfoLayout.marginWidth = 4;
		commonInfoGroup.setLayout(graphInfoLayout);
		graphInfoLabel = new Label(commonInfoGroup, SWT.NONE);

		buttonArea = new Composite(windowArea, SWT.FILL);
		buttonArea.setLayout(new FormLayout());

		buttonPrevious = new Button(buttonArea, SWT.ARROW | SWT.LEFT);
		buttonNext = new Button(buttonArea, SWT.ARROW | SWT.RIGHT);

		FormData buttonPreviousFormData = new FormData();
		buttonPreviousFormData.left = new FormAttachment(0, 5);
		buttonPrevious.setLayoutData(buttonPreviousFormData);

		FormData buttonNextFormData = new FormData();
		buttonNextFormData.left = new FormAttachment(buttonPrevious, 5);
		buttonNext.setLayoutData(buttonNextFormData);

		updateContents();
	}

	@Override
	protected void checkSubclass() {
	}

	public final void updateContents() {
		layout(true, true);
		pack();
	}

	private final static int numberOfAreas = 2;

	private final Composite windowArea;
	private final Composite infoArea;
	private final Composite buttonArea;

	private final Button buttonNext;
	private final Button buttonPrevious;

	public final Composite getInfoArea() {
		return infoArea;
	}

	public final Button getButtonNext() {
		return buttonNext;
	}

	public final Button getButtonPrevious() {
		return buttonPrevious;
	}

	private final Label cellInfoLabel;
	private final Label graphInfoLabel;
	private final Group graphInfoGroup;

	public final Label getCellInfoLabel() {
		return cellInfoLabel;
	}

	public final Label getGraphInfoLabel() {
		return graphInfoLabel;
	}

	public final Group getGraphInfoGroup() {
		return graphInfoGroup;
	}

}
