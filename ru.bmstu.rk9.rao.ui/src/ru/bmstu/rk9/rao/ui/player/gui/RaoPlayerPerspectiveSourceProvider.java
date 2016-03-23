package ru.bmstu.rk9.rao.ui.player.gui;

import java.util.HashMap;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

public class RaoPlayerPerspectiveSourceProvider extends AbstractSourceProvider {
	public final static String RaoPerspectiveKey = "ru.bmstu.rk9.rao.ui.handlers.showActions";
	private final static String RaoPerspective = "showActions";
	private final static String otherPerspective = "hideActions";

	private boolean isRaoPerspective = true;

	@Override
	public void dispose() {
	}

	@Override
	public HashMap<String, String> getCurrentState() {
		HashMap<String, String> currentState = new HashMap<String, String>(1);
		String currentState1 = isRaoPerspective ? RaoPerspective : otherPerspective;
		currentState.put(RaoPerspectiveKey, currentState1);
		return currentState;
	}

	@Override
	public String[] getProvidedSourceNames() {
		return new String[] { RaoPerspectiveKey };
	}

	public void perspectiveChanged(boolean _isRaoPerspective) {
		if (this.isRaoPerspective == _isRaoPerspective)
			return;

		this.isRaoPerspective = _isRaoPerspective;
		String currentState = isRaoPerspective ? RaoPerspective : otherPerspective;
		fireSourceChanged(ISources.WORKBENCH, RaoPerspectiveKey, currentState);
	}
}
