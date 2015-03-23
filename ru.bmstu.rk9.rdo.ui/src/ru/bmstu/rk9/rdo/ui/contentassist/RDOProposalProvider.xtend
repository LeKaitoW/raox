package ru.bmstu.rk9.rdo.ui.contentassist

import ru.bmstu.rk9.rdo.ui.contentassist.AbstractRDOProposalProvider

import org.eclipse.xtext.Keyword
import org.eclipse.xtext.Assignment
import org.eclipse.xtext.CrossReference
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor

import org.eclipse.emf.ecore.EObject

import ru.bmstu.rk9.rdo.rdo.RDORTPParameterSuchAs

import ru.bmstu.rk9.rdo.rdo.Constant


class RDOProposalProvider extends AbstractRDOProposalProvider
{
	override completeKeyword(Keyword keyword, ContentAssistContext context, ICompletionProposalAcceptor acceptor)
	{
		if(RDOKeywordProposalValidator.check(keyword.value, context.previousModel))
			super.completeKeyword(keyword, context, acceptor)
	}

	 // USAGE
	// acceptor.accept(createCompletionProposal(text to insert, description text, image, context))

	override completeRDOSuchAs_Type(
		EObject element,
		Assignment assignment,
		ContentAssistContext context,
		ICompletionProposalAcceptor acceptor)
	{
		val container = switch context.previousModel.eContainer
			{
				RDORTPParameterSuchAs:
					context.previousModel.eContainer.eContainer
				Constant:
					context.previousModel.eContainer
				default: null
			}
		lookupCrossReference(assignment.terminal as CrossReference, context, acceptor, [r |
			(r.EObjectOrProxy as EObject) != container ])
	}
}
