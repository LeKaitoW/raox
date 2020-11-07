package ru.bmstu.rk9.rao.jvmmodel

import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.common.types.impl.JvmFormalParameterImpl
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import ru.bmstu.rk9.rao.rao.LambdaExpression
import ru.bmstu.rk9.rao.rao.VarConst
import org.eclipse.xtext.common.types.impl.JvmFormalParameterImplCustom
import java.util.ArrayList
import org.eclipse.xtext.common.types.JvmFormalParameter

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
				parameters += varconst.lambda.toParameter("lambda", typeRef(LambdaExpression))
				body = '''
					«FOR param : parameters»this.«param.name» = «param.name»;
					«ENDFOR»
				'''
			]
			
			members += varconst.start.toField("start", typeRef(Double))
			members += varconst.stop.toField("stop", typeRef(Double))
			members += varconst.step.toField("step", typeRef(Double))
			members += varconst.lambda.toField("lambda", typeRef(LambdaExpression))
			
			members += varconst.toMethod("getName", typeRef(String)) [
				visibility = JvmVisibility.PUBLIC
				final = true
				body = '''
					return "«vcQualifiedName»";
				'''
			]
			
			members += varconst.toMethod("checkValue", typeRef(Boolean)) [
				visibility = JvmVisibility.PUBLIC
				var value = new JvmFormalParameterImplCustom()
				value.name = "yaBiba"
				value.parameterType = typeRef(Double)
				parameters += value
				
				for (param : varconst.lambda.parameters) {
					var tmp = new JvmFormalParameterImplCustom()
					tmp.name = param.name
					tmp.parameterType = typeRef(Double)
					parameters += tmp
				}
				body = varconst.lambda.body
			]
			
			members += varconst.toMethod("getAllDependencies", typeRef(ArrayList, typeRef(String))) [
				visibility = JvmVisibility.PUBLIC
				body = '''
«««				«»
					return new ArrayList<String>(Arrays.asList(new String[] {«FOR o : varconst.lambda.parameters»"«o.name»"«IF varconst.lambda.parameters.indexOf(o) != varconst.lambda.parameters.size - 1», «ENDIF»«ENDFOR»}));
				'''
			]
			
		]
	}
}