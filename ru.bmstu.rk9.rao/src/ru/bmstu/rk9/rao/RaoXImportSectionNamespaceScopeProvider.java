package ru.bmstu.rk9.rao;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.scoping.impl.ImportNormalizer;
import org.eclipse.xtext.xbase.scoping.XImportSectionNamespaceScopeProvider;

import com.google.common.collect.Lists;

import ru.bmstu.rk9.rao.rao.EnumDeclaration;
import ru.bmstu.rk9.rao.rao.RaoEntity;
import ru.bmstu.rk9.rao.rao.RaoModel;
import ru.bmstu.rk9.rao.rao.ResourceType;

@SuppressWarnings("restriction")
public class RaoXImportSectionNamespaceScopeProvider extends XImportSectionNamespaceScopeProvider {
	public static final List<QualifiedName> raoLibImports = Lists.<QualifiedName>newArrayList(
			QualifiedName.create("ru", "bmstu", "rk9", "rao", "lib", "runtime"),
			QualifiedName.create("ru", "bmstu", "rk9", "rao", "lib", "sequence"),
			QualifiedName.create("ru", "bmstu", "rk9", "rao", "lib", "dpt"),
			QualifiedName.create("ru", "bmstu", "rk9", "rao", "lib", "animation"),
			QualifiedName.create("ru", "bmstu", "rk9", "rao", "lib", "result"));

	@Override
	protected List<ImportNormalizer> internalGetImportedNamespaceResolvers(EObject context, boolean ignoreCase) {
		List<ImportNormalizer> result = super.internalGetImportedNamespaceResolvers(context, ignoreCase);

		if (context instanceof RaoModel) {
			if (result.isEmpty())
				result = new ArrayList<>();

			RaoModel model = (RaoModel) context;

			// TODO what we need here is this string:
			// "<project_name>.<model_name>"
			// maybe there are prettier ways to do that?
			String platformString = model.eResource().getURI().toPlatformString(true);
			String prefix = platformString.substring(1, platformString.length() - ".rao".length()).replace("/", ".");

			for (RaoEntity object : model.getObjects()) {
				if (object instanceof ResourceType || object instanceof EnumDeclaration
						|| object instanceof EnumDeclaration) {
					ImportNormalizer resolver = createImportedNamespaceResolver(prefix + "." + object.getName(),
							ignoreCase);
					if (resolver != null)
						result.add(resolver);
				}
			}
		}

		return result;
	}

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
