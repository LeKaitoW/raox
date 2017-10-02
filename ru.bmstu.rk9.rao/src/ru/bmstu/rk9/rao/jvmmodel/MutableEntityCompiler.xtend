package ru.bmstu.rk9.rao.jvmmodel

import ru.bmstu.rk9.rao.rao.MutableEntity
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.common.types.util.TypeReferences
import com.google.inject.Inject
import java.util.Iterator

class MutableEntityCompiler extends RaoEntityCompiler {

	@Inject
	static TypeReferences typeReferences;

	def static asField(MutableEntity mutableEntity, JvmTypesBuilder jvmTypesBuilder,
		JvmTypeReferenceBuilder typeReferenceBuilder, JvmDeclaredType it, boolean isPreIndexingPhase) {
		initializeCurrent(jvmTypesBuilder, null)

		return mutableEntity.toField(mutableEntity.name, mutableEntity.constructor.inferredType) [
			visibility = JvmVisibility.PUBLIC
			static = true
			//type = typeReferences.getTypeForName(Iterator, mutableEntity, null)
			initializer = mutableEntity.constructor
		]
	}
}
