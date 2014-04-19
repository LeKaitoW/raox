package ru.bmstu.rk9.rdo.generator

import java.util.List
import java.util.ArrayList

import org.eclipse.emf.ecore.EObject

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*
import static extension ru.bmstu.rk9.rdo.generator.RDOExpressionCompiler.*

import ru.bmstu.rk9.rdo.rdo.ResourceType

import ru.bmstu.rk9.rdo.rdo.ResourceDeclaration

import ru.bmstu.rk9.rdo.rdo.OperationConvert
import ru.bmstu.rk9.rdo.rdo.RuleConvert
import ru.bmstu.rk9.rdo.rdo.EventConvert

import ru.bmstu.rk9.rdo.rdo.TerminateIf

import ru.bmstu.rk9.rdo.rdo.VariableMethodCallExpression

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
			{
				var List<String> paramlist = new ArrayList<String>

				switch st.relres.type
				{
					ResourceDeclaration:
						for (p : (st.relres.type as ResourceDeclaration).reference.parameters)
							paramlist.add(p.name)
						
					ResourceType:
						for (p : (st.relres.type as ResourceType).parameters)
							paramlist.add(p.name)
				}

				for (e : st.statements.eAllContents.toIterable.filter(typeof(VariableMethodCallExpression)))
					for (c : e.calls)
						if (paramlist.contains(c.call) && e.calls.size == 1)
							c.setCall('resources.' + st.relres.name + '.' + c.call)

				for (e : st.statements.eAllContents.toIterable.filter(typeof(LegacySetStatement)))
					if (paramlist.contains(e.call))
						e.setCall('resources.' + st.relres.name + '.' + e.call)
					else if (e.call.startsWith(st.relres.name + "."))
						e.setCall('resources.' + e.call)

				return
					'''
					// «st.relres.name» convert event
					{
						«st.statements.compileStatement»
					}
					'''
			}

			RuleConvert:
			{
				var List<String> paramlist = new ArrayList<String>

				switch st.relres.type
				{
					ResourceDeclaration:
						for (p : (st.relres.type as ResourceDeclaration).reference.parameters)
							paramlist.add(p.name)
						
					ResourceType:
						for (p : (st.relres.type as ResourceType).parameters)
							paramlist.add(p.name)
				}

				for (e : st.statements.eAllContents.toIterable.filter(typeof(VariableMethodCallExpression)))
					for (c : e.calls)
						if (paramlist.contains(c.call) && e.calls.size == 1)
							c.setCall('resources.' + st.relres.name + '.' + c.call)

				for (e : st.statements.eAllContents.toIterable.filter(typeof(LegacySetStatement)))
					if (paramlist.contains(e.call))
						e.setCall('resources.' + st.relres.name + '.' + e.call)
					else if (e.call.startsWith(st.relres.name + "."))
						e.setCall('resources.' + e.call)

				return
					'''
					// «st.relres.name» convert rule
					{
						«st.statements.compileStatement»
					}
					'''
			}

			OperationConvert:
			{
				var List<String> paramlist = new ArrayList<String>

				switch st.relres.type
				{
					ResourceDeclaration:
						for (p : (st.relres.type as ResourceDeclaration).reference.parameters)
							paramlist.add(p.name)

					ResourceType:
						for (p : (st.relres.type as ResourceType).parameters)
							paramlist.add(p.name)
				}

				if (parameter == 0)
				{
					for (e : st.beginstatements.eAllContents.toIterable.filter(typeof(VariableMethodCallExpression)))
					for (c : e.calls)
						if (paramlist.contains(c.call) && e.calls.size == 1)
							c.setCall('resources.' + st.relres.name + '.' + c.call)

					for (e : st.beginstatements.eAllContents.toIterable.filter(typeof(LegacySetStatement)))
						if (paramlist.contains(e.call))
							e.setCall('resources.' + st.relres.name + '.' + e.call)
						else if (e.call.startsWith(st.relres.name + "."))
							e.setCall('resources.' + e.call)

					return
						'''
						// «st.relres.name» convert begin
						{
							«st.beginstatements.compileStatement»
						}
						'''
				}
				else
				{
					for (e : st.endstatements.eAllContents.toIterable.filter(typeof(VariableMethodCallExpression)))
					for (c : e.calls)
						if (paramlist.contains(c.call) && e.calls.size == 1)
							c.setCall('resources.' + st.relres.name + '.' + c.call)

					for (e : st.endstatements.eAllContents.toIterable.filter(typeof(LegacySetStatement)))
						if (paramlist.contains(e.call))
							e.setCall('resources.' + st.relres.name + '.' + e.call)
						else if (e.call.startsWith(st.relres.name + "."))
							e.setCall('resources.' + e.call)

					return
						'''
						// «st.relres.name» convert end
						{
							«st.endstatements.compileStatement»
						}
						'''
				}
			}
		}
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
				RDOExpressionCompiler.compileExpression(st.expr) + ";"

			NestedStatement:
				'''
				{
					«st.statements.compileStatement»
				}
				'''

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
						(if (d.value != null) " = " + d.value.compileExpression else "")
					flag = true
				}

				return list
			}

			IfStatement:
				'''
				if(«st.condition.compileExpression»)
				«IF !(st.then instanceof NestedStatement)»	«ENDIF»«st.then.compileStatement»
				«IF st.^else != null»else
				«IF !(st.^else instanceof NestedStatement)»	«ENDIF»«st.^else.compileStatement»
				«ENDIF»
				'''

			ForStatement:
				'''
				for («
					if (st.declaration != null)
						st.declaration.compileStatement.cutLastChars(1) + ""
					else
						if (st.init != null)
							st.init.compileExpression + ";"
						else ";"
					» «
					if (st.condition != null)
						st.condition.compileExpression
					else ""
					»; «
					if (st.update != null)
						st.update.compileExpression
					else ""
					»)
				«IF !(st.body instanceof NestedStatement)»	«ENDIF»«st.body.compileStatement»
				'''

			BreakStatement:
				'''
				break;
				'''

			ReturnStatement:
				'''
				return«IF st.^return != null» «st.^return.compileExpression»«ENDIF»;
				'''

			PlanningStatement:
				"rdo_lib.Simulator.pushEvent(new " +
					st.event.getFullyQualifiedName + "(" + RDOExpressionCompiler.compileExpression(st.value) +
						(if (st.parameters != null) (", " + st.parameters.compileExpression) else
							(", " + compileAllDefault(st.event.parameters.size))) + "));"

			LegacySetStatement:
				'''
				«st.call» = («st.value.compileExpression»);
				'''

			TerminateIf:
				'''
				rdo_lib.Simulator.addTerminateCondition
				(
					new rdo_lib.TerminateCondition()
					{
						@Override
						public boolean check()
						{
							return «st.condition.compileExpression»;
						}
					}
				);
				'''
		}
	}

	def static String cutLastChars(String s, int c)
	{
		return s.substring(0, s.length - 1 - c)
	}
}
