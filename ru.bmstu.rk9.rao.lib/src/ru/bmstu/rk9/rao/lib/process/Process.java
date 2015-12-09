package ru.bmstu.rk9.rao.lib.process;

import java.util.ArrayList;
import java.util.List;

public class Process {

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

	public static void linkDocks(OutputDock outputDock, InputDock inputDock) {
		outputDock.setLinkedDock(inputDock);
		inputDock.setLinkedDock(outputDock);
	}

	public void addBlocks(List<Block> blocks) {
		this.blocks.addAll(blocks);
	}

	public enum ProcessStatus {
		SUCCESS, FAILURE, NOTHING_TO_DO
	};
}
