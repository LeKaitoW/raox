package ru.bmstu.rk9.rdo.generator

import org.eclipse.emf.ecore.resource.Resource

import org.eclipse.xtext.generator.IFileSystemAccess

import ru.bmstu.rk9.rdo.rdo.RDOModel

import ru.bmstu.rk9.rdo.rdo.ResourceType

import ru.bmstu.rk9.rdo.rdo.ResourceDeclaration

import ru.bmstu.rk9.rdo.customizations.RDOQualifiedNameProvider

import ru.bmstu.rk9.rdo.generator.RDONaming
import ru.bmstu.rk9.rdo.generator.RDOExpressionCompiler
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterType
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterBasic
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterString
import ru.bmstu.rk9.rdo.rdo.RDOEnum
import ru.bmstu.rk9.rdo.rdo.Event
import ru.bmstu.rk9.rdo.customizations.IMultipleResourceGenerator

import org.eclipse.emf.ecore.resource.ResourceSet

import static extension org.eclipse.xtext.xbase.lib.IteratorExtensions.*
import ru.bmstu.rk9.rdo.rdo.ConstantDeclaration

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*
import static extension ru.bmstu.rk9.rdo.generator.RDOExpressionCompiler.*

class RDOGenerator implements IMultipleResourceGenerator
{
	override void doGenerate(Resource resource, IFileSystemAccess fsa)
	{}

	override void doGenerate(ResourceSet resources, IFileSystemAccess fsa)
	{
		//===== rdo_lib =======================================================
		fsa.generateFile("rdo_lib/Simulator.java",     compileLibSimulator ())
		fsa.generateFile("rdo_lib/Event.java", compileEvent())
		//=====================================================================

		for (resource : resources.resources)
			if (resource.contents.head != null)
			{
				val filename = RDOQualifiedNameProvider.computeFromURI(resource.contents.head as RDOModel)

				for (e : resource.allContents.toIterable.filter(typeof(ResourceType)))
				{
					fsa.generateFile(filename + "/" + e.name + ".java", e.compileResourceType(filename))
				}

				for (e : resource.allContents.toIterable.filter(typeof(Event)))
				{
					fsa.generateFile(filename + "/" + e.name + ".java", e.compileEvent(filename))
				}
			}

		fsa.generateFile("rdo_model/Constants.java", compileConstants(resources))

		fsa.generateFile("rdo_model/MainClass.java", compileMain(resources))
	}

	def compileMain(ResourceSet rs)
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
				System.out.println("   Source files are «rs.resources.map[r | r.contents.head.nameGeneric].toString»\n");
				System.out.println("   Initialization:");
				«FOR rl : rs.resources»
					«FOR r : rl.contents.head.eAllContents.filter(typeof(ResourceDeclaration)).toIterable»
						«r.reference.fullyQualifiedName».addResource("«r.name»", new «r.reference.fullyQualifiedName»(«
							if (r.parameters != null) r.parameters.compileExpression else ""»));
						System.out.println("      Added resource: '«r.name»' of type '«r.reference.fullyQualifiedName»'");
					«ENDFOR»
				«ENDFOR»

				System.out.println("\n   Started model");

				rdo_lib.Simulator.run();

