package ru.bmstu.rk9.rao.ui.gef.process.blocks.teleportout;

import java.io.Serializable;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import ru.bmstu.rk9.rao.ui.execution.ModelContentsInfo;
import ru.bmstu.rk9.rao.ui.gef.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.BlockNode;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.TeleportHelper;

public class TeleportOutNode extends BlockNode implements Serializable {

	private static final long serialVersionUID = 1;

	public static final String DOCK_IN = "IN";
	public static final String DOCK_OUT = "OUT";
	public static final String name = "Teleport Out";
	protected static final String PROPERTY_NAME = "Name";

	public TeleportOutNode() {
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

	@Override
	public BlockConverterInfo createBlock(ModelContentsInfo modelContentsInfo) {
		BlockConverterInfo teleportInfo = new BlockConverterInfo();

		ru.bmstu.rk9.rao.lib.process.TeleportOut teleportOut = new ru.bmstu.rk9.rao.lib.process.TeleportOut();
		teleportInfo.setBlock(teleportOut);
		teleportInfo.inputDocks.put(DOCK_IN, teleportOut.getInputDock());
		teleportInfo.outputDocks.put(DOCK_OUT, teleportOut.getOutputDock());
		return teleportInfo;
	}

	@Override
	public void createProperties(List<PropertyDescriptor> properties) {
		super.createProperties(properties);

		properties.add(new TextPropertyDescriptor(PROPERTY_NAME, "Name"));
	}

	@Override
	public Object getPropertyValue(String propertyName) {

		switch (propertyName) {
		case PROPERTY_NAME:
			return getName();
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
		}
	}

	@Override
	public void validateProperty(IResource file) throws CoreException {
		validateConnections(file, 1, 1);
	}

	@Override
	public void onDelete() {
		super.onDelete();

		TeleportHelper.nodes.add(this);
	}
}
