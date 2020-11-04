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
				val lambdaExpression = varconst.lambda;
				for (param : varconst.lambda.parameters)
					parameters += param.toParameter(param.name, typeRef(VarConst))
				body = '''
					«FOR param : parameters»this.«param.name» = «param.name»;
					«ENDFOR»
				'''
			]
		]
	}
}