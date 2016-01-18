package ru.bmstu.rk9.rao;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.scoping.impl.ImportNormalizer;
import org.eclipse.xtext.xbase.scoping.XImportSectionNamespaceScopeProvider;

import com.google.common.collect.Lists;

@SuppressWarnings("restriction")
public class RaoXImportSectionNamespaceScopeProvider extends XImportSectionNamespaceScopeProvider {
	public static final List<QualifiedName> raoLibImports = Lists.<QualifiedName> newArrayList(
			QualifiedName.create("ru", "bmstu", "rk9", "rao", "lib", "runtime"),
			QualifiedName.create("ru", "bmstu", "rk9", "rao", "lib", "sequence"));

	@Override
	protected List<ImportNormalizer> getImplicitImports(boolean ignoreCase) {
		List<ImportNormalizer> imports = new ArrayList<>();
		for (QualifiedName qualifiendName : raoLibImports) {
			imports.add(doCreateImportNormalizer(qualifiendName, true, false));
		}

		imports.addAll(super.getImplicitImports(ignoreCase));
		return imports;
	}
}
