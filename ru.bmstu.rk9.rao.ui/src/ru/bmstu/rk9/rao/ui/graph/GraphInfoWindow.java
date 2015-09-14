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
		infoArea.setLayout(new FormLayout());

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

	public final void updateContents() {
		layout(true, true);
		pack();
	}

	@Override
	protected void checkSubclass() {
	}

	public final Composite getInfoArea() {
		return infoArea;
	}

	public final Button getButtonNext() {
		return buttonNext;
	}

	public final Button getButtonPrevious() {
		return buttonPrevious;
	}

	private final Composite windowArea;

	private final static int numberOfAreas = 2;

	private final Composite infoArea;
	private final Composite buttonArea;

	private final Button buttonNext;
	private final Button buttonPrevious;
}
