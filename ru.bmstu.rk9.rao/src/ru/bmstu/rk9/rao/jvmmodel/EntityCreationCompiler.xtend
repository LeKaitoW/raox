package ru.bmstu.rk9.rao.jvmmodel

import ru.bmstu.rk9.rao.rao.EntityCreation
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmVisibility

class EntityCreationCompiler extends RaoEntityCompiler {
	def static asField(EntityCreation entityCreation, JvmTypesBuilder jvmTypesBuilder,
		JvmTypeReferenceBuilder typeReferenceBuilder, JvmDeclaredType it, boolean isPreIndexingPhase) {
		initializeCurrent(jvmTypesBuilder, null)

		return entityCreation.toField(entityCreation.name, entityCreation.constructor.inferredType) [
			visibility = JvmVisibility.PUBLIC
			static = true
			final = true
			initializer = entityCreation.constructor
		]
	}
}
