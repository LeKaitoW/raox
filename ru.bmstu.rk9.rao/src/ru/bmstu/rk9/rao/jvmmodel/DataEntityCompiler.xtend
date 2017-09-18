package ru.bmstu.rk9.rao.jvmmodel

import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.naming.QualifiedName
import java.util.Collection
import org.eclipse.xtext.common.types.JvmPrimitiveType
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import java.nio.ByteBuffer
import ru.bmstu.rk9.rao.rao.FieldDeclaration
import ru.bmstu.rk9.rao.lib.database.Database.DataType
import ru.bmstu.rk9.rao.rao.DataEntity
import org.eclipse.xtext.common.types.JvmAnnotationReference
import org.eclipse.xtext.common.types.impl.TypesFactoryImpl
import org.eclipse.xtext.common.types.JvmAnnotationType
import javax.persistence.Entity
import javax.persistence.Table
import javax.persistence.Column

class DataEntityCompiler extends RaoEntityCompiler {
	def static asClass(DataEntity dataEntity, JvmTypesBuilder jvmTypesBuilder,
		JvmTypeReferenceBuilder typeReferenceBuilder, JvmDeclaredType it, boolean isPreIndexingPhase) {

		initializeCurrent(jvmTypesBuilder, typeReferenceBuilder)

		val typeQualifiedName = QualifiedName.create(qualifiedName, dataEntity.name)

		return dataEntity.toClass(typeQualifiedName) [
			static = true
			annotations += entityAnnotation
			annotations += dataEntity.tableAnnotation

			superTypes += typeRef(ru.bmstu.rk9.rao.lib.resource.ComparableResource, {
				typeRef
			})

			members += dataEntity.toConstructor [
				visibility = JvmVisibility.PRIVATE
				for (param : dataEntity.parameters)
					parameters += param.toParameter(param.declaration.name, param.declaration.parameterType)
				body = '''
					«FOR param : parameters»
						this.«param.name» = «param.name»;
					«ENDFOR»
				'''
			]

			if (!dataEntity.parameters.empty)
				members += dataEntity.toConstructor [
					visibility = JvmVisibility.PRIVATE
				]

			members += dataEntity.toMethod("create", typeRef) [
				visibility = JvmVisibility.PUBLIC
				static = true
				for (param : dataEntity.parameters)
					parameters += param.toParameter(param.declaration.name, param.declaration.parameterType)
				body = '''
					«dataEntity.name» resource = new «dataEntity.name»(«createEnumerationString(parameters, [name])»);
					return resource;
				'''
			]

			members += dataEntity.toMethod("erase", typeRef(void)) [
				visibility = JvmVisibility.PUBLIC
				final = true
				annotations += ru.bmstu.rk9.rao.jvmmodel.RaoEntityCompiler.overrideAnnotation()
				body = '''
				'''
			]

			for (param : dataEntity.parameters) {
				members += param.toField(param.declaration.name, param.declaration.parameterType) [
					initializer = param.^default
				]
				members += param.toMethod("get" + param.declaration.name.toFirstUpper, param.declaration.parameterType) [
					body = '''
						return «param.declaration.name»;
					'''
				]
				members += param.toMethod("set" + param.declaration.name.toFirstUpper, typeRef(void)) [
					parameters += param.toParameter(param.declaration.name, param.declaration.parameterType)
					body = '''
						«dataEntity.name» actual = this;
						
						if (isShallowCopy)
							actual = ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.getModelState().copyOnWrite(this);
						
						actual.«param.declaration.name» = «param.declaration.name»;
					'''
				]
			}

			members += dataEntity.toMethod("checkEqual", typeRef(boolean)) [ m |
				m.visibility = JvmVisibility.PUBLIC
				m.parameters += dataEntity.toParameter("other", typeRef)
				m.annotations += ru.bmstu.rk9.rao.jvmmodel.RaoEntityCompiler.overrideAnnotation()
				m.body = '''
					«IF dataEntity.parameters.isEmpty»
						return true;
					«ELSE»
						return «String.join(" && ", dataEntity.parameters.map[ p |
						'''«IF p.declaration.parameterType.type instanceof JvmPrimitiveType
								»this.«p.declaration.name» == other.«p.declaration.name»«
							ELSE
								»this.«p.declaration.name».equals(other.«p.declaration.name»)«
							ENDIF»
						'''
					])»;
					«ENDIF»
				'''
			]

			members += dataEntity.toMethod("deepCopy", typeRef) [
				visibility = JvmVisibility.PUBLIC
				annotations += ru.bmstu.rk9.rao.jvmmodel.RaoEntityCompiler.overrideAnnotation
				body = '''
					«dataEntity.name» copy = new «dataEntity.name»();
					copy.setNumber(this.number);
					copy.setName(this.name);
					«FOR param : dataEntity.parameters»
						copy.«param.declaration.name» = this.«param.declaration.name»;
					«ENDFOR»
					
					return copy;
				'''
			]

			members += dataEntity.toMethod("getTypeName", typeRef(String)) [
				visibility = JvmVisibility.PUBLIC
				final = true
				annotations += ru.bmstu.rk9.rao.jvmmodel.RaoEntityCompiler.overrideAnnotation()
				body = '''
					return "«typeQualifiedName»";
				'''
			]

			members += dataEntity.toMethod("serialize", typeRef(ByteBuffer)) [
				visibility = JvmVisibility.PUBLIC
				final = true
				annotations += ru.bmstu.rk9.rao.jvmmodel.RaoEntityCompiler.overrideAnnotation()

				var size = 0
				for (param : dataEntity.parameters) {
					size = size + param.getSize()
				}
				val fixedWidthParametersSize = size
				val variableWidthParameters = dataEntity.parameters.filter[!isFixedWidth]
				val fixedWidthParameters = dataEntity.parameters.filter[isFixedWidth]

				body = '''
					int _totalSize = «fixedWidthParametersSize»;
					java.util.List<Integer> _positions = new java.util.ArrayList<>();
					
					int _currentPosition = «fixedWidthParametersSize + variableWidthParameters.size * DataType.INT.size»;
					«FOR param : variableWidthParameters»
						_positions.add(_currentPosition);
						String «param.declaration.name»Value = String.valueOf(«param.declaration.name»);
						byte[] _«param.declaration.name»Bytes = «param.declaration.name»Value.getBytes(java.nio.charset.StandardCharsets.UTF_8);
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

			members += dataEntity.toMethod("getAccessible", typeRef(Collection, {
				typeRef
			})) [
				visibility = JvmVisibility.PUBLIC
				final = true
				static = true
				body = '''
					return ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.getModelState().getAccessible(«dataEntity.name».class);
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
		val type = DataType.getByName(param.declaration.parameterType.simpleName)
		switch type {
			case INT:
				return '''putInt(«param.declaration.name»)'''
			case DOUBLE:
				return '''putDouble(«param.declaration.name»)'''
			case BOOLEAN:
				return '''put(«param.declaration.name» ? (byte)1 : (byte)0)'''
			default:
				return '''/* INTERNAL ERROR: attempting to serialize type «param.declaration.parameterType.simpleName» as fixed width type */'''
		}
	}

	def private static JvmAnnotationReference entityAnnotation() {
		val anno = TypesFactoryImpl.eINSTANCE.createJvmAnnotationReference
		val annoType = typeRef(Entity).type as JvmAnnotationType
		anno.setAnnotation(annoType)
		return anno
	}

	def private static JvmAnnotationReference tableAnnotation(DataEntity dataEntity) {
		val anno = TypesFactoryImpl.eINSTANCE.createJvmAnnotationReference
		val annoType = typeRef(Table).type as JvmAnnotationType
		// annoType.value = dateEntity.name
		anno.setAnnotation(annoType)
		return anno
	}

	def private static JvmAnnotationReference columnAnnotation(FieldDeclaration param) {
		val anno = TypesFactoryImpl.eINSTANCE.createJvmAnnotationReference
		val annoType = typeRef(Column).type as JvmAnnotationType
		anno.setAnnotation(annoType)
		return anno
	}

}
