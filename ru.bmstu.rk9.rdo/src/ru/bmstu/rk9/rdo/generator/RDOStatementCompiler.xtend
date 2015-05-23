package ru.bmstu.rk9.rdo.generator

import org.eclipse.emf.ecore.EObject

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*
import static extension ru.bmstu.rk9.rdo.generator.RDOExpressionCompiler.*

import ru.bmstu.rk9.rdo.rdo.Event

import ru.bmstu.rk9.rdo.rdo.StatementList
import ru.bmstu.rk9.rdo.rdo.ExpressionStatement
import ru.bmstu.rk9.rdo.rdo.NestedStatement
import ru.bmstu.rk9.rdo.rdo.LocalVariableDeclaration
import ru.bmstu.rk9.rdo.rdo.VariableDeclarationList
import ru.bmstu.rk9.rdo.rdo.IfStatement
import ru.bmstu.rk9.rdo.rdo.ForStatement
import ru.bmstu.rk9.rdo.rdo.BreakStatement
import ru.bmstu.rk9.rdo.rdo.ReturnStatement
import ru.bmstu.rk9.rdo.rdo.PlanStatement

import ru.bmstu.rk9.rdo.rdo.FrameObject
import ru.bmstu.rk9.rdo.rdo.FrameObjectText
import ru.bmstu.rk9.rdo.rdo.FrameObjectRectangle
import ru.bmstu.rk9.rdo.rdo.FrameObjectLine
import ru.bmstu.rk9.rdo.rdo.FrameObjectCircle
import ru.bmstu.rk9.rdo.rdo.FrameObjectEllipse
import ru.bmstu.rk9.rdo.rdo.FrameObjectTriangle
import ru.bmstu.rk9.rdo.rdo.FrameColour
import ru.bmstu.rk9.rdo.rdo.ResourceCreateStatement
import ru.bmstu.rk9.rdo.rdo.ResourceEraseStatement
import ru.bmstu.rk9.rdo.rdo.ResourceType
import ru.bmstu.rk9.rdo.rdo.Pattern

class RDOStatementCompiler
{
	def static String compilePatternAction(StatementList st)
	{
		var cont = st.eContainer.eContainer
		switch cont
		{
			Pattern:
					'''
					{
						«st.compileStatementContext(
						(new LocalContext).populateFromPattern(cont))»
					}
					'''
		}
	}

	def static String compileEventAction(StatementList st)
	{
		return
			'''
			{
				«st.compileStatementContext(
					(new LocalContext).populateFromEvent(st.eContainer as Event))»
			}
			'''
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

				for(d : st.declarations)
				{
					list = list + (if(flag) ", " else "") + d.name +
						(if(d.value != null) " = " + d.value.compileExpression.value else "")
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
					for(«
						if(st.declaration != null)
							st.declaration.compileStatement.cutLastChars(1) + ""
						else
							if(st.init != null)
								st.init.compileExpression.value + ";"
							else ";"
						» «
						if(st.condition != null)
							st.condition.compileExpression.value
						else ""
						»; «
						if(st.update != null)
							st.update.compileExpression.value
						else ""
					»)«IF st.body.empty»;«ELSE»
					«IF !(st.body instanceof NestedStatement)»	«ENDIF»«st.body.compileStatement»«ENDIF»
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

			PlanStatement:
				"Simulator.pushEvent(new " +
					st.event.getFullyQualifiedName + "(" + RDOExpressionCompiler.compileExpression(st.value).value +
						(if(st.parameters != null) (", " + st.parameters.compileExpression.value) else
							(if(st.event.parameters != null && !st.event.parameters.empty)
								(", " + compileAllDefault(st.event.parameters.size)) else "")) + "));"

			ResourceCreateStatement: {
				if (localContext != null)
					localContext.addCreatedResource(st)

				return
					'''
					«IF st.name != null»
						«st.reference.fullyQualifiedName» «st.name» = new «
							st.reference.fullyQualifiedName»(«if(st.parameters != null)
								st.parameters.compileExpression.value else ""»);
						«st.name».register();
					«ELSE»
						new «st.reference.fullyQualifiedName»(«if(st.parameters != null)
								st.parameters.compileExpression.value else ""»).register();
					«ENDIF»
					'''
			}

			ResourceEraseStatement:
				'''
					«(st.relres.type as ResourceType).fullyQualifiedName
						».eraseResource(resources.«st.relres.name»);
				'''

			FrameObject:
				switch st
				{
					FrameObjectText:
						'''
						context.drawText
						(
							«st.x.compileExpression.value», «st.y.compileExpression.value»,
							«st.width.compileExpression.value», «st.height.compileExpression.value»,
							«st.backcolour.compileFrameColour»,
							«st.textcolour.compileFrameColour»,
							AnimationContext.Alignment.«IF st.alignment != null
								»«st.alignment.getName»«ELSE»LEFT«ENDIF»,
							«st.text.compileExpression.value»
						);
						'''
					FrameObjectRectangle:
						'''
						context.drawRectangle
						(
							«st.x.compileExpression.value», «st.y.compileExpression.value»,
							«st.width.compileExpression.value», «st.height.compileExpression.value»,
							«st.backcolour.compileFrameColour»,
							«st.bordercolour.compileFrameColour»
						);
						'''
					FrameObjectLine:
						'''
						context.drawLine
						(
							«st.x1.compileExpression.value», «st.y1.compileExpression.value»,
							«st.x2.compileExpression.value», «st.x2.compileExpression.value»,
							«st.colour.compileFrameColour»
						);
						'''
					FrameObjectCircle:
						'''
						context.drawCircle
						(
							«st.x.compileExpression.value», «st.y.compileExpression.value»,
							«st.radius.compileExpression.value»,
							«st.backcolour.compileFrameColour»,
							«st.bordercolour.compileFrameColour»
						);
						'''
					FrameObjectEllipse:
						'''
						context.drawEllipse
						(
							«st.x.compileExpression.value», «st.y.compileExpression.value»,
							«st.width.compileExpression.value», «st.height.compileExpression.value»,
							«st.backcolour.compileFrameColour»,
							«st.bordercolour.compileFrameColour»
						);
						'''
					FrameObjectTriangle:
						'''
						context.drawRectangle
						(
							«st.x1.compileExpression.value», «st.y1.compileExpression.value»,
							«st.x2.compileExpression.value», «st.y2.compileExpression.value»,
							«st.x3.compileExpression.value», «st.y3.compileExpression.value»,
							«st.backcolour.compileFrameColour»,
							«st.bordercolour.compileFrameColour»
						);
						'''
				}
		}
	}

	def static String compileFrameColour(FrameColour colour)
	{
		'''new int[] {«colour.r», «colour.g
			», «colour.b», «255 - colour.alpha»}'''
	}
}
