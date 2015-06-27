package ru.bmstu.rk9.rdo.generator

import org.eclipse.emf.ecore.EObject

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*
import static extension ru.bmstu.rk9.rdo.compilers.RDOEnumCompiler.*

import ru.bmstu.rk9.rdo.rdo.RDODefaultParameter
import ru.bmstu.rk9.rdo.rdo.ParameterTypeBasic
import ru.bmstu.rk9.rdo.rdo.ParameterTypeString
import ru.bmstu.rk9.rdo.rdo.ParameterTypeArray

import ru.bmstu.rk9.rdo.rdo.ResourceCreateStatement
import ru.bmstu.rk9.rdo.rdo.ResourceExpressionList

import ru.bmstu.rk9.rdo.rdo.Constant

import ru.bmstu.rk9.rdo.rdo.DecisionPointActivity
import ru.bmstu.rk9.rdo.rdo.DecisionPointSearchActivity

import ru.bmstu.rk9.rdo.rdo.RDOInt
import ru.bmstu.rk9.rdo.rdo.RDODouble
import ru.bmstu.rk9.rdo.rdo.RDOBoolean
import ru.bmstu.rk9.rdo.rdo.RDOArray
import ru.bmstu.rk9.rdo.rdo.RDOString

import ru.bmstu.rk9.rdo.rdo.ExpressionList
import ru.bmstu.rk9.rdo.rdo.ExpressionExponentiation
import ru.bmstu.rk9.rdo.rdo.ExpressionNegate
import ru.bmstu.rk9.rdo.rdo.ExpressionMultiplication
import ru.bmstu.rk9.rdo.rdo.ExpressionDivision
import ru.bmstu.rk9.rdo.rdo.ExpressionModulo
import ru.bmstu.rk9.rdo.rdo.ExpressionInvert
import ru.bmstu.rk9.rdo.rdo.ExpressionPlus
import ru.bmstu.rk9.rdo.rdo.ExpressionMinus
import ru.bmstu.rk9.rdo.rdo.ExpressionLarger
import ru.bmstu.rk9.rdo.rdo.ExpressionLarger_Equal
import ru.bmstu.rk9.rdo.rdo.ExpressionSmaller
import ru.bmstu.rk9.rdo.rdo.ExpressionSmaller_Equal
import ru.bmstu.rk9.rdo.rdo.ExpressionEqual
import ru.bmstu.rk9.rdo.rdo.ExpressionNot_Equal
import ru.bmstu.rk9.rdo.rdo.ExpressionAnd
import ru.bmstu.rk9.rdo.rdo.ExpressionOr
import ru.bmstu.rk9.rdo.rdo.Primary
import ru.bmstu.rk9.rdo.rdo.IntConstant
import ru.bmstu.rk9.rdo.rdo.DoubleConstant
import ru.bmstu.rk9.rdo.rdo.StringConstant
import ru.bmstu.rk9.rdo.rdo.BoolConstant
import ru.bmstu.rk9.rdo.rdo.GroupExpression
import ru.bmstu.rk9.rdo.rdo.SelectExpression
import ru.bmstu.rk9.rdo.rdo.VariableIncDecExpression
import ru.bmstu.rk9.rdo.rdo.VariableMethodCallExpression
import ru.bmstu.rk9.rdo.rdo.VariableExpression
import ru.bmstu.rk9.rdo.rdo.ExpressionAssignment

import ru.bmstu.rk9.rdo.rdo.PlanStatement

import ru.bmstu.rk9.rdo.rdo.TimeNow

import ru.bmstu.rk9.rdo.rdo.RDOType
import ru.bmstu.rk9.rdo.rdo.RDOEnum

enum ExpressionOperation
{
	plus, minus, multiplication, division, modulo
}

class RDOExpression
{
	public String value
	public String type

	new(String value, String type)
	{
		this.value = value
		this.type = type
	}

	def static RDOExpression getType(RDOExpression left, RDOExpression right, ExpressionOperation operation)
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
				return new RDOExpression(left.value + " / " + right.value, type)

