package ru.bmstu.rk9.rdo.generator

import java.util.List
import java.util.HashMap

import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet

import org.eclipse.xtext.generator.IFileSystemAccess

import static extension org.eclipse.xtext.xbase.lib.IteratorExtensions.*

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*
import static extension ru.bmstu.rk9.rdo.customizations.RDOQualifiedNameProvider.*
import static extension ru.bmstu.rk9.rdo.generator.RDOExpressionCompiler.*
import static extension ru.bmstu.rk9.rdo.generator.RDOStatementCompiler.*

import ru.bmstu.rk9.rdo.customizations.IMultipleResourceGenerator
import ru.bmstu.rk9.rdo.customizations.SMRSelectDialog

import ru.bmstu.rk9.rdo.rdo.RDOModel

import ru.bmstu.rk9.rdo.rdo.ResourceType
import ru.bmstu.rk9.rdo.rdo.ResourceTypeParameter
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterType
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterBasic
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterString
import ru.bmstu.rk9.rdo.rdo.RDOEnum

import ru.bmstu.rk9.rdo.rdo.ResourceDeclaration

import ru.bmstu.rk9.rdo.rdo.ConstantDeclaration

import ru.bmstu.rk9.rdo.rdo.Function
import ru.bmstu.rk9.rdo.rdo.FunctionParameter
import ru.bmstu.rk9.rdo.rdo.FunctionTable
import ru.bmstu.rk9.rdo.rdo.FunctionAlgorithmic
import ru.bmstu.rk9.rdo.rdo.FunctionList

import ru.bmstu.rk9.rdo.rdo.Pattern
import ru.bmstu.rk9.rdo.rdo.PatternParameter
import ru.bmstu.rk9.rdo.rdo.PatternChoiceFrom
import ru.bmstu.rk9.rdo.rdo.PatternChoiceMethod
import ru.bmstu.rk9.rdo.rdo.Operation
import ru.bmstu.rk9.rdo.rdo.Rule
import ru.bmstu.rk9.rdo.rdo.Event

import ru.bmstu.rk9.rdo.rdo.DecisionPoint
import ru.bmstu.rk9.rdo.rdo.DecisionPointPrior
import ru.bmstu.rk9.rdo.rdo.DecisionPointSome

import ru.bmstu.rk9.rdo.rdo.SimulationRun


class RDOGenerator implements IMultipleResourceGenerator
{
	override void doGenerate(Resource resource, IFileSystemAccess fsa)
	{}

	override void doGenerate(ResourceSet resources, IFileSystemAccess fsa)
	{
		//===== rdo_lib ====================================================================
		fsa.generateFile("rdo_lib/Simulator.java",                compileLibSimulator    ())
		fsa.generateFile("rdo_lib/PermanentResourceManager.java", compilePermanentManager())
		fsa.generateFile("rdo_lib/TemporaryResourceManager.java", compileTemporaryManager())
		fsa.generateFile("rdo_lib/SimpleChoiceFrom.java",         compileSimpleChoiceFrom())
		fsa.generateFile("rdo_lib/CombinationalChoiceFrom.java",  compileCommonChoiceFrom())
		fsa.generateFile("rdo_lib/Converter.java",                compileConverter       ())
		fsa.generateFile("rdo_lib/EventScheduler.java",           compileEventScheduler  ())
		fsa.generateFile("rdo_lib/DecisionPoint.java",            compileDecisionPoint   ())
		fsa.generateFile("rdo_lib/TerminateCondition.java",       compileTerminate       ())
		//==================================================================================

		val declarationList = new java.util.ArrayList<ResourceDeclaration>();
		for (resource : resources.resources)
			declarationList.addAll(resource.allContents.filter(typeof(ResourceDeclaration)).toIterable)

		val simulationList = new java.util.ArrayList<SimulationRun>();
		for (resource : resources.resources)
			simulationList.addAll(resource.allContents.filter(typeof(SimulationRun)).toIterable)

		var Resource resWithSMR
		switch simulationList.size
		{
			case 0:
				resWithSMR = null

			case 1:
				resWithSMR = simulationList.get(0).eResource

			default:
			{
				val options = new HashMap<String, Resource>()
				for (s : simulationList)
					options.put(s.eResource.URI.lastSegment, s.eResource)
				resWithSMR = options.get(options.keySet.toList.get(SMRSelectDialog.invoke(options.keySet.toList)))
			}
		}

		for (resource : resources.resources)
			if (resource.contents.head != null)
			{
				val filename = (resource.contents.head as RDOModel).filenameFromURI

				for (e : resource.allContents.toIterable.filter(typeof(ResourceType)))
					fsa.generateFile(filename + "/" + e.name + ".java", e.compileResourceType(filename,
						declarationList.filter[r | r.reference.fullyQualifiedName == e.fullyQualifiedName]))

				for (e : resource.allContents.toIterable.filter(typeof(ConstantDeclaration)))
					fsa.generateFile(filename + "/" + e.name + ".java", e.compileConstant(filename))

				for (e : resource.allContents.toIterable.filter(typeof(Function)))
					fsa.generateFile(filename + "/" + e.name + ".java", e.compileFunction(filename))

				for (e : resource.allContents.toIterable.filter(typeof(Operation)))
					fsa.generateFile(filename + "/" + e.name + ".java", e.compileOperation(filename))

				for (e : resource.allContents.toIterable.filter(typeof(Rule)))
					fsa.generateFile(filename + "/" + e.name + ".java", e.compileRule(filename))

				for (e : resource.allContents.toIterable.filter(typeof(Event)))
					fsa.generateFile(filename + "/" + e.name + ".java", e.compileEvent(filename))

				for (e : resource.allContents.toIterable.filter(typeof(DecisionPoint)))
					fsa.generateFile(filename + "/" + e.name + ".java", e.compileDecisionPoint(filename))
			}

		fsa.generateFile("rdo_model/MainClass.java", compileMain(resources, resWithSMR))
	}

