package ru.bmstu.rk9.rao.jvmmodel

import java.nio.ByteBuffer
import java.util.Collection
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmPrimitiveType
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import ru.bmstu.rk9.rao.lib.database.Database.DataType
import ru.bmstu.rk9.rao.lib.json.JSONObject
import ru.bmstu.rk9.rao.lib.resource.ComparableResource
import ru.bmstu.rk9.rao.rao.FieldDeclaration
import ru.bmstu.rk9.rao.rao.ResourceType

class ResourceTypeCompiler extends RaoEntityCompiler {
	def static asClass(ResourceType resourceType, JvmTypesBuilder jvmTypesBuilder,
		JvmTypeReferenceBuilder typeReferenceBuilder, JvmDeclaredType it, boolean isPreIndexingPhase) {

		initializeCurrent(jvmTypesBuilder, typeReferenceBuilder)

		val typeQualifiedName = QualifiedName.create(qualifiedName, resourceType.name)

		return resourceType.toClass(typeQualifiedName) [
			static = true

			superTypes += typeRef(ComparableResource, {
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
					«resourceType.name» resource = new «resourceType.name»(«FOR param : parameters»«
						param.name»«
						IF parameters.indexOf(param) != parameters.size - 1», «ENDIF»«ENDFOR»);
					ru.bmstu.rk9.rao.lib.simulator.Simulator.getModelState().addResource(resource);
					ru.bmstu.rk9.rao.lib.simulator.Simulator.getDatabase().memorizeResourceEntry(resource,
							ru.bmstu.rk9.rao.lib.database.Database.ResourceEntryType.CREATED);
					return resource;
				'''
			]

			members += resourceType.toMethod("erase", typeRef(void)) [
				visibility = JvmVisibility.PUBLIC
				final = true
				annotations += RaoEntityCompiler.overrideAnnotation()
				body = '''
					ru.bmstu.rk9.rao.lib.simulator.Simulator.getModelState().eraseResource(this);
					ru.bmstu.rk9.rao.lib.simulator.Simulator.getDatabase().memorizeResourceEntry(this,
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
						this._«param.declaration.name» = «param.declaration.name»;
						ru.bmstu.rk9.rao.lib.simulator.Simulator.getDatabase().memorizeResourceEntry(this,
								ru.bmstu.rk9.rao.lib.database.Database.ResourceEntryType.ALTERED);
					'''
				]
			}

			members += resourceType.toMethod("checkEqual", typeRef(boolean)) [ m |
				m.visibility = JvmVisibility.PUBLIC
				m.parameters += resourceType.toParameter("other", typeRef)
				m.annotations += RaoEntityCompiler.overrideAnnotation()
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
				annotations += RaoEntityCompiler.overrideAnnotation
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
				annotations += RaoEntityCompiler.overrideAnnotation()
				body = '''
					return "«typeQualifiedName»";
				'''
			]

			members += resourceType.toMethod("getResParamsInJSON ", typeRef(JSONObject)) [

				visibility = JvmVisibility.PUBLIC
				final = true
				annotations += RaoEntityCompiler.overrideAnnotation()
				body = '''
					JSONArray jsonArray = new JSONArray();
					JSONObject jsonParametersObject = new JSONObject();
					JSONObject jsonObject = new JSONObject();

						«FOR param : resourceType.parameters»
							jsonObject.put("name", "«param.declaration.name»").put("value","_«param.declaration.name»");
							jsonArray.put(jsonObject);
						«ENDFOR»
					jsonParametersObject.put("parameters",jsonArray); 
					return jsonParametersObject;
				'''
			]

			members += resourceType.toMethod("serialize", typeRef(ByteBuffer)) [
				visibility = JvmVisibility.PUBLIC
				final = true
				annotations += RaoEntityCompiler.overrideAnnotation()

				var size = 0
				for (param : resourceType.parameters) {
					size = size + param.getSize()
				}
				val fixedWidthParametersSize = size
				val variableWidthParameters = resourceType.parameters.filter[p|!p.isFixedWidth]

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

					«FOR param : resourceType.parameters.filter[p | p.isFixedWidth]»
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

	// TODO cleaner approach?
	def private static getSize(FieldDeclaration param) {
		switch param.declaration.parameterType.simpleName {
			case "int",
			case "Integer":
				return 4
			case "double",
			case "Double":
				return 8
			case "boolean",
			case "Boolean":
				return 1
		}

		return 0
	}

	def private static isFixedWidth(FieldDeclaration param) {
		return param.getSize != 0
	}

	// TODO cleaner approach?
	def private static serializeAsFixedWidth(FieldDeclaration param) {
		switch param.declaration.parameterType.simpleName {
			case "int",
			case "Integer":
				return '''putInt(_«param.declaration.name»)'''
			case "double",
			case "Double":
				return '''putDouble(_«param.declaration.name»)'''
			case "boolean",
			case "Boolean":
				return '''put(_«param.declaration.name» ? (byte)1 : (byte)0);'''
		}
	}
}
