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
		fsa.generateFile("rdo_lib/Event.java",                    compileEvent           ())
		fsa.generateFile("rdo_lib/Rule.java",                     compileRule            ())
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
				»ResourceManager<MSA> manager = new rdo_lib.«rtp.type.literal.withFirstUpper»ResourceManager<MSA>();

			public static rdo_lib.«rtp.type.literal.withFirstUpper»ResourceManager<MSA> getManager()
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

		public class «evn.name» implements rdo_lib.Event
		{
			@Override
			public String getName()
			{
				return "«evn.fullyQualifiedName»";
			}

			«FOR parameter : evn.parameters»
				private «parameter.type.compileType» «parameter.name»«parameter.type.getDefault»;
			«ENDFOR»

			public «evn.name»(«evn.parameters.compilePatternParameters»)
			{
				«FOR parameter : evn.parameters»
					if («parameter.name» != null)
						this.«parameter.name» = «parameter.name»;
				«ENDFOR»
			}

			@Override
			public void calculateEvent()
			{
				// retrieve/create relevant resources
				«FOR r : evn.relevantresources»
					«IF r.type instanceof ResourceType»
						«(r.type as ResourceType).fullyQualifiedName» «
							r.name» = new «(r.type as ResourceType).fullyQualifiedName»(«
								(r.type as ResourceType).parameters.size.compileAllDefault»);
					«ELSE»
						«(r.type as ResourceDeclaration).reference.fullyQualifiedName» «r.name» = «
							(r.type as ResourceDeclaration).reference.fullyQualifiedName».«
								(r.type as ResourceDeclaration).name»;
					«ENDIF»
				«ENDFOR»

				«FOR e : evn.algorithms»
					«e.compileConvert(0)»

				«ENDFOR»
				«IF evn.relevantresources.map[t | t.type].filter(typeof(ResourceType)).size > 0»
					// add created resources
					«FOR r : evn.relevantresources»
						«IF r.type instanceof ResourceType»
							«(r.type as ResourceType).fullyQualifiedName».getManager().addResource(«r.name»);
						«ENDIF»
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

		public class «rule.name» implements rdo_lib.Rule
		{
			@Override
			public String getName()
			{
				return "«filename».«rule.name»";
			}
			«IF rule.parameters.size > 0»

			«FOR parameter : rule.parameters»
				public «parameter.type.compileType» «parameter.name»«parameter.type.getDefault»;
			«ENDFOR»
			«ENDIF»

			«FOR relres : rule.relevantresources»
				«IF relres.type instanceof ResourceDeclaration»
					public «(relres.type as ResourceDeclaration).reference.fullyQualifiedName» «
						relres.name» = «(relres.type as ResourceDeclaration).reference.fullyQualifiedName».«
							(relres.type as ResourceDeclaration).name»;
				«ELSE»
					public «(relres.type as ResourceType).fullyQualifiedName» «relres.name»«
						IF relres.rule.literal == "Create"» = new «(relres.type as ResourceType).fullyQualifiedName»(«
								(relres.type as ResourceType).parameters.size.compileAllDefault»)«ENDIF»;
				«ENDIF»
			«ENDFOR»

			«IF rule.combinational != null»
			public class ResourceSet
			{
				«FOR relres : rule.relevantresources.filter[r | r.rule.literal != "Create"]»
				«IF relres.type instanceof ResourceType»
					public «(relres.type as ResourceType).fullyQualifiedName» «relres.name»;
				«ELSE»
					public «(relres.type as ResourceDeclaration).reference.fullyQualifiedName» «relres.name»;
				«ENDIF»
				«ENDFOR»
			}

			«ENDIF»
			@Override
			public boolean tryRule()
			{
				«IF rule.combinational != null»
				rdo_lib.CombinationalChoiceFrom<«rule.name», ResourceSet> choice =
					new rdo_lib.CombinationalChoiceFrom<«rule.name», ResourceSet>
					(
						this,
						«rule.combinational.compileChoiceMethod(rule.name, "ResourceSet")»,
						new rdo_lib.CombinationalChoiceFrom.ResourceSetManager<«rule.name», ResourceSet>()
						{
							@Override
							public ResourceSet create(«rule.name» pattern)
							{
								ResourceSet set = new ResourceSet();

								«FOR relres : rule.relevantresources.filter[r | r.rule.literal != "Create"]»
									set.«relres.name» = pattern.«relres.name»;
								«ENDFOR»

								return set;
							}

							@Override
							public void apply(«rule.name» pattern, ResourceSet set)
							{
								«FOR relres : rule.relevantresources.filter[r | r.rule.literal != "Create"]»
									pattern.«relres.name» = set.«relres.name»;
								«ENDFOR»
							}
						}
					);

				«FOR rc : rule.algorithms.filter[r | r.relres.type instanceof ResourceType &&
					r.relres.rule.literal != "Create"]»
				choice.addFinder
				(
					new rdo_lib.CombinationalChoiceFrom.Finder<«rule.name», «rc.relres.type.fullyQualifiedName»>
					(
						«rc.relres.type.fullyQualifiedName».getManager().get«
							IF rc.relres.rule.literal == "Erase"»Temporary«ELSE»All«ENDIF»(),
						new rdo_lib.SimpleChoiceFrom<«rule.name», «rc.relres.type.fullyQualifiedName»>
						(
							«rc.choicefrom.compileChoiceFrom(rule, rc.relres.type.fullyQualifiedName, rc.relres.name)»,
							null
						),
						new rdo_lib.CombinationalChoiceFrom.Setter<«rule.name», «rc.relres.type.fullyQualifiedName»>()
						{
							public void set(«rule.name» pattern, «rc.relres.type.fullyQualifiedName» resource)
							{
								pattern.«rc.relres.name» = resource;
							}
						}
					)
				);

				«ENDFOR»
				if (!choice.find())
					return false;

			«ELSE»
				«FOR rc : rule.algorithms.filter[r | r.relres.type instanceof ResourceType &&
					r.relres.rule.literal != "Create"]»
				// choice «rc.relres.name»
				rdo_lib.SimpleChoiceFrom<«rule.name», «rc.relres.type.fullyQualifiedName»> «rc.relres.name»Choice =
					new rdo_lib.SimpleChoiceFrom<«rule.name», «rc.relres.type.fullyQualifiedName»>
					(
						«rc.choicefrom.compileChoiceFrom(rule, rc.relres.type.fullyQualifiedName, rc.relres.name)»,
						«rc.choicemethod.compileChoiceMethod(rule.name, rc.relres.type.fullyQualifiedName)»
					);

				«rc.relres.name» = «rc.relres.name»Choice.find(«rc.relres.type.fullyQualifiedName».getManager().get«
					IF rc.relres.rule.literal == "Erase"»Temporary«ELSE»All«ENDIF»());
				if («rc.relres.name» == null)
					return false;

				«ENDFOR»
			«ENDIF»
				«FOR e : rule.algorithms.filter[r | r.haverule]»
					«e.compileConvert(0)»

				«ENDFOR»
				«IF rule.relevantresources.filter[t | t.rule.literal == 'Create'].map[t | t.type].size > 0»
					// add created resources
					«FOR r : rule.relevantresources.filter[t | t.rule.literal == 'Create']»
						«(r.type as ResourceType).fullyQualifiedName».getManager().addResource(«r.name»);
					«ENDFOR»

				«ENDIF»
				«IF rule.relevantresources.filter[t | t.rule.literal == 'Erase'].map[t | t.type].size > 0»
					// erase resources
					«FOR r : rule.relevantresources.filter[t | t.rule.literal == 'Erase']»
						«(r.type as ResourceType).fullyQualifiedName».getManager().eraseResource(«r.name»);
					«ENDFOR»

				«ENDIF»
				return true;
			}
		}
		'''
	}

	def compileOperation(Operation op, String filename)
	{
		'''
		package «filename»;

		public class «op.name» implements rdo_lib.Rule, rdo_lib.Event
		{
			@Override
			public String getName()
			{
				return "«filename».«op.name»";
			}
			«IF op.parameters.size > 0»

			«FOR parameter : op.parameters»
				public «parameter.type.compileType» «parameter.name»«parameter.type.getDefault»;
			«ENDFOR»
			«ENDIF»

			«FOR relres : op.relevantresources»
				«IF relres.type instanceof ResourceDeclaration»
					public «(relres.type as ResourceDeclaration).reference.fullyQualifiedName» «
						relres.name» = «(relres.type as ResourceDeclaration).reference.fullyQualifiedName».«
							(relres.type as ResourceDeclaration).name»;
				«ELSE»
					public «(relres.type as ResourceType).fullyQualifiedName» «relres.name»«IF relres.begin.literal ==
						"Create" || relres.end.literal == "Create"» = new «(relres.type as ResourceType).fullyQualifiedName
							»(«(relres.type as ResourceType).parameters.size.compileAllDefault»)«ENDIF»;
				«ENDIF»
			«ENDFOR»

			«IF op.combinational != null»
			public class ResourceSet
			{
				«FOR relres : op.relevantresources.filter[r | r.begin.literal != "Create" && r.end.literal != "Create"]»
				«IF relres.type instanceof ResourceType»
					public «(relres.type as ResourceType).fullyQualifiedName» «relres.name»;
				«ELSE»
					public «(relres.type as ResourceDeclaration).reference.fullyQualifiedName» «relres.name»;
				«ENDIF»
				«ENDFOR»
			}

			«ENDIF»
			@Override
			public boolean tryRule()
			{
				«IF op.combinational != null»
				rdo_lib.CombinationalChoiceFrom<«op.name», ResourceSet> choice =
					new rdo_lib.CombinationalChoiceFrom<«op.name», ResourceSet>
					(
						this,
						«op.combinational.compileChoiceMethod(op.name, "ResourceSet")»,
						new rdo_lib.CombinationalChoiceFrom.ResourceSetManager<«op.name», ResourceSet>()
						{
							@Override
							public ResourceSet create(«op.name» pattern)
							{
								ResourceSet set = new ResourceSet();

								«FOR relres : op.relevantresources.filter[r | r.begin.literal != "Create" && r.end.literal != "Create"]»
									set.«relres.name» = pattern.«relres.name»;
								«ENDFOR»

								return set;
							}

							@Override
							public void apply(«op.name» pattern, ResourceSet set)
							{
								«FOR relres : op.relevantresources.filter[r | r.begin.literal != "Create" && r.end.literal != "Create"]»
									pattern.«relres.name» = set.«relres.name»;
								«ENDFOR»
							}
						}
					);

				«FOR rc : op.algorithms.filter[r | r.relres.type instanceof ResourceType &&
					r.relres.begin.literal != "Create" && r.relres.end.literal != "Create"]»
				choice.addFinder
				(
					new rdo_lib.CombinationalChoiceFrom.Finder<«op.name», «rc.relres.type.fullyQualifiedName»>
					(
						«rc.relres.type.fullyQualifiedName».getManager().get«
							IF rc.relres.begin.literal == "Erase" || rc.relres.end.literal == "Erase"»Temporary«ELSE»All«ENDIF»(),
						new rdo_lib.SimpleChoiceFrom<«op.name», «rc.relres.type.fullyQualifiedName»>
						(
							«rc.choicefrom.compileChoiceFrom(op, rc.relres.type.fullyQualifiedName, rc.relres.name)»,
							null
						),
						new rdo_lib.CombinationalChoiceFrom.Setter<«op.name», «rc.relres.type.fullyQualifiedName»>()
						{
							public void set(«op.name» pattern, «rc.relres.type.fullyQualifiedName» resource)
							{
								pattern.«rc.relres.name» = resource;
							}
						}
					)
				);

				«ENDFOR»
				if (!choice.find())
					return false;

				«ELSE»
				«FOR rc : op.algorithms.filter[r | r.relres.type instanceof ResourceType &&
					r.relres.begin.literal != "Create" && r.relres.end.literal != "Create" && r.havebegin]»
				// choice «rc.relres.name»
				rdo_lib.SimpleChoiceFrom<«op.name», «rc.relres.type.fullyQualifiedName»> «rc.relres.name»Choice =
					new rdo_lib.SimpleChoiceFrom<«op.name», «rc.relres.type.fullyQualifiedName»>
					(
						«rc.choicefrom.compileChoiceFrom(op, rc.relres.type.fullyQualifiedName, rc.relres.name)»,
						«rc.choicemethod.compileChoiceMethod(op.name, rc.relres.type.fullyQualifiedName)»
					);

				«rc.relres.name» = «rc.relres.name»Choice.find(«rc.relres.type.fullyQualifiedName».getManager().get«
					IF rc.relres.begin.literal == "Erase" || rc.relres.end.literal == "Erase"»Temporary«ELSE»All«ENDIF»());
				if («rc.relres.name» == null)
					return false;

				«ENDFOR»
				«ENDIF»
				«FOR e : op.algorithms.filter[r | r.havebegin]»
					«e.compileConvert(0)»

				«ENDFOR»
				«IF op.relevantresources.filter[t | t.begin.literal == "Create"].size > 0»
					// add created resources
					«FOR r : op.relevantresources.filter[t |t.begin.literal == "Create"]»
						«(r.type as ResourceType).fullyQualifiedName».getManager().addResource(«r.name»);
					«ENDFOR»

				«ENDIF»
				«IF op.relevantresources.filter[t | t.begin.literal == "Erase"].size > 0»
					// erase resources
					«FOR r : op.relevantresources.filter[t |t.begin.literal == "Erase"]»
						«(r.type as ResourceType).fullyQualifiedName».getManager().eraseResource(«r.name»);
					«ENDFOR»

				«ENDIF»
				rdo_lib.Simulator.pushEvent(this, rdo_lib.Simulator.getTime() + «op.time.compileExpression»);

				return true;
			}

			@Override
			public void calculateEvent()
			{
				«FOR e : op.algorithms.filter[r | r.haveend]»
					«e.compileConvert(1)»
				«ENDFOR»
				«IF op.relevantresources.filter[t | t.end.literal == "Create"].size > 0»

					// add created resources
					«FOR r : op.relevantresources.filter[t | t.end.literal == "Create"]»
						«(r.type as ResourceType).fullyQualifiedName».getManager().addResource(«r.name»);
					«ENDFOR»
				«ENDIF»
				«IF op.relevantresources.filter[t | t.end.literal == "Erase"].size > 0»

					// erase resources
					«FOR r : op.relevantresources.filter[t | t.end.literal == "Erase"]»
						«(r.type as ResourceType).fullyQualifiedName».getManager().eraseResource(«r.name»);
					«ENDFOR»
				«ENDIF»
			}
		}
		'''
	}

	def static compileChoiceFrom(PatternChoiceFrom cf, Pattern pattern, String resource, String relres)
	{
		val havecf = cf != null && !cf.nocheck;

		var List<String> relreslist;

		switch pattern
		{
			Operation:
				relreslist = pattern.relevantresources.map[r | r.name]

			Rule:
				relreslist = pattern.relevantresources.map[r | r.name]
		}

		return
			'''
			new rdo_lib.SimpleChoiceFrom.Checker<«pattern.nameGeneric», «resource»>(this)
			{
				@Override
				public boolean check(«resource» «relres»)
				{
					return «IF havecf»(«cf.compileForPattern»)«ELSE»true«ENDIF»«FOR r : relreslist»«IF relres != r»
						«"\t"»&& «relres» != pattern.«r»«ENDIF»«ENDFOR»;
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
				new rdo_lib.SimpleChoiceFrom.ChoiceMethod<«pattern», «resource»>(this)
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
			public static abstract class Checker<P, T>
			{
				protected P pattern;

				public Checker(P pattern)
				{
					this.pattern = pattern;
				}

				public abstract boolean check(T res);
			}

			public static abstract class ChoiceMethod<P, T> implements Comparator<T>
			{
				protected P pattern;

				public ChoiceMethod(P pattern)
				{
					this.pattern = pattern;
				}
			}

			private Checker<P, T> checker;

			public SimpleChoiceFrom(Checker<P, T> checker, Comparator<T> comparator)
			{
				this.checker = checker;
				if (comparator != null)
					matchingList = new PriorityQueue<T>(1, comparator);
				else
					matchingList = new LinkedList<T>();
			}

			private Queue<T> matchingList;

			public Collection<T> findAll(Collection<T> reslist)
			{
				matchingList.clear();

				T res;
				for (Iterator<T> iterator = reslist.iterator(); iterator.hasNext();)
				{
					res = iterator.next();
					if (checker.check(res))
						matchingList.add(res);
				}

				return matchingList;
			}

			public T find(Collection<T> reslist)
			{
				matchingList.clear();

				T res;
				for (Iterator<T> iterator = reslist.iterator(); iterator.hasNext();)
				{
					res = iterator.next();
					if (checker.check(res))
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

		public class CombinationalChoiceFrom<P, A>
		{
			private P pattern;
			private ResourceSetManager<P, A> setManager;
			private PriorityQueue<A> matchingList;

			public CombinationalChoiceFrom(P pattern, ChoiceMethod<P, A> comparator, ResourceSetManager<P, A> setManager)
			{
				this.pattern = pattern;
				this.setManager = setManager;
				matchingList = new PriorityQueue<A>(1, comparator);
			}

			public static abstract class Setter<P, T>
			{
				public abstract void set(P pattern, T resource);
			}

			public static abstract class ResourceSetManager<P, A>
			{
				public abstract A create(P pattern);
				public abstract void apply(P pattern, A set);
			}

			public static class Finder<P, T>
			{
				private Collection<T> reslist;
				private SimpleChoiceFrom<P, T> choice;
				private Setter<P, T> setter;

				public Finder(Collection<T> reslist, SimpleChoiceFrom<P, T> choice, Setter<P, T> setter)
				{
					this.reslist = reslist;
					this.choice  = choice;
					this.setter  = setter;
				}

				public <A> boolean find(P pattern, Iterator<Finder<P, ?>> finder, PriorityQueue<A> matchingList, ResourceSetManager<P, A> setManager)
				{
					Collection<T> all = choice.findAll(reslist);
					Iterator<T> iterator = all.iterator();
					if (finder.hasNext())
					{
						while (iterator.hasNext())
						{
							setter.set(pattern, iterator.next());
							if (finder.next().find(pattern, finder, matchingList, setManager))
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
								setter.set(pattern, all.iterator().next());
								return true;
							}
							else
							{
								for (T a : all)
								{
									setter.set(pattern, a);
									matchingList.add(setManager.create(pattern));
								}
								return true;
							}
						else
							return false;
					}
				}
			}

			private List<Finder<P, ?>> finders = new ArrayList<Finder<P, ?>>();

			public void addFinder(Finder<P, ?> finder)
			{
				finders.add(finder);
			}

			public boolean find()
			{
				if (finders.size() == 0)
					return false;

				Iterator<Finder<P, ?>> finder = finders.iterator();

				if (finder.next().find(pattern, finder, matchingList, setManager)
					&& matchingList.comparator() == null)
						return true;

				if (matchingList.size() > 0)
				{
					setManager.apply(pattern, matchingList.poll());
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

		import rdo_lib.Event;

		public abstract class Simulator
		{
			private static double time = 0;

			public static double getTime()
			{
				return time;
			}

			private static class PlannedEvent
			{
				private Event event;
				private double plannedFor;

				public Event getEvent()
				{
					return event;
				}

				public double getTimePlanned()
				{
					return plannedFor;
				}

				public PlannedEvent(Event event, double time)
				{
					this.event = event;
					this.plannedFor = time;
				}
			}

			private static Comparator<PlannedEvent> comparator = new Comparator<PlannedEvent>()
			{
				@Override
				public int compare(PlannedEvent x, PlannedEvent y)
				{
					if (x.getTimePlanned() < y.getTimePlanned()) return -1;
					if (x.getTimePlanned() > y.getTimePlanned()) return  1;
					return 0;
				}
			};

			private static PriorityQueue<PlannedEvent> eventList = new PriorityQueue<PlannedEvent>(1, comparator);

			public static void pushEvent(Event event, double time)
			{
				eventList.add(new PlannedEvent(event, time));
			}

			private static PlannedEvent popEvent()
			{
				return eventList.remove();
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
				while(eventList.size() > 0)
				{
					while(checkDPT());

					PlannedEvent current = popEvent();

					time = current.getTimePlanned();
					System.out.println("      " + String.valueOf(time) + ":	'" + current.getEvent().getName() + "' happens");

					current.getEvent().calculateEvent();

					for (TerminateCondition c : terminateList)
						if (c.check())
							return 1;
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

	def compileEvent()
	{
		'''
		package rdo_lib;

		public interface Event
		{
			public String getName();
			public void calculateEvent();
		}
		'''
	}

	def compileRule()
	{
		'''
		package rdo_lib;

		public interface Rule
		{
			public String getName();
			public boolean tryRule();
		}
		'''
	}

	def compileDecisionPoint()
	{
		'''
		
		'''
	}
}

