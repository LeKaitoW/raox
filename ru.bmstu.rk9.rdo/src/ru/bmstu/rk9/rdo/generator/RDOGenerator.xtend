package ru.bmstu.rk9.rdo.generator

import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.IGenerator
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

class RDOGenerator implements IGenerator
{
	//====== Extension methods ================================================================//
	def getModelRoot           (EObject o)            { RDONaming.getModelRoot           (o)    }
	def getNameGeneric         (EObject o)            { RDONaming.getNameGeneric         (o)    }
	def getFullyQualifiedName  (EObject o)            { RDONaming.getFullyQualifiedName  (o)    }
	def getContextQualifiedName(EObject o, EObject c) { RDONaming.getContextQualifiedName(o, c) }
	def compileType            (EObject o)            { RDOExpressionCompiler.compileType(o)    }
	//=========================================================================================//

	override void doGenerate(Resource resource, IFileSystemAccess fsa)
	{

		val filename = RDOQualifiedNameProvider.computeFromURI(resource.contents.head as RDOModel)
		
		fsa.generateFile("rdo_lib/Simulator.java", compileLibSimulator())
		
		for (e : resource.allContents.toIterable.filter(typeof(ResourceType)))
		{
			fsa.generateFile(filename + "/" + e.name + ".java", e.compileResourceType(filename))
		}
		
		if (filename.length > 0)
			fsa.generateFile("rdo_model/" + filename + "_ResourcesDeclaration.java", compileResources(resource.contents.head as RDOModel, filename))
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
	
	def compileResourceType(ResourceType rtp, String filename)
	{
		'''
		package «filename»;

		public class «rtp.name»
		{
			private static java.util.ArrayList<«rtp.name»> resources;

			private «rtp.name»(/*PARAMETERS*/)
			{
				
			}

			public static «rtp.name» addResource(/*PARAMETERS*/)
			{
				«rtp.name» res = new «rtp.name»(/*PARAMETERS*/);
				resources.add(res);
				return res;
			}

			public static int getResourceCount()
			{
				return resources.size();
			}
		
			public static «rtp.name» getResource(int index)
			{
				return resources.get(index);
			}
		
			public static int findResource(«rtp.name» resource)
			{
				return resources.indexOf(resource);
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
		}
		'''
	}

	def compileResources(RDOModel model, String filename)
	{
		'''
		package rdo_model;

		@SuppressWarnings("unused")
		
		class «filename»_ResourcesDeclaration
		{
			«filename»_ResourcesDeclaration()
			{
				«FOR r : model.eAllContents.toIterable.filter(typeof(ResourceDeclaration))»
					«r.reference.fullyQualifiedName» «r.name» = «r.reference.fullyQualifiedName».addResource(/**/);
				«ENDFOR»
			}
		}
		'''
	}

	def String getDefault(RDORTPParameterType parameter)
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
		
		public class Simulator
		{
			private static double time = 0;

			public static double getTime()
			{
				return time;
			}
		}
		'''
	}

}
