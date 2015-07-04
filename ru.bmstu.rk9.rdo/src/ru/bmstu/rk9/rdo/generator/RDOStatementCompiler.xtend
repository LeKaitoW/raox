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
import ru.bmstu.rk9.rdo.rdo.RelevantResource

class RDOStatementCompiler
{
	def static String compilePatternAction(StatementList statementList)
	{
		var container = statementList.eContainer.eContainer
		switch container
		{
			Pattern:
				'''
				{
					«statementList.compileStatementContext(
					(new LocalContext).populateFromPattern(container))»
				}
				'''
		}
	}

	def static String compileEventAction(StatementList statementList)
	{
		return
			'''
			{
				«statementList.compileStatementContext(
					(new LocalContext).populateFromEvent(statementList.eContainer as Event))»
			}
			'''
	}

	private static LocalContext localContext

	def static String compileStatementContext(EObject statement, LocalContext context)
	{
		localContext = context
		val ret = statement.compileStatement
		localContext = null

		return ret
	}

	def static String compileStatement(EObject statement)
	{
		switch statement
		{
			StatementList:
				'''
				«FOR nestedStatement : statement.statements»
					«nestedStatement.compileStatement»
				«ENDFOR»
				'''

			ExpressionStatement:
			{
				statement.expr.compileExpressionContext(localContext).value + ";"
			}

			NestedStatement:
			{
				val backupContext = localContext
				localContext = new LocalContext(localContext)

				val ret =
					'''
					{
						«statement.statements.compileStatement»
					}
					'''

				localContext = backupContext
				return ret
			}

			LocalVariableDeclaration:
				'''
				«statement.type.compileType» «statement.list.compileStatement»;
				'''

			VariableDeclarationList:
			{
				var flag = false
				var list = ""

				for (declaration : statement.declarations)
				{
					list = list + (if (flag) ", " else "") + declaration.name +
						(if (declaration.value != null) " = " + declaration.value.compileExpression.value else "")
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
					if («statement.condition.compileExpressionContext(localContext).value»)
					«IF !(statement.then instanceof NestedStatement)»	«ENDIF»«statement.then.compileStatement»
					«IF statement.^else != null»else
					«IF !(statement.^else instanceof NestedStatement)»	«ENDIF»«statement.^else.compileStatement»
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
						if (statement.declaration != null)
							statement.declaration.compileStatement.cutLastChars(1) + ""
						else
							if (statement.init != null)
								statement.init.compileExpression.value + ";"
							else ";"
						» «
						if (statement.condition != null)
							statement.condition.compileExpression.value
						else ""
						»; «
						if (statement.update != null)
							statement.update.compileExpression.value
						else ""
					»)«IF statement.body.empty»;«ELSE»
					«IF !(statement.body instanceof NestedStatement)»	«ENDIF»«statement.body.compileStatement»«ENDIF»
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
				val ret = (if (statement.^return != null) statement.^return.compileExpressionContext(localContext) else null)

				return
					'''
					return«IF ret != null» «ret.value»«ENDIF»;
					'''
			}

			PlanStatement:
				"Simulator.pushEvent(new " +
					statement.event.getFullyQualifiedName + "(" + RDOExpressionCompiler.compileExpression(statement.value).value +
						(if (statement.parameters != null) (", " + statement.parameters.compileExpression.value) else
							(if (statement.event.parameters != null && !statement.event.parameters.empty)
								(", " + compileAllDefault(statement.event.parameters.size)) else "")) + "));"

			ResourceCreateStatement: {
				if (localContext != null)
					localContext.addCreatedResource(statement)

				return
					'''
					«IF statement.name != null»
						«statement.type.fullyQualifiedName» «statement.name» = new «
							statement.type.fullyQualifiedName»(«if (statement.parameters != null)
								statement.parameters.compileExpression.value else ""»);
						«statement.name».register();
						Simulator.getDatabase().memorizeResourceEntry(
								«statement.name».copy(),
								Database.ResourceEntryType.CREATED);

					«ELSE»
						Simulator.getDatabase().memorizeResourceEntry(
								new «statement.type.fullyQualifiedName»(«if (statement.parameters != null)
									statement.parameters.compileExpression.value else ""»).register().copy(),
								Database.ResourceEntryType.CREATED);
					«ENDIF»
					'''
			}

			ResourceEraseStatement:
			{
				var ResourceType type
				var String resourceReference
				if (statement.res instanceof RelevantResource) {
					type = ((statement.res as RelevantResource).type) as ResourceType
					resourceReference = "resources." + statement.res.name
				}

				if (statement.res instanceof ResourceCreateStatement) {
					type = ((statement.res as ResourceCreateStatement).type) as ResourceType
					resourceReference = statement.res.name
				}

				'''
				Simulator.getDatabase().memorizeResourceEntry(
						«resourceReference»,
						Database.ResourceEntryType.ERASED);

				«type.fullyQualifiedName
					».eraseResource(«resourceReference»);

				'''
			}

			FrameObject:
				switch statement
				{
					FrameObjectText:
						'''
						context.drawText
						(
							«statement.x.compileExpression.value», «statement.y.compileExpression.value»,
							«statement.width.compileExpression.value», «statement.height.compileExpression.value»,
							«statement.backColour.compileFrameColour»,
							«statement.textcolour.compileFrameColour»,
							AnimationContext.Alignment.«IF statement.alignment != null
								»«statement.alignment.getName»«ELSE»LEFT«ENDIF»,
							«statement.text.compileExpression.value»
						);
						'''
					FrameObjectRectangle:
						'''
						context.drawRectangle
						(
							«statement.x.compileExpression.value», «statement.y.compileExpression.value»,
							«statement.width.compileExpression.value», «statement.height.compileExpression.value»,
							«statement.backColour.compileFrameColour»,
							«statement.borderColour.compileFrameColour»
						);
						'''
					FrameObjectLine:
						'''
						context.drawLine
						(
							«statement.x1.compileExpression.value», «statement.y1.compileExpression.value»,
							«statement.x2.compileExpression.value», «statement.x2.compileExpression.value»,
							«statement.colour.compileFrameColour»
						);
						'''
					FrameObjectCircle:
						'''
						context.drawCircle
						(
							«statement.x.compileExpression.value», «statement.y.compileExpression.value»,
							«statement.radius.compileExpression.value»,
							«statement.backColour.compileFrameColour»,
							«statement.borderColour.compileFrameColour»
						);
						'''
					FrameObjectEllipse:
						'''
						context.drawEllipse
						(
							«statement.x.compileExpression.value», «statement.y.compileExpression.value»,
							«statement.width.compileExpression.value», «statement.height.compileExpression.value»,
							«statement.backColour.compileFrameColour»,
							«statement.borderColour.compileFrameColour»
						);
						'''
					FrameObjectTriangle:
						'''
						context.drawRectangle
						(
							«statement.x1.compileExpression.value», «statement.y1.compileExpression.value»,
							«statement.x2.compileExpression.value», «statement.y2.compileExpression.value»,
							«statement.x3.compileExpression.value», «statement.y3.compileExpression.value»,
							«statement.backColour.compileFrameColour»,
							«statement.borderColour.compileFrameColour»
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
