package ru.bmstu.rk9.rao.jvmmodel

import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import ru.bmstu.rk9.rao.rao.DefaultMethod
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import java.util.function.Supplier
import ru.bmstu.rk9.rao.validation.DefaultMethodsHelper

class DefaultMethodCompiler extends RaoEntityCompiler {
	def static asClass(DefaultMethod method, JvmTypesBuilder jvmTypesBuilder,
		JvmTypeReferenceBuilder typeReferenceBuilder, JvmDeclaredType it, boolean isPreIndexingPhase) {
		initializeCurrent(jvmTypesBuilder, typeReferenceBuilder);

		switch (method.name) {
			case DefaultMethodsHelper.GlobalMethodInfo.EXPERIMENTS.name:
				return method.initAsClass(it, isPreIndexingPhase)
			case DefaultMethodsHelper.GlobalMethodInfo.INIT.name:
				return method.initAsClass(it, isPreIndexingPhase)
			case DefaultMethodsHelper.GlobalMethodInfo.TERMINATE_CONDITION.name:
				return method.terminateAsClass(it, isPreIndexingPhase)
		}
	}

	def private static initAsClass(DefaultMethod method, JvmDeclaredType it, boolean isPreIndexingPhase) {
		return method.toClass(method.name) [
			superTypes += typeRef(Runnable)
			visibility = JvmVisibility.PROTECTED
			static = true
			members += method.toMethod("run", typeRef(void)) [
				visibility = JvmVisibility.PUBLIC
				final = true
				annotations += overrideAnnotation
				body = method.body
			]
		]
	}

	def private static terminateAsClass(DefaultMethod method, JvmDeclaredType it, boolean isPreIndexingPhase) {
		return method.toClass(method.name) [
			superTypes += typeRef(Supplier, {
				typeRef(Boolean)
			})
			visibility = JvmVisibility.PROTECTED
			static = true
			members += method.toMethod("get", typeRef(Boolean)) [
				visibility = JvmVisibility.PUBLIC
				final = true
				annotations += overrideAnnotation
				body = method.body
			]
		]
	}
}
