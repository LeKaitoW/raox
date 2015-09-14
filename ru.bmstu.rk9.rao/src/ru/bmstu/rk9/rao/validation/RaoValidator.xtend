package ru.bmstu.rk9.rao.validation

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

import ru.bmstu.rk9.rao.generator.GlobalContext

import static extension ru.bmstu.rk9.rao.generator.RaoNaming.*
import static extension ru.bmstu.rk9.rao.generator.RaoExpressionCompiler.*

import ru.bmstu.rk9.rao.rao.RaoPackage

import ru.bmstu.rk9.rao.rao.RaoModel

import ru.bmstu.rk9.rao.rao.ResourceType

import ru.bmstu.rk9.rao.rao.ResourceCreateStatement

import ru.bmstu.rk9.rao.rao.Sequence

import ru.bmstu.rk9.rao.rao.Constant

import ru.bmstu.rk9.rao.rao.Function
import ru.bmstu.rk9.rao.rao.FunctionTable

import ru.bmstu.rk9.rao.rao.Pattern
import ru.bmstu.rk9.rao.rao.Parameter
import ru.bmstu.rk9.rao.rao.PatternSelectMethod
import ru.bmstu.rk9.rao.rao.RelevantResource
import ru.bmstu.rk9.rao.rao.Event

import ru.bmstu.rk9.rao.rao.DecisionPoint

import ru.bmstu.rk9.rao.rao.Frame

import ru.bmstu.rk9.rao.rao.Result

import ru.bmstu.rk9.rao.rao.RaoInt
import ru.bmstu.rk9.rao.rao.RaoEnum
import ru.bmstu.rk9.rao.rao.DefaultMethod
import ru.bmstu.rk9.rao.rao.DecisionPointSome
import ru.bmstu.rk9.rao.rao.DptSetConditionStatement
import ru.bmstu.rk9.rao.rao.DptSetPriorityStatement
import ru.bmstu.rk9.rao.rao.DptSetParentStatement
import ru.bmstu.rk9.rao.rao.DptEvaluateByStatement
import ru.bmstu.rk9.rao.rao.DptCompareTopsStatement
import ru.bmstu.rk9.rao.rao.DptSetTerminateConditionStatement
import ru.bmstu.rk9.rao.rao.DecisionPointSearch
import ru.bmstu.rk9.rao.rao.FunctionType
import ru.bmstu.rk9.rao.rao.PatternType

class RaoValidator extends AbstractRaoValidator
{

	@Inject
	private ResourceDescriptionsProvider resourceDescriptionsProvider

	@Inject
	private IContainer.Manager containerManager

	private static List<Resource> resourceIndex = new LinkedList<Resource>
	private static HashMap<String, GlobalContext> variableIndex = new HashMap<String, GlobalContext>

	@Check
	def exportResources(RaoModel model)
	{
		val index = resourceDescriptionsProvider.createResourceDescriptions
		val resourceDescription = index.getResourceDescription(model.eResource.URI)

		resourceIndex.clear

		if (resourceDescription == null)
			return
		else
		{
			for (IContainer container : containerManager.getVisibleContainers(resourceDescription, index))
				for (IResourceDescription containerResourceDescription : container.getResourceDescriptions())
				{
					resourceIndex.add(model.eResource.resourceSet.getResource(containerResourceDescription.URI, true))
					if (!variableIndex.containsKey(resourceIndex.last.resourceName))
						variableIndex.put(resourceIndex.last.resourceName, new GlobalContext)
				}

			if (variableIndex.size != resourceIndex.size)
			{
				variableIndex.clear
				for (resource : resourceIndex)
					variableIndex.put(resourceIndex.last.resourceName, new GlobalContext)
			}
		}
	}

