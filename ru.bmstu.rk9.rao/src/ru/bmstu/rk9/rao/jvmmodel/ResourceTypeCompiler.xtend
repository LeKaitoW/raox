package ru.bmstu.rk9.rao.jvmmodel

import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmVisibility
import ru.bmstu.rk9.rao.rao.ResourceType
import org.eclipse.xtext.naming.QualifiedName
import java.util.Collection
import org.eclipse.xtext.common.types.JvmPrimitiveType
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import java.nio.ByteBuffer

class ResourceTypeCompiler extends RaoEntityCompiler {
	def static asClass(ResourceType resourceType, JvmTypesBuilder jvmTypesBuilder,
		JvmTypeReferenceBuilder typeReferenceBuilder, JvmDeclaredType it, boolean isPreIndexingPhase) {
		initializeCurrent(jvmTypesBuilder, typeReferenceBuilder)

		val typeQualifiedName = QualifiedName.create(qualifiedName, resourceType.name)

		return resourceType.toClass(typeQualifiedName) [
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

			members += resourceType.toMethod("serialize", typeRef(ByteBuffer)) [
				visibility = JvmVisibility.PUBLIC
				final = true
				annotations += generateOverrideAnnotation()

				var size = 0
				// TODO cleaner approach
				for (param : resourceType.parameters) {
					switch param.declaration.parameterType.simpleName {
						case "int",
						case "Integer":
							size = size + 4
						case "double",
						case "Double":
							size = size + 8
						case "boolean",
						case "Boolean":
							size = size + 1
					}
				}
				val totalSize = size

				// FIXME stub
				body = '''
					ByteBuffer buffer = ByteBuffer.allocate(«totalSize»);
					return buffer;
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

			members += resourceType.toMethod("getAccessible", typeRef(Collection, {
				typeRef
			})) [
				visibility = JvmVisibility.PUBLIC
				final = true
				static = true
				body = '''
					return ru.bmstu.rk9.rao.lib.simulator.Simulator.getModelState().getAccessible(«resourceType.name».class);
				'''
			]
		]
	}
}
