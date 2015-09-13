package ru.bmstu.rk9.rao.generator

import org.eclipse.emf.ecore.EObject

import static extension ru.bmstu.rk9.rao.generator.RaoNaming.*
import static extension ru.bmstu.rk9.rao.compilers.EnumCompiler.*

import ru.bmstu.rk9.rao.rao.RaoDefaultParameter

import ru.bmstu.rk9.rao.rao.ResourceCreateStatement
import ru.bmstu.rk9.rao.rao.ResourceExpressionList

import ru.bmstu.rk9.rao.rao.Constant

import ru.bmstu.rk9.rao.rao.DecisionPointActivity
import ru.bmstu.rk9.rao.rao.DecisionPointSearchActivity

import ru.bmstu.rk9.rao.rao.RaoInt
import ru.bmstu.rk9.rao.rao.RaoDouble
import ru.bmstu.rk9.rao.rao.RaoBoolean
import ru.bmstu.rk9.rao.rao.RaoArray
import ru.bmstu.rk9.rao.rao.RaoString

import ru.bmstu.rk9.rao.rao.ExpressionList
import ru.bmstu.rk9.rao.rao.ExpressionExponentiation
import ru.bmstu.rk9.rao.rao.ExpressionNegate
import ru.bmstu.rk9.rao.rao.ExpressionMultiplication
import ru.bmstu.rk9.rao.rao.ExpressionDivision
import ru.bmstu.rk9.rao.rao.ExpressionModulo
import ru.bmstu.rk9.rao.rao.ExpressionInvert
import ru.bmstu.rk9.rao.rao.ExpressionPlus
import ru.bmstu.rk9.rao.rao.ExpressionMinus
import ru.bmstu.rk9.rao.rao.ExpressionLarger
import ru.bmstu.rk9.rao.rao.ExpressionLarger_Equal
import ru.bmstu.rk9.rao.rao.ExpressionSmaller
import ru.bmstu.rk9.rao.rao.ExpressionSmaller_Equal
import ru.bmstu.rk9.rao.rao.ExpressionEqual
import ru.bmstu.rk9.rao.rao.ExpressionNot_Equal
import ru.bmstu.rk9.rao.rao.ExpressionAnd
import ru.bmstu.rk9.rao.rao.ExpressionOr
import ru.bmstu.rk9.rao.rao.Primary
import ru.bmstu.rk9.rao.rao.IntConstant
import ru.bmstu.rk9.rao.rao.DoubleConstant
import ru.bmstu.rk9.rao.rao.StringConstant
import ru.bmstu.rk9.rao.rao.BoolConstant
import ru.bmstu.rk9.rao.rao.GroupExpression
import ru.bmstu.rk9.rao.rao.SelectExpression
import ru.bmstu.rk9.rao.rao.VariableIncDecExpression
import ru.bmstu.rk9.rao.rao.VariableMethodCallExpression
import ru.bmstu.rk9.rao.rao.VariableExpression
import ru.bmstu.rk9.rao.rao.ExpressionAssignment

import ru.bmstu.rk9.rao.rao.PlanStatement

import ru.bmstu.rk9.rao.rao.TimeNow

import ru.bmstu.rk9.rao.rao.RaoType
import ru.bmstu.rk9.rao.rao.RaoEnum
import ru.bmstu.rk9.rao.rao.Parameter
import org.eclipse.emf.common.util.EList
import ru.bmstu.rk9.rao.rao.Expression

enum ExpressionOperation
{
	plus, minus, multiplication, division, modulo
}

class RaoExpression
{
	public String value
	public String type

	new(String value, String type)
	{
		this.value = value
		this.type = type
	}

	def static RaoExpression getType(RaoExpression left, RaoExpression right, ExpressionOperation operation)
	{
		var type = "unknown"

		if (left.type == "error" || right.type == "error")
			type = "error"

		if (left.type == "Integer")
		{
			if (right.type == "Integer")
				type = "Integer"
			if (right.type == "Double")
				type = "Double"
		}

		if (left.type == "Double")
		{
			if (right.type == "Integer")
				type = "Double"
			if (right.type == "Double")
				type = "Double"
		}

		switch operation
		{
			case division:
				return new RaoExpression(left.value + " / " + right.value, type)

			case minus:
				return new RaoExpression(left.value + " - " + right.value, type)

			case modulo:
				return new RaoExpression(left.value + " % " + right.value, type)

			case multiplication:
				return new RaoExpression(left.value + " * " + right.value, left.type)

			case plus:
				return new RaoExpression(left.value + " + " + right.value, left.type)
		}
	}
}

