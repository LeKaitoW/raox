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

import ru.bmstu.rk9.rao.lib.simulator.EmbeddedSimulation

class RaoJvmModelInferrer extends AbstractModelInferrer {
	@Inject extension JvmTypesBuilder

	def dispatch void infer(RaoModel element, IJvmDeclaredTypeAcceptor acceptor, boolean isPreIndexingPhase) {
		acceptor.accept(element.toClass(QualifiedName.create("model"))) [
			superTypes += typeRef(EmbeddedSimulation)

			// TODO add override annotation to that method
			members += element.toMethod("run", typeRef(int)) [
				visibility = JvmVisibility.PROTECTED
				final = true
				body = '''
				System.out.println("so, we meet again");
				return 0;'''
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