	def compileMain(ResourceSet rs, Resource smr)
	{
		'''
		package rdo_model;

		public class MainClass
		{
			public static void main(String[] args)
			{
				long startTime = System.currentTimeMillis();

				System.out.println(" === RDO-Simulator ===\n");
				System.out.println("   Project «RDONaming.getProjectName(rs.resources.get(0).URI)»");
				System.out.println("      Source files are «rs.resources.map[r | r.contents.head.nameGeneric].toString»\n");

				«IF smr != null»«FOR c :smr.allContents.filter(typeof(SimulationRun)).head.commands»
					«c.compileStatement»
				«ENDFOR»«ENDIF»

				«FOR r : rs.resources»
				«FOR c : r.allContents.filter(typeof(DecisionPoint)).toIterable»
					new «c.fullyQualifiedName»();
				«ENDFOR»
				«ENDFOR»

				System.out.println("   Started model");

				int result = rdo_lib.Simulator.run();

				if (result == 1)
					System.out.println("\n   Stopped by terminate condition");

				if (result == 0)
					System.out.println("\n   Stopped (no more events)");

				System.out.println("\n   Finished model in " + String.valueOf((System.currentTimeMillis() - startTime)/1000.0) + "s");
			}
		}
		'''
	}

	def compileConstant(ConstantDeclaration con, String filename)
	{
		'''
		package «filename»;

		public class «con.name»
		{
			public static final «con.type.compileType» value = «con.value.compileExpression»;
			«IF con.type instanceof RDOEnum»

				public enum «(con.type as RDOEnum).getEnumParentName(false)»_enum
				{
					«(con.type as RDOEnum).makeEnumBody»
				}«
			ENDIF»
		}
		'''
	}

	def withFirstUpper(String s)
	{
		return Character.toUpperCase(s.charAt(0)) + s.substring(1)
	}

	def compileResourceType(ResourceType rtp, String filename, Iterable<ResourceDeclaration> instances)
	{
		'''
		package «filename»;

		public class «rtp.name»
		{
			private final static rdo_lib.«rtp.type.literal.withFirstUpper
				»ResourceManager<«rtp.name»> manager = new rdo_lib.«rtp.type.literal.withFirstUpper»ResourceManager<«rtp.name»>();

			public static rdo_lib.«rtp.type.literal.withFirstUpper»ResourceManager<«rtp.name»> getManager()
			{
				return manager;
			}

			«IF rtp.eAllContents.filter(typeof(RDOEnum)).toList.size > 0»// ENUMS«ENDIF»
			«FOR e : rtp.eAllContents.toIterable.filter(typeof(RDOEnum))»
				public enum «e.getEnumParentName(false)»_enum
				{
					«e.makeEnumBody»
				}

			«ENDFOR»
			«FOR parameter : rtp.parameters»
				public «parameter.type.compileType» «parameter.name»«parameter.type.getDefault»;
			«ENDFOR»

			public «rtp.name»(«rtp.parameters.compileResourceTypeParameters»)
			{
				«FOR parameter : rtp.parameters»
					if («parameter.name» != null)
						this.«parameter.name» = «parameter.name»;
				«ENDFOR»
			}
			«FOR r : instances»

				public static final «rtp.name» «r.name» = new «rtp.name»(«
					if (r.parameters != null) r.parameters.compileExpression else ""»);
				static
				{
					«rtp.name».getManager().addResource("«r.name»", «r.name»);
				}
			«ENDFOR»
		}
		'''
	}

	def makeEnumBody(RDOEnum e)
	{
		var flag = false
		var body = ""

		for (i : e.enums)
		{
			if (flag)
				body = body + ", "
			body = body + i.name
			flag = true
		}
		return body
	}

	def compileFunction(Function fun, String filename)
	{
		'''
		package «filename»;

		public class «fun.name»
		'''
		 +
		switch fun.type
		{
			FunctionAlgorithmic:
			'''
			{

			}
			'''
			FunctionTable:
			'''
			{

			}
			'''
			FunctionList:
			'''
			{

			}
			'''
		}
	}

