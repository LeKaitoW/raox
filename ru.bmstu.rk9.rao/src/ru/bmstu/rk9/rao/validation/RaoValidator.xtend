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

import static extension ru.bmstu.rk9.rao.naming.RaoNaming.*
import ru.bmstu.rk9.rao.lib.sequence.NumericSequence
import ru.bmstu.rk9.rao.lib.sequence.ArbitraryTypeSequence
import ru.bmstu.rk9.rao.rao.Logic
import ru.bmstu.rk9.rao.rao.Search
import org.eclipse.xtext.xbase.XNullLiteral
import org.eclipse.xtext.xbase.XExpression
import ru.bmstu.rk9.rao.rao.EntityCreation
import ru.bmstu.rk9.rao.rao.RaoEntity
import java.util.Set
import java.util.HashSet
import ru.bmstu.rk9.rao.rao.Activity
import ru.bmstu.rk9.rao.rao.Edge
import ru.bmstu.rk9.rao.rao.Event
import ru.bmstu.rk9.rao.rao.DataSource
import ru.bmstu.rk9.rao.rao.RelevantResourceTuple
import ru.bmstu.rk9.rao.validation.DefaultMethodsHelper.MethodInfo
import ru.bmstu.rk9.rao.rao.VarConst

class RaoValidator extends AbstractRaoValidator {

	def private checkDefaultMethodCountGeneric(
		EObject parent,
		Iterable<DefaultMethod> methods,
		DefaultMethodsHelper.AbstractMethodInfo[] methodInfos
	) {
		var Map<String, MethodInfo> counts = new HashMap<String, MethodInfo>()
		for (value : methodInfos)
			counts.put(
				value.name,
				new MethodInfo(value.validatorAction, value.parameters)
			)
		for (method : methods) {
			if (!counts.containsKey(method.name))
				error(
					"Error - incorrect default method name",
					method,
					method.getNameStructuralFeature
				)
			else if (counts.get(method.name).count > 0)
				error(
					"Error - default method cannot be set more than once",
					method,
					method.getNameStructuralFeature
				)
			else {
				var defaultParameters = counts.get(method.name).parameters;
				val parameterString = newArrayList();
				for (parameter : method.parameters) {
					parameterString.add(parameter.parameterType.simpleName + " " + parameter.name);
				}
				if (!defaultParameters.toString.equals(parameterString.toString))
					error(
						"Error - incorrect default method parameters. Parameters should be " + defaultParameters.toString,
						method,
						method.getNameStructuralFeature
					)
				var count = counts.get(method.name)
				count.count++
				counts.put(method.name, count)
			}
		}

		for (name : counts.keySet)
			if (counts.get(name).count == 0) {
				switch counts.get(name).action {
					case WARNING:
						warning("Warning - default method " + name + " not set", parent,
							parent.getNameStructuralFeature)
					case ERROR:
						error("Error - default method " + name + " not set", parent, parent.getNameStructuralFeature)
					default: {
					}
				}
			}
	}

	@Check
	def checkDefaultMethodGlobalCount(RaoModel model) {
		var methods = model.objects.filter(typeof(DefaultMethod))
		checkDefaultMethodCountGeneric(model, methods, DefaultMethodsHelper.GlobalMethodInfo.values)
	}

	@Check
	def checkDefaultMethodPatternCount(Pattern pattern) {
		switch (pattern.type) {
			case RULE:
				checkDefaultMethodCountGeneric(pattern, pattern.defaultMethods, DefaultMethodsHelper.RuleMethodInfo.values)
			case OPERATION:
				checkDefaultMethodCountGeneric(pattern, pattern.defaultMethods,
					DefaultMethodsHelper.OperationMethodInfo.values)
		}
	}

	@Check
	def checkDefaultMethodLogicCount(Logic logic) {
		checkDefaultMethodCountGeneric(logic, logic.defaultMethods, DefaultMethodsHelper.DptMethodInfo.values)
	}

	@Check
	def checkDefaultMethodSearchCount(Search search) {
		checkDefaultMethodCountGeneric(search, search.defaultMethods, DefaultMethodsHelper.DptMethodInfo.values)
	}

	@Check
	def checkDefaultMethodDataSourceCount(DataSource dataSource) {
		checkDefaultMethodCountGeneric(dataSource, dataSource.defaultMethods, DefaultMethodsHelper.DataSourceMethodInfo.values)
	}

	@Check
	def checkDefaultMethodFrameCount(Frame frame) {
		checkDefaultMethodCountGeneric(frame, frame.defaultMethods, DefaultMethodsHelper.FrameMethodInfo.values)
	}

