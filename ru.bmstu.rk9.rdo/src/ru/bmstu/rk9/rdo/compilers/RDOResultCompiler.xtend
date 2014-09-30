package ru.bmstu.rk9.rdo.compilers

import static extension ru.bmstu.rk9.rdo.compilers.Util.*

import static extension ru.bmstu.rk9.rdo.generator.RDOExpressionCompiler.*

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*

import ru.bmstu.rk9.rdo.rdo.Results
import ru.bmstu.rk9.rdo.rdo.ResultDeclaration

import ru.bmstu.rk9.rdo.rdo.ResultType

import ru.bmstu.rk9.rdo.rdo.ResultGetValue
import ru.bmstu.rk9.rdo.rdo.ResultWatchParameter
import ru.bmstu.rk9.rdo.rdo.ResultWatchQuant
import ru.bmstu.rk9.rdo.rdo.ResultWatchState
import ru.bmstu.rk9.rdo.rdo.ResultWatchValue

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

		import java.nio.ByteBuffer;

		import ru.bmstu.rk9.rdo.lib.*;
		@SuppressWarnings("all")

		public class «name» implements Result
		{
			private static «name» INSTANCE = new «name»();

			public static void init()
			{
				Simulator.addResult(INSTANCE);
				Simulator.getDatabase().registerResult(INSTANCE);
			}

			@Override
			public String getName()
			{
				return "«result.fullyQualifiedName»";
			}

			@Override
			public Result.Type getType()
			{
				return «result.type.compileTypeEnum»;
			}

			«result.type.compileResultBody»
		}
		'''
	}

	def private static String compileTypeEnum(ResultType type)
	{
		switch(type)
		{
			ResultGetValue:       "Result.Type.GET_VALUE"
			ResultWatchParameter: "Result.Type.WATCH_PAR"
			ResultWatchQuant:     "Result.Type.WATCH_QUANT"
			ResultWatchState:     "Result.Type.WATCH_STATE"
			ResultWatchValue:     "Result.Type.WATCH_VALUE"
		}
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

				@Override
				public ByteBuffer serialize()
				{
					return null;
				}
				'''
			ResultWatchParameter:
				'''
				@Override
				public void update()
				{
					Simulator.getDatabase().addResultEntry(this);
				}

				@Override
				public String get()
				{
					return "";
				}

				@Override
				public ByteBuffer serialize()
				{
					ByteBuffer data = ByteBuffer.allocate(«type.parameter.compileExpression.type.getTypeSize("value")»);

					return data;
				}
				'''
		}
	}
}
