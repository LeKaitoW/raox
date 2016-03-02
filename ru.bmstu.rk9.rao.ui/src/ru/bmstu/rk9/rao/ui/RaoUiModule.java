package ru.bmstu.rk9.rao.ui;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.xtext.ui.editor.syntaxcoloring.ISemanticHighlightingCalculator;

import ru.bmstu.rk9.rao.ui.highlightning.RaoHighlightningCalculator;

@SuppressWarnings("deprecation")
public class RaoUiModule extends AbstractRaoUiModule {
	private void registerPerspectiveAdapter() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				RaoPerspectiveAdapter wsPerspectiveListener = new RaoPerspectiveAdapter();
				workbenchWindow.addPerspectiveListener(wsPerspectiveListener);

				wsPerspectiveListener.perspectiveActivated(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getPerspective());
			}
		});
	}

	public RaoUiModule(AbstractUIPlugin plugin) {
		super(plugin);
		registerPerspectiveAdapter();
	}

	@Override
	public Class<? extends ISemanticHighlightingCalculator> bindISemanticHighlightingCalculator() {
		return RaoHighlightningCalculator.class;
	}
}