class RaoExpressionCompiler
{
	private static LocalContext localContext

	def static RaoExpression compileExpressionContext(EObject expression, LocalContext context)
	{
		val backupContext = localContext
		localContext = context
		val ret = expression.compileExpression
		localContext = backupContext

		return ret
	}

	def static RaoExpression compileExpression(EObject expression)
	{
		switch expression
		{
			IntConstant:
				return new RaoExpression(expression.value.toString, "Integer")

			DoubleConstant:
				return new RaoExpression(expression.value.toString, "Double")

			TimeNow:
				return new RaoExpression("Simulator.getTime()", "Double")

			StringConstant:
				return new RaoExpression('"' + expression.value + '"', "String")

			BoolConstant:
				return new RaoExpression(expression.value.toString, "Boolean")

			GroupExpression:
			{
				val oldContext = localContext
				localContext = (new LocalContext(localContext)).populateFromGroupBy(expression.arg)

				val ret =
					'''
					Select.«expression.type.literal»(
						«expression.arg.type.fullyQualifiedName».getAll(),
						new Select.Checker<«expression.arg.type.fullyQualifiedName»>()
						{
							@Override
							public boolean check(«expression.arg.type.fullyQualifiedName» current)
							{
								return «IF expression.arg.condition == null»true«ELSE»«expression.arg.condition.compileExpression.value»«ENDIF»;
							}
						}
					)'''

				localContext = oldContext
				return new RaoExpression(ret, "Boolean")
			}

			SelectExpression:
			{
				val oldContext = localContext
				localContext = (new LocalContext(localContext)).populateFromGroupBy(expression.arg)

				val ret =
					'''
					Select.«expression.method.literal»(
						«expression.arg.type.fullyQualifiedName».getAll(),
						new Select.Checker<«expression.arg.type.fullyQualifiedName»>()
						{
							@Override
							public boolean check(«expression.arg.type.fullyQualifiedName» current)
							{
								return «IF expression.arg.condition == null»true«ELSE»«expression.arg.condition.compileExpression.value
									»«ENDIF»«IF expression.arg2 != null» && «expression.arg2.compileExpression.value»«ENDIF»;
							}
						}
					)'''

				localContext = oldContext
				return new RaoExpression(ret, if (expression.method.literal == "Size") "Integer" else "Boolean")
			}

			VariableIncDecExpression:
			{
				val ret = expression.^var.compileExpression

				if (ret.value.contains(".get_") && ret.value.endsWith("()")) {
					var String incrementDecrement = null
					if (expression.pre != null)
						incrementDecrement = expression.pre
					else
						incrementDecrement = expression.post

					if (incrementDecrement != null)
						return new RaoExpression(ret.value.replace(".get_", ".set_").cutLastChars(2) +
							"(" + ret.value + (if (incrementDecrement == "--") "- 1" else " + 1") + ")", ret.type)
				}

				return new RaoExpression((if (expression.pre != null) expression.pre else "") +
					 ret.value + (if (expression.post != null) expression.post else ""), ret.type)
			}

			VariableMethodCallExpression:
			{
				if (localContext != null)
				{
					var localCall = expression.lookupLocal
					if (localCall != null)
						return new RaoExpression(localCall.generated, localCall.type)
				}

				var globalCall = expression.lookupGlobal

				if (globalCall != null)
					return new RaoExpression(globalCall.generated, globalCall.type)

				var methodCall = ""
				var flag = false

				for (call : expression.calls)
				{
					methodCall = methodCall + (if (flag) "." else "") + call.compileExpression.value
					flag = true
				}

				return new RaoExpression(methodCall, "unknown")
			}

			VariableExpression:
			{
				var call = expression.call

				if (expression.functionFirst)
					call = call + "(" + (if (expression.args != null) expression.args.compileExpression.value else "") + ")"

				if (expression.arrayFirst)
					call = call + "[" + expression.iterator.compileExpression.value + "]"

				if (expression.functionLast)
					call = call + "(" + (if (expression.args != null) expression.args.compileExpression.value else "") + ")"

				if (expression.arrayLast)
					call = call + "[" + expression.iterator.compileExpression.value + "]"

				return new RaoExpression(call, "unknown")
			}

			Primary:
			{
				val ret = expression.exp.compileExpression
				return new RaoExpression("(" + ret.value + ")", ret.type)
			}

			ExpressionExponentiation:
				return new RaoExpression("Math.pow(" + expression.left.compileExpression.value +
					", " + expression.right.compileExpression.value + ")", "Double")

			ExpressionNegate:
				return new RaoExpression("!" + expression.exp.compileExpression.value, "Boolean")

			ExpressionInvert:
			{
				val originalExpression = expression.exp.compileExpression
				return new RaoExpression("-" + originalExpression.value, originalExpression.type)
			}

			ExpressionMultiplication:
			{
				val left = expression.left.compileExpression
				val right = expression.right.compileExpression
				return RaoExpression.getType(left, right, ExpressionOperation.multiplication)
			}

			ExpressionDivision:
			{
				val left = expression.left.compileExpression
				val right = expression.right.compileExpression
				return RaoExpression.getType(left, right, ExpressionOperation.division)
			}

			ExpressionModulo:
			{
				val left = expression.left.compileExpression
				val right = expression.right.compileExpression
				return RaoExpression.getType(left, right, ExpressionOperation.modulo)
			}

			ExpressionPlus:
			{
				val left = expression.left.compileExpression
				val right = expression.right.compileExpression
				return RaoExpression.getType(left, right, ExpressionOperation.plus)
			}

			ExpressionMinus:
			{
				val left = expression.left.compileExpression
				val right = expression.right.compileExpression
				return RaoExpression.getType(left, right, ExpressionOperation.minus)
			}

			ExpressionLarger:
				return new RaoExpression(expression.left.compileExpression.value +
					" > " + expression.right.compileExpression.value, "Boolean")

			ExpressionLarger_Equal:
				return new RaoExpression(expression.left.compileExpression.value +
					" >= " + expression.right.compileExpression.value, "Boolean")

			ExpressionSmaller:
				return new RaoExpression(expression.left.compileExpression.value +
					" < " + expression.right.compileExpression.value, "Boolean")

			ExpressionSmaller_Equal:
				return new RaoExpression(expression.left.compileExpression.value +
					" <= " + expression.right.compileExpression.value, "Boolean")

			ExpressionEqual:
			{
				val left = expression.left.compileExpression
				val right = expression.right.compileExpression

				if (left.type == "unknown" && checkValidEnumID(right.type, left.value))
					left.value = compileEnumValue(right.type, left.value)

				if (right.type == "unknown" && checkValidEnumID(left.type, right.value))
					right.value = compileEnumValue(left.type, right.value)

				return new RaoExpression(left.value +
					" == " + right.value, "Boolean")
			}

			ExpressionNot_Equal:
			{
				val left = expression.left.compileExpression
				val right = expression.right.compileExpression

				if (left.type == "unknown" && checkValidEnumID(right.type, left.value))
					left.value = compileEnumValue(right.type, left.value)

				if (right.type == "unknown" && checkValidEnumID(left.type, right.value))
					right.value = compileEnumValue(left.type, right.value)

				return new RaoExpression(left.value +
					" != " + right.value, "Boolean")
			}
			ExpressionAnd:
				return new RaoExpression(expression.left.compileExpression.value +
					" && " + expression.right.compileExpression.value, "Boolean")

			ExpressionOr:
				return new RaoExpression(expression.left.compileExpression.value +
					" || " + expression.right.compileExpression.value, "Boolean")

			ExpressionAssignment:
			{
				val left = expression.left.compileExpression
				val next = expression.next.compileExpression

				if (next.type == "unknown" && checkValidEnumID(left.type, next.value))
					next.value = compileEnumValue(left.type, next.value)

				var value = next.value
				if (left.type.isStandardCompiledType)
					value = left.type + ".valueOf(" + value + ")"

				if (expression.left instanceof VariableIncDecExpression)
				{
					val idex = expression.left as VariableIncDecExpression
					if (left.value.contains(".get_") && left.value.endsWith("()"))
					{
						var operation = (if (idex.operation.length > 1) idex.operation.substring(0, 1) else null)

						if (operation != null) {
							value = left.value + " " + operation + " " + value
						}

						return new RaoExpression(cutLastChars(left.value, 2).replace(".get_", ".set_")
								+ "(" + value + ")", left.type)
					}
				}

				return new RaoExpression(left.value +
					" " + expression.left.operation + " " + next.value, left.type)
			}

			ExpressionList:
			{
				var String list = ""
				var flag = false

				for (value : expression.values)
				{
					list = list + ( if (flag) ", " else "" ) + value.compileExpression.value
					flag = true
				}
				return new RaoExpression(list, "List")
			}

			ResourceExpressionList:
			{
				val parent = expression.eContainer
				val parameters = switch parent
				{
					ResourceCreateStatement:
						parent.type.parameters

					PlanStatement:
						parent.event.parameters
				}

				var String list = ""
				var flag = false

				for (resourceParameterExpression : expression.expressions)
				{
					if (resourceParameterExpression instanceof RaoDefaultParameter)
						list = list + ( if (flag) ", " else "" ) + "null"
					else
					{
						val parameterNumber = expression.expressions.indexOf(resourceParameterExpression)
						val parameter = if (parameters != null && parameters.size > parameterNumber)
							parameters.get(parameterNumber) else null
						val compiled = resourceParameterExpression.compileExpression

						if (parameter != null && compiled.type == "unknown"
								&& checkValidEnumID(parameter.compileType, compiled.value))
							compiled.value = parameter.compileType + "."
									+ compiled.value.substring(compiled.value.lastIndexOf('.') + 1)

						if (parameter != null && parameter.type.isStandardType)
							compiled.value = parameter.compileType + ".valueOf(" + compiled.value + ")"

						list = list + ( if (flag) ", " else "" ) + compiled.value
					}
					flag = true
				}
				return new RaoExpression(list, "List")
			}

			DecisionPointActivity:
			{
				val pattern = expression.pattern
				val patternParameters = pattern.parameters
				val expressionParameters = expression.parameters

				var String list = compileActivityParameters(patternParameters, expressionParameters)
				return new RaoExpression(list, "List")
			}

			DecisionPointSearchActivity:
			{
				val pattern = expression.pattern
				val patternParameters = pattern.parameters
				val expressionParameters = expression.parameters

				var String list = compileActivityParameters(patternParameters, expressionParameters)
				return new RaoExpression(list, "List")
			}

			default:
				return new RaoExpression("VAL", "unknown")
		}
	}

