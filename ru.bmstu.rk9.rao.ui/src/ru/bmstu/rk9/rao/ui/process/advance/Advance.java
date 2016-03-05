package ru.bmstu.rk9.rao.ui.process.advance;

import java.io.Serializable;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.lib.process.Block;
import ru.bmstu.rk9.rao.ui.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.process.Node;
import ru.bmstu.rk9.rao.ui.process.NodeWithProperty;

public class Advance extends NodeWithProperty implements Serializable {

	private static final long serialVersionUID = 1;

	public Advance() {
		super(backgroundColor.getRGB());
	}

	private static Color backgroundColor = ColorConstants.green;
	public static String name = "Advance";

	@Override
	public BlockConverterInfo createBlock() {
		ru.bmstu.rk9.rao.lib.process.Advance advance = new ru.bmstu.rk9.rao.lib.process.Advance(() -> 10);
		BlockConverterInfo advanceInfo = new BlockConverterInfo(advance);
		advanceInfo.inputDocks.put(Node.TERMINAL_IN, advance.getInputDock());
		advanceInfo.outputDocks.put(Node.TERMINAL_OUT, advance.getOutputDock());
		return advanceInfo;
	}

}
