package ru.bmstu.rk9.rdo.validation

import java.util.List
import java.util.ArrayList

import org.eclipse.xtext.validation.Check

import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EStructuralFeature

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*

import ru.bmstu.rk9.rdo.customizations.RDOQualifiedNameProvider

import ru.bmstu.rk9.rdo.rdo.RdoPackage

import ru.bmstu.rk9.rdo.rdo.RDOModel

import ru.bmstu.rk9.rdo.rdo.ResourceType
import ru.bmstu.rk9.rdo.rdo.ResourceTypeParameter
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterSuchAs

import ru.bmstu.rk9.rdo.rdo.ResourceDeclaration
import ru.bmstu.rk9.rdo.rdo.ResourceTrace

import ru.bmstu.rk9.rdo.rdo.Sequence

import ru.bmstu.rk9.rdo.rdo.ConstantDeclaration

import ru.bmstu.rk9.rdo.rdo.Function

import ru.bmstu.rk9.rdo.rdo.Pattern
import ru.bmstu.rk9.rdo.rdo.PatternParameter
import ru.bmstu.rk9.rdo.rdo.Operation
import ru.bmstu.rk9.rdo.rdo.OperationRelevantResource
import ru.bmstu.rk9.rdo.rdo.OperationConvert
import ru.bmstu.rk9.rdo.rdo.Rule
import ru.bmstu.rk9.rdo.rdo.RuleRelevantResource
import ru.bmstu.rk9.rdo.rdo.RuleConvert
import ru.bmstu.rk9.rdo.rdo.Event
import ru.bmstu.rk9.rdo.rdo.EventRelevantResource
import ru.bmstu.rk9.rdo.rdo.EventConvert
import ru.bmstu.rk9.rdo.rdo.PatternChoiceMethod

import ru.bmstu.rk9.rdo.rdo.DecisionPoint

import ru.bmstu.rk9.rdo.rdo.ResultDeclaration

