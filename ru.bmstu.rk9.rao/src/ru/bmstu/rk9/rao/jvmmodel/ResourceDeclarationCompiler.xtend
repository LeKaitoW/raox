package ru.bmstu.rk9.rao.jvmmodel

import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import org.eclipse.xtext.common.types.JvmDeclaredType
import ru.bmstu.rk9.rao.rao.ResourceDeclaration
import org.eclipse.xtext.common.types.JvmVisibility
import ru.bmstu.rk9.rao.rao.RaoModel

import org.eclipse.xtext.naming.QualifiedName

class ResourceDeclarationCompiler extends RaoEntityCompiler {
	def static asGlobalInitializationMethod(RaoModel model, JvmTypesBuilder jvmTypesBuilder,
		JvmTypeReferenceBuilder typeReferenceBuilder, JvmDeclaredType it, boolean isPreIndexingPhase) {

		initializeCurrent(jvmTypesBuilder, typeReferenceBuilder)

		val resources = model.objects.filter(typeof(ResourceDeclaration))
		val modelQualifiedNamePart = qualifiedName

		return model.toClass("resourcesPreinitializer") [
			superTypes += typeRef(Runnable)
			visibility = JvmVisibility.PROTECTED
			static = true

			members += model.toMethod("run", typeRef(void)) [
				visibility = JvmVisibility.PUBLIC
				final = true
				annotations += overrideAnnotation
				body = '''
					«FOR resource : resources»
						«val resourceQualifiedName = QualifiedName.create(modelQualifiedNamePart, resource.name)»
						ru.bmstu.rk9.rao.lib.resource.Resource «resource.name» = initialize«resource.name.toFirstUpper»();
						«resource.name».setName("«resourceQualifiedName»");
					«ENDFOR»
				'''
			]
		]
	}

	def static asInitializationMethod(ResourceDeclaration resource, JvmTypesBuilder jvmTypesBuilder,
		JvmTypeReferenceBuilder typeReferenceBuilder, JvmDeclaredType it, boolean isPreIndexingPhase) {

		initializeCurrent(jvmTypesBuilder, typeReferenceBuilder)

		return resource.toMethod("initialize" + resource.name.toFirstUpper, resource.constructor.inferredType) [
			visibility = JvmVisibility.PRIVATE
			final = true
			static = true
			body = resource.constructor
		]
	}

	def static asGetter(ResourceDeclaration resource, JvmTypesBuilder jvmTypesBuilder,
		JvmTypeReferenceBuilder typeReferenceBuilder, JvmDeclaredType it, boolean isPreIndexingPhase) {

		initializeCurrent(jvmTypesBuilder, typeReferenceBuilder)
		val resourceQualifiedName = QualifiedName.create(qualifiedName, resource.name)

		return resource.toMethod("get" + resource.name.toFirstUpper, resource.constructor.inferredType) [
			visibility = JvmVisibility.
				PUBLIC
			final = true
			static = true
			body = '''
				return («returnType.simpleName») ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.getModelState().getResource(
						«returnType.simpleName».class,
						"«resourceQualifiedName»");
			'''
		]
	}
}
