package ru.bmstu.rk9.rao.ui.gef.process.blocks;

import java.util.ArrayList;
import java.util.List;

import ru.bmstu.rk9.rao.ui.gef.process.blocks.teleportin.TeleportInNode;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.teleportout.TeleportOutNode;
import ru.bmstu.rk9.rao.ui.gef.process.connection.Connection;

public class TeleportHelper {
	public static List<TeleportOutNode> nodes = new ArrayList<>();

	public static Connection connectBlocks(TeleportInNode in, String outName) {
		Connection connection = null;
		for (TeleportOutNode out : nodes) {
			if (out instanceof TeleportOutNode && out.getName().equals(in.getOutName())) {
				if (connection != null) {
					connection.disconnect();
					return null;
				}

				connection = new Connection(in, out, TeleportInNode.DOCK_OUT, TeleportOutNode.DOCK_IN);
				connection.setVisible(false);
				connection.connect();
			}
		}

		return connection;
	}
}
