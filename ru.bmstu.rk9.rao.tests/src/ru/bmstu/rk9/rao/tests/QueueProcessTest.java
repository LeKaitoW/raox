package ru.bmstu.rk9.rao.tests;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

import org.junit.Test;

import ru.bmstu.rk9.rao.lib.process.Advance;
import ru.bmstu.rk9.rao.lib.process.Block;
import ru.bmstu.rk9.rao.lib.process.Generate;
import ru.bmstu.rk9.rao.lib.process.Queue;
import ru.bmstu.rk9.rao.lib.process.Release;
import ru.bmstu.rk9.rao.lib.process.Resource;
import ru.bmstu.rk9.rao.lib.process.Seize;
import ru.bmstu.rk9.rao.lib.process.Terminate;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.lib.simulator.Simulator.SimulationStopCode;
import ru.bmstu.rk9.rao.lib.process.Process;

public class QueueProcessTest {

	@Test
	public void test() {
		ProcessTestSuite.initEmptySimulation();
		Simulator.addTerminateCondition(() -> Simulator.getTime() > 1000);
		Simulator.getProcess().addBlocks(generateSituation());
		SimulationStopCode simulationStopCode = Simulator.run();
		assertEquals("linear_process_test",
				SimulationStopCode.TERMINATE_CONDITION, simulationStopCode);
	}

	private List<Block> generateSituation() {
		List<Block> blocks = new ArrayList<Block>();
		Generate generate = new Generate(() -> 10);
		Terminate terminate = new Terminate();
		Advance advance = new Advance(() -> 15);
		Resource resource = new Resource();
		Seize seize = new Seize(resource);
		Release release = new Release(resource);
		Queue queue = new Queue();
		blocks.add(generate);
		blocks.add(queue);
		blocks.add(seize);
		blocks.add(advance);
		blocks.add(release);
		blocks.add(terminate);
		Process.linkDocks(generate.getOutputDock(), queue.getInputDock());
		Process.linkDocks(queue.getOutputDock(), seize.getInputDock());
		Process.linkDocks(seize.getOutputDock(), advance.getInputDock());
		Process.linkDocks(advance.getOutputDock(), release.getInputDock());
		Process.linkDocks(release.getOutputDock(), terminate.getInputDock());

		return blocks;
	}

}
