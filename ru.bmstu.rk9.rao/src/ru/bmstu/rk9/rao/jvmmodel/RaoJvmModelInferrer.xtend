package ru.bmstu.rk9.rao.jvmmodel

import com.google.inject.Inject
import org.eclipse.xtext.xbase.jvmmodel.AbstractModelInferrer
import org.eclipse.xtext.xbase.jvmmodel.IJvmDeclaredTypeAcceptor
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import ru.bmstu.rk9.rao.rao.RaoModel
import org.eclipse.xtext.naming.QualifiedName
import ru.bmstu.rk9.rao.rao.Constant
import ru.bmstu.rk9.rao.rao.FunctionDeclaration
import org.eclipse.xtext.common.types.JvmVisibility

import static extension ru.bmstu.rk9.rao.jvmmodel.RaoNaming.*

import ru.bmstu.rk9.rao.rao.Event
import ru.bmstu.rk9.rao.rao.DefaultMethod
import org.eclipse.xtext.common.types.JvmDeclaredType
import ru.bmstu.rk9.rao.rao.ResourceType
import org.eclipse.xtext.common.types.JvmPrimitiveType
import ru.bmstu.rk9.rao.rao.ResourceDeclaration
import org.eclipse.xtext.common.types.JvmAnnotationType
import org.eclipse.xtext.common.types.JvmAnnotationReference
import org.eclipse.xtext.common.types.impl.TypesFactoryImpl
import ru.bmstu.rk9.rao.rao.EnumDeclaration
import ru.bmstu.rk9.rao.rao.Sequence
import ru.bmstu.rk9.rao.rao.Generator
import java.util.function.Supplier
import ru.bmstu.rk9.rao.rao.Logic
import ru.bmstu.rk9.rao.rao.Pattern
import ru.bmstu.rk9.rao.rao.PatternType
import ru.bmstu.rk9.rao.rao.Search
import java.util.Collection

class RaoJvmModelInferrer extends AbstractModelInferrer {
	@Inject extension JvmTypesBuilder

	def JvmAnnotationReference generateOverrideAnnotation() {
		val anno = TypesFactoryImpl.eINSTANCE.createJvmAnnotationReference
		val annoType = typeRef(Override).type as JvmAnnotationType
		anno.setAnnotation(annoType)
		return anno
	}

	def dispatch void infer(RaoModel element, IJvmDeclaredTypeAcceptor acceptor, boolean isPreIndexingPhase) {
		acceptor.accept(element.toClass(QualifiedName.create(element.eResource.URI.projectName, element.nameGeneric))) [
			for (entity : element.objects) {
				entity.compileRaoEntity(it, isPreIndexingPhase)
			}
		]
	}

	def dispatch compileRaoEntity(Constant constant, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += constant.toField(constant.constant.name, constant.constant.parameterType) [
			visibility = JvmVisibility.PUBLIC
			static = true
			final = true
			initializer = constant.value
		]
	}

	def dispatch compileRaoEntity(Sequence sequence, JvmDeclaredType it, boolean isPreIndexingPhase) {
		if (!isPreIndexingPhase && sequence.constructor != null) {
			members += sequence.toField(sequence.name, sequence.constructor.inferredType) [
				visibility = JvmVisibility.PUBLIC
				static = true
				final = true
				initializer = sequence.constructor
			]
		}
	}

	def dispatch compileRaoEntity(ResourceDeclaration resource, JvmDeclaredType it, boolean isPreIndexingPhase) {
		if (!isPreIndexingPhase && resource.constructor != null)
			members += resource.toField(resource.name, resource.constructor.inferredType) [
				visibility = JvmVisibility.PUBLIC
				static = true
				final = true
				initializer = resource.constructor
			]
	}

	def dispatch compileRaoEntity(EnumDeclaration enumDeclaration, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += enumDeclaration.toEnumerationType(enumDeclaration.name) [
			visibility = JvmVisibility.PUBLIC
			static = true
			enumDeclaration.values.forEach [ value |
				members += enumDeclaration.toEnumerationLiteral(value)
			]
		]
	}

	def dispatch compileRaoEntity(FunctionDeclaration function, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += function.toMethod(function.name, function.type) [
			for (param : function.parameters)
				parameters += function.toParameter(param.name, param.parameterType)
			visibility = JvmVisibility.PUBLIC
			static = true
			final = true
			body = function.body
		]
	}

