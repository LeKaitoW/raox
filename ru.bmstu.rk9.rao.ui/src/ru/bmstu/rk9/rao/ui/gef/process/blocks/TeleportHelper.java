package ru.bmstu.rk9.rao.ui.gef.process.blocks;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.xtext.xbase.lib.Pair;

import ru.bmstu.rk9.rao.ui.gef.process.blocks.teleportin.TeleportInNode;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.teleportout.TeleportOutNode;
import ru.bmstu.rk9.rao.ui.gef.process.connection.Connection;

public class TeleportHelper {
	public static List<TeleportOutNode> nodes = new ArrayList<>();

	public static Pair<Connection, Integer> connectBlocks(TeleportInNode in, String outName) {
		Connection connection = null;
		int id = -1;
		for (TeleportOutNode out : nodes) {
			if (out instanceof TeleportOutNode && out.getName().equals(outName)) {
				if (connection != null) {
					connection.disconnect();
					return null; // check name duplicates
				}

				connection = new Connection(in, out, TeleportInNode.DOCK_OUT, TeleportOutNode.DOCK_IN);
				connection.setVisible(false);
				connection.connect();
				id = out.getID();
			}
		}

		return (connection == null || id == -1) ? null : Pair.of(connection, id);
	}
}