	def compileEvent(Event evn, String filename)
	{
		'''
		package «filename»;

		public class «evn.name» implements rdo_lib.EventScheduler
		{
			private static final String name =  "«evn.fullyQualifiedName»";

			private static class ResourceSet
			{
				«FOR r : evn.relevantresources»
					«IF r.type instanceof ResourceType»
						public «r.type.fullyQualifiedName» «r.name»;
					«ELSE»
						public «(r.type as ResourceDeclaration).reference.fullyQualifiedName» «r.name»;
					«ENDIF»
				«ENDFOR»

				public ResourceSet init()
				{
					«FOR r : evn.relevantresources»
						«IF r.type instanceof ResourceDeclaration»
							«r.name» = «(r.type as ResourceDeclaration).reference.fullyQualifiedName».«(r.type as ResourceDeclaration).name»;
						«ELSE»
							«IF r.rule.literal == "Create"»
								«r.name» = new «r.type.fullyQualifiedName»(«(r.type as ResourceType).parameters.size.compileAllDefault»);
							«ENDIF»
						«ENDIF»
					«ENDFOR»

					return this;
				}
			}

			private static ResourceSet classSet = new ResourceSet();

			public static class ParameterSet
			{
				«FOR p : evn.parameters»
					public «p.type.compileType» «p.name»«p.type.getDefault»;
				«ENDFOR»
			}

			private ParameterSet parameters = new ParameterSet();

			private double time;

			@Override
			public double getTime()
			{
				return time;
			}

			private static rdo_lib.Converter<ResourceSet, ParameterSet> converter =
				new rdo_lib.Converter<ResourceSet, ParameterSet>()
				{
					@Override
					public void run(ResourceSet resources, ParameterSet parameters)
					{
						«FOR e : evn.algorithms»
							«e.compileConvert(0)»
						«ENDFOR»
					}
				};


			@Override
			public void run()
			{
				converter.run(classSet.init(), parameters);
				«IF evn.relevantresources.filter[t | t.rule.literal == "Create"].size > 0»

					// add resources
					«FOR r : evn.relevantresources.filter[t |t.rule.literal == "Create"]»
						«(r.type as ResourceType).fullyQualifiedName».getManager().addResource(classSet.«r.name»);
					«ENDFOR»

				«ENDIF»
			}

			public «evn.name»(double time«IF evn.parameters.size > 0», «ENDIF»«evn.parameters.compilePatternParameters»)
			{
				this.time = time;
				«IF evn.parameters.size > 0»

				«FOR parameter : evn.parameters»
					if («parameter.name» != null)
						this.parameters.«parameter.name» = «parameter.name»;
				«ENDFOR»
				«ENDIF»
			}
		}
		'''
	}

	def compileRule(Rule rule, String filename)
	{
		'''
		package «filename»;

		public class «rule.name»
		{
			private static final String name = "«filename».«rule.name»";

			private static class ResourceSet
			{
				«FOR r : rule.relevantresources.filter[res | res.rule.literal != "Create"]»
					«IF r.type instanceof ResourceType»
						public «r.type.fullyQualifiedName» «r.name»;
					«ELSE»
						public «(r.type as ResourceDeclaration).reference.fullyQualifiedName» «r.name» = «(r.type as ResourceDeclaration).reference.fullyQualifiedName».«(r.type as ResourceDeclaration).name»;
					«ENDIF»
				«ENDFOR»

				public ResourceSet copy()
				{
					ResourceSet clone = new ResourceSet();

					«FOR r : rule.relevantresources.filter[res | res.rule.literal != "Create"]»
						clone.«r.name» = this.«r.name»;
					«ENDFOR»

					return clone;
				}

				public void clear()
				{
					«FOR r : rule.relevantresources.filter[res | res.type instanceof ResourceType && res.rule.literal != "Create"]»
						this.«r.name» = null;
					«ENDFOR»
				}
			}

			private static ResourceSet classSet = new ResourceSet();
			private ResourceSet instanceSet;

			public static class ParameterSet
			{
				«FOR p : rule.parameters»
					public «p.type.compileType» «p.name»«p.type.getDefault»;
				«ENDFOR»

				public ParameterSet(«rule.parameters.compilePatternParameters»)
				{
					«FOR p : rule.parameters»
						if («p.name» != null)
							this.«p.name» = «p.name»;
					«ENDFOR»
				}
			}

			private ParameterSet parameters;

			private «rule.name»(double time, ResourceSet resources, ParameterSet ps)
			{
				this.time = time;
				this.instanceSet = resources;
				this.parameters = ps;
			}

			«IF rule.combinational != null»
			static private rdo_lib.CombinationalChoiceFrom<ResourceSet> choice =
				new rdo_lib.CombinationalChoiceFrom<ResourceSet>
				(
					classSet,
					«rule.combinational.compileChoiceMethod("ResourceSet", "ResourceSet")»,
					new rdo_lib.CombinationalChoiceFrom.ResourceSetManager<ResourceSet>()
					{
						@Override
						public ResourceSet create(ResourceSet set)
						{
							ResourceSet clone = new ResourceSet();

							«FOR relres : rule.relevantresources.filter[r | r.rule.literal != "Create"]»
								clone.«relres.name» = set.«relres.name»;
							«ENDFOR»

							return clone;
						}

						@Override
						public void apply(ResourceSet origin, ResourceSet set)
						{
							«FOR relres : rule.relevantresources.filter[r | r.rule.literal != "Create"]»
								origin.«relres.name» = set.«relres.name»;
							«ENDFOR»
						}
					}
				);
				static
				{
				«FOR rc : rule.algorithms.filter[r | r.relres.type instanceof ResourceType && r.relres.rule.literal != "Create"]»
					choice.addFinder
					(
						new rdo_lib.CombinationalChoiceFrom.Finder<ResourceSet, «rc.relres.type.fullyQualifiedName»>
						(
							new rdo_lib.CombinationalChoiceFrom.Retriever<«rc.relres.type.fullyQualifiedName»>()
							{
								@Override
								public java.util.Collection<«rc.relres.type.fullyQualifiedName»> getResources()
								{
									return «rc.relres.type.fullyQualifiedName».getManager().get«
										IF rc.relres.rule.literal == "Erase"»Temporary«ELSE»All«ENDIF»();
								}
							},
							new rdo_lib.SimpleChoiceFrom<ResourceSet, «rc.relres.type.fullyQualifiedName»>
							(
								«rc.choicefrom.compileChoiceFrom(rule, rc.relres.type.fullyQualifiedName, rc.relres.name, rc.relres.type.fullyQualifiedName)»,
								null
							),
							new rdo_lib.CombinationalChoiceFrom.Setter<ResourceSet, «rc.relres.type.fullyQualifiedName»>()
							{
								public void set(ResourceSet set, «rc.relres.type.fullyQualifiedName» resource)
								{
									set.«rc.relres.name» = resource;
								}
							}
						)
					);

				«ENDFOR»
				}

			«ELSE»
			«FOR rc : rule.algorithms.filter[r | r.relres.type instanceof ResourceType && r.relres.rule.literal != "Create"]»
			// choice «rc.relres.name»
			private static rdo_lib.SimpleChoiceFrom<ResourceSet, «rc.relres.type.fullyQualifiedName»> «rc.relres.name»Choice =
				new rdo_lib.SimpleChoiceFrom<ResourceSet, «rc.relres.type.fullyQualifiedName»>
				(
					«rc.choicefrom.compileChoiceFrom(rule, rc.relres.type.fullyQualifiedName, rc.relres.name, rc.relres.type.fullyQualifiedName)»,
					«rc.choicemethod.compileChoiceMethod(rule.name, rc.relres.type.fullyQualifiedName)»
				);

			«ENDFOR»
			«ENDIF»

			private static rdo_lib.Converter<ResourceSet, ParameterSet> rule =
					new rdo_lib.Converter<ResourceSet, ParameterSet>()
					{
						@Override
						public void run(ResourceSet resources, ParameterSet ps)
						{
							«FOR e : rule.algorithms.filter[r | r.haverule]»
								«e.compileConvert(0)»
							«ENDFOR»
						}
					};

			public static boolean tryRule(ParameterSet parameters)
			{
				classSet.clear();

				«IF rule.combinational != null»
				if (!choice.find())
					return false;

				«ELSE»
				«FOR rc : rule.algorithms.filter[r | r.relres.type instanceof ResourceType && r.relres.rule.literal != "Create"]»
					classSet.«rc.relres.name» = «rc.relres.name»Choice.find(classSet, «rc.relres.type.fullyQualifiedName».getManager().get«
						IF rc.relres.rule.literal == "Erase"»Temporary«ELSE»All«ENDIF»());

					if (classSet.«rc.relres.name» == null)
						return false;

				«ENDFOR»
				«ENDIF»
				ResourceSet matched = classSet.copy();

				«IF rule.relevantresources.filter[t | t.rule.literal == "Create"].size > 0»
					// create resources
					«FOR r : rule.relevantresources.filter[t |t.rule.literal == "Create"]»
						matched.«r.name» = new «(r.type as ResourceType).fullyQualifiedName»(«
							(r.type as ResourceType).parameters.size.compileAllDefault»);
					«ENDFOR»
					«FOR r : rule.relevantresources.filter[t |t.rule.literal == "Create"]»
						«(r.type as ResourceType).fullyQualifiedName».getManager().addResource(matched.«r.name»);
					«ENDFOR»

				«ENDIF»
				«IF rule.relevantresources.filter[t | t.rule.literal == "Erase"].size > 0»
					// erase resources
					«FOR r : rule.relevantresources.filter[t |t.rule.literal == "Erase"]»
						«(r.type as ResourceType).fullyQualifiedName».getManager().eraseResource(matched.«r.name»);
					«ENDFOR»

				«ENDIF»
				rule.run(matched, parameters);

				return true;
			}
		}
		'''
	}

