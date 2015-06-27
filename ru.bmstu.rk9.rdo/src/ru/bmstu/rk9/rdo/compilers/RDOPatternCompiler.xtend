package ru.bmstu.rk9.rdo.compilers

import java.util.List

import org.eclipse.emf.ecore.EObject

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*
import static extension ru.bmstu.rk9.rdo.generator.RDOExpressionCompiler.*
import static extension ru.bmstu.rk9.rdo.generator.RDOStatementCompiler.*
import static extension ru.bmstu.rk9.rdo.compilers.RDOResourceTypeCompiler.*

import ru.bmstu.rk9.rdo.generator.LocalContext

import ru.bmstu.rk9.rdo.rdo.ResourceType

import ru.bmstu.rk9.rdo.rdo.ResourceCreateStatement

import ru.bmstu.rk9.rdo.rdo.Pattern
import ru.bmstu.rk9.rdo.rdo.ParameterType
import ru.bmstu.rk9.rdo.rdo.PatternSelectMethod
import ru.bmstu.rk9.rdo.rdo.RelevantResource
import ru.bmstu.rk9.rdo.rdo.PatternSelectLogic

class RDOPatternCompiler
{
	def static compilePattern(Pattern pattern, String filename)
	{
		switch (pattern.type)
		{
			case OPERATION,
			case KEYBOARD:
				return compileOperation(pattern, filename)
			case RULE:
				return compileRule(pattern, filename)
		}
	}

