
package ru.bmstu.rk9.rao.ui.player.gui;

import java.util.HashMap;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

public class RaoPlayerPerspectiveSourceProvider extends AbstractSourceProvider {
	public final static String RaoPerspectiveKey = "ru.bmstu.rk9.rao.ui.handlers.showPlayerActions";
	private final static String RaoPerspective = "showPlayerActions";
	private final static String otherPerspective = "hidePlayerActions";

	private boolean isRaoPlayerPerspective = true;

	@Override
	public void dispose() {
	}

	@Override
	public HashMap<String, String> getCurrentState() {
		HashMap<String, String> currentState = new HashMap<String, String>(1);
		String currentState1 = isRaoPlayerPerspective ? RaoPerspective : otherPerspective;
		currentState.put(RaoPerspectiveKey, currentState1);
		return currentState;
	}

	@Override
	public String[] getProvidedSourceNames() {
		return new String[] { RaoPerspectiveKey };
	}

	public void perspectiveChanged(boolean _isRaoPerspective) {
		if (this.isRaoPlayerPerspective == _isRaoPerspective)
			return;

		this.isRaoPlayerPerspective = _isRaoPerspective;
		String currentState = isRaoPlayerPerspective ? RaoPerspective : otherPerspective;
		fireSourceChanged(ISources.WORKBENCH, RaoPerspectiveKey, currentState);
	}
}
