package ru.bmstu.rk9.rao.ui;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import ru.bmstu.rk9.rao.ui.highlightning.RaoHighlightningCalculator;
import ru.bmstu.rk9.rao.ui.player.gui.RaoPlayerPerspectiveAdapter;

public class RaoUiModule extends AbstractRaoUiModule {
	private void registerPerspectiveAdapter() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				RaoPerspectiveAdapter wsPerspectiveListener = new RaoPerspectiveAdapter();
				workbenchWindow.addPerspectiveListener(wsPerspectiveListener);
				RaoPlayerPerspectiveAdapter wsPlayerPerspectiveListener = new RaoPlayerPerspectiveAdapter();
				workbenchWindow.addPerspectiveListener(wsPlayerPerspectiveListener);

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
	public Class<? extends org.eclipse.xtext.ide.editor.syntaxcoloring.ISemanticHighlightingCalculator> bindIdeSemanticHighlightingCalculator() {
		return RaoHighlightningCalculator.class;
	}
}
