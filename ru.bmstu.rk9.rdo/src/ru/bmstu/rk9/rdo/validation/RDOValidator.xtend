package ru.bmstu.rk9.rdo.validation

import java.util.List
import java.util.LinkedList
import java.util.ArrayList

import java.util.Map
import java.util.HashMap

import com.google.inject.Inject

import org.eclipse.xtext.validation.Check

import org.eclipse.xtext.resource.IResourceDescription
import org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider

import org.eclipse.xtext.resource.IContainer

import org.eclipse.emf.ecore.resource.Resource

import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EStructuralFeature

import ru.bmstu.rk9.rdo.generator.GlobalContext

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*
import static extension ru.bmstu.rk9.rdo.generator.RDOExpressionCompiler.*

import ru.bmstu.rk9.rdo.rdo.RdoPackage

import ru.bmstu.rk9.rdo.rdo.RDOModel

import ru.bmstu.rk9.rdo.rdo.ResourceType

import ru.bmstu.rk9.rdo.rdo.ResourceDeclaration

import ru.bmstu.rk9.rdo.rdo.Sequence

import ru.bmstu.rk9.rdo.rdo.Constant

import ru.bmstu.rk9.rdo.rdo.Function
import ru.bmstu.rk9.rdo.rdo.FunctionTable

import ru.bmstu.rk9.rdo.rdo.Pattern
import ru.bmstu.rk9.rdo.rdo.ParameterType
import ru.bmstu.rk9.rdo.rdo.PatternSelectMethod
import ru.bmstu.rk9.rdo.rdo.Operation
import ru.bmstu.rk9.rdo.rdo.OperationRelevantResource
import ru.bmstu.rk9.rdo.rdo.Rule
import ru.bmstu.rk9.rdo.rdo.RuleRelevantResource
import ru.bmstu.rk9.rdo.rdo.Event
import ru.bmstu.rk9.rdo.rdo.EventRelevantResource

import ru.bmstu.rk9.rdo.rdo.DecisionPoint

import ru.bmstu.rk9.rdo.rdo.Frame

import ru.bmstu.rk9.rdo.rdo.Result

import ru.bmstu.rk9.rdo.rdo.RDOInteger
import ru.bmstu.rk9.rdo.rdo.RDOEnum
import ru.bmstu.rk9.rdo.rdo.DefaultMethod

class RDOValidator extends AbstractRDOValidator
{

	@Inject
	private ResourceDescriptionsProvider resourceDescriptionsProvider

	@Inject
	private IContainer.Manager containerManager

	private static List<Resource> resourceIndex = new LinkedList<Resource>
	private static HashMap<String, GlobalContext> variableIndex = new HashMap<String, GlobalContext>

	@Check
	def exportResources(RDOModel model)
	{
		val index = resourceDescriptionsProvider.createResourceDescriptions
		val resDesc = index.getResourceDescription(model.eResource.URI)

		resourceIndex.clear

		if(resDesc == null)
			return
		else
		{
			for(IContainer c : containerManager.getVisibleContainers(resDesc, index))
				for(IResourceDescription rd : c.getResourceDescriptions())
				{
					resourceIndex.add(model.eResource.resourceSet.getResource(rd.URI, true))
					if(!variableIndex.containsKey(resourceIndex.last.resourceName))
						variableIndex.put(resourceIndex.last.resourceName, new GlobalContext)
				}

			if(variableIndex.size != resourceIndex.size)
			{
				variableIndex.clear
				for(r : resourceIndex)
					variableIndex.put(resourceIndex.last.resourceName, new GlobalContext)
			}
		}
	}

	@Check
	def exportResourceDeclarations(ResourceDeclaration rss)
	{
		val model = rss.modelRoot
		if(variableIndex == null || variableIndex.get(model.nameGeneric) == null)
			return

		val resindex = variableIndex.get(model.nameGeneric).resources
		val resmodel = model.eAllContents.filter(typeof(ResourceDeclaration)).toMap[name]
		for(r : resmodel.keySet)
			if(resindex.get(r) == null || r == rss.name)
				resindex.put(r, variableIndex.get(model.nameGeneric).newRSS(resmodel.get(r)))

		clearMissing(resindex, resmodel)
	}

