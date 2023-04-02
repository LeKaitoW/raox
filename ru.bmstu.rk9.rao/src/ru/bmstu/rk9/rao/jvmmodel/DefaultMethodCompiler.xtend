package ru.bmstu.rk9.rao.jvmmodel

import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import ru.bmstu.rk9.rao.rao.DefaultMethod
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import java.util.function.Supplier
import ru.bmstu.rk9.rao.validation.DefaultMethodsHelper
import java.util.function.Function

class DefaultMethodCompiler extends RaoEntityCompiler {
	def static asClass(DefaultMethod method, JvmTypesBuilder jvmTypesBuilder,
		JvmTypeReferenceBuilder typeReferenceBuilder, JvmDeclaredType it, boolean isPreIndexingPhase) {
		initializeCurrent(jvmTypesBuilder, typeReferenceBuilder);

		switch (method.name) {
			case DefaultMethodsHelper.GlobalMethodInfo.INIT.name:
				return method.asRunnable(it, isPreIndexingPhase)
			case DefaultMethodsHelper.GlobalMethodInfo.TERMINATE_CONDITION.name:
				return method.asSupplier(it, Boolean, isPreIndexingPhase)
			case DefaultMethodsHelper.GlobalMethodInfo.TIME_FORMAT.name:
				return method.asFunction(it, Double, "time", String, isPreIndexingPhase)
			case DefaultMethodsHelper.GlobalMethodInfo.TIME_START.name:
				return method.asSupplier(it, Double, isPreIndexingPhase)
		}
	}

	def private static asRunnable(DefaultMethod method, JvmDeclaredType it, boolean isPreIndexingPhase) {
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

	def private static asSupplier(DefaultMethod method, JvmDeclaredType it, Class<?> returnType,
		boolean isPreIndexingPhase) {
		return method.toClass(method.name) [
			superTypes += typeRef(Supplier, {
				typeRef(returnType)
			})
			visibility = JvmVisibility.PROTECTED
			static = true
			members += method.toMethod("get", typeRef(returnType)) [
				visibility = JvmVisibility.PUBLIC
				final = true
				annotations += overrideAnnotation
				body = method.body
			]
		]
	}

	def private static asFunction(DefaultMethod method, JvmDeclaredType it, Class<?> type, String argumentName,
		Class<?> returnType, boolean isPreIndexingPhase) {
		{
			typeRef(returnType)
			typeRef(type)
		}
		return method.toClass(method.name) [
			superTypes += typeRef(Function, {
				typeRef(type)
			}, {
				typeRef(returnType)
			})
			visibility = JvmVisibility.PROTECTED
			static = true
			members += method.toMethod("apply", typeRef(returnType)) [
				visibility = JvmVisibility.PUBLIC
				final = true
				annotations += overrideAnnotation
				parameters += method.toParameter(argumentName, typeRef(type))
				body = method.body
			]
		]
	}
}
