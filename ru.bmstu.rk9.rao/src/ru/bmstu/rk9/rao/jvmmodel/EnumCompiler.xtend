package ru.bmstu.rk9.rao.jvmmodel

import ru.bmstu.rk9.rao.rao.EnumDeclaration
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder

class EnumCompiler extends RaoEntityCompiler {
	def static asType(EnumDeclaration enumDeclaration, JvmTypesBuilder jvmTypesBuilder,
		JvmTypeReferenceBuilder typeReferenceBuilder, JvmDeclaredType it, boolean isPreIndexingPhase) {
		initializeCurrent(jvmTypesBuilder, typeReferenceBuilder)

		return enumDeclaration.toEnumerationType(enumDeclaration.name) [
			visibility = JvmVisibility.PUBLIC
			static = true
			enumDeclaration.values.forEach [ value |
				members += enumDeclaration.toEnumerationLiteral(value)
			]
		]
	}
}
