package ru.bmstu.rk9.rao;

import org.eclipse.xtext.naming.DefaultDeclarativeQualifiedNameProvider;
import org.eclipse.xtext.naming.QualifiedName;

import ru.bmstu.rk9.rao.rao.RaoModel;

public class RaoQualifiedNameProvider extends
		DefaultDeclarativeQualifiedNameProvider {

	public static String filenameFromURI(RaoModel model) {
		if (model == null)
			return "";

		String name = model.eResource().getURI().lastSegment();
		if (name.endsWith(".rao"))
			name = name.substring(0, name.length() - 4);
		name.replace(".", "_");

		return name;
	}

	QualifiedName qualifiedName(RaoModel model) {
		return QualifiedName.create(filenameFromURI(model));
	}
}