	@Check
	def checkVarConstDependencies(RaoModel model) {
		var vcList = model.objects.filter(typeof(VarConst)).toList
		
		val List<String> vcNames = new ArrayList<String>()
		
		for (vc : vcList)
			vcNames.add(vc.nameGeneric)
		
		for (vc : vcList) {
			if (vc.lambda !== null) {
				for (param : vc.lambda.parameters) {
					if (!vcNames.contains(param.name) ||
							vcNames.indexOf(param.name) > vcNames.indexOf(vc.nameGeneric)
					) {
						error("Error - lambda of \"" + vc.nameGeneric + "\" takes reference of undefined VarConst object",
								param, param.nameStructuralFeature
						)
					}
				}
			}
		}
	}

	@Check
	def checkDuplicateNamesForEntities(RaoModel model) {
		val List<String> entities = new ArrayList<String>()
		val List<String> duplicates = new ArrayList<String>()

		val List<EObject> checklist = model.eAllContents.filter [ eObject |
			eObject instanceof ResourceType || eObject instanceof ResourceDeclaration || eObject instanceof Sequence ||
				eObject instanceof Constant || eObject instanceof FunctionDeclaration || eObject instanceof Pattern ||
				eObject instanceof Logic || eObject instanceof Search || eObject instanceof Frame ||
				eObject instanceof Result || eObject instanceof VarConst
		].toList

		for (eObject : checklist) {
			if (eObject.nameGeneric.equals(model.nameGeneric))
				error("Error - object cannot have the same name as model ('" + eObject.nameGeneric + "').",
					eObject, eObject.getNameStructuralFeature)

			val name = eObject.fullyQualifiedName
			if (entities.contains(name)) {
				if (!duplicates.contains(name))
					duplicates.add(name)
			} else
				entities.add(name)
		}

		if (!duplicates.empty) {
			for (eObject : checklist) {
				val name = eObject.fullyQualifiedName
				val hasNoName = (name.contains(".") && name.substring(name.lastIndexOf(".") + 1) == "null")
				if (!hasNoName && duplicates.contains(name))
					error("Error - multiple declarations of object '" + name + "'.", eObject,
						eObject.getNameStructuralFeature)
			}
		}
	}

	@Check
	def checkDuplicateNamesForRelevants(Pattern pattern) {
		val Set<String> names = new HashSet<String>()
		val Set<String> duplicates = new HashSet<String>()

		for (relevant : pattern.relevantResources) {
			if (names.contains(relevant.name)) {
				duplicates.add(relevant.name)
			} else {
				names.add(relevant.name)
			}
		}

		for (tuple : pattern.relevantTuples) {
			for (name : tuple.names) {
				if (names.contains(name)) {
					duplicates.add(name)
				} else {
					names.add(name)
				}
			}
		}

		if (!duplicates.empty) {
			for (relevant : pattern.relevantResources) {
				val name = relevant.name
				if (duplicates.contains(name))
					error("Error - multiple declarations of relevant resource '" + name + "'.", relevant,
						relevant.getNameStructuralFeature)
			}

			for (tuple : pattern.relevantTuples) {
				for (name : tuple.names) {
					if (duplicates.contains(name))
						error("Error - multiple declarations of relevant resource '" + name + "'.", tuple,
							tuple.getNameStructuralFeature)
				}
			}
		}
	}

	@Check
	def checkNumRelevantTuplesInPattern(Pattern pattern) {
		if (pattern.relevantTuples.size > 1) {
			error("Error - only one combinational selection of relevant resources is allowed", pattern,
				pattern.getNameStructuralFeature)
		}
	}

	@Check
	def checkRelevantsCount(RelevantResourceTuple tuple) {
		if (tuple.names.size != tuple.types.size) {
			error("Error - numbers of names and types declarations should be equal for relevant set", tuple,
				tuple.getNameStructuralFeature)
		}
	}

	enum NameCase {
		FIRST_LOWER,
		FIRST_UPPER,
		DOES_NOT_MATTER
	}

	@Check
	def dispatch checkCase(RaoEntity entity) {
		var NameCase expectedNameCase = NameCase.DOES_NOT_MATTER

		if (entity.name == null || entity.name.isEmpty)
			return;

		if (entity instanceof ResourceDeclaration
				|| entity instanceof Sequence
				|| entity instanceof VarConst
				|| entity instanceof DefaultMethod)
			expectedNameCase = NameCase.FIRST_LOWER

		if (entity instanceof ResourceType
				|| entity instanceof Pattern
				|| entity instanceof Event
				|| entity instanceof Logic
				|| entity instanceof Search
				|| entity instanceof Frame)
			expectedNameCase = NameCase.FIRST_UPPER

		checkCaseInternal(entity, entity.name, expectedNameCase, RaoPackage.eINSTANCE.raoEntity_Name)
	}

