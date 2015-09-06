package ru.bmstu.rk9.rao.ui.labeling

import com.google.inject.Inject

import org.eclipse.emf.ecore.EObject

import static extension ru.bmstu.rk9.rao.generator.RaoNaming.*

import ru.bmstu.rk9.rao.rao.RaoModel

import ru.bmstu.rk9.rao.rao.ResourceType
import ru.bmstu.rk9.rao.rao.Parameter

import ru.bmstu.rk9.rao.rao.ResourceCreateStatement

import ru.bmstu.rk9.rao.rao.Sequence

import ru.bmstu.rk9.rao.rao.Function
import ru.bmstu.rk9.rao.rao.FunctionParameter

import ru.bmstu.rk9.rao.rao.Constant

import ru.bmstu.rk9.rao.rao.EnumerativeSequence
import ru.bmstu.rk9.rao.rao.RegularSequence
import ru.bmstu.rk9.rao.rao.HistogramSequence

import ru.bmstu.rk9.rao.rao.Event
import ru.bmstu.rk9.rao.rao.RelevantResource

import ru.bmstu.rk9.rao.rao.DecisionPointSome
import ru.bmstu.rk9.rao.rao.DecisionPointSearch
import ru.bmstu.rk9.rao.rao.DecisionPointSearchActivity
import ru.bmstu.rk9.rao.rao.DecisionPointActivity

import ru.bmstu.rk9.rao.rao.Result
import ru.bmstu.rk9.rao.rao.ResultWatchParameter
import ru.bmstu.rk9.rao.rao.ResultWatchState
import ru.bmstu.rk9.rao.rao.ResultWatchQuant
import ru.bmstu.rk9.rao.rao.ResultWatchValue
import ru.bmstu.rk9.rao.rao.ResultGetValue
import ru.bmstu.rk9.rao.rao.DefaultMethod
import ru.bmstu.rk9.rao.rao.Pattern

class RaoLabelProvider extends org.eclipse.xtext.ui.label.DefaultEObjectLabelProvider {

	@Inject
	new(org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider delegate) {
		super(delegate);
	}

	def image(RaoModel model) { "model.gif" }

	def text(DefaultMethod defaultMethod) { "set : " + defaultMethod.name }

	def image(DefaultMethod defaultMethod) { "run.gif" }

	def text(ResourceType resourceType) { "resource type : " + resourceType.name }

	def image(ResourceType resourceType) { "puzzle_plus.gif" }

	def text(Parameter parameter) { parameter.name + parameter.typeGenericLabel }

	def image(Parameter parameter) { "parameter.gif" }

	def text(ResourceCreateStatement resource) { "resource : " + resource.name }

	def image(ResourceCreateStatement resource) { "plus.gif" }

	def text(Constant constant) { constant.name + constant.type.typeGenericLabel }

	def image(Constant constant) { "constant2.gif" }

	def text(Sequence sequence) {
		"sequence : " + sequence.name + " : " + (
		if (sequence.type instanceof EnumerativeSequence)
			"enumerative"
		else
			"" + if (sequence.type instanceof RegularSequence)
				(sequence.type as RegularSequence).type
			else
				"" + if(sequence.type instanceof HistogramSequence) "histogram" else "" ) +
			sequence.returnType.typeGenericLabel
	}

	def image(Sequence sequence) { "chart.gif" }

	def text(Function function) { "function : " + function.type.name + function.returnType.typeGenericLabel }

	def image(Function function) { "calc_arrow.gif" }

	def image(FunctionParameter parameter) { "parameter.gif" }

	def text(FunctionParameter parameter) { parameter.name + parameter.type.typeGenericLabel }

	def text(Event event) { "event : " + event.name + " : event" }

	def image(Event event) { "event.gif" }

	def text(Pattern pattern) { "pattern : " + pattern.name + " : " + pattern.type.literal }

	def image(Pattern pattern) { "script_block.gif" }

	def image(RelevantResource relevantResource) { "parameter.gif" }

	def text(RelevantResource relevantResource) { relevantResource.name + relevantResource.type.relevantResourceName }

	def getRelevantResourceName(EObject object) {
		switch object {
			ResourceType: " : RTP : " + object.name
			ResourceCreateStatement: " : RSS : " + object.name
			default: ""
		}
	}

	def image(DecisionPointSearchActivity activity) { "script_block.gif" }

	def image(DecisionPointActivity activity) { "script_block.gif" }

	def text(DecisionPointSome decisionPoint) { "decision point : " + decisionPoint.name + " : some" }

	def image(DecisionPointSome decisionPoint) { "block.gif" }

	def text(DecisionPointSearch search) { "search : " + search.name + " : search" }

	def image(DecisionPointSearch search) { "search.gif" }

	def text(Result result) { result.name + " : " + resultype(result) }

	def resultype(Result declaration) {
		switch declaration.type {
			ResultWatchParameter: "watchParameter"
			ResultWatchState: "watchState"
			ResultWatchQuant: "watchQuant"
			ResultWatchValue: "watchValue"
			ResultGetValue: "getValue"
		}
	}

	def image(Result result) { "parameter.gif" }
}