	@Check
	def exportSequences(Sequence seq)
	{
		val model = seq.modelRoot
		if(variableIndex == null || variableIndex.get(model.nameGeneric) == null)
			return

		val seqindex = variableIndex.get(model.nameGeneric).sequences
		val seqmodel = model.eAllContents.filter(typeof(Sequence)).toMap[name]
		for(r : seqmodel.keySet)
			if(seqindex.get(r) == null || r == seq.name)
				seqindex.put(r, variableIndex.get(model.nameGeneric).newSEQ(seqmodel.get(r)))

		clearMissing(seqindex, seqmodel)
	}

	@Check
	def exportConstants(Constant con)
	{
		val model = con.modelRoot
		if(variableIndex == null || variableIndex.get(model.nameGeneric) == null)
			return

		val conindex = variableIndex.get(model.nameGeneric).constants
		val conmodel = model.eAllContents.filter(typeof(Constant)).toMap[name]
		for(r : conmodel.keySet)
			if(conindex.get(r) == null || r == con.name)
				conindex.put(r, variableIndex.get(model.nameGeneric).newCON(conmodel.get(r)))

		clearMissing(conindex, conmodel)
	}

	@Check
	def exportFunctions(Function fun)
	{
		val model = fun.modelRoot
		if(variableIndex == null || variableIndex.get(model.nameGeneric) == null)
			return

		val funindex = variableIndex.get(model.nameGeneric).functions
		val funmodel = model.eAllContents.filter(typeof(Function)).toMap[name]
		for(r : funmodel.keySet)
			if(funindex.get(r) == null || r == fun.name)
				funindex.put(r, variableIndex.get(model.nameGeneric).newFUN(funmodel.get(r)))

		clearMissing(funindex, funmodel)
	}

	def clearMissing(Map<String, ?> index, Map<String, ?> model)
	{
		if(index.size != model.size)
		{
			var iter = index.keySet.iterator
			while(iter.hasNext)
			{
				val next = iter.next
				if(model.get(next) == null)
				{
					index.remove(next)
					iter = index.keySet.iterator
				}
			}
		}
	}

	def checkDefaultMethodCountGeneric(EObject parent, Iterable<DefaultMethod> methods,
			Map<String, Integer> counts
	)
	{
		for(d : methods) {
			if (!counts.containsKey(d.method.name))
				error("Error - incorrect default method name", d,
					d.getNameStructuralFeature
				)
			else if (counts.get(d.method.name) > 0)
				error("Error - default method cannot be set more than once", d,
					d.getNameStructuralFeature
				)
			else
				counts.put(d.method.name, 1)
		}

		for(k : counts.keySet)
			if (counts.get(k) == 0)
				warning("Warning - default method " + k + " not set", parent,
					parent.getNameStructuralFeature
				)
	}

	@Check
	def checkDefaultMethodGlobalCount(RDOModel model)
	{
		var Map<String, Integer> counts = new HashMap<String, Integer>()
		for (v : RDOValidatorHelper.DefaultMethodsHelper.GlobalMethods.values)
			counts.put(v.name, 0)

		var methods = model.objects.filter(typeof(DefaultMethod))
		checkDefaultMethodCountGeneric(model, methods, counts)
	}

	@Check
	def checkDefaultMethodOperatioCount(Operation op)
	{
		var Map<String, Integer> counts = new HashMap<String, Integer>()
		for (v : RDOValidatorHelper.DefaultMethodsHelper.OperationMethods.values)
			counts.put(v.name, 0)

		checkDefaultMethodCountGeneric(op, op.defaultMethods, counts)
	}

	@Check
	def checkDefaultMethodRuleCount(Rule rule)
	{
		var Map<String, Integer> counts = new HashMap<String, Integer>()
		for (v : RDOValidatorHelper.DefaultMethodsHelper.EventOrRuleMethods.values)
			counts.put(v.name, 0)

		checkDefaultMethodCountGeneric(rule, rule.defaultMethods, counts)
	}

