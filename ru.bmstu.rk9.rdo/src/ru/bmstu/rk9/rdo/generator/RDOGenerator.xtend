package ru.bmstu.rk9.rdo.generator

import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.IGenerator
import org.eclipse.xtext.generator.IFileSystemAccess

import ru.bmstu.rk9.rdo.rdo.RDOModel

import ru.bmstu.rk9.rdo.rdo.ResourceType

import ru.bmstu.rk9.rdo.rdo.ResourceDeclaration

import ru.bmstu.rk9.rdo.rdo.RDOType

import ru.bmstu.rk9.rdo.rdo.RDOEnum
import ru.bmstu.rk9.rdo.rdo.RDOSuchAs
import ru.bmstu.rk9.rdo.rdo.RDOArray
import ru.bmstu.rk9.rdo.rdo.RDOOwnType
import ru.bmstu.rk9.rdo.customizations.RDOQualifiedNameProvider
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterType
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterBasic
import ru.bmstu.rk9.rdo.rdo.RDOInteger
import ru.bmstu.rk9.rdo.rdo.RDOReal
import ru.bmstu.rk9.rdo.rdo.RDOBoolean
import org.eclipse.emf.ecore.EObject
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterString
import ru.bmstu.rk9.rdo.rdo.ResourceTypeParameter

class RDOGenerator implements IGenerator
{

	def RDOModel getModelRoot(EObject object)
	{
		switch object
		{
			RDOModel: return object
			default : return object.eContainer.modelRoot
		}
	}
	
	// Name getters
	def getNameGeneric(EObject object)
	{
		switch object
		{
			RDOModel:
			{
				var name = object.eResource().getURI().lastSegment()
				if (name.endsWith(".rdo"))
					name = name.substring(0, name.length() - 4)
				name.replace(".", "_")
				return name
			}

			ResourceType:
				return object.name

			ResourceTypeParameter:
				return object.name

			default:
				return "ERROR"
		}
	}
	
	def getFullyQualifiedName(EObject object) 
	{
		switch object
		{
			RDOModel:
				return object.nameGeneric

			ResourceType:
				return object.eContainer.nameGeneric + "." + object.name

			ResourceTypeParameter:
				return object.eContainer.eContainer.nameGeneric +
					"." + object.eContainer.nameGeneric + "." + object.name

			default:
				return "ERROR"
		}
	}

	def processType(EObject type)
	{
		switch type
		{
			RDORTPParameterBasic :
			{
				val basic = type.type
				switch basic
				{
					RDOInteger: "Integer"
					RDOReal: "Double"
					RDOBoolean: "Boolean"
					default: "Integer"
				}
			}
			RDORTPParameterString: "String"
			default: "Integer"
		}
	}

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
			public «processType(parameter.type)» «parameter.name»;
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

