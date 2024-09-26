package ru.bmstu.rk9.rao.tests.unit;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ru.bmstu.rk9.rao.lib.process.Block;
import ru.bmstu.rk9.rao.lib.process.Connection;
import ru.bmstu.rk9.rao.lib.process.Generate;
import ru.bmstu.rk9.rao.lib.process.Hold;
import ru.bmstu.rk9.rao.lib.process.Queue;
import ru.bmstu.rk9.rao.lib.process.Queue.Queueing;
import ru.bmstu.rk9.rao.lib.process.Release;
import ru.bmstu.rk9.rao.lib.process.Seize;
import ru.bmstu.rk9.rao.lib.process.Terminate;
import ru.bmstu.rk9.rao.lib.resource.Resource;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.SimulationStopCode;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorInitializationInfo;

public class QueueProcessTest {

	@Test
	public void test() {
		ProcessTestSuite.initEmptySimulation();
		SimulatorInitializationInfo initializationInfo = new SimulatorInitializationInfo();
		initializationInfo.terminateConditions.add(() -> CurrentSimulator.getTime() > 60);
		initializationInfo.processBlocks.addAll(generateSituation());
		CurrentSimulator.initialize(initializationInfo);
		SimulationStopCode simulationStopCode = CurrentSimulator.run();
		assertEquals("linear_process_test", SimulationStopCode.TERMINATE_CONDITION, simulationStopCode);
	}

	private List<Block> generateSituation() {
		int ID = 0;
		List<Block> blocks = new ArrayList<Block>();
		Generate generate = new Generate(ID++, () -> 10.0);
		Terminate terminate = new Terminate(ID++);
		Hold hold = new Hold(ID++, () -> 15.0);
		Resource resource = TestResource.create();
		Seize seize = new Seize(ID++, resource);
		Release release = new Release(ID++, resource);
		Queue queue = new Queue(ID++, Integer.MAX_VALUE, Queueing.FIFO);
		blocks.add(generate);
		blocks.add(queue);
		blocks.add(seize);
		blocks.add(hold);
		blocks.add(release);
		blocks.add(terminate);
		Connection.linkDocks(generate.getOutputDock(), queue.getInputDock());
		Connection.linkDocks(queue.getOutputDock(), seize.getInputDock());
		Connection.linkDocks(seize.getOutputDock(), hold.getInputDock());
		Connection.linkDocks(hold.getOutputDock(), release.getInputDock());
		Connection.linkDocks(release.getOutputDock(), terminate.getInputDock());

		return blocks;
	}

}