	@Check
	def checkDefaultMethodEventCount(Event evn)
	{
		var Map<String, Integer> counts = new HashMap<String, Integer>()
		for (v : RDOValidatorHelper.DefaultMethodsHelper.EventOrRuleMethods.values)
			counts.put(v.name, 0)

		checkDefaultMethodCountGeneric(evn, evn.defaultMethods, counts)
	}

	@Check
	def checkDuplicateNamesForEntities(RDOModel model)
	{
		val List<String> entities = new ArrayList<String>()
		val List<String> duplicates = new ArrayList<String>()

		val List<EObject> checklist = model.eAllContents.filter[e |
			e instanceof ResourceType        ||
			e instanceof ResourceDeclaration ||
			e instanceof Sequence            ||
			e instanceof Constant ||
			e instanceof Function            ||
			e instanceof Pattern             ||
			e instanceof DecisionPoint       ||
			e instanceof Frame               ||
			e instanceof Result
		].toList

		for(e : checklist)
		{
			val name = e.fullyQualifiedName
			if(entities.contains(name))
			{
				if(!duplicates.contains(name))
					duplicates.add(name)
			}
			else
				entities.add(name)
		}

		if(!duplicates.empty)
			for(e : checklist)
			{
				val name = e.fullyQualifiedName
				if(duplicates.contains(name))
					error("Error - multiple declarations of object '" + name + "'.", e,
						e.getNameStructuralFeature)
			}
	}

	@Check
	def checkNamesInPatterns (Pattern pat)
	{
		val List<EObject> paramlist  = pat.eAllContents.filter[e |
			e instanceof ParameterType
		].toList
		val List<EObject> relreslist = pat.eAllContents.filter[e |
			e instanceof OperationRelevantResource ||
			e instanceof RuleRelevantResource      ||
			e instanceof EventRelevantResource
		].toList

		for(e : paramlist)
			if(relreslist.map[r | r.nameGeneric].contains(e.nameGeneric))
				error("Error - parameter name shouldn't match relevant resource name '" + e.nameGeneric + "'.", e,
					e.getNameStructuralFeature)

		for(e : relreslist)
			if(paramlist.map[r | r.nameGeneric].contains(e.nameGeneric))
				error("Error - relevant resource name shouldn't match parameter name '" + e.nameGeneric + "'.", e,
					e.getNameStructuralFeature)
	}

	def EStructuralFeature getNameStructuralFeature (EObject object)
	{
		switch object
		{
			ResourceType:
				RdoPackage.eINSTANCE.META_RelResType_Name

			ResourceDeclaration:
				RdoPackage.eINSTANCE.META_RelResType_Name

			Sequence:
				RdoPackage.eINSTANCE.sequence_Name

			Function:
				RdoPackage.eINSTANCE.function_Name

			Operation:
				RdoPackage.eINSTANCE.operation_Name

			Rule:
				RdoPackage.eINSTANCE.rule_Name

			Event:
				RdoPackage.eINSTANCE.event_Name

			DecisionPoint:
				RdoPackage.eINSTANCE.decisionPoint_Name

			Frame:
				RdoPackage.eINSTANCE.frame_Name

			Result:
				RdoPackage.eINSTANCE.result_Name

//			ParameterType:
//				RdoPackage.eINSTANCE.ParameterType_Type

			OperationRelevantResource:
				RdoPackage.eINSTANCE.operationRelevantResource_Name

			RuleRelevantResource:
				RdoPackage.eINSTANCE.ruleRelevantResource_Name

			EventRelevantResource:
				RdoPackage.eINSTANCE.eventRelevantResource_Name
		}
	}

