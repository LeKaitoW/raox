package ru.bmstu.rk9.rao.ui.process.blocks.selectpath;

import java.io.Serializable;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import ru.bmstu.rk9.rao.ui.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.process.blocks.BlockNodeWithProbability;

public class SelectPathNode extends BlockNodeWithProbability implements Serializable {

	private static final long serialVersionUID = 1;

	public static final String DOCK_IN = "IN";
	public static final String DOCK_TRUE_OUT = "TRUE_OUT";
	public static final String DOCK_FALSE_OUT = "FALSE_OUT";
	public static String name = "SelectPath";

	public SelectPathNode() {
		setName(name);
		registerDock(DOCK_IN);
		registerDock(DOCK_FALSE_OUT);
		registerDock(DOCK_TRUE_OUT);
	}

	@Override
	public BlockConverterInfo createBlock() {
		ru.bmstu.rk9.rao.lib.process.SelectPath selectPath = new ru.bmstu.rk9.rao.lib.process.SelectPath(
				Double.valueOf(this.probability));
		BlockConverterInfo selectPathInfo = new BlockConverterInfo();
		selectPathInfo.setBlock(selectPath);
		selectPathInfo.inputDocks.put(DOCK_IN, selectPath.getInputDock());
		selectPathInfo.outputDocks.put(DOCK_TRUE_OUT, selectPath.getTrueOutputDock());
		selectPathInfo.outputDocks.put(DOCK_FALSE_OUT, selectPath.getFalseOutputDock());
		return selectPathInfo;
	}

	@Override
	public void validateProperty(IResource file) throws CoreException {
		validateProbability(file);
		validateConnections(file, 2, 1);
	}
}
