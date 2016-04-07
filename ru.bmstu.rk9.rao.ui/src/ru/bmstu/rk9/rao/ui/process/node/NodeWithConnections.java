package ru.bmstu.rk9.rao.ui.process.node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.RGB;

import ru.bmstu.rk9.rao.ui.process.BlockConverterInfo;
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
	private final Map<String, Integer> dockNames = new HashMap<>();

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

	public abstract BlockConverterInfo createBlock();
}
