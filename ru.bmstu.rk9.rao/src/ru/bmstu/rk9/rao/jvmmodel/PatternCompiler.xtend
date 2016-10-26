package ru.bmstu.rk9.rao.jvmmodel

import java.util.function.Supplier
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import ru.bmstu.rk9.rao.rao.Pattern
import ru.bmstu.rk9.rao.rao.PatternType

import static extension ru.bmstu.rk9.rao.jvmmodel.TupleInfoFactory.*
import ru.bmstu.rk9.rao.validation.DefaultMethodsHelper

class PatternCompiler extends RaoEntityCompiler {
	def static asClass(Pattern pattern, JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder typeReferenceBuilder,
		JvmDeclaredType it, boolean isPreIndexingPhase) {
		initializeCurrent(jvmTypesBuilder, typeReferenceBuilder)

		val patternQualifiedName = QualifiedName.create(qualifiedName, pattern.name)

		return pattern.toClass(patternQualifiedName) [
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
					return () -> new «pattern.name»(«createEnumerationString(parameters, [name])»);
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

			val tupleInfoMap = pattern.relevantTuples.createTuplesInfo(currentJvmTypesBuilder,
				currentJvmTypeReferenceBuilder)

			for (tuple : pattern.relevantTuples) {
				val tupleInfo = tupleInfoMap.get(tuple)
				if (tupleInfo.isUnique) {
					val tupleType = tuple.toClass(
						QualifiedName.create(qualifiedName, tupleInfo.genericTupleInfo.genericName)) [
						visibility = JvmVisibility.PUBLIC
						static = true
						for (tupleElementInfo : tupleInfo.tupleElementsInfo)
							typeParameters += tupleElementInfo.genericInfo.jvmTypeParameter

						for (tupleElementInfo : tupleInfo.tupleElementsInfo) {
							members +=
								tuple.toField(tupleElementInfo.name,
									typeRef(tupleElementInfo.genericInfo.jvmTypeParameter)) [
									visibility = JvmVisibility.PUBLIC
								]
						}

						members += tuple.toConstructor [
							visibility = JvmVisibility.PRIVATE
							for (tupleElementInfo : tupleInfo.tupleElementsInfo)
								parameters +=
									tuple.toParameter(tupleElementInfo.name,
										typeRef(tupleElementInfo.genericInfo.jvmTypeParameter))
							body = '''
								«FOR tupleElementInfo : tupleInfo.tupleElementsInfo»this.«tupleElementInfo.name» = «tupleElementInfo.name»;
								«ENDFOR»
							'''
						]
					]

					members += tupleType
					members +=
						tuple.toMethod("combination",
							typeRef(Iterable, tupleType.typeRef(tupleInfo.genericTupleInfo.staticTypeReferencesArray))) [
							visibility = JvmVisibility.PUBLIC
							static = true
							for (tupleElementInfo : tupleInfo.tupleElementsInfo) {
								typeParameters += tupleElementInfo.genericInfo.staticJvmTypeParameter
								parameters +=
									tuple.toParameter("__" + tupleElementInfo.name + "_arr", typeRef(Iterable, {
										typeRef(tupleElementInfo.genericInfo.staticJvmTypeParameter)
									}))
							}

							body = '''
								java.util.Set<«tupleInfo.genericTupleInfo.genericName»<«createEnumerationString(tuple.names, [createTupleGenericTypeName])»>> combinations = new java.util.HashSet<>();
								«FOR name : tuple.names»for(«createTupleGenericTypeName(name)» __«name» : «parameters.get(tuple.names.indexOf(name)).name») {
									«IF tuple.names.indexOf(name) == tuple.names.size - 1»combinations.add(new «tupleInfo.genericTupleInfo.genericName»(
										«createEnumerationString(tuple.names, [n | "__" + n])»));«ENDIF»
									«ENDFOR»«FOR name : tuple.names»}
								«ENDFOR»
								return combinations;
							'''
						]
				}
			}

			if (!isPreIndexingPhase) {
				for (relevant : pattern.relevantResources) {
					members += relevant.toMethod("__resolve" + relevant.name.toFirstUpper, relevant.value.inferredType) [
						visibility = JvmVisibility.PRIVATE
						body = relevant.value
					]

					members += relevant.toField(relevant.name, relevant.value.inferredType)
				}

				for (tuple : pattern.relevantTuples) {
					val tupleInfo = tupleInfoMap.get(tuple)

					val resolveMethod = tuple.toMethod(tupleInfo.resolveMethodName, tuple.value.inferredType) [
						visibility = JvmVisibility.PRIVATE
						body = tuple.value
					]
					members += resolveMethod

					for (name: tuple.names)
						members += tuple.toField(name, tuple.types.get(tuple.names.indexOf(name)))
				}

				for (method : pattern.defaultMethods) {
					members += method.toMethod(method.name, method.name.getPatternMethodTypeRef) [
						visibility = JvmVisibility.PUBLIC
						final = true
						annotations += ru.bmstu.rk9.rao.jvmmodel.RaoEntityCompiler.overrideAnnotation()
						body = method.body
					]
				}

				members += pattern.toMethod("selectRelevantResources", typeRef(boolean)) [
					visibility = JvmVisibility.PUBLIC
					final = true
					annotations += ru.bmstu.rk9.rao.jvmmodel.RaoEntityCompiler.overrideAnnotation()

					body = '''
						«FOR relevant : pattern.relevantResources»
							this.«relevant.name» = __resolve«relevant.name.toFirstUpper»();
							if (this.«relevant.name» == null) {
								finish();
								return false;
							}
							this.«relevant.name».take();
							this.relevantResourcesNumbers.add(this.«relevant.name».getNumber());
						«ENDFOR»
						«FOR tuple : pattern.relevantTuples»«
							val tupleInfo = tupleInfoMap.get(tuple)
							»«tupleInfo.genericTupleInfo.genericName»<«createEnumerationString(tuple.names, [toFirstUpper])
							»> __«tupleInfo.name» = «tupleInfo.resolveMethodName»();
							if (__«tupleInfo.name» == null) {
								finish();
								return false;
							} else {
								«FOR name : tuple.names»
									this.«name» = __«tupleInfo.name».«tupleInfo.tupleElementsInfo.get(tuple.names.indexOf(name)).name»;
									this.«name».take();
									this.relevantResourcesNumbers.add(this.«name».getNumber());
								«ENDFOR»
							}
						«ENDFOR»
						return true;
					'''
				]

				members += pattern.toMethod("finish", typeRef(void)) [
					visibility = JvmVisibility.PUBLIC
					final = true
					annotations += ru.bmstu.rk9.rao.jvmmodel.RaoEntityCompiler.overrideAnnotation()

					body = '''
						«FOR relevant : pattern.relevantResources»
							if (this.«relevant.name» != null)
								this.«relevant.name».put();
						«ENDFOR»
						«FOR tuple : pattern.relevantTuples»
							«FOR name : tuple.names»
								if (this.«name» != null)
									this.«name».put();
							«ENDFOR»
						«ENDFOR»
					'''
				]

				members += pattern.toMethod("getTypeName", typeRef(String)) [
					visibility = JvmVisibility.PUBLIC
					final = true
					annotations += ru.bmstu.rk9.rao.jvmmodel.RaoEntityCompiler.overrideAnnotation()

					body = '''return "«patternQualifiedName»";'''
				]
			}
		]
	}

	def static private getPatternMethodTypeRef(String name) {
		switch name {
			case DefaultMethodsHelper.OperationMethodInfo.BEGIN.name,
			case DefaultMethodsHelper.OperationMethodInfo.END.name,
			case DefaultMethodsHelper.RuleMethodInfo.EXECUTE.name:
				return typeRef(void)
			case DefaultMethodsHelper.OperationMethodInfo.DURATION.name:
				return typeRef(double)
		}

		return null
	}
}
