package ru.bmstu.rk9.rdo.compilers

import static extension ru.bmstu.rk9.rdo.generator.RDOExpressionCompiler.*

import ru.bmstu.rk9.rdo.rdo.Results
import ru.bmstu.rk9.rdo.rdo.ResultType
import ru.bmstu.rk9.rdo.rdo.ResultGetValue
import ru.bmstu.rk9.rdo.rdo.ResultDeclaration


class RDOResultCompiler
{
	def public static compileResult(ResultDeclaration result, String filename)
	{
		'''
		package «filename»;

		import ru.bmstu.rk9.rdo.lib.*;
		@SuppressWarnings("all")

		public class «IF (result.eContainer as Results).name != null»«(result.eContainer as Results).name
			»_«ENDIF»«result.name»
		{
			private static Result result = new Result()
				{
					«result.type.compileResultBody»
				};

			public static void init()
			{
				Simulator.addResult(result);
			}
		}
		'''
	}

	def public static compileResultBody(ResultType type)
	{
		switch type
		{
			ResultGetValue:
				'''
				@Override
				public void update() {}

				@Override
				public void get()
				{
					System.out.println("«(type.eContainer as ResultDeclaration).name»\t\t|\tType: get_value\t\t|\tValue: " +
						(«type.expression.compileExpression.value»));
				}
				'''
			default:
				'''
				'''
		}
	}
}
