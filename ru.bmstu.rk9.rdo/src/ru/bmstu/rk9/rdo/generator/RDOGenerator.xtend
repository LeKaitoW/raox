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
					«RDOStatementCompiler.compileStatement(e)»

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

			«FOR relres : rule.relevantresources»
				«IF relres.type instanceof ResourceDeclaration»
					public «(relres.type as ResourceDeclaration).reference.fullyQualifiedName» «
						relres.name» = «(relres.type as ResourceDeclaration).reference.fullyQualifiedName».«
							(relres.type as ResourceDeclaration).name»;
				«ELSE»
					public «(relres.type as ResourceType).fullyQualifiedName» «
						relres.name»;
				«ENDIF»
			«ENDFOR»

			@Override
			public boolean tryRule()
			{
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
				«FOR e : rule.algorithms»
					«RDOStatementCompiler.compileStatement(e)»

				«ENDFOR»
				«IF rule.relevantresources.filter[t | t.rule.literal == 'Create'].map[t | t.type].size > 0»
					// add created resources
					«FOR r : rule.relevantresources.filter[t | t.rule.literal == 'Create']»
						«IF r.type instanceof ResourceType»
							«(r.type as ResourceType).fullyQualifiedName».getManager().addResource(«r.name»);
						«ENDIF»
					«ENDFOR»
				«ENDIF»

				return true;
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
					return «IF havecf»(«cf.compileExpression»)«ELSE»true«ENDIF»«FOR r : relreslist»«IF relres != r»	
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
						if («cm.compileExpression»)
							return  1;
						else
							return -1;
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
			private ChoiceMethod<P, T> comparator;

			public SimpleChoiceFrom(Checker<P, T> checker, ChoiceMethod<P, T> comparator)
			{
				if (checker != null)
					this.checker = checker;

				if (comparator != null)
					 matchingList = new PriorityQueue<T>(1, comparator);
				else
					this.comparator = comparator;
			}

			private PriorityQueue<T> matchingList;

			public T find(Collection<T> reslist)
			{
				T res;
				for (Iterator<T> iterator = reslist.iterator(); iterator.hasNext();)
				{
					res = iterator.next();

					if (checker == null || checker.check(res))
						if (comparator == null)
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

	def compileLibSimulator()
	{
		'''
		package rdo_lib;

		import java.util.List;
		import java.util.ArrayList;

		import java.util.Comparator;
		import java.util.PriorityQueue;

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

			private static List<TerminateCondition> terminateList = new ArrayList<TerminateCondition>();

			public static void addTerminateCondition(TerminateCondition c)
			{
				terminateList.add(c);
			}

			public static int run()
			{
				while(eventList.size() > 0)
				{
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
}
