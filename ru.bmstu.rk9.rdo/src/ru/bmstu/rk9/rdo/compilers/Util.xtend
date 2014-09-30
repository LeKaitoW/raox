package ru.bmstu.rk9.rdo.compilers

class Util
{
	def public static withFirstUpper(String s)
	{
		return Character.toUpperCase(s.charAt(0)) + s.substring(1)
	}

	def public static String getTypeSize(String type, String name)
	{
		if(type == "Integer")
			return "4"

		if(type == "Double")
			return "8"

		if(type == "Boolean")
			return "1"

		if(type == "String")
			return name + ".length()"

		if(type.endsWith("_enum"))
			return "2"

		return "0"
	}
	
}
