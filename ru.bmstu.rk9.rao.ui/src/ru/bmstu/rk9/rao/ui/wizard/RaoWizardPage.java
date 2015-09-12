package ru.bmstu.rk9.rao.ui.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class RaoWizardPage extends WizardPage {

	private Composite container;

	public RaoWizardPage(String pageName) {
		super(pageName);
	}

	@Override
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		final GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.verticalSpacing = 15;
		container.setLayout(gridLayout);

		final Label projectNameLabel = new Label(container, SWT.NONE);
		projectNameLabel.setText("Project name:");

		final Text projectNameText = new Text(container, SWT.BORDER
				| SWT.SINGLE);
		final GridData projectNameData = new GridData(SWT.FILL, SWT.CENTER,
				true, false);
		projectNameText.setLayoutData(projectNameData);
		projectNameText.setFocus();

		final Group templateGroup = new Group(container, SWT.SHADOW_IN);
		final GridData templateData = new GridData(SWT.FILL, SWT.CENTER, true,
				false);
		templateData.horizontalSpan = 2;
		templateGroup.setLayoutData(templateData);
		templateGroup.setText("Templates");
		templateGroup.setLayout(new GridLayout());

		final Button noTemplateButton = new Button(templateGroup, SWT.RADIO);
		noTemplateButton.setText("Пустая модель");
		noTemplateButton.setSelection(true);

		final Button barberSimpleButton = new Button(templateGroup, SWT.RADIO);
		barberSimpleButton.setText("Модель простейшей СМО");

		final Button barberEventButton = new Button(templateGroup, SWT.RADIO);
		barberEventButton.setText("Модель простейшей СМО на событиях");

		final Button barberClientsButton = new Button(templateGroup, SWT.RADIO);
		barberClientsButton.setText("Модель простейшей СМО с клиентами");

		setControl(container);
		setPageComplete(false);
	}
}
