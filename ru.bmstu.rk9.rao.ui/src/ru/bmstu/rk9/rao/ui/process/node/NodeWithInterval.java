package ru.bmstu.rk9.rao.ui.process.node;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.RGB;

public abstract class NodeWithInterval extends NodeWithConnections {

	private static final long serialVersionUID = 1;

	public static final String PROPERTY_INTERVAL = "NodeInterval";
	private String intervalName;

	protected String interval = "";

	public NodeWithInterval(RGB foregroundColor) {
		super(foregroundColor);
	}

	public NodeWithInterval(RGB foregroundColor, String intervalName) {
		super(foregroundColor);
		this.intervalName = intervalName;
	}

	public void setInterval(String interval) {
		String oldInterval = this.interval;
		this.interval = interval;
		getListeners().firePropertyChange(PROPERTY_INTERVAL, oldInterval, interval);
	}

	public String getInterval() {
		return interval;
	}

	public String getIntervalName() {
		return intervalName;
	}

	public final void validateInterval(IResource file) throws CoreException {
		try {
			Double.valueOf(interval);
		} catch (NumberFormatException e) {
			IMarker marker = file.createMarker(NodeWithProperty.PROCESS_MARKER);
			marker.setAttribute(IMarker.MESSAGE, "Wrong " + getIntervalName());
			marker.setAttribute(IMarker.LOCATION, getName());
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
		}
	}
}
