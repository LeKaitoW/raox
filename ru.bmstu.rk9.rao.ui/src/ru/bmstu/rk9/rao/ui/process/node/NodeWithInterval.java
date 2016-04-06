package ru.bmstu.rk9.rao.ui.process.node;

import org.eclipse.swt.graphics.RGB;

public abstract class NodeWithInterval extends NodeWithProperty {

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
}