	@Check
	def checkConvertStatus(OperationRelevantResource relres)
	{
		val type  = relres.type
		val begin = relres.begin.literal
		val end   = relres.end.literal

		switch type
		{
			ResourceType:
			{
				if(begin == "NonExist" && end != "Create")
					error("Invalid convert status: "+end+" - for NonExist convert begin status a resource should be created",
						RdoPackage.eINSTANCE.operationRelevantResource_End)

				if(end == "Create" && begin != "NonExist")
					error("Invalid convert status: "+begin+" - there is no resource initially for Create convert end status",
						RdoPackage.eINSTANCE.operationRelevantResource_Begin)

				if(begin == "Erase" && end != "NonExist")
					error("Invalid convert status: "+end+" - convert end status for erased resource should be NonExist",
						RdoPackage.eINSTANCE.operationRelevantResource_End)

				if(end == "NonExist" && begin != "Erase")
					error("Invalid convert status: "+begin+" - relevant resource isn't being erased",
						RdoPackage.eINSTANCE.operationRelevantResource_Begin)
			}

			ResourceDeclaration:
			{
				if(begin == "Create" || begin == "Erase" || begin == "NonExist")
					error("Invalid convert status: "+begin+" - only Keep and NoChange statuses could be used for resource",
						RdoPackage.eINSTANCE.operationRelevantResource_Begin)

				if(end == "Create" || end == "Erase" || end == "NonExist")
					error("Invalid convert status: "+end+" - only Keep and NoChange statuses could be used for resource",
						RdoPackage.eINSTANCE.operationRelevantResource_End)
			}
		}
	}

	@Check
	def checkConvertStatus(RuleRelevantResource relres)
	{
		val type = relres.type
		val rule = relres.rule.literal

		if(rule == "NonExist") {
			error("Invalid convert status: "+rule+" couldn't be used in condition-action rule",
				RdoPackage.eINSTANCE.ruleRelevantResource_Rule)
			return
		}

		switch type
		{
			ResourceDeclaration:
			{
				if(rule == "Create" || rule == "Erase")
					error("Invalid convert status: "+rule+" - only Keep and NoChange statuses could be used for resource",
						RdoPackage.eINSTANCE.ruleRelevantResource_Rule)
			}
		}
	}

	@Check
	def checkConvertStatus(EventRelevantResource relres)
	{
		val type = relres.type
		val rule = relres.rule.literal

		if(rule == "NonExist" || rule == "Erase" || rule == "NoChange")
		{
			error("Invalid convert status: "+rule+" couldn't be used in event",
				RdoPackage.eINSTANCE.eventRelevantResource_Rule)
			return
		}

		switch type
		{
			ResourceType:
			{
				if(rule == 'Keep')
					error("Invalid convert status: "+rule+" couldn't be used for resource type in event",
						RdoPackage.eINSTANCE.eventRelevantResource_Rule)
			}
			ResourceDeclaration:
				if(rule == 'Create')
					error("Invalid convert status: "+rule+" couldn't be used for resource in event",
						RdoPackage.eINSTANCE.eventRelevantResource_Rule)
		}
	}

	@Check
	def checkForCombinationalChioceMethod(Pattern pat)
	{
		var havechoicemethods = false
		var iscombinatorial = false

		for(e : pat.eAllContents.toList.filter(typeof(PatternSelectMethod)))
		{
			switch e.eContainer
			{
				Operation: iscombinatorial = true
				Rule     : iscombinatorial = true

				OperationRelevantResource: havechoicemethods = true
				RuleRelevantResource     : havechoicemethods = true
			}
		}

		if(havechoicemethods && iscombinatorial)
			for(e : pat.eAllContents.toList.filter(typeof(PatternSelectMethod)))
			{
				switch e.eContainer
				{
					OperationRelevantResource:
						error("Operation " + (pat as Operation).name + " already uses combinational approach for relevant resources search",
							e.eContainer, RdoPackage.eINSTANCE.operationRelevantResource_Selectmethod)

					RuleRelevantResource:
						error("Rule " + (pat as Rule).name + " already uses combinational approach for relevant resources search",
							e.eContainer, RdoPackage.eINSTANCE.ruleRelevantResource_Selectmethod)
				}
			}
	}

	@Check
	def checkTableParameters(FunctionTable fun)
	{
		if(fun.parameters == null)
			return;

		for(p : fun.parameters)
		{
			val actual = p.type.resolveAllTypes
			if(!(actual instanceof RDOEnum || (actual instanceof RDOInteger && (actual as RDOInteger).range != null)))
				error("Invalid parameter type. Table function allows enumerative and ranged integer parameters only",
					p, RdoPackage.eINSTANCE.functionParameter_Type)
		}
	}

}
