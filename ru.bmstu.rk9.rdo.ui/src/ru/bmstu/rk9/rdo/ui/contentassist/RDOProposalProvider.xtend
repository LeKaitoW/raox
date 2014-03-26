package ru.bmstu.rk9.rdo.ui.contentassist

import ru.bmstu.rk9.rdo.ui.contentassist.AbstractRDOProposalProvider

import org.eclipse.xtext.Keyword
import org.eclipse.xtext.Assignment
import org.eclipse.xtext.CrossReference
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor

import org.eclipse.emf.ecore.EObject
import ru.bmstu.rk9.rdo.rdo.Resources

class RDOProposalProvider extends AbstractRDOProposalProvider
{
	override completeKeyword(Keyword keyword, ContentAssistContext context, ICompletionProposalAcceptor acceptor)
	{
		if (RDOKeywordProposalValidator.check(keyword.value, context.previousModel))
			super.completeKeyword(keyword, context, acceptor)
	}
	
	 // USAGE 
	// acceptor.accept(createCompletionProposal(text to insert, description text, image, context))

	override completeResourceTrace_Trace(
		EObject element,
		Assignment assignment,
		ContentAssistContext context,
		ICompletionProposalAcceptor acceptor)
	{
		lookupCrossReference(assignment.terminal as CrossReference, context, acceptor, [r |
			!(context.previousModel.eContainer as Resources).trace.map[t | t.trace].contains(r.EObjectOrProxy as EObject) &&
		     (context.previousModel.eContainer as Resources).resources.contains(r.EObjectOrProxy as EObject) ])
	}
}
