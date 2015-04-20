package ru.bmstu.rk9.rdo.compilers

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*

import ru.bmstu.rk9.rdo.rdo.EnumDeclaration
import ru.bmstu.rk9.rdo.rdo.RDOEnum

class RDOEnumCompiler
{
	def public static compileEnum(EnumDeclaration e, String filename)
	{
		'''
		package «filename»;

		import ru.bmstu.rk9.rdo.lib.json.*;

		import ru.bmstu.rk9.rdo.lib.*;
		@SuppressWarnings("all")

		public class «e.name»
		{
			public enum «e.name»_enum
			{
				«e.makeEnumBody»
			}

			public final static JSONObject structure = new JSONObject()
				«e.compileStructure»;
		}
		'''
	}

	def private static compileStructure(EnumDeclaration e)
	{
		var enums =
			'''
			.put
			(
				"enums",
				new JSONArray()
			'''

		for(v : e.values)
			enums = enums + "\t\t.put(\"" + v.name + "\")\n"

		enums = enums +
			'''
			)
			'''

		return enums
	}

	def public static getFullEnumName(RDOEnum e)
	{
		e.id.eContainer.nameGeneric + "."
				+ e.id.name + "." + e.id.name + "_enum"
	}

	def public static makeEnumBody(EnumDeclaration e)
	{
		var flag = false
		var body = ""

		for(i : e.values)
		{
			if(flag)
				body = body + ", "
			body = body + i.name
			flag = true
		}
		return body
	}
}
