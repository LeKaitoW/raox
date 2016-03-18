package ru.bmstu.rk9.rao.jvmmodel

import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import ru.bmstu.rk9.rao.rao.Event
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder

class EventCompiler extends RaoEntityCompiler {
	def static asClass(Event event, JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder typeReferenceBuilder,
		JvmDeclaredType it, boolean isPreIndexingPhase) {
		initializeCurrent(jvmTypesBuilder, typeReferenceBuilder);

		val eventQualifiedName = QualifiedName.create(qualifiedName, event.name)

		return event.toClass(eventQualifiedName) [
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
}
