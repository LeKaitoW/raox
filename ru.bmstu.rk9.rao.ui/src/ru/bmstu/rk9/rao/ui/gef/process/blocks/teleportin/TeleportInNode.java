package ru.bmstu.rk9.rao.ui.gef.process.blocks.teleportin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.eclipse.xtext.xbase.lib.Pair;

import ru.bmstu.rk9.rao.ui.execution.ModelContentsInfo;
import ru.bmstu.rk9.rao.ui.gef.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.BlockNode;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.TeleportHelper;
import ru.bmstu.rk9.rao.ui.gef.process.connection.Connection;

public class TeleportInNode extends BlockNode implements Serializable {

	private static final long serialVersionUID = 1;

	public static final String DOCK_IN = "IN";
	public static final String DOCK_OUT = "OUT";
	public static final String name = "Teleport In";
	protected static final String PROPERTY_NAME = "Name";
	protected static final String PROPERTY_OUT_NAME = "Out names";

	private List<String> outNames = new ArrayList<>();
	private Map<Connection, Integer> connectionsToID = new HashMap<>();

	public TeleportInNode() {
		setName(name);
		registerDock(DOCK_IN);
		registerDock(DOCK_OUT);
	}

	@Override
	public final void setName(String name) {
		String previousValue = getName();
		getListeners().firePropertyChange(PROPERTY_NAME, previousValue, name);

		super.setName(name);
	}

	public final String getOutNames() {
		return String.join(", ", outNames).trim();
	}

	public final void setOutNames(String outName) {
		List<String> previousValue = this.outNames;
		String[] values = outName.split(",");
		for (int i = 0; i < values.length; ++i)
			values[i] = values[i].trim();
		this.outNames = Arrays.asList(values);

		getListeners().firePropertyChange(PROPERTY_NAME, previousValue, outName);

		for (Map.Entry<Connection, Integer> entry : connectionsToID.entrySet())
			entry.getKey().disconnect();
		connectionsToID.clear();

		for (String name : outNames) {
			Pair<Connection, Integer> result = TeleportHelper.connectBlocks(this, name);
			if (result != null)
				connectionsToID.put(result.getKey(), result.getValue());
		}
	}

	@Override
	public BlockConverterInfo createBlock(ModelContentsInfo modelContentsInfo) {
		BlockConverterInfo teleportInfo = new BlockConverterInfo();
		ru.bmstu.rk9.rao.lib.process.TeleportIn teleportIn = new ru.bmstu.rk9.rao.lib.process.TeleportIn(ID,
				connectionsToID.values());
		teleportInfo.setBlock(teleportIn);
		teleportInfo.inputDocks.put(DOCK_IN, teleportIn.getInputDock());
		teleportInfo.outputDocks.put(DOCK_OUT, teleportIn.getOutputDock());

		return teleportInfo;
	}

	@Override
	public void createProperties(List<PropertyDescriptor> properties) {
		super.createProperties(properties);

		properties.add(new TextPropertyDescriptor(PROPERTY_NAME, "Name"));
		properties.add(new TextPropertyDescriptor(PROPERTY_OUT_NAME, "Out name"));
	}

	@Override
	public Object getPropertyValue(String propertyName) {

		switch (propertyName) {
		case PROPERTY_NAME:
			return getName();
		case PROPERTY_OUT_NAME:
			return getOutNames();
		}

		return super.getPropertyValue(propertyName);
	}

	@Override
	public void setPropertyValue(String propertyName, Object value) {
		super.setPropertyValue(propertyName, value);

		switch (propertyName) {
		case PROPERTY_NAME:
			setName((String) value);
			break;
		case PROPERTY_OUT_NAME:
			setOutNames((String) value);
			break;
		}
	}

	@Override
	public void validateProperty(IResource file) throws CoreException {
		if (connectionsToID.isEmpty())
			createProblemMarker(file, "Teleport is not connected", IMarker.SEVERITY_WARNING);

		validateConnections(file, 1, 1);
	}
}
