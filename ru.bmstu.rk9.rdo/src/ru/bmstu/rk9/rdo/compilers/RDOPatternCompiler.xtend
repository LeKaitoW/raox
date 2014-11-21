package ru.bmstu.rk9.rdo.compilers

import java.util.List

import org.eclipse.emf.ecore.EObject

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*
import static extension ru.bmstu.rk9.rdo.generator.RDOExpressionCompiler.*
import static extension ru.bmstu.rk9.rdo.generator.RDOStatementCompiler.*

import static extension ru.bmstu.rk9.rdo.compilers.RDOResourceTypeCompiler.*

import ru.bmstu.rk9.rdo.generator.LocalContext

import ru.bmstu.rk9.rdo.rdo.ResourceType

import ru.bmstu.rk9.rdo.rdo.ResourceDeclaration

import ru.bmstu.rk9.rdo.rdo.Pattern
import ru.bmstu.rk9.rdo.rdo.PatternParameter
import ru.bmstu.rk9.rdo.rdo.PatternChoiceFrom
import ru.bmstu.rk9.rdo.rdo.PatternChoiceMethod
import ru.bmstu.rk9.rdo.rdo.PatternConvertStatus
import ru.bmstu.rk9.rdo.rdo.Event
import ru.bmstu.rk9.rdo.rdo.Operation
import ru.bmstu.rk9.rdo.rdo.OperationConvert
import ru.bmstu.rk9.rdo.rdo.Rule
import ru.bmstu.rk9.rdo.rdo.RuleConvert


class RDOPatternCompiler
{
	def public static compileEvent(Event evn, String filename)
	{
		'''
		package «filename»;

		import ru.bmstu.rk9.rdo.lib.json.*;

		import ru.bmstu.rk9.rdo.lib.*;
		@SuppressWarnings("all")

		public class «evn.name» implements Event, Pattern
		{
			private static final String name =  "«evn.fullyQualifiedName»";

			@Override
			public String getName()
			{
				return name;
			}

			private static class RelevantResources
			{
				«FOR r : evn.relevantresources»
					«IF r.type instanceof ResourceType»
						public «r.type.fullyQualifiedName» «r.name»;
					«ELSE»
						public «(r.type as ResourceDeclaration).reference.fullyQualifiedName» «r.name»;
					«ENDIF»
				«ENDFOR»

				public RelevantResources init()
				{
					«FOR r : evn.relevantresources»
						«IF r.type instanceof ResourceDeclaration»
							«r.name» = «(r.type as ResourceDeclaration).reference.fullyQualifiedName
								».getResource("«(r.type as ResourceDeclaration).fullyQualifiedName»");
						«ELSE»
							«IF r.rule.literal == "Create"»
								«r.name» = new «r.type.fullyQualifiedName»(«(r.type as ResourceType).parameters.size.compileAllDefault»);
							«ENDIF»
						«ENDIF»
					«ENDFOR»

					return this;
				}
			}

			private static RelevantResources staticResources = new RelevantResources();

			private static class Parameters
			{
				«IF evn.parameters.size > 0»
				«FOR p : evn.parameters»
					public «p.type.compileType» «p.name»«p.type.getDefault»;
				«ENDFOR»

				public Parameters(«evn.parameters.compilePatternParameters»)
				{
					«FOR parameter : evn.parameters»
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

			private static Converter<RelevantResources, Parameters> converter =
				new Converter<RelevantResources, Parameters>()
				{
					@Override
					public void run(RelevantResources resources, Parameters parameters)
					{
						«FOR e : evn.algorithms»
							«e.compileConvert(0)»
						«ENDFOR»
					}
				};

			@Override
			public void run()
			{
				converter.run(staticResources.init(), parameters);
				Database db = Simulator.getDatabase();
				«IF evn.relevantresources.filter[t | t.rule.literal == "Create"].size > 0»

					// add resources
					«FOR r : evn.relevantresources.filter[t | t.rule.literal == "Create"]»
						staticResources.«r.name».register();
					«ENDFOR»
				«ENDIF»

				// database operations
				db.addEventEntry(Database.PatternType.EVENT, this);

				«FOR r : evn.relevantresources.filter[t |
						t.rule != PatternConvertStatus.NOCHANGE && t.rule != PatternConvertStatus.NONEXIST]»
					db.addResourceEntry(«r.rule.compileResourceTraceStatus», staticResources.«r.name
						», "«evn.fullyQualifiedName».«r.name»");
				«ENDFOR»
			}

			public «evn.name»(double time«IF evn.parameters.size > 0», «ENDIF»«evn.parameters.compilePatternParameters»)
			{
				this.time = time;
				this.parameters = new Parameters(«evn.parameters.compilePatternParametersCall»);
			}

			@Override
			public int[] getRelevantInfo()
			{
				return new int[]
				{
					«FOR i : 0 ..< evn.relevantresources.size»
						staticResources.«evn.relevantresources.get(i).name».getNumber()«
							IF i < evn.relevantresources.size - 1»,«ENDIF»
					«ENDFOR»
				};
			}

			public static final JSONObject structure = new JSONObject()
				.put("name", "«evn.fullyQualifiedName»")
				.put("type", "event")
				.put
				(
					"relevant_resources", new JSONArray()
						«FOR r : evn.relevantresources»
							.put
							(
								new JSONObject()
									.put("name", "«r.name»")
									.put("type", "«
										IF r.type instanceof ResourceDeclaration
											»«(r.type as ResourceDeclaration).reference.fullyQualifiedName»«
										ELSE
											»«(r.type as ResourceType).fullyQualifiedName»«
										ENDIF»")
									.put("convert_event", "«r.rule.literal»")
							)
						«ENDFOR»
				);
		}
		'''
	}

