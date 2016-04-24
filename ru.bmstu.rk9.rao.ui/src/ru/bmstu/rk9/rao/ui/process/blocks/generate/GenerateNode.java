package ru.bmstu.rk9.rao.ui.process.blocks.generate;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import ru.bmstu.rk9.rao.ui.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.process.blocks.BlockNode;

public class GenerateNode extends BlockNode {

	private static final long serialVersionUID = 1;

	public static final String DOCK_OUT = "OUT";
	public static final String name = "Generate";
	protected static final String PROPERTY_INTERVAL = "Interval";

	private String interval = "";

	public GenerateNode() {
		setName(name);
		registerDock(DOCK_OUT);
	}

	public final String getInterval() {
		return interval;
	}

	public final void setInterval(String interval) {
		String previousValue = this.interval;
		this.interval = interval;
		getListeners().firePropertyChange(PROPERTY_INTERVAL, previousValue, interval);
	}

	@Override
	public BlockConverterInfo createBlock() {
		BlockConverterInfo generateInfo = new BlockConverterInfo();
		Double interval;
		try {
			interval = Double.valueOf(this.interval);
		} catch (NumberFormatException e) {
			generateInfo.isSuccessful = false;
			generateInfo.errorMessage = e.getMessage();
			System.out.println(generateInfo.errorMessage);
			return generateInfo;
		}
		ru.bmstu.rk9.rao.lib.process.Generate generate = new ru.bmstu.rk9.rao.lib.process.Generate(() -> interval);
		generateInfo.setBlock(generate);
		generateInfo.outputDocks.put(DOCK_OUT, generate.getOutputDock());
		return generateInfo;
	}

	@Override
	public void createProperties(List<PropertyDescriptor> properties) {
		super.createProperties(properties);

		properties.add(new TextPropertyDescriptor(PROPERTY_INTERVAL, "Interval"));
	}

	@Override
	public Object getPropertyValue(String propertyName) {

		switch (propertyName) {
		case PROPERTY_INTERVAL:
			return getInterval();
		}

		return super.getPropertyValue(propertyName);
	}

	@Override
	public void setPropertyValue(String propertyName, Object value) {
		super.setPropertyValue(propertyName, value);

		switch (propertyName) {
		case PROPERTY_INTERVAL:
			setInterval((String) value);
			break;
		}
	}

	@Override
	public void validateProperty(IResource file) throws CoreException {
		try {
			Double.valueOf(interval);
		} catch (NumberFormatException e) {
			createProblemMarker(file, "Wrong interval", IMarker.SEVERITY_ERROR);
		}
		validateConnections(file, 1, 0);
	}
}
