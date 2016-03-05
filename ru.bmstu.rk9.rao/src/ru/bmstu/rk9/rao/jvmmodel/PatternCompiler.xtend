package ru.bmstu.rk9.rao.jvmmodel

import java.util.function.Supplier
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import ru.bmstu.rk9.rao.lib.pattern.Operation
import ru.bmstu.rk9.rao.lib.pattern.Rule
import ru.bmstu.rk9.rao.rao.Pattern
import ru.bmstu.rk9.rao.rao.PatternType

import static extension ru.bmstu.rk9.rao.jvmmodel.TupleInfoFactory.*

class PatternCompiler extends RaoEntityCompiler {
	def static asClass(Pattern pattern, JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder typeReferenceBuilder,
		JvmDeclaredType it, boolean isPreIndexingPhase) {
		initializeCurrent(jvmTypesBuilder, typeReferenceBuilder)

		return pattern.toClass(QualifiedName.create(qualifiedName, pattern.name)) [
			static = true
			superTypes += if (pattern.type == PatternType.RULE)
				typeRef(Rule)
			else
				typeRef(Operation)

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
								java.util.Set<«tupleInfo.genericTupleInfo.genericName»<«FOR p : tuple.names»«p.toUpperCase
								»«IF tuple.names.indexOf(p) != tuple.names.size - 1», «ENDIF»«ENDFOR»>> combinations = new java.util.HashSet<>();
								«FOR p : tuple.names»for(«p.toUpperCase» __«p» : «parameters.get(tuple.names.indexOf(p)).name») {
									«IF tuple.names.indexOf(p) == tuple.names.size - 1»combinations.add(new «tupleInfo.genericTupleInfo.genericName»(«FOR t : tuple.names»__«t
										»«IF tuple.names.indexOf(t) != tuple.names.size - 1», «ENDIF»«ENDFOR»));«ENDIF»
									«ENDFOR»«FOR p : tuple.names»}
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

					for (name : tuple.names) {
						// TODO derive actual resrouce type
						members += tuple.toField(name, typeRef(ru.bmstu.rk9.rao.lib.resource.Resource))
					}
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
							this.«relevant.name» = __resolve«relevant.name.toFirstUpper»();
							if (this.«relevant.name» == null)
								return false;
						«ENDFOR»
						«FOR tuple : pattern.relevantTuples»«
							val tupleInfo = tupleInfoMap.get(tuple)
							»«tupleInfo.genericTupleInfo.genericName» __«tupleInfo.name» = «tupleInfo.resolveMethodName»();
								if (__«tupleInfo.name» == null)
									return false;
								else {
									«FOR name : tuple.names»
										this.«name» = __«tupleInfo.name».«tupleInfo.tupleElementsInfo.get(tuple.names.indexOf(name)).name»;
									«ENDFOR»
								}
						«ENDFOR»
						return true;
					'''
				]
			}
		]
	}

	// FIXME ugly
	def static private getPatternMethodTypeRef(String name) {
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
