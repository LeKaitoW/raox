package ru.bmstu.rk9.rao.ui.gef.process.blocks.release;

import java.util.Optional;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import ru.bmstu.rk9.rao.lib.resource.Resource;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;
import ru.bmstu.rk9.rao.ui.execution.ModelContentsInfo;
import ru.bmstu.rk9.rao.ui.gef.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.BlockNodeWithResource;

public class ReleaseNode extends BlockNodeWithResource {

	private static final long serialVersionUID = 1;

	public static final String DOCK_IN = "IN";
	public static final String DOCK_OUT = "OUT";
	public static String name = "Release";

	public ReleaseNode() {
		setName(name);
		registerDock(DOCK_IN);
		registerDock(DOCK_OUT);
	}

	@Override
	public BlockConverterInfo createBlock(ModelContentsInfo modelContentsInfo) {
		Optional<Resource> optional = CurrentSimulator.getModelState().getAllResources().stream()
				.filter(resource -> resource.getName().equals(resourceName)).findAny();
		BlockConverterInfo releaseInfo = new BlockConverterInfo();
		if (!optional.isPresent()) {
			releaseInfo.isSuccessful = false;
			releaseInfo.errorMessage = "Resource name not selected";
			resourceName = "";
			return releaseInfo;
		}
		Resource seizedResource = optional.get();
		ru.bmstu.rk9.rao.lib.process.Release release = new ru.bmstu.rk9.rao.lib.process.Release(ID, seizedResource);
		releaseInfo.setBlock(release);
		releaseInfo.inputDocks.put(DOCK_IN, release.getInputDock());
		releaseInfo.outputDocks.put(DOCK_OUT, release.getOutputDock());
		return releaseInfo;
	}

	@Override
	public void validateProperty(IResource file) throws CoreException {
		validateResource(file);
		validateConnections(file, 1, 1);
	}
}