import ru.bmstu.rk9.rdo.rdo.RDOSuchAs


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
		if (RDOQualifiedNameProvider.filenameFromURI((history.head.eContainer.eContainer as RDOModel)) != extname)
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
			e instanceof ResultDeclaration
		].toList

		for (e : checklist)
		{
			val name = e.fullyQualifiedName
			if (entities.contains(name))
			{
				if (!duplicates.contains(name))
					duplicates.add(name)
			}
			else
				entities.add(name)
		}

		if (duplicates.size > 0)
			for (e : checklist)
			{
				val name = e.fullyQualifiedName
				if (duplicates.contains(name))
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

		for (e : paramlist)
			if (relreslist.map[r | r.nameGeneric].contains(e.nameGeneric))
				error("Error - parameter name shouldn't match relevant resource name '" + e.nameGeneric + "'.", e,
					e.getNameStructuralFeature)

		for (e : relreslist)
			if (paramlist.map[r | r.nameGeneric].contains(e.nameGeneric))
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
		if (object.eContainer == null)	// check unresolved reference in order
		    return false              	// to avoid exception in second switch

		switch object
		{
			ResourceTypeParameter:
			{
				switch object.type
				{
					RDORTPParameterSuchAs:
					{
						if (history.add(object))
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
						if (history.add(object))
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
		if (resolveCyclicSuchAs(ref.type, history))
			error("Cyclic such_as found in '" + first.nameGeneric + "': " + history.text +". Resulting type is unknown.",
				first, RdoPackage.eINSTANCE.META_SuchAs_Name)
	}

	@Check
	def checkMultipleTraces(ResourceTrace trc)
	{
		var found = 0
		for (e : trc.eContainer.eContainer.eAllContents.filter(typeof(ResourceTrace)).toIterable)
			if (trc.trace == e.trace)
				found = found + 1
		if (found > 1)
			for (e : trc.eContainer.eContainer.eAllContents.filter(typeof(ResourceTrace)).toIterable)
				if (trc.trace == e.trace)
					error("Multiple trace statements for '" + trc.trace.name + "'.",
						e, RdoPackage.eINSTANCE.resourceTrace_Trace)
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
				if (type.type.literal == "permanent")
				{
					if (begin == "Create" || begin == "Erase" || begin == "NonExist")
						error("Invalid convert status: "+begin+" - resource type " + type.getNameGeneric + " is permanent",
							RdoPackage.eINSTANCE.operationRelevantResource_Begin)

					if (end == "Create" || end == "Erase" || end == "NonExist")
						error("Invalid convert status: "+end+" - resource type " + type.getNameGeneric + " is permanent",
							RdoPackage.eINSTANCE.operationRelevantResource_End)
				}
				else
				{
					if (begin == "NonExist" && end != "Create")
						error("Invalid convert status: "+end+" - for NonExist convert begin status a resource should be created",
							RdoPackage.eINSTANCE.operationRelevantResource_End)

					if (end == "Create" && begin != "NonExist")
						error("Invalid convert status: "+begin+" - for Create convert end status there is no resource initially",
							RdoPackage.eINSTANCE.operationRelevantResource_Begin)

					if (begin == "Erase" && end != "NonExist")
						error("Invalid convert status: "+end+" - convert end status for erased resource should be NonExist",
							RdoPackage.eINSTANCE.operationRelevantResource_End)

					if (end == "NonExist" && begin != "Erase")
						error("Invalid convert status: "+begin+" - relevant resource isn't being erased",
							RdoPackage.eINSTANCE.operationRelevantResource_Begin)
				}
			}

			ResourceDeclaration:
			{
				if (begin == "Create" || begin == "Erase" || begin == "NonExist")
					error("Invalid convert status: "+begin+" - only Keep and NoChange statuses could be used for resource",
						RdoPackage.eINSTANCE.operationRelevantResource_Begin)

				if (end == "Create" || end == "Erase" || end == "NonExist")
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

		if (rule == "NonExist") {
			error("Invalid convert status: "+rule+" couldn't be used in condition-action rule",
				RdoPackage.eINSTANCE.ruleRelevantResource_Rule)
			return
		}

		switch type
		{
			ResourceType:
			{
				if (type.type.literal == "permanent")
					if (rule == "Create" || rule == "Erase")
							error("Invalid convert status: "+rule+" - resource type " + type.getNameGeneric + " is permanent",
								RdoPackage.eINSTANCE.ruleRelevantResource_Rule)
			}
			ResourceDeclaration:
			{
				if (rule == "Create" || rule == "Erase")
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

		if (rule == "NonExist" || rule == "Erase" || rule == "NoChange")
		{
			error("Invalid convert status: "+rule+" couldn't be used in event",
				RdoPackage.eINSTANCE.eventRelevantResource_Rule)
			return
		}

		switch type
		{
			ResourceType:
			{
				if (rule == 'Keep')
					error("Invalid convert status: "+rule+" couldn't be used for resource type in event",
						RdoPackage.eINSTANCE.eventRelevantResource_Rule)
				else
					if (type.type.literal == "permanent")
						error("Invalid resource type: '"+type.name+"' is not temporary",
							RdoPackage.eINSTANCE.eventRelevantResource_Rule)
			}
			ResourceDeclaration:
				if (rule == 'Create')
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

				for (r : relreslist)
				{
					i = i + 1
					count = 0
					found = false
					for (c : convertlist)
						if (c.relres.name == r.name)
						{
							count = count + 1
							found = true
						}

					if (count > 1)
						for (c : convertlist)
							if (c.relres == r)
								error("Multiple converts for relevant resource " + r.name,
									c, RdoPackage.eINSTANCE.operationConvert_Relres)
					if (!found)
						error("No convert found for relevant resource " + r.name,
							r, RdoPackage.eINSTANCE.operationRelevantResource_Name)

					j = 0
					if (found && (count == 1))
						for (c : convertlist)
						{
							j = j + 1
							if ((i == j) && (c.relres != r))
								if (c.relres.name != null)
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

				for (r : relreslist)
				{
					i = i + 1
					count = 0
					found = false
					for (c : convertlist)
						if (c.relres.name == r.name)
						{
							count = count + 1
							found = true
						}

					if (count > 1)
						for (c : convertlist)
							if (c.relres == r)
								error("Multiple converts for relevant resource " + r.name,
									c, RdoPackage.eINSTANCE.ruleConvert_Relres)
					if (!found)
						error("No convert found for relevant resource " + r.name,
							r, RdoPackage.eINSTANCE.ruleRelevantResource_Name)

					j = 0
					if (found && (count == 1))
						for (c : convertlist)
						{
							j = j + 1
							if ((i == j) && (c.relres != r))
								if (c.relres.name != null)
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

				for (r : relreslist)
				{
					i = i + 1
					count = 0
					found = false
					for (c : convertlist)
						if (c.relres.name == r.name)
						{
							count = count + 1
							found = true
						}

					if (count > 1)
						for (c : convertlist)
							if (c.relres == r)
								error("Multiple converts for relevant resource " + r.name,
									c, RdoPackage.eINSTANCE.eventConvert_Relres)
					if (!found)
						error("No convert found for relevant resource " + r.name,
							r, RdoPackage.eINSTANCE.eventRelevantResource_Name)

					j = 0
					if (found && (count == 1))
						for (c : convertlist)
						{
							j = j + 1
							if ((i == j) && (c.relres != r))
								if (c.relres.name != null)
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

		if (begin == "Keep" || begin == "Create")
			if (!c.havebegin)
				error("Resource " + c.relres.name + " with convert status "+begin+" "+end+" is missing a convert begin",
					RdoPackage.eINSTANCE.operationConvert_Relres)

		if (end == "Keep" || end == "Create")
			if (!c.haveend)
				error("Resource " + c.relres.name + " with convert status "+begin+" "+end+" is missing a convert end",
					RdoPackage.eINSTANCE.operationConvert_Relres)

		if (begin == "Erase" || begin == "NonExist" || begin == "NoChange")
			if (c.havebegin)
				error("Resource " + c.relres.name + " with convert status "+begin+" "+end+" shouldn't have a convert begin",
					RdoPackage.eINSTANCE.operationConvert_Havebegin)

		if (end == "Erase" || end == "NonExist" || end == "NoChange")
			if (c.haveend)
				error("Resource " + c.relres.name + " with convert status "+begin+" "+end+" shouldn't have a convert end",
					RdoPackage.eINSTANCE.operationConvert_Haveend)
	}

	@Check
	def checkConvertAssociations(RuleConvert c)
	{
		val rule = c.relres.rule.literal

		if (rule == "Keep" || rule == "Create")
			if (!c.haverule)
				error("Resource " + c.relres.name + " with convert status "+rule+" is missing a convert rule",
					RdoPackage.eINSTANCE.ruleConvert_Relres)

		if (rule == "Erase" || rule == "NoChange")
			if (c.haverule)
				error("Resource " + c.relres.name + " with convert status "+rule+" shouldn't have a convert rule",
					RdoPackage.eINSTANCE.ruleConvert_Haverule)

		if (rule == "Create" && c.havechoice)
			error("Relevant resource " + c.relres.name + " with convert status "+rule+" shouldn't have a choice from",
				RdoPackage.eINSTANCE.ruleConvert_Havechoice)

		if (c.relres.type instanceof ResourceDeclaration && c.havechoice)
			error("Relevant resource " + c.relres.name + " shouldn't have a choice from for being a resource declaration",
				RdoPackage.eINSTANCE.ruleConvert_Havechoice)
	}

	@Check
	def checkForCombinationalChioceMethod(Pattern pat)
	{
		var havechoicemethods = false
		var iscombinatorial = false

		for (e : pat.eAllContents.toList.filter(typeof(PatternChoiceMethod)))
		{
			switch e.eContainer
			{
				Operation: iscombinatorial = true
				Rule     : iscombinatorial = true

				OperationConvert: havechoicemethods = true
				RuleConvert     : havechoicemethods = true
			}
		}

		if (havechoicemethods && iscombinatorial)
			for (e : pat.eAllContents.toList.filter(typeof(PatternChoiceMethod)))
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

}
