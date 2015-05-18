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
	def static compileFunction(Function fun, String filename)
	{
		val type = fun.type
		return
		'''
		package «filename»;

		import ru.bmstu.rk9.rdo.lib.*;
		@SuppressWarnings("all")

		public class «fun.type.name»
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
					public static «fun.returntype.compileType» evaluate(«IF type.parameters != null
						»«type.parameters.compileFunctionTypeParameters»«ENDIF»)
					{
						if(true)
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
				private static «fun.returntype.compileType»[] values =
				{
					«type.table.compileTable(
						if(fun.returntype.compileType.endsWith("_enum"))
							(new LocalContext).populateWithEnums(fun.returntype as RDOEnum)
						else
							null,
						type.parameters.get(0).type.resolveAllTypes.tableLength
					)»
				};

				public static «fun.returntype.compileType» evaluate(«IF type.parameters != null
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
					if(type.parameters != null)
						type.parameters.map
							[ p |
								if(p.type.compileType.endsWith("_enum"))
									(new LocalContext).populateWithEnums(p.type as RDOEnum)
								else
									null
							]
					else
						null

				'''
				{
					«IF type.parameters != null
						»«type.parameters.compileEnumsForFunction»«ENDIF»
					public static «fun.returntype.compileType» evaluate(«IF type.parameters != null
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
				if(type.range != null)
					return
						if(type.range.hi instanceof IntConstant)
							(type.range.hi as IntConstant).value
						else
							((type.range.hi as DoubleConstant).value as int)
						-
						if(type.range.lo instanceof IntConstant)
							(type.range.lo as IntConstant).value
						else
							((type.range.lo as DoubleConstant).value as int)
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

		for(e : table)
		{
			values = values + (if(flag) "," + if(i != cut) " " else "" else "") + (if(i == cut) "\n" else "") + (
				if(context != null)
					e.compileExpressionContext(context).value
				else
					e.compileExpression.value)
			i = i + 1
			if(i > cut)
				i = 1
			flag = true
		}

		return values
	}

	def private static compileTableReturn(List<FunctionParameter> parameters)
	{
		val list = newIntArrayOfSize(parameters.size)
		var multiplier = 1;
		for(i : 0 ..< parameters.size)
		{
			list.set(i, multiplier)
			multiplier = multiplier * parameters.get(i).type.resolveAllTypes.tableLength
		}

		var compiled = ""
		var flag = false
		for(i : 0 ..< list.size)
		{
			compiled = compiled + (if(flag) " +\n" else "") + list.get(i).toString + " * " +
				parameters.get(i).name + (if(parameters.get(i).type.compileType.endsWith("_enum")) ".ordinal()" else "")
			flag = true
		}
		return compiled
	}

	def private static compileFunctionTypeParameters(List<FunctionParameter> parameters)
	{
		'''«IF !parameters.empty»«parameters.get(0).type.compileType» «
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
		«FOR p : parameters.filter[c | c.type instanceof RDOEnum]»
		public static enum «p.name»_enum
		{
			«(p.type as RDOEnum).type.makeEnumBody»
		}

		«ENDFOR»
		'''
	}
}
