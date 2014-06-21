package ru.bmstu.rk9.rdo.generator

import org.eclipse.emf.ecore.EObject

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*
import static extension ru.bmstu.rk9.rdo.generator.RDOStatementCompiler.*

import ru.bmstu.rk9.rdo.rdo.ResourceTypeParameter
import ru.bmstu.rk9.rdo.rdo.RDODefaultParameter
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterBasic
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterString
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterSuchAs
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterEnum
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterArray

import ru.bmstu.rk9.rdo.rdo.ResourceDeclaration
import ru.bmstu.rk9.rdo.rdo.ResourceExpressionList

import ru.bmstu.rk9.rdo.rdo.ConstantDeclaration

import ru.bmstu.rk9.rdo.rdo.FunctionAlgorithmic
import ru.bmstu.rk9.rdo.rdo.FunctionTable
import ru.bmstu.rk9.rdo.rdo.FunctionList

import ru.bmstu.rk9.rdo.rdo.Rule
import ru.bmstu.rk9.rdo.rdo.Operation

import ru.bmstu.rk9.rdo.rdo.DecisionPointActivity
import ru.bmstu.rk9.rdo.rdo.DecisionPointSearchActivity

import ru.bmstu.rk9.rdo.rdo.RDOInteger
import ru.bmstu.rk9.rdo.rdo.RDOReal
import ru.bmstu.rk9.rdo.rdo.RDOBoolean
import ru.bmstu.rk9.rdo.rdo.RDOSuchAs
import ru.bmstu.rk9.rdo.rdo.RDOEnum
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

import ru.bmstu.rk9.rdo.rdo.PlanningStatement

import ru.bmstu.rk9.rdo.rdo.TimeNow

