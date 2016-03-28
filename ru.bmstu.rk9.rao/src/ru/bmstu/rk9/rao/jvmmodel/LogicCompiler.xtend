package ru.bmstu.rk9.rao.jvmmodel

import ru.bmstu.rk9.rao.rao.Logic
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.common.types.JvmVisibility

class LogicCompiler extends RaoEntityCompiler {
	def static asClass(Logic logic, JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder typeReferenceBuilder,
		JvmDeclaredType it, boolean isPreIndexingPhase) {
		initializeCurrent(jvmTypesBuilder, typeReferenceBuilder);

		val logicQualifiedName = QualifiedName.create(qualifiedName, logic.name)

		return logic.toClass(logicQualifiedName) [
			static = true
			superTypes += typeRef(ru.bmstu.rk9.rao.lib.dpt.Logic)

			for (activity : logic.activities) {
				members += activity.toField(activity.name, typeRef(ru.bmstu.rk9.rao.lib.dpt.Activity)) [
					visibility = JvmVisibility.PRIVATE
				]

				members += activity.toMethod("initialize" + activity.name.toFirstUpper, typeRef(ru.bmstu.rk9.rao.lib.dpt.Activity)) [
					visibility = JvmVisibility.PRIVATE
					final = true
					body = activity.constructor
				]
			}

			members += logic.toMethod("initializeActivities", typeRef(void)) [
				visibility = JvmVisibility.PROTECTED
				final = true
				annotations += generateOverrideAnnotation()
				body = '''
					«FOR activity : logic.activities»
						this.«activity.name» = initialize«activity.name.toFirstUpper»();
						this.«activity.name».setName("«activity.name»");
						addActivity(this.«activity.name»);
					«ENDFOR»
				'''
			]

			members += logic.toMethod("getTypeName", typeRef(String)) [
				visibility = JvmVisibility.PUBLIC
				final = true
				annotations += generateOverrideAnnotation()
				body = '''
					return "«logicQualifiedName»";
				'''
			]

			for (method : logic.defaultMethods) {
				members += method.toMethod(method.name, typeRef(void)) [
					visibility = JvmVisibility.PUBLIC
					final = true
					annotations += generateOverrideAnnotation()
					body = method.body
				]
			}
		]
	}
}
