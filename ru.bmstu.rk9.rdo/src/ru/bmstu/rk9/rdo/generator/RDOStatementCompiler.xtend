package ru.bmstu.rk9.rdo.generator

import org.eclipse.emf.ecore.EObject

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*
import static extension ru.bmstu.rk9.rdo.generator.RDOExpressionCompiler.*

import ru.bmstu.rk9.rdo.rdo.Operation
import ru.bmstu.rk9.rdo.rdo.OperationConvert
import ru.bmstu.rk9.rdo.rdo.Rule
import ru.bmstu.rk9.rdo.rdo.RuleConvert
import ru.bmstu.rk9.rdo.rdo.Event
import ru.bmstu.rk9.rdo.rdo.EventConvert

import ru.bmstu.rk9.rdo.rdo.TerminateIf

import ru.bmstu.rk9.rdo.rdo.StatementList
import ru.bmstu.rk9.rdo.rdo.ExpressionStatement
import ru.bmstu.rk9.rdo.rdo.NestedStatement
import ru.bmstu.rk9.rdo.rdo.LocalVariableDeclaration
import ru.bmstu.rk9.rdo.rdo.VariableDeclarationList
import ru.bmstu.rk9.rdo.rdo.IfStatement
import ru.bmstu.rk9.rdo.rdo.ForStatement
import ru.bmstu.rk9.rdo.rdo.BreakStatement
import ru.bmstu.rk9.rdo.rdo.ReturnStatement
import ru.bmstu.rk9.rdo.rdo.PlanningStatement
import ru.bmstu.rk9.rdo.rdo.LegacySetStatement


class RDOStatementCompiler
{
	def static String compileConvert(EObject st, int parameter)
	{
		switch st
		{
			EventConvert:
					'''
					// «st.relres.name» convert event
					{
						«st.statements.compileStatementContext(
							(new LocalContext).populateFromEvent(st.eContainer as Event).tuneForConvert(st.relres.name))»
					}
					'''

			RuleConvert:
					'''
					// «st.relres.name» convert rule
					{
						«st.statements.compileStatementContext(
							(new LocalContext).populateFromRule(st.eContainer as Rule).tuneForConvert(st.relres.name))»
					}
					'''

			OperationConvert:
				if (parameter == 0)
						'''
						// «st.relres.name» convert begin
						{
							«st.beginstatements.compileStatementContext(
							(new LocalContext).populateFromOperation(st.eContainer as Operation).tuneForConvert(st.relres.name))»
						}
						'''
				else
						'''
						// «st.relres.name» convert end
						{
							«st.endstatements.compileStatementContext(
							(new LocalContext).populateFromOperation(st.eContainer as Operation).tuneForConvert(st.relres.name))»
						}
						'''
		}
	}

	private static LocalContext localContext

	def static String compileStatementContext(EObject st, LocalContext context)
	{
		localContext = context
		val ret = st.compileStatement
		localContext = null

		return ret
	}

	def static String compileStatement(EObject st)
	{
		switch st
		{
			StatementList:
				'''
				«FOR s : st.statements»
				«s.compileStatement»
				«ENDFOR»
				'''

			ExpressionStatement:
			{
				st.expr.compileExpressionContext(localContext).value + ";"
			}

			NestedStatement:
			{
				val backupContext = localContext
				localContext = new LocalContext(localContext)

				val ret =
					'''
					{
						«st.statements.compileStatement»
					}
					'''

				localContext = backupContext
				return ret
			}

			LocalVariableDeclaration:
				'''
				«st.type.compileType» «st.list.compileStatement»;
				'''

			VariableDeclarationList:
			{
				var flag = false
				var list = ""

				for (d : st.declarations)
				{
					list = list + (if (flag) ", " else "") + d.name +
						(if (d.value != null) " = " + d.value.compileExpression.value else "")
					flag = true
				}

				return list
			}

			IfStatement:
			{
				val backupContext = localContext
				localContext = new LocalContext(localContext)

				val ret =
					'''
					if(«st.condition.compileExpressionContext(localContext).value»)
					«IF !(st.then instanceof NestedStatement)»	«ENDIF»«st.then.compileStatement»
					«IF st.^else != null»else
					«IF !(st.^else instanceof NestedStatement)»	«ENDIF»«st.^else.compileStatement»
					«ENDIF»
					'''

				localContext = backupContext
				return ret
			}

			ForStatement:
			{
				val backupContext = localContext
				localContext = new LocalContext(localContext)

				val ret =
					'''
					for («
						if (st.declaration != null)
							st.declaration.compileStatement.cutLastChars(1) + ""
						else
							if (st.init != null)
								st.init.compileExpression.value + ";"
							else ";"
						» «
						if (st.condition != null)
							st.condition.compileExpression.value
						else ""
						»; «
						if (st.update != null)
							st.update.compileExpression.value
						else ""
						»)
					«IF !(st.body instanceof NestedStatement)»	«ENDIF»«st.body.compileStatement»
					'''

				localContext = backupContext
				return ret
			}

			BreakStatement:
				'''
				break;
				'''

			ReturnStatement:
			{
				val ret = (if(st.^return != null) st.^return.compileExpressionContext(localContext) else null)

				return
					'''
					return«IF ret != null» «ret.value»«ENDIF»;
					'''
			}

			PlanningStatement:
				"Simulator.pushEvent(new " +
					st.event.getFullyQualifiedName + "(" + RDOExpressionCompiler.compileExpression(st.value).value +
						(if (st.parameters != null) (", " + st.parameters.compileExpression.value) else
							(if(st.event.parameters != null && st.event.parameters.size > 0)
								(", " + compileAllDefault(st.event.parameters.size)) else "")) + "));"

			LegacySetStatement:
				'''
				«st.call» = («st.value.compileExpression.value»);
				'''

			TerminateIf:
				'''
				Simulator.addTerminateCondition
				(
					new TerminateCondition()
					{
						@Override
						public boolean check()
						{
							return «st.condition.compileExpression.value»;
						}
					}
				);
				'''
		}
	}

	def static String cutLastChars(String s, int c)
	{
		return s.substring(0, s.length - c)
	}
}
