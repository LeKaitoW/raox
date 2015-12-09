package ru.bmstu.rk9.rao.lib.process;

import java.util.ArrayList;
import java.util.List;

public class Process {

	public Process() {
		Generate generate = new Generate();
		Terminate terminate = new Terminate();
		Advance advance = new Advance();
		Resource resource = new Resource();
		Seize seize = new Seize(resource);
		Release release = new Release(resource);
		blocks.add(generate);
		blocks.add(seize);
		blocks.add(advance);
		blocks.add(release);
		blocks.add(terminate);
		linkDocks(generate.getOutputDock(), seize.getInputDock());
		linkDocks(seize.getOutputDock(), advance.getInputDock());
		linkDocks(advance.getOutputDock(), release.getInputDock());
		linkDocks(release.getOutputDock(), terminate.getInputDock());
	}

	private final List<Block> blocks = new ArrayList<Block>();

	public ProcessStatus scan() {
		boolean processFailed = false;
		for (Block block : blocks) {
			ProcessStatus processStatus = block.check();
			if (processStatus == ProcessStatus.SUCCESS)
				return processStatus;
			if (processStatus == ProcessStatus.FAILURE)
				processFailed = true;
		}

		return processFailed ? ProcessStatus.FAILURE
				: ProcessStatus.NOTHING_TO_DO;
	}

	public void linkDocks(OutputDock outputDock, InputDock inputDock) {
		outputDock.setLinkedDock(inputDock);
		inputDock.setLinkedDock(outputDock);
	}

	public enum ProcessStatus {
		SUCCESS, FAILURE, NOTHING_TO_DO
	};
}
