package ru.bmstu.rk9.rao.lib.simulator;

import java.util.List;

import ru.bmstu.rk9.rao.lib.database.Database;
import ru.bmstu.rk9.rao.lib.event.Event;
import ru.bmstu.rk9.rao.lib.logger.Logger;
import ru.bmstu.rk9.rao.lib.modeldata.StaticModelData;
import ru.bmstu.rk9.rao.lib.notification.Notifier;
import ru.bmstu.rk9.rao.lib.result.AbstractResult;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.ExecutionState;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.SimulationStopCode;

public interface ISimulator {
	public void preinitilize(SimulatorPreinitializationInfo info);

	public void initialize(SimulatorInitializationInfo initializationInfo);

	public Database getDatabase();

	public StaticModelData getStaticModelData();

	public ModelState getModelState();

	public void setModelState(ModelState modelState);

	public double getTime();

	public void pushEvent(Event event);

	public Notifier<ExecutionState> getExecutionStateNotifier();

	public void notifyChange(ExecutionState category);

	public void abortExecution();

	public void runExperiments();

	public SimulationStopCode run();

	public List<AbstractResult<?>> getResults();

	public Logger getLogger();
}
