package ru.bmstu.rk9.rao.ui.gef.process.blocks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.properties.ColorPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.bmstu.rk9.rao.ui.execution.ModelContentsInfo;
import ru.bmstu.rk9.rao.ui.gef.CheckboxPropertyDescriptor;
import ru.bmstu.rk9.rao.ui.gef.Node;
import ru.bmstu.rk9.rao.ui.gef.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.gef.process.ProcessColors;
import ru.bmstu.rk9.rao.ui.gef.process.ProcessLogicException;
import ru.bmstu.rk9.rao.ui.gef.process.connection.Connection;

public abstract class BlockNode extends Node {

	private static final long serialVersionUID = 1L;

	public static final String SOURCE_CONNECTION_UPDATED = "SourceConnectionUpdated";
	public static final String TARGET_CONNECTION_UPDATED = "TargetConnectionUpdated";
	public static final String PROCESS_PROBLEM_MARKER = "ru.bmstu.rk9.rao.ui.gef.ProcessProblemMarker";
	protected static final String PROPERTY_COLOR = "Color";
	protected static final String PROPERTY_SHOW_NAME = "ShowName";
	public static final String BLOCK_NODE_MARKER = "BlockNodeID";

	protected CopyOnWriteArrayList<Connection> sourceConnections = new CopyOnWriteArrayList<Connection>();
	protected CopyOnWriteArrayList<Connection> targetConnections = new CopyOnWriteArrayList<Connection>();
	private final Map<String, Integer> dockNames = new HashMap<>();
	private RGB color = ProcessColors.BLOCK_COLOR.getRGB();
	private boolean showName = true;
	private BlockTitleNode title;
	protected int ID;
	private String name = "Unknown";

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
		if (title != null)
			title.setText(getName());
	}

	final void attachTitle(BlockTitleNode title) {
		this.title = title;
		title.attachBlockNode(this);

		title.setText(getName());

		Rectangle titleConstraint = getConstraint().getCopy();
		titleConstraint.setSize(title.getTextBounds());
		titleConstraint.setX(titleConstraint.x - (titleConstraint.width - getConstraint().width) / 2);
		titleConstraint.setY(titleConstraint.y - titleConstraint.height);
		title.setConstraint(titleConstraint);
	}

	private final void detachTitle() {
		title = null;
	}

	final void cleanup() {
		getParent().removeChild(this);
		disconnect(getSourceConnections());
		disconnect(getTargetConnections());
		detachTitle();
	}

	@Override
	public void onDelete() {
		if (title != null) {
			// Когда удаляется блок, то метод onDelete заголовка сам не
			// вызывается, а в нем происходит отписка от прослушивания изменений
			// глобального шрифта
			title.onDelete();
			// Данный метод вызывается в верхнем, и здесь не нужен
			// title.cleanup();
		}
		cleanup();
	}

	protected final BlockTitleNode getTitle() {
		return title;
	}

	public final boolean getShowName() {
		return showName;
	}

	public final void setShowName(boolean showName) {
		boolean previousValue = this.showName;
		this.showName = showName;
		getListeners().firePropertyChange(PROPERTY_SHOW_NAME, previousValue, showName);
	}

	public final RGB getColor() {
		return color;
	}

	public final void setColor(RGB color) {
		RGB oldColor = this.color;
		this.color = color;
		getListeners().firePropertyChange(PROPERTY_COLOR, oldColor, color);
	}

	@Override
	public void createProperties(List<PropertyDescriptor> properties) {
		properties.add(new ColorPropertyDescriptor(PROPERTY_COLOR, "Color"));
		properties.add(new CheckboxPropertyDescriptor(PROPERTY_SHOW_NAME, "Show name"));
	}

	@Override
	public Object getPropertyValue(String propertyName) {

		switch (propertyName) {
		case PROPERTY_COLOR:
			return getColor();

		case PROPERTY_SHOW_NAME:
			return getShowName();
		}

		return null;
	}

	@Override
	public void setPropertyValue(String propertyName, Object value) {

		switch (propertyName) {
		case PROPERTY_COLOR:
			setColor((RGB) value);
			break;

		case PROPERTY_SHOW_NAME:
			setShowName((boolean) value);
			break;
		}

	}

	public final boolean addConnection(Connection connection) {
		if (connection.getSourceBlockNode() == this) {
			if (!sourceConnections.contains(connection)) {
				if (sourceConnections.add(connection)) {
					getListeners().firePropertyChange(SOURCE_CONNECTION_UPDATED, null, connection);
					return true;
				}
				return false;
			}
		} else if (connection.getTargetBlockNode() == this) {
			if (!targetConnections.contains(connection)) {
				if (targetConnections.add(connection)) {
					getListeners().firePropertyChange(TARGET_CONNECTION_UPDATED, null, connection);
					return true;
				}
				return false;
			}
		}
		return false;
	}

	public final boolean removeConnection(Connection connection) {
		if (connection.getSourceBlockNode() == this) {
			if (sourceConnections.contains(connection)) {
				if (sourceConnections.remove(connection)) {
					getListeners().firePropertyChange(SOURCE_CONNECTION_UPDATED, null, connection);
					return true;
				}
				return false;
			}
		} else if (connection.getTargetBlockNode() == this) {
			if (targetConnections.contains(connection)) {
				if (targetConnections.remove(connection)) {
					getListeners().firePropertyChange(TARGET_CONNECTION_UPDATED, null, connection);
					return true;
				}
				return false;
			}
		}
		return false;
	}

	public final List<Connection> getSourceConnections() {
		return sourceConnections;
	}

	public final List<Connection> getTargetConnections() {
		return targetConnections;
	}

	public final int getDocksCount(String dockName) {
		return dockNames.get(dockName);
	}

	public final void registerDock(String dockName) {
		if (dockNames.containsKey(dockName))
			throw new ProcessLogicException("Dock already registered: " + getDockFullName(dockName));

		dockNames.put(dockName, 0);
	}

	public final void captureDock(String dockName) {
		if (!dockNames.containsKey(dockName))
			throw new ProcessLogicException("Undefined dock: " + getDockFullName(dockName));

		dockNames.put(dockName, dockNames.get(dockName) + 1);
	}

	public final void releaseDock(String dockName) {
		if (!dockNames.containsKey(dockName))
			throw new ProcessLogicException("Undefined dock: " + getDockFullName(dockName));

		int count = dockNames.get(dockName);
		if (count == 0)
			throw new ProcessLogicException("Dock already released: " + getDockFullName(dockName));

		count--;
		dockNames.put(dockName, count);
	}

	private final String getDockFullName(String dockName) {
		return getName() + "." + dockName;
	}

	protected final void validateConnections(IResource file, int sourceConnections, int targetConnections)
			throws CoreException {
		if (getSourceConnections().size() == sourceConnections && getTargetConnections().size() == targetConnections)
			return;

		createProblemMarker(file, "Not all docks are connected", IMarker.SEVERITY_WARNING);
	}

	protected final IMarker createProblemMarker(IResource file, String message, int severity) throws CoreException {
		IMarker marker = file.createMarker(PROCESS_PROBLEM_MARKER);
		marker.setAttribute(IMarker.MESSAGE, message);
		marker.setAttribute(IMarker.LOCATION, getName());
		marker.setAttribute(IMarker.SEVERITY, severity);
		marker.setAttribute(BLOCK_NODE_MARKER, getID());
		return marker;
	}

	private final void disconnect(List<Connection> connections) {
		if (connections.isEmpty())
			return;

		Iterator<Connection> iterator = connections.iterator();
		while (iterator.hasNext()) {
			Connection connection = iterator.next();
			connection.disconnect();
		}
	}

	public abstract BlockConverterInfo createBlock(ModelContentsInfo modelContentsInfo);

	public abstract void validateProperty(IResource file) throws CoreException;
}
