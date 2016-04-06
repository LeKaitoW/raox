package ru.bmstu.rk9.rao.ui.process.node;

import java.util.Map;

import org.eclipse.gef.requests.CreationFactory;

import ru.bmstu.rk9.rao.ui.process.ProcessEditor;
import ru.bmstu.rk9.rao.ui.process.ProcessNodeInfo;

public class NodeCreationFactory implements CreationFactory {

	private Class<?> template;

	public NodeCreationFactory(Class<?> template) {
		this.template = template;
	}

	@Override
	public Object getNewObject() {
		Map<Class<?>, ProcessNodeInfo> processNodesInfo = ProcessEditor.processNodesInfo;
		ProcessNodeInfo processNode = processNodesInfo.get(template);
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