	def compileOperation(Operation op, String filename)
	{
		'''
		package «filename»;

		public class «op.name» implements rdo_lib.EventScheduler
		{
			private static final String name = "«filename».«op.name»";

			private static class ResourceSet
			{
				«FOR r : op.relevantresources.filter[res | res.begin.literal != "Create" && res.end.literal != "Create"]»
					«IF r.type instanceof ResourceType»
						public «r.type.fullyQualifiedName» «r.name»;
					«ELSE»
						public «(r.type as ResourceDeclaration).reference.fullyQualifiedName» «r.name» = «(r.type as ResourceDeclaration).reference.fullyQualifiedName».«(r.type as ResourceDeclaration).name»;
					«ENDIF»
				«ENDFOR»

				public ResourceSet copy()
				{
					ResourceSet clone = new ResourceSet();

					«FOR r : op.relevantresources.filter[res | res.begin.literal != "Create" && res.end.literal != "Create"]»
						clone.«r.name» = this.«r.name»;
					«ENDFOR»

					return clone;
				}

				public void clear()
				{
					«FOR r : op.relevantresources.filter[res | res.type instanceof ResourceType && res.begin.literal != "Create" && res.end.literal != "Create"]»
						this.«r.name» = null;
					«ENDFOR»
				}
			}

			private static ResourceSet classSet = new ResourceSet();
			private ResourceSet instanceSet;

			public static class ParameterSet
			{
				«FOR p : op.parameters»
					public «p.type.compileType» «p.name»«p.type.getDefault»;
				«ENDFOR»

				public ParameterSet(«op.parameters.compilePatternParameters»)
				{
					«FOR p : op.parameters»
						if («p.name» != null)
							this.«p.name» = «p.name»;
					«ENDFOR»
				}
			}

			private ParameterSet parameters;

			private «op.name»(double time, ResourceSet resources, ParameterSet ps)
			{
				this.time = time;
				this.instanceSet = resources;
				this.parameters = ps;
			}

			«IF op.combinational != null»
			static private rdo_lib.CombinationalChoiceFrom<ResourceSet> choice =
				new rdo_lib.CombinationalChoiceFrom<ResourceSet>
				(
					classSet,
					«op.combinational.compileChoiceMethod("ResourceSet", "ResourceSet")»,
					new rdo_lib.CombinationalChoiceFrom.ResourceSetManager<ResourceSet>()
					{
						@Override
						public ResourceSet create(ResourceSet set)
						{
							ResourceSet clone = new ResourceSet();

							«FOR relres : op.relevantresources.filter[r | r.begin.literal != "Create" && r.end.literal != "Create"]»
								clone.«relres.name» = set.«relres.name»;
							«ENDFOR»

							return clone;
						}

						@Override
						public void apply(ResourceSet origin, ResourceSet set)
						{
							«FOR relres : op.relevantresources.filter[r | r.begin.literal != "Create" && r.end.literal != "Create"]»
								origin.«relres.name» = set.«relres.name»;
							«ENDFOR»
						}
					}
				);
				static
				{
				«FOR rc : op.algorithms.filter[r | r.relres.type instanceof ResourceType &&
						r.relres.begin.literal != "Create" && r.relres.end.literal != "Create"]»
					choice.addFinder
					(
						new rdo_lib.CombinationalChoiceFrom.Finder<ResourceSet, «rc.relres.type.fullyQualifiedName»>
						(
							new rdo_lib.CombinationalChoiceFrom.Retriever<«rc.relres.type.fullyQualifiedName»>()
							{
								@Override
								public java.util.Collection<«rc.relres.type.fullyQualifiedName»> getResources()
								{
									return «rc.relres.type.fullyQualifiedName».getManager().get«
										IF rc.relres.begin.literal == "Erase" || rc.relres.end.literal == "Erase"»Temporary«ELSE»All«ENDIF»();
								}
							},
							new rdo_lib.SimpleChoiceFrom<ResourceSet, «rc.relres.type.fullyQualifiedName»>
							(
								«rc.choicefrom.compileChoiceFrom(op, rc.relres.type.fullyQualifiedName, rc.relres.name, rc.relres.type.fullyQualifiedName)»,
								null
							),
							new rdo_lib.CombinationalChoiceFrom.Setter<ResourceSet, «rc.relres.type.fullyQualifiedName»>()
							{
								public void set(ResourceSet set, «rc.relres.type.fullyQualifiedName» resource)
								{
									set.«rc.relres.name» = resource;
								}
							}
						)
					);

				«ENDFOR»
				}

			«ELSE»
			«FOR rc : op.algorithms.filter[r | r.relres.type instanceof ResourceType &&
				r.relres.begin.literal != "Create" && r.relres.end.literal != "Create"]»
			// choice «rc.relres.name»
			private static rdo_lib.SimpleChoiceFrom<ResourceSet, «rc.relres.type.fullyQualifiedName»> «rc.relres.name»Choice =
				new rdo_lib.SimpleChoiceFrom<ResourceSet, «rc.relres.type.fullyQualifiedName»>
				(
					«rc.choicefrom.compileChoiceFrom(op, rc.relres.type.fullyQualifiedName, rc.relres.name, rc.relres.type.fullyQualifiedName)»,
					«rc.choicemethod.compileChoiceMethod(op.name, rc.relres.type.fullyQualifiedName)»
				);

			«ENDFOR»
			«ENDIF»

			private static rdo_lib.Converter<ResourceSet, ParameterSet> begin =
					new rdo_lib.Converter<ResourceSet, ParameterSet>()
					{
						@Override
						public void run(ResourceSet resources, ParameterSet ps)
						{
							«FOR e : op.algorithms.filter[r | r.havebegin]»
								«e.compileConvert(0)»
							«ENDFOR»
						}
					};

			private static rdo_lib.Converter<ResourceSet, ParameterSet> end =
					new rdo_lib.Converter<ResourceSet, ParameterSet>()
					{
						@Override
						public void run(ResourceSet resources, ParameterSet ps)
						{
							«FOR e : op.algorithms.filter[r | r.haveend]»
								«e.compileConvert(1)»
							«ENDFOR»
						}
					};

			public static boolean tryRule(ParameterSet parameters)
			{
				classSet.clear();

				«IF op.combinational != null»
				if (!choice.find())
					return false;

				«ELSE»
				«FOR rc : op.algorithms.filter[r | r.relres.type instanceof ResourceType &&
					r.relres.begin.literal != "Create" && r.relres.end.literal != "Create"]»
					classSet.«rc.relres.name» = «rc.relres.name»Choice.find(classSet, «rc.relres.type.fullyQualifiedName».getManager().get«
						IF rc.relres.begin.literal == "Erase" || rc.relres.end.literal == "Erase"»Temporary«ELSE»All«ENDIF»());

					if (classSet.«rc.relres.name» == null)
						return false;

				«ENDFOR»
				«ENDIF»
				ResourceSet matched = classSet.copy();

				«IF op.relevantresources.filter[t | t.begin.literal == "Create"].size > 0»
					// create resources
					«FOR r : op.relevantresources.filter[t |t.begin.literal == "Create"]»
						matched.«r.name» = new «(r.type as ResourceType).fullyQualifiedName»(«
							(r.type as ResourceType).parameters.size.compileAllDefault»);
					«ENDFOR»
					«FOR r : op.relevantresources.filter[t |t.begin.literal == "Create"]»
						«(r.type as ResourceType).fullyQualifiedName».getManager().addResource(matched.«r.name»);
					«ENDFOR»

				«ENDIF»
				«IF op.relevantresources.filter[t | t.begin.literal == "Erase" || t.end.literal == "Erase"].size > 0»
					// erase resources
					«FOR r : op.relevantresources.filter[t |t.begin.literal == "Erase" || t.end.literal == "Erase"]»
						«(r.type as ResourceType).fullyQualifiedName».getManager().eraseResource(matched.«r.name»);
					«ENDFOR»

				«ENDIF»
				begin.run(matched, parameters);

				rdo_lib.Simulator.pushEvent(new «op.name»(rdo_lib.Simulator.getTime() + «op.time.compileExpression», matched, parameters));

				return true;
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
				«IF op.relevantresources.filter[t | t.end.literal == "Create"].size > 0»
					// create resources
					«FOR r : op.relevantresources.filter[t |t.end.literal == "Create"]»
						instanceSet.«r.name» = new «(r.type as ResourceType).fullyQualifiedName»(«
							(r.type as ResourceType).parameters.size.compileAllDefault»);
					«ENDFOR»
					«FOR r : op.relevantresources.filter[t |t.end.literal == "Create"]»
						«(r.type as ResourceType).fullyQualifiedName».getManager().addResource(instanceSet.«r.name»);
					«ENDFOR»

				«ENDIF»
				end.run(instanceSet, parameters);
			}
		}
		'''
	}

