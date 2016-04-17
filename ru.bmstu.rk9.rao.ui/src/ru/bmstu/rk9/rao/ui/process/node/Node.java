package ru.bmstu.rk9.rao.ui.process.node;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertySource;

public class Node extends ru.bmstu.rk9.rao.ui.gef.Node implements IAdaptable {

	private static final long serialVersionUID = 1;

	public static final String NODE_MARKER = "NodeID";

	protected int ID;
	private String name = "Unknown";
	private transient IPropertySource propertySource = null;

	public final int getID() {
		return ID;
	}

	public final void setID(int ID) {
		this.ID = ID;
	}

	public final String getName() {
		return name;
	}

	public final void setName(String name) {
		this.name = name;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySource.class) {
			if (propertySource == null) {
				propertySource = new NodePropertySource(this);
			}
			return propertySource;
		}
		return null;
	}
}