	def private static compileResourceTraceStatus(PatternConvertStatus status)
	{
		switch status
		{
			case CREATE: "Database.ResourceEntryType.CREATED"
			case ERASE : "Database.ResourceEntryType.ERASED"
			case KEEP  : "Database.ResourceEntryType.ALTERED"
			default    : "null"
		}
	}

	def public static compileRule(Rule rule, String filename)
	{
		'''
		package «filename»;

		import ru.bmstu.rk9.rdo.lib.json.*;

		import ru.bmstu.rk9.rdo.lib.*;
		@SuppressWarnings("all")

		public class «rule.name» implements Pattern
		{
			private static final String name = "«filename».«rule.name»";

			@Override
			public String getName()
			{
				return name;
			}

			private static class RelevantResources
			{
				«FOR r : rule.relevantresources.filter[res | res.rule.literal != "Create"]»
					«IF r.type instanceof ResourceType»
						public «r.type.fullyQualifiedName» «r.name»;
					«ELSE»
						public «(r.type as ResourceDeclaration).reference.fullyQualifiedName» «r.name»;
					«ENDIF»
				«ENDFOR»
				«FOR r : rule.relevantresources.filter[res | res.rule.literal == "Create"]»
					public «r.type.fullyQualifiedName» «r.name»;
				«ENDFOR»

				public RelevantResources copy()
				{
					RelevantResources clone = new RelevantResources();

					«FOR r : rule.relevantresources.filter[res | res.rule.literal != "Create"]»
						clone.«r.name» = this.«r.name»;
					«ENDFOR»

					return clone;
				}

				public void clear()
				{
					«FOR r : rule.relevantresources.filter[res | res.rule.literal != "Create"]»
					«IF r.type instanceof ResourceDeclaration»
						this.«r.name» = «(r.type as ResourceDeclaration).reference.fullyQualifiedName
							».getResource("«(r.type as ResourceDeclaration).fullyQualifiedName»");
					«ELSE»
						this.«r.name» = null;
					«ENDIF»
					«ENDFOR»
				}
			}

			private static RelevantResources staticResources = new RelevantResources();
			private RelevantResources instanceResources;

			public static class Parameters
			{
				«IF rule.parameters.size > 0»
				«FOR p : rule.parameters»
					public «p.type.compileType» «p.name»«p.type.getDefault»;
				«ENDFOR»

				public Parameters(«rule.parameters.compilePatternParameters»)
				{
					«FOR p : rule.parameters»
						if («p.name» != null)
							this.«p.name» = «p.name»;
					«ENDFOR»
				}
				«ENDIF»
			}

			private Parameters parameters;

			private «rule.name»(RelevantResources resources)
			{
				this.instanceResources = resources;
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

							«FOR relres : rule.relevantresources.filter[r | r.rule.literal != "Create"]»
								clone.«relres.name» = set.«relres.name»;
							«ENDFOR»

							return clone;
						}

						@Override
						public void apply(RelevantResources origin, RelevantResources set)
						{
							«FOR relres : rule.relevantresources.filter[r | r.rule.literal != "Create"]»
								origin.«relres.name» = set.«relres.name»;
							«ENDFOR»
						}
					}
				);
				static
				{
				«FOR rc : rule.algorithms.filter[r | r.relres.rule.literal != "Create"]»
					choice.addFinder
					(
						new CombinationalChoiceFrom.Finder<RelevantResources, «rc.relres.type.relResFullyQualifiedName», Parameters>
						(
							new CombinationalChoiceFrom.Retriever<«rc.relres.type.relResFullyQualifiedName»>()
							{
								@Override
								public java.util.Collection<«rc.relres.type.relResFullyQualifiedName»> getResources()
								{
									«IF rc.relres.type instanceof ResourceDeclaration»
									java.util.LinkedList<«rc.relres.type.relResFullyQualifiedName»> singlelist =
										new java.util.LinkedList<«rc.relres.type.relResFullyQualifiedName»>();
									singlelist.add(«rc.relres.type.relResFullyQualifiedName».getResource("«
										(rc.relres.type as ResourceDeclaration).fullyQualifiedName»"));
									return singlelist;
									«ELSE»
									return «rc.relres.type.relResFullyQualifiedName».get«
										IF rc.relres.rule.literal == "Erase"»Temporary«ELSE»All«ENDIF»();
									«ENDIF»
								}
							},
							new SimpleChoiceFrom<RelevantResources, «rc.relres.type.relResFullyQualifiedName», Parameters>
							(
								«rc.choicefrom.compileChoiceFrom(rule, rc.relres.type.relResFullyQualifiedName, rc.relres.name, rc.relres.type.relResFullyQualifiedName)»,
								null
							),
							new CombinationalChoiceFrom.Setter<RelevantResources, «rc.relres.type.relResFullyQualifiedName»>()
							{
								public void set(RelevantResources set, «rc.relres.type.relResFullyQualifiedName» resource)
								{
									set.«rc.relres.name» = resource;
								}
							}
						)
					);

				«ENDFOR»
				}

			«ELSE»
			«FOR rc : rule.algorithms.filter[r | r.relres.rule.literal != "Create"]»
			«IF rc.relres.type instanceof ResourceDeclaration»
			// just checker «rc.relres.name»
			private static SimpleChoiceFrom.Checker<RelevantResources, «rc.relres.type.relResFullyQualifiedName», Parameters> «
				rc.relres.name»Checker = «rc.choicefrom.compileChoiceFrom(rule, rc.relres.type.relResFullyQualifiedName,
						rc.relres.name, rc.relres.type.relResFullyQualifiedName)»;

			«ELSE»
			// choice «rc.relres.name»
			private static SimpleChoiceFrom<RelevantResources, «rc.relres.type.relResFullyQualifiedName», Parameters> «rc.relres.name»Choice =
				new SimpleChoiceFrom<RelevantResources, «rc.relres.type.relResFullyQualifiedName», Parameters>
				(
					«rc.choicefrom.compileChoiceFrom(rule, rc.relres.type.relResFullyQualifiedName, rc.relres.name, rc.relres.type.relResFullyQualifiedName)»,
					«rc.choicemethod.compileChoiceMethod(rule.name, rc.relres.type.relResFullyQualifiedName)»
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
							«FOR e : rule.algorithms.filter[r | r.haverule]»
								«e.compileConvert(0)»
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
				«FOR rc : rule.algorithms.filter[r | r.relres.rule.literal != "Create"]»
				«IF rc.relres.type instanceof ResourceDeclaration»
					if (!«rc.relres.name»Checker.check(staticResources, staticResources.«rc.relres.name», parameters))
						return false;

				«ELSE»
					staticResources.«rc.relres.name» = «rc.relres.name»Choice.find(staticResources, «rc.relres.type.fullyQualifiedName».get«
						IF rc.relres.rule.literal == "Erase"»Temporary«ELSE»All«ENDIF»(), parameters);

					if (staticResources.«rc.relres.name» == null)
						return false;

				«ENDIF»
				«ENDFOR»
				«ENDIF»
				return true;
			}

			public static «rule.name» executeRule(Parameters parameters)
			{
				RelevantResources resources = staticResources.copy();

				Database db = Simulator.getDatabase();
				«IF rule.relevantresources.filter[t | t.rule.literal == "Create"].size > 0»
					// create resources
					«FOR r : rule.relevantresources.filter[t |t.rule.literal == "Create"]»
						resources.«r.name» = new «(r.type as ResourceType).fullyQualifiedName»(«
							(r.type as ResourceType).parameters.size.compileAllDefault»);
						resources.«r.name».register();
					«ENDFOR»

				«ENDIF»
				«IF rule.relevantresources.filter[t | t.rule.literal == "Erase"].size > 0»
					// erase resources
					«FOR r : rule.relevantresources.filter[t |t.rule.literal == "Erase"]»
						«(r.type as ResourceType).fullyQualifiedName».eraseResource(resources.«r.name»);
					«ENDFOR»

				«ENDIF»
				rule.run(resources, parameters);

				return new «rule.name»(resources);
			}

			@Override
			public void addResourceEntriesToDatabase(Pattern.ExecutedFrom executedFrom)
			{
				Database db = Simulator.getDatabase();

				«FOR r : rule.relevantresources.filter[t |
						t.rule != PatternConvertStatus.NOCHANGE && t.rule != PatternConvertStatus.NONEXIST]»
					db.addResourceEntry
					(
						executedFrom.resourceSpecialStatus != null
							? executedFrom.resourceSpecialStatus
							: «r.rule.compileResourceTraceStatus»,
						instanceResources.«r.name»,
						"«rule.fullyQualifiedName».«r.name»"
					);
				«ENDFOR»
			}

			@Override
			public int[] getRelevantInfo()
			{
				return new int[]
				{
					«FOR i : 0 ..< rule.relevantresources.size»
						instanceResources.«rule.relevantresources.get(i).name».getNumber()«
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
										IF r.type instanceof ResourceDeclaration
											»«(r.type as ResourceDeclaration).reference.fullyQualifiedName»«
										ELSE
											»«(r.type as ResourceType).fullyQualifiedName»«
										ENDIF»")
									.put("convert_rule", "«r.rule.literal»")
							)
						«ENDFOR»
				);
		}
		'''
	}

