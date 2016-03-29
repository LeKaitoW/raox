package ru.bmstu.rk9.rao.ui.process.release;

import java.util.Optional;

import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.lib.resource.Resource;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.ui.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.process.NodeWithResource;

public class Release extends NodeWithResource {

	private static final long serialVersionUID = 1;

	public static final String TERMINAL_IN = "IN";
	public static final String TERMINAL_OUT = "OUT";

	public Release() {
		super(backgroundColor.getRGB());
		linksCount.put(TERMINAL_IN, 0);
		linksCount.put(TERMINAL_OUT, 0);
	}

	private static Color backgroundColor = new Color(null, 67, 181, 129);
	public static String name = "Release";

	@Override
	public BlockConverterInfo createBlock() {
		Optional<Resource> optional = Simulator.getModelState().getAllResources().stream()
				.filter(resource -> resource.getName().equals(resourceName)).findAny();
		BlockConverterInfo releaseInfo = new BlockConverterInfo();
		if (!optional.isPresent()) {
			releaseInfo.isSuccessful = false;
			releaseInfo.errorMessage = "Resource name not selected";
			resourceName = "";
			return releaseInfo;
		}
		Resource seizedResource = optional.get();
		ru.bmstu.rk9.rao.lib.process.Release release = new ru.bmstu.rk9.rao.lib.process.Release(seizedResource);
		releaseInfo.setBlock(release);
		releaseInfo.inputDocks.put(TERMINAL_IN, release.getInputDock());
		releaseInfo.outputDocks.put(TERMINAL_OUT, release.getOutputDock());
		return releaseInfo;
	}
}
