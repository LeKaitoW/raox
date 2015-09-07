package ru.bmstu.rk9.rao.ui.contentassist

import ru.bmstu.rk9.rao.ui.contentassist.AbstractRaoProposalProvider

import org.eclipse.xtext.Keyword
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor

class RaoProposalProvider extends AbstractRaoProposalProvider
{
	override completeKeyword(Keyword keyword, ContentAssistContext context, ICompletionProposalAcceptor acceptor)
	{
		super.completeKeyword(keyword, context, acceptor)
	}

	 // USAGE
	// acceptor.accept(createCompletionProposal(text to insert, description text, image, context))
}
