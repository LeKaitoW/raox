package ru.bmstu.rk9.rao.ui.process.blocks;

import java.util.Map;

import org.eclipse.gef.requests.CreationFactory;

import ru.bmstu.rk9.rao.ui.gef.NodeInfo;
import ru.bmstu.rk9.rao.ui.process.ProcessEditor;

public class BlockNodeFactory implements CreationFactory {

	private Class<?> template;

	public BlockNodeFactory(Class<?> template) {
		this.template = template;
	}

	@Override
	public Object getNewObject() {
		Map<Class<?>, NodeInfo> processNodesInfo = ProcessEditor.processNodesInfo;
		NodeInfo processNode = processNodesInfo.get(template);
		if (processNode == null)
			return null;
		return processNode.getNodeFactory().get();
		// exception, sysout template
	}

	@Override
	public Object getObjectType() {
		return template;
	}

}
