package ru.bmstu.rk9.rao.ui.wizard;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
		setTitle("Create a Rao Project");
		setDescription("Enter a project name");
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
		final GridData projectNameData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		projectNameText.setLayoutData(projectNameData);
		projectNameText.setFocus();

		final Group templateGroup = new Group(container, SWT.SHADOW_IN);
		final GridData templateData = new GridData(SWT.FILL, SWT.CENTER, true, false);
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

		final Button graphicProcessButton = new Button(templateGroup, SWT.RADIO);
		graphicProcessButton.setText("Графический редактор процессного подхода");
		templates.put(graphicProcessButton, TemplateType.GRAPHIC_PROCESS);

		projectNameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});

		setControl(container);
		setPageComplete(false);
	}

	private void validate() {
		String projectName = projectNameText.getText();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		if (projectName.isEmpty()) {
			setDescription("Enter a project name");
			setPageComplete(false);
			return;
		}
		if (!isValidJavaIdentifier(projectName)) {
			setDescription("Project name is not a valid Java identifier.");
			setPageComplete(false);
			return;
		}
		if (isJavaKeyword(projectName)) {
			setDescription("Project name can not be a Java keyword.");
			setPageComplete(false);
			return;
		}
		if (root.getProject(projectName).exists()) {
			setDescription("A project with this name already exists.");
			setPageComplete(false);
			return;
		}
		setDescription("Create a Rao project in the workspace.");
		setPageComplete(true);
	}

	public String getProjectName() {
		return projectNameText.getText();
	}

	public TemplateType getTemplate() {
		for (Button button : templates.keySet()) {
			if (button.getSelection())
				return templates.get(button);
		}
		throw new RaoWizardException("Template for button is not defined");
	}

	private static boolean isValidJavaIdentifier(String identifier) {
		if (!Character.isJavaIdentifierStart(identifier.charAt(0))) {
			return false;
		}
		for (int i = 1; i < identifier.length(); i++) {
			if (!Character.isJavaIdentifierPart(identifier.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	private static boolean isJavaKeyword(String keyword) {
		final String keywords[] = { "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
				"const", "continue", "default", "do", "double", "else", "enum", "extends", "false", "final", "finally",
				"float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long",
				"native", "new", "null", "package", "private", "protected", "public", "return", "short", "static",
				"strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "true", "try",
				"void", "volatile", "while" };
		return (Arrays.binarySearch(keywords, keyword) >= 0);
	}
}
