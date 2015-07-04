package ru.bmstu.rk9.rdo.generator

import org.eclipse.emf.common.util.URI

import org.eclipse.emf.ecore.resource.Resource

import org.eclipse.emf.ecore.EObject

import ru.bmstu.rk9.rdo.rdo.RDOModel

import ru.bmstu.rk9.rdo.rdo.ResourceType
import ru.bmstu.rk9.rdo.rdo.ParameterType
import ru.bmstu.rk9.rdo.rdo.ParameterTypeBasic
import ru.bmstu.rk9.rdo.rdo.ParameterTypeString
import ru.bmstu.rk9.rdo.rdo.ParameterTypeArray

import ru.bmstu.rk9.rdo.rdo.ResourceCreateStatement


import ru.bmstu.rk9.rdo.rdo.Sequence

import ru.bmstu.rk9.rdo.rdo.Constant

import ru.bmstu.rk9.rdo.rdo.Function
import ru.bmstu.rk9.rdo.rdo.FunctionParameter

import ru.bmstu.rk9.rdo.rdo.RelevantResource
import ru.bmstu.rk9.rdo.rdo.Event

import ru.bmstu.rk9.rdo.rdo.DecisionPoint

import ru.bmstu.rk9.rdo.rdo.Frame

import ru.bmstu.rk9.rdo.rdo.Result

import ru.bmstu.rk9.rdo.rdo.RDOInt
import ru.bmstu.rk9.rdo.rdo.RDODouble
import ru.bmstu.rk9.rdo.rdo.RDOBoolean
import ru.bmstu.rk9.rdo.rdo.RDOString
import ru.bmstu.rk9.rdo.rdo.RDOArray
import ru.bmstu.rk9.rdo.rdo.EnumDeclaration
import ru.bmstu.rk9.rdo.rdo.RDOEnum
import ru.bmstu.rk9.rdo.rdo.Pattern
import ru.bmstu.rk9.rdo.rdo.META_RelevantResourceType

class RDONaming
{
	def static getProjectName(URI uri)
	{
		return uri.toPlatformString(true).substring(1, uri.toPlatformString(true).indexOf("/", 1))
	}

	def static getResourceName(Resource resource)
	{
		var name = resource.getURI().lastSegment()
		if (name.endsWith(".rdo"))
			name = name.substring(0, name.length() - 4)
		name.replace(".", "_")
		return name
	}

	def static RDOModel getModelRoot(EObject object)
	{
		switch object
		{
			RDOModel: return object
			default : return object.eContainer.modelRoot
		}
	}

	def static getNameGeneric(EObject object)
	{
		switch object
		{
			RDOModel:
				return object.eResource.resourceName

			ResourceType:
				return object.name

			ParameterType:
				return object.name

			ResourceCreateStatement:
				return object.name

			Sequence:
				return object.name

			Constant:
				return object.name

			Function:
				return object.type.name

			FunctionParameter:
				return object.name

			Pattern:
				return object.name

			RelevantResource:
				return object.name

			Event:
				return object.name

			DecisionPoint:
				return object.name

			Frame:
				return object.name

			Result:
				return object.name

			default:
				return "ERROR"
		}
	}

	def static String getFullyQualifiedName(EObject object)
	{
		switch object
		{
			RDOModel:
				return object.nameGeneric

			ResourceType:
				return object.eContainer.nameGeneric + "." + object.name

			ParameterType:
				return object.eContainer.eContainer.nameGeneric +
					"." + object.eContainer.nameGeneric + "." + object.name

			ResourceCreateStatement:
				return object.eContainer.nameGeneric + "." + object.name

			Sequence:
				return object.eContainer.nameGeneric + "." + object.name

			Constant:
				return object.eContainer.eContainer.nameGeneric + "." + object.name

			Function:
				return object.eContainer.nameGeneric + "." + object.type.name

			FunctionParameter:
				return object.eContainer.eContainer.eContainer.fullyQualifiedName + "." + object.name

			Pattern:
				return object.eContainer.nameGeneric + "." + object.name

			Event:
				return object.eContainer.nameGeneric + "." + object.name

			DecisionPoint:
				return object.eContainer.nameGeneric + "." + object.name

			Frame:
				return object.eContainer.nameGeneric + "." + object.name

			Result:
				return object.eContainer.nameGeneric + "." + object.name

			EnumDeclaration:
				return object.eContainer.nameGeneric + "." + object.name

			default:
				return "ERROR"
		}
	}

	def static relevantResourceFullyQualifiedName(META_RelevantResourceType relevantResource)
	{
		if (relevantResource instanceof ResourceCreateStatement)
			return (relevantResource as ResourceCreateStatement).type.fullyQualifiedName
		else
			return relevantResource.fullyQualifiedName
	}

	def static getContextQualifiedName(EObject object, EObject context)
	{
		var objectName = object.fullyQualifiedName
		var contextName = context.fullyQualifiedName

		while (objectName.startsWith(contextName.substring(0,
			if (contextName.indexOf(".") > 0)	contextName.indexOf(".") else (contextName.length - 1))))
		{
			objectName = objectName.substring(objectName.indexOf(".") + 1)
			contextName = contextName.substring(contextName.indexOf(".") + 1)
		}
		return objectName
	}

	def static String getTypeGenericLabel(EObject type)
	{
		switch type
		{
			ParameterTypeBasic : getTypeGenericLabel(type.type)
			ParameterTypeString: getTypeGenericLabel(type.type)
			ParameterTypeArray : getTypeGenericLabel(type.type)

			RDOInt: " : " + type.type
			RDODouble   : " : " + type.type
			RDOBoolean: " : " + type.type
			RDOString : " : " + type.type
			RDOArray  : " : array" + getTypeGenericLabel(type.arrayType)
			RDOEnum: " : " + type.type

			default: ""
		}
	}
}
