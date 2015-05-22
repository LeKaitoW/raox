package ru.bmstu.rk9.rdo.ui.labeling

import com.google.inject.Inject

import org.eclipse.emf.ecore.EObject

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*

import ru.bmstu.rk9.rdo.rdo.RDOModel

import ru.bmstu.rk9.rdo.rdo.ResourceType
import ru.bmstu.rk9.rdo.rdo.ParameterType

import ru.bmstu.rk9.rdo.rdo.Resources
import ru.bmstu.rk9.rdo.rdo.ResourceCreateStatement

import ru.bmstu.rk9.rdo.rdo.Sequence

import ru.bmstu.rk9.rdo.rdo.Function
import ru.bmstu.rk9.rdo.rdo.FunctionParameter

import ru.bmstu.rk9.rdo.rdo.Constant

import ru.bmstu.rk9.rdo.rdo.EnumerativeSequence
import ru.bmstu.rk9.rdo.rdo.RegularSequence
import ru.bmstu.rk9.rdo.rdo.HistogramSequence

import ru.bmstu.rk9.rdo.rdo.Event
import ru.bmstu.rk9.rdo.rdo.Operation
import ru.bmstu.rk9.rdo.rdo.Rule
import ru.bmstu.rk9.rdo.rdo.RelevantResource

import ru.bmstu.rk9.rdo.rdo.DecisionPointSome
import ru.bmstu.rk9.rdo.rdo.DecisionPointSearch
import ru.bmstu.rk9.rdo.rdo.DecisionPointSearchActivity
import ru.bmstu.rk9.rdo.rdo.DecisionPointActivity

import ru.bmstu.rk9.rdo.rdo.Result
import ru.bmstu.rk9.rdo.rdo.ResultWatchParameter
import ru.bmstu.rk9.rdo.rdo.ResultWatchState
import ru.bmstu.rk9.rdo.rdo.ResultWatchQuant
import ru.bmstu.rk9.rdo.rdo.ResultWatchValue
import ru.bmstu.rk9.rdo.rdo.ResultGetValue
import ru.bmstu.rk9.rdo.rdo.DefaultMethod

class RDOLabelProvider extends org.eclipse.xtext.ui.label.DefaultEObjectLabelProvider {

	@Inject
	new(org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider delegate) {
		super(delegate);
	}

	// Model
	def image(RDOModel m) { "model.gif" }

	//Default methods
	def text(DefaultMethod dm) { "set : " + dm.method.name }
	def image(DefaultMethod dm) { "run.gif" }

	// Resource types
	def  text(ResourceType rtp) { "RTP : " + rtp.name }
	def image(ResourceType rtp) { "puzzle_plus.gif" }

	//Parameter types
	def  text(ParameterType p) { p.name + p.typeGenericLabel }
	def image(ParameterType p) { "parameter.gif" }

	// Resource declaration
	def  text(Resources rss) { "rss : " + rss.resource.name }
	def image(Resources rss) { "plus.gif" }

	// Constants
	def image(Constant c) { "constant2.gif" }
	def  text(Constant c) { c.name + c.type.typeGenericLabel }

	// Sequence
	def  text(Sequence seq) { "SEQ : " + seq.name + " : " + (
		if(seq.type instanceof EnumerativeSequence) "enumerative" else "" +
		if(seq.type instanceof RegularSequence) (seq.type as RegularSequence).type else "" +
		if(seq.type instanceof HistogramSequence) "histogram" else "" ) + seq.returntype.typeGenericLabel }
	def image(Sequence seq) { "chart.gif" }

	// Function
	def  text(Function fun) { "FUN : " + fun.type.name + fun.returntype.typeGenericLabel }
	def image(Function fun) { "calc_arrow.gif" }

	def image(FunctionParameter p) { "parameter.gif" }
	def  text(FunctionParameter p) { p.name + p.type.typeGenericLabel }

	// Event
	def  text(Event evn) { "EVN : " + evn.name + " : event"}
	def image(Event evn) { "event.gif" }

	// Operation
	def  text(Operation pat) { "PAT : " + pat.name + " : " + pat.type.literal}
	def image(Operation pat) { "script_block.gif" }

	// Rule
	def  text(Rule pat) { "PAT : " + pat.name + " : rule"}
	def image(Rule pat) { "script_block.gif" }

	// Common for patterns

	def image(RelevantResource r) { "parameter.gif" }
	def  text(RelevantResource r) { r.name + r.type.relResName }

	def getRelResName(EObject object) {
		switch object {
			ResourceType: " : RTP : " + object.name
			ResourceCreateStatement: " : RSS : " + object.name
			default: ""
		}
	}

	// Decision points
	def image(DecisionPointSearchActivity d) { "script_block.gif" }
	def image(DecisionPointActivity d) { "script_block.gif" }

	// DecisionPointSome
	def  text(DecisionPointSome dpt) { "DPT : " + dpt.name + " : some" }
	def image(DecisionPointSome dpt) { "block.gif" }

	// DecisionPointSearch
	def  text(DecisionPointSearch dpt) {"DPT : " + dpt.name + " : search" }
	def image(DecisionPointSearch dpt) { "search.gif" }

	// Results
	def  text(Result d) { d.name + " : " + resultype(d)}
	def resultype(Result declaration) {
		switch declaration.type {
			ResultWatchParameter: "watchPar"
			ResultWatchState    : "watchState"
			ResultWatchQuant    : "watchQuant"
			ResultWatchValue    : "watchValue"
			ResultGetValue      : "getValue"
		}
	}
	def image(Result d) { "parameter.gif" }
}
