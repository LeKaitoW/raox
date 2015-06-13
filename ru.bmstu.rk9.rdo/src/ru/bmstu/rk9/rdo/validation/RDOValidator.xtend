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

import ru.bmstu.rk9.rdo.RDOQualifiedNameProvider

import ru.bmstu.rk9.rdo.rdo.RdoPackage

import ru.bmstu.rk9.rdo.rdo.RDOModel

import ru.bmstu.rk9.rdo.rdo.ResourceType
import ru.bmstu.rk9.rdo.rdo.ResourceTypeParameter
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterSuchAs

import ru.bmstu.rk9.rdo.rdo.ResourceDeclaration

import ru.bmstu.rk9.rdo.rdo.Sequence

import ru.bmstu.rk9.rdo.rdo.ConstantDeclaration

import ru.bmstu.rk9.rdo.rdo.Function
import ru.bmstu.rk9.rdo.rdo.FunctionTable

import ru.bmstu.rk9.rdo.rdo.Pattern
import ru.bmstu.rk9.rdo.rdo.PatternParameter
import ru.bmstu.rk9.rdo.rdo.PatternChoiceMethod
import ru.bmstu.rk9.rdo.rdo.Operation
import ru.bmstu.rk9.rdo.rdo.OperationRelevantResource
import ru.bmstu.rk9.rdo.rdo.OperationConvert
import ru.bmstu.rk9.rdo.rdo.Rule
import ru.bmstu.rk9.rdo.rdo.RuleRelevantResource
import ru.bmstu.rk9.rdo.rdo.RuleConvert
import ru.bmstu.rk9.rdo.rdo.Event
import ru.bmstu.rk9.rdo.rdo.EventRelevantResource
import ru.bmstu.rk9.rdo.rdo.EventConvert

import ru.bmstu.rk9.rdo.rdo.DecisionPoint

import ru.bmstu.rk9.rdo.rdo.Frame

import ru.bmstu.rk9.rdo.rdo.ResultDeclaration

import ru.bmstu.rk9.rdo.rdo.SimulationRun

import ru.bmstu.rk9.rdo.rdo.RDOSuchAs
import ru.bmstu.rk9.rdo.rdo.RDOEnum
import ru.bmstu.rk9.rdo.rdo.RDOInteger
import ru.bmstu.rk9.rdo.rdo.DecisionPointSearch

class SuchAsHistory
{
	private List<EObject> history
	private String text

	new(EObject first)
	{
		history = new ArrayList<EObject>
		history.add(first)

		switch first
		{
			ResourceTypeParameter:
				text = first.eContainer.getNameGeneric + "." + first.getNameGeneric

			default:
				text = first.getNameGeneric
		}
	}

	public def boolean add(EObject object)
	{
		var ret = !history.contains(object)
		history.add(object)

		var extmodel = ""
		var extname = RDOQualifiedNameProvider.filenameFromURI((object.eContainer.eContainer as RDOModel))
		if(RDOQualifiedNameProvider.filenameFromURI((history.head.eContainer.eContainer as RDOModel)) != extname)
			extmodel = extmodel + extname + "."

		switch object
		{
			ResourceTypeParameter:
				text = text + " \u2192 " + extmodel + object.eContainer.getNameGeneric + "." + object.getNameGeneric

			default:
				text = text + " \u2192 " + extmodel + object.getNameGeneric
		}
		return ret
	}