	def dispatch compileRaoEntity(DefaultMethod method, JvmDeclaredType it, boolean isPreIndexingPhase) {
		switch (method.name) {
			case "init": {
				members += method.toClass(method.name) [
					superTypes += typeRef(Runnable)
					visibility = JvmVisibility.PROTECTED
					static = true
					members += method.toMethod("run", typeRef(void)) [
						visibility = JvmVisibility.PUBLIC
						final = true
						body = method.body
					]
				]
			}
			case "terminateCondition": {
				members += method.toClass(method.name) [
					superTypes += typeRef(Supplier, {
						typeRef(Boolean)
					})
					visibility = JvmVisibility.PROTECTED
					static = true
					members += method.toMethod("get", typeRef(Boolean)) [
						visibility = JvmVisibility.PUBLIC
						final = true
						body = method.body
					]
				]
			}
		}
	}

	def dispatch compileRaoEntity(ResourceType resourceType, JvmDeclaredType it, boolean isPreIndexingPhase) {
		val typeQualifiedName = QualifiedName.create(qualifiedName, resourceType.name)

		members += resourceType.toClass(typeQualifiedName) [
			static = true

			superTypes += typeRef(ru.bmstu.rk9.rao.lib.resource.ComparableResource, {
				typeRef
			})

			members += resourceType.toConstructor [
				visibility = JvmVisibility.PRIVATE
				for (param : resourceType.parameters)
					parameters += param.toParameter(param.declaration.name, param.declaration.parameterType)
				body = '''
					«FOR param : parameters»
						this._«param.name» = «param.name»;
					«ENDFOR»
				'''
			]

			members += resourceType.toMethod("create", typeRef) [
				visibility = JvmVisibility.PUBLIC
				static = true
				for (param : resourceType.parameters)
					parameters += param.toParameter(param.declaration.name, param.declaration.parameterType)
				body = '''
					«resourceType.name» resource = new «resourceType.name»(«FOR param : parameters»«
						param.name»«
						IF parameters.indexOf(param) != parameters.size - 1», «ENDIF»«ENDFOR»);
					ru.bmstu.rk9.rao.lib.simulator.Simulator.getModelState().addResource(resource);
					return resource;
				'''
			]

			members += resourceType.toMethod("erase", typeRef(void)) [
				visibility = JvmVisibility.PUBLIC
				final = true
				annotations += generateOverrideAnnotation()
				body = '''
					ru.bmstu.rk9.rao.lib.simulator.Simulator.getModelState().eraseResource(this);
				'''
			]

			for (param : resourceType.parameters) {
				members += param.toField("_" + param.declaration.name, param.declaration.parameterType) [
					initializer = param.^default
				]
				members += param.toMethod("get" + param.declaration.name.toFirstUpper, param.declaration.parameterType) [
					body = '''
						return _«param.declaration.name»;
					'''
				]
				members += param.toMethod("set" + param.declaration.name.toFirstUpper, typeRef(void)) [
					parameters += param.toParameter(param.declaration.name, param.declaration.parameterType)
					body = '''
						this._«param.declaration.name» = «param.declaration.name»;
					'''
				]
			}

			members += resourceType.toMethod("checkEqual", typeRef(boolean)) [ m |
				m.visibility = JvmVisibility.PUBLIC
				m.parameters += resourceType.toParameter("other", typeRef)
				m.annotations += generateOverrideAnnotation()
				m.body = '''
					return «String.join(" && ", resourceType.parameters.map[ p |
						'''«IF p.declaration.parameterType.type instanceof JvmPrimitiveType
								»this._«p.declaration.name» == other._«p.declaration.name»«
							ELSE
								»this._«p.declaration.name».equals(other._«p.declaration.name»)«
							ENDIF»
						'''
					])»;
				'''
			]

			members += resourceType.toMethod("getTypeName", typeRef(String)) [
				visibility = JvmVisibility.PUBLIC
				final = true
				annotations += generateOverrideAnnotation()
				body = '''
					return "«typeQualifiedName»";
				'''
			]

			members += resourceType.toMethod("getAll", typeRef(Collection, {
				typeRef
			})) [
				visibility = JvmVisibility.PUBLIC
				final = true
				static = true
				body = '''
					return ru.bmstu.rk9.rao.lib.simulator.Simulator.getModelState().getAll(«resourceType.name».class);
				'''
			]
		]
	}