				System.out.println("\n   Finished model in " + String.valueOf((System.currentTimeMillis() - startTime)/1000.0) + "s");

			}
		}
		'''
	}

	def compileConstants(ResourceSet rs)
	{
		'''
		package rdo_model;

		@SuppressWarnings("all")

		public class Constants
		{
			«FOR rl : rs.resources»«IF rl.contents.head.eAllContents.filter(typeof(ConstantDeclaration)).size > 0»
				private static class Constants_«rl.contents.head.nameGeneric»
				{
					«FOR r : rl.contents.head.eAllContents.filter(typeof(ConstantDeclaration)).toIterable»
						public static final «r.type.compileType» «r.name» = «r.value.compileExpression»;
					«ENDFOR»
				}

			«ENDIF»«ENDFOR»
			«FOR rl : rs.resources»«IF rl.contents.head.eAllContents.filter(typeof(ConstantDeclaration)).size > 0»
				public static final Constants_«rl.contents.head.nameGeneric» «rl.contents.head.nameGeneric» = new Constants_«rl.contents.head.nameGeneric»();
			«ENDIF»«ENDFOR»
		}
		'''
	}

	def compileResourceType(ResourceType rtp, String filename)
	{
		'''
		package «filename»;

		public class «rtp.name»
		{
			private static java.util.Map<String, «rtp.name»> resources = new java.util.HashMap<String, «rtp.name»>();

			public static void addResource(String name, «rtp.name» res)
			{
				resources.put(name, res);
			}

			public static «rtp.name» getResource(String name)
			{
				return resources.get(name);
			}

			«IF rtp.type.literal == "temporary"»
				private static java.util.Map<Integer, «rtp.name»> temporary = new java.util.HashMap<Integer, «rtp.name»>();

				private static java.util.Queue<Integer> vacantList = new java.util.LinkedList<Integer>();
				private static int currentLast = 0;

				public static void addResource(«rtp.name» res)
				{
					int number;
					if (vacantList.size() > 0)
						number = vacantList.poll();
					else
						number = currentLast++;
					
					res.number = number;
					temporary.put(number, res);
				}

				public static void eraseResource(«rtp.name» resource)
				{
					temporary.remove(resource.number);
					vacantList.add(resource.number);
				}

				public static java.util.Collection<«rtp.name»> getAll()
				{
					java.util.Collection<MSA> all = resources.values();
					all.addAll(temporary.values());		
					return all;
				}

				public static java.util.Collection<«rtp.name»> getTemporary()
				{
					return temporary.values();
				}

				private int number;
			«ELSE»
				public static java.util.Collection<«rtp.name»> getAll()
				{
					return resources.values();
				}
			«ENDIF»

			«IF rtp.eAllContents.filter(typeof(RDOEnum)).toList.size > 0»// ENUMS«ENDIF»
			«FOR e : rtp.eAllContents.toIterable.filter(typeof(RDOEnum))»
				enum «RDONaming.getEnumParentName(e, false)»_enum
				{
					«e.makeEnumBody»
				}

			«ENDFOR»
			// PARAMETERS
			«FOR parameter : rtp.parameters»
				public «parameter.type.compileType» «parameter.name»«parameter.type.getDefault»;
			«ENDFOR»

			public «rtp.name»(«
				IF rtp.parameters.size > 0»«rtp.parameters.get(0).type.compileType» «
					rtp.parameters.get(0).name»«
					FOR parameter : rtp.parameters.subList(1, rtp.parameters.size)», «
						parameter.type.compileType» «
						parameter.name»«
					ENDFOR»«
				ENDIF»)
			{
				«FOR parameter : rtp.parameters»
					if («parameter.name» != null) this.«parameter.name» = «parameter.name»;
				«ENDFOR»
			}
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

	def compileEvent(Event evn, String filename)
	{
		'''
		package «filename»;

		public class «evn.name» extends rdo_lib.Event
		{
			«FOR parameter : evn.parameters»
				private «parameter.type.compileType» «parameter.name»«parameter.type.getDefault»;
			«ENDFOR»

			public «evn.name»(«
			IF evn.parameters.size > 0»«evn.parameters.get(0).type.compileType» «
				evn.parameters.get(0).name»«
				FOR parameter : evn.parameters.subList(1, evn.parameters.size)», «
					parameter.type.compileType» «
					parameter.name»«
				ENDFOR»«
			ENDIF»)
			{
				«FOR parameter : evn.parameters»
					if («parameter.name» != null) this.«parameter.name» = «parameter.name»;
				«ENDFOR»
			}

			@Override
			public String getName()
			{
				return "«evn.fullyQualifiedName»";
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
							(r.type as ResourceDeclaration).reference.fullyQualifiedName».getResource("«
								(r.type as ResourceDeclaration).name»");
					«ENDIF»
				«ENDFOR»

				«FOR e : evn.algorithms»
					«RDOStatementCompiler.compileStatement(e)»

				«ENDFOR»
				«IF evn.relevantresources.map[t | t.type].filter(typeof(ResourceType)).size > 0»
					// add created resources
					«FOR r : evn.relevantresources»
						«IF r.type instanceof ResourceType»
							«(r.type as ResourceType).fullyQualifiedName».addResource(«r.name»);
						«ENDIF»
					«ENDFOR»
				«ENDIF»
			}
		}
		'''
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

	def compileLibSimulator()
	{
		'''
		package rdo_lib;

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

			public static void run()
			{
				while(eventList.size() > 0)
				{
					PlannedEvent current = popEvent();

					time = current.getTimePlanned();
					System.out.println("      " + String.valueOf(time) + ":	'" + current.getEvent().getName() + "' happens");

					current.getEvent().calculateEvent();
				}
			}
		}
		'''
	}

	def compileEvent()
	{
		'''
		package rdo_lib;

		public abstract class Event
		{
			public abstract String getName();
			public abstract void calculateEvent();
		}
		'''
	}
}
