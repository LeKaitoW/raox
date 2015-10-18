package ru.bmstu.rk9.rao.jvmmodel

import org.eclipse.emf.common.util.URI

import org.eclipse.emf.ecore.resource.Resource

import org.eclipse.emf.ecore.EObject

import ru.bmstu.rk9.rao.rao.RaoModel

import ru.bmstu.rk9.rao.rao.ResourceType
import ru.bmstu.rk9.rao.rao.Parameter

import ru.bmstu.rk9.rao.rao.Sequence

import ru.bmstu.rk9.rao.rao.Constant

import ru.bmstu.rk9.rao.rao.FunctionDeclaration

import ru.bmstu.rk9.rao.rao.RelevantResource
import ru.bmstu.rk9.rao.rao.Event

import ru.bmstu.rk9.rao.rao.DecisionPoint

import ru.bmstu.rk9.rao.rao.Frame

import ru.bmstu.rk9.rao.rao.Result

import ru.bmstu.rk9.rao.rao.EnumDeclaration
import ru.bmstu.rk9.rao.rao.Pattern

class RaoNaming
{
	def static getProjectName(URI uri)
	{
		return uri.toPlatformString(true).substring(1, uri.toPlatformString(true).indexOf("/", 1))
	}

	def static getResourceName(Resource resource)
	{
		var name = resource.getURI().lastSegment()
		if (name.endsWith(".rao"))
			name = name.substring(0, name.length() - 4)
		name.replace(".", "_")
		return name
	}

	def static RaoModel getModelRoot(EObject object)
	{
		switch object
		{
			RaoModel: return object
			default : return object.eContainer.modelRoot
		}
	}

	def static getNameGeneric(EObject object)
	{
		switch object
		{
			RaoModel:
				return object.eResource.resourceName

			ResourceType:
				return object.name

			Parameter:
				return object.name

			Sequence:
				return object.name

			Constant:
				return object.constant.name

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
			RaoModel:
				return object.nameGeneric

			ResourceType:
				return object.eContainer.nameGeneric + "." + object.name

			Parameter:
				return object.eContainer.eContainer.nameGeneric +
					"." + object.eContainer.nameGeneric + "." + object.name

			Sequence:
				return object.eContainer.nameGeneric + "." + object.name

			Constant:
				return object.eContainer.eContainer.nameGeneric + "." + object.constant.name

			FunctionDeclaration:
				return object.eContainer.nameGeneric + "." + object.name

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
}
