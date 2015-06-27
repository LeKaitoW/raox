package ru.bmstu.rk9.rdo.compilers

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*

import ru.bmstu.rk9.rdo.rdo.EnumDeclaration
import ru.bmstu.rk9.rdo.rdo.RDOEnum

class RDOEnumCompiler {
	def static compileEnum(EnumDeclaration enumDeclaration, String filename) {
		'''
			package «filename»;

			import ru.bmstu.rk9.rdo.lib.json.*;

			import ru.bmstu.rk9.rdo.lib.*;
			@SuppressWarnings("all")

			public class «enumDeclaration.name»
			{
				public enum «enumDeclaration.name»_enum
				{
					«enumDeclaration.makeEnumBody»
				}

				public final static JSONObject structure = new JSONObject()
				«enumDeclaration.compileStructure»;
			}
		'''
	}

	def private static compileStructure(EnumDeclaration enumDeclaration) {
		var enums = '''
			.put
			(
				"enums",
				new JSONArray()
		'''

		for (value : enumDeclaration.values)
			enums = enums + "\t\t.put(\"" + value + "\")\n"

		enums = enums + '''
			)
		'''

		return enums
	}

	def static getFullEnumName(RDOEnum ^enum) {
		enum.type.eContainer.nameGeneric + "." + enum.type.name + "." + enum.type.name + "_enum"
	}

	def static makeEnumBody(EnumDeclaration enumDeclaration) {
		var flag = false
		var body = ""

		for (value : enumDeclaration.values) {
			if (flag)
				body = body + ", "
			body = body + value
			flag = true
		}
		return body
	}

	def static boolean checkValidEnumID(String type, String enumID) {
		if (!type.endsWith("_enum"))
			return false
		if (enumID.indexOf("(") != -1 || enumID.indexOf(")") != -1)
			return false
		if (!enumID.contains("."))
			return false

		var typeName = type.substring(type.indexOf(".") + 1)
		typeName = typeName.substring(0, typeName.lastIndexOf("."))
		val idTypeName = enumID.substring(0, enumID.lastIndexOf("."))
		if (typeName != idTypeName)
			return false

		return true
	}

	def static compileEnumValue(String type, String enumID) {
		return type + "." + enumID.substring(enumID.lastIndexOf('.') + 1)
	}
}
