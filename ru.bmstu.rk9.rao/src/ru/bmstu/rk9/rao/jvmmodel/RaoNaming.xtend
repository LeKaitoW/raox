package ru.bmstu.rk9.rao.jvmmodel

import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import ru.bmstu.rk9.rao.rao.EntityCreation
import ru.bmstu.rk9.rao.rao.EnumDeclaration
import ru.bmstu.rk9.rao.rao.Event
import ru.bmstu.rk9.rao.rao.FieldDeclaration
import ru.bmstu.rk9.rao.rao.Frame
import ru.bmstu.rk9.rao.rao.FunctionDeclaration
import ru.bmstu.rk9.rao.rao.Pattern
import ru.bmstu.rk9.rao.rao.RaoModel
import ru.bmstu.rk9.rao.rao.RelevantResource
import ru.bmstu.rk9.rao.rao.ResourceType
import ru.bmstu.rk9.rao.rao.Result

class RaoNaming {
	def static getProjectName(URI uri) {
		return uri.toPlatformString(true).substring(1, uri.toPlatformString(true).indexOf("/", 1))
	}

	def static getResourceName(Resource resource) {
		var name = resource.getURI().lastSegment()
		if (name.endsWith(".rao"))
			name = name.substring(0, name.length() - 4)
		name.replace(".", "_")
		return name
	}

	def static RaoModel getModelRoot(EObject object) {
		var candidate = object
		while (!(candidate instanceof RaoModel))
			candidate = candidate.eContainer

		return candidate as RaoModel
	}

	def static getNameGeneric(EObject object) {
		switch object {
			RaoModel:
				return object.eResource.resourceName
			EntityCreation:
				return object.name
			ResourceType:
				return object.name
			FieldDeclaration:
				return object.declaration.name
			Pattern:
				return object.name
			RelevantResource:
				return object.name
			Event:
				return object.name
			Frame:
				return object.name
			Result:
				return object.name
			default:
				return "ERROR"
		}
	}

	def static String getFullyQualifiedName(EObject object) {
		switch object {
			RaoModel:
				return object.nameGeneric
			EntityCreation:
				return object.eContainer.nameGeneric + "." + object.name
			ResourceType:
				return object.eContainer.nameGeneric + "." + object.name
			FieldDeclaration:
				return object.eContainer.eContainer.nameGeneric + "." + object.eContainer.nameGeneric + "." +
					object.declaration.name
			FunctionDeclaration:
				return object.eContainer.nameGeneric + "." + object.name
			Pattern:
				return object.eContainer.nameGeneric + "." + object.name
			Event:
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
}