	public def String getText()
	{
		return text
	}
}

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
	def exportConstants(ConstantDeclaration con)
	{
		val model = con.modelRoot
		if(variableIndex == null || variableIndex.get(model.nameGeneric) == null)
			return

		val conindex = variableIndex.get(model.nameGeneric).constants
		val conmodel = model.eAllContents.filter(typeof(ConstantDeclaration)).toMap[name]
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

	@Check
	def checkSMRCount(SimulationRun smr)
	{
		var ArrayList<SimulationRun> smrs = new ArrayList<SimulationRun>();
		for(r : resourceIndex)
			smrs.addAll(r.allContents.toIterable.filter(typeof(SimulationRun)))


		if(smrs.size > 1)
			for(e : smrs)
				if(e.eResource == smr.eResource)
					error("Error - project can have no more than one Simulation run block", e,
						e.getNameStructuralFeature)
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
			e instanceof ConstantDeclaration ||
			e instanceof Function            ||
			e instanceof Pattern             ||
			e instanceof DecisionPoint       ||
			e instanceof Frame               ||
			e instanceof ResultDeclaration
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
			e instanceof PatternParameter
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

			ConstantDeclaration:
				RdoPackage.eINSTANCE.META_SuchAs_Name

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

			ResultDeclaration:
				RdoPackage.eINSTANCE.resultDeclaration_Name

			PatternParameter:
				RdoPackage.eINSTANCE.patternParameter_Name

			OperationRelevantResource:
				RdoPackage.eINSTANCE.operationRelevantResource_Name

			RuleRelevantResource:
				RdoPackage.eINSTANCE.ruleRelevantResource_Name

			EventRelevantResource:
				RdoPackage.eINSTANCE.eventRelevantResource_Name
		}
	}

	def boolean resolveCyclicSuchAs(EObject object, SuchAsHistory history)
	{
		if(object.eContainer == null)	// check unresolved reference in order
		    return false              	// to avoid exception in second switch

		switch object
		{
			ResourceTypeParameter:
			{
				switch object.type
				{
					RDORTPParameterSuchAs:
					{
						if(history.add(object))
							return resolveCyclicSuchAs((object.type as RDORTPParameterSuchAs).type.type, history)
						else
							return true
					}
					default: return false
				}
			}
			ConstantDeclaration:
			{
				switch object.type
				{
					RDOSuchAs:
					{
						if(history.add(object))
							return resolveCyclicSuchAs((object.type as RDOSuchAs).type, history)
						else
							return true
					}
					default: return false
				}
			}
			default:
			{
				error("This is a bug. Please, let us know and send us the text of your model.",
					RdoPackage.eINSTANCE.META_SuchAs_Name)
				return false
			}
		}
	}

	@Check
	def checkCyclicSuchAs(RDOSuchAs ref)
	{
		var EObject first

		switch ref.eContainer
		{
			RDORTPParameterSuchAs:
				first = ref.eContainer.eContainer

			ConstantDeclaration:
				first = ref.eContainer

			default: return
		}
		var SuchAsHistory history = new SuchAsHistory(first)
		if(resolveCyclicSuchAs(ref.type, history))
			error("Cyclic such_as found in '" + first.nameGeneric + "': " + history.text +". Resulting type is unknown.",
				first, RdoPackage.eINSTANCE.META_SuchAs_Name)
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
				if(type.type.literal == "permanent")
				{
					if(begin == "Create" || begin == "Erase" || begin == "NonExist")
						error("Invalid convert status: "+begin+" - resource type " + type.getNameGeneric + " is permanent",
							RdoPackage.eINSTANCE.operationRelevantResource_Begin)

					if(end == "Create" || end == "Erase" || end == "NonExist")
						error("Invalid convert status: "+end+" - resource type " + type.getNameGeneric + " is permanent",
							RdoPackage.eINSTANCE.operationRelevantResource_End)
				}
				else
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
			ResourceType:
			{
				if(type.type.literal == "permanent")
					if(rule == "Create" || rule == "Erase")
							error("Invalid convert status: "+rule+" - resource type " + type.getNameGeneric + " is permanent",
								RdoPackage.eINSTANCE.ruleRelevantResource_Rule)
			}
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
				else
					if(type.type.literal == "permanent")
						error("Invalid resource type: '"+type.name+"' is not temporary",
							RdoPackage.eINSTANCE.eventRelevantResource_Rule)
			}
			ResourceDeclaration:
				if(rule == 'Create')
					error("Invalid convert status: "+rule+" couldn't be used for resource in event",
						RdoPackage.eINSTANCE.eventRelevantResource_Rule)
		}
	}

	@Check
	def checkPatternRelResConverts(Pattern o)
	{
		var count = 0; var i = 0; var j = 0
		var found = false

		switch o
		{
			Operation:
			{
				val relreslist  = o.eAllContents.toList.filter(typeof(OperationRelevantResource))
				val convertlist = o.eAllContents.toList.filter(typeof(OperationConvert))

				for(r : relreslist)
				{
					i = i + 1
					count = 0
					found = false
					for(c : convertlist)
						if(c.relres.name == r.name)
						{
							count = count + 1
							found = true
						}

					if(count > 1)
						for(c : convertlist)
							if(c.relres == r)
								error("Multiple converts for relevant resource " + r.name,
									c, RdoPackage.eINSTANCE.operationConvert_Relres)
					if(!found)
						error("No convert found for relevant resource " + r.name,
							r, RdoPackage.eINSTANCE.operationRelevantResource_Name)

					j = 0
					if(found && (count == 1))
						for(c : convertlist)
						{
							j = j + 1
							if((i == j) && (c.relres != r))
								if(c.relres.name != null)
									error("Wrong relevant resource converts order: found " +
										c.relres.name +	" instead of " + r.name, c,
											RdoPackage.eINSTANCE.operationConvert_Relres)
								else
									i = i + 1
						}
				}

			}
			Rule:
			{
				val relreslist  = o.eAllContents.toList.filter(typeof(RuleRelevantResource))
				val convertlist = o.eAllContents.toList.filter(typeof(RuleConvert))

				for(r : relreslist)
				{
					i = i + 1
					count = 0
					found = false
					for(c : convertlist)
						if(c.relres.name == r.name)
						{
							count = count + 1
							found = true
						}

					if(count > 1)
						for(c : convertlist)
							if(c.relres == r)
								error("Multiple converts for relevant resource " + r.name,
									c, RdoPackage.eINSTANCE.ruleConvert_Relres)
					if(!found)
						error("No convert found for relevant resource " + r.name,
							r, RdoPackage.eINSTANCE.ruleRelevantResource_Name)

					j = 0
					if(found && (count == 1))
						for(c : convertlist)
						{
							j = j + 1
							if((i == j) && (c.relres != r))
								if(c.relres.name != null)
									error("Wrong relevant resource converts order: found " +
										c.relres.name +	" instead of " + r.name, c,
											RdoPackage.eINSTANCE.ruleConvert_Relres)
								else
									i = i + 1
						}
				}

			}
			Event:
			{
				var relreslist  = o.eAllContents.toList.filter(typeof(EventRelevantResource))
				val convertlist = o.eAllContents.toList.filter(typeof(EventConvert))

				for(r : relreslist)
				{
					i = i + 1
					count = 0
					found = false
					for(c : convertlist)
						if(c.relres.name == r.name)
						{
							count = count + 1
							found = true
						}

					if(count > 1)
						for(c : convertlist)
							if(c.relres == r)
								error("Multiple converts for relevant resource " + r.name,
									c, RdoPackage.eINSTANCE.eventConvert_Relres)
					if(!found)
						error("No convert found for relevant resource " + r.name,
							r, RdoPackage.eINSTANCE.eventRelevantResource_Name)

					j = 0
					if(found && (count == 1))
						for(c : convertlist)
						{
							j = j + 1
							if((i == j) && (c.relres != r))
								if(c.relres.name != null)
									error("Wrong relevant resource converts order: found " +
										c.relres.name +	" instead of " + r.name, c,
											RdoPackage.eINSTANCE.eventConvert_Relres)
								else
									i = i + 1
						}
				}
			}
		}
	}

	@Check
	def checkConvertAssociations(OperationConvert c)
	{
		val begin = c.relres.begin.literal
		val end   = c.relres.end.literal

		if(begin == "Keep" || begin == "Create")
			if(!c.havebegin)
				error("Resource " + c.relres.name + " with convert status "+begin+" "+end+" is missing a convert begin",
					RdoPackage.eINSTANCE.operationConvert_Relres)

		if(end == "Keep" || end == "Create")
			if(!c.haveend)
				error("Resource " + c.relres.name + " with convert status "+begin+" "+end+" is missing a convert end",
					RdoPackage.eINSTANCE.operationConvert_Relres)

		if(begin == "NonExist" || begin == "NoChange")
			if(c.havebegin)
				error("Resource " + c.relres.name + " with convert status "+begin+" "+end+" shouldn't have a convert begin",
					RdoPackage.eINSTANCE.operationConvert_Havebegin)

		if(end == "NonExist" || end == "NoChange")
			if(c.haveend)
				error("Resource " + c.relres.name + " with convert status "+begin+" "+end+" shouldn't have a convert end",
					RdoPackage.eINSTANCE.operationConvert_Haveend)
	}

	@Check
	def checkConvertAssociations(RuleConvert c)
	{
		val rule = c.relres.rule.literal

		if(rule == "Keep" || rule == "Create")
			if(!c.haverule)
				error("Resource " + c.relres.name + " with convert status "+rule+" is missing a convert rule",
					RdoPackage.eINSTANCE.ruleConvert_Relres)

		if(rule == "NoChange")
			if(c.haverule)
				error("Resource " + c.relres.name + " with convert status "+rule+" shouldn't have a convert rule",
					RdoPackage.eINSTANCE.ruleConvert_Haverule)

		if(rule == "Create" && c.havechoice)
			error("Relevant resource " + c.relres.name + " with convert status "+rule+" shouldn't have a choice from",
				RdoPackage.eINSTANCE.ruleConvert_Havechoice)
	}

	@Check
	def checkForCombinationalChioceMethod(Pattern pat)
	{
		var havechoicemethods = false
		var iscombinatorial = false

		for(e : pat.eAllContents.toList.filter(typeof(PatternChoiceMethod)))
		{
			switch e.eContainer
			{
				Operation: iscombinatorial = true
				Rule     : iscombinatorial = true

				OperationConvert: havechoicemethods = true
				RuleConvert     : havechoicemethods = true
			}
		}

		if(havechoicemethods && iscombinatorial)
			for(e : pat.eAllContents.toList.filter(typeof(PatternChoiceMethod)))
			{
				switch e.eContainer
				{
					OperationConvert:
						error("Operation " + (pat as Operation).name + " already uses combinational approach for relevant resources search",
							e.eContainer, RdoPackage.eINSTANCE.operationConvert_Choicemethod)

					RuleConvert:
						error("Rule " + (pat as Rule).name + " already uses combinational approach for relevant resources search",
							e.eContainer, RdoPackage.eINSTANCE.ruleConvert_Choicemethod)
				}
			}
	}

	@Check
	def checkTableParameters(FunctionTable fun)
	{
		if(fun.parameters == null)
			return;

		for(p : fun.parameters.parameters)
		{
			val actual = p.type.resolveAllSuchAs
			if(!(actual instanceof RDOEnum || (actual instanceof RDOInteger && (actual as RDOInteger).range != null)))
				error("Invalid parameter type. Table function allows enumerative and ranged integer parameters only",
					p, RdoPackage.eINSTANCE.functionParameter_Type)
		}
	}

	@Check
	def checkConvertStatusSearch(DecisionPointSearch search)
	{
		val activities = search.activities

		for (activity : activities) {
			val rule = activity.pattern

			if (rule instanceof Rule) {
				for (relevantResource : rule.relevantresources) {
					val status = relevantResource.rule.literal

					if (!(status == "Keep" || status == "NoChange"))
						error(
							"Invalid convert status: " + status +
								" - only Keep and NoChange statuses could be used for resource",
							activity, RdoPackage.eINSTANCE.decisionPointSearchActivity_Pattern)
				}
			} else {
				error("Invalid pattern name: " + rule.name + " - only Rule pattern type allowed",
					RdoPackage.eINSTANCE.decisionPoint_Name)
			}
		}
	}
	
}
