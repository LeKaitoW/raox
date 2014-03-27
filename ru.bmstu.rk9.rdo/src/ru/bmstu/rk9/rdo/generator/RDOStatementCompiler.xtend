package ru.bmstu.rk9.rdo.generator

import org.eclipse.emf.ecore.EObject

import ru.bmstu.rk9.rdo.rdo.StatementList
import ru.bmstu.rk9.rdo.rdo.NestedStatement
import ru.bmstu.rk9.rdo.rdo.PlanningStatement
import ru.bmstu.rk9.rdo.rdo.ExpressionStatement
import ru.bmstu.rk9.rdo.rdo.EventConvert
import ru.bmstu.rk9.rdo.rdo.ResourceDeclaration
import java.util.List
import java.util.ArrayList
import ru.bmstu.rk9.rdo.rdo.ResourceType
import ru.bmstu.rk9.rdo.rdo.VariableMethodCallExpression
import ru.bmstu.rk9.rdo.rdo.IfStatement

import static extension ru.bmstu.rk9.rdo.generator.RDOExpressionCompiler.*
import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*

class RDOStatementCompiler
{	
	def static String compileStatement(EObject st)
	{
		switch st
		{
			//==== Context-dependent statements ====
			EventConvert:
			{
				var List<String> paramlist = new ArrayList<String>

				switch st.relres.type
				{
					ResourceDeclaration:
					{
						for (p : (st.relres.type as ResourceDeclaration).reference.parameters)
							paramlist.add(p.name)

						for (e : st.statements.eAllContents.toIterable.filter(typeof(VariableMethodCallExpression)))
							for (c : e.calls)
							{
								var repl = (st.relres.type as ResourceDeclaration).reference.fullyQualifiedName +
									'.getResource("' +	st.relres.type.name + '")'
								
								if(c.call == st.relres.name && e.calls.size == 2)
									c.setCall(repl)

								if (paramlist.contains(c.call) && e.calls.size == 1)
									c.setCall(repl + '.' + c.call)
							}
						return
							st.statements.compileStatement
					}
					ResourceType:
					{
						return
							'''
							«FOR p : (st.relres.type as ResourceType).parameters»
							«RDOExpressionCompiler.compileType(p.type)» «p.name»«RDOGenerator.getDefault(p.type)»;
							«ENDFOR»
							«st.statements.compileStatement»
							/*«RDONaming.getFullyQualifiedName(st.relres.type)».addResource(new «RDONaming.getFullyQualifiedName(st.relres.type)»(
								«var flag = false»«FOR p : (st.relres.type as ResourceType).parameters»
									«IF flag», «ENDIF»«flag = true»«p.name»
								«ENDFOR»
							));*/
							'''
					}
				}
			}	
			//======================================
			
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
			
			IfStatement:
				'''
				if(«st.condition.compileExpression»)
					«st.then.compileStatement»
				«IF st.^else != null»else
					«st.^else.compileStatement»
				«ENDIF»
				'''
			
			PlanningStatement:
				"rdo_lib.Simulator.pushEvent(new " +
					RDONaming.getFullyQualifiedName(st.event) +	"(/* PARAMETERS */), " +
						RDOExpressionCompiler.compileExpression(st.value) + ");"
		}
	}
	
}