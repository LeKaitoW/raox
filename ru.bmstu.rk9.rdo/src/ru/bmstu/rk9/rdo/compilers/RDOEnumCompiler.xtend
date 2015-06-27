package ru.bmstu.rk9.rdo.compilers

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*

import ru.bmstu.rk9.rdo.rdo.EnumDeclaration
import ru.bmstu.rk9.rdo.rdo.RDOEnum

class RDOEnumCompiler
{
	def static compileEnum(EnumDeclaration e, String filename)
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
			enums = enums + "\t\t.put(\"" + v + "\")\n"

		enums = enums +
			'''
			)
			'''

		return enums
	}

	def static getFullEnumName(RDOEnum e)
	{
		e.type.eContainer.nameGeneric + "."
				+ e.type.name + "." + e.type.name + "_enum"
	}

	def static makeEnumBody(EnumDeclaration e)
	{
		var flag = false
		var body = ""

		for(i : e.values)
		{
			if(flag)
				body = body + ", "
			body = body + i
			flag = true
		}
		return body
	}

	def static boolean checkValidEnumID(String type, String id)
	{
		if (!type.endsWith("_enum"))
			return false
		if (id.indexOf("(") != -1 || id.indexOf(")") != -1)
			return false
		if (!id.contains("."))
			return false

		var typeName = type.substring(type.indexOf(".") + 1)
		typeName = typeName.substring(0, typeName.lastIndexOf("."))
		val idTypeName = id.substring(0, id.lastIndexOf("."))
		if (typeName != idTypeName)
			return false

		return true
	}

	def static compileEnumValue(String type, String id)
	{
		return type + "." + id.substring(id.lastIndexOf('.') + 1)
	}
}