import ru.bmstu.rk9.rdo.rdo.RDOType

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

	def static RDOExpression getType(RDOExpression left, RDOExpression right, ExpressionOperation op)
	{
		var type = "unknown"

		if(left.type == "error" || right.type == "error")
			type = "error"

		if(left.type == "Integer")
		{
			if(right.type == "Integer")
				type = "Integer"
			if(right.type == "Double")
				type = "Double"
		}

		if(left.type == "Double")
		{
			if(right.type == "Integer")
				type = "Double"
			if(right.type == "Double")
				type = "Double"
		}

		switch op
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

	def static RDOExpression compileExpressionContext(EObject expr, LocalContext context)
	{
		val backupContext = localContext
		localContext = context
		val ret = expr.compileExpression
		localContext = backupContext

		return ret
	}

	def static RDOExpression compileExpression(EObject expr)
	{
		switch expr
		{
			IntConstant:
				return new RDOExpression(expr.value.toString, "Integer")

			DoubleConstant:
				return new RDOExpression(expr.value.toString, "Double")

			TimeNow:
				return new RDOExpression("Simulator.getTime()", "Double")

			StringConstant:
				return new RDOExpression('"' + expr.value + '"', "String")

			BoolConstant:
				return new RDOExpression(expr.value.toString, "Boolean")

			GroupExpression:
			{
				val oldcontext = localContext
				localContext = (new LocalContext(localContext)).populateFromGroupBy(expr.arg)

				val ret =
					'''
					Select.«expr.type.literal»(
						«expr.arg.type.fullyQualifiedName».getAll(),
						new Select.Checker<«expr.arg.type.fullyQualifiedName»>()
						{
							@Override
							public boolean check(«expr.arg.type.fullyQualifiedName» current)
							{
								return «IF expr.arg.condition == null»true«ELSE»«expr.arg.condition.compileExpression.value»«ENDIF»;
							}
						}
					)'''

				localContext = oldcontext
				return new RDOExpression(ret, "Boolean")
			}

			SelectExpression:
			{
				val oldcontext = localContext
				localContext = (new LocalContext(localContext)).populateFromGroupBy(expr.arg)

				val ret =
					'''
					Select.«expr.method.literal»(
						«expr.arg.type.fullyQualifiedName».getAll(),
						new Select.Checker<«expr.arg.type.fullyQualifiedName»>()
						{
							@Override
							public boolean check(«expr.arg.type.fullyQualifiedName» current)
							{
								return «IF expr.arg.condition == null»true«ELSE»«expr.arg.condition.compileExpression.value
									»«ENDIF»«IF expr.arg2 != null» && «expr.arg2.compileExpression.value»«ENDIF»;
							}
						}
					)'''

				localContext = oldcontext
				return new RDOExpression(ret, if(expr.method.literal == "Size") "Integer" else "Boolean")
			}

			VariableIncDecExpression:
			{
				val ret = expr.^var.compileExpression
				if(expr.pre != null && ret.value.contains(".get_") && ret.value.endsWith("()") && expr.^var.lookupLocal != null)
					return new RDOExpression(ret.value.replace(".get_", ".set_").cutLastChars(2) +
						"(" + ret.value + (if(expr.pre == "--") "- 1" else " + 1") + ")", ret.type)

				if(expr.post != null && ret.value.contains(".get_") && ret.value.endsWith("()") && expr.^var.lookupLocal != null)
						return new RDOExpression(ret.value.replace(".get_", ".set_").cutLastChars(2) +
							"_after(" + ret.value + (if(expr.post == "--") " - 1" else " + 1") + ")", ret.type)

				return new RDOExpression((if (expr.pre != null) expr.pre else "") +
					 ret.value + (if (expr.post != null) expr.post else ""), ret.type)
			}

			VariableMethodCallExpression:
			{
				if(localContext != null)
				{
					var lcall = expr.lookupLocal
					if(lcall != null)
						return new RDOExpression(lcall.generated, lcall.type)
				}

				var gcall = expr.lookupGlobal
				if(gcall != null)
					return new RDOExpression(gcall.generated, gcall.type)

				var mcall = ""
				var flag = false

				for (c : expr.calls)
				{
					mcall = mcall + (if(flag) "." else "") + c.compileExpression.value
					flag = true
				}

				return new RDOExpression(mcall, "unknown")
			}

			VariableExpression:
			{
				var call = expr.call

				if (expr.functionfirst)
					call = call + "(" + (if (expr.args != null) expr.args.compileExpression.value else "") + ")"

				if (expr.arrayfirst)
					call = call + "[" + expr.iterator.compileExpression.value + "]"

				if (expr.functionlast)
					call = call + "(" + (if (expr.args != null) expr.args.compileExpression.value else "") + ")"

				if (expr.arraylast)
					call = call + "[" + expr.iterator.compileExpression.value + "]"

				return new RDOExpression(call, "unknown")
			}

			Primary:
			{
				val ret = expr.exp.compileExpression
				return new RDOExpression("(" + ret.value + ")", ret.type)
			}

			ExpressionExponentiation:
				return new RDOExpression("Math.pow(" + expr.left.compileExpression.value +
					", " + expr.right.compileExpression.value + ")", "Double")

			ExpressionNegate:
				return new RDOExpression("!" + expr.exp.compileExpression.value, "Boolean")

			ExpressionInvert:
			{
				val e = expr.exp.compileExpression
				return new RDOExpression("-" + e.value, e.type)
			}

			ExpressionMultiplication:
			{
				val left = expr.left.compileExpression
				val right = expr.right.compileExpression
				return RDOExpression.getType(left, right, ExpressionOperation.multiplication)
			}

			ExpressionDivision:
			{
				val left = expr.left.compileExpression
				val right = expr.right.compileExpression
				return RDOExpression.getType(left, right, ExpressionOperation.division)
			}

			ExpressionModulo:
			{
				val left = expr.left.compileExpression
				val right = expr.right.compileExpression
				return RDOExpression.getType(left, right, ExpressionOperation.modulo)
			}

			ExpressionPlus:
			{
				val left = expr.left.compileExpression
				val right = expr.right.compileExpression
				return RDOExpression.getType(left, right, ExpressionOperation.plus)
			}

			ExpressionMinus:
			{
				val left = expr.left.compileExpression
				val right = expr.right.compileExpression
				return RDOExpression.getType(left, right, ExpressionOperation.minus)
			}

			ExpressionLarger:
				return new RDOExpression(expr.left.compileExpression.value +
					" > " + expr.right.compileExpression.value, "Boolean")

			ExpressionLarger_Equal:
				return new RDOExpression(expr.left.compileExpression.value +
					" >= " + expr.right.compileExpression.value, "Boolean")

			ExpressionSmaller:
				return new RDOExpression(expr.left.compileExpression.value +
					" < " + expr.right.compileExpression.value, "Boolean")

			ExpressionSmaller_Equal:
				return new RDOExpression(expr.left.compileExpression.value +
					" <= " + expr.right.compileExpression.value, "Boolean")

			ExpressionEqual:
			{
				val left = expr.left.compileExpression
				val right = expr.right.compileExpression

				if(left.type == "unknown" && right.type.endsWith("_enum") && left.value.checkSingleID)
					left.value = right.type + "." + left.value

				if(right.type == "unknown" && left.type.endsWith("_enum") && right.value.checkSingleID)
					right.value = left.type + "." + right.value

				return new RDOExpression(left.value +
					" == " + right.value, "Boolean")
			}

			ExpressionNot_Equal:
			{
				val left = expr.left.compileExpression
				val right = expr.right.compileExpression

				if(left.type == "unknown" && right.type.endsWith("_enum") && left.value.checkSingleID)
					left.value = right.type + "." + left.value

				if(right.type == "unknown" && left.type.endsWith("_enum") && right.value.checkSingleID)
					right.value = left.type + "." + right.value

				return new RDOExpression(left.value +
					" != " + right.value, "Boolean")
			}
			ExpressionAnd:
				return new RDOExpression(expr.left.compileExpression.value +
					" && " + expr.right.compileExpression.value, "Boolean")

			ExpressionOr:
				return new RDOExpression(expr.left.compileExpression.value +
					" || " + expr.right.compileExpression.value, "Boolean")

			ExpressionAssignment:
			{

				val left = expr.left.compileExpression
				val next = expr.next.compileExpression

				if(next.type == "unknown" && left.type.endsWith("_enum") && next.value.checkSingleID)
					next.value = left.type + "." + next.value

				if(expr.left instanceof VariableIncDecExpression)
				{

					val idex = expr.left as VariableIncDecExpression
					var lcall = lookupLocal(idex.^var)
					if(lcall != null && lcall.generated.contains(".get_") && lcall.generated.endsWith("()"))
					{
						var op = (if(idex.op.length > 1) idex.op.substring(0, 1) else null)
						return new RDOExpression(RDOStatementCompiler.cutLastChars(lcall.generated, 2).replace(".get_", ".set_") + "("
							+ (if(op != null) lcall.generated + " " + op + " " else "") + next.value + ")", lcall.type)
					}
				}

				return new RDOExpression(left.value +
					" " + expr.left.op + " " + next.value, left.type)
			}

			ExpressionList:///!!!!!!!!
			{
				var String list = ""
				var flag = false

				for (e : expr.values)
				{
					list = list + ( if(flag) ", " else "" ) + e.compileExpression.value
					flag = true
				}
				return new RDOExpression(list, "List")
			}

			ResourceExpressionList:
			{
				val parent = expr.eContainer
				val parameters = switch parent
				{
					ResourceDeclaration:
						parent.reference.parameters.map[p | p.type.compileType]

					PlanningStatement:
						parent.event.parameters.map[p | p.type.compileType]
				}

				var String list = ""
				var flag = false

				for (e : expr.expressions)
				{
					if (e instanceof RDODefaultParameter)
						list = list + ( if(flag) ", " else "" ) + "null"
					else
					{
						val exp = e.compileExpression
						if(parameters != null && exp.type == "unknown" && exp.value.checkSingleID &&
							parameters.size > expr.expressions.indexOf(e) &&
								parameters.get(expr.expressions.indexOf(e)).endsWith("_enum"))
							exp.value = parameters.get(expr.expressions.indexOf(e)) + "." + exp.value
						list = list + ( if(flag) ", " else "" ) + exp.value
					}
					flag = true
				}
				return new RDOExpression(list, "List")
			}

			DecisionPointActivity:
			{
				val pattern = expr.pattern
				val parameters = switch pattern
				{
					Rule:
						pattern.parameters.map[p | p.type.compileType]

					Operation:
						pattern.parameters.map[p | p.type.compileType]
				}

				var String list = ""
				var flag = false

				for (e : expr.parameters)
				{
					val exp = e.compileExpression
					if(parameters != null && exp.type == "unknown" && exp.value.checkSingleID &&
						parameters.size > expr.parameters.indexOf(e) &&
							parameters.get(expr.parameters.indexOf(e)).endsWith("_enum"))
						exp.value = parameters.get(expr.parameters.indexOf(e)) + "." + exp.value
					list = list + ( if(flag) ", " else "" ) + exp.value
					flag = true
				}
				return new RDOExpression(list, "List")
			}

			DecisionPointSearchActivity:
			{
				val parameters = expr.pattern.parameters.map[p | p.type.compileType]

				var String list = ""
				var flag = false

				for (e : expr.parameters)
				{
					val exp = e.compileExpression
					if(parameters != null && exp.type == "unknown" && exp.value.checkSingleID &&
						parameters.size > expr.parameters.indexOf(e) &&
							parameters.get(expr.parameters.indexOf(e)).endsWith("_enum"))
						exp.value = parameters.get(expr.parameters.indexOf(e)) + "." + exp.value
					list = list + ( if(flag) ", " else "" ) + exp.value
					flag = true
				}
				return new RDOExpression(list, "List")
			}

			default:
				return new RDOExpression("VAL", "unknown")
		}
	}

	def static boolean checkSingleID(String s)
	{
		val sum = s.indexOf(".") + s.indexOf("(") + s.indexOf(")") + s.indexOf(" ")
		if (sum == -4)
			return true
		else
			return false
	}

	def static boolean checkPlainCall(VariableExpression expr)
	{
		return expr.arrayfirst || expr.arraylast || expr.functionfirst || expr.functionlast
	}

	def static LocalContext.ContextEntry lookupLocal(VariableMethodCallExpression expr)
	{
		var lcall = ""
		var flag = false

		val iter = expr.calls.iterator
		while(iter.hasNext)
		{
			lcall = lcall + (if(flag) "." else "") + iter.next.call
			flag = true
			if (localContext.findEntry(lcall) != null && !iter.hasNext)
				return localContext.findEntry(lcall)
		}

		return null
	}

	def static LocalContext.ContextEntry lookupGlobal(VariableMethodCallExpression expr)
	{
		var gcall = ""

		val iter = expr.calls.iterator

		var GlobalContext info = RDOGenerator.variableIndex.get(expr.calls.get(0).call)

		if(info == null)
		{
			info = RDOGenerator.variableIndex.get(expr.modelRoot.nameGeneric)
			gcall = gcall +  expr.modelRoot.nameGeneric
		}
		else
			gcall = gcall + iter.next.call

		var next = (if(iter.hasNext) iter.next else null)

		if(next == null)
			return null

		if(info.resources.get(next.call) != null)
		{
			val rtp = info.resources.get(next.call).reference
			gcall = gcall + "." + rtp + ".getResource(\"" + next.call + "\")"
			next = (if(iter.hasNext) iter.next else null)
			if(next == null)
				return null
			if(info.restypes.get(rtp).parameters.get(next.call) != null && !iter.hasNext)
			{
				gcall = gcall + ".get_"+ next.call +"()"
				return new LocalContext.ContextEntry(gcall, info.restypes.get(rtp).parameters.get(next.call))
			}
		}

		if(info.sequences.get(next.call) != null && !iter.hasNext)
		{
			gcall = gcall + "." + next.call + ".getNext("
			if(next.args != null)
				gcall = gcall + next.args.compileExpression.value
			return new LocalContext.ContextEntry(gcall + ")", info.sequences.get(next.call).type)
		}

		if(info.constants.get(next.call) != null && !iter.hasNext)
		{
			return new LocalContext.ContextEntry(gcall + "." + next.call + ".value", info.constants.get(next.call).type)
		}

		if(info.functions.get(next.call) != null && !iter.hasNext)
		{
			gcall = gcall + "." + next.call + ".evaluate("
			
			val fun = info.functions.get(next.call).origin
			val params = switch fun.type
			{
				FunctionAlgorithmic:
					(fun.type as FunctionAlgorithmic).parameters
				FunctionTable:
					(fun.type as FunctionTable).parameters
				FunctionList:
					(fun.type as FunctionList).parameters		
			}
			
			if(next.args != null && params != null && params.parameters.size == next.args.values.size)
			{
				var flag = false
				var i = 0
				for(a : next.args.values)
				{
					gcall = gcall + (if(flag) ", " else "") +
						if(params.parameters.get(i).type.compileType.endsWith("_enum"))
							a.compileExpressionContext((new LocalContext(localContext)).
								populateWithEnums(params.parameters.get(i).type.resolveAllSuchAs as RDOEnum)).value
						else
							a.compileExpression.value
					i = i + 1 
					flag = true
				}
				
			}
			return new LocalContext.ContextEntry(gcall + ")", info.functions.get(next.call).type)
		}

		return null
	}

	def static String compileType(EObject type)
	{
		switch type
		{
			ResourceTypeParameter: type.type.compileType

			RDORTPParameterBasic : type.type.compileType
			RDORTPParameterString: type.type.compileType
			RDORTPParameterSuchAs: type.type.compileType
			RDORTPParameterEnum  : type.type.compileType
			RDORTPParameterArray : type.type.compileType

			ConstantDeclaration: type.type.compileType

			RDOInteger:
//					if(type.range != null)
//						"RDORangedInteger"
//					else
						"Integer"

			RDOReal   :
//					if(type.range != null)
//						"RDORangedDouble"
//					else
						"Double"

			RDOBoolean: "Boolean"
			RDOString : "String"
			RDOEnum   : type.getEnumParentName(true) + "_enum"
			RDOSuchAs : type.type.compileType
			RDOArray  : "java.util.ArrayList<" + type.arraytype.compileType + ">"

			default: "Integer /* TYPE IS ACTUALLY UNKNOWN */"
		}
	}

	def static String compileTypePrimitive(EObject type)
	{
		switch type
		{
			ResourceTypeParameter: type.type.compileTypePrimitive

			RDORTPParameterBasic : type.type.compileTypePrimitive
			RDORTPParameterString: type.type.compileTypePrimitive
			RDORTPParameterSuchAs: type.type.compileTypePrimitive
			RDORTPParameterEnum  : type.type.compileTypePrimitive
			RDORTPParameterArray : type.type.compileTypePrimitive

			ConstantDeclaration: type.type.compileTypePrimitive

			RDOInteger: "int"
			RDOReal   : "double"
			RDOBoolean: "boolean"
			RDOString : "String"
			RDOEnum   : type.getEnumParentName(true) + "_enum"
			RDOSuchAs : type.type.compileTypePrimitive
			RDOArray  : "java.util.ArrayList<" + type.arraytype.compileType + ">"

			default: "int /* TYPE IS ACTUALLY UNKNOWN */"
		}
	}

	def static RDOType resolveAllSuchAs(EObject type)
	{
		switch type
		{
			ResourceTypeParameter: type.type.resolveAllSuchAs

			RDORTPParameterBasic : type.type
			RDORTPParameterString: type.type
			RDORTPParameterSuchAs: type.type.type.resolveAllSuchAs
			RDORTPParameterEnum  : type.type
			RDORTPParameterArray : type.type

			ConstantDeclaration: type.type.resolveAllSuchAs

			RDOInteger: type
			RDOReal   : type
			RDOBoolean: type
			RDOString : type
			RDOEnum   : type
			RDOSuchAs : type.type.resolveAllSuchAs
			RDOArray  : type

			default: null
		}

	}

	def static String compileAllDefault(int count)
	{
				var String list = ""
				var flag = false

				for (i : 0 ..< count)
				{
					list = list + ( if(flag) ", " else "" ) + "null"
					flag = true
				}
				return list
	}

}
