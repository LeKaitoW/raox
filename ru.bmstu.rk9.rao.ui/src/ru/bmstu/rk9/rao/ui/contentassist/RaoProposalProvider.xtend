package ru.bmstu.rk9.rao.ui.contentassist

import ru.bmstu.rk9.rao.ui.contentassist.AbstractRaoProposalProvider

import org.eclipse.xtext.Keyword
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.Assignment
import ru.bmstu.rk9.rao.rao.Pattern
import ru.bmstu.rk9.rao.rao.DefaultMethod
import ru.bmstu.rk9.rao.validation.DefaultMethodsHelper
import ru.bmstu.rk9.rao.rao.RaoModel
import ru.bmstu.rk9.rao.rao.Logic
import ru.bmstu.rk9.rao.rao.Search
import ru.bmstu.rk9.rao.rao.Frame

class RaoProposalProvider extends AbstractRaoProposalProvider {
	override completeKeyword(Keyword keyword, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		super.completeKeyword(keyword, context, acceptor)
	}

	override completeDefaultMethod_Name(EObject model, Assignment assignment, ContentAssistContext context,
		ICompletionProposalAcceptor acceptor) {
		var EObject toComplete

		if (model instanceof DefaultMethod) {
			toComplete = model.eContainer
		} else {
			toComplete = model
		}

		internalCompleteDefaultMethod_Name(toComplete, context, acceptor)
	}

	def dispatch internalCompleteDefaultMethod_Name(Pattern pattern, ContentAssistContext context,
		ICompletionProposalAcceptor acceptor) {

		switch (pattern.type) {
			case OPERATION: {
				for (value : DefaultMethodsHelper.OperationMethodInfo.values) {
					acceptor.accept(createCompletionProposal(value.name, context))
				}
			}
			case RULE: {
				for (value : DefaultMethodsHelper.RuleMethodInfo.values) {
					acceptor.accept(createCompletionProposal(value.name, context))
				}
			}
		}
	}

	def dispatch internalCompleteDefaultMethod_Name(Frame frame, ContentAssistContext context,
		ICompletionProposalAcceptor acceptor) {

		for (value : DefaultMethodsHelper.FrameMethodInfo.values) {
			acceptor.accept(createCompletionProposal(value.name, context))
		}
	}

	def dispatch internalCompleteDefaultMethod_Name(Logic logic, ContentAssistContext context,
		ICompletionProposalAcceptor acceptor) {

		for (value : DefaultMethodsHelper.DptMethodInfo.values) {
			acceptor.accept(createCompletionProposal(value.name, context))
		}
	}

	def dispatch internalCompleteDefaultMethod_Name(Search search, ContentAssistContext context,
		ICompletionProposalAcceptor acceptor) {

		for (value : DefaultMethodsHelper.DptMethodInfo.values) {
			acceptor.accept(createCompletionProposal(value.name, context))
		}
	}

	def dispatch internalCompleteDefaultMethod_Name(RaoModel model, ContentAssistContext context,
		ICompletionProposalAcceptor acceptor) {

		for (value : DefaultMethodsHelper.GlobalMethodInfo.values) {
			acceptor.accept(createCompletionProposal(value.name, context))
		}
	}
}