	def private static compileActivityParameters(
			EList<Parameter> patternParameters,
			EList<Expression> arguments
	)
	{
		var String list = ""
		var flag = false

		for (activityParameter : arguments)
		{
			val parameterNumber = arguments.indexOf(activityParameter)
			val compiled = activityParameter.compileExpression
			val parameter = if (patternParameters != null && patternParameters.size > parameterNumber)
					patternParameters.get(parameterNumber) else null

			if (parameter != null && compiled.type == "unknown"
					&& checkValidEnumID(patternParameters.get(parameterNumber).compileType, compiled.value)) {
				compiled.value = compileEnumValue(
					patternParameters.get(parameterNumber).compileType,
					compiled.value)
			}

			if (parameter != null && parameter.type.isStandardType)
				compiled.value = parameter.compileType + ".valueOf(" + compiled.value + ")"

			list = list + ( if (flag) ", " else "" ) + compiled.value
			flag = true
		}

		return list
	}

	def private static LocalContext.ContextEntry lookupLocal(VariableMethodCallExpression expression)
	{
		var localCall = ""
		var flag = false

		val iter = expression.calls.iterator
		while (iter.hasNext)
		{
			localCall = localCall + (if (flag) "." else "") + iter.next.call
			flag = true
			if (localContext.findEntry(localCall) != null && !iter.hasNext)
				return localContext.findEntry(localCall)
		}

		return null
	}

