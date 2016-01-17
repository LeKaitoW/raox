package ru.bmstu.rk9.rao.validation

import java.util.ArrayList
import java.util.HashMap
import java.util.List
import java.util.Map
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EStructuralFeature
import org.eclipse.xtext.validation.Check
import ru.bmstu.rk9.rao.rao.Constant
import ru.bmstu.rk9.rao.rao.DecisionPoint
import ru.bmstu.rk9.rao.rao.DecisionPointSearch
import ru.bmstu.rk9.rao.rao.DecisionPointSome
import ru.bmstu.rk9.rao.rao.DefaultMethod
import ru.bmstu.rk9.rao.rao.DptCompareTopsStatement
import ru.bmstu.rk9.rao.rao.DptEvaluateByStatement
import ru.bmstu.rk9.rao.rao.DptSetConditionStatement
import ru.bmstu.rk9.rao.rao.DptSetParentStatement
import ru.bmstu.rk9.rao.rao.DptSetPriorityStatement
import ru.bmstu.rk9.rao.rao.DptSetTerminateConditionStatement
import ru.bmstu.rk9.rao.rao.Event
import ru.bmstu.rk9.rao.rao.Frame
import ru.bmstu.rk9.rao.rao.FunctionDeclaration
import ru.bmstu.rk9.rao.rao.FieldDeclaration
import ru.bmstu.rk9.rao.rao.Pattern
import ru.bmstu.rk9.rao.rao.PatternSelectMethod
import ru.bmstu.rk9.rao.rao.PatternType
import ru.bmstu.rk9.rao.rao.RaoModel
import ru.bmstu.rk9.rao.rao.RaoPackage
import ru.bmstu.rk9.rao.rao.RelevantResource
import ru.bmstu.rk9.rao.rao.ResourceType
import ru.bmstu.rk9.rao.rao.ResourceDeclaration
import ru.bmstu.rk9.rao.rao.Result
import ru.bmstu.rk9.rao.rao.Sequence

import static extension ru.bmstu.rk9.rao.jvmmodel.RaoNaming.*

class RaoValidator extends AbstractRaoValidator
{
	def private checkDefaultMethodCountGeneric(EObject parent,
			Iterable<DefaultMethod> methods,
			Map<String, DefaultMethodsHelper.MethodInfo> counts
	)
	{
		for (method : methods) {
			if (!counts.containsKey(method.name))
				error("Error - incorrect default method name", method,
					method.getNameStructuralFeature
				)
			else if (counts.get(method.name).count > 0)
				error("Error - default method cannot be set more than once", method,
					method.getNameStructuralFeature
				)
			else {
				var count = counts.get(method.name)
				count.count++
				counts.put(method.name, count)
			}
		}

		for (name : counts.keySet)
			if (counts.get(name).count == 0) {
				switch counts.get(name).action {
					case WARNING:
						warning("Warning - default method " + name + " not set",
								parent,
								parent.getNameStructuralFeature)
					case ERROR:
						error("Error - default method " + name + " not set",
								parent,
								parent.getNameStructuralFeature)
					default: {}
				}
			}
	}

	@Check
	def checkDefaultMethodGlobalCount(RaoModel model)
	{
		var Map<String, DefaultMethodsHelper.MethodInfo> counts =
				new HashMap<String, DefaultMethodsHelper.MethodInfo>()
		for (value : DefaultMethodsHelper.GlobalMethodInfo.values)
			counts.put(value.name,
					new DefaultMethodsHelper.MethodInfo(value.validatorAction)
			)

		var methods = model.objects.filter(typeof(DefaultMethod))
		checkDefaultMethodCountGeneric(model, methods, counts)
	}

	@Check
	def checkDefaultMethodPatternCount(Pattern pattern)
	{
		var Map<String, DefaultMethodsHelper.MethodInfo> counts =
				new HashMap<String, DefaultMethodsHelper.MethodInfo>()
		switch (pattern.type)
		{
			case RULE:
				for (value : DefaultMethodsHelper.RuleMethodInfo.values)
					counts.put(value.name,
							new DefaultMethodsHelper.MethodInfo(value.validatorAction))
			case OPERATION,
			case KEYBOARD:
				for (value : DefaultMethodsHelper.OperationMethodInfo.values)
					counts.put(value.name,
							new DefaultMethodsHelper.MethodInfo(value.validatorAction))
		}

		checkDefaultMethodCountGeneric(pattern, pattern.defaultMethods, counts)
	}

