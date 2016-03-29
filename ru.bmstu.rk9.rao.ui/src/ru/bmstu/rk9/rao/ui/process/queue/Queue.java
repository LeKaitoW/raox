package ru.bmstu.rk9.rao.ui.process.queue;

import java.io.Serializable;

import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.process.NodeWithCapacity;
import ru.bmstu.rk9.rao.ui.process.ProcessColors;

public class Queue extends NodeWithCapacity implements Serializable {

	private static final long serialVersionUID = 1;

	public static final String TERMINAL_IN = "IN";
	public static final String TERMINAL_OUT = "OUT";

	public Queue() {
		super(foregroundColor.getRGB());
		linksCount.put(TERMINAL_IN, 0);
		linksCount.put(TERMINAL_OUT, 0);
	}

	private static Color foregroundColor = ProcessColors.BLOCK_COLOR;
	public static String name = "Queue";

	@Override
	public BlockConverterInfo createBlock() {
		ru.bmstu.rk9.rao.lib.process.Queue queue;
		if (this.capacity.isEmpty()) {
			queue = new ru.bmstu.rk9.rao.lib.process.Queue(Integer.MAX_VALUE, this.queueing);
		} else {
			queue = new ru.bmstu.rk9.rao.lib.process.Queue(Integer.valueOf(this.capacity), this.queueing);
		}
		BlockConverterInfo queueInfo = new BlockConverterInfo();
		queueInfo.setBlock(queue);
		queueInfo.inputDocks.put(TERMINAL_IN, queue.getInputDock());
		queueInfo.outputDocks.put(TERMINAL_OUT, queue.getOutputDock());
		return queueInfo;
	}
}
