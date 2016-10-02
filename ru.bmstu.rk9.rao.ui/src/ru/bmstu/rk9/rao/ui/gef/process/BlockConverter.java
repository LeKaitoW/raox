package ru.bmstu.rk9.rao.ui.gef.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.bmstu.rk9.rao.lib.process.Block;
import ru.bmstu.rk9.rao.ui.execution.ModelContentsInfo;
import ru.bmstu.rk9.rao.ui.gef.Node;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.BlockNode;
import ru.bmstu.rk9.rao.ui.gef.process.connection.Connection;

public class BlockConverter {

	public static List<Block> convertModelToBlocks(Node model, ModelContentsInfo modelContentsInfo) {
		List<Node> children = model.getChildren();
		List<Block> blocks = new ArrayList<Block>();
		Map<BlockNode, BlockConverterInfo> blockNodes = new HashMap<>();
		for (Node node : children) {
			if (!(node instanceof BlockNode))
				continue;

			BlockNode sourceBlockNode = (BlockNode) node;
			BlockConverterInfo sourceBlockInfo;
			if (blockNodes.containsKey(sourceBlockNode)) {
				sourceBlockInfo = blockNodes.get(sourceBlockNode);
			} else {
				sourceBlockInfo = sourceBlockNode.createBlock(modelContentsInfo);
				if (!sourceBlockInfo.isSuccessful)
					throw new ProcessParsingException(sourceBlockInfo.errorMessage);
				blocks.add(sourceBlockInfo.block);
				blockNodes.put(sourceBlockNode, sourceBlockInfo);
			}

			for (Connection sourceConnection : sourceBlockNode.getSourceConnections()) {
				BlockNode targetBlockNode = sourceConnection.getTargetBlockNode();
				BlockConverterInfo targetBlockInfo;
				if (blockNodes.containsKey(targetBlockNode)) {
					targetBlockInfo = blockNodes.get(targetBlockNode);
				} else {
					targetBlockInfo = targetBlockNode.createBlock(modelContentsInfo);
					if (!targetBlockInfo.isSuccessful)
						throw new ProcessParsingException(targetBlockInfo.errorMessage);
					blocks.add(targetBlockInfo.block);
					blockNodes.put(targetBlockNode, targetBlockInfo);
				}

				ru.bmstu.rk9.rao.lib.process.Connection.linkDocks(
						sourceBlockInfo.outputDocks.get(sourceConnection.getSourceDockName()),
						targetBlockInfo.inputDocks.get(sourceConnection.getTargetDockName()));
			}
		}
		return blocks;
	}
}
