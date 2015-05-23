package ru.bmstu.rk9.rdo.compilers

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*
import static extension ru.bmstu.rk9.rdo.generator.RDOExpressionCompiler.*
import static extension ru.bmstu.rk9.rdo.generator.RDOStatementCompiler.*
import static extension ru.bmstu.rk9.rdo.compilers.RDOResourceTypeCompiler.*
import static extension ru.bmstu.rk9.rdo.compilers.RDOPatternCompiler.*

import ru.bmstu.rk9.rdo.rdo.Event
import ru.bmstu.rk9.rdo.rdo.ParameterType
import java.util.List

class RDOEventCompiler
{
	def static compileEvent(Event evn, String filename)
	{
		'''
		package «filename»;

		import ru.bmstu.rk9.rdo.lib.json.*;

		import ru.bmstu.rk9.rdo.lib.*;
		@SuppressWarnings("all")

		public class «evn.name» implements Event
		{
			private static final String name =  "«evn.fullyQualifiedName»";

			@Override
			public String getName()
			{
				return name;
			}

			private static class Parameters
			{
				«IF !evn.parameters.empty»
				«FOR p : evn.parameters»
					public «p.compileType» «p.name»«p.getDefault»;
				«ENDFOR»

				public Parameters(«evn.parameters.compileParameterTypes»)
				{
					«FOR parameter : evn.parameters»
						if(«parameter.name» != null)
							this.«parameter.name» = «parameter.name»;
					«ENDFOR»
				}
				«ENDIF»
			}

			private Parameters parameters;

			private double time;

			@Override
			public double getTime()
			{
				return time;
			}

			private static void execute(Parameters parameters)
			{
				«evn.body.compileEventAction()»
			}

			@Override
			public void run()
			{
				execute(parameters);
				Database db = Simulator.getDatabase();

				// database operations
				db.addEventEntry(this);
			}

			public «evn.name»(double time«IF !evn.parameters.empty», «ENDIF»«evn.parameters.compileParameterTypes»)
			{
				this.time = time;
				this.parameters = new Parameters(«evn.parameters.compileParameterTypesCall»);
			}

			public static final JSONObject structure = new JSONObject()
				.put("name", "«evn.fullyQualifiedName»")
				.put("type", "event");
		}
		'''
	}

	def private static compileParameterTypesCall(List<ParameterType> parameters)
	{
		'''«IF !parameters.empty»«
			parameters.get(0).name»«
			FOR parameter : parameters.subList(1, parameters.size)», «
				parameter.name»«
			ENDFOR»«
		ENDIF»'''
	}
}