	def static compileChoiceFrom(PatternChoiceFrom cf, Pattern pattern, String resource, String relres, String relrestype)
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
			new rdo_lib.SimpleChoiceFrom.Checker<ResourceSet, «resource»>()
			{
				@Override
				public boolean check(ResourceSet pattern, «resource» «relres»)
				{
					return «IF havecf»(«cf.compileForPattern»)«ELSE»true«ENDIF»«FOR i : 0 ..< relreslist.size»«
						IF relres != relreslist.get(i) && relrestype == relrestypes.get(i)»
							«"\t"»&& «relres» != pattern.«relreslist.get(i)»«ENDIF»«ENDFOR»;
				}
			}'''
	}

	def compileChoiceMethod(PatternChoiceMethod cm, String pattern, String resource)
	{
		if (cm == null || cm.first)
			return "null"
		else
		{
			return
				'''
				new rdo_lib.SimpleChoiceFrom.ChoiceMethod<ResourceSet, «resource»>()
				{
					@Override
					public int compare(«resource» x, «resource» y)
					{
						if («cm.compileForPattern»)
							return -1;
						else
							return  1;
					}
				}'''
		}
	}

	def static String getDefault(RDORTPParameterType parameter)
	{
		switch parameter
		{
			RDORTPParameterBasic:
				return if (parameter.^default != null) " = " + RDOExpressionCompiler.compileExpression(parameter.^default) else ""
			RDORTPParameterString:
				return if (parameter.^default != null) ' = "' + parameter.^default + '"' else ""
			default:
				return ""
		}
	}

	def static compileResourceTypeParameters(List<ResourceTypeParameter> parameters)
	{
		'''«IF parameters.size > 0»«parameters.get(0).type.compileType» «
			parameters.get(0).name»«
			FOR parameter : parameters.subList(1, parameters.size)», «
				parameter.type.compileType» «
				parameter.name»«
			ENDFOR»«
		ENDIF»'''
	}

	def static compilePatternParameters(List<PatternParameter> parameters)
	{
		'''«IF parameters.size > 0»«parameters.get(0).type.compileType» «
			parameters.get(0).name»«
			FOR parameter : parameters.subList(1, parameters.size)», «
				parameter.type.compileType» «
				parameter.name»«
			ENDFOR»«
		ENDIF»'''
	}

	def static compileFunctionParameters(List<FunctionParameter> parameters)
	{
		'''«IF parameters.size > 0»«parameters.get(0).type.compileType» «
			parameters.get(0).name»«
			FOR parameter : parameters.subList(1, parameters.size)», «
				parameter.type.compileType» «
				parameter.name»«
			ENDFOR»«
		ENDIF»'''
	}

	def compileDecisionPoint(DecisionPoint dpt, String filename)
	{
		val activities = switch dpt
		{
			DecisionPointSome :dpt.activities
			DecisionPointPrior: dpt.activities
		}

		val parameters = activities.map[a |
			if(a.pattern instanceof Operation)
				(a.pattern as Operation).parameters
			else
				(a.pattern as Rule).parameters
		]

		return
			'''
			package «filename»;

