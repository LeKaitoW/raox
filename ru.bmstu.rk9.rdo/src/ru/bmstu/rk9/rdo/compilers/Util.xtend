package ru.bmstu.rk9.rdo.compilers

class Util
{
	def public static withFirstUpper(String s)
	{
		return Character.toUpperCase(s.charAt(0)) + s.substring(1)
	}
}
