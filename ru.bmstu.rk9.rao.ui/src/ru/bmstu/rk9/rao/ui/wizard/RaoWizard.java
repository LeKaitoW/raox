package ru.bmstu.rk9.rao.ui.wizard;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import ru.bmstu.rk9.rao.ui.wizard.ProjectConfigurator.ProjectWizardStatus;

public class RaoWizard extends Wizard implements IWorkbenchWizard {

	protected RaoWizardPage wizardPage;

	public void addPages() {
		wizardPage = new RaoWizardPage("");
		addPage(wizardPage);
	};

	@Override
	public boolean performFinish() {
		final ProjectInfo info = new ProjectInfo(wizardPage.getProjectName(),
				wizardPage.getTemplate());
		final ProjectWizardStatus projectWizardStatus = new ProjectConfigurator(
				info).initializeProject();
		switch (projectWizardStatus) {
		case SUCCESS:
			return true;
		case UNDEFINED_ERROR:
			return false;
		default:
			return false;
		}
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	@Override
	public String getWindowTitle() {
		return "New Rao Project";
	}
}
