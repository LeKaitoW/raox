package ru.bmstu.rk9.rao.compilers

import static extension ru.bmstu.rk9.rao.generator.RaoNaming.*
import static extension ru.bmstu.rk9.rao.generator.RaoExpressionCompiler.*
import static extension ru.bmstu.rk9.rao.generator.RaoStatementCompiler.*
import static extension ru.bmstu.rk9.rao.compilers.PatternCompiler.*

import ru.bmstu.rk9.rao.rao.Event
import ru.bmstu.rk9.rao.rao.Parameter
import java.util.List

class EventCompiler
{
	def static compileEvent(Event event, String filename)
	{
		'''
		package «filename»;

		import ru.bmstu.rk9.rao.lib.json.*;

		import ru.bmstu.rk9.rao.lib.*;
		import ru.bmstu.rk9.rao.lib.event.*;
		import ru.bmstu.rk9.rao.lib.simulator.*;
		import ru.bmstu.rk9.rao.lib.database.*;
		@SuppressWarnings("all")

		public class «event.name» implements Event
		{
			private static final String name =  "«event.fullyQualifiedName»";

			@Override
			public String getName()
			{
				return name;
			}

			private static class Parameters
			{
				«IF !event.parameters.empty»
				«FOR parameter : event.parameters»
					public «parameter.compileType» «parameter.name»«parameter.getDefault»;
				«ENDFOR»

				public Parameters(«event.parameters.compileParameterTypes»)
				{
					«FOR parameter : event.parameters»
						if («parameter.name» != null)
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
				«event.body.compileEventAction()»
			}

			@Override
			public void run()
			{
				execute(parameters);
				Database db = Simulator.getDatabase();

				// database operations
				db.addEventEntry(this);
				db.addMemorizedResourceEntries(
						"«event.fullyQualifiedName».createdResources",
						null);
			}

			public «event.name»(double time«IF !event.parameters.empty», «ENDIF»«event.parameters.compileParameterTypes»)
			{
				this.time = time;
				this.parameters = new Parameters(«event.parameters.compileParameterTypesCall»);
			}

			public static final JSONObject structure = new JSONObject()
				.put("name", "«event.fullyQualifiedName»");
		}
		'''
	}

	def private static compileParameterTypesCall(List<Parameter> parameters)
	{
		'''«IF !parameters.empty»«
			parameters.get(0).name»«
			FOR parameter : parameters.subList(1, parameters.size)», «
				parameter.name»«
			ENDFOR»«
		ENDIF»'''
	}
}