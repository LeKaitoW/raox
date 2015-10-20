package ru.bmstu.rk9.rao.ui.process;

import java.util.Map;

import org.eclipse.gef.requests.CreationFactory;

public class NodeCreationFactory implements CreationFactory {

	private Class<?> template;

	public NodeCreationFactory(Class<?> template) {
		this.template = template;
	}

	@Override
	public Object getNewObject() {
		Map<Class<?>, ProcessNodeInfo> processNodesInfo = ProcessEditor.processNodesInfo;
		if (!processNodesInfo.containsKey(template))
			return null;
		return processNodesInfo.get(template).getNodeFactory().get();
		// exception, sysout template
	}

	@Override
	public Object getObjectType() {
		return template;
	}

}
