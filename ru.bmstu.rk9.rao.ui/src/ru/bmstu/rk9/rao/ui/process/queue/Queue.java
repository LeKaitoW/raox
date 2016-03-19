package ru.bmstu.rk9.rao.ui.process.queue;

import java.io.Serializable;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;
import ru.bmstu.rk9.rao.ui.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.process.NodeWithCapacity;

public class Queue extends NodeWithCapacity implements Serializable {

	private static final long serialVersionUID = 1;

	public static final String TERMINAL_IN = "IN";
	public static final String TERMINAL_OUT = "OUT";

	public Queue() {
		super(backgroundColor.getRGB());
	}

	private static Color backgroundColor = ColorConstants.gray;
	public static String name = "Queue";

	@Override
	public BlockConverterInfo createBlock() {
		ru.bmstu.rk9.rao.lib.process.Queue queue;
		if (this.capacity.isEmpty()) {
			queue = new ru.bmstu.rk9.rao.lib.process.Queue(Integer.MAX_VALUE);
		} else {
			queue = new ru.bmstu.rk9.rao.lib.process.Queue(Integer.valueOf(this.capacity));
		}
		BlockConverterInfo queueInfo = new BlockConverterInfo(queue);
		queueInfo.inputDocks.put(TERMINAL_IN, queue.getInputDock());
		queueInfo.outputDocks.put(TERMINAL_OUT, queue.getOutputDock());
		return queueInfo;
	}
}
