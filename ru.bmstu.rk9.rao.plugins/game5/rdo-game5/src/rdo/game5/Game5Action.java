package rdo.game5;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class Game5Action implements IWorkbenchWindowActionDelegate {

	public Game5Action() {
	}

	public void run(IAction action) {
		ModelNameView.modelNameView();
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
	}
}