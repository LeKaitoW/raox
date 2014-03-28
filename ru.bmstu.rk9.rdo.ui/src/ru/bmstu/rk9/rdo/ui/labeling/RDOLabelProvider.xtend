package ru.bmstu.rk9.rdo.ui.labeling

import com.google.inject.Inject

import org.eclipse.emf.ecore.EObject

import ru.bmstu.rk9.rdo.rdo.RDOModel

import ru.bmstu.rk9.rdo.generator.RDONaming

import ru.bmstu.rk9.rdo.rdo.ResourceType
import ru.bmstu.rk9.rdo.rdo.ResourceTypeParameter

import ru.bmstu.rk9.rdo.rdo.Resources
import ru.bmstu.rk9.rdo.rdo.ResourceTrace
import ru.bmstu.rk9.rdo.rdo.ResourceDeclaration

import ru.bmstu.rk9.rdo.rdo.Sequence

import ru.bmstu.rk9.rdo.rdo.Function
import ru.bmstu.rk9.rdo.rdo.FunctionParameter

import ru.bmstu.rk9.rdo.rdo.Constants
import ru.bmstu.rk9.rdo.rdo.ConstantDeclaration

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
import ru.bmstu.rk9.rdo.rdo.DecisionPointSearchActivities
import ru.bmstu.rk9.rdo.rdo.DecisionPointActivities

import ru.bmstu.rk9.rdo.rdo.Process
import ru.bmstu.rk9.rdo.rdo.ProcessCommand
import ru.bmstu.rk9.rdo.rdo.ProcessGenerate
import ru.bmstu.rk9.rdo.rdo.ProcessSeize
import ru.bmstu.rk9.rdo.rdo.SeizeID
import ru.bmstu.rk9.rdo.rdo.ProcessRelease
import ru.bmstu.rk9.rdo.rdo.ReleaseID
import ru.bmstu.rk9.rdo.rdo.ProcessAdvance
import ru.bmstu.rk9.rdo.rdo.ProcessQueue
import ru.bmstu.rk9.rdo.rdo.ProcessDepart
import ru.bmstu.rk9.rdo.rdo.ProcessAssign
import ru.bmstu.rk9.rdo.rdo.ProcessTerminate

import ru.bmstu.rk9.rdo.rdo.Results
import ru.bmstu.rk9.rdo.rdo.ResultDeclaration
import ru.bmstu.rk9.rdo.rdo.ResultWatchParameter
import ru.bmstu.rk9.rdo.rdo.ResultWatchState
import ru.bmstu.rk9.rdo.rdo.ResultWatchQuant
import ru.bmstu.rk9.rdo.rdo.ResultWatchValue
import ru.bmstu.rk9.rdo.rdo.ResultGetValue
import ru.bmstu.rk9.rdo.generator.RDOExpressionCompiler

class RDOLabelProvider extends org.eclipse.xtext.ui.label.DefaultEObjectLabelProvider {

	@Inject
	new(org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider delegate) {
		super(delegate);
	}

	//====== Extension methods ================================================================//
	def getModelRoot           (EObject o)            { RDONaming.getModelRoot           (o)    }
	def getNameGeneric         (EObject o)            { RDONaming.getNameGeneric         (o)    }
	def getFullyQualifiedName  (EObject o)            { RDONaming.getFullyQualifiedName  (o)    }
	def getContextQualifiedName(EObject o, EObject c) { RDONaming.getContextQualifiedName(o, c) }
	def compileType            (EObject o)            { RDOExpressionCompiler.compileType(o)    }
	def getTypeGenericLabel    (EObject o)            { RDONaming.getTypeGenericLabel    (o)    }
	//=========================================================================================//


	// Model
	def image(RDOModel m) { "model.gif" }

	// Resource types
	def  text(ResourceType rtp) { "RTP : " + rtp.name }
	def image(ResourceType rtp) { if (rtp.type.literal == "permanent") "puzzle_plus.gif" else "puzzle_plus_gray.gif" }

	def  text(ResourceTypeParameter p) { p.name + p.type.typeGenericLabel }
	def image(ResourceTypeParameter p) { "parameter.gif" }

	// Resources
	def  text(Resources rss) {"RSS : " +
		(if (rss.resources.size > 0) {rss.resources.size.toString + " objects"} else "") +
		(if (rss.resources.size * rss.trace.size > 0) ", " else "") +
		(if (rss.trace.size > 0) {rss.trace.size.toString + " traced"} else "") +
		(if (rss.resources.size + rss.trace.size == 0) "empty" else "") }
	def image(Resources rss) { "puzzle.gif" }

