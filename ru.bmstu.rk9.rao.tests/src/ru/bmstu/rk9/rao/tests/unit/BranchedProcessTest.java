package ru.bmstu.rk9.rao.tests.unit;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import ru.bmstu.rk9.rao.lib.process.Hold;
import ru.bmstu.rk9.rao.lib.process.Block;
import ru.bmstu.rk9.rao.lib.process.Generate;
import ru.bmstu.rk9.rao.lib.process.Connection;
import ru.bmstu.rk9.rao.lib.process.Queue;
import ru.bmstu.rk9.rao.lib.process.Release;
import ru.bmstu.rk9.rao.lib.process.Seize;
import ru.bmstu.rk9.rao.lib.process.Terminate;
import ru.bmstu.rk9.rao.lib.process.SelectPath;
import ru.bmstu.rk9.rao.lib.process.Queue.Queueing;
import ru.bmstu.rk9.rao.lib.resource.Resource;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorInitializationInfo;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.SimulationStopCode;

public class BranchedProcessTest {

	@org.junit.Test
	public void test() {
		ProcessTestSuite.initEmptySimulation();
		SimulatorInitializationInfo initializationInfo = new SimulatorInitializationInfo();
		initializationInfo.getTerminateConditions().add(() -> CurrentSimulator.getTime() > 60);
		initializationInfo.getProcessBlocks().addAll(generateSituation());
		CurrentSimulator.initialize(initializationInfo);
		SimulationStopCode simulationStopCode = CurrentSimulator.run();
		assertEquals("linear_process_test", SimulationStopCode.TERMINATE_CONDITION, simulationStopCode);
	}

	private List<Block> generateSituation() {
		List<Block> blocks = new ArrayList<Block>();
		Generate generate = new Generate(() -> 10.0);
		Terminate terminate = new Terminate();
		Hold hold = new Hold(() -> 15.0);
		Resource resource = TestResource.create();
		Seize seize = new Seize(resource);
		Release release = new Release(resource);
		Queue queue = new Queue(Integer.MAX_VALUE, Queueing.FIFO);
		SelectPath selectPath = new SelectPath(0.5);
		blocks.add(generate);
		blocks.add(selectPath);
		blocks.add(queue);
		blocks.add(seize);
		blocks.add(hold);
		blocks.add(release);
		blocks.add(terminate);
		Connection.linkDocks(generate.getOutputDock(), selectPath.getInputDock());
		Connection.linkDocks(selectPath.getTrueOutputDock(), queue.getInputDock());
		Connection.linkDocks(queue.getOutputDock(), seize.getInputDock());
		Connection.linkDocks(seize.getOutputDock(), hold.getInputDock());
		Connection.linkDocks(hold.getOutputDock(), release.getInputDock());
		Connection.linkDocks(release.getOutputDock(), terminate.getInputDock());
		Connection.linkDocks(selectPath.getFalseOutputDock(), terminate.getInputDock());

		return blocks;
	}
}