	def static LocalContext.ContextEntry lookupGlobal(VariableMethodCallExpression expression)
	{
		var globalCall = ""

		val iter = expression.calls.iterator

		var GlobalContext globalContext = RaoGenerator.variableIndex.get(expression.calls.get(0).call)

		if (globalContext == null)
		{
			globalContext = RaoGenerator.variableIndex.get(expression.modelRoot.nameGeneric)
			globalCall = globalCall +  expression.modelRoot.nameGeneric
		}
		else
			globalCall = globalCall + iter.next.call

		var next = (if (iter.hasNext) iter.next else null)

		if (next == null)
			return null

		if (globalContext.resources.get(next.call) != null)
		{
			val resourceType = globalContext.resources.get(next.call).type
			globalCall = globalCall + "." + resourceType + ".getResource(\"" + globalCall + "." + next.call + "\")"
			next = (if (iter.hasNext) iter.next else null)
			if (next == null)
				return null
			if (globalContext.resourceTypes.get(resourceType).parameters.get(next.call) != null && !iter.hasNext)
			{
				globalCall = globalCall + ".get_"+ next.call +"()"
				return new LocalContext.ContextEntry(globalCall, globalContext.resourceTypes.get(resourceType).parameters.get(next.call))
			}
		}

		if (globalContext.sequences.get(next.call) != null && iter.hasNext && next.args == null)
		{
			val getNextMethod = iter.next
			globalCall = globalCall + "." + next.call + "." + getNextMethod.call + "("
			if (getNextMethod.args != null)
				globalCall = globalCall + getNextMethod.args.compileExpression.value
			return new LocalContext.ContextEntry(globalCall + ")", globalContext.sequences.get(next.call).type)
		}

		if (globalContext.constants.get(next.call) != null && !iter.hasNext)
		{
			return new LocalContext.ContextEntry(globalCall + "." + next.call + ".value", globalContext.constants.get(next.call).type)
		}

		if (globalContext.functions.get(next.call) != null && !iter.hasNext)
		{
			globalCall = globalCall + "." + next.call + ".evaluate("

			val function = globalContext.functions.get(next.call).origin
			val params = function.type.parameters

			if (next.args != null && params != null && params.size == next.args.values.size)
			{
				var flag = false
				var i = 0
				for (value : next.args.values)
				{
					val paramType = params.get(i).type
					globalCall = globalCall + (if (flag) ", " else "") +
						if (paramType.compileType.endsWith("_enum"))
							value.compileExpressionContext((new LocalContext(localContext)).
								populateWithEnums(params.get(i).type as RaoEnum)).value
						else if (paramType.isStandardType)
							paramType.compileType + ".valueOf(" + value.compileExpression.value + ")"
						else
							value.compileExpression.value
					i = i + 1
					flag = true
				}

			}
			return new LocalContext.ContextEntry(globalCall + ")", globalContext.functions.get(next.call).type)
		}

		return null
	}

