package ru.bmstu.rk9.rao.ui.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.bmstu.rk9.rao.lib.process.Block;
import ru.bmstu.rk9.rao.lib.process.Link;
import ru.bmstu.rk9.rao.ui.process.link.ProcessLink;

public class BlockConverter {

	public static List<Block> convertModelToBlocks(Node model) {
		List<Node> children = model.getChildren();
		List<Block> blocks = new ArrayList<Block>();
		Map<Node, BlockConverterInfo> blocksMap = new HashMap<>();
		for (Node node : children) {
			BlockConverterInfo blockInfo;
			if (!blocksMap.containsKey(node)) {
				blockInfo = node.createBlock();
				blocks.add(blockInfo.block);
				blocksMap.put(node, blockInfo);
			} else {
				blockInfo = blocksMap.get(node);
			}

			for (ProcessLink sourceLink : node.getSourceConnectionsArray()) {
				Node targetNode = sourceLink.getTargetNode();
				BlockConverterInfo targetBlockInfo;
				if (!blocksMap.containsKey(targetNode)) {
					targetBlockInfo = targetNode.createBlock();
					blocks.add(targetBlockInfo.block);
					blocksMap.put(targetNode, targetBlockInfo);
				} else {
					targetBlockInfo = blocksMap.get(targetNode);
				}
				Link.linkDocks(blockInfo.outputDocks.get(sourceLink.getSourceTerminal()),
						targetBlockInfo.inputDocks.get(sourceLink.getTargetTerminal()));
			}

		}
		return blocks;
	}
}
