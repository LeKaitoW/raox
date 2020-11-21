package ru.bmstu.rk9.rao.jvmmodel

import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import ru.bmstu.rk9.rao.rao.VarConst
import java.util.ArrayList
import org.eclipse.xtext.common.types.impl.JvmFormalParameterImplCustom
import java.util.Arrays

class VarConstCompiler extends RaoEntityCompiler {
	def static asClass(VarConst varconst, JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder typeReferenceBuilder,
		JvmDeclaredType it, boolean isPreIndexingPhase) {
		initializeCurrent(jvmTypesBuilder, typeReferenceBuilder);
		
		val vcQualifiedName = QualifiedName.create(qualifiedName, varconst.name)
		
		return varconst.toClass(vcQualifiedName) [
			static = true
			superTypes += typeRef(ru.bmstu.rk9.rao.lib.varconst.VarConst)

			members += varconst.toConstructor [
				visibility = JvmVisibility.PRIVATE
				parameters += varconst.start.toParameter("start", typeRef(Double))
				parameters += varconst.stop.toParameter("stop", typeRef(Double))
				parameters += varconst.step.toParameter("step", typeRef(Double))
				if (varconst.lambda !== null)
					parameters += varconst.lambda.toParameter("lambda", typeRef(ru.bmstu.rk9.rao.lib.lambdaexpression.LambdaExpression))
				body = '''
					«FOR param : parameters»this.«param.name» = «param.name»;
					«ENDFOR»
				'''
			]
			
			members += varconst.start.toField("start", typeRef(Double))
			members += varconst.stop.toField("stop", typeRef(Double))
			members += varconst.step.toField("step", typeRef(Double))
			if (varconst.lambda !== null)
				members += varconst.lambda.toField("lambda", typeRef(ru.bmstu.rk9.rao.lib.lambdaexpression.LambdaExpression))
			
			members += varconst.toMethod("getName", typeRef(String)) [
				visibility = JvmVisibility.PUBLIC
				final = true
				body = '''
					return "«vcQualifiedName»";
				'''
			]
			
			members += varconst.toMethod("checkValue", typeRef(Boolean)) [
				visibility = JvmVisibility.PUBLIC
				if (varconst.lambda !== null)
				{
					for (param : varconst.lambda.parameters) {
						var tmp = new JvmFormalParameterImplCustom()
						tmp.name = param.name
						tmp.parameterType = typeRef(Double)
						parameters += tmp
					}
					body = varconst.lambda.body
				}
				else {
					body = '''
						return true;
					'''
				}
			]
			
			members += varconst.toMethod("getAllDependencies", typeRef(ArrayList, typeRef(String))) [
				visibility = JvmVisibility.PUBLIC
				if (varconst.lambda !== null)
				{
//					autist feature
//					idk how to import Arrays in normal way, so I add Arrays variable in case 
//					xtend do it automatically
					body = '''
	«««				«»
						return new ArrayList<String>(«Arrays».asList(new String[] {«FOR o : varconst.lambda.parameters»"«o.name»"«IF varconst.lambda.parameters.indexOf(o) != varconst.lambda.parameters.size - 1», «ENDIF»«ENDFOR»}));
					'''
				}
				else {
					body = '''
						return null;
					'''
				}
			]
		]
	}
}