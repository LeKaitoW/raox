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
		val name =
			(if ((result.eContainer as Results).name != null)
				(result.eContainer as Results).name + "_" else "")
			+ result.name
		'''
		package «filename»;

		import ru.bmstu.rk9.rdo.lib.*;
		@SuppressWarnings("all")

		public class «name» implements Result
		{
			«result.type.compileResultBody»

			private static «name» INSTANCE = new «name»();

			public static void init()
			{
				Simulator.addResult(INSTANCE);
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
				public String get()
				{
					return "«(type.eContainer as ResultDeclaration).name»\t\t|\tType: get_value\t\t|\tValue: " +
						String.valueOf(«type.expression.compileExpression.value»);
				}
				'''
			default:
				'''
				@Override
				public void update() {}

				@Override
				public String get()
				{
					return "";
				}
				'''
		}
	}
}
