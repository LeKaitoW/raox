package ru.bmstu.rk9.rdo.generator

import org.eclipse.emf.common.util.URI

import org.eclipse.emf.ecore.resource.Resource

import org.eclipse.emf.ecore.EObject

import ru.bmstu.rk9.rdo.rdo.RDOModel

import ru.bmstu.rk9.rdo.rdo.ResourceType
import ru.bmstu.rk9.rdo.rdo.ResourceTypeParameter
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterType
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterBasic
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterString
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterEnum
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterSuchAs
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterArray

import ru.bmstu.rk9.rdo.rdo.ResourceDeclaration

import ru.bmstu.rk9.rdo.rdo.META_RelResType

import ru.bmstu.rk9.rdo.rdo.Sequence

import ru.bmstu.rk9.rdo.rdo.ConstantDeclaration

import ru.bmstu.rk9.rdo.rdo.Function
import ru.bmstu.rk9.rdo.rdo.FunctionParameter

import ru.bmstu.rk9.rdo.rdo.PatternParameter
import ru.bmstu.rk9.rdo.rdo.Operation
import ru.bmstu.rk9.rdo.rdo.OperationRelevantResource
import ru.bmstu.rk9.rdo.rdo.Rule
import ru.bmstu.rk9.rdo.rdo.RuleRelevantResource
import ru.bmstu.rk9.rdo.rdo.Event
import ru.bmstu.rk9.rdo.rdo.EventRelevantResource

import ru.bmstu.rk9.rdo.rdo.DecisionPoint

import ru.bmstu.rk9.rdo.rdo.Frame

import ru.bmstu.rk9.rdo.rdo.Results
import ru.bmstu.rk9.rdo.rdo.ResultDeclaration

import ru.bmstu.rk9.rdo.rdo.RDOInteger
import ru.bmstu.rk9.rdo.rdo.RDOReal
import ru.bmstu.rk9.rdo.rdo.RDOBoolean
import ru.bmstu.rk9.rdo.rdo.RDOString
import ru.bmstu.rk9.rdo.rdo.RDOEnum
import ru.bmstu.rk9.rdo.rdo.RDOSuchAs
import ru.bmstu.rk9.rdo.rdo.RDOArray
import ru.bmstu.rk9.rdo.rdo.RDOOwnType


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

			ResourceTypeParameter:
				return object.name

			ResourceDeclaration:
				return object.name

			Sequence:
				return object.name

			ConstantDeclaration:
				return object.name

			Function:
				return object.name

			FunctionParameter:
				return object.name

			PatternParameter:
				return object.name

			Operation:
				return object.name

			OperationRelevantResource:
				return object.name

			Rule:
				return object.name

			RuleRelevantResource:
				return object.name

			Event:
				return object.name

			EventRelevantResource:
				return object.name

			DecisionPoint:
				return object.name

			Frame:
				return object.name

			Results:
				return (if(object.name == null) "*null*" else object.name)

			ResultDeclaration:
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

			ResourceTypeParameter:
				return object.eContainer.eContainer.nameGeneric +
					"." + object.eContainer.nameGeneric + "." + object.name

			ResourceDeclaration:
				return object.eContainer.eContainer.nameGeneric + "." + object.name

			Sequence:
				return object.eContainer.nameGeneric + "." + object.name

			ConstantDeclaration:
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

			Results:
				return object.eContainer.nameGeneric + "." + object.nameGeneric

			ResultDeclaration:
				return object.eContainer.eContainer.nameGeneric + "." +
					(if(object.eContainer.nameGeneric != "*null*")
						(object.eContainer.nameGeneric + "_") else "") + object.name

			default:
				return "ERROR"
		}
	}

	def static relResFullyQualifiedName(META_RelResType relres)
	{
		if(relres instanceof ResourceDeclaration)
			return (relres as ResourceDeclaration).reference.fullyQualifiedName
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
			RDORTPParameterBasic : getTypeGenericLabel(type.type)
			RDORTPParameterString: getTypeGenericLabel(type.type)
			RDORTPParameterEnum  : getTypeGenericLabel(type.type)
			RDORTPParameterSuchAs: getTypeGenericLabel(type.type)
			RDORTPParameterArray : getTypeGenericLabel(type.type)

			RDOInteger: " : " + type.type
			RDOReal   : " : " + type.type
			RDOBoolean: " : " + type.type
			RDOString : " : " + type.type
			RDOEnum   : " : enumerative"
			RDOSuchAs : " : such_as " + switch type.eContainer
			{
				RDORTPParameterSuchAs:
					getContextQualifiedName(type.type, type.eContainer.eContainer.eContainer)
				default: getContextQualifiedName(type.type, type.modelRoot)
			}

			RDOArray  : " : array" + getTypeGenericLabel(type.arraytype)
			RDOOwnType: " : " + type.id.nameGeneric

			default: ""
		}
	}

	def static String getEnumParentName(RDOEnum enm, boolean isFQN)
	{
		var container = enm.eContainer

		while(container instanceof RDOArray)
		{
			container = container.eContainer
		}

		var doubleclass = true
		if(container instanceof RDORTPParameterType)
		{
			doubleclass = false
			container = container.eContainer
		}

		if(container instanceof FunctionParameter)
		{
			doubleclass = false
		}

		if(isFQN)
			return container.fullyQualifiedName + (if(doubleclass) ("." + container.nameGeneric) else "")
		else
			return container.nameGeneric
	}

}