			public class «dpt.name»
			{
				«IF dpt instanceof DecisionPointSome || dpt instanceof DecisionPointPrior»
				«FOR i : 0 ..< activities.size»
				«IF activities.get(i).parameters.size == parameters.get(i).size»
				private static «activities.get(i).pattern.fullyQualifiedName».ParameterSet «activities.get(i).name» =
					new «activities.get(i).pattern.fullyQualifiedName».ParameterSet(«activities.get(i).compileExpression»);
				«ENDIF»
				«ENDFOR»

				«ENDIF»
				private static rdo_lib.DecisionPoint dpt =
					new rdo_lib.DecisionPoint
					(
						"«dpt.fullyQualifiedName»",
						«IF dpt.parent != null»«dpt.parent.fullyQualifiedName».getDPT()«ELSE»null«ENDIF»,
						«IF dpt instanceof DecisionPointPrior»«dpt.priority.compileExpression»«ELSE»null«ENDIF»,
						«IF dpt.condition != null
						»new rdo_lib.DecisionPoint.Condition()
						{
							@Override
							public boolean check()
							{
								return true;
							}
						}«ELSE»null«ENDIF»
					);
				static
				{
					«IF dpt instanceof DecisionPointSome || dpt instanceof DecisionPointPrior»
					«FOR a : activities»
						dpt.addActivity(
							new rdo_lib.DecisionPoint.Activity()
							{
								@Override
								public String getName()
								{
									return "«filename».«a.name»";
								}

								@Override
								public boolean checkActivity()
								{
									return («a.pattern.fullyQualifiedName».tryRule(«a.name»));
								}
							}
						);
					«ENDFOR»

					«ENDIF»
					rdo_lib.Simulator.addDecisionPoint(dpt);
				}

