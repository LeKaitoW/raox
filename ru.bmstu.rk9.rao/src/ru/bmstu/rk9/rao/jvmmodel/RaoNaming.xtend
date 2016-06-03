package ru.bmstu.rk9.rao.jvmmodel

import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import ru.bmstu.rk9.rao.rao.Activity
import ru.bmstu.rk9.rao.rao.Edge
import ru.bmstu.rk9.rao.rao.FieldDeclaration
import ru.bmstu.rk9.rao.rao.RaoEntity
import ru.bmstu.rk9.rao.rao.RaoModel
import ru.bmstu.rk9.rao.rao.RelevantResource

class RaoNaming {
	def static getProjectName(URI uri) {
		return uri.toPlatformString(true).substring(1, uri.toPlatformString(true).indexOf("/", 1))
	}

	def static getModelResourceName(Resource resource) {
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
				return object.eResource.modelResourceName
			RaoEntity:
				return object.name
			FieldDeclaration:
				return object.declaration.name
			RelevantResource:
				return object.name
			Edge:
				return object.name
			Activity:
				return object.name
			default:
				return "ERROR"
		}
	}

	def static String getFullyQualifiedName(EObject object) {
		switch object {
			RaoModel:
				return object.nameGeneric
			RaoEntity:
				return object.eContainer.nameGeneric + "." + object.name
			FieldDeclaration:
				return object.eContainer.eContainer.nameGeneric + "." + object.eContainer.nameGeneric + "." +
					object.declaration.name
			RelevantResource:
				return object.eContainer.eContainer.nameGeneric + "." + object.eContainer.nameGeneric + "." +
					object.name
			Edge:
				return object.eContainer.eContainer.nameGeneric + "." + object.eContainer.nameGeneric + "." +
					object.name
			Activity:
				return object.eContainer.eContainer.nameGeneric + "." + object.eContainer.nameGeneric + "." +
					object.name
			default:
				return "ERROR"
		}
	}
}
