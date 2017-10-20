package ru.bmstu.rk9.raox.plugin.game5;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

import ru.bmstu.rk9.raox.plugin.game5.Game5ProjectConfigurator.ProjectWizardStatus;

public class Game5Wizard extends BasicNewProjectResourceWizard implements IWorkbenchWizard, IExecutableExtension {

	protected Game5WizardPage wizardPage;
	private IConfigurationElement configurationElement;

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	@Override
	public boolean performFinish() {
		final ProjectWizardStatus status = Game5ProjectConfigurator.initializeProject(wizardPage.getProjectName());
		switch (status) {
		case SUCCESS:
			updatePerspective(configurationElement);
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
		return "New Rao X Game5 Project";
	}

	@Override
	public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {
		configurationElement = cfig;
	}
}