	def static compileRule(Pattern rule, String filename)
	{
		'''
		package «filename»;

		import ru.bmstu.rk9.rdo.lib.json.*;

		import ru.bmstu.rk9.rdo.lib.*;
		@SuppressWarnings("all")

		public class «rule.name» implements Rule
		{
			private static final String name = "«filename».«rule.name»";

			@Override
			public String getName()
			{
				return name;
			}

			private static class RelevantResources
			{
				«FOR relevantResource : rule.relevantResources»
					«IF relevantResource.type instanceof ResourceType»
						public «relevantResource.type.fullyQualifiedName» «relevantResource.name»;
					«ELSE»
						public «(relevantResource.type as ResourceCreateStatement).type.fullyQualifiedName» «relevantResource.name»;
					«ENDIF»
				«ENDFOR»

				public RelevantResources copyUpdate()
				{
					RelevantResources clone = new RelevantResources();

					«FOR relevantResource : rule.relevantResources»
						clone.«relevantResource.name» = «(
							if (relevantResource.type instanceof ResourceCreateStatement)
								(relevantResource.type as ResourceCreateStatement).type
							else relevantResource.type).fullyQualifiedName
							».getResource(this.«relevantResource.name».getNumber());
					«ENDFOR»

					return clone;
				}

				public void clear()
				{
					«FOR relevantResource : rule.relevantResources»
						«IF relevantResource.type instanceof ResourceCreateStatement»
							this.«relevantResource.name» = «(relevantResource.type as ResourceCreateStatement).type.fullyQualifiedName
								».getResource("«(relevantResource.type as ResourceCreateStatement).fullyQualifiedName»");
						«ELSE»
							this.«relevantResource.name» = null;
						«ENDIF»
					«ENDFOR»
				}
			}

			private static RelevantResources staticResources = new RelevantResources();
			private RelevantResources resources;

			public static class Parameters
			{
				«IF !rule.parameters.empty»
				«FOR parameter : rule.parameters»
					public «parameter.compileType» «parameter.name»«parameter.getDefault»;
				«ENDFOR»

				public Parameters(«rule.parameters.compileParameterTypes»)
				{
					«FOR parameter : rule.parameters»
						if («parameter.name» != null)
							this.«parameter.name» = «parameter.name»;
					«ENDFOR»
				}
				«ENDIF»
			}

			private Parameters parameters;

			private «rule.name»(RelevantResources resources)
			{
				this.resources = resources;
			}

			«IF rule.combinational != null»
			static private CombinationalChoiceFrom<RelevantResources, Parameters> choice =
				new CombinationalChoiceFrom<RelevantResources, Parameters>
				(
					staticResources,
					«rule.combinational.compileChoiceMethod("RelevantResources")»,
					new CombinationalChoiceFrom.RelevantResourcesManager<RelevantResources>()
					{
						@Override
						public RelevantResources create(RelevantResources set)
						{
							RelevantResources clone = new RelevantResources();

							«FOR relevantResource : rule.relevantResources»
								clone.«relevantResource.name» = set.«relevantResource.name»;
							«ENDFOR»

							return clone;
						}

						@Override
						public void apply(RelevantResources origin, RelevantResources set)
						{
							«FOR relevantResource : rule.relevantResources»
								origin.«relevantResource.name» = set.«relevantResource.name»;
							«ENDFOR»
						}
					}
				);
				static
				{
				«FOR relevantResource : rule.relevantResources»
					choice.addFinder
					(
						new CombinationalChoiceFrom.Finder<RelevantResources, «relevantResource.type.relevantResourceFullyQualifiedName», Parameters>
						(
							new CombinationalChoiceFrom.Retriever<«relevantResource.type.relevantResourceFullyQualifiedName»>()
							{
								@Override
								public java.util.Collection<«relevantResource.type.relevantResourceFullyQualifiedName»> getResources()
								{
									«IF relevantResource.type instanceof ResourceCreateStatement»
									java.util.LinkedList<«relevantResource.type.relevantResourceFullyQualifiedName»> singlelist =
										new java.util.LinkedList<«relevantResource.type.relevantResourceFullyQualifiedName»>();
									singlelist.add(«relevantResource.type.relevantResourceFullyQualifiedName».getResource("«
										(relevantResource.type as ResourceCreateStatement).fullyQualifiedName»"));
									return singlelist;
									«ELSE»
									return «relevantResource.type.relevantResourceFullyQualifiedName».getAll();
									«ENDIF»
								}
							},
							new SimpleChoiceFrom<RelevantResources, «relevantResource.type.relevantResourceFullyQualifiedName», Parameters>
							(
								«relevantResource.select.compileChoiceFrom(rule, relevantResource.type.relevantResourceFullyQualifiedName, relevantResource.name, relevantResource.type.relevantResourceFullyQualifiedName)»,
								null
							),
							new CombinationalChoiceFrom.Setter<RelevantResources, «relevantResource.type.relevantResourceFullyQualifiedName»>()
							{
								public void set(RelevantResources set, «relevantResource.type.relevantResourceFullyQualifiedName» resource)
								{
									set.«relevantResource.name» = resource;
								}
							}
						)
					);
				«ENDFOR»
				}

			«ELSE»
				«FOR relevantResource : rule.relevantResources»
					«IF relevantResource.type instanceof ResourceCreateStatement»
					// just checker «relevantResource.name»
					private static SimpleChoiceFrom.Checker<RelevantResources, «relevantResource.type.relevantResourceFullyQualifiedName», Parameters> «
						relevantResource.name»Checker = «relevantResource.select.compileChoiceFrom(
							rule, relevantResource.type.relevantResourceFullyQualifiedName,
							relevantResource.name, relevantResource.type.relevantResourceFullyQualifiedName)»;
					«ELSE»
					// choice «relevantResource.name»
					private static SimpleChoiceFrom<RelevantResources, «
						relevantResource.type.relevantResourceFullyQualifiedName», Parameters> «relevantResource.name»Choice =
						new SimpleChoiceFrom<RelevantResources, «relevantResource.type.relevantResourceFullyQualifiedName», Parameters>
						(
							«relevantResource.select.compileChoiceFrom(
								rule, relevantResource.type.relevantResourceFullyQualifiedName,
								relevantResource.name,
								relevantResource.type.relevantResourceFullyQualifiedName
							)»,
							«relevantResource.selectMethod.compileChoiceMethod(relevantResource.type.relevantResourceFullyQualifiedName
							)»
						);
					«ENDIF»
				«ENDFOR»
			«ENDIF»

			private static Converter<RelevantResources, Parameters> rule =
					new Converter<RelevantResources, Parameters>()
					{
						@Override
						public void run(RelevantResources resources, Parameters parameters)
						{
							«FOR defaultMethod : rule.defaultMethods.filter[defaultMethod | defaultMethod.name == "execute"]»
								«defaultMethod.body.compilePatternAction»
							«ENDFOR»
						}
					};

			public static boolean findResources(Parameters parameters)
			{
				staticResources.clear();

				«IF rule.combinational != null»
				if (!choice.find(parameters))
					return false;
				«ELSE»
					«FOR relevantResource : rule.relevantResources»
						«IF relevantResource.type instanceof ResourceCreateStatement»
							if (!«relevantResource.name»Checker.check(staticResources, staticResources.«relevantResource.name», parameters))
								return false;

						«ELSE»
							staticResources.«relevantResource.name» = «relevantResource.name»Choice.find(
									staticResources,
									«relevantResource.type.fullyQualifiedName».getAll(),
									parameters);

							if (staticResources.«relevantResource.name» == null)
								return false;

						«ENDIF»
					«ENDFOR»
				«ENDIF»
				return true;
			}

			public static «rule.name» executeRule(Parameters parameters)
			{
				RelevantResources resources = staticResources;

				rule.run(resources, parameters);

				«rule.name» executed = new «rule.name»(resources.copyUpdate());

				return executed;
			}

			@Override
			public void addResourceEntriesToDatabase(Pattern.ExecutedFrom executedFrom)
			{
				Database db = Simulator.getDatabase();
				db.addMemorizedResourceEntries(
						"«rule.fullyQualifiedName».createdResources",
						executedFrom);
			}

			@Override
			public int[] getRelevantInfo()
			{
				return new int[]
				{
					«FOR i : 0 ..< rule.relevantResources.size»
						resources.«rule.relevantResources.get(i).name».getNumber()«
							IF i < rule.relevantResources.size - 1»,«ENDIF»
					«ENDFOR»
				};
			}

			public static final JSONObject structure = new JSONObject()
				.put("name", "«rule.fullyQualifiedName»")
				.put("type", "rule")
				.put
				(
					"relevant_resources", new JSONArray()
						«FOR relevantResource : rule.relevantResources»
							.put
							(
								new JSONObject()
									.put("name", "«relevantResource.name»")
									.put("type", "«
										IF relevantResource.type instanceof ResourceCreateStatement
											»«(relevantResource.type as ResourceCreateStatement).type.fullyQualifiedName»«
										ELSE
											»«(relevantResource.type as ResourceType).fullyQualifiedName»«
										ENDIF»")
							)
						«ENDFOR»
				);
		}
		'''
	}

