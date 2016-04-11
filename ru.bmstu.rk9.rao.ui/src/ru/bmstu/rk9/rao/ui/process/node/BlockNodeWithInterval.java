package ru.bmstu.rk9.rao.ui.process.node;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.RGB;

public abstract class BlockNodeWithInterval extends BlockNode {

	private static final long serialVersionUID = 1;

	public static final String PROPERTY_INTERVAL = "NodeInterval";
	private String intervalName;
	protected String interval = "";

	public BlockNodeWithInterval(RGB foregroundColor) {
		super(foregroundColor);
	}

	public BlockNodeWithInterval(RGB foregroundColor, String intervalName) {
		super(foregroundColor);
		this.intervalName = intervalName;
	}

	public String getInterval() {
		return interval;
	}

	public void setInterval(String interval) {
		String oldInterval = this.interval;
		this.interval = interval;
		getListeners().firePropertyChange(PROPERTY_INTERVAL, oldInterval, interval);
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
		}
	}
}
