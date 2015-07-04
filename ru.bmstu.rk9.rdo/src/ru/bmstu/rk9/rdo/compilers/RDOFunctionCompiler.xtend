package ru.bmstu.rk9.rdo.compilers

import java.util.List

import static extension ru.bmstu.rk9.rdo.generator.RDOExpressionCompiler.*
import static extension ru.bmstu.rk9.rdo.generator.RDOStatementCompiler.*


import ru.bmstu.rk9.rdo.generator.LocalContext

import ru.bmstu.rk9.rdo.rdo.Function
import ru.bmstu.rk9.rdo.rdo.FunctionParameter

import ru.bmstu.rk9.rdo.rdo.FunctionAlgorithmic

import ru.bmstu.rk9.rdo.rdo.FunctionTable

import ru.bmstu.rk9.rdo.rdo.FunctionList

import ru.bmstu.rk9.rdo.rdo.RDOType
import ru.bmstu.rk9.rdo.rdo.RDOInt
import ru.bmstu.rk9.rdo.rdo.IntConstant
import ru.bmstu.rk9.rdo.rdo.DoubleConstant

import ru.bmstu.rk9.rdo.rdo.Expression
import static extension ru.bmstu.rk9.rdo.compilers.RDOEnumCompiler.*
import ru.bmstu.rk9.rdo.rdo.RDOEnum

class RDOFunctionCompiler
{
	def static compileFunction(Function function, String filename)
	{
		val type = function.type
		return
		'''
		package «filename»;

		import ru.bmstu.rk9.rdo.lib.*;
		@SuppressWarnings("all")

		public class «function.type.name»
		'''
		 +
		switch type
		{
			FunctionAlgorithmic:
			{
				var context = (new LocalContext).populateFromFunction(type)

				'''
				{
					«IF type.parameters != null»«type.parameters.compileEnumsForFunction»«ENDIF»
					public static «function.returnType.compileType» evaluate(«IF type.parameters != null
						»«type.parameters.compileFunctionTypeParameters»«ENDIF»)
					{
						if (true)
						{
							«type.algorithm.compileStatementContext(context)»
						}
						return null;
					}
				}
				'''
			}

			FunctionTable:
			'''
			{
				«IF type.parameters != null»«type.parameters.compileEnumsForFunction»«ENDIF»
				private static «function.returnType.compileType»[] values =
				{
					«type.table.compileTable(
						if (function.returnType.compileType.endsWith("_enum"))
							(new LocalContext).populateWithEnums(function.returnType as RDOEnum)
						else
							null,
						type.parameters.get(0).type.resolveAllTypes.tableLength
					)»
				};

				public static «function.returnType.compileType» evaluate(«IF type.parameters != null
						»«type.parameters.compileFunctionTypeParameters»«ENDIF»)
				{
					return values[
						«type.parameters.compileTableReturn»
					];
				}
			}
			'''
			FunctionList:
			{
				var context = (new LocalContext).populateFromFunction(type)

				var paramscontext =
					if (type.parameters != null)
						type.parameters.map
							[ parameter |
								if (parameter.type.compileType.endsWith("_enum"))
									(new LocalContext).populateWithEnums(parameter.type as RDOEnum)
								else
									null
							]
					else
						null

				'''
				{
					«IF type.parameters != null
						»«type.parameters.compileEnumsForFunction»«ENDIF»
					public static «function.returnType.compileType» evaluate(«IF type.parameters != null
							»«type.parameters.compileFunctionTypeParameters»«ENDIF»)
					{
						«IF type.parameters != null»
						«FOR i : 0 ..< type.list.size»
						«IF type.list.get(i).parameters.size == type.parameters.size»
							if
							(
								«FOR j : 0 ..< type.list.get(i).parameters.size»
									«type.parameters.get(j).name» == «
										type.list.get(i).parameters.get(j).compileExpressionContext(
											paramscontext.get(j)).value»«
										IF j < type.list.get(i).parameters.size - 1» &&«ENDIF»
								«ENDFOR»
							)
								return «type.list.get(i).value.compileExpressionContext(context).value»;

						«ENDIF»
						«ENDFOR»
						«ENDIF»
						return null;
					}
				}
				'''
			}
		}
	}

	def private static int getTableLength(RDOType type)
	{
		switch type
		{
			RDOInt:
				if (type.range != null)
					return
						if (type.range.upperBound instanceof IntConstant)
							(type.range.upperBound as IntConstant).value
						else
							((type.range.upperBound as DoubleConstant).value as int)
						-
						if (type.range.lowerBound instanceof IntConstant)
							(type.range.lowerBound as IntConstant).value
						else
							((type.range.lowerBound as DoubleConstant).value as int)
				else
					return 0

			RDOEnum:
				return type.type.values.size

			default:
				return 0
		}
	}

	def private static compileTable(List<Expression> table, LocalContext context, int cut)
	{
		var values = ""
		var flag = false
		var i = 0

		for (expression : table)
		{
			values = values + (if (flag) "," + if (i != cut) " " else "" else "") + (if (i == cut) "\n" else "") + (
				if (context != null)
					expression.compileExpressionContext(context).value
				else
					expression.compileExpression.value)
			i = i + 1
			if (i > cut)
				i = 1
			flag = true
		}

		return values
	}

	def private static compileTableReturn(List<FunctionParameter> parameters)
	{
		val list = newIntArrayOfSize(parameters.size)
		var multiplier = 1;
		for (i : 0 ..< parameters.size)
		{
			list.set(i, multiplier)
			multiplier = multiplier * parameters.get(i).type.resolveAllTypes.tableLength
		}

		var compiled = ""
		var flag = false
		for (i : 0 ..< list.size)
		{
			compiled = compiled + (if (flag) " +\n" else "") + list.get(i).toString + " * " +
				parameters.get(i).name + (if (parameters.get(i).type.compileType.endsWith("_enum")) ".ordinal()" else "")
			flag = true
		}
		return compiled
	}

	def private static compileFunctionTypeParameters(List<FunctionParameter> parameters)
	{
		'''
		«IF !parameters.empty»«parameters.get(0).type.compileType» «
			parameters.get(0).name»«
			FOR parameter : parameters.subList(1, parameters.size)», «
				parameter.type.compileType» «
				parameter.name»«
			ENDFOR»«
		ENDIF»'''
	}

	def private static compileEnumsForFunction(List<FunctionParameter> parameters)
	{
		'''
		«FOR parameter : parameters.filter[parameter | parameter.type instanceof RDOEnum]»
		public static enum «parameter.name»_enum
		{
			«(parameter.type as RDOEnum).type.makeEnumBody»
		}
		«ENDFOR»
		'''
	}
}
