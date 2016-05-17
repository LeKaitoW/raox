package ru.bmstu.rk9.rao.formatting2;

import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.formatting2.IFormattableDocument
import org.eclipse.xtext.xbase.formatting2.XbaseFormatter
import org.eclipse.xtext.xbase.formatting2.XbaseFormatterPreferenceKeys
import ru.bmstu.rk9.rao.rao.DefaultMethod
import ru.bmstu.rk9.rao.rao.EntityCreation
import ru.bmstu.rk9.rao.rao.EnumDeclaration
import ru.bmstu.rk9.rao.rao.Event
import ru.bmstu.rk9.rao.rao.FieldDeclaration
import ru.bmstu.rk9.rao.rao.Frame
import ru.bmstu.rk9.rao.rao.FunctionDeclaration
import ru.bmstu.rk9.rao.rao.Generator
import ru.bmstu.rk9.rao.rao.Logic
import ru.bmstu.rk9.rao.rao.Pattern
import ru.bmstu.rk9.rao.rao.RelevantResource
import ru.bmstu.rk9.rao.rao.RelevantResourceTuple
import ru.bmstu.rk9.rao.rao.ResourceDeclaration
import ru.bmstu.rk9.rao.rao.ResourceType
import ru.bmstu.rk9.rao.rao.Search

class RaoFormatter extends XbaseFormatter {
	def dispatch void format(Event event, extension IFormattableDocument document) {
		formatAsBlock(event, document)

		for (parameter : event.parameters)
			format(parameter, document)

		format(event.body, document)

		event.body.append(XbaseFormatterPreferenceKeys.blankLinesAroundExpression)
	}

	def dispatch void format(Pattern pattern, extension IFormattableDocument document) {
		formatAsBlock(pattern, document)

		for (parameter : pattern.parameters)
			format(parameter, document)

		for (relevantResource : pattern.relevantResources) {
			relevantResource.append(XbaseFormatterPreferenceKeys.blankLinesAroundExpression)
			format(relevantResource, document)
		}

		for (tuple : pattern.relevantTuples) {
			tuple.append(XbaseFormatterPreferenceKeys.blankLinesAroundExpression)
			format(tuple, document)
		}

		for (defaultMethod : pattern.defaultMethods) {
			defaultMethod.append(XbaseFormatterPreferenceKeys.blankLinesAroundExpression)
			format(defaultMethod, document)
		}
	}

	def dispatch void format(RelevantResource relevantResource, extension IFormattableDocument document) {
		format(relevantResource.value, document)
	}

	def dispatch void format(RelevantResourceTuple tuple, extension IFormattableDocument document) {
		format(tuple.value, document)
	}

	def dispatch void format(Logic logic, extension IFormattableDocument document) {
		formatAsBlock(logic, document)

		for (activity : logic.activities) {
			activity.append(XbaseFormatterPreferenceKeys.blankLinesAroundExpression)
			format(activity, document)
		}

		for (defaultMethod : logic.defaultMethods) {
			defaultMethod.append(XbaseFormatterPreferenceKeys.blankLinesAroundExpression)
			format(defaultMethod, document)
		}
	}

	def dispatch void format(Search search, extension IFormattableDocument document) {
		formatAsBlock(search, document)

		for (edge : search.edges) {
			edge.append(XbaseFormatterPreferenceKeys.blankLinesAroundExpression)
			format(edge, document)
		}

		for (defaultMethod : search.defaultMethods) {
			defaultMethod.append(XbaseFormatterPreferenceKeys.blankLinesAroundExpression)
			format(defaultMethod, document)
		}
	}

	def dispatch void format(ResourceType type, extension IFormattableDocument document) {
		formatAsBlock(type, document)

		for (parameter : type.parameters) {
			parameter.append(XbaseFormatterPreferenceKeys.blankLinesAroundExpression)
			format(parameter, document)
		}
	}

	def dispatch void format(FieldDeclaration fieldDeclaration, extension IFormattableDocument document) {
		format(fieldDeclaration.declaration, document)
		format(fieldDeclaration.^default, document)
	}

	def dispatch void format(EnumDeclaration enumDeclaration, extension IFormattableDocument document) {
		enumDeclaration.append(XbaseFormatterPreferenceKeys.blankLinesAroundExpression)
		enumDeclaration.regionFor.keywords(",").forEach[prepend[noSpace].append[oneSpace]]
	}

	def dispatch void format(FunctionDeclaration function, extension IFormattableDocument document) {
		formatAsBlock(function, document)

		for (parameter : function.parameters)
			format(parameter, document)

		format(function.body, document)

		function.body.append(XbaseFormatterPreferenceKeys.blankLinesAroundExpression)
	}

	def dispatch void format(Generator generator, extension IFormattableDocument document) {
		formatAsBlock(generator, document)

		for (parameter : generator.parameters)
			format(parameter, document)

		format(generator.body, document)

		generator.body.append(XbaseFormatterPreferenceKeys.blankLinesAroundExpression)
	}

	def dispatch void format(Frame frame, extension IFormattableDocument document) {
		formatAsBlock(frame, document)

		for (defaultMethod : frame.defaultMethods) {
			defaultMethod.append(XbaseFormatterPreferenceKeys.blankLinesAroundExpression)
			format(defaultMethod, document)
		}
	}

	def dispatch void format(ResourceDeclaration resourceDeclaration, extension IFormattableDocument document) {
		resourceDeclaration.append(XbaseFormatterPreferenceKeys.blankLinesAroundExpression)
		format(resourceDeclaration.constructor, document)
	}

	def dispatch void format(EntityCreation entityCreation, extension IFormattableDocument document) {
		entityCreation.append(XbaseFormatterPreferenceKeys.blankLinesAroundExpression)
		format(entityCreation.constructor, document)
	}

	def formatAsBlock(EObject obj, extension IFormattableDocument document) {
		val open = obj.regionFor.keyword("{")
		val close = obj.regionFor.keyword("}")

		if (obj.eContainer == null)
			obj.surround[noSpace]

		interior(open, close)[indent]
		open.prepend(XbaseFormatterPreferenceKeys.bracesInNewLine)
		open.append(XbaseFormatterPreferenceKeys.blankLinesAroundExpression)

		close.append(XbaseFormatterPreferenceKeys.blankLinesAroundExpression)
	}

	def dispatch void format(DefaultMethod defaultMethod, extension IFormattableDocument document) {
		defaultMethod.append(XbaseFormatterPreferenceKeys.blankLinesAroundExpression)
		format(defaultMethod.body, document)
	}
}