	@Check
	def dispatch checkCase(RelevantResource relevant) {
		checkCaseInternal(relevant, relevant.name, NameCase.FIRST_LOWER, RaoPackage.eINSTANCE.relevantResource_Name)
	}

	@Check
	def dispatch checkCase(Activity activity) {
		checkCaseInternal(activity, activity.name, NameCase.FIRST_LOWER, RaoPackage.eINSTANCE.activity_Name)
	}

	@Check
	def dispatch checkCase(Edge edge) {
		checkCaseInternal(edge, edge.name, NameCase.FIRST_LOWER, RaoPackage.eINSTANCE.edge_Name)
	}

	def checkCaseInternal(EObject object, String name, NameCase expectedNameCase, EStructuralFeature feature) {
		var String description
		var NameCase actualNameCase

		if (expectedNameCase == NameCase.DOES_NOT_MATTER)
			return;

		if (expectedNameCase == NameCase.FIRST_LOWER)
			description = "lowercase"
		else
			description = "uppercase"

		if (Character.isUpperCase(name.codePointAt(0)))
			actualNameCase = NameCase.FIRST_UPPER
		else
			actualNameCase = NameCase.FIRST_LOWER

		if (expectedNameCase != actualNameCase)
			error("Error in declaration of \"" + name + "\": should start with " + description + " letter",
				feature)
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
	def checkActivityDeclaration(Activity activity) {
		if (!activity.constructor.actualType.isSubtypeOf(ru.bmstu.rk9.rao.lib.dpt.Activity)) {
			error("Error in declaration of \"" + activity.name + "\": must be Activity class subtype.",
				RaoPackage.eINSTANCE.activity_Constructor)
		}
	}

	@Check
	def checkEdgeDeclaration(Edge edge) {
		if (!edge.constructor.actualType.isSubtypeOf(ru.bmstu.rk9.rao.lib.dpt.Edge)) {
			error("Error in declaration of \"" + edge.name + "\": must be Edge class subtype.",
				RaoPackage.eINSTANCE.edge_Constructor)
		}
	}

	@Check
	def checkResultDeclaration(Result result) {
		if (!result.constructor.actualType.isSubtypeOf(typeof(ru.bmstu.rk9.rao.lib.result.AbstractResult)))
			error("Error in declaration of \"" + result.name + "\": only Rao results are allowed. Use Result.create() method",
				RaoPackage.eINSTANCE.result_Constructor)
	}

	@Check
	def checkEntityNotNull(EntityCreation entity) {
		if (entity.constructor.checkForNull)
			error("Error in declaration of \"" + entity.name + "\": cannot be null.",
				RaoPackage.eINSTANCE.entityCreation_Constructor)
	}
	
	@Check
	def checkLambdaNotNull(VarConst varconst) {
		if (varconst.lambda !== null && (varconst.lambda.body.checkForNull || varconst.lambda.body.checkForEmpty))
			error("Error in declaration of \"" + varconst.name + "\": lambda's body isn't set up.",
				RaoPackage.eINSTANCE.varConst_Lambda)
	}
	
	@Check 
	def checkVarConstParams(VarConst varconst) {
		if (Double.valueOf(varconst.start) < Double.valueOf(varconst.stop) && 
				Double.valueOf(varconst.step) <= 0) 
			error("Error in declaration of \"" + varconst.name + "\": step cannot be negative or equals to zero, if start < stop",
				RaoPackage.eINSTANCE.varConst_Step)
				
		if (Double.valueOf(varconst.start) > Double.valueOf(varconst.stop) && 
				Double.valueOf(varconst.step) >= 0) 
			error("Error in declaration of \"" + varconst.name + "\": step cannot be positive or equals to zero, if start > stop",
				RaoPackage.eINSTANCE.varConst_Step)
	}
	
	@Check
	def checkLambdaSelfReference(VarConst varconst) {
		if (varconst.lambda !== null) {
			
			var isContains = 0
			
			for (param : varconst.lambda.parameters) {
				if (param.name == varconst.name)
					isContains++
			}
			if (isContains == 0)
				error("Error in declaration of \"" + varconst.name + "\": lambda should take itself in args",
				RaoPackage.eINSTANCE.varConst_Lambda)
		}
	}

	def private checkForNull(XExpression expression) {
		return expression.eClass.instanceClass.equals(typeof(XNullLiteral))
	}
	
	def private checkForEmpty(XExpression expression) {
		return expression.toString.replaceAll("[\\p{Ps}\\p{Pe}]", "").strip().equals("")
	}

	def private EStructuralFeature getNameStructuralFeature(EObject object) {
		switch object {
			RaoEntity:
				RaoPackage.eINSTANCE.raoEntity_Name
			RelevantResource:
				RaoPackage.eINSTANCE.relevantResource_Name
		}
	}
}
