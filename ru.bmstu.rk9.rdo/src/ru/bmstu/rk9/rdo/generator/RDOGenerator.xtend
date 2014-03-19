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

class RDOGenerator implements IGenerator
{
	//====== Extension methods ================================================================//
	def getModelRoot           (EObject o)            { RDONaming.getModelRoot           (o)    }
	def getNameGeneric         (EObject o)            { RDONaming.getNameGeneric         (o)    }
	def getFullyQualifiedName  (EObject o)            { RDONaming.getFullyQualifiedName  (o)    }
	def getContextQualifiedName(EObject o, EObject c) { RDONaming.getContextQualifiedName(o, c) }
	def compileType            (EObject o)            { RDONaming.compileType            (o)    }
	def getTypeGeneric         (EObject o)            { RDONaming.getTypeGeneric         (o)    }
	//=========================================================================================//

	override void doGenerate(Resource resource, IFileSystemAccess fsa)
	{

		val filename = RDOQualifiedNameProvider.computeFromURI(resource.contents.head as RDOModel)
		
		fsa.generateFile("rdo_lib/ResourceType.java", makeResourceType())
		fsa.generateFile("rdo_lib/ResourceTypeList.java", makeResourceTypeList())
		
		for (e : resource.allContents.toIterable.filter(typeof(ResourceType)))
		{
			fsa.generateFile(filename + "/" + e.name + ".java", e.compileResourceType(filename))
			fsa.generateFile("model/" + filename + "/" + e.name + "Factory.java", e.compileResourceFactory(filename))
		}
		
		if (filename.length > 0)
			fsa.generateFile("model/" + filename + "/ResourcesDeclaration.java", compileResources(resource.contents.head as RDOModel, filename))
	}
	
	def makeResourceType()
	{
		'''
		package rdo_lib;

		public abstract class ResourceType {}
		'''
	}

	def makeResourceTypeList()
	{
		'''
		package rdo_lib;
		
		import java.util.ArrayList;
		
		public abstract class ResourceTypeList
		{
			protected ArrayList<ResourceType> resources;
			
			public abstract ResourceType addResource();
		
			public int getResourceCount()
			{
				return resources.size();
			}
		
			public ResourceType getResource(int index)
			{
				return resources.get(index);
			}
		
			public int findResource(ResourceType resource)
			{
				return resources.indexOf(resource);
			}
		}
		'''
	}

	def compileResourceType(ResourceType rtp, String filename)
	{
		'''
		package «filename»;

		public class «rtp.name» extends rdo_lib.ResourceType
		{

			«FOR parameter : rtp.parameters»
			public «compileType(parameter.type)» «parameter.name»;
			«ENDFOR»

		}
		'''
	}
	
	def compileResourceFactory(ResourceType rtp, String filename)
	{
		'''
		package model.«filename»;

		public class «rtp.name»Factory extends rdo_lib.ResourceTypeList
		{
			public «filename».«rtp.name» addResource(/*SIGNATURE*/)
			{
				«filename».«rtp.name» res = new «filename».«rtp.name»();
				resources.add(res);
				return res;
			}
		}
		'''
	}	

	def compileResources(RDOModel model, String name)
	{
		'''
		package model.«name»;

		@SuppressWarnings("unused")
		
		class ResourcesDeclaration
		{

			ResourcesDeclaration()
			{
				«FOR r : model.eAllContents.toIterable.filter(typeof(ResourceType))»
					«r.name»Factory «r.name»List = new «r.name»Factory();
				«ENDFOR»

				«FOR r : model.eAllContents.toIterable.filter(typeof(ResourceDeclaration))»
					«r.reference.fullyQualifiedName» «r.name» = «r.reference.nameGeneric»List.addResource(/*PARAMETERS*/);
				«ENDFOR»

			}

		}
		'''
	}

}

