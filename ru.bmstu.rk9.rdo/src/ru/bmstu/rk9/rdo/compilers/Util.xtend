package ru.bmstu.rk9.rdo.compilers

class Util
{
	def static withFirstUpper(String s)
	{
		return Character.toUpperCase(s.charAt(0)) + s.substring(1)
	}

	def static String getTypeConstantSize(String type)
	{
		if(type == "Integer")
			return "4"

		if(type == "Double")
			return "8"

		if(type == "Boolean")
			return "1"

		if(type.endsWith("_enum"))
			return "2"

		return "0"
	}

	def static String backToRDOType(String type)
	{
		if(type.startsWith("java.util.ArrayList"))
			"array<" + type.substring(20, type.length - 21).backToRDOType + ">"

		switch(type)
		{
			case "String": "string"
			case "Integer": "integer"
			case "Boolean": "boolean"
			case "Double": "real"
			default: type
		}
	}

	def static String toSimpleType(String type)
	{
		switch(type)
		{
			case "Integer": "int"
			case "Short"  : "short"
			case "Long"   : "long"
			case "Byte"   : "byte"
			case "Boolean": "boolean"
			case "Double" : "double"
			default: type
		}
	}
}
