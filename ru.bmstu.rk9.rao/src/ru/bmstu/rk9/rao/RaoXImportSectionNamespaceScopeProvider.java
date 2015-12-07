package ru.bmstu.rk9.rao;

import java.util.List;

import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.scoping.impl.ImportNormalizer;
import org.eclipse.xtext.xbase.scoping.XImportSectionNamespaceScopeProvider;

import com.google.common.collect.Lists;

@SuppressWarnings("restriction")
public class RaoXImportSectionNamespaceScopeProvider extends XImportSectionNamespaceScopeProvider {
	public static final QualifiedName RAO_RT_LIB = QualifiedName.create("ru", "bmstu", "rk9", "rao", "lib", "runtime");

	@Override
	protected List<ImportNormalizer> getImplicitImports(boolean ignoreCase) {
		List<ImportNormalizer> imports = Lists
				.<ImportNormalizer> newArrayList(doCreateImportNormalizer(RAO_RT_LIB, true, false));

		imports.addAll(super.getImplicitImports(ignoreCase));
		return imports;
	}
}
