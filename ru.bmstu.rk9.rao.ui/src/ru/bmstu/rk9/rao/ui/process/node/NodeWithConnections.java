package ru.bmstu.rk9.rao.ui.process.node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.RGB;

import ru.bmstu.rk9.rao.ui.process.ProcessLogicException;
import ru.bmstu.rk9.rao.ui.process.connection.Connection;

public abstract class NodeWithConnections extends Node {

	private static final long serialVersionUID = 1L;

	public static final String SOURCE_CONNECTION_UPDATED = "SourceConnectionUpdated";
	public static final String TARGET_CONNECTION_UPDATED = "TargetConnectionUpdated";

	public NodeWithConnections(RGB foregroundColor) {
		super(foregroundColor);
	}

	protected List<Connection> sourceConnections;
	protected List<Connection> targetConnections;
	private final Map<String, Integer> docks = new HashMap<>();

	public final boolean addConnection(Connection connection) {
		if (connection.getSourceNode() == this) {
			if (!sourceConnections.contains(connection)) {
				if (sourceConnections.add(connection)) {
					getListeners().firePropertyChange(SOURCE_CONNECTION_UPDATED, null, connection);
					return true;
				}
				return false;
			}
		} else if (connection.getTargetNode() == this) {
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
		if (connection.getSourceNode() == this) {
			if (sourceConnections.contains(connection)) {
				if (sourceConnections.remove(connection)) {
					getListeners().firePropertyChange(SOURCE_CONNECTION_UPDATED, null, connection);
					return true;
				}
				return false;
			}
		} else if (connection.getTargetNode() == this) {
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

	public final int getDocksCount(String dock) {
		return docks.get(dock);
	}

	public final void registerDock(String dock) {
		if (docks.containsKey(dock))
			throw new ProcessLogicException("Dock already registered: " + getDockFullName(dock));

		docks.put(dock, 0);
	}

	public final void captureDock(String dock) {
		if (!docks.containsKey(dock))
			throw new ProcessLogicException("Undefined dock: " + getDockFullName(dock));

		docks.put(dock, docks.get(dock) + 1);
	}

	public final void releaseDock(String dock) {
		if (!docks.containsKey(dock))
			throw new ProcessLogicException("Undefined dock: " + getDockFullName(dock));

		int count = docks.get(dock);
		if (count == 0)
			throw new ProcessLogicException("Dock already released: " + getDockFullName(dock));

		count--;
		docks.put(dock, count);
	}

	private final String getDockFullName(String dock) {
		return getName() + "." + dock;
	}
}