				public rdo_lib.DecisionPoint getDPT()
				{
					return dpt;
				}
			}
			'''
	}

	def compilePermanentManager()
	{
		'''
		package rdo_lib;

		import java.util.Map;
		import java.util.HashMap;

		public class PermanentResourceManager<T>
		{
			protected Map<String, T> resources = new HashMap<String, T>();

			public void addResource(String name, T res)
			{
				resources.put(name, res);
			}

			public T getResource(String name)
			{
				return resources.get(name);
			}

			public java.util.Collection<T> getAll()
			{
				return resources.values();
			}
		}
		'''
	}

	def compileTemporaryManager()
	{
		'''
		package rdo_lib;

		import java.util.Map;
		import java.util.HashMap;

		import java.util.Queue;
		import java.util.LinkedList;

		import java.util.Collection;

		public class TemporaryResourceManager<T> extends PermanentResourceManager<T>
		{
			private Map<T, Integer> temporary = new HashMap<T, Integer>();

			private Queue<Integer> vacantList = new LinkedList<Integer>();
			private int currentLast = 0;

			public void addResource(T res)
			{
				if (temporary.containsKey(res))
					return;

				int number;
				if (vacantList.size() > 0)
					number = vacantList.poll();
				else
					number = currentLast++;

				temporary.put(res, number);
			}

			public void eraseResource(T res)
			{
				vacantList.add(temporary.get(res));
				temporary.remove(res);
			}

			@Override
			public Collection<T> getAll()
			{
				Collection<T> all = resources.values();
				all.addAll(temporary.keySet());
				return all;
			}

			public Collection<T> getTemporary()
			{
				return temporary.keySet();
			}
		}
		'''
	}

	def compileSimpleChoiceFrom()
	{
		'''
		package rdo_lib;

		import java.util.Collection;
		import java.util.Iterator;

		import java.util.Queue;
		import java.util.LinkedList;

		import java.util.PriorityQueue;
		import java.util.Comparator;

		public class SimpleChoiceFrom<P, T>
		{
			public static interface Checker<P, T>
			{
				public boolean check(P pattern, T res);
			}

			public static abstract class ChoiceMethod<P, T> implements Comparator<T>
			{
				protected P pattern;

				private void setPattern(P pattern)
				{
					this.pattern = pattern;
				}
			}

			private Checker<P, T> checker;
			private ChoiceMethod<P, T> comparator;

			public SimpleChoiceFrom(Checker<P, T> checker, ChoiceMethod<P, T> comparator)
			{
				this.checker = checker;
				if (comparator != null)
				{
					this.comparator = comparator;
					matchingList = new PriorityQueue<T>(1, comparator);
				}
				else
					matchingList = new LinkedList<T>();
			}

			private Queue<T> matchingList;

			public Collection<T> findAll(P pattern, Collection<T> reslist)
			{
				matchingList.clear();
				if (comparator != null)
					comparator.setPattern(pattern);

				T res;
				for (Iterator<T> iterator = reslist.iterator(); iterator.hasNext();)
				{
					res = iterator.next();
					if (checker.check(pattern, res))
						matchingList.add(res);
				}

				return matchingList;
			}

			public T find(P pattern, Collection<T> reslist)
			{
				matchingList.clear();
				if (comparator != null)
					comparator.setPattern(pattern);

				T res;
				for (Iterator<T> iterator = reslist.iterator(); iterator.hasNext();)
				{
					res = iterator.next();
					if (checker.check(pattern, res))
						if (matchingList instanceof LinkedList)
							return res;
						else
							matchingList.add(res);
				}

				if (matchingList.size() == 0)
					return null;
				else
					return matchingList.poll();
			}
		}
		'''
	}

	def compileCommonChoiceFrom()
	{
		'''
		package rdo_lib;

		import java.util.List;
		import java.util.ArrayList;

		import java.util.Collection;
		import java.util.Iterator;

		import java.util.PriorityQueue;

		import rdo_lib.SimpleChoiceFrom.ChoiceMethod;

		public class CombinationalChoiceFrom<R>
		{
			private R set;
			private ResourceSetManager<R> setManager;
			private PriorityQueue<R> matchingList;

			public CombinationalChoiceFrom(R set, ChoiceMethod<R, R> comparator, ResourceSetManager<R> setManager)
			{
				this.set = set;
				this.setManager = setManager;
				matchingList = new PriorityQueue<R>(1, comparator);
			}

			public static abstract class Setter<R, T>
			{
				public abstract void set(R set, T resource);
			}

			public static interface Retriever<T>
			{
				public Collection<T> getResources();
			}