	def public static compileOperation(Operation op, String filename)
	{
		'''
		package «filename»;

		import ru.bmstu.rk9.rdo.lib.json.*;

		import ru.bmstu.rk9.rdo.lib.*;
		@SuppressWarnings("all")

		public class «op.name» implements Event, Pattern
		{
			private static final String name = "«filename».«op.name»";

			@Override
			public String getName()
			{
				return name;
			}

			private static class RelevantResources
			{
				«FOR r : op.relevantresources.filter[res | res.begin.literal != "Create" && res.end.literal != "Create"]»
					«IF r.type instanceof ResourceType»
						public «r.type.fullyQualifiedName» «r.name»;
					«ELSE»
						public «(r.type as ResourceDeclaration).reference.fullyQualifiedName» «r.name»;
					«ENDIF»
				«ENDFOR»
				«FOR r : op.relevantresources.filter[res | res.begin.literal == "Create" || res.end.literal == "Create"]»
					public «r.type.fullyQualifiedName» «r.name»;
				«ENDFOR»

				public RelevantResources copy()
				{
					RelevantResources clone = new RelevantResources();

					«FOR r : op.relevantresources.filter[res | res.begin.literal != "Create" && res.end.literal != "Create"]»
						clone.«r.name» = this.«r.name»;
					«ENDFOR»

					return clone;
				}

				public void clear()
				{
					«FOR r : op.relevantresources.filter[res | res.begin.literal != "Create" && res.end.literal != "Create"]»
					«IF r.type instanceof ResourceDeclaration»
						this.«r.name» = «(r.type as ResourceDeclaration).reference.fullyQualifiedName
							».getResource("«(r.type as ResourceDeclaration).fullyQualifiedName»");
					«ELSE»
						this.«r.name» = null;
					«ENDIF»
					«ENDFOR»
				}
			}

			private static RelevantResources staticResources = new RelevantResources();
			private RelevantResources instanceResources;

			public static class Parameters
			{
				«IF op.parameters.size > 0»
				«FOR p : op.parameters»
					public «p.type.compileType» «p.name»«p.type.getDefault»;
				«ENDFOR»

				public Parameters(«op.parameters.compilePatternParameters»)
				{
					«FOR p : op.parameters»
						if («p.name» != null)
							this.«p.name» = «p.name»;
					«ENDFOR»
				}
				«ENDIF»
			}

			private Parameters parameters;

			private «op.name»(double time, RelevantResources resources, Parameters parameters)
			{
				this.time = time;
				this.instanceResources = resources;
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

							«FOR relres : op.relevantresources.filter[r | r.begin.literal != "Create" && r.end.literal != "Create"]»
								clone.«relres.name» = set.«relres.name»;
							«ENDFOR»

							return clone;
						}

						@Override
						public void apply(RelevantResources origin, RelevantResources set)
						{
							«FOR relres : op.relevantresources.filter[r | r.begin.literal != "Create" && r.end.literal != "Create"]»
								origin.«relres.name» = set.«relres.name»;
							«ENDFOR»
						}
					}
				);
				static
				{
				«FOR rc : op.algorithms.filter[r | r.relres.begin.literal != "Create" && r.relres.end.literal != "Create"]»
					choice.addFinder
					(
						new CombinationalChoiceFrom.Finder<RelevantResources, «rc.relres.type.fullyQualifiedName», Parameters>
						(
							new CombinationalChoiceFrom.Retriever<«rc.relres.type.fullyQualifiedName»>()
							{
								@Override
								public java.util.Collection<«rc.relres.type.fullyQualifiedName»> getResources()
								{
									«IF rc.relres.type instanceof ResourceDeclaration»
									java.util.LinkedList<«rc.relres.type.relResFullyQualifiedName»> singlelist =
										new java.util.LinkedList<«rc.relres.type.relResFullyQualifiedName»>();
									singlelist.add(«rc.relres.type.relResFullyQualifiedName».getResource("«
										(rc.relres.type as ResourceDeclaration).fullyQualifiedName»"));
									return singlelist;
									«ELSE»
									return «rc.relres.type.relResFullyQualifiedName».get«
										IF rc.relres.begin.literal == "Erase" || rc.relres.end.literal == "Erase"»Temporary«ELSE»All«ENDIF»();
									«ENDIF»
								}
							},
							new SimpleChoiceFrom<RelevantResources, «rc.relres.type.fullyQualifiedName», Parameters>
							(
								«rc.choicefrom.compileChoiceFrom(op, rc.relres.type.fullyQualifiedName, rc.relres.name, rc.relres.type.fullyQualifiedName)»,
								null
							),
							new CombinationalChoiceFrom.Setter<RelevantResources, «rc.relres.type.fullyQualifiedName»>()
							{
								public void set(RelevantResources set, «rc.relres.type.fullyQualifiedName» resource)
								{
									set.«rc.relres.name» = resource;
								}
							}
						)
					);

				«ENDFOR»
				}

			«ELSE»
			«FOR rc : op.algorithms.filter[r | r.relres.begin.literal != "Create" && r.relres.end.literal != "Create"]»
			«IF rc.relres.type instanceof ResourceDeclaration»
			// just checker «rc.relres.name»
			private static SimpleChoiceFrom.Checker<RelevantResources, «rc.relres.type.relResFullyQualifiedName», Parameters> «
				rc.relres.name»Checker = «rc.choicefrom.compileChoiceFrom(op, rc.relres.type.relResFullyQualifiedName,
						rc.relres.name, rc.relres.type.relResFullyQualifiedName)»;

			«ELSE»
			// choice «rc.relres.name»
			private static SimpleChoiceFrom<RelevantResources, «rc.relres.type.relResFullyQualifiedName», Parameters> «rc.relres.name»Choice =
				new SimpleChoiceFrom<RelevantResources, «rc.relres.type.relResFullyQualifiedName», Parameters>
				(
					«rc.choicefrom.compileChoiceFrom(op, rc.relres.type.relResFullyQualifiedName, rc.relres.name, rc.relres.type.relResFullyQualifiedName)»,
					«rc.choicemethod.compileChoiceMethod(op.name, rc.relres.type.relResFullyQualifiedName)»
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
							«FOR e : op.algorithms.filter[r | r.havebegin]»
								«e.compileConvert(0)»
							«ENDFOR»
						}
					};

			private static Converter<RelevantResources, Parameters> end =
					new Converter<RelevantResources, Parameters>()
					{
						@Override
						public void run(RelevantResources resources, Parameters parameters)
						{
							«FOR e : op.algorithms.filter[r | r.haveend]»
								«e.compileConvert(1)»
							«ENDFOR»
						}
					};

			public static boolean findResources(Parameters parameters)
			{
				staticResources.clear();

				«IF op.combinational != null»
				if (!choice.find(parameters))
					return false;

				«ELSE»
				«FOR rc : op.algorithms.filter[r | r.relres.begin.literal != "Create" && r.relres.end.literal != "Create"]»
				«IF rc.relres.type instanceof ResourceDeclaration»
					if (!«rc.relres.name»Checker.check(staticResources, staticResources.«rc.relres.name», parameters))
						return false;

				«ELSE»
					staticResources.«rc.relres.name» = «rc.relres.name»Choice.find(staticResources, «rc.relres.type.fullyQualifiedName».get«
						IF rc.relres.begin.literal == "Erase" || rc.relres.end.literal == "Erase"»Temporary«ELSE»All«ENDIF»(), parameters);

					if (staticResources.«rc.relres.name» == null)
						return false;

				«ENDIF»
				«ENDFOR»
				«ENDIF»
				return true;
			}

			public static «op.name» executeRule(Parameters parameters)
			{
				RelevantResources resources = staticResources.copy();

				Database db = Simulator.getDatabase();
				«IF op.relevantresources.filter[t | t.begin.literal == "Create"].size > 0»
					// create resources
					«FOR r : op.relevantresources.filter[t |t.begin.literal == "Create"]»
						resources.«r.name» = new «(r.type as ResourceType).fullyQualifiedName»(«
							(r.type as ResourceType).parameters.size.compileAllDefault»);
						resources.«r.name».register();
					«ENDFOR»

				«ENDIF»
				«IF op.relevantresources.filter[t | t.begin.literal == "Erase" || t.end.literal == "Erase"].size > 0»
					// erase resources
					«FOR r : op.relevantresources.filter[t |t.begin.literal == "Erase" || t.end.literal == "Erase"]»
						«(r.type as ResourceType).fullyQualifiedName».eraseResource(resources.«r.name»);
					«ENDFOR»

				«ENDIF»
				begin.run(resources, parameters);

				«op.name» instance = new «op.name»(Simulator.getTime() + «
					op.time.compileExpressionContext((new LocalContext).populateFromOperation(op)).value», resources, parameters);

				Simulator.pushEvent(instance);

				return instance;
			}

			@Override
			public void addResourceEntriesToDatabase(Pattern.ExecutedFrom executedFrom)
			{
				Database db = Simulator.getDatabase();

				«FOR r : op.relevantresources.filter[t |
						t.begin != PatternConvertStatus.NOCHANGE && t.begin != PatternConvertStatus.NONEXIST]»
					db.addResourceEntry
					(
						executedFrom.resourceSpecialStatus != null
							? executedFrom.resourceSpecialStatus
							: «r.begin.compileResourceTraceStatus»,
						instanceResources.«r.name»,
						"«op.fullyQualifiedName».«r.name»"
					);
				«ENDFOR»
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
				«IF op.relevantresources.filter[t | t.end.literal == "Create"].size > 0»
					// create resources
					«FOR r : op.relevantresources.filter[t |t.end.literal == "Create"]»
						instanceResources.«r.name» = new «(r.type as ResourceType).fullyQualifiedName»(«
							(r.type as ResourceType).parameters.size.compileAllDefault»);
						instanceResources.«r.name».register();
						
					«ENDFOR»

				«ENDIF»
				end.run(instanceResources, parameters);

				// database operations
				db.addEventEntry(Database.PatternType.OPERATION_END, this);

				«FOR r : op.relevantresources.filter[t |
						t.end != PatternConvertStatus.NOCHANGE && t.end != PatternConvertStatus.NONEXIST]»
					db.addResourceEntry(«r.end.compileResourceTraceStatus», instanceResources.«r.name
						», "«op.fullyQualifiedName».«r.name»");
				«ENDFOR»
			}

			@Override
			public int[] getRelevantInfo()
			{
				return new int[]
				{
					«FOR i : 0 ..< op.relevantresources.size»
						instanceResources.«op.relevantresources.get(i).name».getNumber()«
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
										IF r.type instanceof ResourceDeclaration
											»«(r.type as ResourceDeclaration).reference.fullyQualifiedName»«
										ELSE
											»«(r.type as ResourceType).fullyQualifiedName»«
										ENDIF»")
									.put("convert_begin", "«r.begin.literal»")
									.put("convert_end", "«r.end.literal»")
							)
						«ENDFOR»
				);
		}
		'''
	}

