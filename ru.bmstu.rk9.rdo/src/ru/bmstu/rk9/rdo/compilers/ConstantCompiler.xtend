package ru.bmstu.rk9.rdo.compilers

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*
import static extension ru.bmstu.rk9.rdo.generator.RDOExpressionCompiler.*


import ru.bmstu.rk9.rdo.generator.LocalContext

import ru.bmstu.rk9.rdo.rdo.Constant

import static extension ru.bmstu.rk9.rdo.compilers.EnumCompiler.*
import ru.bmstu.rk9.rdo.rdo.RDOEnum

class ConstantCompiler
{
	def static compileConstant(Constant constant, String filename)
	{
		'''
		package «filename»;

		import ru.bmstu.rk9.rdo.lib.*;
		@SuppressWarnings("all")

		public class «constant.name»
		{
			public static final «constant.type.compileType» value = «IF constant.type.compileType.endsWith("_enum")»«
				constant.value.compileExpressionContext((new LocalContext).populateWithEnums(
					constant.type as RDOEnum)).value»«ELSE»«constant.value.compileExpression.value»«ENDIF»;
			«IF constant.type instanceof RDOEnum»

				public enum «(constant.type as RDOEnum).type.eContainer.nameGeneric
					».«(constant.type as RDOEnum).type.name».«
						(constant.type as RDOEnum).type.name»_enum
				{
					«(constant.type as RDOEnum).type.makeEnumBody»
				}«
			ENDIF»
		}
		'''
	}
}
