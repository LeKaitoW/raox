package ru.bmstu.rk9.rao.ui.process.blocks.queue;

import java.io.Serializable;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import ru.bmstu.rk9.rao.lib.process.Queue;
import ru.bmstu.rk9.rao.lib.process.Queue.Queueing;
import ru.bmstu.rk9.rao.ui.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.process.blocks.BlockNode;

public class QueueNode extends BlockNode implements Serializable {

	private static final long serialVersionUID = 1;

	public static final String DOCK_IN = "IN";
	public static final String DOCK_OUT = "OUT";
	public static String name = "Queue";
	protected static final String PROPERTY_CAPACITY = "Capacity";
	protected static final String PROPERTY_QUEUEING = "Queueing";

	private String capacity = "";
	private Queueing queueing = Queueing.FIFO;

	public QueueNode() {
		setName(name);
		registerDock(DOCK_IN);
		registerDock(DOCK_OUT);
	}

	private final String getCapacity() {
		return capacity;
	}

	private final void setCapacity(String capacity) {
		String previousValue = this.capacity;
		this.capacity = capacity;
		getListeners().firePropertyChange(PROPERTY_CAPACITY, previousValue, capacity);
	}

	private final Integer getQueueingIndex() {
		return queueing.ordinal();
	}

	private final void setQueueing(int index) {
		Queueing previousValue = this.queueing;
		this.queueing = Queueing.values()[index];
		getListeners().firePropertyChange(PROPERTY_QUEUEING, previousValue, queueing);
	}

	@Override
	public BlockConverterInfo createBlock() {
		ru.bmstu.rk9.rao.lib.process.Queue queue;
		if (this.capacity.isEmpty()) {
			queue = new ru.bmstu.rk9.rao.lib.process.Queue(Integer.MAX_VALUE, this.queueing);
		} else {
			queue = new ru.bmstu.rk9.rao.lib.process.Queue(Integer.valueOf(this.capacity), this.queueing);
		}
		BlockConverterInfo queueInfo = new BlockConverterInfo();
		queueInfo.setBlock(queue);
		queueInfo.inputDocks.put(DOCK_IN, queue.getInputDock());
		queueInfo.outputDocks.put(DOCK_OUT, queue.getOutputDock());
		return queueInfo;
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

	private final void validateCapacity(IResource file) throws CoreException {
		if (capacity.isEmpty())
			return;

		try {
			Double.valueOf(capacity);
		} catch (NumberFormatException e) {
			createProblemMarker(file, "Wrong capacity", IMarker.SEVERITY_ERROR);
		}
	}

	@Override
	public void validateProperty(IResource file) throws CoreException {
		validateCapacity(file);
		validateConnections(file, 1, 1);
	}
}
