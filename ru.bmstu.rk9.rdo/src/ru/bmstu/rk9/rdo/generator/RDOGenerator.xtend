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
						«r.reference.fullyQualifiedName».addResource("«r.name»", new «r.reference.fullyQualifiedName»());
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
		
			public static int getResourceCount()
			{
				return resources.size();
			}
		
			public static «rtp.name» getResource(String name)
			{
				return resources.get(name);
			}
		
			public static java.util.Collection<«rtp.name»> getAll()
			{
				return resources.values();
			}

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

			public «rtp.name»(/*PARAMETERS*/)
			{
				
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
			public «evn.name»(/* PARAMETERS */)
			{
			}

			@Override
			public String getName()
			{
				return "«evn.fullyQualifiedName»";
			}
		
			@Override
			public void calculateEvent()
			{
				«FOR e : evn.algorithms»
					// Statement list
					«RDOStatementCompiler.compileStatement(e)»
				«ENDFOR»
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
