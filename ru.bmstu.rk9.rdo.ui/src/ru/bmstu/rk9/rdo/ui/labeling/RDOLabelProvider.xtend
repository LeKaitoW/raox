package ru.bmstu.rk9.rdo.ui.labeling

import com.google.inject.Inject

import org.eclipse.emf.ecore.EObject

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*

import ru.bmstu.rk9.rdo.rdo.RDOModel

import ru.bmstu.rk9.rdo.rdo.ResourceType
import ru.bmstu.rk9.rdo.rdo.ResourceTypeParameter

import ru.bmstu.rk9.rdo.rdo.Resources
import ru.bmstu.rk9.rdo.rdo.ResourceDeclaration

import ru.bmstu.rk9.rdo.rdo.Sequence

import ru.bmstu.rk9.rdo.rdo.Function
import ru.bmstu.rk9.rdo.rdo.FunctionParameter

import ru.bmstu.rk9.rdo.rdo.Constants
import ru.bmstu.rk9.rdo.rdo.ConstantDeclaration

import ru.bmstu.rk9.rdo.rdo.EnumerativeSequence
import ru.bmstu.rk9.rdo.rdo.RegularSequence
import ru.bmstu.rk9.rdo.rdo.HistogramSequence

import ru.bmstu.rk9.rdo.rdo.PatternParameter
import ru.bmstu.rk9.rdo.rdo.Event
import ru.bmstu.rk9.rdo.rdo.Operation
import ru.bmstu.rk9.rdo.rdo.Rule
import ru.bmstu.rk9.rdo.rdo.EventConvert
import ru.bmstu.rk9.rdo.rdo.RuleConvert
import ru.bmstu.rk9.rdo.rdo.OperationConvert
import ru.bmstu.rk9.rdo.rdo.OperationRelevantResource
import ru.bmstu.rk9.rdo.rdo.RuleRelevantResource
import ru.bmstu.rk9.rdo.rdo.EventRelevantResource

import ru.bmstu.rk9.rdo.rdo.DecisionPointSome
import ru.bmstu.rk9.rdo.rdo.DecisionPointPrior
import ru.bmstu.rk9.rdo.rdo.DecisionPointSearch
import ru.bmstu.rk9.rdo.rdo.DecisionPointSearchActivity
import ru.bmstu.rk9.rdo.rdo.DecisionPointPriorActivity
import ru.bmstu.rk9.rdo.rdo.DecisionPointActivity

import ru.bmstu.rk9.rdo.rdo.Results
import ru.bmstu.rk9.rdo.rdo.ResultDeclaration
import ru.bmstu.rk9.rdo.rdo.ResultWatchParameter
import ru.bmstu.rk9.rdo.rdo.ResultWatchState
import ru.bmstu.rk9.rdo.rdo.ResultWatchQuant
import ru.bmstu.rk9.rdo.rdo.ResultWatchValue
import ru.bmstu.rk9.rdo.rdo.ResultGetValue

import ru.bmstu.rk9.rdo.rdo.SimulationRun


class RDOLabelProvider extends org.eclipse.xtext.ui.label.DefaultEObjectLabelProvider {

	@Inject
	new(org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider delegate) {
		super(delegate);
	}

	// Model
	def image(RDOModel m) { "model.gif" }

	// Resource types
	def  text(ResourceType rtp) { "RTP : " + rtp.name }
	def image(ResourceType rtp) { "puzzle_plus.gif" }

	def  text(ResourceTypeParameter p) { p.name + p.type.typeGenericLabel }
	def image(ResourceTypeParameter p) { "parameter.gif" }

	// Resources
	def  text(Resources rss) {"RSS : " +
		(if(!rss.resources.empty) {rss.resources.size.toString + " objects"} else "") }
	def image(Resources rss) { "puzzle.gif" }

		// Resource declaration
		def  text(ResourceDeclaration rss) { "rss : " + rss.name }
		def image(ResourceDeclaration rss) { "plus.gif" }

	// Constants
	def  text(Constants c) { "CON : " + c.eAllContents.toList.filter(typeof(ConstantDeclaration)).size.toString +
		" constant" + if(c.eAllContents.toList.filter(typeof(ConstantDeclaration)).size % 10 != 1) "s" else ""}
	def image(Constants c) { "floppy.gif" }

