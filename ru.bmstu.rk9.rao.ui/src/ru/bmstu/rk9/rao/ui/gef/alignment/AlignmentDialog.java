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
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		gridLayout.makeColumnsEqualWidth = true;
		container.setLayout(gridLayout);
		// Массив всех кнопок
		List<Button> buttons = new ArrayList<>(9);
		// Данные по ячейке, в которой будет кнопка
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		for (Alignment alignmentVariant : Alignment.ordered) {
			Button button = new Button(container, SWT.TOGGLE);
			button.setLayoutData(gridData);
			button.setText(alignmentVariant.getDescription());
			buttons.add(button);
			// Устанавливаем кнопку, совпадающую с alignment нажатой
			if (alignmentVariant.equals(alignment)) {
				button.setSelection(true);
			}
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					// Не даем разжать кнопку
					if (alignment == alignmentVariant) {
						button.setSelection(true);
						return;
					}
					// Назначаем новое выравнивание
					alignment = alignmentVariant;
					// Убираем у остальных кнопок состояние нажато
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