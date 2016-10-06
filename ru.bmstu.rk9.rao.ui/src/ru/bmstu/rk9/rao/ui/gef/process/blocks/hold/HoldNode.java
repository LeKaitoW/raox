package ru.bmstu.rk9.rao.ui.gef.process.blocks.hold;

import java.io.Serializable;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import ru.bmstu.rk9.rao.ui.execution.ModelContentsInfo;
import ru.bmstu.rk9.rao.ui.gef.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.BlockNode;

public class HoldNode extends BlockNode implements Serializable {

	private static final long serialVersionUID = 1;

	public static final String DOCK_IN = "IN";
	public static final String DOCK_OUT = "OUT";
	public static final String name = "Hold";
	protected static final String PROPERTY_DURATION = "Duration";

	private String duration = "";

	public HoldNode() {
		setName(name);
		registerDock(DOCK_IN);
		registerDock(DOCK_OUT);
	}

	public final String getDuration() {
		return duration;
	}

	public final void setDuration(String duration) {
		String previousValue = this.duration;
		this.duration = duration;
		getListeners().firePropertyChange(PROPERTY_DURATION, previousValue, duration);
	}

	@Override
	public BlockConverterInfo createBlock(ModelContentsInfo modelContentsInfo) {
		BlockConverterInfo holdInfo = new BlockConverterInfo();
		Double duration;
		try {
			duration = Double.valueOf(this.duration);
		} catch (NumberFormatException e) {
			holdInfo.isSuccessful = false;
			holdInfo.errorMessage = e.getMessage();
			System.out.println(holdInfo.errorMessage);
			return holdInfo;
		}
		ru.bmstu.rk9.rao.lib.process.Hold hold = new ru.bmstu.rk9.rao.lib.process.Hold(() -> duration);
		holdInfo.setBlock(hold);
		holdInfo.inputDocks.put(DOCK_IN, hold.getInputDock());
		holdInfo.outputDocks.put(DOCK_OUT, hold.getOutputDock());
		return holdInfo;
	}

	@Override
	public void createProperties(List<PropertyDescriptor> properties) {
		super.createProperties(properties);

		properties.add(new TextPropertyDescriptor(PROPERTY_DURATION, "Duration"));
	}

	@Override
	public Object getPropertyValue(String propertyName) {

		switch (propertyName) {
		case PROPERTY_DURATION:
			return getDuration();
		}

		return super.getPropertyValue(propertyName);
	}

	@Override
	public void setPropertyValue(String propertyName, Object value) {
		super.setPropertyValue(propertyName, value);

		switch (propertyName) {
		case PROPERTY_DURATION:
			setDuration((String) value);
			break;
		}
	}

	@Override
	public void validateProperty(IResource file) throws CoreException {
		try {
			Double.valueOf(duration);
		} catch (NumberFormatException e) {
			createProblemMarker(file, "Wrong duration", IMarker.SEVERITY_ERROR);
		}
		validateConnections(file, 1, 1);
	}
}
