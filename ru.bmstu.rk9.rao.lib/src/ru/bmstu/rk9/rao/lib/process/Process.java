package ru.bmstu.rk9.rao.lib.process;

import java.util.ArrayList;
import java.util.List;

public class Process {

	public Process(List<Block> blocks) {
		this.blocks.addAll(blocks);
	}

	private final List<Block> blocks = new ArrayList<Block>();

	public ProcessStatus scan() {
		boolean needCheckAgain = false;
		for (Block block : blocks) {
			BlockStatus blockStatus = block.check();
			if (blockStatus == BlockStatus.SUCCESS)
				return ProcessStatus.SUCCESS;
			if (blockStatus == BlockStatus.CHECK_AGAIN)
				needCheckAgain = true;
		}

		return needCheckAgain ? ProcessStatus.FAILURE : ProcessStatus.NOTHING_TO_DO;
	}

	public enum ProcessStatus {
		SUCCESS, FAILURE, NOTHING_TO_DO
	};

	public enum BlockStatus {
		SUCCESS, CHECK_AGAIN, NOTHING_TO_DO
	}
}
