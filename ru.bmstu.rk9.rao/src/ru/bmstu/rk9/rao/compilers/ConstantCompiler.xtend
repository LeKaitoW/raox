package ru.bmstu.rk9.rao.compilers

import static extension ru.bmstu.rk9.rao.generator.RaoNaming.*
import static extension ru.bmstu.rk9.rao.generator.RaoExpressionCompiler.*


import ru.bmstu.rk9.rao.generator.LocalContext

import ru.bmstu.rk9.rao.rao.Constant

import static extension ru.bmstu.rk9.rao.compilers.EnumCompiler.*
import ru.bmstu.rk9.rao.rao.RaoEnum

class ConstantCompiler
{
	def static compileConstant(Constant constant, String filename)
	{
		'''
		package «filename»;

		«Util.putImports»

		public class «constant.name»
		{
			public static final «constant.type.compileType» value = «IF constant.type.compileType.endsWith("_enum")»«
				constant.value.compileExpressionContext((new LocalContext).populateWithEnums(
					constant.type as RaoEnum)).value»«ELSE»«constant.value.compileExpression.value»«ENDIF»;
			«IF constant.type instanceof RaoEnum»

				public enum «(constant.type as RaoEnum).type.eContainer.nameGeneric
					».«(constant.type as RaoEnum).type.name».«
						(constant.type as RaoEnum).type.name»_enum
				{
					«(constant.type as RaoEnum).type.makeEnumBody»
				}«
			ENDIF»
		}
		'''
	}
}
