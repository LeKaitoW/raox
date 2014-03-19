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

class RDOExpressionCompiler
{
	private static boolean collectA = false
	private static boolean collectL = false
	
	def static String compileExpression(Expression expr)
	{
		switch expr
		{
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