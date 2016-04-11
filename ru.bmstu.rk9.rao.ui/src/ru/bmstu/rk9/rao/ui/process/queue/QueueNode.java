package ru.bmstu.rk9.rao.ui.process.queue;

import java.io.Serializable;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.process.ProcessColors;
import ru.bmstu.rk9.rao.ui.process.node.BlockNodeWithCapacity;

public class QueueNode extends BlockNodeWithCapacity implements Serializable {

	private static final long serialVersionUID = 1;

	public static final String DOCK_IN = "IN";
	public static final String DOCK_OUT = "OUT";
	private static Color foregroundColor = ProcessColors.BLOCK_COLOR;
	public static String name = "Queue";

	public QueueNode() {
		super(foregroundColor.getRGB());
		setName(name);
		registerDock(DOCK_IN);
		registerDock(DOCK_OUT);
	}

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
		queueInfo.inputDocks.put(DOCK_IN, queue.getInputDock());
		queueInfo.outputDocks.put(DOCK_OUT, queue.getOutputDock());
		return queueInfo;
	}

	@Override
	public void validateProperty(IResource file) throws CoreException {
		validateCapacity(file);
		validateConnections(file, 1, 1);
	}
}