	def image(ConstantDeclaration c) { "constant2.gif" }
	def  text(ConstantDeclaration c) { c.name + c.type.typeGenericLabel }

	// Sequence
	def  text(Sequence seq) { "SEQ : " + seq.name + " : " + (
		if(seq.type instanceof EnumerativeSequence) "enumerative" else "" +
		if(seq.type instanceof RegularSequence) (seq.type as RegularSequence).type.literal else "" +
		if(seq.type instanceof HistogramSequence) "histogram" else "" ) + seq.returntype.typeGenericLabel }
	def image(Sequence seq) { "chart.gif" }

	// Function
	def  text(Function fun) { "FUN : " + fun.name + fun.returntype.typeGenericLabel }
	def image(Function fun) { "calc_arrow.gif" }

	def image(FunctionParameter p) { "parameter.gif" }
	def  text(FunctionParameter p) { p.name + p.type.typeGenericLabel }

	// Pattern
	def image(PatternParameter p) { "parameter.gif" }

	// Event
	def  text(Event evn) { "EVN : " + evn.name + " : event"}
	def image(Event evn) { "event.gif" }

	def  text(EventConvert c) { c.relres.name }
	def image(EventConvert c) { "parameter.gif" }

	def image(EventRelevantResource r) { "parameter.gif" }
	def  text(EventRelevantResource r) { r.name + r.type.relResName }

	// Operation
	def  text(Operation pat) { "PAT : " + pat.name + " : " + pat.type.literal}
	def image(Operation pat) { "script_block.gif" }

	def  text(OperationConvert c) { c.relres.name }
	def image(OperationConvert c) { "parameter.gif" }

	def image(OperationRelevantResource r) { "parameter.gif" }
	def  text(OperationRelevantResource r) { r.name + r.type.relResName }

	// Rule
	def  text(Rule pat) { "PAT : " + pat.name + " : rule"}
	def image(Rule pat) { "script_block.gif" }

	def  text(RuleConvert c) { c.relres.name }
	def image(RuleConvert c) { "parameter.gif" }

	def image(RuleRelevantResource r) { "parameter.gif" }
	def  text(RuleRelevantResource r) { r.name + r.type.relResName }

	// Common for patterns
	def getRelResName(EObject object) {
		switch object {
			ResourceType: " : RTP : " + object.name
			ResourceDeclaration: " : RSS : " + object.name
			default: ""
		}
	}

	// Decision points
	def image(DecisionPointSearchActivity d) { "script_block.gif" }
	def image(DecisionPointPriorActivity d) { "script_block.gif" }
	def image(DecisionPointActivity d) { "script_block.gif" }

	// DecisionPointSome
	def  text(DecisionPointSome dpt) { "DPT : " + dpt.name + " : some" }
	def image(DecisionPointSome dpt) { "block.gif" }

	// DecisionPointPrior
	def  text(DecisionPointPrior dpt) { "DPT : " + dpt.name + " : prior" }
	def image(DecisionPointPrior dpt) { "block_prior.gif" }

	// DecisionPointSearch
	def  text(DecisionPointSearch dpt) {"DPT : " + dpt.name + " : search" }
	def image(DecisionPointSearch dpt) { "search.gif" }

	// Results
	def  text(Results r) {
		"Results : " + (if(r.name != null) {r.name + " "} else "") + "(" +
			r.eAllContents.toList.filter(typeof(ResultDeclaration)).size.toString + ")" }
	def image(Results r) { "clipboard.gif" }

	def  text(ResultDeclaration d) { d.name + " : " + resultype(d)}
	def resultype(ResultDeclaration declaration) {
		switch declaration.type {
			ResultWatchParameter: "watch_par"
			ResultWatchState    : "watch_state"
			ResultWatchQuant    : "watch_quant"
			ResultWatchValue    : "watch_value"
			ResultGetValue      : "get_value"
		}
	}
	def image(ResultDeclaration d) { "parameter.gif" }

	// Simulation run
	def  text(SimulationRun smr) { "SMR : Simulation run" }
	def image(SimulationRun smr) { "run.gif" }
}