	@Check
	def checkDuplicateNamesForEntities(RaoModel model)
	{
		val List<String> entities = new ArrayList<String>()
		val List<String> duplicates = new ArrayList<String>()

		val List<EObject> checklist = model.eAllContents.filter[eObject |
			eObject instanceof ResourceType ||
			eObject instanceof ResourceDeclaration ||
			eObject instanceof Sequence ||
			eObject instanceof Constant ||
			eObject instanceof FunctionDeclaration ||
			eObject instanceof Pattern ||
			eObject instanceof DecisionPoint ||
			eObject instanceof Frame ||
			eObject instanceof Result
		].toList

		for (eObject : checklist)
		{
			if (eObject.nameGeneric.equals(model.nameGeneric))
				error("Error - object cannot have the same name as model ('" + eObject.nameGeneric + "').",
						eObject, eObject.getNameStructuralFeature)

			val name = eObject.fullyQualifiedName
			if (entities.contains(name))
			{
				if (!duplicates.contains(name))
					duplicates.add(name)
			}
			else
				entities.add(name)
		}

		if (!duplicates.empty)
			for (eObject : checklist)
			{
				val name = eObject.fullyQualifiedName
				val hasNoName = (name.contains(".")
					&& name.substring(name.lastIndexOf(".") + 1) == "null"
				)
				if (!hasNoName && duplicates.contains(name))
					error("Error - multiple declarations of object '" + name + "'.", eObject,
						eObject.getNameStructuralFeature)
			}
	}

	@Check
	def checkResourceDeclaration(ResourceDeclaration resource) {
		if (!resource.constructor.actualType.isSubtypeOf(typeof(ru.bmstu.rk9.rao.lib.resource.Resource)))
			error("Error in declaration of \"" + resource.name + "\": only Rao resources are allowed.",
				RaoPackage.eINSTANCE.resourceDeclaration_Constructor)
	}

	@Check
	def checkSequenceDeclaration(Sequence sequence) {
		if (!sequence.constructor.actualType.isSubtypeOf(typeof(ru.bmstu.rk9.rao.lib.sequence.Sequence)))
			error("Error in declaration of \"" + sequence.name + "\": only Rao sequences are allowed.",
				RaoPackage.eINSTANCE.sequence_Constructor)
	}

	@Check
	def checkNamesInPatterns(Pattern pattern)
	{
		val List<EObject> parameters  = pattern.eAllContents.filter[eObject |
			eObject instanceof FieldDeclaration
		].toList
		val List<EObject> relevantResources = pattern.eAllContents.filter[eObject |
			eObject instanceof RelevantResource].toList

		for (parameter : parameters)
			if (relevantResources.map[relevantResource | relevantResource.nameGeneric].contains(parameter.nameGeneric))
				error("Error - parameter name shouldn't match relevant resource name '" + parameter.nameGeneric + "'.", parameter,
					parameter.getNameStructuralFeature)

		for (relevantResource : relevantResources)
			if (parameters.map[parameter | parameter.nameGeneric].contains(relevantResource.nameGeneric))
				error("Error - relevant resource name shouldn't match parameter name '" + relevantResource.nameGeneric + "'.", relevantResource,
					relevantResource.getNameStructuralFeature)
	}

	def private EStructuralFeature getNameStructuralFeature (EObject object)
	{
		switch object
		{
			ResourceType:
				RaoPackage.eINSTANCE.resourceType_Name

			ResourceDeclaration:
				RaoPackage.eINSTANCE.resourceDeclaration_Name

			Sequence:
				RaoPackage.eINSTANCE.sequence_Name

			FunctionDeclaration:
				RaoPackage.eINSTANCE.functionDeclaration_Name

			Pattern:
				RaoPackage.eINSTANCE.pattern_Name

			Event:
				RaoPackage.eINSTANCE.event_Name

			DecisionPoint:
				RaoPackage.eINSTANCE.decisionPoint_Name

			Frame:
				RaoPackage.eINSTANCE.frame_Name

			Result:
				RaoPackage.eINSTANCE.result_Name

			RelevantResource:
				RaoPackage.eINSTANCE.relevantResource_Name

			DptSetConditionStatement:
				RaoPackage.eINSTANCE.dptSetConditionStatement_Name

			DptSetParentStatement:
				RaoPackage.eINSTANCE.dptSetParentStatement_Name

			DptEvaluateByStatement:
				RaoPackage.eINSTANCE.dptEvaluateByStatement_Name

			DptSetPriorityStatement:
				RaoPackage.eINSTANCE.dptSetPriorityStatement_Name

			DptCompareTopsStatement:
				RaoPackage.eINSTANCE.dptCompareTopsStatement_Name

			DptSetTerminateConditionStatement:
				RaoPackage.eINSTANCE.dptSetTerminateConditionStatement_Name
		}
	}