	def static compileOperation(Pattern operation, String filename)
	{
		'''
		package «filename»;

		import ru.bmstu.rk9.rdo.lib.json.*;

		import ru.bmstu.rk9.rdo.lib.*;
		@SuppressWarnings("all")

		public class «operation.name» implements Rule, Event
		{
			private static final String name = "«filename».«operation.name»";

			@Override
			public String getName()
			{
				return name;
			}

			private static class RelevantResources
			{
				«FOR relevantResource : operation.relevantResources»
					«IF relevantResource.type instanceof ResourceType»
						public «relevantResource.type.fullyQualifiedName» «relevantResource.name»;
					«ELSE»
						public «(relevantResource.type as ResourceCreateStatement).type.fullyQualifiedName» «relevantResource.name»;
					«ENDIF»
				«ENDFOR»

				public RelevantResources copyUpdate()
				{
					RelevantResources clone = new RelevantResources();

					«FOR relevantResource : operation.relevantResources»
						clone.«relevantResource.name» = «(
							if (relevantResource.type instanceof ResourceCreateStatement)
								(relevantResource.type as ResourceCreateStatement).type
							else relevantResource.type).fullyQualifiedName
							».getResource(this.«relevantResource.name».getNumber());
					«ENDFOR»

					return clone;
				}

				public void clear()
				{
					«FOR relevantResource : operation.relevantResources»
						«IF relevantResource.type instanceof ResourceCreateStatement»
							this.«relevantResource.name» = «(relevantResource.type as ResourceCreateStatement).type.fullyQualifiedName
								».getResource("«(relevantResource.type as ResourceCreateStatement).fullyQualifiedName»");
						«ELSE»
							this.«relevantResource.name» = null;
						«ENDIF»
					«ENDFOR»
				}
			}

			private static RelevantResources staticResources = new RelevantResources();
			private RelevantResources resources;

			public static class Parameters
			{
				«IF !operation.parameters.empty»
				«FOR parameter : operation.parameters»
					public «parameter.compileType» «parameter.name»«parameter.getDefault»;
				«ENDFOR»

				public Parameters(«operation.parameters.compileParameterTypes»)
				{
					«FOR parameter : operation.parameters»
						if («parameter.name» != null)
							this.«parameter.name» = «parameter.name»;
					«ENDFOR»
				}
				«ENDIF»
			}

			private Parameters parameters;

			private «operation.name»(double time, RelevantResources resources, Parameters parameters)
			{
				this.time = time;
				this.resources = resources;
				this.parameters = parameters;
			}

			«IF operation.combinational != null»
			static private CombinationalChoiceFrom<RelevantResources, Parameters> choice =
				new CombinationalChoiceFrom<RelevantResources, Parameters>
				(
					staticResources,
					«operation.combinational.compileChoiceMethod("RelevantResources")»,
					new CombinationalChoiceFrom.RelevantResourcesManager<RelevantResources>()
					{
						@Override
						public RelevantResources create(RelevantResources set)
						{
							RelevantResources clone = new RelevantResources();

							«FOR relevantResource : operation.relevantResources»
								clone.«relevantResource.name» = set.«relevantResource.name»;
							«ENDFOR»

							return clone;
						}

						@Override
						public void apply(RelevantResources origin, RelevantResources set)
						{
							«FOR relevantResource : operation.relevantResources»
								origin.«relevantResource.name» = set.«relevantResource.name»;
							«ENDFOR»
						}
					}
				);
				static
				{
				«FOR relevantResource : operation.relevantResources»
					choice.addFinder
					(
						new CombinationalChoiceFrom.Finder<RelevantResources, «relevantResource.type.fullyQualifiedName», Parameters>
						(
							new CombinationalChoiceFrom.Retriever<«relevantResource.type.fullyQualifiedName»>()
							{
								@Override
								public java.util.Collection<«relevantResource.type.fullyQualifiedName»> getResources()
								{
									«IF relevantResource.type instanceof ResourceCreateStatement»
									java.util.LinkedList<«relevantResource.type.relevantResourceFullyQualifiedName»> singlelist =
										new java.util.LinkedList<«relevantResource.type.relevantResourceFullyQualifiedName»>();
									singlelist.add(«relevantResource.type.relevantResourceFullyQualifiedName».getResource("«
										(relevantResource.type as ResourceCreateStatement).fullyQualifiedName»"));
									return singlelist;
									«ELSE»
									return «relevantResource.type.relevantResourceFullyQualifiedName».getAll();
									«ENDIF»
								}
							},
							new SimpleChoiceFrom<RelevantResources, «relevantResource.type.fullyQualifiedName», Parameters>
							(
								«relevantResource.select.compileChoiceFrom(operation, relevantResource.type.fullyQualifiedName, relevantResource.name, relevantResource.type.fullyQualifiedName)»,
								null
							),
							new CombinationalChoiceFrom.Setter<RelevantResources, «relevantResource.type.fullyQualifiedName»>()
							{
								public void set(RelevantResources set, «relevantResource.type.fullyQualifiedName» resource)
								{
									set.«relevantResource.name» = resource;
								}
							}
						)
					);

				«ENDFOR»
				}
			«ELSE»
				«FOR relevantResource : operation.relevantResources»
					«IF relevantResource.type instanceof ResourceCreateStatement»
						// just checker «relevantResource.name»
						private static SimpleChoiceFrom.Checker<RelevantResources, «relevantResource.type.relevantResourceFullyQualifiedName», Parameters> «
							relevantResource.name»Checker = «relevantResource.select.compileChoiceFrom(operation, relevantResource.type.relevantResourceFullyQualifiedName,
									relevantResource.name, relevantResource.type.relevantResourceFullyQualifiedName)»;
					«ELSE»
						// choice «relevantResource.name»
						private static SimpleChoiceFrom<RelevantResources, «relevantResource.type.relevantResourceFullyQualifiedName», Parameters> «relevantResource.name»Choice =
							new SimpleChoiceFrom<RelevantResources, «relevantResource.type.relevantResourceFullyQualifiedName», Parameters>
							(
								«relevantResource.select.compileChoiceFrom(operation, relevantResource.type.relevantResourceFullyQualifiedName, relevantResource.name, relevantResource.type.relevantResourceFullyQualifiedName)»,
								«relevantResource.selectMethod.compileChoiceMethod(relevantResource.type.relevantResourceFullyQualifiedName)»
							);
					«ENDIF»
				«ENDFOR»
			«ENDIF»

			private static Converter<RelevantResources, Parameters> begin =
					new Converter<RelevantResources, Parameters>()
					{
						@Override
						public void run(RelevantResources resources, Parameters parameters)
						{
							«FOR defaultMethod : operation.defaultMethods.filter[defaultMethod | defaultMethod.name == "begin"]»
								«defaultMethod.body.compilePatternAction»
							«ENDFOR»
						}
					};

			private static Converter<RelevantResources, Parameters> end =
					new Converter<RelevantResources, Parameters>()
					{
						@Override
						public void run(RelevantResources resources, Parameters parameters)
						{
							«FOR defaultMethod: operation.defaultMethods.filter[defaultMethod | defaultMethod.name == "end"]»
								«defaultMethod.body.compilePatternAction»
							«ENDFOR»
						}
					};

			public static boolean findResources(Parameters parameters)
			{
				staticResources.clear();

				«IF operation.combinational != null»
				if (!choice.find(parameters))
					return false;

				«ELSE»
				«FOR relevantResource : operation.relevantResources»
				«IF relevantResource.type instanceof ResourceCreateStatement»
					if (!«relevantResource.name»Checker.check(staticResources, staticResources.«relevantResource.name», parameters))
						return false;

				«ELSE»
					staticResources.«relevantResource.name» = «relevantResource.name»Choice.find(staticResources, «relevantResource.type.fullyQualifiedName».getAll(), parameters);

					if (staticResources.«relevantResource.name» == null)
						return false;

				«ENDIF»
				«ENDFOR»
				«ENDIF»
				return true;
			}

			private static double duration(RelevantResources resources, Parameters parameters)
			{
				«FOR defaultMethod: operation.defaultMethods.filter[defaultMethod | defaultMethod.name == "duration"]»
					«defaultMethod.body.compileStatementContext(
						(new LocalContext).populateFromPattern(operation)
					)»
				«ENDFOR»
			}

			public static «operation.name» executeRule(Parameters parameters)
			{
				RelevantResources resources = staticResources;

				begin.run(resources, parameters);


				«operation.name» instance = new «operation.name»(
					Simulator.getTime() + duration(resources, parameters),
					resources.copyUpdate(),
					parameters);


				Simulator.pushEvent(instance);

				return instance;
			}

			@Override
			public void addResourceEntriesToDatabase(Pattern.ExecutedFrom executedFrom)
			{
				Database db = Simulator.getDatabase();
				db.addMemorizedResourceEntries(
						"«operation.fullyQualifiedName».createdResources",
						null);
			}

			private double time;

			@Override
			public double getTime()
			{
				return time;
			}

			@Override
			public void run()
			{
				Database db = Simulator.getDatabase();

				end.run(resources, parameters);

				// database operations
				db.addOperationEndEntry(this);
				db.addMemorizedResourceEntries(
						"«operation.fullyQualifiedName»",
						null);
			}

			@Override
			public int[] getRelevantInfo()
			{
				return new int[]
				{
					«FOR i : 0 ..< operation.relevantResources.size»
						resources.«operation.relevantResources.get(i).name».getNumber()«
							IF i < operation.relevantResources.size - 1»,«ENDIF»
					«ENDFOR»
				};
			}

			public static final JSONObject structure = new JSONObject()
				.put("name", "«operation.fullyQualifiedName»")
				.put("type", "operation")
				.put
				(
					"relevant_resources", new JSONArray()
						«FOR relevantResource : operation.relevantResources»
							.put
							(
								new JSONObject()
									.put("name", "«relevantResource.name»")
									.put("type", "«
										IF relevantResource.type instanceof ResourceCreateStatement
											»«(relevantResource.type as ResourceCreateStatement).type.fullyQualifiedName»«
										ELSE
											»«(relevantResource.type as ResourceType).fullyQualifiedName»«
										ENDIF»")
							)
						«ENDFOR»
				);
		}
		'''
	}

