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

	// Model
	def image(RaoModel m) { "model.gif" }

	// Default methods
	def text(DefaultMethod dm) { "set : " + dm.name }
	def image(DefaultMethod dm) { "run.gif" }

	// Resource types
	def  text(ResourceType rtp) { "RTP : " + rtp.name }
	def image(ResourceType rtp) { "puzzle_plus.gif" }

	// Parameter types
	def  text(Parameter p) { p.name + p.typeGenericLabel }
	def image(Parameter p) { "parameter.gif" }

	// Resource declaration
	def  text(ResourceCreateStatement rss) { "rss : " + rss.name }
	def image(ResourceCreateStatement rss) { "plus.gif" }

	// Constants
	def image(Constant c) { "constant2.gif" }
	def  text(Constant c) { c.name + c.type.typeGenericLabel }

	// Sequence
	def  text(Sequence seq) { "SEQ : " + seq.name + " : " + (
		if(seq.type instanceof EnumerativeSequence) "enumerative" else "" +
		if(seq.type instanceof RegularSequence) (seq.type as RegularSequence).type else "" +
		if(seq.type instanceof HistogramSequence) "histogram" else "" ) + seq.returnType.typeGenericLabel }
	def image(Sequence seq) { "chart.gif" }

	// Function
	def  text(Function fun) { "FUN : " + fun.type.name + fun.returnType.typeGenericLabel }
	def image(Function fun) { "calc_arrow.gif" }

	def image(FunctionParameter p) { "parameter.gif" }
	def  text(FunctionParameter p) { p.name + p.type.typeGenericLabel }

	// Event
	def  text(Event evn) { "EVN : " + evn.name + " : event"}
	def image(Event evn) { "event.gif" }

	// Operation
	def  text(Pattern pat) { "PAT : " + pat.name + " : " + pat.type.literal}
	def image(Pattern pat) { "script_block.gif" }

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
