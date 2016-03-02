package ru.bmstu.rk9.rao.ui.highlightning;

import org.eclipse.xtext.TerminalRule;
import org.eclipse.xtext.xbase.ui.highlighting.XbaseHighlightingCalculator;

import ru.bmstu.rk9.rao.services.RaoGrammarAccess;

import com.google.inject.Inject;

@SuppressWarnings({ "restriction", "deprecation" })
public class RaoHighlightningCalculator extends XbaseHighlightingCalculator {
	@Inject
	RaoGrammarAccess raoGrammarAccess;

	@Override
	protected TerminalRule getIDRule() {
		return raoGrammarAccess.getIDRule();
	}
}
