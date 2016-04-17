package ru.bmstu.rk9.rao.ui.process.node;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

public abstract class BlockNodeWithInterval extends BlockNode {

	private static final long serialVersionUID = 1;

	protected static final String PROPERTY_INTERVAL = "Interval";
	private String intervalName;
	protected String interval = "";

	public BlockNodeWithInterval(String intervalName) {
		this.intervalName = intervalName;
	}

	public String getInterval() {
		return interval;
	}

	public void setInterval(String interval) {
		String previousValue = this.interval;
		this.interval = interval;
		getListeners().firePropertyChange(PROPERTY_INTERVAL, previousValue, interval);
	}

	public String getIntervalName() {
		return intervalName;
	}

	public final void validateInterval(IResource file) throws CoreException {
		try {
			Double.valueOf(interval);
		} catch (NumberFormatException e) {
			IMarker marker = file.createMarker(BlockNode.PROCESS_MARKER);
			marker.setAttribute(IMarker.MESSAGE, "Wrong " + getIntervalName());
			marker.setAttribute(IMarker.LOCATION, getName());
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			marker.setAttribute(NODE_MARKER, getID());
		}
	}

	@Override
	public void createProperties(List<PropertyDescriptor> properties) {
		super.createProperties(properties);

		properties.add(new TextPropertyDescriptor(PROPERTY_INTERVAL, getIntervalName()));
	}

	@Override
	public Object getPropertyValue(Object propertyName) {
		Object value = super.getPropertyValue(propertyName);
		if (value != null)
			return value;

		if (propertyName.equals(PROPERTY_INTERVAL))
			return getInterval();

		return null;
	}

	@Override
	public void setPropertyValue(Object propertyName, Object value) {
		super.setPropertyValue(propertyName, value);

		if (propertyName.equals(PROPERTY_INTERVAL))
			setInterval((String) value);
	}
}