	def static compileChoiceFrom(PatternSelectLogic selectLogic, Pattern pattern, String resource,
			String relevantResource, String relevantResourceType) {
		val haveSelectLogic = selectLogic != null && !selectLogic.any;

		var List<String> relevantResouces;
		var List<String> relevantResourcesTypes;

		relevantResouces = pattern.relevantResources.map[relRes|relRes.name]
		relevantResourcesTypes = pattern.relevantResources.map [ relRes |
			if (relRes.type instanceof ResourceType)
				(relRes.type as ResourceType).fullyQualifiedName
			else
				(relRes.type as ResourceCreateStatement).type.fullyQualifiedName
		]

		return '''
		new SimpleChoiceFrom.Checker<RelevantResources, «resource», Parameters>()
		{
			@Override
			public boolean check(RelevantResources resources, «resource» «relevantResource», Parameters parameters)
			{
			return
				«IF haveSelectLogic»
					(«selectLogic.logic.compileExpressionContext(
						(new LocalContext).populateFromPattern(pattern)).value.replaceAll(
							"resources." + relevantResource + ".", relevantResource + ".")»)
				«ELSE»
					true
				«ENDIF»
				«FOR i : 0 ..< relevantResouces.size»
					«IF relevantResource != relevantResouces.get(i) && relevantResourceType == relevantResourcesTypes.get(i)»
						«"\t"»&& «relevantResource» != resources.«relevantResouces.get(i)»
					«ENDIF»
				«ENDFOR»;
			}
		}'''
	}