			case minus:
				return new RDOExpression(left.value + " - " + right.value, type)

			case modulo:
				return new RDOExpression(left.value + " % " + right.value, type)

			case multiplication:
				return new RDOExpression(left.value + " * " + right.value, left.type)

			case plus:
				return new RDOExpression(left.value + " + " + right.value, left.type)
		}
	}
}

class RDOExpressionCompiler
{
	private static LocalContext localContext

	def static RDOExpression compileExpressionContext(EObject expression, LocalContext context)
	{
		val backupContext = localContext
		localContext = context
		val ret = expression.compileExpression
		localContext = backupContext

		return ret
	}

	def static RDOExpression compileExpression(EObject expression)
	{
		switch expression
		{
			IntConstant:
				return new RDOExpression(expression.value.toString, "Integer")

			DoubleConstant:
				return new RDOExpression(expression.value.toString, "Double")

			TimeNow:
				return new RDOExpression("Simulator.getTime()", "Double")

			StringConstant:
				return new RDOExpression('"' + expression.value + '"', "String")

			BoolConstant:
				return new RDOExpression(expression.value.toString, "Boolean")

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
				return new RDOExpression(ret, "Boolean")
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
				return new RDOExpression(ret, if (expression.method.literal == "Size") "Integer" else "Boolean")
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
						return new RDOExpression(ret.value.replace(".get_", ".set_").cutLastChars(2) +
							"(" + ret.value + (if (incrementDecrement == "--") "- 1" else " + 1") + ")", ret.type)
				}

				return new RDOExpression((if (expression.pre != null) expression.pre else "") +
					 ret.value + (if (expression.post != null) expression.post else ""), ret.type)
			}

			VariableMethodCallExpression:
			{
				if (localContext != null)
				{
					var localCall = expression.lookupLocal
					if (localCall != null)
						return new RDOExpression(localCall.generated, localCall.type)
				}

				var globalCall = expression.lookupGlobal

				if (globalCall != null)
					return new RDOExpression(globalCall.generated, globalCall.type)

				var methodCall = ""
				var flag = false

				for (call : expression.calls)
				{
					methodCall = methodCall + (if (flag) "." else "") + call.compileExpression.value
					flag = true
				}

				return new RDOExpression(methodCall, "unknown")
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

				return new RDOExpression(call, "unknown")
			}

			Primary:
			{
				val ret = expression.exp.compileExpression
				return new RDOExpression("(" + ret.value + ")", ret.type)
			}

			ExpressionExponentiation:
				return new RDOExpression("Math.pow(" + expression.left.compileExpression.value +
					", " + expression.right.compileExpression.value + ")", "Double")

			ExpressionNegate:
				return new RDOExpression("!" + expression.exp.compileExpression.value, "Boolean")

			ExpressionInvert:
			{
				val originalExpression = expression.exp.compileExpression
				return new RDOExpression("-" + originalExpression.value, originalExpression.type)
			}

			ExpressionMultiplication:
			{
				val left = expression.left.compileExpression
				val right = expression.right.compileExpression
				return RDOExpression.getType(left, right, ExpressionOperation.multiplication)
			}

			ExpressionDivision:
			{
				val left = expression.left.compileExpression
				val right = expression.right.compileExpression
				return RDOExpression.getType(left, right, ExpressionOperation.division)
			}

			ExpressionModulo:
			{
				val left = expression.left.compileExpression
				val right = expression.right.compileExpression
				return RDOExpression.getType(left, right, ExpressionOperation.modulo)
			}

			ExpressionPlus:
			{
				val left = expression.left.compileExpression
				val right = expression.right.compileExpression
				return RDOExpression.getType(left, right, ExpressionOperation.plus)
			}

			ExpressionMinus:
			{
				val left = expression.left.compileExpression
				val right = expression.right.compileExpression
				return RDOExpression.getType(left, right, ExpressionOperation.minus)
			}

