package ru.bmstu.rk9.rao.jvmmodel

import com.google.inject.Inject
import org.eclipse.xtext.xbase.jvmmodel.AbstractModelInferrer
import org.eclipse.xtext.xbase.jvmmodel.IJvmDeclaredTypeAcceptor
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import ru.bmstu.rk9.rao.rao.RaoModel
import org.eclipse.xtext.naming.QualifiedName
import ru.bmstu.rk9.rao.rao.Constant
import ru.bmstu.rk9.rao.rao.FunctionDeclaration
import org.eclipse.xtext.common.types.JvmVisibility

class RaoJvmModelInferrer extends AbstractModelInferrer {
	@Inject extension JvmTypesBuilder

	def dispatch void infer(RaoModel element, IJvmDeclaredTypeAcceptor acceptor, boolean isPreIndexingPhase) {
		acceptor.accept(element.toClass(QualifiedName.create("model"))) [
			members += element.toMethod("run", typeRef(void)) [
				final = true
				static = true
				body = '''System.out.println("so, we meet again");'''
			]

			for (entity : element.objects) {
				switch (entity) {
					Constant:
						members += entity.compileConstantAsField
					FunctionDeclaration:
						members += entity.compileFunctionAsMethod
				}
			}
		]
	}

	def compileConstantAsField(Constant constant) {
		return constant.toField(constant.constant.name, constant.constant.parameterType) [
			visibility = JvmVisibility.PUBLIC
			static = true
			final = true
			initializer = constant.value
		]
	}

	def compileFunctionAsMethod(FunctionDeclaration function) {
		return function.toMethod(function.name, function.type) [
			for (param : function.params)
				parameters += function.toParameter(param.name, param.parameterType)
			visibility = JvmVisibility.PUBLIC
			static = true
			final = true
			varArgs = false
			body = function.body
		]
	}
}

