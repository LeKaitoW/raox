package ru.bmstu.rk9.rao.lib.result;

import java.util.LinkedList;
import java.util.List;

import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;

public class ResultManager {

	public ResultManager(List<AbstractResult<?>> results) {
		this.results.addAll(results);
		CurrentSimulator.getExecutionStateNotifier().addSubscriber(this.stateChangedSubscriber,
				CurrentSimulator.ExecutionState.STATE_CHANGED);
	}

	private final Subscriber stateChangedSubscriber = new Subscriber() {

		@Override
		public void fireChange() {
			for (AbstractResult<?> abstractResult : results) {
				if (!(abstractResult instanceof EvaluatableResult))
					continue;
				EvaluatableResult<?> result = (EvaluatableResult<?>) abstractResult;
				if (result.getResultMode() == ResultMode.AUTO)
					result.update();
			}
		}
	};

	private final List<AbstractResult<?>> results = new LinkedList<AbstractResult<?>>();

	public List<AbstractResult<?>> getResults() {
		return results;
	}
}
