package ru.bmstu.rk9.rao.compilers

class Util {
	def static String getTypeConstantSize(String type) {
		if (type == "Integer")
			return "4"

		if (type == "Double")
			return "8"

		if (type == "Boolean")
			return "1"

		if (type.endsWith("_enum"))
			return "2"

		return "0"
	}

	def static String backToRaoType(String type) {
		if (type.startsWith("java.util.ArrayList"))
			"array<" + type.substring(20, type.length - 21).backToRaoType + ">"

		switch (type) {
			case "Integer",
			case "Boolean",
			case "Double": toSimpleType(type)
			default: type
		}
	}

	def static String toSimpleType(String type) {
		switch (type) {
			case "Integer": "int"
			case "Short": "short"
			case "Long": "long"
			case "Byte": "byte"
			case "Boolean": "boolean"
			case "Double": "double"
			default: type
		}
	}

	def static putImports() {
		return '''import ru.bmstu.rk9.rao.lib.*;
		import ru.bmstu.rk9.rao.lib.animation.*;
		import ru.bmstu.rk9.rao.lib.animation.RaoColor.*;
		import ru.bmstu.rk9.rao.lib.database.*;
		import ru.bmstu.rk9.rao.lib.dpt.*;
		import ru.bmstu.rk9.rao.lib.event.*;
		import ru.bmstu.rk9.rao.lib.json.*;
		import ru.bmstu.rk9.rao.lib.math.*;
		import ru.bmstu.rk9.rao.lib.modelStructure.*;
		import ru.bmstu.rk9.rao.lib.naming.*;
		import ru.bmstu.rk9.rao.lib.notification.*;
		import ru.bmstu.rk9.rao.lib.pattern.*;
		import ru.bmstu.rk9.rao.lib.resource.*;
		import ru.bmstu.rk9.rao.lib.result.*;
		import ru.bmstu.rk9.rao.lib.sequence.*;
		import ru.bmstu.rk9.rao.lib.simulator.*;
		import ru.bmstu.rk9.rao.lib.simulator.Simulator.*;
		import ru.bmstu.rk9.rao.lib.type.*;
		@SuppressWarnings("all")
		'''
	}
}
