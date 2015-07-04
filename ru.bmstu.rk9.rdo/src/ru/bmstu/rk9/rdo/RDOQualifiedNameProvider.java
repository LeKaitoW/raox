package ru.bmstu.rk9.rdo;

import org.eclipse.xtext.naming.DefaultDeclarativeQualifiedNameProvider;
import org.eclipse.xtext.naming.QualifiedName;

import ru.bmstu.rk9.rdo.rdo.RDOModel;

public class RDOQualifiedNameProvider extends
		DefaultDeclarativeQualifiedNameProvider {

	public static String filenameFromURI(RDOModel model) {
		if (model == null)
			return "";

		String name = model.eResource().getURI().lastSegment();
		if (name.endsWith(".rdo"))
			name = name.substring(0, name.length() - 4);
		name.replace(".", "_");

		return name;
	}

	QualifiedName qualifiedName(RDOModel model) {
		return QualifiedName.create(filenameFromURI(model));
	}
}
