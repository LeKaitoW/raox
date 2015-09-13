package ru.bmstu.rk9.rao.ui.wizard;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ru.bmstu.rk9.rao.ui.wizard.ProjectInfo.TemplateType;

public class RaoWizardPage extends WizardPage {

	private Composite container;
	private Text projectNameText;
	private final Map<Button, TemplateType> templates = new HashMap<Button, TemplateType>();

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

		projectNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
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
		templates.put(noTemplateButton, TemplateType.NO_TEMPLATE);
		noTemplateButton.setSelection(true);

		final Button barberSimpleButton = new Button(templateGroup, SWT.RADIO);
		barberSimpleButton.setText("Модель простейшей СМО");
		templates.put(barberSimpleButton, TemplateType.BARBER_SIMPLE);

		final Button barberEventButton = new Button(templateGroup, SWT.RADIO);
		barberEventButton.setText("Модель простейшей СМО на событиях");
		templates.put(barberEventButton, TemplateType.BARBER_EVENTS);

		final Button barberClientsButton = new Button(templateGroup, SWT.RADIO);
		barberClientsButton.setText("Модель простейшей СМО с клиентами");
		templates.put(barberClientsButton, TemplateType.BARBER_CLIENTS);

		projectNameText.addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (!projectNameText.getText().isEmpty())
					setPageComplete(true);
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});

		setControl(container);
		setPageComplete(false);
	}

	public String getProjectName() {
		return projectNameText.getText();
	}

	public TemplateType getTemplate() {
		for (Button button : templates.keySet()) {
			if (button.getSelection())
				return templates.get(button);
		}
		return null;
		// TODO throw exception
	}
}
