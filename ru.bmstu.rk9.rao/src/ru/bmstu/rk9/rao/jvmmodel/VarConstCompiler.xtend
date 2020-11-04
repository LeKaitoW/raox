package ru.bmstu.rk9.rao.jvmmodel

import ru.bmstu.rk9.rao.rao.VarConst
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.common.types.JvmVisibility

class VarConstCompiler extends RaoEntityCompiler {
	def static asClass(VarConst varconst, JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder typeReferenceBuilder,
		JvmDeclaredType it, boolean isPreIndexingPhase) {
		initializeCurrent(jvmTypesBuilder, typeReferenceBuilder);
		
		val vcQualifiedName = QualifiedName.create(qualifiedName, varconst.name)
		
		return varconst.toClass(vcQualifiedName) [
			static = true
//			superTypes += typeRef(ru.bmstu.rk9.rao.lib.event.Event)

			members += varconst.toConstructor [
				visibility = JvmVisibility.PRIVATE
				parameters += varconst.start.toParameter("start", typeRef(Double))
				parameters += varconst.stop.toParameter("stop", typeRef(Double))
				parameters += varconst.step.toParameter("step", typeRef(Double))
				for (param : varconst.lambda.parameters)
					parameters += param.toParameter(param.name, typeRef(VarConst))
				body = '''
					«FOR param : parameters»this.«param.name» = «param.name»;
					«ENDFOR»
				'''
			]

			for (param : varconst.lambda.parameters)
				members += param.toField(param.name, param.parameterType)

			members += varconst.toMethod("getName", typeRef(String)) [
				visibility = JvmVisibility.PUBLIC
				final = true
				annotations += ru.bmstu.rk9.rao.jvmmodel.RaoEntityCompiler.overrideAnnotation()
				body = '''
					return "«vcQualifiedName»";
				'''
			]

			members += event.toMethod("execute", typeRef(void)) [
				visibility = JvmVisibility.PROTECTED
				final = true
				annotations += ru.bmstu.rk9.rao.jvmmodel.RaoEntityCompiler.overrideAnnotation()
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
					«event.name» event = new «event.name»(«createEnumerationString(parameters, [name])»);
					ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.pushEvent(event);
				'''
			]
		]
	}
}