	def private static compileChoiceMethod(PatternSelectMethod selectMethod, String resource)
	{
		if (selectMethod == null || selectMethod.first)
			return "null"
		else
		{
			var EObject pattern;
			var String relevantResource = "@NOPE";
			if (selectMethod.eContainer instanceof Pattern)
				pattern = selectMethod.eContainer
			else
			{
				pattern = selectMethod.eContainer.eContainer
				relevantResource = (if (selectMethod.eContainer instanceof RelevantResource) (selectMethod.eContainer as RelevantResource).name
					else (selectMethod.eContainer as RelevantResource).name)
			}

			val context = (new LocalContext).populateFromPattern(pattern as Pattern)

			val expression = selectMethod.expression.compileExpressionContext(context).value
			return
				'''
				new SimpleChoiceFrom.ChoiceMethod<RelevantResources, «resource», Parameters>()
				{
					@Override
					public int compare(«resource» x, «resource» y)
					{
						if («expression.replaceAll("resources." + relevantResource + ".", "x.")» «IF selectMethod.withType.literal
							== "with_min"»<«ELSE»>«ENDIF» «expression.replaceAll("resources." + relevantResource + ".", "y.")»)
							return -1;
						else
							return 1;
					}
				}'''
		}
	}

	def static compileParameterTypes(List<ParameterType> parameters)
	{
		'''«IF !parameters.empty»«parameters.get(0).compileType» «
			parameters.get(0).name»«
			FOR parameter : parameters.subList(1, parameters.size)», «
				parameter.compileType» «
				parameter.name»«
			ENDFOR»«
		ENDIF»'''
	}
}