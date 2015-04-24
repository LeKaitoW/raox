package ru.bmstu.rk9.rdo.ui.contentassist

import ru.bmstu.rk9.rdo.ui.contentassist.AbstractRDOProposalProvider

import org.eclipse.xtext.Keyword
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor

class RDOProposalProvider extends AbstractRDOProposalProvider
{
	override completeKeyword(Keyword keyword, ContentAssistContext context, ICompletionProposalAcceptor acceptor)
	{
		if(RDOKeywordProposalValidator.check(keyword.value, context.previousModel))
			super.completeKeyword(keyword, context, acceptor)
	}

	 // USAGE
	// acceptor.accept(createCompletionProposal(text to insert, description text, image, context))
}
