package ru.bmstu.rk9.rdo.generator

import java.util.List

import org.eclipse.emf.ecore.EObject

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*

import ru.bmstu.rk9.rdo.rdo.ResourceTypeParameter
import ru.bmstu.rk9.rdo.rdo.RDODefaultParameter
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterBasic
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterString
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterSuchAs
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterEnum
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterArray

import ru.bmstu.rk9.rdo.rdo.ResourceExpressionList

import ru.bmstu.rk9.rdo.rdo.PatternChoiceFrom
import ru.bmstu.rk9.rdo.rdo.PatternChoiceMethod
import ru.bmstu.rk9.rdo.rdo.Rule
import ru.bmstu.rk9.rdo.rdo.RuleConvert
import ru.bmstu.rk9.rdo.rdo.Operation
import ru.bmstu.rk9.rdo.rdo.OperationConvert

import ru.bmstu.rk9.rdo.rdo.DecisionPointActivity

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
import ru.bmstu.rk9.rdo.rdo.ArrayValues
import ru.bmstu.rk9.rdo.rdo.VariableIncDecExpression
import ru.bmstu.rk9.rdo.rdo.VariableMethodCallExpression
import ru.bmstu.rk9.rdo.rdo.VariableExpression
import ru.bmstu.rk9.rdo.rdo.ExpressionAssignment

import ru.bmstu.rk9.rdo.rdo.TimeNow


class RDOExpressionCompiler
{
	def static String compileForPattern(EObject expr)
	{
		val parent = expr.eContainer
		
		var String relres
		var List<String> relreslist

		switch parent
		{
			RuleConvert:
			{
				relres = parent.relres.name
				relreslist = (parent.eContainer as Rule).relevantresources.map[r | r.name]
			}

			OperationConvert:
			{
				relres = parent.relres.name
				relreslist = (parent.eContainer as Operation).relevantresources.map[r | r.name]
			}

			Rule:
			{
				relres = "@NOPE"
				relreslist = parent.relevantresources.map[r | r.name]
			}

			Operation:
			{
				relres = "@NOPE"
				relreslist = parent.relevantresources.map[r | r.name]
			}
		}
		
		switch expr
		{
			PatternChoiceFrom:
			{
				for (e : expr.logic.eAllContents.toIterable.filter(typeof(VariableMethodCallExpression)))
					if (e.calls.size == 2 && relreslist.contains(e.calls.get(0).call) && e.calls.get(0).call != relres)
						e.calls.get(0).setCall("pattern." + e.calls.get(0).call)

				return expr.logic.compileExpression
			}

			PatternChoiceMethod:
			{
				var String sign;
				if (expr.withtype.literal == "with_min")
					sign = " < "
				else
					sign = " > "

				for (e : expr.expression.eAllContents.toIterable.filter(typeof(VariableMethodCallExpression)))
				{
					if (e.calls.size == 2 && relreslist.contains(e.calls.get(0).call) && e.calls.get(0).call != relres)
						e.calls.get(0).setCall("@PAT." + e.calls.get(0).call)
					if (e.calls.size == 2 && e.calls.get(0).call == relres)
						e.calls.get(0).setCall("@RES")
				}
				var ret = expr.expression.compileExpression

				if (parent instanceof Rule || parent instanceof Operation)
					return ret.replaceAll("@PAT", "x") + sign + ret.replaceAll("@PAT", "y")
				else
					ret = ret.replaceAll("@PAT", "pattern")

				return ret.replaceAll("@RES", "x") + sign + ret.replaceAll("@RES", "y")

			}
		}
	}

