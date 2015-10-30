package ru.bmstu.rk9.rao.ui.graph;

import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
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
	public static final int InfoKeyColumnWidth = 200;
	public static final int InfoValueColumnWidth = 200;

	GraphInfoWindow(Display display) {
		super(display, SWT.SHELL_TRIM);
		setText("Graph Info");
		setLayout(new FillLayout());

		windowArea = new Composite(this, SWT.FILL);
		RowLayout windowAreaLayout = new RowLayout(SWT.VERTICAL);
		windowAreaLayout.fill = true;
		windowAreaLayout.marginLeft = 5;
		windowAreaLayout.marginRight = 5;
		windowAreaLayout.spacing = 5;
		windowArea.setLayout(windowAreaLayout);

		infoArea = new Composite(windowArea, SWT.FILL);
		RowLayout infoAreaLayout = new RowLayout(SWT.VERTICAL);
		infoAreaLayout.fill = true;
		infoAreaLayout.spacing = 5;
		infoArea.setLayout(infoAreaLayout);

		Composite cellInfoComposite = new Composite(infoArea, SWT.FILL
				| SWT.BORDER);
		cellInfoViewer = new TableViewer(cellInfoComposite, SWT.FILL
				| SWT.H_SCROLL | SWT.V_SCROLL);
		configureInfoViewer(cellInfoViewer);

		Composite graphInfoComposite = new Composite(infoArea, SWT.FILL
				| SWT.BORDER);
		graphInfoViewer = new TableViewer(graphInfoComposite, SWT.FILL
				| SWT.H_SCROLL | SWT.V_SCROLL);
		configureInfoViewer(graphInfoViewer);

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

	private final void configureInfoViewer(TableViewer viewer) {
		TableViewerColumn keyColumn = new TableViewerColumn(viewer, SWT.NONE);
		keyColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				InfoElement cellInfo = (InfoElement) element;
				return cellInfo.getKey();
			}
		});

		TableViewerColumn valueColumn = new TableViewerColumn(viewer, SWT.NONE);
		valueColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				InfoElement cellInfo = (InfoElement) element;
				return cellInfo.getValue();
			}
		});

		viewer.setContentProvider(new ArrayContentProvider());

		TableColumnLayout tableLayout = new TableColumnLayout();
		viewer.getTable().getParent().setLayout(tableLayout);
		tableLayout.setColumnData(keyColumn.getColumn(), new ColumnWeightData(
				100, InfoKeyColumnWidth));
		tableLayout.setColumnData(valueColumn.getColumn(),
				new ColumnWeightData(0, InfoValueColumnWidth));

		viewer.getTable().setLinesVisible(true);
	}

	@Override
	protected void checkSubclass() {
	}

	public final void updateContents() {
		cellInfoViewer.refresh();
		graphInfoViewer.refresh();
		pack();
	}

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

	private final TableViewer cellInfoViewer;
	private final TableViewer graphInfoViewer;

	static class InfoElement {
		public InfoElement(String key, String value) {
			this.key = key;
			this.value = value;
		}

		public final String getKey() {
			return key;
		}

		public final String getValue() {
			return value;
		}

		private final String key;
		private final String value;
	}

	public final void setCellInfoInput(List<InfoElement> newInput) {
		cellInfoViewer.setInput(newInput);
		updateContents();
	}

	public final void setGraphInfoInput(List<InfoElement> newInput) {
		graphInfoViewer.setInput(newInput);
		updateContents();
	}
}