			public static abstract class ResourceSetManager<R>
			{
				public abstract R create(R set);
				public abstract void apply(R origin, R set);
			}

			public static class Finder<R, T>
			{
				private Retriever<T> retriever;
				private SimpleChoiceFrom<R, T> choice;
				private Setter<R, T> setter;

				public Finder(Retriever<T> retriever, SimpleChoiceFrom<R, T> choice, Setter<R, T> setter)
				{
					this.retriever = retriever;
					this.choice  = choice;
					this.setter  = setter;
				}

				public boolean find(R set, Iterator<Finder<R, ?>> finder, PriorityQueue<R> matchingList, ResourceSetManager<R> setManager)
				{
					Collection<T> all = choice.findAll(set, retriever.getResources());
					Iterator<T> iterator = all.iterator();
					if (finder.hasNext())
					{
						Finder<R, ?> currentFinder = finder.next();
						while (iterator.hasNext())
						{
							setter.set(set, iterator.next());
							if (currentFinder.find(set, finder, matchingList, setManager))
								if (matchingList.iterator() == null)
									return true;
						}
						return false;
					}
					else
					{
						if (all.size() > 0)
							if (matchingList.comparator() == null)
							{
								setter.set(set, all.iterator().next());
								return true;
							}
							else
							{
								for (T a : all)
								{
									setter.set(set, a);
									matchingList.add(setManager.create(set));
								}
								return true;
							}
						else
							return false;
					}
				}
			}

			private List<Finder<R, ?>> finders = new ArrayList<Finder<R, ?>>();

			public void addFinder(Finder<R, ?> finder)
			{
				finders.add(finder);
			}

			public boolean find()
			{
				matchingList.clear();

				if (finders.size() == 0)
					return false;

				Iterator<Finder<R, ?>> finder = finders.iterator();

				if (finder.next().find(set, finder, matchingList, setManager)
					&& matchingList.comparator() == null)
						return true;

				if (matchingList.size() > 0)
				{
					setManager.apply(set, matchingList.poll());
					return true;
				}
				else
					return false;
			}
		}
		'''
	}

	def compileLibSimulator()
	{
		'''
		package rdo_lib;

		import java.util.Iterator;

		import java.util.List;
		import java.util.LinkedList;

		import java.util.PriorityQueue;
		import java.util.Comparator;

		public abstract class Simulator
		{
			private static double time = 0;

			public static double getTime()
			{
				return time;
			}

			private static Comparator<EventScheduler> comparator = new Comparator<EventScheduler>()
			{
				@Override
				public int compare(EventScheduler x, EventScheduler y)
				{
					if (x.getTime() < y.getTime()) return -1;
					if (x.getTime() > y.getTime()) return  1;
					return 0;
				}
			};

			private static PriorityQueue<EventScheduler> eventList = new PriorityQueue<EventScheduler>(1, comparator);

			public static void pushEvent(EventScheduler event)
			{
				if (time >= Simulator.time)
					eventList.add(event);
			}

			private static EventScheduler popEvent()
			{
				return eventList.poll();
			}

			private static List<TerminateCondition> terminateList = new LinkedList<TerminateCondition>();

			public static void addTerminateCondition(TerminateCondition c)
			{
				terminateList.add(c);
			}

			private static List<DecisionPoint> dptList = new LinkedList<DecisionPoint>();

			public static void addDecisionPoint(DecisionPoint dpt)
			{
				dptList.add(dpt);
			}

			private static boolean checkDPT()
			{
				Iterator<DecisionPoint> dptIterator = dptList.iterator();

				while (dptIterator.hasNext())
					if (dptIterator.next().checkActivities())
						return true;

				return false;
			}

			public static int run()
			{
				while(checkDPT());

				while(eventList.size() > 0)
				{
					EventScheduler current = popEvent();

					time = current.getTime();

					current.run();

					for (TerminateCondition c : terminateList)
						if (c.check())
							return 1;

					while(checkDPT());
				}
				return 0;
			}
		}
		'''
	}

	def compileTerminate()
	{
		'''
		package rdo_lib;

		public interface TerminateCondition
		{
			public boolean check();
		}
		'''
	}

	def compileConverter()
	{
		'''
		package rdo_lib;

		public interface Converter<R, P>
		{
			public void run(R resources, P parameters);
		}
		'''
	}

	def compileEventScheduler()
	{
		'''
		package rdo_lib;

		public interface EventScheduler
		{
			public double getTime();
			public void run();
		}
		'''
	}

	def compileDecisionPoint()
	{
		'''
		package rdo_lib;

		import java.util.List;
		import java.util.LinkedList;

		public class DecisionPoint
		{
			private String name;
			private DecisionPoint parent;
			private Double priority;

			public static interface Condition
			{
				public boolean check();
			}

			private Condition condition;

			public DecisionPoint(String name, DecisionPoint parent, Double priority, Condition condition)
			{
				this.name = name;
				this.parent = parent;
				this.priority = priority;
				this.condition = condition;
			}

			public String getName()
			{
				return name;
			}

			public Double getPriority()
			{
				return priority;
			}

			public static interface Activity
			{
				public String getName();
				public boolean checkActivity();
			}

			private List<Activity> activities = new LinkedList<Activity>();

			public void addActivity(Activity a)
			{
				activities.add(a);
			}

			public boolean checkActivities()
			{
				if (condition != null && !condition.check())
					return false;

				for (Activity a : activities)
					if (a.checkActivity())
						return true;

				return false;
			}
		}
		'''
	}
}

