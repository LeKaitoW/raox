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

import ru.bmstu.rk9.rdo.rdo.META_RelResType

import ru.bmstu.rk9.rdo.rdo.Sequence

import ru.bmstu.rk9.rdo.rdo.Constant

import ru.bmstu.rk9.rdo.rdo.Function
import ru.bmstu.rk9.rdo.rdo.FunctionParameter

import ru.bmstu.rk9.rdo.rdo.Operation
import ru.bmstu.rk9.rdo.rdo.SelectableRelevantResource
import ru.bmstu.rk9.rdo.rdo.Rule
import ru.bmstu.rk9.rdo.rdo.Event
import ru.bmstu.rk9.rdo.rdo.EventRelevantResource

import ru.bmstu.rk9.rdo.rdo.DecisionPoint

import ru.bmstu.rk9.rdo.rdo.Frame

import ru.bmstu.rk9.rdo.rdo.Result

import ru.bmstu.rk9.rdo.rdo.RDOInteger
import ru.bmstu.rk9.rdo.rdo.RDOReal
import ru.bmstu.rk9.rdo.rdo.RDOBoolean
import ru.bmstu.rk9.rdo.rdo.RDOString
import ru.bmstu.rk9.rdo.rdo.RDOArray
import ru.bmstu.rk9.rdo.rdo.EnumDeclaration
import ru.bmstu.rk9.rdo.rdo.RDOEnum

class RDONaming
{
	def static getProjectName(URI uri)
	{
		return uri.toPlatformString(true).substring(1, uri.toPlatformString(true).indexOf("/", 1))
	}

	def static getResourceName(Resource res)
	{
		var name = res.getURI().lastSegment()
		if(name.endsWith(".rdo"))
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
				return object.name

			FunctionParameter:
				return object.name

			Operation:
				return object.name

			SelectableRelevantResource:
				return object.name

			Rule:
				return object.name

			Event:
				return object.name

			EventRelevantResource:
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
				return object.eContainer.eContainer.nameGeneric + "." + object.name

			Sequence:
				return object.eContainer.nameGeneric + "." + object.name

			Constant:
				return object.eContainer.eContainer.nameGeneric + "." + object.name

			Function:
				return object.eContainer.nameGeneric + "." + object.name

			FunctionParameter:
				return object.eContainer.eContainer.eContainer.fullyQualifiedName + "." + object.name

			Operation:
				return object.eContainer.nameGeneric + "." + object.name

			Rule:
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

	def static relResFullyQualifiedName(META_RelResType relres)
	{
		if(relres instanceof ResourceCreateStatement)
			return (relres as ResourceCreateStatement).reference.fullyQualifiedName
		else
			return relres.fullyQualifiedName
	}

	def static getContextQualifiedName(EObject object, EObject context)
	{
		var oname = object.fullyQualifiedName
		var cname = context.fullyQualifiedName

		while(oname.startsWith(cname.substring(0,
			if(cname.indexOf(".") > 0)	cname.indexOf(".") else (cname.length - 1))))
		{
			oname = oname.substring(oname.indexOf(".") + 1)
			cname = cname.substring(cname.indexOf(".") + 1)
		}
		return oname
	}

	def static String getTypeGenericLabel(EObject type)
	{
		switch type
		{
			ParameterTypeBasic : getTypeGenericLabel(type.type)
			ParameterTypeString: getTypeGenericLabel(type.type)
			ParameterTypeArray : getTypeGenericLabel(type.type)

			RDOInteger: " : " + type.type
			RDOReal   : " : " + type.type
			RDOBoolean: " : " + type.type
			RDOString : " : " + type.type
			RDOArray  : " : array" + getTypeGenericLabel(type.arraytype)
			RDOEnum: " : " + type.type

			default: ""
		}
	}
}
