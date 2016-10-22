package ru.bmstu.rk9.rao.lib.result;

import java.util.LinkedList;
import java.util.List;

import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;

public class ResultManager {

	public ResultManager(List<Result<?>> results) {
		this.results.addAll(results);
		CurrentSimulator.getExecutionStateNotifier().addSubscriber(this.stateChangedSubscriber,
				CurrentSimulator.ExecutionState.STATE_CHANGED);
	}

	private final Subscriber stateChangedSubscriber = new Subscriber() {

		@Override
		public void fireChange() {
			for (Result<?> result : results) {
				if (result.getResultMode() == ResultMode.AUTO)
					result.update();
			}
		}
	};

	private final List<Result<?>> results = new LinkedList<Result<?>>();

	public List<Result<?>> getResults() {
		return results;
	}
}
