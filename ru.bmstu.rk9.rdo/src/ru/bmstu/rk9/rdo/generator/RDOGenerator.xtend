package ru.bmstu.rk9.rdo.generator

import org.eclipse.emf.ecore.resource.Resource

import org.eclipse.xtext.generator.IFileSystemAccess

import ru.bmstu.rk9.rdo.rdo.RDOModel

import ru.bmstu.rk9.rdo.rdo.ResourceType

import ru.bmstu.rk9.rdo.rdo.ResourceDeclaration

import ru.bmstu.rk9.rdo.customizations.RDOQualifiedNameProvider

import org.eclipse.emf.ecore.EObject

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

class RDOGenerator implements IMultipleResourceGenerator
{
	//====== Extension methods ================================================================//
	def getModelRoot           (EObject o)            { RDONaming.getModelRoot           (o)    }
	def getNameGeneric         (EObject o)            { RDONaming.getNameGeneric         (o)    }
	def getFullyQualifiedName  (EObject o)            { RDONaming.getFullyQualifiedName  (o)    }
	def getContextQualifiedName(EObject o, EObject c) { RDONaming.getContextQualifiedName(o, c) }
	def compileType            (EObject o)            { RDOExpressionCompiler.compileType(o)    }
	//=========================================================================================//
	
	override void doGenerate(Resource resource, IFileSystemAccess fsa)
	{}

	override void doGenerate(ResourceSet resources, IFileSystemAccess fsa)
	{
		//===== rdo_lib =======================================================
		fsa.generateFile("rdo_lib/Simulator.java",     compileLibSimulator ())
		fsa.generateFile("rdo_lib/AbstractEvent.java", compileAbstractEvent())
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
		
				if (filename.length > 0)
					fsa.generateFile("rdo_model/" + filename + "_ResourcesDeclaration.java", compileResources(resource.contents.head as RDOModel, filename))
			}

		fsa.generateFile("rdo_model/MainClass.java", compileMain(resources))
	}
	
	def compileMain(ResourceSet rs)
	{
		'''
		package rdo_model;
		
		class MainClass
		{
			public static void main(String[] args)
			{
				«FOR r : rs.resources.map(r|r.allContents.toIterable.filter(typeof(ResourceDeclaration)))»
				
				«ENDFOR»
			}
		}
		'''
	}
	
	def compileResourceType(ResourceType rtp, String filename)
	{
		'''
		package «filename»;

		public class «rtp.name»
		{
			private static java.util.Map<String, «rtp.name»> resources;

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
			public «compileType(parameter.type)» «parameter.name»«parameter.type.getDefault»;
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
		
		public class «evn.name» extends rdo_lib.AbstractEvent
		{
			public «evn.name»(double time)
			{
				super(time);
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

	def compileResources(RDOModel model, String filename)
	{
		'''
		package rdo_model;

		class «filename»_ResourcesDeclaration
		{
			«filename»_ResourcesDeclaration()
			{
				«FOR r : model.eAllContents.toIterable.filter(typeof(ResourceDeclaration))»
					«r.reference.fullyQualifiedName».addResource("«r.name»", new «r.reference.fullyQualifiedName»());
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
		
		import rdo_lib.AbstractEvent;
		
		public class Simulator
		{
			private static double time = 0;
		
			public static double getTime()
			{
				return time;
			}
		
			private static EventTimeComparator comparator;
			private static PriorityQueue<AbstractEvent> eventList = new PriorityQueue<AbstractEvent>(0, comparator);
		
			public static void pushEvent(AbstractEvent event)
			{
				eventList.add(event);
			}
			
			public static AbstractEvent popEvent()
			{
				return eventList.remove();
			}
		
			private class EventTimeComparator implements Comparator<AbstractEvent>
			{
				@Override
			    public int compare(AbstractEvent x, AbstractEvent y)
			    {
		    		if (x.getTimePlanned() < y.getTimePlanned()) return -1;
					if (x.getTimePlanned() > y.getTimePlanned()) return  1;
					return 0;
			    }
			}
		}
		'''
	}

	def compileAbstractEvent()
	{
		'''
		package rdo_lib;

		public abstract class AbstractEvent
		{
			public AbstractEvent(double time)
			{
				this.plannedFor = time;
			}

			double plannedFor;

			public double getTimePlanned()
			{
				return plannedFor;
			}

			public abstract void calculateEvent();
		}
		'''
	}
}
