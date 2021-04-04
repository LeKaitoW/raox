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
import ru.bmstu.rk9.rao.rao.FieldDeclaration
import ru.bmstu.rk9.rao.lib.database.Database.DataType
import org.eclipse.xtext.common.types.impl.JvmFormalParameterImplCustom

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
			
			val tmp = new JvmFormalParameterImplCustom()
			
			members += resourceType.toConstructor [
				visibility = JvmVisibility.PRIVATE
				for (param : resourceType.parameters)
					parameters += param.toParameter(param.declaration.name, param.declaration.parameterType)
				parameters += tmp.toParameter("simulatorId", typeRef(Double))
				body = '''
					«FOR param : parameters»
						this._«param.name» = «param.name»;
					«ENDFOR»
				'''
			]

			if (!resourceType.parameters.empty)
				members += resourceType.toConstructor [
					visibility = JvmVisibility.PRIVATE
				]

			members += resourceType.toMethod("create", typeRef) [
				visibility = JvmVisibility.PUBLIC
				static = true
				for (param : resourceType.parameters)
					parameters += param.toParameter(param.declaration.name, param.declaration.parameterType)
				body = '''
					«resourceType.name» resource = new «resourceType.name»(«createEnumerationString(parameters, [name])»);
					ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.getModelState().addResource(resource);
					ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.getDatabase().memorizeResourceEntry(resource,
							ru.bmstu.rk9.rao.lib.database.Database.ResourceEntryType.CREATED);
					return resource;
				'''
			]

			members += resourceType.toMethod("erase", typeRef(void)) [
				visibility = JvmVisibility.PUBLIC
				final = true
				annotations += ru.bmstu.rk9.rao.jvmmodel.RaoEntityCompiler.overrideAnnotation()
				body = '''
					ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.getModelState().eraseResource(this);
					ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.getDatabase().memorizeResourceEntry(this,
							ru.bmstu.rk9.rao.lib.database.Database.ResourceEntryType.ERASED);
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
						«resourceType.name» actual = this;

						if (isShallowCopy)
							actual = ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.getModelState().copyOnWrite(this);

						actual._«param.declaration.name» = «param.declaration.name»;
						ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.getDatabase().memorizeResourceEntry(actual,
								ru.bmstu.rk9.rao.lib.database.Database.ResourceEntryType.ALTERED);
					'''
				]
			}

			members += resourceType.toMethod("checkEqual", typeRef(boolean)) [ m |
				m.visibility = JvmVisibility.PUBLIC
				m.parameters += resourceType.toParameter("other", typeRef)
				m.annotations += ru.bmstu.rk9.rao.jvmmodel.RaoEntityCompiler.overrideAnnotation()
				m.body = '''
					«IF resourceType.parameters.isEmpty»
						return true;
					«ELSE»
						return «String.join(" && ", resourceType.parameters.map[ p |
						'''«IF p.declaration.parameterType.type instanceof JvmPrimitiveType
								»this._«p.declaration.name» == other._«p.declaration.name»«
							ELSE
								»this._«p.declaration.name».equals(other._«p.declaration.name»)«
							ENDIF»
						'''
					])»;
					«ENDIF»
				'''
			]

			members += resourceType.toMethod("deepCopy", typeRef) [
				visibility = JvmVisibility.PUBLIC
				annotations += ru.bmstu.rk9.rao.jvmmodel.RaoEntityCompiler.overrideAnnotation
				body = '''
					«resourceType.name» copy = new «resourceType.name»();
					copy.setNumber(this.number);
					copy.setName(this.name);
					«FOR param : resourceType.parameters»
						copy._«param.declaration.name» = this._«param.declaration.name»;
					«ENDFOR»

					return copy;
				'''
			]

			members += resourceType.toMethod("getTypeName", typeRef(String)) [
				visibility = JvmVisibility.PUBLIC
				final = true
				annotations += ru.bmstu.rk9.rao.jvmmodel.RaoEntityCompiler.overrideAnnotation()
				body = '''
					return "«typeQualifiedName»";
				'''
			]

			members += resourceType.toMethod("serialize", typeRef(ByteBuffer)) [
				visibility = JvmVisibility.PUBLIC
				final = true
				annotations += ru.bmstu.rk9.rao.jvmmodel.RaoEntityCompiler.overrideAnnotation()

				var size = 0
				for (param : resourceType.parameters) {
					size = size + param.getSize()
				}
				val fixedWidthParametersSize = size
				val variableWidthParameters = resourceType.parameters.filter[!isFixedWidth]
				val fixedWidthParameters = resourceType.parameters.filter[isFixedWidth]

				body = '''
					int _totalSize = «fixedWidthParametersSize»;
					java.util.List<Integer> _positions = new java.util.ArrayList<>();

					int _currentPosition = «fixedWidthParametersSize + variableWidthParameters.size * DataType.INT.size»;
					«FOR param : variableWidthParameters»
						_positions.add(_currentPosition);
						String _«param.declaration.name»Value = String.valueOf(_«param.declaration.name»);
						byte[] _«param.declaration.name»Bytes = _«param.declaration.name»Value.getBytes(java.nio.charset.StandardCharsets.UTF_8);
						int _«param.declaration.name»Length = _«param.declaration.name»Bytes.length;
						_currentPosition += _«param.declaration.name»Length + «DataType.INT.size»;
						_totalSize += _«param.declaration.name»Length + «2 * DataType.INT.size»;
					«ENDFOR»

					ByteBuffer buffer = ByteBuffer.allocate(_totalSize);

					«FOR param : fixedWidthParameters»
						buffer.«param.serializeAsFixedWidth»;
					«ENDFOR»

					java.util.Iterator<Integer> _it = _positions.iterator();
					«FOR param : variableWidthParameters»
						buffer.putInt(_it.next());
					«ENDFOR»

					«FOR param : variableWidthParameters»
						buffer.putInt(_«param.declaration.name»Length);
						buffer.put(_«param.declaration.name»Bytes);
					«ENDFOR»

					return buffer;
				'''
			]

			members += resourceType.toMethod("getAny", typeRef) [
				visibility = JvmVisibility.PUBLIC
				final = true
				static = true
				body = '''
					return ru.bmstu.rk9.rao.lib.runtime.RaoCollectionExtensions.any(getAll());
				'''
			]

			members += resourceType.toMethod("getAll", typeRef(Collection, {
				typeRef
			})) [
				visibility = JvmVisibility.PUBLIC
				final = true
				static = true
				body = '''
					return ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.getModelState().getAll(«resourceType.name».class);
				'''
			]

			members += resourceType.toMethod("getAccessible", typeRef(Collection, {
				typeRef
			})) [
				visibility = JvmVisibility.
					PUBLIC
				final = true
				static = true
				body = '''
					return ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.getModelState().getAccessible(«resourceType.name».class);
				'''
			]
		]
	}

	def private static getSize(FieldDeclaration param) {
		return DataType.getByName(param.declaration.parameterType.simpleName).size
	}

	def private static isFixedWidth(FieldDeclaration param) {
		return param.getSize != 0
	}

	def private static serializeAsFixedWidth(FieldDeclaration param) {
		val type = DataType.getByName(param.declaration.parameterType.
			simpleName)
		switch type {
			case INT:
				return '''putInt(_«param.declaration.name»)'''
			case DOUBLE:
				return '''putDouble(_«param.declaration.name»)'''
			case BOOLEAN:
				return '''put(_«param.declaration.name» ? (byte)1 : (byte)0)'''
			default:
				return '''/* INTERNAL ERROR: attempting to serialize type «param.declaration.parameterType.simpleName» as fixed width type */'''
		}
	}
}
