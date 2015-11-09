package ru.bmstu.rk9.rao.ui.wizard;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

import ru.bmstu.rk9.rao.ui.wizard.ProjectConfigurator.ProjectWizardStatus;

public class RaoWizard extends BasicNewProjectResourceWizard implements IWorkbenchWizard, IExecutableExtension {

	protected RaoWizardPage wizardPage;
	private IConfigurationElement configurationElement;

	@Override
	public void addPages() {
		wizardPage = new RaoWizardPage("");
		addPage(wizardPage);
	};

	@Override
	public boolean performFinish() {
		final ProjectInfo info = new ProjectInfo(wizardPage.getProjectName(), wizardPage.getTemplate());
		final ProjectWizardStatus projectWizardStatus = new ProjectConfigurator(info).initializeProject();
		switch (projectWizardStatus) {
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
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	@Override
	public String getWindowTitle() {
		return "New Rao Project";
	}

	@Override
	public void setInitializationData(IConfigurationElement configurationElement, String propertyName, Object data) {
		this.configurationElement = configurationElement;
	}
}
