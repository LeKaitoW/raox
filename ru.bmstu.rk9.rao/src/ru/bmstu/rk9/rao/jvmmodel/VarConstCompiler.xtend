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
import java.util.HashMap

class VarConstCompiler extends RaoEntityCompiler {
	
	def static asClass(VarConst varconst, JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder typeReferenceBuilder,
		JvmDeclaredType it, boolean isPreIndexingPhase) {
		initializeCurrent(jvmTypesBuilder, typeReferenceBuilder);
		
		val vcQualifiedName = QualifiedName.create(qualifiedName, varconst.name + "VarConst")
		
		return varconst.toClass(vcQualifiedName) [
			static = true
			superTypes += typeRef(ru.bmstu.rk9.rao.lib.varconst.VarConst)
			
			var tmp = new JvmFormalParameterImplCustom()
			for (name : Arrays.asList("start", "stop", "step"))
				members += tmp.toField(name, typeRef(Double))
				
			if (varconst.lambda !== null)
				members += varconst.lambda.toField("lambda", typeRef(ru.bmstu.rk9.rao.lib.lambdaexpression.LambdaExpression))

			members += varconst.toConstructor [
				visibility = JvmVisibility.PUBLIC
				
				body = '''
					start = «Double.valueOf(varconst.start)»;
					stop = «Double.valueOf(varconst.stop)»;
					step = «Double.valueOf(varconst.step)»;
				'''
			]
			
			members += varconst.toMethod("getName", typeRef(String)) [
				visibility = JvmVisibility.PUBLIC
				final = true
				
				body = '''
					return "«vcQualifiedName»";
				'''
			]
			
			members += varconst.toMethod("checkValue", typeRef(Boolean)) [
				visibility = JvmVisibility.PUBLIC
				
				var mapArg = new JvmFormalParameterImplCustom()
				mapArg.name = "args"
				mapArg.parameterType = typeRef(HashMap, typeRef(String), typeRef(Double))
				parameters += mapArg
				
				if (varconst.lambda !== null) {
					body = '''
						«FOR param : varconst.lambda.parameters»
							Double «param.name»;
						«ENDFOR»
						
						«FOR param : varconst.lambda.parameters»
							«param.name» = args.get("«param.name»");
						«ENDFOR»
						
						return this.checkLambda(«FOR o : varconst.lambda.parameters»«o.name»«IF varconst.lambda.parameters.indexOf(o) != varconst.lambda.parameters.size - 1», «ENDIF»«ENDFOR»);
					''' 
				}
				else
					body = '''
						return true;
					'''
			]
			
			if (varconst.lambda !== null) {
				members += varconst.toMethod("checkLambda", typeRef(Boolean)) [
					visibility = JvmVisibility.PRIVATE
					
					for (param : varconst.lambda.parameters) {
						var cur = new JvmFormalParameterImplCustom()
						cur.name = param.name
						cur.parameterType = typeRef(Double)
						parameters += cur
					}
					body = varconst.lambda.body
				]
			}
			
			members += varconst.toMethod("getAllDependencies", typeRef(ArrayList, typeRef(String))) [
				visibility = JvmVisibility.PUBLIC
				if (varconst.lambda !== null)
				{
					body = '''
						return new ArrayList<String>(«Arrays».asList(new String[] {«FOR o : varconst.lambda.parameters»"«o.name»"«IF varconst.lambda.parameters.indexOf(o) != varconst.lambda.parameters.size - 1», «ENDIF»«ENDFOR»}));
					'''
				}
				else {
					body = '''
						return new ArrayList<String>();
					'''
				}
			]
		]
	}
}