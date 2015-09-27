package ru.bmstu.rk9.rao.generator

import org.eclipse.emf.ecore.EObject

import static extension ru.bmstu.rk9.rao.generator.RaoNaming.*
import static extension ru.bmstu.rk9.rao.generator.RaoExpressionCompiler.*

import ru.bmstu.rk9.rao.rao.Event

import ru.bmstu.rk9.rao.rao.StatementList
import ru.bmstu.rk9.rao.rao.ExpressionStatement
import ru.bmstu.rk9.rao.rao.NestedStatement
import ru.bmstu.rk9.rao.rao.LocalVariableDeclaration
import ru.bmstu.rk9.rao.rao.IfStatement
import ru.bmstu.rk9.rao.rao.ForStatement
import ru.bmstu.rk9.rao.rao.BreakStatement
import ru.bmstu.rk9.rao.rao.ReturnStatement
import ru.bmstu.rk9.rao.rao.PlanStatement

import ru.bmstu.rk9.rao.rao.FrameObject
import ru.bmstu.rk9.rao.rao.FrameObjectText
import ru.bmstu.rk9.rao.rao.FrameObjectRectangle
import ru.bmstu.rk9.rao.rao.FrameObjectLine
import ru.bmstu.rk9.rao.rao.FrameObjectCircle
import ru.bmstu.rk9.rao.rao.FrameObjectEllipse
import ru.bmstu.rk9.rao.rao.FrameObjectTriangle
import ru.bmstu.rk9.rao.rao.FrameColor
import ru.bmstu.rk9.rao.rao.ResourceCreateStatement
import ru.bmstu.rk9.rao.rao.ResourceEraseStatement
import ru.bmstu.rk9.rao.rao.ResourceType
import ru.bmstu.rk9.rao.rao.Pattern
import ru.bmstu.rk9.rao.rao.RelevantResource

class RaoStatementCompiler
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
			{
				var variableType = statement.type
				var flag = false
				var list = ""

				for (declaration : statement.list.declarations)
				{
					var value = ""
					if (declaration.value != null) {
						value = declaration.value.compileExpression.value
						if (variableType.isStandardType)
							value = variableType.compileType + ".valueOf(" + value + ")"
						value = " = " + value
					}

					list = list + (if (flag) ", " else "") + declaration.name + value
					flag = true
				}

				return variableType.compileType + " " + list + ";"
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
					statement.event.getFullyQualifiedName + "(" + RaoExpressionCompiler.compileExpression(statement.value).value +
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
							«statement.text.compileExpression.value»,
							(int) («statement.x.compileExpression.value»),
							(int) («statement.y.compileExpression.value»),
							«statement.textColor.compileFrameColor»
							«IF statement.area != null»,
							(int) («statement.area.width.compileExpression.value»),
							AnimationContext.Alignment.«statement.area.alignment.getName»
							«ENDIF»
						);
						'''
					FrameObjectRectangle:
						'''
						context.drawRectangle
						(
							(int) («statement.x.compileExpression.value»),
							(int) («statement.y.compileExpression.value»),
							(int) («statement.width.compileExpression.value»),
							(int) («statement.height.compileExpression.value»),
							«statement.backColor.compileFrameColor»,
							«statement.borderColor.compileFrameColor»
						);
						'''
					FrameObjectLine:
						'''
						context.drawLine
						(
							(int) («statement.x1.compileExpression.value»),
							(int) («statement.y1.compileExpression.value»),
							(int) («statement.x2.compileExpression.value»),
							(int) («statement.y2.compileExpression.value»),
							«statement.color.compileFrameColor»
						);
						'''
					FrameObjectCircle:
						'''
						context.drawCircle
						(
							(int) («statement.x.compileExpression.value»),
							(int) («statement.y.compileExpression.value»),
							(int) («statement.radius.compileExpression.value»),
							«statement.backColor.compileFrameColor»,
							«statement.borderColor.compileFrameColor»
						);
						'''
					FrameObjectEllipse:
						'''
						context.drawEllipse
						(
							(int) («statement.x.compileExpression.value»),
							(int) («statement.y.compileExpression.value»),
							(int) («statement.width.compileExpression.value»),
							(int) («statement.height.compileExpression.value»),
							«statement.backColor.compileFrameColor»,
							«statement.borderColor.compileFrameColor»
						);
						'''
					FrameObjectTriangle:
						'''
						context.drawTriangle
						(
							(int) («statement.x1.compileExpression.value»),
							(int) («statement.y1.compileExpression.value»),
							(int) («statement.x2.compileExpression.value»),
							(int) («statement.y2.compileExpression.value»),
							(int) («statement.x3.compileExpression.value»),
							(int) («statement.y3.compileExpression.value»),
							«statement.backColor.compileFrameColor»,
							«statement.borderColor.compileFrameColor»
						);
						'''
				}
		}
	}

	def static String compileFrameColor(FrameColor color)
	{
		'''new int[] {«color.r», «color.g
			», «color.b», «255 - color.alpha»}'''
	}
}
