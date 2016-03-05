package ru.bmstu.rk9.rao.jvmmodel

import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.naming.QualifiedName
import ru.bmstu.rk9.rao.rao.Generator
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder

class GeneratorCompiler extends RaoEntityCompiler {
	def static asClass(Generator generator, JvmTypesBuilder jvmTypesBuilder,
		JvmTypeReferenceBuilder typeReferenceBuilder, JvmDeclaredType it, boolean isPreIndexingPhase) {
		initializeCurrent(jvmTypesBuilder, typeReferenceBuilder);

		return generator.toClass(QualifiedName.create(qualifiedName, generator.name)) [
			static = true

			superTypes += typeRef(ru.bmstu.rk9.rao.lib.sequence.Generator, {
				generator.type
			})

			members += generator.toConstructor [
				visibility = JvmVisibility.PUBLIC
				for (param : generator.parameters)
					parameters += param.toParameter(param.name, param.parameterType)
				body = '''
					«FOR param : parameters»this.«param.name» = «param.name»;
					«ENDFOR»
				'''
			]

			for (param : generator.parameters)
				members += param.toField(param.name, param.parameterType)

			members += generator.toMethod("run", typeRef(void)) [
				visibility = JvmVisibility.PUBLIC
				annotations += generateOverrideAnnotation()
				body = generator.body
			]
		]
	}
}
