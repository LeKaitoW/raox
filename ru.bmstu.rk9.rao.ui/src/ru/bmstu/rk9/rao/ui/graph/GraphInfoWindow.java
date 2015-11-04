package ru.bmstu.rk9.rao.ui.graph;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class GraphInfoWindow extends Dialog {
	protected GraphInfoWindow(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Graph Info");
	}

	@Override
	protected Control createButtonBar(final Composite parent) {
		return null;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		area.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		scrolledWindowArea = new ScrolledComposite(area, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FILL);
		scrolledWindowArea.setLayout(new FillLayout());
		scrolledWindowArea.setExpandHorizontal(true);
		scrolledWindowArea.setExpandVertical(true);

		windowArea = new Composite(scrolledWindowArea, SWT.FILL);
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

		graphInfoViewerWrapper = new InfoTableWrapper(infoArea);
		cellInfoViewerWrapper = new InfoTableWrapper(infoArea);

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

		scrolledWindowArea.setContent(windowArea);

		return area;
	}

	private ScrolledComposite scrolledWindowArea;
	private Composite windowArea;
	private Composite infoArea;
	private Composite buttonArea;

	private Button buttonNext;
	private Button buttonPrevious;

	public final Composite getInfoArea() {
		return infoArea;
	}

	public final Button getButtonNext() {
		return buttonNext;
	}

	public final Button getButtonPrevious() {
		return buttonPrevious;
	}

	private InfoTableWrapper cellInfoViewerWrapper;
	private InfoTableWrapper graphInfoViewerWrapper;

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
		cellInfoViewerWrapper.setInput(newInput);
	}

	public final void setGraphInfoInput(List<InfoElement> newInput) {
		graphInfoViewerWrapper.setInput(newInput);
	}

	private class InfoTableWrapper {
		public static final int infoKeyColumnWidth = 200;
		public static final int infoValueColumnWidth = 200;

		private final Composite composite;
		private final TableViewer tableViewer;

		private InfoTableWrapper(Composite parent) {
			composite = new Composite(parent, SWT.FILL | SWT.BORDER);
			tableViewer = new TableViewer(composite, SWT.FILL);

			TableViewerColumn keyColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			keyColumn.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					InfoElement cellInfo = (InfoElement) element;
					return cellInfo.getKey();
				}
			});

			TableViewerColumn valueColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			valueColumn.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					InfoElement cellInfo = (InfoElement) element;
					return cellInfo.getValue();
				}
			});

			tableViewer.setContentProvider(new ArrayContentProvider());

			TableColumnLayout tableLayout = new TableColumnLayout();
			tableLayout.setColumnData(keyColumn.getColumn(), new ColumnWeightData(0, infoKeyColumnWidth));
			tableLayout.setColumnData(valueColumn.getColumn(), new ColumnWeightData(0, infoValueColumnWidth));
			composite.setLayout(tableLayout);

			tableViewer.getTable().setLinesVisible(true);

			update();
		}

		private final void setInput(List<InfoElement> newInput) {
			tableViewer.setInput(newInput);
			tableViewer.refresh();
			update();
		}

		private final void update() {
			scrolledWindowArea.setMinSize(windowArea.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		}
	}
}
