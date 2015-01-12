package ru.bmstu.rk9.rdo.ui;

import java.util.HashMap;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

public class RDOPerspectiveSourceProvider extends AbstractSourceProvider
{
	public final static String RDOPerspectiveKey = "ru.bmstu.rk9.rdo.ui.handlers.showActions";
	private final static String RDOPerspective = "showActions";
	private final static String otherPerspective = "hideActions";

	private boolean isRDOPerspective = true;

	@Override
	public void dispose()
	{}

	@Override
	public HashMap<String, String> getCurrentState()
	{
		HashMap<String, String> currentState = new HashMap<String, String>(1);
		String currentState1 = isRDOPerspective ? RDOPerspective : otherPerspective;
		currentState.put(RDOPerspectiveKey, currentState1);
		return currentState;
	}

	@Override
	public String[] getProvidedSourceNames()
	{
    	return new String[] { RDOPerspectiveKey };
	}

	public void perspectiveChanged(boolean _isRDOPerspective)
	{
    	if(this.isRDOPerspective == _isRDOPerspective)
    		return;

    	this.isRDOPerspective = _isRDOPerspective;
    	String currentState = isRDOPerspective ? RDOPerspective : otherPerspective;
    	fireSourceChanged(ISources.WORKBENCH, RDOPerspectiveKey, currentState);
	}
}