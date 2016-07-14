package ru.bmstu.rk9.rao.jvmmodel

import ru.bmstu.rk9.rao.jvmmodel.RaoEntityCompiler
import ru.bmstu.rk9.rao.rao.ResultType
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.common.types.JvmVisibility
import ru.bmstu.rk9.rao.lib.result.ResultMode
import ru.bmstu.rk9.rao.lib.result.Statistics
import ru.bmstu.rk9.rao.validation.DefaultMethodsHelper.ResultTypeMethodInfo
import org.eclipse.xtext.common.types.JvmTypeReference
import ru.bmstu.rk9.rao.rao.Result

class ResultCompiler extends RaoEntityCompiler {
	def static asClass(ResultType result, JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder typeReferenceBuilder,
		JvmDeclaredType it, boolean isPreIndexingPhase) {
			
		return result.toClass(QualifiedName.create(qualifiedName, result.name)) [
			static = true
			superTypes += typeRef(ru.bmstu.rk9.rao.lib.result.Result, {result.evaluateType})

			members += result.toConstructor [
				visibility = JvmVisibility.PUBLIC
				for (param : result.parameters)
					parameters += param.toParameter(param.name, param.parameterType)
				parameters += result.toParameter("resultMode", typeRef(ResultMode))
				parameters += result.toParameter("statistics", typeRef(Statistics, {result.evaluateType}))
				body = '''
					«FOR param : parameters»this.«param.name» = «param.name»;
					«ENDFOR»
				'''
			]
			
			members += result.toConstructor [
				visibility = JvmVisibility.PUBLIC
				for (param : result.parameters)
					parameters += param.toParameter(param.name, param.parameterType)
				parameters += result.toParameter("resultMode", typeRef(ResultMode))
				body = '''
					this(«FOR param : parameters»«param.name», «ENDFOR»getDefaultStatistics());
				'''
			]
			
			members += result.toConstructor [
				visibility = JvmVisibility.PUBLIC
				for (param : result.parameters)
					parameters += param.toParameter(param.name, param.parameterType)
				parameters += result.toParameter("statistics", typeRef(Statistics, {result.evaluateType}))
				body = '''
					this(«FOR param : result.parameters»«param.name», «ENDFOR»ResultMode.AUTO, statistics);
				'''
				
			]
			
			members += result.toConstructor [
				visibility = JvmVisibility.PUBLIC
				for (param : result.parameters)
					parameters += param.toParameter(param.name, param.parameterType)
				body = '''
					this(«FOR param : parameters»«param.name», «ENDFOR»ResultMode.AUTO, getDefaultStatistics());
				'''
			]

			members += result.toMethod("getDefaultStatistics", typeRef(Statistics, {result.evaluateType})) [
				val evaluateType = result.evaluateType.type;
				visibility = JvmVisibility.PUBLIC
				final = true
				static = true
				body = '''
					if (Number.class.isAssignableFrom(«evaluateType».class))
						return new ru.bmstu.rk9.rao.lib.result.WeightedStorelessNumericStatistics();
					else if (Enum.class.isAssignableFrom(«evaluateType».class) ||
							String.class.isAssignableFrom(«evaluateType».class) ||
							Boolean.class.isAssignableFrom(«evaluateType».class))
						return new ru.bmstu.rk9.rao.lib.result.CategoricalStatistics<«evaluateType»>();
					else
						return new ru.bmstu.rk9.rao.lib.result.ValueStatistics<«evaluateType»>();
				'''
			]

			for (param : result.parameters)
				members += param.toField(param.name, param.parameterType)

			if (!isPreIndexingPhase) {
				for (method : result.defaultMethods) {

					var JvmTypeReference defaultMethodReturnType;

					switch (method.name) {
						case ResultTypeMethodInfo.EVALUATE.name: {
							defaultMethodReturnType = result.evaluateType;
						}
						case ResultTypeMethodInfo.CONDITION.name: {
							defaultMethodReturnType = typeRef(boolean);
						}
					}

					members += method.toMethod(method.name, defaultMethodReturnType) [
						visibility = JvmVisibility.PUBLIC
						final = true
						annotations += overrideAnnotation()
						body = method.body
					]
				}
			}
		]
			
	}
	
	def static asField(Result result, JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder typeReferenceBuilder,
		JvmDeclaredType it, boolean isPreIndexingPhase) {
		return toField(result.name, result.constructor.inferredType) [
			visibility = JvmVisibility.PUBLIC
			static = true
			final = true
			initializer = result.constructor
		]
	}
}
