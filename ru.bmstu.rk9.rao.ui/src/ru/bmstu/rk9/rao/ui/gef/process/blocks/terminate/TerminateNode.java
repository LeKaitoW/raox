package ru.bmstu.rk9.rao.ui.gef.process.blocks.terminate;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import ru.bmstu.rk9.rao.ui.execution.ModelContentsInfo;
import ru.bmstu.rk9.rao.ui.gef.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.BlockNode;

public class TerminateNode extends BlockNode {

	private static final long serialVersionUID = 1;

	public static final String DOCK_IN = "IN";
	public static String name = "Terminate";

	public TerminateNode() {
		setName(name);
		registerDock(DOCK_IN);
	}

	@Override
	public BlockConverterInfo createBlock(ModelContentsInfo modelContentsInfo) {
		ru.bmstu.rk9.rao.lib.process.Terminate terminate = new ru.bmstu.rk9.rao.lib.process.Terminate();
		BlockConverterInfo terminateInfo = new BlockConverterInfo();
		terminateInfo.setBlock(terminate);
		terminateInfo.inputDocks.put(DOCK_IN, terminate.getInputDock());
		return terminateInfo;
	}

	@Override
	public void validateProperty(IResource file) throws CoreException {
		validateConnections(file, 0, 1);
	}
}
