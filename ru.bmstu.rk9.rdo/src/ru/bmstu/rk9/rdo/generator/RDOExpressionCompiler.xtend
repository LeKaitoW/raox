package ru.bmstu.rk9.rdo.generator

import ru.bmstu.rk9.rdo.rdo.Expression
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
import ru.bmstu.rk9.rdo.rdo.ExpressionAssignment
import ru.bmstu.rk9.rdo.rdo.ExpressionList
import ru.bmstu.rk9.rdo.rdo.Primary
import ru.bmstu.rk9.rdo.rdo.IntConstant
import ru.bmstu.rk9.rdo.rdo.DoubleConstant
import ru.bmstu.rk9.rdo.rdo.StringConstant
import ru.bmstu.rk9.rdo.rdo.BoolConstant
import ru.bmstu.rk9.rdo.rdo.GroupExpression
import ru.bmstu.rk9.rdo.rdo.ArrayValues
import ru.bmstu.rk9.rdo.rdo.VariableIncDecExpression
import org.eclipse.emf.ecore.EObject
import ru.bmstu.rk9.rdo.rdo.VariableMethodCallExpression
import ru.bmstu.rk9.rdo.rdo.VariableExpression

class RDOExpressionCompiler
{
	private static boolean collectA = false
	private static boolean collectL = false
	
	def static String compileExpression(EObject expr)
	{
		switch expr
		{
			IntConstant:
				return expr.value.toString
				
			DoubleConstant:
				return expr.value.toString
				
			StringConstant:
				return '"' + expr.value + '"'
				
			BoolConstant:
				return expr.value.toString

			GroupExpression:
				return "groupby"
				
			ArrayValues:
			{
				collectL = true
				var array = "[ "
				array = array + expr.values.compileExpression
				array = array + " ]"
				collectL = false
				return array
			}
			
			VariableIncDecExpression:
				return if (expr.pre != null) expr.pre else "" +
					expr.^var.compileExpression + if (expr.post != null) expr.post else ""
			
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
				return expr.call +
					( if(expr.isfunction) "(" + expr.args.compileExpression     + ")" else "" ) +
					( if(expr.isarray)    "[" + expr.iterator.compileExpression + "]" else "")

			Primary:
				return "( " + expr.exp.compileExpression + " )"
			
			ExpressionExponentiation:
				return "Math.pow(" + expr.left.compileExpression +
				             ", " + expr.right.compileExpression + ")"
			
			ExpressionNegate:
				return "!( " + expr.exp.compileExpression + " )"
				
			ExpressionInvert:
				return "-( " + expr.exp.compileExpression + " )"
				
			ExpressionMultiplication:
				return "( " + expr.left.compileExpression +
				     " * " + expr.right.compileExpression + " )"
			
			ExpressionDivision:
				return "( " + expr.left.compileExpression +
				     " / " + expr.right.compileExpression + " )"
					
			ExpressionModulo:
				return "( " + expr.left.compileExpression +
				     " % " + expr.right.compileExpression + " )"
					
			ExpressionPlus:
				return "( " + expr.left.compileExpression +
				     " + " + expr.right.compileExpression + " )"
					
			ExpressionMinus:
				return "( " + expr.left.compileExpression +
				     " - " + expr.right.compileExpression + " )"
					
			ExpressionLarger:
				return "( " + expr.left.compileExpression +
				     " > " + expr.right.compileExpression + " )"
					
			ExpressionLarger_Equal:
				return "( " + expr.left.compileExpression +
				    " >= " + expr.right.compileExpression + " )"
					
			ExpressionSmaller:
				return "( " + expr.left.compileExpression +
				     " < " + expr.right.compileExpression + " )"
					
			ExpressionSmaller_Equal:
				return "( " + expr.left.compileExpression +
				    " <= " + expr.right.compileExpression + " )"
					
			ExpressionEqual:
				return "( " + expr.left.compileExpression +
				    " == " + expr.right.compileExpression + " )"
					
			ExpressionNot_Equal:
				return "( " + expr.left.compileExpression +
				    " != " + expr.right.compileExpression + " )"
					
			ExpressionAnd:
				return "( " + expr.left.compileExpression +
				    " && " + expr.right.compileExpression + " )"
					
			ExpressionOr:
				return "( " + expr.left.compileExpression +
				    " || " + expr.right.compileExpression + " )"
					
			ExpressionAssignment:
			{
				var String assgn = ""

				var boolean donottouchA = false

				if (collectA == false)
				{
					collectA = true
					assgn = assgn + "( "
				}
				else donottouchA = true
				
				assgn = assgn + expr.left.compileExpression +
				      " " + expr.left.op + " " + expr.next.compileExpression

				if (collectA == true && !donottouchA)
				{
					collectA = false
					assgn = assgn + " )"
				}
				
				return assgn
			}		
			ExpressionList:
			{
				var String list = "" 
				
				var boolean donottouchL = false
				
				if (collectL == false)
				{
					collectL = true
					list = list + "("
				}
				else donottouchL = true
				
				list = list + expr.left.compileExpression +
				       ", " + expr.next.compileExpression
				
				if (collectL == true && !donottouchL)
				{
					collectL = false
					list = list + ")"
				}
				
				return list
			}
			default:
				return "VAL"
		}
	}

}