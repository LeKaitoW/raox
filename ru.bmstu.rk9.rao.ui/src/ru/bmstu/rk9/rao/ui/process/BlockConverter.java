package ru.bmstu.rk9.rao.ui.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.bmstu.rk9.rao.lib.process.Block;
import ru.bmstu.rk9.rao.ui.process.link.Link;
import ru.bmstu.rk9.rao.ui.process.node.Node;
import ru.bmstu.rk9.rao.ui.process.node.NodeWithLinks;

public class BlockConverter {

	public static List<Block> convertModelToBlocks(Node model) {
		List<Node> children = model.getChildren();
		List<Block> blocks = new ArrayList<Block>();
		Map<Node, BlockConverterInfo> blocksMap = new HashMap<>();
		for (Node node : children) {
			BlockConverterInfo blockInfo;
			if (!blocksMap.containsKey(node)) {
				blockInfo = node.createBlock();
				if (!blockInfo.isSuccessful)
					throw new ProcessParsingException(blockInfo.errorMessage);
				blocks.add(blockInfo.block);
				blocksMap.put(node, blockInfo);
			} else {
				blockInfo = blocksMap.get(node);
			}

			NodeWithLinks nodeWithLinks = (NodeWithLinks) node;
			for (Link sourceLink : nodeWithLinks.getSourceLinks()) {
				Node targetNode = sourceLink.getTargetNode();
				BlockConverterInfo targetBlockInfo;
				if (!blocksMap.containsKey(targetNode)) {
					targetBlockInfo = targetNode.createBlock();
					if (!targetBlockInfo.isSuccessful)
						throw new ProcessParsingException(targetBlockInfo.errorMessage);
					blocks.add(targetBlockInfo.block);
					blocksMap.put(targetNode, targetBlockInfo);
				} else {
					targetBlockInfo = blocksMap.get(targetNode);
				}
				ru.bmstu.rk9.rao.lib.process.Link.linkDocks(blockInfo.outputDocks.get(sourceLink.getSourceDock()),
						targetBlockInfo.inputDocks.get(sourceLink.getTargetDock()));
			}
		}
		return blocks;
	}
}