	def dispatch compileRaoEntity(Generator generator, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += generator.toClass(QualifiedName.create(qualifiedName, generator.name)) [
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

	def dispatch compileRaoEntity(Event event, JvmDeclaredType it, boolean isPreIndexingPhase) {
		val eventQualifiedName = QualifiedName.create(qualifiedName, event.name)

		members += event.toClass(eventQualifiedName) [
			static = true
			superTypes += typeRef(ru.bmstu.rk9.rao.lib.event.Event)

			members += event.toConstructor [
				visibility = JvmVisibility.PRIVATE
				parameters += event.toParameter("time", typeRef(double))
				for (param : event.parameters)
					parameters += param.toParameter(param.name, param.parameterType)
				body = '''
					«FOR param : parameters»this.«param.name» = «param.name»;
					«ENDFOR»
				'''
			]

			for (param : event.parameters)
				members += param.toField(param.name, param.parameterType)

			members += event.toMethod("getName", typeRef(String)) [
				visibility = JvmVisibility.PUBLIC
				final = true
				annotations += generateOverrideAnnotation()
				body = '''
					return "«eventQualifiedName»";
				'''
			]

			members += event.toMethod("execute", typeRef(void)) [
				visibility = JvmVisibility.PROTECTED
				final = true
				annotations += generateOverrideAnnotation()
				body = event.body
			]

			members += event.toMethod("plan", typeRef(void)) [
				visibility = JvmVisibility.PUBLIC
				static = true
				final = true
				parameters += event.toParameter("time", typeRef(double))
				for (param : event.parameters)
					parameters += event.toParameter(param.name, param.parameterType)

				body = '''
					«event.name» event = new «event.name»(«FOR param : parameters»«
							param.name»«
							IF parameters.indexOf(param) != parameters.size - 1», «ENDIF»«ENDFOR»);
					ru.bmstu.rk9.rao.lib.simulator.Simulator.pushEvent(event);
				'''
			]
		]
	}

	def dispatch compileRaoEntity(Logic logic, JvmDeclaredType it, boolean isPreIndexingPhase) {
		if (!isPreIndexingPhase)
			members += logic.toField(logic.name, typeRef(ru.bmstu.rk9.rao.lib.dpt.Logic)) [
				visibility = JvmVisibility.PUBLIC
				static = true
				final = true
				initializer = logic.constructor
			]
	}

	def dispatch compileRaoEntity(Search search, JvmDeclaredType it, boolean isPreIndexingPhase) {
		if (!isPreIndexingPhase)
			members += search.toField(search.name, typeRef(ru.bmstu.rk9.rao.lib.dpt.Search)) [
				visibility = JvmVisibility.PUBLIC
				static = true
				final = true
				initializer = search.constructor
			]
	}

	def dispatch compileRaoEntity(Pattern pattern, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += pattern.toClass(QualifiedName.create(qualifiedName, pattern.name)) [
			static = true
			superTypes += if (pattern.type == PatternType.RULE)
				typeRef(ru.bmstu.rk9.rao.lib.pattern.Rule)
			else
				typeRef(ru.bmstu.rk9.rao.lib.pattern.Operation)

			members += pattern.toMethod("create", typeRef(Supplier, {
				typeRef
			})) [
				visibility = JvmVisibility.PUBLIC
				static = true
				for (param : pattern.parameters)
					parameters += param.toParameter(param.name, param.parameterType)
				body = '''
					return () -> new «pattern.name»(«FOR param : parameters»«
							param.name»«
							IF parameters.indexOf(param) != parameters.size - 1», «ENDIF»«ENDFOR»);
				'''
			]

			members += pattern.toConstructor [
				visibility = JvmVisibility.PRIVATE
				for (param : pattern.parameters)
					parameters += param.toParameter(param.name, param.parameterType)
				body = '''
					«FOR param : parameters»this.«param.name» = «param.name»;
					«ENDFOR»
				'''
			]

			for (param : pattern.parameters)
				members += param.toField(param.name, param.parameterType)

			if (!isPreIndexingPhase) {
				for (relevant : pattern.relevantResources) {
					members += relevant.toMethod("resolve" + relevant.name.toFirstUpper, relevant.value.inferredType) [
						visibility = JvmVisibility.PRIVATE
						body = relevant.value
					]

					members += relevant.toField(relevant.name, relevant.value.inferredType)
				}

				for (method : pattern.defaultMethods) {
					members += method.toMethod(method.name, method.name.getPatternMethodTypeRef) [
						visibility = JvmVisibility.PUBLIC
						final = true
						annotations += generateOverrideAnnotation()
						body = method.body
					]
				}

				members += pattern.toMethod("selectRelevantResources", typeRef(boolean)) [
					visibility = JvmVisibility.PUBLIC
					final = true
					annotations += generateOverrideAnnotation()
					body = '''
						«FOR relevant : pattern.relevantResources»
							this.«relevant.name» = resolve«relevant.name.toFirstUpper»();
							if (this.«relevant.name» == null)
								return false;
						«ENDFOR»
						return true;
					'''
				]
			}
		]
	}

	// FIXME ugly
	def getPatternMethodTypeRef(String name) {
		switch name {
			case "execute",
			case "begin",
			case "end":
				return typeRef(void)
			case "duration":
				return typeRef(double)
		}

		return null
	}
}
