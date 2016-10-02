package ru.bmstu.rk9.rao.ui.gef.process.blocks.queue;

import java.io.Serializable;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import ru.bmstu.rk9.rao.lib.process.Queue.Queueing;
import ru.bmstu.rk9.rao.ui.execution.ModelContentsInfo;
import ru.bmstu.rk9.rao.ui.gef.EnumComboBoxPropertyDescriptor;
import ru.bmstu.rk9.rao.ui.gef.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.BlockNode;

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

	private final Queueing getQueueing() {
		return queueing;
	}

	private final void setQueueing(Queueing queueing) {
		Queueing previousValue = this.queueing;
		this.queueing = queueing;
		getListeners().firePropertyChange(PROPERTY_QUEUEING, previousValue, queueing);
	}

	@Override
	public BlockConverterInfo createBlock(ModelContentsInfo modelContentsInfo) {
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
		properties.add(new EnumComboBoxPropertyDescriptor<>(PROPERTY_QUEUEING, "Queueing", Queueing.class));
	}

	@Override
	public Object getPropertyValue(String propertyName) {

		switch (propertyName) {
		case PROPERTY_CAPACITY:
			return getCapacity();

		case PROPERTY_QUEUEING:
			return getQueueing();
		}

		return super.getPropertyValue(propertyName);
	}

	@Override
	public void setPropertyValue(String propertyName, Object value) {
		super.setPropertyValue(propertyName, value);

		switch (propertyName) {
		case PROPERTY_CAPACITY:
			setCapacity((String) value);
			break;

		case PROPERTY_QUEUEING:
			setQueueing((Queueing) value);
			break;
		}
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
