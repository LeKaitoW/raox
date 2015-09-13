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
		setText("Cell Info");
		setLayout(new FillLayout());

		windowArea = new Composite(this, SWT.FILL);
		RowLayout windowAreaLayout = new RowLayout(numberOfAreas);
		windowAreaLayout.fill = true;
		windowArea.setLayout(windowAreaLayout);

		infoArea = new Composite(windowArea, SWT.FILL);
		infoArea.setLayout(new FormLayout());

		buttonArea = new Composite(windowArea, SWT.FILL);
		buttonArea.setLayout(new FormLayout());

		buttonPrevious = new Button(buttonArea, SWT.NONE);
		buttonPrevious.setText("Previous Cell");

		buttonNext = new Button(buttonArea, SWT.NONE);
		buttonNext.setText("Next Cell");

		FormData buttonNextFormData = new FormData();
		buttonNextFormData.right = new FormAttachment(100, -5);
		buttonNext.setLayoutData(buttonNextFormData);

		FormData buttonPreviousFormData = new FormData();
		buttonPreviousFormData.right = new FormAttachment(buttonNext, -5);
		buttonPrevious.setLayoutData(buttonPreviousFormData);

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
