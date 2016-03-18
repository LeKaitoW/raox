package ru.bmstu.rk9.rao.tests.unit;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

import org.junit.Test;

import ru.bmstu.rk9.rao.lib.process.Advance;
import ru.bmstu.rk9.rao.lib.process.Block;
import ru.bmstu.rk9.rao.lib.process.Generate;
import ru.bmstu.rk9.rao.lib.process.Link;
import ru.bmstu.rk9.rao.lib.process.Release;
import ru.bmstu.rk9.rao.lib.process.Resource;
import ru.bmstu.rk9.rao.lib.process.Seize;
import ru.bmstu.rk9.rao.lib.process.Terminate;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorInitializationInfo;
import ru.bmstu.rk9.rao.lib.simulator.Simulator.SimulationStopCode;

public class LinearProcessTest {

	@Test
	public void test() {
		ProcessTestSuite.initEmptySimulation();
		SimulatorInitializationInfo initializationInfo = new SimulatorInitializationInfo();
		initializationInfo.terminateConditions.add(() -> Simulator.getTime() > 1000);
		initializationInfo.processBlocks.addAll(generateSituation());
		Simulator.initialize(initializationInfo);
		SimulationStopCode simulationStopCode = Simulator.run();
		assertEquals("linear_process_test", SimulationStopCode.RUNTIME_ERROR, simulationStopCode);
		assertTrue(Math.abs(Simulator.getTime() - 40) < 1e16);
	}

	private List<Block> generateSituation() {
		List<Block> blocks = new ArrayList<Block>();
		Generate generate = new Generate(() -> 10);
		Terminate terminate = new Terminate();
		Advance advance = new Advance(() -> 15);
		Resource resource = Resource.create();
		Seize seize = new Seize(resource);
		Release release = new Release(resource);
		blocks.add(generate);
		blocks.add(seize);
		blocks.add(advance);
		blocks.add(release);
		blocks.add(terminate);
		Link.linkDocks(generate.getOutputDock(), seize.getInputDock());
		Link.linkDocks(seize.getOutputDock(), advance.getInputDock());
		Link.linkDocks(advance.getOutputDock(), release.getInputDock());
		Link.linkDocks(release.getOutputDock(), terminate.getInputDock());

		return blocks;
	}

}