		// Resource declaration
		def  text(ResourceDeclaration rss) { "rss : " + rss.name }
		def image(ResourceDeclaration rss) { "plus.gif" }

		// Trace
		def  text(ResourceTrace rsstrc) { "trc : " + rsstrc.trace.name.toString }
		def image(ResourceTrace rsstrc) { "tag.gif" }

	// Constants
	def  text(Constants c) { "CON : " + c.eAllContents.toList.filter(typeof(ConstantDeclaration)).size.toString +
		" constant" + if (c.eAllContents.toList.filter(typeof(ConstantDeclaration)).size % 10 != 1) "s" else ""}
	def image(Constants c) { "floppy.gif" }

	def image(ConstantDeclaration c) { "constant2.gif" }
	def  text(ConstantDeclaration c) { c.name + c.type.typeGenericLabel }

	// Sequence
	def  text(Sequence seq) { "SEQ : " + seq.name + " : " + (
		if (seq.type.enumerative != null) "enumerative" else "" +
		if (seq.type.regular != null) seq.type.regular.type.literal else "" +
		if (seq.type.histogram != null) "histogram" else "" ) + seq.returntype.typeGenericLabel }
	def image(Sequence seq) { "chart.gif" }

	// Function
	def  text(Function fun) { "FUN : " + fun.name + fun.returntype.typeGenericLabel }
	def image(Function fun) { "calc_arrow.gif" }

	def image(FunctionParameter p) { "parameter.gif" }
	def  text(FunctionParameter p) { p.name + p.type.typeGenericLabel }

	// Event
	def  text(Event evn) { "EVN : " + evn.name + " : event" + if (evn.trace) " , traced" else "" }
	def image(Event evn) { "event.gif" }

	def  text(EventConvert c) { c.relres.name }
	def image(EventConvert c) { "parameter.gif" }

	def image(EventRelevantResource r) { "parameter.gif" }
	def  text(EventRelevantResource r) { r.name + r.type.relResName }

	// Operation
	def  text(Operation pat) { "PAT : " + pat.name + " : " + pat.type.literal + if (pat.trace) " , traced" else "" }
	def image(Operation pat) { "script_block.gif" }

	def  text(OperationConvert c) { c.relres.name }
	def image(OperationConvert c) { "parameter.gif" }

	def image(OperationRelevantResource r) { "parameter.gif" }
	def  text(OperationRelevantResource r) { r.name + r.type.relResName }

	// Rule
	def  text(Rule pat) { "PAT : " + pat.name + " : rule" + if (pat.trace) " , traced" else "" }
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
	def image(DecisionPointSearchActivities d) { "script_block.gif" }
	def image(DecisionPointActivities d) { "script_block.gif" }

	// DecisionPointSome
	def  text(DecisionPointSome dpt) { "DPT : " + dpt.name + " : some" }
	def image(DecisionPointSome dpt) { "block.gif" }

	// DecisionPointPrior
	def  text(DecisionPointPrior dpt) { "DPT : " + dpt.name + " : prior" }
	def image(DecisionPointPrior dpt) { "block_prior.gif" }

	// DecisionPointSearch
	def  text(DecisionPointSearch dpt) {"DPT : " + dpt.name + " : search" }
	def image(DecisionPointSearch dpt) { "search.gif" }

	// Processes
	def  text(Process prc) { "PRC : " + prc.name }
	def image(Process prc) { "processor.gif" }

	def  text(ProcessGenerate pg) { "GENERATE" }

	def  text(ProcessSeize ps) { "SEIZE" }
	def image(SeizeID s) { "parameter.gif" }

	def  text(ProcessRelease pr) { "RELEASE" }
	def  text(ReleaseID r) { r.id.name }
	def image(ReleaseID r) { "parameter.gif" }

	def  text(ProcessAdvance pa) { "ADVANCE" }

	def  text(ProcessQueue pq) { "QUEUE" }

	def  text(ProcessDepart pd) { "DEPART" }

	def  text(ProcessAssign pa) { "ASSIGN" }

	def  text(ProcessTerminate pt) { "TERMINATE" }

	def image(ProcessCommand pc) { "process.gif" }

	// Results
	def  text(Results r) {
		"Results : " + (if (r.name != null) {r.name + " : "} else "") +
			r.eAllContents.toList.filter(typeof(ResultDeclaration)).size.toString + " positions" }
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

}
