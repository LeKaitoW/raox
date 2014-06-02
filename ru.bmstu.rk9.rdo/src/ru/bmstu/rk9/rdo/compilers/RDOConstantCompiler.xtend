package ru.bmstu.rk9.rdo.compilers

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*
import static extension ru.bmstu.rk9.rdo.generator.RDOExpressionCompiler.*

import static extension ru.bmstu.rk9.rdo.compilers.RDOEnumCompiler.*

import ru.bmstu.rk9.rdo.generator.LocalContext

import ru.bmstu.rk9.rdo.rdo.ConstantDeclaration

import ru.bmstu.rk9.rdo.rdo.RDOEnum


class RDOConstantCompiler
{
	def public static compileConstant(ConstantDeclaration con, String filename)
	{
		'''
		package «filename»;

		public class «con.name»
		{
			public static final «con.type.compileType» value = «IF con.type.compileType.endsWith("_enum")»«
				con.value.compileExpressionContext((new LocalContext).populateWithEnums(
					con.type.resolveAllSuchAs as RDOEnum)).value»«ELSE»«con.value.compileExpression.value»«ENDIF»;
			«IF con.type instanceof RDOEnum»

				public enum «(con.type as RDOEnum).getEnumParentName(false)»_enum
				{
					«(con.type as RDOEnum).makeEnumBody»
				}«
			ENDIF»
		}
		'''
	}
	
}