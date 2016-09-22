package ru.bmstu.rk9.rao.jvmmodel

import ru.bmstu.rk9.rao.rao.Search
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.common.types.JvmVisibility

class SearchCompiler extends RaoEntityCompiler {
	def static asClass(Search search, JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder typeReferenceBuilder,
		JvmDeclaredType it, boolean isPreIndexingPhase) {
		initializeCurrent(jvmTypesBuilder, typeReferenceBuilder);

		val logicQualifiedName = QualifiedName.create(qualifiedName, search.name)

		return search.toClass(logicQualifiedName) [
			static = true
			superTypes += typeRef(ru.bmstu.rk9.rao.lib.dpt.Search)

			for (edge : search.edges) {
				members += edge.toField(edge.name, typeRef(ru.bmstu.rk9.rao.lib.dpt.Edge)) [
					visibility = JvmVisibility.PRIVATE
				]

				members += edge.toMethod("initialize" + edge.name.toFirstUpper, typeRef(ru.bmstu.rk9.rao.lib.dpt.Edge)) [
					visibility = JvmVisibility.PRIVATE
					final = true
					body = edge.constructor
				]
			}

			members += search.toMethod("initializeEdges", typeRef(void)) [
				visibility = JvmVisibility.PROTECTED
				final = true
				annotations += ru.bmstu.rk9.rao.jvmmodel.RaoEntityCompiler.overrideAnnotation()
				body = '''
					«FOR edge : search.edges»
						this.«edge.name» = initialize«edge.name.toFirstUpper»();
						this.«edge.name».setName("«edge.name»");
						addActivity(this.«edge.name»);
					«ENDFOR»
				'''
			]

			members += search.toMethod("getTypeName", typeRef(String)) [
				visibility = JvmVisibility.PUBLIC
				final = true
				annotations += ru.bmstu.rk9.rao.jvmmodel.RaoEntityCompiler.overrideAnnotation()
				body = '''
					return "«logicQualifiedName»";
				'''
			]

			for (method : search.defaultMethods) {
				members += method.toMethod(method.name, typeRef(void)) [
					visibility = JvmVisibility.PUBLIC
					final = true
					annotations += ru.bmstu.rk9.rao.jvmmodel.RaoEntityCompiler.overrideAnnotation()
					body = method.body
				]
			}
		]
	}
}