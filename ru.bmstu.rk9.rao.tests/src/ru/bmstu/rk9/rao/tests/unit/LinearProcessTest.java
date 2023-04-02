package ru.bmstu.rk9.rao.tests.unit;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

import org.junit.Test;

import ru.bmstu.rk9.rao.lib.process.Hold;
import ru.bmstu.rk9.rao.lib.process.Block;
import ru.bmstu.rk9.rao.lib.process.Generate;
import ru.bmstu.rk9.rao.lib.process.Connection;
import ru.bmstu.rk9.rao.lib.process.Release;
import ru.bmstu.rk9.rao.lib.process.Seize;
import ru.bmstu.rk9.rao.lib.process.Terminate;
import ru.bmstu.rk9.rao.lib.resource.Resource;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorInitializationInfo;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.SimulationStopCode;

public class LinearProcessTest {

	@Test
	public void test() {
		ProcessTestSuite.initEmptySimulation();
		SimulatorInitializationInfo initializationInfo = new SimulatorInitializationInfo();
		initializationInfo.getTerminateConditions().add(() -> CurrentSimulator.getTime() > 1000);
		initializationInfo.getProcessBlocks().addAll(generateSituation());
		CurrentSimulator.initialize(initializationInfo);
		SimulationStopCode simulationStopCode = CurrentSimulator.run();
		assertEquals("linear_process_test", SimulationStopCode.RUNTIME_ERROR, simulationStopCode);
		assertTrue(Math.abs(CurrentSimulator.getTime() - 40) < 1e16);
	}

	private List<Block> generateSituation() {
		List<Block> blocks = new ArrayList<Block>();
		Generate generate = new Generate(() -> 10.0);
		Terminate terminate = new Terminate();
		Hold hold = new Hold(() -> 15.0);
		Resource resource = TestResource.create();
		Seize seize = new Seize(resource);
		Release release = new Release(resource);
		blocks.add(generate);
		blocks.add(seize);
		blocks.add(hold);
		blocks.add(release);
		blocks.add(terminate);
		Connection.linkDocks(generate.getOutputDock(), seize.getInputDock());
		Connection.linkDocks(seize.getOutputDock(), hold.getInputDock());
		Connection.linkDocks(hold.getOutputDock(), release.getInputDock());
		Connection.linkDocks(release.getOutputDock(), terminate.getInputDock());

		return blocks;
	}
}
