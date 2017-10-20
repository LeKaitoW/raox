package ru.bmstu.rk9.raox.plugin.game5;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;

public class Game5Handler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		WizardDialog wizardDialog = new WizardDialog(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell(), new Game5Wizard());
		wizardDialog.open();
		return null;
	}
}
