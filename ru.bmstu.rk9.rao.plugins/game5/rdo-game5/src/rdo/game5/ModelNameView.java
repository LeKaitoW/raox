package rdo.game5;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

public class ModelNameView {

	private static String name;

	public static final void modelNameView() {
		final Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay(),
				SWT.SHELL_TRIM & (~SWT.RESIZE));
		shell.setLayout(new GridLayout(2, true));
		shell.setText("New model");

		final Label modelLabel = new Label(shell, SWT.NONE);
		modelLabel.setText("Model name:");
		final Text nameText = new Text(shell, SWT.NONE);

		final Label emptyNameLabel = new Label(shell, SWT.NONE);
		emptyNameLabel.setText("Empty name!");
		emptyNameLabel.setVisible(false);
		final Color red = new Color(PlatformUI.getWorkbench().getDisplay(),
				0x9B, 0x11, 0x1E);
		emptyNameLabel.setForeground(red);
		final GridData emptyNameData = new GridData(SWT.FILL, SWT.BEGINNING,
				true, false, 2, 1);
		emptyNameLabel.setLayoutData(emptyNameData);

		final Button okButton = new Button(shell, SWT.NONE);
		okButton.setText("Ok");
		shell.setDefaultButton(okButton);

		final Button cancelButton = new Button(shell, SWT.NONE);
		cancelButton.setText("Cancel");

		okButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				name = nameText.getText();
				if (!name.isEmpty()) {
					shell.close();
					Game5ProjectConfigurator.initializeProject();
				} else {
					emptyNameLabel.setVisible(true);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		nameText.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent arg0) {
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				emptyNameLabel.setVisible(false);
			}
		});

		cancelButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				shell.dispose();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		nameText.addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					shell.dispose();
				}
			}
		});

		shell.pack();
		shell.open();
		Rectangle shellBounds = PlatformUI.getWorkbench().getDisplay()
				.getBounds();
		Point dialogSize = shell.getSize();
		shell.setLocation(shellBounds.x + (shellBounds.width - dialogSize.x)
				/ 2, shellBounds.y + (shellBounds.height - dialogSize.y) / 2);
	}

	public static final String getName() {
		return name;
	}
}
