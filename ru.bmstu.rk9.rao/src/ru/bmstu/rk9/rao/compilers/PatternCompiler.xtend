package ru.bmstu.rk9.rao.compilers

import java.util.List

import org.eclipse.emf.ecore.EObject

import static extension ru.bmstu.rk9.rao.generator.RaoNaming.*
import static extension ru.bmstu.rk9.rao.generator.RaoExpressionCompiler.*
import static extension ru.bmstu.rk9.rao.generator.RaoStatementCompiler.*

import ru.bmstu.rk9.rao.generator.LocalContext

import ru.bmstu.rk9.rao.rao.ResourceType

import ru.bmstu.rk9.rao.rao.ResourceCreateStatement

import ru.bmstu.rk9.rao.rao.Pattern
import ru.bmstu.rk9.rao.rao.Parameter
import ru.bmstu.rk9.rao.rao.PatternSelectMethod
import ru.bmstu.rk9.rao.rao.RelevantResource
import ru.bmstu.rk9.rao.rao.PatternSelectLogic

class PatternCompiler
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

		import ru.bmstu.rk9.rao.lib.json.*;

		import ru.bmstu.rk9.rao.lib.*;
		import ru.bmstu.rk9.rao.lib.pattern.*;
		import ru.bmstu.rk9.rao.lib.simulator.*;
		import ru.bmstu.rk9.rao.lib.database.*;
		@SuppressWarnings("all")

		public class «rule.name» implements Rule
		{
			private «rule.name»(RelevantResources resources)
			{
				this.resources = resources;
			}

			«rule.compileCommonPatternBodyPart(filename)»

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

			public static «rule.name» executeRule(Parameters parameters)
			{
				RelevantResources resources = staticResources;

				rule.run(resources, parameters);

				«rule.name» executed = new «rule.name»(resources.copyUpdate());

				return executed;
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

		import ru.bmstu.rk9.rao.lib.json.*;

		import ru.bmstu.rk9.rao.lib.*;
		import ru.bmstu.rk9.rao.lib.pattern.*;
		import ru.bmstu.rk9.rao.lib.simulator.*;
		import ru.bmstu.rk9.rao.lib.database.*;
		import ru.bmstu.rk9.rao.lib.event.*;
		@SuppressWarnings("all")

		public class «operation.name» implements Rule, Event
		{
			private «operation.name»(double time, RelevantResources resources, Parameters parameters)
			{
				this.time = time;
				this.resources = resources;
				this.parameters = parameters;
			}

			«operation.compileCommonPatternBodyPart(filename)»

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

			private static double duration(RelevantResources resources, Parameters parameters)
			{
				«val durationMethods = operation.defaultMethods.filter[defaultMethod | defaultMethod.name == "duration"]»
				«IF durationMethods.size > 0»
					«FOR durationMethod: durationMethods»
						«IF durationMethod.body.statements.size > 0»
							«durationMethod.body.compileStatementContext(
								(new LocalContext).populateFromPattern(operation)
							)»
						«ELSE»
							return 0;
						«ENDIF»
					«ENDFOR»
				«ELSE»
					return 0;
				«ENDIF»
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

	def private static compileCommonPatternBodyPart(Pattern pattern, String filename) {
		return
			'''
			private static final String name = "«filename».«pattern.name»";

			@Override
			public String getName()
			{
				return name;
			}

			private static class RelevantResources
			{
				«FOR relevantResource : pattern.relevantResources»
					«IF relevantResource.type instanceof ResourceType»
						public «relevantResource.type.fullyQualifiedName» «relevantResource.name»;
					«ELSE»
						public «(relevantResource.type as ResourceCreateStatement).type.fullyQualifiedName» «relevantResource.name»;
					«ENDIF»
				«ENDFOR»

				public RelevantResources copyUpdate()
				{
					RelevantResources clone = new RelevantResources();

					«FOR relevantResource : pattern.relevantResources»
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
					«FOR relevantResource : pattern.relevantResources»
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
				«IF !pattern.parameters.empty»
				«FOR parameter : pattern.parameters»
					public «parameter.compileType» «parameter.name»«parameter.getDefault»;
				«ENDFOR»

				public Parameters(«pattern.parameters.compileParameterTypes»)
				{
					«FOR parameter : pattern.parameters»
						if («parameter.name» != null)
							this.«parameter.name» = «parameter.name»;
					«ENDFOR»
				}
				«ENDIF»
			}

			private Parameters parameters;

			«IF pattern.combinational != null»
			static private CombinationalChoiceFrom<RelevantResources, Parameters> choice =
				new CombinationalChoiceFrom<RelevantResources, Parameters>
				(
					staticResources,
					«pattern.combinational.compileChoiceMethod("RelevantResources")»,
					new CombinationalChoiceFrom.RelevantResourcesManager<RelevantResources>()
					{
						@Override
						public RelevantResources create(RelevantResources set)
						{
							RelevantResources clone = new RelevantResources();

							«FOR relevantResource : pattern.relevantResources»
								clone.«relevantResource.name» = set.«relevantResource.name»;
							«ENDFOR»

							return clone;
						}

						@Override
						public void apply(RelevantResources origin, RelevantResources set)
						{
							«FOR relevantResource : pattern.relevantResources»
								origin.«relevantResource.name» = set.«relevantResource.name»;
							«ENDFOR»
						}
					}
				);
				static
				{
				«FOR relevantResource : pattern.relevantResources»
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
								«relevantResource.select.compileChoiceFrom(pattern, relevantResource.type.relevantResourceFullyQualifiedName, relevantResource.name, relevantResource.type.relevantResourceFullyQualifiedName)»,
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
				«FOR relevantResource : pattern.relevantResources»
					«IF relevantResource.type instanceof ResourceCreateStatement»
					// just checker «relevantResource.name»
					private static SimpleChoiceFrom.Checker<RelevantResources, «relevantResource.type.relevantResourceFullyQualifiedName», Parameters> «
						relevantResource.name»Checker = «relevantResource.select.compileChoiceFrom(
							pattern, relevantResource.type.relevantResourceFullyQualifiedName,
							relevantResource.name, relevantResource.type.relevantResourceFullyQualifiedName)»;
					«ELSE»
					// choice «relevantResource.name»
					private static SimpleChoiceFrom<RelevantResources, «
						relevantResource.type.relevantResourceFullyQualifiedName», Parameters> «relevantResource.name»Choice =
						new SimpleChoiceFrom<RelevantResources, «relevantResource.type.relevantResourceFullyQualifiedName», Parameters>
						(
							«relevantResource.select.compileChoiceFrom(
								pattern, relevantResource.type.relevantResourceFullyQualifiedName,
								relevantResource.name,
								relevantResource.type.relevantResourceFullyQualifiedName
							)»,
							«relevantResource.selectMethod.compileChoiceMethod(relevantResource.type.relevantResourceFullyQualifiedName
							)»
						);
					«ENDIF»
				«ENDFOR»
			«ENDIF»

			public static boolean findResources(Parameters parameters)
			{
				staticResources.clear();

				«IF pattern.combinational != null»
				if (!choice.find(parameters))
					return false;
				«ELSE»
					«FOR relevantResource : pattern.relevantResources»
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

			@Override
			public void addResourceEntriesToDatabase(Pattern.ExecutedFrom executedFrom)
			{
				Database db = Simulator.getDatabase();
				db.addMemorizedResourceEntries(
						"«pattern.fullyQualifiedName».createdResources",
						executedFrom);
			}

			@Override
			public int[] getRelevantInfo()
			{
				return new int[]
				{
					«FOR i : 0 ..< pattern.relevantResources.size»
						resources.«pattern.relevantResources.get(i).name».getNumber()«
							IF i < pattern.relevantResources.size - 1»,«ENDIF»
					«ENDFOR»
				};
			}
			'''
	}

	def private static compileChoiceFrom(PatternSelectLogic selectLogic, Pattern pattern, String resource,
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

	def static compileParameterTypes(List<Parameter> parameters)
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