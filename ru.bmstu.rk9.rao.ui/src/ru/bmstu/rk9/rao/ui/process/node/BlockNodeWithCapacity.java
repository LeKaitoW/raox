package ru.bmstu.rk9.rao.ui.process.node;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import ru.bmstu.rk9.rao.lib.process.Queue;
import ru.bmstu.rk9.rao.lib.process.Queue.Queueing;

public abstract class BlockNodeWithCapacity extends BlockNode {

	private static final long serialVersionUID = 1;

	protected static final String PROPERTY_CAPACITY = "Capacity";
	protected static final String PROPERTY_QUEUEING = "Queueing";

	protected String capacity = "";
	protected Queueing queueing = Queueing.FIFO;

	public String getCapacity() {
		return capacity;
	}

	public void setCapacity(String capacity) {
		String oldCapacity = this.capacity;
		this.capacity = capacity;
		getListeners().firePropertyChange(PROPERTY_CAPACITY, oldCapacity, capacity);
	}

	public Integer getQueueingIndex() {
		return queueing.ordinal();
	}

	public void setQueueing(int index) {
		Queueing oldQueueig = this.queueing;
		this.queueing = Queueing.values()[index];
		getListeners().firePropertyChange(PROPERTY_QUEUEING, oldQueueig, queueing);
	}

	protected final void validateCapacity(IResource file) throws CoreException {
		if (capacity.isEmpty())
			return;

		try {
			Double.valueOf(capacity);
		} catch (NumberFormatException e) {
			createProblemMarker(file, "Wrong capacity", IMarker.SEVERITY_ERROR);
		}
	}

	@Override
	public void createProperties(List<PropertyDescriptor> properties) {
		super.createProperties(properties);

		properties.add(new TextPropertyDescriptor(PROPERTY_CAPACITY, "Capacity"));
		properties.add(new ComboBoxPropertyDescriptor(PROPERTY_QUEUEING, "Queueing", Queue.getQueueingArray()));
	}

	@Override
	public Object getPropertyValue(Object propertyName) {
		if (propertyName.equals(PROPERTY_CAPACITY))
			return getCapacity();

		if (propertyName.equals(PROPERTY_QUEUEING))
			return getQueueingIndex();

		return super.getPropertyValue(propertyName);
	}

	@Override
	public void setPropertyValue(Object propertyName, Object value) {
		super.setPropertyValue(propertyName, value);

		if (propertyName.equals(PROPERTY_CAPACITY))
			setCapacity((String) value);

		if (propertyName.equals(PROPERTY_QUEUEING))
			setQueueing((int) value);
	}
}