	def public static compileChoiceFrom(PatternChoiceFrom cf, Pattern pattern, String resource, String relres, String relrestype)
	{
		val havecf = cf != null && !cf.nocheck;

		var List<String> relreslist;
		var List<String> relrestypes;

		switch pattern
		{
			Operation:
			{
				relreslist = pattern.relevantresources.map[r | r.name]
				relrestypes = pattern.relevantresources.map[r |
					if (r.type instanceof ResourceType)
						(r.type as ResourceType).fullyQualifiedName
					else
						(r.type as ResourceDeclaration).reference.fullyQualifiedName
				]
			}

			Rule:
			{
				relreslist = pattern.relevantresources.map[r | r.name]
				relrestypes = pattern.relevantresources.map[r |
					if (r.type instanceof ResourceType)
						(r.type as ResourceType).fullyQualifiedName
					else
						(r.type as ResourceDeclaration).reference.fullyQualifiedName
				]
			}
		}

		return
			'''
			new SimpleChoiceFrom.Checker<RelevantResources, «resource», Parameters>()
			{
				@Override
				public boolean check(RelevantResources resources, «resource» «relres», Parameters parameters)
				{
					return «IF havecf»(«IF pattern instanceof Operation»«
						cf.logic.compileExpressionContext((new LocalContext).populateFromOperation(pattern as Operation)).value.
							replaceAll("resources." + relres + ".", relres + ".")»«	ELSE»«
								cf.logic.compileExpressionContext((new LocalContext).populateFromRule(pattern as Rule)).value.
									replaceAll("resources." + relres + ".", relres + ".")»«ENDIF»)«ELSE»true«ENDIF»«
										FOR i : 0 ..< relreslist.size»«IF relres != relreslist.get(i) && relrestype ==
											relrestypes.get(i)»«"\t"»&& «relres» != resources.«relreslist.get(i)»«ENDIF»«ENDFOR»;
				}
			}'''
	}

