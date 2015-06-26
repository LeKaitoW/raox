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
				«FOR r : rule.relevantresources»
					«IF r.type instanceof ResourceType»
						public «r.type.fullyQualifiedName» «r.name»;
					«ELSE»
						public «(r.type as ResourceCreateStatement).type.fullyQualifiedName» «r.name»;
					«ENDIF»
				«ENDFOR»

				public RelevantResources copyUpdate()
				{
					RelevantResources clone = new RelevantResources();

					«FOR r : rule.relevantresources»
						clone.«r.name» = «(
							if(r.type instanceof ResourceCreateStatement)
								(r.type as ResourceCreateStatement).type
							else r.type).fullyQualifiedName
							».getResource(this.«r.name».getNumber());
					«ENDFOR»

					return clone;
				}

				public void clear()
				{
					«FOR r : rule.relevantresources»
					«IF r.type instanceof ResourceCreateStatement»
						this.«r.name» = «(r.type as ResourceCreateStatement).type.fullyQualifiedName
							».getResource("«(r.type as ResourceCreateStatement).fullyQualifiedName»");
					«ELSE»
						this.«r.name» = null;
					«ENDIF»
					«ENDFOR»
				}
			}

			private static RelevantResources staticResources = new RelevantResources();
			private RelevantResources resources;

			public static class Parameters
			{
				«IF !rule.parameters.empty»
				«FOR p : rule.parameters»
					public «p.compileType» «p.name»«p.getDefault»;
				«ENDFOR»

				public Parameters(«rule.parameters.compileParameterTypes»)
				{
					«FOR p : rule.parameters»
						if(«p.name» != null)
							this.«p.name» = «p.name»;
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
					«rule.combinational.compileChoiceMethod("RelevantResources", "RelevantResources")»,
					new CombinationalChoiceFrom.RelevantResourcesManager<RelevantResources>()
					{
						@Override
						public RelevantResources create(RelevantResources set)
						{
							RelevantResources clone = new RelevantResources();

							«FOR relres : rule.relevantresources»
								clone.«relres.name» = set.«relres.name»;
							«ENDFOR»

							return clone;
						}

						@Override
						public void apply(RelevantResources origin, RelevantResources set)
						{
							«FOR relres : rule.relevantresources»
								origin.«relres.name» = set.«relres.name»;
							«ENDFOR»
						}
					}
				);
				static
				{
				«FOR relres : rule.relevantresources»
					choice.addFinder
					(
						new CombinationalChoiceFrom.Finder<RelevantResources, «relres.type.relResFullyQualifiedName», Parameters>
						(
							new CombinationalChoiceFrom.Retriever<«relres.type.relResFullyQualifiedName»>()
							{
								@Override
								public java.util.Collection<«relres.type.relResFullyQualifiedName»> getResources()
								{
									«IF relres.type instanceof ResourceCreateStatement»
									java.util.LinkedList<«relres.type.relResFullyQualifiedName»> singlelist =
										new java.util.LinkedList<«relres.type.relResFullyQualifiedName»>();
									singlelist.add(«relres.type.relResFullyQualifiedName».getResource("«
										(relres.type as ResourceCreateStatement).fullyQualifiedName»"));
									return singlelist;
									«ELSE»
									return «relres.type.relResFullyQualifiedName».getAll();
									«ENDIF»
								}
							},
							new SimpleChoiceFrom<RelevantResources, «relres.type.relResFullyQualifiedName», Parameters>
							(
								«relres.select.compileChoiceFrom(rule, relres.type.relResFullyQualifiedName, relres.name, relres.type.relResFullyQualifiedName)»,
								null
							),
							new CombinationalChoiceFrom.Setter<RelevantResources, «relres.type.relResFullyQualifiedName»>()
							{
								public void set(RelevantResources set, «relres.type.relResFullyQualifiedName» resource)
								{
									set.«relres.name» = resource;
								}
							}
						)
					);
				«ENDFOR»
				}

			«ELSE»
			«FOR relres : rule.relevantresources»
			«IF relres.type instanceof ResourceCreateStatement»
			// just checker «relres.name»
			private static SimpleChoiceFrom.Checker<RelevantResources, «relres.type.relResFullyQualifiedName», Parameters> «
				relres.name»Checker = «relres.select.compileChoiceFrom(
					rule, relres.type.relResFullyQualifiedName,
					relres.name, relres.type.relResFullyQualifiedName)»;

			«ELSE»
			// choice «relres.name»
			private static SimpleChoiceFrom<RelevantResources, «
				relres.type.relResFullyQualifiedName», Parameters> «relres.name»Choice =
				new SimpleChoiceFrom<RelevantResources, «relres.type.relResFullyQualifiedName», Parameters>
				(
					«relres.select.compileChoiceFrom(
						rule, relres.type.relResFullyQualifiedName,
						relres.name,
						relres.type.relResFullyQualifiedName
					)»,
					«relres.selectmethod.compileChoiceMethod(
						rule.name, relres.type.relResFullyQualifiedName
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
							«FOR m: rule.defaultMethods.filter[m | m.name == "execute"]»
								«m.body.compilePatternAction»
							«ENDFOR»
						}
					};

			public static boolean findResources(Parameters parameters)
			{
				staticResources.clear();

				«IF rule.combinational != null»
				if(!choice.find(parameters))
					return false;

				«ELSE»
				«FOR relres : rule.relevantresources»
				«IF relres.type instanceof ResourceCreateStatement»
					if(!«relres.name»Checker.check(staticResources, staticResources.«relres.name», parameters))
						return false;

				«ELSE»
					staticResources.«relres.name» = «relres.name»Choice.find(
							staticResources,
							«relres.type.fullyQualifiedName».getAll(),
							parameters);

					if(staticResources.«relres.name» == null)
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
					«FOR i : 0 ..< rule.relevantresources.size»
						resources.«rule.relevantresources.get(i).name».getNumber()«
							IF i < rule.relevantresources.size - 1»,«ENDIF»
					«ENDFOR»
				};
			}

			public static final JSONObject structure = new JSONObject()
				.put("name", "«rule.fullyQualifiedName»")
				.put("type", "rule")
				.put
				(
					"relevant_resources", new JSONArray()
						«FOR r : rule.relevantresources»
							.put
							(
								new JSONObject()
									.put("name", "«r.name»")
									.put("type", "«
										IF r.type instanceof ResourceCreateStatement
											»«(r.type as ResourceCreateStatement).type.fullyQualifiedName»«
										ELSE
											»«(r.type as ResourceType).fullyQualifiedName»«
										ENDIF»")
							)
						«ENDFOR»
				);
		}
		'''
	}

	def static compileOperation(Pattern op, String filename)
	{
		'''
		package «filename»;

		import ru.bmstu.rk9.rdo.lib.json.*;

		import ru.bmstu.rk9.rdo.lib.*;
		@SuppressWarnings("all")

		public class «op.name» implements Rule, Event
		{
			private static final String name = "«filename».«op.name»";

			@Override
			public String getName()
			{
				return name;
			}

			private static class RelevantResources
			{
				«FOR r : op.relevantresources»
					«IF r.type instanceof ResourceType»
						public «r.type.fullyQualifiedName» «r.name»;
					«ELSE»
						public «(r.type as ResourceCreateStatement).type.fullyQualifiedName» «r.name»;
					«ENDIF»
				«ENDFOR»

				public RelevantResources copyUpdate()
				{
					RelevantResources clone = new RelevantResources();

					«FOR r : op.relevantresources»
						clone.«r.name» = «(
							if(r.type instanceof ResourceCreateStatement)
								(r.type as ResourceCreateStatement).type
							else r.type).fullyQualifiedName
							».getResource(this.«r.name».getNumber());
					«ENDFOR»

					return clone;
				}

				public void clear()
				{
					«FOR r : op.relevantresources»
					«IF r.type instanceof ResourceCreateStatement»
						this.«r.name» = «(r.type as ResourceCreateStatement).type.fullyQualifiedName
							».getResource("«(r.type as ResourceCreateStatement).fullyQualifiedName»");
					«ELSE»
						this.«r.name» = null;
					«ENDIF»
					«ENDFOR»
				}
			}

			private static RelevantResources staticResources = new RelevantResources();
			private RelevantResources resources;

			public static class Parameters
			{
				«IF !op.parameters.empty»
				«FOR p : op.parameters»
					public «p.compileType» «p.name»«p.getDefault»;
				«ENDFOR»

				public Parameters(«op.parameters.compileParameterTypes»)
				{
					«FOR p : op.parameters»
						if(«p.name» != null)
							this.«p.name» = «p.name»;
					«ENDFOR»
				}
				«ENDIF»
			}

			private Parameters parameters;

			private «op.name»(double time, RelevantResources resources, Parameters parameters)
			{
				this.time = time;
				this.resources = resources;
				this.parameters = parameters;
			}

			«IF op.combinational != null»
			static private CombinationalChoiceFrom<RelevantResources, Parameters> choice =
				new CombinationalChoiceFrom<RelevantResources, Parameters>
				(
					staticResources,
					«op.combinational.compileChoiceMethod("RelevantResources", "RelevantResources")»,
					new CombinationalChoiceFrom.RelevantResourcesManager<RelevantResources>()
					{
						@Override
						public RelevantResources create(RelevantResources set)
						{
							RelevantResources clone = new RelevantResources();

							«FOR relres : op.relevantresources»
								clone.«relres.name» = set.«relres.name»;
							«ENDFOR»

							return clone;
						}

						@Override
						public void apply(RelevantResources origin, RelevantResources set)
						{
							«FOR relres : op.relevantresources»
								origin.«relres.name» = set.«relres.name»;
							«ENDFOR»
						}
					}
				);
				static
				{
				«FOR relres : op.relevantresources»
					choice.addFinder
					(
						new CombinationalChoiceFrom.Finder<RelevantResources, «relres.type.fullyQualifiedName», Parameters>
						(
							new CombinationalChoiceFrom.Retriever<«relres.type.fullyQualifiedName»>()
							{
								@Override
								public java.util.Collection<«relres.type.fullyQualifiedName»> getResources()
								{
									«IF relres.type instanceof ResourceCreateStatement»
									java.util.LinkedList<«relres.type.relResFullyQualifiedName»> singlelist =
										new java.util.LinkedList<«relres.type.relResFullyQualifiedName»>();
									singlelist.add(«relres.type.relResFullyQualifiedName».getResource("«
										(relres.type as ResourceCreateStatement).fullyQualifiedName»"));
									return singlelist;
									«ELSE»
									return «relres.type.relResFullyQualifiedName».getAll();
									«ENDIF»
								}
							},
							new SimpleChoiceFrom<RelevantResources, «relres.type.fullyQualifiedName», Parameters>
							(
								«relres.select.compileChoiceFrom(op, relres.type.fullyQualifiedName, relres.name, relres.type.fullyQualifiedName)»,
								null
							),
							new CombinationalChoiceFrom.Setter<RelevantResources, «relres.type.fullyQualifiedName»>()
							{
								public void set(RelevantResources set, «relres.type.fullyQualifiedName» resource)
								{
									set.«relres.name» = resource;
								}
							}
						)
					);

				«ENDFOR»
				}

			«ELSE»
			«FOR relres : op.relevantresources»
			«IF relres.type instanceof ResourceCreateStatement»
			// just checker «relres.name»
			private static SimpleChoiceFrom.Checker<RelevantResources, «relres.type.relResFullyQualifiedName», Parameters> «
				relres.name»Checker = «relres.select.compileChoiceFrom(op, relres.type.relResFullyQualifiedName,
						relres.name, relres.type.relResFullyQualifiedName)»;

			«ELSE»
			// choice «relres.name»
			private static SimpleChoiceFrom<RelevantResources, «relres.type.relResFullyQualifiedName», Parameters> «relres.name»Choice =
				new SimpleChoiceFrom<RelevantResources, «relres.type.relResFullyQualifiedName», Parameters>
				(
					«relres.select.compileChoiceFrom(op, relres.type.relResFullyQualifiedName, relres.name, relres.type.relResFullyQualifiedName)»,
					«relres.selectmethod.compileChoiceMethod(op.name, relres.type.relResFullyQualifiedName)»
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
							«FOR m: op.defaultMethods.filter[m | m.name == "begin"]»
								«m.body.compilePatternAction»
							«ENDFOR»
						}
					};

			private static Converter<RelevantResources, Parameters> end =
					new Converter<RelevantResources, Parameters>()
					{
						@Override
						public void run(RelevantResources resources, Parameters parameters)
						{
							«FOR m: op.defaultMethods.filter[m | m.name == "end"]»
								«m.body.compilePatternAction»
							«ENDFOR»
						}
					};

			public static boolean findResources(Parameters parameters)
			{
				staticResources.clear();

				«IF op.combinational != null»
				if(!choice.find(parameters))
					return false;

				«ELSE»
				«FOR relres : op.relevantresources»
				«IF relres.type instanceof ResourceCreateStatement»
					if(!«relres.name»Checker.check(staticResources, staticResources.«relres.name», parameters))
						return false;

				«ELSE»
					staticResources.«relres.name» = «relres.name»Choice.find(staticResources, «relres.type.fullyQualifiedName».getAll(), parameters);

					if(staticResources.«relres.name» == null)
						return false;

				«ENDIF»
				«ENDFOR»
				«ENDIF»
				return true;
			}

			private static double duration(RelevantResources resources, Parameters parameters)
			{
				«FOR m: op.defaultMethods.filter[m | m.name == "duration"]»
					«m.body.compileStatementContext(
						(new LocalContext).populateFromPattern(op)
					)»
				«ENDFOR»
			}

			public static «op.name» executeRule(Parameters parameters)
			{
				RelevantResources resources = staticResources;

				begin.run(resources, parameters);


				«op.name» instance = new «op.name»(
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
						"«op.fullyQualifiedName».createdResources",
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
						"«op.fullyQualifiedName»",
						null);
			}

			@Override
			public int[] getRelevantInfo()
			{
				return new int[]
				{
					«FOR i : 0 ..< op.relevantresources.size»
						resources.«op.relevantresources.get(i).name».getNumber()«
							IF i < op.relevantresources.size - 1»,«ENDIF»
					«ENDFOR»
				};
			}

			public static final JSONObject structure = new JSONObject()
				.put("name", "«op.fullyQualifiedName»")
				.put("type", "operation")
				.put
				(
					"relevant_resources", new JSONArray()
						«FOR r : op.relevantresources»
							.put
							(
								new JSONObject()
									.put("name", "«r.name»")
									.put("type", "«
										IF r.type instanceof ResourceCreateStatement
											»«(r.type as ResourceCreateStatement).type.fullyQualifiedName»«
										ELSE
											»«(r.type as ResourceType).fullyQualifiedName»«
										ENDIF»")
							)
						«ENDFOR»
				);
		}
		'''
	}

	def static compileChoiceFrom(PatternSelectLogic cf, Pattern pattern, String resource, String relres, String relrestype)
	{
		val havecf = cf != null && !cf.any;

		var List<String> relreslist;
		var List<String> relrestypes;

		relreslist = pattern.relevantresources.map[r | r.name]
		relrestypes = pattern.relevantresources.map[r |
			if(r.type instanceof ResourceType)
				(r.type as ResourceType).fullyQualifiedName
			else
				(r.type as ResourceCreateStatement).type.fullyQualifiedName
		]

		return
			'''
			new SimpleChoiceFrom.Checker<RelevantResources, «resource», Parameters>()
			{
				@Override
				public boolean check(RelevantResources resources, «resource» «relres», Parameters parameters)
				{
					return «IF havecf»(«
						cf.logic.compileExpressionContext((new LocalContext).populateFromPattern(pattern)).value.
							replaceAll("resources." + relres + ".", relres + ".")»)«ELSE»true«ENDIF»«
										FOR i : 0 ..< relreslist.size»«IF relres != relreslist.get(i) && relrestype ==
											relrestypes.get(i)»«"\t"»&& «relres» != resources.«relreslist.get(i)»«ENDIF»«ENDFOR»;
				}
			}'''
	}

	def private static compileChoiceMethod(PatternSelectMethod cm, String pattern, String resource)
	{
		if(cm == null || cm.first)
			return "null"
		else
		{
			var EObject pat;
			var String relres = "@NOPE";
			if(cm.eContainer instanceof Pattern)
				pat = cm.eContainer
			else
			{
				pat = cm.eContainer.eContainer
				relres = (if(cm.eContainer instanceof RelevantResource) (cm.eContainer as RelevantResource).name
					else (cm.eContainer as RelevantResource).name)
			}

			val context = (new LocalContext).populateFromPattern(pat as Pattern)

			val expr = cm.expression.compileExpressionContext(context).value
			return
				'''
				new SimpleChoiceFrom.ChoiceMethod<RelevantResources, «resource», Parameters>()
				{
					@Override
					public int compare(«resource» x, «resource» y)
					{
						if(«expr.replaceAll("resources." + relres + ".", "x.")» «IF cm.withtype.literal
							== "with_min"»<«ELSE»>«ENDIF» «expr.replaceAll("resources." + relres + ".", "y.")»)
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