package ru.bmstu.rk9.rdo.compilers

import ru.bmstu.rk9.rdo.rdo.RDOEnum


class RDOEnumCompiler
{
	def static public makeEnumBody(RDOEnum e)
	{
		var flag = false
		var body = ""

		for(i : e.enums)
		{
			if(flag)
				body = body + ", "
			body = body + i.name
			flag = true
		}
		return body
	}
}