	def static String compileExpression(EObject expr)
	{
		val parent = expr.eContainer
		switch parent
		{
			Operation:
				if (parent.time == expr)
					for (e : expr.eAllContents.toIterable.filter(typeof(VariableMethodCallExpression)))
						for (c : e.calls)
							if (parent.relevantresources.map[r | r.name].contains(c.call) && e.calls.size == 2)
								c.setCall('matched.' + c.call)
		}

		switch expr
		{
			IntConstant:
				return expr.value.toString

			DoubleConstant:
				return expr.value.toString

			TimeNow:
				return "rdo_lib.Simulator.getTime()"

			StringConstant:
				return '"' + expr.value + '"'

			BoolConstant:
				return expr.value.toString

			GroupExpression:
				return "groupby"

			ArrayValues:
				return "[" + expr.values.compileExpression + "]"

			VariableIncDecExpression:
				return (if (expr.pre != null) expr.pre else "") +
					expr.^var.compileExpression + (if (expr.post != null) expr.post else "")

			VariableMethodCallExpression:
			{
				var mcall = ""
				var flag = false

				for (c : expr.calls)
				{
					mcall = mcall + (if(flag) "." else "") + c.compileExpression
					flag = true
				}

				return mcall
			}

			VariableExpression:
			{
				var call = expr.call

				if (expr.functionfirst)
					call = call + "(" + (if (expr.args != null) expr.args.compileExpression else "") + ")"

				if (expr.arrayfirst)
					call = call + "[" + expr.iterator.compileExpression + "]"

				if (expr.functionlast)
					call = call + "(" + (if (expr.args != null) expr.args.compileExpression else "") + ")"

				if (expr.arraylast)
					call = call + "[" + expr.iterator.compileExpression + "]"

				return call
			}
			Primary:
				return "(" + expr.exp.compileExpression + ")"

			ExpressionExponentiation:
				return "Math.pow(" + expr.left.compileExpression +
				             ", " + expr.right.compileExpression + ")"

			ExpressionNegate:
				return "!" + expr.exp.compileExpression

			ExpressionInvert:
				return "-" + expr.exp.compileExpression

			ExpressionMultiplication:
				return expr.left.compileExpression +
					" * " + expr.right.compileExpression

			ExpressionDivision:
				return expr.left.compileExpression +
					" / " + expr.right.compileExpression

			ExpressionModulo:
				return expr.left.compileExpression +
					" % " + expr.right.compileExpression

			ExpressionPlus:
				return expr.left.compileExpression +
					" + " + expr.right.compileExpression

			ExpressionMinus:
				return expr.left.compileExpression +
					" - " + expr.right.compileExpression

			ExpressionLarger:
				return expr.left.compileExpression +
					" > " + expr.right.compileExpression

			ExpressionLarger_Equal:
				return expr.left.compileExpression +
					" >= " + expr.right.compileExpression

			ExpressionSmaller:
				return expr.left.compileExpression +
					" < " + expr.right.compileExpression

			ExpressionSmaller_Equal:
				return expr.left.compileExpression +
					" <= " + expr.right.compileExpression

			ExpressionEqual:
				return expr.left.compileExpression +
					" == " + expr.right.compileExpression

			ExpressionNot_Equal:
				return expr.left.compileExpression +
					" != " + expr.right.compileExpression

			ExpressionAnd:
				return expr.left.compileExpression +
					" && " + expr.right.compileExpression

			ExpressionOr:
				return expr.left.compileExpression +
					" || " + expr.right.compileExpression

			ExpressionAssignment:
				return expr.left.compileExpression +
				      " " + expr.left.op + " " + expr.next.compileExpression

			ExpressionList:
			{
				var String list = ""
				var flag = false

				for (e : expr.values)
				{
					list = list + ( if(flag) ", " else "" ) + e.compileExpression
					flag = true
				}
				return list
			}

			ResourceExpressionList:
			{
				var String list = ""
				var flag = false

				for (e : expr.expressions)
				{
					list = list + ( if(flag) ", " else "" ) +
						if (e instanceof RDODefaultParameter)
							"null"
						else
							e.compileExpression
					flag = true
				}
				return list
			}
			
			DecisionPointActivity:
			{
				var String list = ""
				var flag = false

				for (e : expr.parameters)
				{
					list = list + ( if(flag) ", " else "" ) +
						e.compileExpression
					flag = true
				}
				return list
			}

			default:
				return "VAL"
		}
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

			RDOInteger: "Integer"
			RDOReal   : "Double"
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
