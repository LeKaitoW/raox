package ru.bmstu.rk9.rao.jvmmodel

import com.google.inject.Inject
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.xbase.jvmmodel.AbstractModelInferrer
import org.eclipse.xtext.xbase.jvmmodel.IJvmDeclaredTypeAcceptor
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import ru.bmstu.rk9.rao.rao.DefaultMethod
import ru.bmstu.rk9.rao.rao.EnumDeclaration
import ru.bmstu.rk9.rao.rao.Event
import ru.bmstu.rk9.rao.rao.Frame
import ru.bmstu.rk9.rao.rao.FunctionDeclaration
import ru.bmstu.rk9.rao.rao.Generator
import ru.bmstu.rk9.rao.rao.Logic
import ru.bmstu.rk9.rao.rao.Pattern
import ru.bmstu.rk9.rao.rao.RaoModel
import ru.bmstu.rk9.rao.rao.ResourceDeclaration
import ru.bmstu.rk9.rao.rao.ResourceType
import ru.bmstu.rk9.rao.rao.Search
import java.util.Collection
import ru.bmstu.rk9.rao.rao.Result
import ru.bmstu.rk9.rao.rao.ResultType
import ru.bmstu.rk9.rao.lib.result.ResultMode
import ru.bmstu.rk9.rao.lib.result.Statistics
import org.eclipse.xtext.common.types.JvmTypeReference

import static extension ru.bmstu.rk9.rao.jvmmodel.DefaultMethodCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.EntityCreationCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.EnumCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.EventCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.FrameCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.FunctionCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.GeneratorCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.LogicCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.PatternCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.ResourceDeclarationCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.ResourceTypeCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.SearchCompiler.*
import static extension ru.bmstu.rk9.rao.naming.RaoNaming.*
import ru.bmstu.rk9.rao.rao.EntityCreation
import org.eclipse.xtext.common.types.JvmVisibility

class RaoJvmModelInferrer extends AbstractModelInferrer {
	@Inject extension JvmTypesBuilder jvmTypesBuilder

	def dispatch void infer(RaoModel element, IJvmDeclaredTypeAcceptor acceptor, boolean isPreIndexingPhase) {
		acceptor.accept(element.toClass(QualifiedName.create(element.eResource.URI.projectName, element.nameGeneric))) [
			for (entity : element.objects) {
				entity.compileRaoEntity(it, isPreIndexingPhase)
			}

			element.compileResourceInitialization(it, isPreIndexingPhase)
		]
	}

	def dispatch compileRaoEntity(EntityCreation entity, JvmDeclaredType it, boolean isPreIndexingPhase) {
		if (!isPreIndexingPhase && entity.constructor != null)
			members += entity.asField(jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase)
	}

	def dispatch compileRaoEntity(EnumDeclaration enumDeclaration, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += enumDeclaration.asType(jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase)
	}

	def dispatch compileRaoEntity(FunctionDeclaration function, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += function.asMethod(jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase)
	}

	def dispatch compileRaoEntity(DefaultMethod method, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += method.asClass(jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase)
	}

	def dispatch compileRaoEntity(ResourceType resourceType, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += resourceType.asClass(jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase)
	}

	def dispatch compileRaoEntity(Generator generator, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += generator.asClass(jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase)
	}

	def dispatch compileRaoEntity(Event event, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += event.asClass(jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase)
	}

	def dispatch compileRaoEntity(Pattern pattern, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += pattern.asClass(jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase);
	}

	def dispatch compileRaoEntity(Logic logic, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += logic.asClass(jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase);
	}

	def dispatch compileRaoEntity(Search search, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += search.asClass(jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase);
	}

	def dispatch compileRaoEntity(Frame frame, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += frame.asClass(jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase);
	}

	def dispatch compileRaoEntity(ResourceDeclaration resource, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += resource.asInitializationMethod(jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase)
		members += resource.asGetter(jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase)
	}

	def dispatch compileRaoEntity(ResultType result, JvmDeclaredType it, boolean isPreIndexingPhase) {

		members += result.toClass(QualifiedName.create(qualifiedName, result.name)) [
			static = true
			superTypes += typeRef(ru.bmstu.rk9.rao.lib.result.Result, {result.evaluateType})

			members += result.toConstructor [
				visibility = JvmVisibility.PRIVATE
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
				visibility = JvmVisibility.PRIVATE
				for (param : result.parameters)
					parameters += param.toParameter(param.name, param.parameterType)
				parameters += result.toParameter("resultMode", typeRef(ResultMode))
				body = '''
					this(«FOR param : parameters»«param.name», «ENDFOR»getDefaultStatistics());
				'''
			]
			
			members += result.toConstructor [
				visibility = JvmVisibility.PRIVATE
				for (param : result.parameters)
					parameters += param.toParameter(param.name, param.parameterType)
				parameters += result.toParameter("statistics", typeRef(Statistics, {result.evaluateType}))
				body = '''
					this(«FOR param : result.parameters»«param.name», «ENDFOR»ResultMode.AUTO, statistics);
				'''
				
			]
			
			members += result.toConstructor [
				visibility = JvmVisibility.PRIVATE
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
						return new ru.bmstu.rk9.rao.lib.result.StorelessNumericStatistics<«evaluateType»>();
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
						case "evaluate": {
							defaultMethodReturnType = result.evaluateType;
						}
						case "condition": {
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

	def dispatch compileRaoEntity(Result result, JvmDeclaredType it, boolean isPreIndexingPhase) {
		if (!isPreIndexingPhase && result.constructor != null)
			members += result.toField(result.name, result.constructor.inferredType) [
				visibility = JvmVisibility.PUBLIC
				static = true
				final = true
				initializer = result.constructor
			]
	}

	def compileResourceInitialization(RaoModel element, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += element.asGlobalInitializationMethod(jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase)
	}

}
