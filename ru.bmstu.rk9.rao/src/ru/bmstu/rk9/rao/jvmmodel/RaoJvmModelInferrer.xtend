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
import ru.bmstu.rk9.rao.rao.Event
import ru.bmstu.rk9.rao.rao.DefaultMethod
import ru.bmstu.rk9.rao.lib.simulator.TerminateCondition
import ru.bmstu.rk9.rao.lib.simulator.Simulator.SimulationStopCode

// TODO add override annotation to all generated overriden methods
class RaoJvmModelInferrer extends AbstractModelInferrer {
	@Inject extension JvmTypesBuilder

	def dispatch void infer(RaoModel element, IJvmDeclaredTypeAcceptor acceptor, boolean isPreIndexingPhase) {
		acceptor.accept(element.toClass(QualifiedName.create("model"))) [
			superTypes += typeRef(EmbeddedSimulation)

			members += element.compileInstanceField

			members += element.toMethod("startSimulation", typeRef(SimulationStopCode)) [
				visibility = JvmVisibility.PUBLIC
				static = true
				final = true
				body = '''
					return INSTANCE.run();
				'''
			]

			for (entity : element.objects) {
				switch (entity) {
					Constant:
						members += entity.compileConstantAsField
					FunctionDeclaration:
						members += entity.compileFunctionAsMethod
					DefaultMethod:
						switch (entity.name) {
							case "init": {
								members += entity.compileInitMethod
							}
							case "terminateCondition": {
								members += entity.compileTerminateConditionClass
								members += entity.compileTerminateConditionGetter
							}
						}
				}
			}
		]

		for (entity : element.objects) {
			switch (entity) {
				Event:
					entity.compileEventAsClass(acceptor)
			}
		}
	}

	def compileEventAsClass(Event event, IJvmDeclaredTypeAcceptor acceptor) {
		acceptor.accept(event.toClass(QualifiedName.create(event.name))) [
			superTypes += typeRef(ru.bmstu.rk9.rao.lib.event.Event)

			members += event.toConstructor [
				visibility = JvmVisibility.PUBLIC
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
				body = '''
					return "«event.name»";
				'''
			]

			members += event.toMethod("run", typeRef(void)) [
				visibility = JvmVisibility.PUBLIC
				final = true
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
					pushEvent(event);
				'''
			]
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
			for (param : function.parameters)
				parameters += function.toParameter(param.name, param.parameterType)
			visibility = JvmVisibility.PUBLIC
			static = true
			final = true
			body = function.body
		]
	}

	def compileInitMethod(DefaultMethod method) {
		return method.toMethod(method.name, typeRef(void)) [
			visibility = JvmVisibility.PROTECTED
			final = true
			body = method.body
		]
	}

	def compileTerminateConditionClass(DefaultMethod method) {
		return method.toClass("ModelTerminateCondition") [
			superTypes += typeRef(TerminateCondition)
			static = true

			members += method.toMethod("check", typeRef(boolean)) [
				visibility = JvmVisibility.PUBLIC
				final = true
				body = method.body
			]
		]
	}

	def compileTerminateConditionGetter(DefaultMethod method) {
		return method.toMethod("getTerminateCondition", typeRef(TerminateCondition)) [
			visibility = JvmVisibility.PROTECTED
			final = true
			body = '''return new ModelTerminateCondition();'''
		]
	}

	def compileInstanceField(RaoModel element) {
		return element.toField("INSTANCE", typeRef(EmbeddedSimulation)) [
			visibility = JvmVisibility.PRIVATE
			static = true
			final = true
			initializer = '''new model()'''
		]
	}
}

