package rdo.game5;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import rdo.game5.Game5ProjectConfigurator.ProjectWizardStatus;

public class Game5Wizard extends Wizard implements IWorkbenchWizard {

	protected Game5WizardPage wizardPage;

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	@Override
	public boolean performFinish() {
		final ProjectWizardStatus status = Game5ProjectConfigurator
				.initializeProject(wizardPage.getProjectName());
		switch (status) {
		case SUCCESS:
			return true;
		case UNDEFINED_ERROR:
			return false;
		default:
			return false;
		}
	}

	@Override
	public void addPages() {
		wizardPage = new Game5WizardPage("");
		addPage(wizardPage);
	}

	@Override
	public String getWindowTitle() {
		return "New Rao Game 5 Project";
	}
}
