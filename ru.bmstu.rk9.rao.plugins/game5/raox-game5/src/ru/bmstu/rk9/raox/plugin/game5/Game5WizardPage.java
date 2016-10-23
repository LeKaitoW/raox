package ru.bmstu.rk9.raox.plugin.game5;

import java.util.Arrays;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class Game5WizardPage extends WizardPage {

	private Composite container;
	private Text projectNameText;

	protected Game5WizardPage(String pageName) {
		super(pageName);
		setTitle("Create a Rao X Game5 Project");
		setDescription("Enter a project name");
	}

	@Override
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(2, false));

		final Label projectNameLabel = new Label(container, SWT.NONE);
		projectNameLabel.setText("Project name:");

		projectNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		final GridData projectNameData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		projectNameText.setLayoutData(projectNameData);
		projectNameText.setFocus();

		projectNameText.addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(KeyEvent e) {
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
				if (projectName.equals("model")) {
					setDescription("\"model\" is an invalid name for Rao X project");
					setPageComplete(false);
					return;
				}
				if (root.getProject(projectName).exists()) {
					setDescription("A project with this name already exists.");
					setPageComplete(false);
					return;
				}
				setDescription("Create a Rao X Game5 project in the workspace.");
				setPageComplete(true);
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});

		setControl(container);
		setPageComplete(false);
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

	public String getProjectName() {
		return projectNameText.getText();
	}
}
