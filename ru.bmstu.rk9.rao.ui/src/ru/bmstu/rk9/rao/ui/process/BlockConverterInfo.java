package ru.bmstu.rk9.rao.ui.process;

import java.util.HashMap;
import java.util.Map;

import ru.bmstu.rk9.rao.lib.process.Block;
import ru.bmstu.rk9.rao.lib.process.InputDock;
import ru.bmstu.rk9.rao.lib.process.OutputDock;

public class BlockConverterInfo {

	public final Block block;
	public final Map<String, InputDock> inputDocks = new HashMap<>();
	public final Map<String, OutputDock> outputDocks = new HashMap<>();

	public BlockConverterInfo(Block block) {
		this.block = block;
	}
}
