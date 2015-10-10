package ru.bmstu.rk9.rao.ui.process;

import org.eclipse.gef.requests.CreationFactory;

import ru.bmstu.rk9.rao.ui.process.advance.Advance;
import ru.bmstu.rk9.rao.ui.process.generate.Generate;
import ru.bmstu.rk9.rao.ui.process.release.Release;
import ru.bmstu.rk9.rao.ui.process.resource.Resource;
import ru.bmstu.rk9.rao.ui.process.seize.Seize;
import ru.bmstu.rk9.rao.ui.process.terminate.Terminate;

public class NodeCreationFactory implements CreationFactory {

	private Class<?> template;

	public NodeCreationFactory(Class<?> template) {
		this.template = template;
	}

	@Override
	public Object getNewObject() {
		if (template == null)
			return null;
		if (template == Generate.class) {
			Generate generate = new Generate();
			return generate;
		}
		if (template == Advance.class) {
			Advance advance = new Advance();
			return advance;
		}
		if (template == Release.class) {
			Release release = new Release();
			return release;
		}
		if (template == Resource.class) {
			Resource resource = new Resource();
			return resource;
		}
		if (template == Seize.class) {
			Seize seize = new Seize();
			return seize;
		}
		if (template == Terminate.class) {
			Terminate terminate = new Terminate();
			return terminate;
		}
		return null;
	}

	@Override
	public Object getObjectType() {
		return template;
	}

}
