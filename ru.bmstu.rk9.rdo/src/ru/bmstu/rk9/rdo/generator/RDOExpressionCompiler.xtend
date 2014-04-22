package ru.bmstu.rk9.rdo.generator

import java.util.List

import org.eclipse.emf.ecore.EObject

import ru.bmstu.rk9.rdo.validation.VariableInfo

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*

import ru.bmstu.rk9.rdo.rdo.ResourceTypeParameter
import ru.bmstu.rk9.rdo.rdo.RDODefaultParameter
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterBasic
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterString
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterSuchAs
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterEnum
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterArray

import ru.bmstu.rk9.rdo.rdo.ResourceExpressionList

import ru.bmstu.rk9.rdo.rdo.ConstantDeclaration

import ru.bmstu.rk9.rdo.rdo.PatternChoiceFrom
import ru.bmstu.rk9.rdo.rdo.PatternChoiceMethod
import ru.bmstu.rk9.rdo.rdo.Rule
import ru.bmstu.rk9.rdo.rdo.RuleConvert
import ru.bmstu.rk9.rdo.rdo.Operation
import ru.bmstu.rk9.rdo.rdo.OperationConvert

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
import ru.bmstu.rk9.rdo.rdo.ArrayValues
import ru.bmstu.rk9.rdo.rdo.VariableIncDecExpression
import ru.bmstu.rk9.rdo.rdo.VariableMethodCallExpression
import ru.bmstu.rk9.rdo.rdo.VariableExpression
import ru.bmstu.rk9.rdo.rdo.ExpressionAssignment

import ru.bmstu.rk9.rdo.rdo.TimeNow


class RDOExpressionCompiler
{
	private static LocalContext localContext

	def static String compileExpressionContext(EObject expr, LocalContext context)
	{
		localContext = context
		val ret = expr.compileExpression
		localContext = null

		return ret
	}

	def static String compileExpression(EObject expr)
	{
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
			{
				val oldcontext = localContext
				localContext = (new LocalContext(localContext)).populateFromGroupBy(expr.arg)

				val ret =
					'''
					rdo_lib.Select.«expr.type.literal»(
						«expr.arg.type.fullyQualifiedName».getAll(),
						new rdo_lib.Select.Checker<«expr.arg.type.fullyQualifiedName»>()
						{
							@Override
							public boolean check(«expr.arg.type.fullyQualifiedName» current)
							{
								return «IF expr.arg.condition == null»true«ELSE»«expr.arg.condition.compileExpression»«ENDIF»;
							}
						}
					)'''

				localContext = oldcontext
				return ret
			}

			SelectExpression:
			{
				val oldcontext = localContext
				localContext = (new LocalContext(localContext)).populateFromGroupBy(expr.arg)

				val ret =				
					'''
					rdo_lib.Select.«expr.method.literal»(
						«expr.arg.type.fullyQualifiedName».getAll(),
						new rdo_lib.Select.Checker<«expr.arg.type.fullyQualifiedName»>()
						{
							@Override
							public boolean check(«expr.arg.type.fullyQualifiedName» current)
							{
								return «IF expr.arg.condition == null»true«ELSE»«expr.arg.condition.compileExpression»«ENDIF»«IF expr.arg2 != null» && «expr.arg2.compileExpression»«ENDIF»;
							}
						}
					)'''

				localContext = oldcontext
				return ret
			}

			ArrayValues:
				return "[" + expr.values.compileExpression + "]"

			VariableIncDecExpression:
				return (if (expr.pre != null) expr.pre else "") +
					expr.^var.compileExpression + (if (expr.post != null) expr.post else "")

			VariableMethodCallExpression:
			{
				if(localContext != null)
				{
					var lcall = expr.lookupLocal
					if(lcall != null)
						return lcall
				}

				var gcall = expr.lookupGlobal
				if(gcall != null)
					return gcall

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

			DecisionPointSearchActivity:
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

	def static boolean checkPlainCall(VariableExpression expr)
	{
		return expr.arrayfirst || expr.arraylast || expr.functionfirst || expr.functionlast
	}

	def static String lookupLocal(VariableMethodCallExpression expr)
	{
		var lcall = ""
		var flag = false

		val iter = expr.calls.iterator 
		while(iter.hasNext)
		{
			lcall = lcall + (if(flag) "." else "") + iter.next.call
			flag = true
			if (localContext.findEntry(lcall) != null && !iter.hasNext)
				return localContext.findEntry(lcall).generated
		}

		return null
	}

	def static String lookupGlobal(VariableMethodCallExpression expr)
	{
		var gcall = ""

		val iter = expr.calls.iterator

		var VariableInfo info = RDOGenerator.variableIndex.get(expr.calls.get(0).call)

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
				return gcall
			}
		}

		if(info.sequences.get(next.call) != null && !iter.hasNext)
		{
			gcall = gcall + "." + next.call + ".getNext("
			if(next.args != null)
				gcall = gcall + next.args.compileExpression
			return gcall + ")"
		}

		if(info.constants.get(next.call) != null && !iter.hasNext)
		{
			return gcall + "." + next.call + ".value"
		}

		if(info.functions.get(next.call) != null && !iter.hasNext)
		{
			gcall = gcall + "." + next.call + ".evaluate("
			if(next.args != null)
				gcall = gcall + next.args.compileExpression
			return gcall + ")"
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
