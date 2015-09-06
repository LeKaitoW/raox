package ru.bmstu.rk9.rao;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.xtext.diagnostics.Diagnostic;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.diagnostics.DiagnosticMessage;
import org.eclipse.xtext.linking.impl.LinkingDiagnosticMessageProvider;

import ru.bmstu.rk9.rao.rao.Parameter;
import ru.bmstu.rk9.rao.rao.ResourceType;

public class RaoLinkingDiagnosticMessageProvider extends
		LinkingDiagnosticMessageProvider {

	@Override
	public DiagnosticMessage getUnresolvedProxyMessage(
			ILinkingDiagnosticContext context) {

		EClass referenceType = context.getReference().getEReferenceType();
		String msg;
		switch (referenceType.getName()) {

		case "META_TypeDeclaration":
			msg = "Couldn't find type with name '" + context.getLinkText()
					+ "'.";
			break;

		case "META_RelResType":
			msg = "Couldn't find resource type or resource with name '"
					+ context.getLinkText() + "'.";
			break;

		case "RelevantResource":
			msg = "Couldn't find relevant resorce with name '"
					+ context.getLinkText() + "'.";
			break;

		case "EnumID":
			Parameter parent = (Parameter) context
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
