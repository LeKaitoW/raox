package ru.bmstu.rk9.rao.validation

import java.util.ArrayList
import java.util.HashMap
import java.util.List
import java.util.Map
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EStructuralFeature
import org.eclipse.xtext.validation.Check
import ru.bmstu.rk9.rao.rao.Constant
import ru.bmstu.rk9.rao.rao.DefaultMethod
import ru.bmstu.rk9.rao.rao.Frame
import ru.bmstu.rk9.rao.rao.FunctionDeclaration
import ru.bmstu.rk9.rao.rao.Pattern
import ru.bmstu.rk9.rao.rao.RaoModel
import ru.bmstu.rk9.rao.rao.RaoPackage
import ru.bmstu.rk9.rao.rao.RelevantResource
import ru.bmstu.rk9.rao.rao.ResourceType
import ru.bmstu.rk9.rao.rao.ResourceDeclaration
import ru.bmstu.rk9.rao.rao.Result
import ru.bmstu.rk9.rao.rao.Sequence

import static extension ru.bmstu.rk9.rao.jvmmodel.RaoNaming.*
import ru.bmstu.rk9.rao.lib.sequence.NumericSequence
import ru.bmstu.rk9.rao.lib.sequence.ArbitraryTypeSequence
import ru.bmstu.rk9.rao.rao.Logic
import ru.bmstu.rk9.rao.rao.Search
import org.eclipse.xtext.xbase.XNullLiteral
import org.eclipse.xtext.xbase.XExpression
import ru.bmstu.rk9.rao.rao.EntityCreation
import ru.bmstu.rk9.rao.rao.RaoEntity

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
			case OPERATION:
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
			eObject instanceof Logic ||
			eObject instanceof Search ||
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

	def private checkForNull(XExpression expression) {
		return expression.eClass.instanceClass.equals(typeof(XNullLiteral))
	}

	@Check
	def checkResourceDeclaration(ResourceDeclaration resource) {
		if (!resource.constructor.actualType.isSubtypeOf(typeof(ru.bmstu.rk9.rao.lib.resource.Resource)))
			error("Error in declaration of \"" + resource.name + "\": only Rao resources are allowed.",
				RaoPackage.eINSTANCE.entityCreation_Constructor)
	}

	@Check
	def checkRelevantResourceDeclaration(RelevantResource relevant) {
		if (!relevant.value.actualType.isSubtypeOf(typeof(ru.bmstu.rk9.rao.lib.resource.Resource)))
			error("Error in declaration of \"" + relevant.name + "\": only Rao resources are allowed.",
				RaoPackage.eINSTANCE.relevantResource_Value)
	}

	@Check
	def checkSequenceDeclaration(Sequence sequence) {
		if (!sequence.constructor.actualType.isSubtypeOf(typeof(NumericSequence)) &&
				!sequence.constructor.actualType.isSubtypeOf(typeof(ArbitraryTypeSequence))) {
			error("Error in declaration of \"" + sequence.name + "\": only Rao sequences are allowed.",
				RaoPackage.eINSTANCE.entityCreation_Constructor)
		}
	}

	@Check
	def checkLogicDeclaration(Logic logic) {
		if (!logic.constructor.actualType.isSubtypeOf(ru.bmstu.rk9.rao.lib.dpt.Logic)) {
			error("Error in declaration of \"" + logic.name + "\": must be Logic class subtype.",
				RaoPackage.eINSTANCE.entityCreation_Constructor)
		}
	}

	@Check
	def checkSearchDeclaration(Search search) {
		if (!search.constructor.actualType.isSubtypeOf(ru.bmstu.rk9.rao.lib.dpt.Search)) {
			error("Error in declaration of \"" + search.name + "\": must be Search class subtype.",
				RaoPackage.eINSTANCE.entityCreation_Constructor)
		}
	}

	@Check
	def checkEntityNotNull(EntityCreation entity) {
		if (entity.constructor.checkForNull)
			error("Error in declaration of \"" + entity.name + "\": cannot be null.",
				RaoPackage.eINSTANCE.entityCreation_Constructor)
	}

	def private EStructuralFeature getNameStructuralFeature(EObject object)
	{
		switch object
		{
			RaoEntity:
				RaoPackage.eINSTANCE.raoEntity_Name

			RelevantResource:
				RaoPackage.eINSTANCE.relevantResource_Name
		}
	}
}
