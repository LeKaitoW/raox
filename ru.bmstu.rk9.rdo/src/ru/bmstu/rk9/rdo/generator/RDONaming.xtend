package ru.bmstu.rk9.rdo.generator

import ru.bmstu.rk9.rdo.rdo.RDOModel
import org.eclipse.emf.ecore.EObject
import ru.bmstu.rk9.rdo.rdo.ResourceType
import ru.bmstu.rk9.rdo.rdo.ResourceTypeParameter
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterBasic
import ru.bmstu.rk9.rdo.rdo.RDOInteger
import ru.bmstu.rk9.rdo.rdo.RDOReal
import ru.bmstu.rk9.rdo.rdo.RDOBoolean
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterString
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterEnum
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterSuchAs
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterArray
import ru.bmstu.rk9.rdo.rdo.RDOString
import ru.bmstu.rk9.rdo.rdo.RDOEnum
import ru.bmstu.rk9.rdo.rdo.RDOSuchAs
import ru.bmstu.rk9.rdo.rdo.RDOArray
import ru.bmstu.rk9.rdo.rdo.RDOOwnType
import ru.bmstu.rk9.rdo.rdo.ResourceDeclaration
import ru.bmstu.rk9.rdo.rdo.ConstantDeclaration
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterType

class RDONaming
{
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

			ResourceDeclaration:
				return object.name

			ConstantDeclaration:
				return object.name

			default:
				return "ERROR"
		}
	}

	def static getFullyQualifiedName(EObject object) 
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

	def static getContextQualifiedName(EObject object, EObject context)
	{
		var oname = object.fullyQualifiedName
		var cname = context.fullyQualifiedName
		
		while (oname.startsWith(cname.substring(0,
			if (cname.indexOf(".") > 0)	cname.indexOf(".") else (cname.length - 1))))
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

		while (container instanceof RDOArray)
		{
			container = container.eContainer
		}
		
		if (container instanceof RDORTPParameterType)
				container = container.eContainer
		
		if (isFQN)
			return container.fullyQualifiedName
		else
			return container.nameGeneric
	}

}