package ru.bmstu.rk9.rao.ui.gef.process.blocks.seize;

import java.util.Optional;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import ru.bmstu.rk9.rao.lib.resource.Resource;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;
import ru.bmstu.rk9.rao.ui.execution.ModelContentsInfo;
import ru.bmstu.rk9.rao.ui.gef.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.BlockNodeWithResource;

public class SeizeNode extends BlockNodeWithResource {

	private static final long serialVersionUID = 1;

	public static final String DOCK_IN = "IN";
	public static final String DOCK_OUT = "OUT";
	public static String name = "Seize";

	public SeizeNode() {
		setName(name);
		registerDock(DOCK_IN);
		registerDock(DOCK_OUT);
	}

	@Override
	public BlockConverterInfo createBlock(ModelContentsInfo modelContentsInfo) {
		Optional<Resource> optional = CurrentSimulator.getModelState().getAllResources().stream()
				.filter(resource -> resource.getName().equals(resourceName)).findAny();
		BlockConverterInfo seizeInfo = new BlockConverterInfo();
		if (!optional.isPresent()) {
			seizeInfo.isSuccessful = false;
			seizeInfo.errorMessage = "Resource name not selected";
			resourceName = "";
			return seizeInfo;
		}
		Resource releasedResource = optional.get();
		ru.bmstu.rk9.rao.lib.process.Seize seize = new ru.bmstu.rk9.rao.lib.process.Seize(ID, releasedResource);
		seizeInfo.setBlock(seize);
		seizeInfo.inputDocks.put(DOCK_IN, seize.getInputDock());
		seizeInfo.outputDocks.put(DOCK_OUT, seize.getOutputDock());
		return seizeInfo;
	}

	@Override
	public void validateProperty(IResource file) throws CoreException {
		validateResource(file);
		validateConnections(file, 1, 1);
	}
}
