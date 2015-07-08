package rdo.game5;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public class ModelNameView {

	private static String name;

	public static final void modelNameView() {
		final Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
		shell.setLayout(new FillLayout());
		shell.setText("New model");

		final Label modelLabel = new Label(shell, SWT.NONE);
		modelLabel.setText("Model name:");
		final Text nameText = new Text(shell, SWT.NONE);

		final Button okButton = new Button(shell, SWT.NONE);
		okButton.setText("Ok");
		final Button cancelButton = new Button(shell, SWT.NONE);
		cancelButton.setText("Cancel");

		okButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				name = nameText.getText();
				shell.close();
				Game5ProjectConfigurator.initializeProject();
				Game5ProjectConfigurator.configProject();
				Game5ProjectConfigurator.createFile();

				final IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, Game5ProjectConfigurator.getFile());

					page.openEditor(new Game5EditorInput(), Game5View.ID);
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				shell.close();
			}
		});
		shell.open();
	}

	public static final String getName() {
		return name;
	}
}
