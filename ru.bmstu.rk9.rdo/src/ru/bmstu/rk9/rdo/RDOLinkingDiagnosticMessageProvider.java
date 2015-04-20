package ru.bmstu.rk9.rdo;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.xtext.diagnostics.Diagnostic;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.diagnostics.DiagnosticMessage;
import org.eclipse.xtext.linking.impl.LinkingDiagnosticMessageProvider;

import ru.bmstu.rk9.rdo.rdo.ResourceType;
import ru.bmstu.rk9.rdo.rdo.ResourceTypeParameter;

public class RDOLinkingDiagnosticMessageProvider extends
		LinkingDiagnosticMessageProvider {

	@Override
	public DiagnosticMessage getUnresolvedProxyMessage(
			ILinkingDiagnosticContext context) {

		EClass referenceType = context.getReference().getEReferenceType();
		String msg;
		switch (referenceType.getName()) {

		case "META_SuchAs":
			msg = "Couldn't resolve such_as reference to '"
					+ context.getLinkText() + "'.";
			break;

		case "META_TypeDeclaration":
			msg = "Couldn't find type with name '" + context.getLinkText()
					+ "'.";
			break;

		case "META_RelResType":
			msg = "Couldn't find resource type or resource with name '"
					+ context.getLinkText() + "'.";
			break;

		case "RuleRelevantResource":
			msg = "Couldn't find relevant resorce with name '"
					+ context.getLinkText() + "'.";
			break;

		case "EnumID":
			ResourceTypeParameter parent = (ResourceTypeParameter) context
					.getContext().eContainer();
			ResourceType grandparent = (ResourceType) parent.eContainer();
			msg = "Value '" + context.getLinkText()
					+ "' not found in enumerative resource type parameter "
					+ grandparent.getName() + "." + parent.getName() + ".";
			break;

		case "ResourceType":
			msg = "Couldn't find resource type with name '"
					+ context.getLinkText() + "'.";
			break;

		default:
			msg = "Couldn't find " + referenceType.getName() + " with name '"
					+ context.getLinkText() + "'.";

		}

		return new DiagnosticMessage(msg, Severity.ERROR,
				Diagnostic.LINKING_DIAGNOSTIC);
	}
}
