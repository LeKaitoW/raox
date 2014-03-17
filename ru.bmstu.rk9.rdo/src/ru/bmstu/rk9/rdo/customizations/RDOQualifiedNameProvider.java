package ru.bmstu.rk9.rdo.customizations;

import org.eclipse.xtext.naming.DefaultDeclarativeQualifiedNameProvider;
import org.eclipse.xtext.naming.QualifiedName;

import ru.bmstu.rk9.rdo.rdo.RDOModel;

public class RDOQualifiedNameProvider extends DefaultDeclarativeQualifiedNameProvider{
	
	public static String computeFromURI(RDOModel e) {
		String name = e.eResource().getURI().lastSegment();
		if (name.endsWith(".rdo"))
			name = name.substring(0, name.length() - 4);
		name.replace(".", "_");
		return name;
	}
	
	QualifiedName qualifiedName(RDOModel e) {
		return QualifiedName.create(computeFromURI(e));
	}
}