			ExpressionLarger:
				return new RDOExpression(expression.left.compileExpression.value +
					" > " + expression.right.compileExpression.value, "Boolean")

			ExpressionLarger_Equal:
				return new RDOExpression(expression.left.compileExpression.value +
					" >= " + expression.right.compileExpression.value, "Boolean")

			ExpressionSmaller:
				return new RDOExpression(expression.left.compileExpression.value +
					" < " + expression.right.compileExpression.value, "Boolean")

			ExpressionSmaller_Equal:
				return new RDOExpression(expression.left.compileExpression.value +
					" <= " + expression.right.compileExpression.value, "Boolean")

			ExpressionEqual:
			{
				val left = expression.left.compileExpression
				val right = expression.right.compileExpression

				if (left.type == "unknown" && checkValidEnumID(right.type, left.value))
					left.value = compileEnumValue(right.type, left.value)

				if (right.type == "unknown" && checkValidEnumID(left.type, right.value))
					right.value = compileEnumValue(left.type, right.value)

				return new RDOExpression(left.value +
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

				return new RDOExpression(left.value +
					" != " + right.value, "Boolean")
			}
			ExpressionAnd:
				return new RDOExpression(expression.left.compileExpression.value +
					" && " + expression.right.compileExpression.value, "Boolean")

			ExpressionOr:
				return new RDOExpression(expression.left.compileExpression.value +
					" || " + expression.right.compileExpression.value, "Boolean")

			ExpressionAssignment:
			{
				val left = expression.left.compileExpression
				val next = expression.next.compileExpression

				if (next.type == "unknown" && checkValidEnumID(left.type, next.value))
					next.value = compileEnumValue(left.type, next.value)

				if (expression.left instanceof VariableIncDecExpression)
				{
					val idex = expression.left as VariableIncDecExpression
					if (left.value.contains(".get_") && left.value.endsWith("()"))
					{
						var operation = (if (idex.operation.length > 1) idex.operation.substring(0, 1) else null)
						return new RDOExpression(cutLastChars(left.value, 2).replace(".get_", ".set_") + "("
							+ (if (operation != null) left.value + " " + operation + " " else "") + next.value + ")", left.type)
					}
				}

				return new RDOExpression(left.value +
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
				return new RDOExpression(list, "List")
			}

			ResourceExpressionList:
			{
				val parent = expression.eContainer
				val parameters = switch parent
				{
					ResourceCreateStatement:
						parent.type.parameters.map[parameter | parameter.compileType]

					PlanStatement:
						parent.event.parameters.map[parameter | parameter.compileType]
				}

				var String list = ""
				var flag = false

				for (resourceParameterExpression : expression.expressions)
				{
					if (resourceParameterExpression instanceof RDODefaultParameter)
						list = list + ( if (flag) ", " else "" ) + "null"
					else
					{
						val compiled = resourceParameterExpression.compileExpression

						if (parameters != null && compiled.type == "unknown"
								&& parameters.size > expression.expressions.indexOf(resourceParameterExpression)
								&& checkValidEnumID(
									parameters.get(expression.expressions.indexOf(resourceParameterExpression)), compiled.value))
							compiled.value = parameters.get(expression.expressions.indexOf(resourceParameterExpression)) + "."
									 + compiled.value.substring(compiled.value.lastIndexOf('.') + 1)

						list = list + ( if (flag) ", " else "" ) + compiled.value
					}
					flag = true
				}
				return new RDOExpression(list, "List")
			}

			DecisionPointActivity:
			{
				val pattern = expression.pattern
				val parameters = pattern.parameters.map[parameter | parameter.compileType]

				var String list = ""
				var flag = false

				for (activityParameter : expression.parameters)
				{
					val compiled = activityParameter.compileExpression
					if (parameters != null && compiled.type == "unknown"
							&& parameters.size > expression.parameters.indexOf(activityParameter)
							&& checkValidEnumID(
								parameters.get(expression.parameters.indexOf(activityParameter)), compiled.value
							)) {
						compiled.value = compileEnumValue(
							parameters.get(expression.parameters.indexOf(activityParameter)),
							compiled.value)
					}

					list = list + ( if (flag) ", " else "" ) + compiled.value
					flag = true
				}
				return new RDOExpression(list, "List")
			}

			DecisionPointSearchActivity:
			{
				val parameters = expression.pattern.parameters.map[p | p.compileType]

				var String list = ""
				var flag = false

				for (activityParameter : expression.parameters)
				{
					val compiled = activityParameter.compileExpression

					if (parameters != null && compiled.type == "unknown"
							&& parameters.size > expression.parameters.indexOf(activityParameter)
							&& checkValidEnumID(
								parameters.get(expression.parameters.indexOf(activityParameter)), compiled.value
							)) {
						compiled.value = compileEnumValue(
							parameters.get(expression.parameters.indexOf(activityParameter)),
							compiled.value)
					}

					list = list + ( if (flag) ", " else "" ) + compiled.value
					flag = true
				}
				return new RDOExpression(list, "List")
			}

			default:
				return new RDOExpression("VAL", "unknown")
		}
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

		var GlobalContext globalContext = RDOGenerator.variableIndex.get(expression.calls.get(0).call)

		if (globalContext == null)
		{
			globalContext = RDOGenerator.variableIndex.get(expression.modelRoot.nameGeneric)
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
					globalCall = globalCall + (if (flag) ", " else "") +
						if (params.get(i).type.compileType.endsWith("_enum"))
							value.compileExpressionContext((new LocalContext(localContext)).
								populateWithEnums(params.get(i).type as RDOEnum)).value
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

	def static String compileType(EObject type)
	{
		switch type
		{
			ParameterTypeBasic: type.type.compileType
			ParameterTypeString: type.type.compileType
			ParameterTypeArray: type.type.compileType

			Constant: type.type.compileType

			RDOInt:
//					if (type.range != null)
//						"RDORangedInteger"
//					else
						"Integer"

			RDODouble:
//					if (type.range != null)
//						"RDORangedDouble"
//					else
						"Double"

			RDOBoolean: "Boolean"
			RDOString: "String"
			RDOArray: "java.util.ArrayList<" + type.arrayType.compileType + ">"
			RDOEnum: type.getFullEnumName

			default: "Integer /* TYPE IS ACTUALLY UNKNOWN */"
		}
	}

	def static String compileTypePrimitive(EObject type)
	{
		switch type
		{
			ParameterTypeBasic : type.type.compileTypePrimitive
			ParameterTypeString: type.type.compileTypePrimitive
			ParameterTypeArray : type.type.compileTypePrimitive

			Constant: type.type.compileTypePrimitive

			RDOInt: "int"
			RDODouble: "double"
			RDOBoolean: "boolean"
			RDOString: "String"
			RDOArray: "java.util.ArrayList<" + type.arrayType.compileType + ">"
			RDOEnum: type.getFullEnumName

			default: "int /* TYPE IS ACTUALLY UNKNOWN */"
		}
	}

	def static RDOType resolveAllArrays(EObject type)
	{
		switch type
		{
			ParameterTypeBasic: type.type
			ParameterTypeString: type.type
			ParameterTypeArray: type.type.resolveAllArrays

			Constant: type.type.resolveAllTypes.resolveAllArrays

			RDOInt,
			RDODouble,
			RDOBoolean,
			RDOString,
			RDOEnum: type
			RDOArray: type.arrayType.resolveAllArrays

			default: null
		}
	}

	def static RDOType resolveAllTypes(EObject type)
	{
		switch type
		{
			ParameterTypeBasic: type.type
			ParameterTypeString: type.type
			ParameterTypeArray: type.type
			Constant: type.type.resolveAllTypes

			RDOInt,
			RDODouble,
			RDOBoolean,
			RDOString,
			RDOArray,
			RDOEnum: type

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