	@Check
	def exportResourceCreateStatements(ResourceCreateStatement resourceCreateStatement)
	{
		val model = resourceCreateStatement.modelRoot
		if (variableIndex == null || variableIndex.get(model.nameGeneric) == null)
			return

		val resourceIndex = variableIndex.get(model.nameGeneric).resources
		val resources = model.eAllContents.filter(typeof(ResourceCreateStatement)).toMap[name]
		for (name : resources.keySet)
			if (resourceIndex.get(name) == null || name == resourceCreateStatement.name)
				resourceIndex.put(name, variableIndex.get(model.nameGeneric).newResourceReference(resources.get(name)))

		clearMissing(resourceIndex, resources)
	}

	@Check
	def exportSequences(Sequence sequence)
	{
		val model = sequence.modelRoot
		if (variableIndex == null || variableIndex.get(model.nameGeneric) == null)
			return

		val sequenceIndex = variableIndex.get(model.nameGeneric).sequences
		val sequences = model.eAllContents.filter(typeof(Sequence)).toMap[name]
		for (name : sequences.keySet)
			if (sequenceIndex.get(name) == null || name == sequence.name)
				sequenceIndex.put(name, variableIndex.get(model.nameGeneric).newSequenceReference(sequences.get(name)))

		clearMissing(sequenceIndex, sequences)
	}

	@Check
	def exportConstants(Constant constant)
	{
		val model = constant.modelRoot
		if (variableIndex == null || variableIndex.get(model.nameGeneric) == null)
			return

		val constantIndex = variableIndex.get(model.nameGeneric).constants
		val constants = model.eAllContents.filter(typeof(Constant)).toMap[name]
		for (name : constants.keySet)
			if (constantIndex.get(name) == null || name == constant.name)
				constantIndex.put(name, variableIndex.get(model.nameGeneric).newConstantReference(constants.get(name)))

		clearMissing(constantIndex, constants)
	}

	@Check
	def exportFunctions(Function function)
	{
		val model = function.modelRoot
		if (variableIndex == null || variableIndex.get(model.nameGeneric) == null)
			return

		val functionIndex = variableIndex.get(model.nameGeneric).functions
		val functions = model.eAllContents.filter(typeof(Function)).toMap[type.name]
		for (name : functions.keySet)
			if (functionIndex.get(name) == null || name == function.type.name)
				functionIndex.put(name, variableIndex.get(model.nameGeneric).newFunctionReference(functions.get(name)))

		clearMissing(functionIndex, functions)
	}

	def private clearMissing(Map<String, ?> index, Map<String, ?> model)
	{
		if (index.size != model.size)
		{
			var iter = index.keySet.iterator
			while (iter.hasNext)
			{
				val next = iter.next
				if (model.get(next) == null)
				{
					index.remove(next)
					iter = index.keySet.iterator
				}
			}
		}
	}

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
			eObject instanceof ResourceCreateStatement ||
			eObject instanceof Sequence ||
			eObject instanceof Constant ||
			eObject instanceof Function ||
			eObject instanceof Pattern ||
			eObject instanceof DecisionPoint ||
			eObject instanceof Frame ||
			eObject instanceof Result
		].toList

		for (eObject : checklist)
		{
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
	def checkNamesInPatterns (Pattern pattern)
	{
		val List<EObject> parameters  = pattern.eAllContents.filter[eObject |
			eObject instanceof Parameter
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

			ResourceCreateStatement:
				RaoPackage.eINSTANCE.META_ResourceReference_Name

			Sequence:
				RaoPackage.eINSTANCE.sequence_Name

			FunctionType:
				RaoPackage.eINSTANCE.functionType_Name

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

			Parameter:
				RaoPackage.eINSTANCE.parameter_Name

			RelevantResource:
				RaoPackage.eINSTANCE.META_ResourceReference_Name

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
	def checkTableParameters(FunctionTable function)
	{
		if (function.parameters == null)
			return;

		for (parameter : function.parameters)
		{
			val actual = parameter.type.resolveAllTypes
			if (!(actual instanceof RaoEnum || (actual instanceof RaoInt && (actual as RaoInt).range != null)))
				error("Invalid parameter type. Table function allows enumerative and ranged integer parameters only",
					parameter, RaoPackage.eINSTANCE.functionParameter_Type)
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
