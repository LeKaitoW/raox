package ru.bmstu.rk9.rao.ui.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.bmstu.rk9.rao.lib.process.Block;

public class BlockConverter {

	public static List<Block> convertModelToBlocks(Node model) {
		List<Node> children = model.getChildren();
		List<Block> blocks = new ArrayList<Block>();
		Map<Node, Block> blocksMap = new HashMap<>();
		for (Node node : children) {
			if (!blocksMap.containsKey(node)) {
				Block block = node.createBlock();
				blocks.add(block);
				blocksMap.put(node, block);
			}
		}
		return blocks;
	}
}