	def static boolean isStandardType(EObject type)
	{
		switch type
		{
			RaoInt,
			RaoDouble,
			RaoString,
			RaoBoolean:
				return true
			default:
				return false
		}
	}

	def static boolean isStandardCompiledType(String type)
	{
		switch type
		{
			case "Integer",
			case "Double",
			case "Boolean",
			case "String":
				return true
			default:
				return false
		}
	}

	def static String compileType(EObject type)
	{
		switch type
		{
			Parameter: type.type.compileType
			Constant: type.type.compileType

			RaoInt: "Integer"
			RaoDouble: "Double"
			RaoBoolean: "Boolean"
			RaoString: "String"
			RaoArray: "java.util.ArrayList<" + type.arrayType.compileType + ">"
			RaoEnum: type.getFullEnumName

			default: "/* ERROR UNKNOWN TYPE */"
		}
	}

	def static String compileTypePrimitive(EObject type)
	{
		switch type
		{
			Parameter: type.type.compileTypePrimitive
			Constant: type.type.compileTypePrimitive

			RaoInt: "int"
			RaoDouble: "double"
			RaoBoolean: "boolean"
			RaoString: "String"
			RaoArray: "java.util.ArrayList<" + type.arrayType.compileType + ">"
			RaoEnum: type.getFullEnumName

			default: "/* ERROR UNKNOWN TYPE */"
		}
	}

	def static RaoType resolveAllArrays(EObject type)
	{
		switch type
		{
			Parameter: type.type.resolveAllTypes.resolveAllArrays
			Constant: type.type.resolveAllTypes.resolveAllArrays

			RaoInt,
			RaoDouble,
			RaoBoolean,
			RaoString,
			RaoEnum: type
			RaoArray: type.arrayType.resolveAllArrays

			default: null
		}
	}

	def static RaoType resolveAllTypes(EObject type)
	{
		switch type
		{
			Parameter: type.type.resolveAllTypes
			Constant: type.type.resolveAllTypes

			RaoInt,
			RaoDouble,
			RaoBoolean,
			RaoString,
			RaoArray,
			RaoEnum: type

			default: null
		}
	}

	def static String compileAllDefault(int count)
	{
		var String list = ""
		var flag = false

		for (i : 0 ..< count)
		{
			list = list + ( if (flag) ", " else "" ) + "null"
			flag = true
		}
		return list
	}

	def static String cutLastChars(String string, int numberOfCharactersToCut)
	{
		return string.substring(0, string.length - numberOfCharactersToCut)
	}
}
