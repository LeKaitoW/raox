package ru.bmstu.rk9.rdo.validation

import java.util.List

import org.eclipse.xtext.validation.Check

import ru.bmstu.rk9.rdo.rdo.RdoPackage

import org.eclipse.emf.ecore.EObject

import ru.bmstu.rk9.rdo.rdo.RDOSuchAs

import ru.bmstu.rk9.rdo.rdo.ResourceType
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterSuchAs

import ru.bmstu.rk9.rdo.rdo.ResourceDeclaration
import ru.bmstu.rk9.rdo.rdo.ResourceTrace

import ru.bmstu.rk9.rdo.rdo.ConstantDeclaration

import ru.bmstu.rk9.rdo.rdo.Pattern
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
import ru.bmstu.rk9.rdo.rdo.ResourceTypeParameter
import java.util.ArrayList
import ru.bmstu.rk9.rdo.rdo.RDOModel
import ru.bmstu.rk9.rdo.customizations.RDOQualifiedNameProvider
import ru.bmstu.rk9.rdo.generator.RDONaming

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
				text = RDONaming.getNameGeneric(first.eContainer) + "." + RDONaming.getNameGeneric(first)

			default:
				text = RDONaming.getNameGeneric(first)
		}
	}
	
	public def boolean add(EObject object)
	{
		var ret = !history.contains(object)
		history.add(object)
		
		var extmodel = ""
		var extname = RDOQualifiedNameProvider.computeFromURI((object.eContainer.eContainer as RDOModel))
		if (RDOQualifiedNameProvider.computeFromURI((history.head.eContainer.eContainer as RDOModel)) != extname)
			extmodel = extmodel + extname + "."
		
		switch object
		{
			ResourceTypeParameter:
				text = text + " \u2192 " + extmodel + RDONaming.getNameGeneric(object.eContainer) + "." + RDONaming.getNameGeneric(object)

			default:
				text = text + " \u2192 " + extmodel + RDONaming.getNameGeneric(object)
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
	//====== Extension methods =================================//
	def getNameGeneric (EObject o) { RDONaming.getNameGeneric(o) }
	//==========================================================//
	
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
					RdoPackage.Literals.META_SUCH_AS__NAME)
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
				first, RdoPackage.Literals.META_SUCH_AS__NAME)
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
						e, RdoPackage.Literals.RESOURCE_TRACE__TRACE)
	}

	// чем обоснован запрет Create в convert_end при отличных от NonExist статусах convert_begin
	// создадим два ресурса, какие проблемы
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
								RdoPackage.Literals.OPERATION_RELEVANT_RESOURCE__BEGIN)

					if (end == "Create" || end == "Erase" || end == "NonExist")
						error("Invalid convert status: "+end+" - resource type " + type.getNameGeneric + " is permanent",
								RdoPackage.Literals.OPERATION_RELEVANT_RESOURCE__END)
				}
				else
				{
					if (begin == "NonExist" && end != "Create")
						error("Invalid convert status: "+end+" - for NonExist convert begin status a resource should be created",
								RdoPackage.Literals.OPERATION_RELEVANT_RESOURCE__END)
	
					if (begin == "Erase" && end != "NonExist")
						error("Invalid convert status: "+end+" - convert end status for erased resource should be NonExist",
								RdoPackage.Literals.OPERATION_RELEVANT_RESOURCE__END)
	
					if (end == "NonExist" && begin != "Erase")
						error("Invalid convert status: "+begin+" - relevant resource isn't being erased",
								RdoPackage.Literals.OPERATION_RELEVANT_RESOURCE__BEGIN)
				}
			}

			ResourceDeclaration:
			{
				if (begin == "Create" || begin == "Erase" || begin == "NonExist")
					error("Invalid convert status: "+begin+" - only Keep and NoChange statuses could be used for resource",
							RdoPackage.Literals.OPERATION_RELEVANT_RESOURCE__BEGIN)

				if (end == "Create" || end == "Erase" || end == "NonExist")
					error("Invalid convert status: "+end+" - only Keep and NoChange statuses could be used for resource",
							RdoPackage.Literals.OPERATION_RELEVANT_RESOURCE__END)
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
					RdoPackage.Literals.RULE_RELEVANT_RESOURCE__RULE)
			return
		}

		switch type
		{
			ResourceType: 
			{
				if (type.type.literal == "permanent")
					if (rule == "Create" || rule == "Erase")
							error("Invalid convert status: "+rule+" - resource type " + type.getNameGeneric + " is permanent",
									RdoPackage.Literals.RULE_RELEVANT_RESOURCE__RULE)
			}
			ResourceDeclaration:
			{
				if (rule == "Create" || rule == "Erase")
					error("Invalid convert status: "+rule+" - only Keep and NoChange statuses could be used for resource",
							RdoPackage.Literals.RULE_RELEVANT_RESOURCE__RULE)
			}
		}
	}	
	
	// и почему это для типов ресурсов нельзя использовать Keep
	@Check
	def checkConvertStatus(EventRelevantResource relres)
	{
		val type = relres.type
		val rule = relres.rule.literal

		if (rule == "NonExist" || rule == "Erase" || rule == "NoChange")
		{
			error("Invalid convert status: "+rule+" couldn't be used in event",
					RdoPackage.Literals.EVENT_RELEVANT_RESOURCE__RULE)
			return
		}

		switch type
		{
			ResourceType:
				if (rule == 'Keep')
					error("Invalid convert status: "+rule+" couldn't be used for resource type in event",
							RdoPackage.Literals.EVENT_RELEVANT_RESOURCE__RULE)
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
						if (c.relres == r)
						{
							count = count + 1
							found = true
						}
	
					if (count > 1)
						for (c : convertlist)
							if (c.relres == r)
								error("Multiple converts for relevant resource " + r.name,
										c, RdoPackage.Literals.OPERATION_CONVERT__RELRES)
					if (!found)
						error("No convert found for relevant resource " + r.name,
							r, RdoPackage.Literals.OPERATION_RELEVANT_RESOURCE__NAME)

					j = 0
					if (found && (count == 1))
						for (c : convertlist)
						{
							j = j + 1
							if ((i == j) && (c.relres != r))
								if (c.relres.name != null)
									error("Wrong relevant resource converts order: found " +
										c.relres.name +	" instead of " + r.name, c,
											RdoPackage.Literals.OPERATION_CONVERT__RELRES)
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
						if (c.relres == r)
						{
							count = count + 1
							found = true
						}
	
					if (count > 1)
						for (c : convertlist)
							if (c.relres == r)
								error("Multiple converts for relevant resource " + r.name,
										c, RdoPackage.Literals.RULE_CONVERT__RELRES)
					if (!found)
						error("No convert found for relevant resource " + r.name,
							r, RdoPackage.Literals.RULE_RELEVANT_RESOURCE__NAME)

					j = 0
					if (found && (count == 1))
						for (c : convertlist)
						{
							j = j + 1
							if ((i == j) && (c.relres != r))
								if (c.relres.name != null)
									error("Wrong relevant resource converts order: found " +
										c.relres.name +	" instead of " + r.name, c,
											RdoPackage.Literals.RULE_CONVERT__RELRES)
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
						if (c.relres == r)
						{
							count = count + 1
							found = true
						}
	
					if (count > 1)
						for (c : convertlist)
							if (c.relres == r)
								error("Multiple converts for relevant resource " + r.name,
										c, RdoPackage.Literals.EVENT_CONVERT__RELRES)
					if (!found)
						error("No convert found for relevant resource " + r.name,
							r, RdoPackage.Literals.EVENT_RELEVANT_RESOURCE__NAME)

					j = 0
					if (found && (count == 1))
						for (c : convertlist)
						{
							j = j + 1
							if ((i == j) && (c.relres != r))
								if (c.relres.name != null)
									error("Wrong relevant resource converts order: found " +
										c.relres.name +	" instead of " + r.name, c,
											RdoPackage.Literals.EVENT_CONVERT__RELRES)
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
					RdoPackage.Literals.OPERATION_CONVERT__RELRES)

		if (end == "Keep" || end == "Create")
			if (!c.haveend)
				error("Resource " + c.relres.name + " with convert status "+begin+" "+end+" is missing a convert end",
					RdoPackage.Literals.OPERATION_CONVERT__RELRES)

		if (begin == "Erase" || begin == "NonExist" || begin == "NoChange")
			if (c.havebegin)
				error("Resource " + c.relres.name + " with convert status "+begin+" "+end+" shouldn't have a convert begin",
					RdoPackage.Literals.OPERATION_CONVERT__HAVEBEGIN)

		if (end == "Erase" || end == "NonExist" || end == "NoChange")
			if (c.haveend)
				error("Resource " + c.relres.name + " with convert status "+begin+" "+end+" shouldn't have a convert end",
					RdoPackage.Literals.OPERATION_CONVERT__HAVEEND)
	}
	
	@Check
	def checkConvertAssociations(RuleConvert c)
	{
		val rule = c.relres.rule.literal
		
		if (rule == "Keep" || rule == "Create")
			if (!c.haverule)
				error("Resource " + c.relres.name + " with convert status "+rule+" is missing a convert rule",
					RdoPackage.Literals.RULE_CONVERT__RELRES)

		if (rule == "Erase" || rule == "NoChange")
			if (c.haverule)
				error("Resource " + c.relres.name + " with convert status "+rule+" shouldn't have a convert rule",
					RdoPackage.Literals.RULE_CONVERT__HAVERULE)
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
						error("Operation " + pat.name + " already uses combinational approach for relevant resources search",
								e.eContainer, RdoPackage.Literals.OPERATION_CONVERT__CHOICEMETHOD)

					RuleConvert:
						error("Rule " + pat.name + " already uses combinational approach for relevant resources search",
								e.eContainer, RdoPackage.Literals.RULE_CONVERT__CHOICEMETHOD)
				}
			}
	}

}
