package ru.bmstu.rk9.rdo.ui.runtime;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.RadioState;

import ru.bmstu.rk9.rdo.ui.animation.RDOAnimationView;

public class SetExecutionModeHandler extends AbstractHandler
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		if(HandlerUtil.matchesRadioState(event))
			return null;

		String currentState = event.getParameter(RadioState.PARAMETER_ID);

		SimulationSynchronizer.setState(currentState);

		RDOAnimationView.disableAnimation(currentState.equals("NA") ? true : false);

		HandlerUtil.updateRadioState(event.getCommand(), currentState);

		return null;
	}
}
