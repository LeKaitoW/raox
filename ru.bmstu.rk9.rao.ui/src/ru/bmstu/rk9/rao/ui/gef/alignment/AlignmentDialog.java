package ru.bmstu.rk9.rao.ui.gef.alignment;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class AlignmentDialog extends Dialog {

	private Alignment alignment;

	public AlignmentDialog(Shell parentShell) {
		super(parentShell);
	}

	public Alignment getAlignment() {
		return alignment;
	}

	public void setAlignment(Alignment alignment) {
		this.alignment = alignment;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		AlignmentDialog dialog = this;
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		gridLayout.makeColumnsEqualWidth = true;
		container.setLayout(gridLayout);

		List<Button> buttons = new ArrayList<>(Alignment.values().length);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		for (Alignment alignmentVariant : Alignment.values()) {
			Button button = new Button(container, SWT.TOGGLE);
			button.setLayoutData(gridData);
			button.setText(alignmentVariant.getDescription());
			buttons.add(button);

			if (dialog.alignment == alignmentVariant) {
				button.setSelection(true);
			}
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {

					if (alignment == alignmentVariant) {
						button.setSelection(true);
						return;
					}

					alignment = alignmentVariant;

					for (Button deselectButton : buttons) {
						if (!button.equals(deselectButton))
							deselectButton.setSelection(false);
					}
				}
			});
		}
		return container;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Alignment selection dialog");
	}

}