	def public static compileChoiceMethod(PatternChoiceMethod cm, String pattern, String resource)
	{
		if (cm == null || cm.first)
			return "null"
		else
		{
			var EObject pat;
			var String relres = "@NOPE";
			if(cm.eContainer instanceof Rule || cm.eContainer instanceof Operation)
				pat = cm.eContainer
			else
			{
				pat = cm.eContainer.eContainer
				relres = (if(cm.eContainer instanceof OperationConvert) (cm.eContainer as OperationConvert).relres.name
					else (cm.eContainer as RuleConvert).relres.name)
			}

			val context = (if(pat instanceof Rule) (new LocalContext).populateFromRule(pat as Rule)
				else (new LocalContext).populateFromOperation(pat as Operation))

			val expr = cm.expression.compileExpressionContext(context).value
			return
				'''
				new SimpleChoiceFrom.ChoiceMethod<RelevantResources, «resource», Parameters>()
				{
					@Override
					public int compare(«resource» x, «resource» y)
					{
						if («expr.replaceAll("resources." + relres + ".", "x.")» «IF cm.withtype.literal
							== "with_min"»<«ELSE»>«ENDIF» «expr.replaceAll("resources." + relres + ".", "y.")»)
							return -1;
						else
							return 1;
					}
				}'''
		}
	}

	def public static compilePatternParametersCall(List<PatternParameter> parameters)
	{
		'''«IF parameters.size > 0»«
			parameters.get(0).name»«
			FOR parameter : parameters.subList(1, parameters.size)», «
				parameter.name»«
			ENDFOR»«
		ENDIF»'''
	}

	def public static compilePatternParameters(List<PatternParameter> parameters)
	{
		'''«IF parameters.size > 0»«parameters.get(0).type.compileType» «
			parameters.get(0).name»«
			FOR parameter : parameters.subList(1, parameters.size)», «
				parameter.type.compileType» «
				parameter.name»«
			ENDFOR»«
		ENDIF»'''
	}
}