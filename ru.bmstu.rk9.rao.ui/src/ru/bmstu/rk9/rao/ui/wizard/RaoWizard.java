package ru.bmstu.rk9.rao.ui.wizard;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

public class RaoWizard extends Wizard implements IWorkbenchWizard {

	protected RaoWizardPage wizardPage;

	public void addPages() {
		wizardPage = new RaoWizardPage("");
		addPage(wizardPage);
	};

	@Override
	public boolean performFinish() {
		ProjectInfo info = new ProjectInfo(wizardPage.getProjectName(),
				wizardPage.getTemplate());
		new ProjectConfigurator(info).initializeProject();
		return true;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

}