	@Check
	def checkForCombinationalChioceMethod(Pattern pattern)
	{
		var haveSelectMethods = false
		var isCombinatorial = false

		for (selectMethod : pattern.eAllContents.toList.filter(typeof(PatternSelectMethod)))
		{
			switch selectMethod.eContainer
			{
				Pattern: isCombinatorial = true
				RelevantResource: haveSelectMethods = true
			}
		}

		if (haveSelectMethods && isCombinatorial)
			for (selectMethod : pattern.eAllContents.toList.filter(typeof(PatternSelectMethod)))
			{
				switch selectMethod.eContainer
				{
					RelevantResource:
						error("Pattern " + pattern.name + " already uses combinational approach for relevant resources search",
							selectMethod.eContainer, RaoPackage.eINSTANCE.relevantResource_SelectMethod)}
			}
	}

	@Check
	def checkSearchActivities(DecisionPointSearch search)
	{
		for (activity : search.activities)
			if (activity.pattern.type != PatternType.RULE)
				error("Only rules are allowed as search activities",
					activity, RaoPackage.eINSTANCE.decisionPointSearchActivity_Name)
	}

	//TODO generalize set method for dpt instead of validating its name here
	@Check
	def checkDptInitName(DecisionPoint decisionPoint)
	{
		if (decisionPoint.initMethodName != null && decisionPoint.initMethodName != "init")
			error("Invalid default method name \"" + decisionPoint.initMethodName + "\".",
				decisionPoint, RaoPackage.eINSTANCE.decisionPoint_InitMethodName)
	}

	@Check
	def checkDptInitStatements(DecisionPointSome decisionPoint)
	{
		var setCondidionStatements = decisionPoint.initStatements.filter(
				statement | statement instanceof DptSetConditionStatement
			)
		var setPriorStatements = decisionPoint.initStatements.filter(
				statement | statement instanceof DptSetPriorityStatement
			)
		var setParentStatements = decisionPoint.initStatements.filter(
				statement | statement instanceof DptSetParentStatement
			)

		if (setCondidionStatements.size > 1)
			for (statement : setCondidionStatements)
				error("Multiple setCondition() calls are not allowed",
						statement, statement.getNameStructuralFeature)

		if (setPriorStatements.size > 1)
			for (statement : setPriorStatements)
				error("Multiple setPriority() calls are not allowed",
						statement, statement.getNameStructuralFeature)

		if (setParentStatements.size > 1)
			for (statement : setParentStatements)
				error("Multiple setParent() calls are not allowed",
						statement, statement.getNameStructuralFeature)
	}

	@Check
	def checkSearchInitStatements(DecisionPointSearch decisionPoint)
	{
		var setConditionStatements = decisionPoint.initStatements.filter(
				statement | statement instanceof DptSetConditionStatement
			)
		var setParentStatements = decisionPoint.initStatements.filter(
				statement | statement instanceof DptSetParentStatement
			)
		var setTerminateConditionStatements = decisionPoint.initStatements.filter(
				statement | statement instanceof DptSetTerminateConditionStatement
			)
		var evaluateByStatements = decisionPoint.initStatements.filter(
				statement | statement instanceof DptEvaluateByStatement
			)
		var compareTopsStatements = decisionPoint.initStatements.filter(
				statement | statement instanceof DptCompareTopsStatement
			)

		if (setConditionStatements.size > 1)
			for (statement : setConditionStatements)
				error("Multiple setCondition() calls are not allowed",
						statement, statement.getNameStructuralFeature)

		if (setParentStatements.size > 1)
			for (statement : setParentStatements)
				error("Multiple setParent() calls are not allowed",
						statement, statement.getNameStructuralFeature)

		if (setTerminateConditionStatements.size > 1)
			for (statement : setTerminateConditionStatements)
				error("Multiple setTerminateCondition() calls are not allowed",
						statement, statement.getNameStructuralFeature)
		else if (setTerminateConditionStatements.empty)
				error("setTerminateCondition() method must be called explicitly",
						decisionPoint, decisionPoint.getNameStructuralFeature)

		if (evaluateByStatements.size > 1)
			for (statement : evaluateByStatements)
				error("Multiple evaluateBy() calls are not allowed",
						statement, statement.getNameStructuralFeature)
		else if (evaluateByStatements.empty)
				error("evaluateBy() method must be called explicitly",
						decisionPoint, decisionPoint.getNameStructuralFeature)

		if (compareTopsStatements.size > 1)
			for (statement : compareTopsStatements)
				error("Multiple compareTops() calls are not allowed",
						statement, statement.getNameStructuralFeature)
		else if (compareTopsStatements.empty)
				error("compareTops() method must be called explicitly",
						decisionPoint, decisionPoint.getNameStructuralFeature)
	}

	@Check
	def checkSearchPatternType(DecisionPointSearch search)
	{
		val activities = search.activities

		for (activity : activities) {
			val pattern = activity.pattern
			if (pattern.type != PatternType.RULE) {
				error("Invalid pattern name: " + pattern.name + " - only Rule pattern type allowed",
					RaoPackage.eINSTANCE.decisionPoint_Name)
			}
		}
	}
}
