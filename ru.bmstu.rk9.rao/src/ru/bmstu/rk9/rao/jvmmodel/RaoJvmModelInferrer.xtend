package ru.bmstu.rk9.rao.jvmmodel

import com.google.inject.Inject
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.xbase.jvmmodel.AbstractModelInferrer
import org.eclipse.xtext.xbase.jvmmodel.IJvmDeclaredTypeAcceptor
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import ru.bmstu.rk9.rao.rao.DefaultMethod
import ru.bmstu.rk9.rao.rao.EnumDeclaration
import ru.bmstu.rk9.rao.rao.Event
import ru.bmstu.rk9.rao.rao.Frame
import ru.bmstu.rk9.rao.rao.FunctionDeclaration
import ru.bmstu.rk9.rao.rao.Generator
import ru.bmstu.rk9.rao.rao.Logic
import ru.bmstu.rk9.rao.rao.Pattern
import ru.bmstu.rk9.rao.rao.RaoModel
import ru.bmstu.rk9.rao.rao.ResourceDeclaration
import ru.bmstu.rk9.rao.rao.ResourceType
import ru.bmstu.rk9.rao.rao.Search
import ru.bmstu.rk9.rao.rao.Result
import ru.bmstu.rk9.rao.rao.DataSource

import static extension ru.bmstu.rk9.rao.jvmmodel.DefaultMethodCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.EntityCreationCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.EnumCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.EventCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.FrameCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.FunctionCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.GeneratorCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.LogicCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.PatternCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.ResourceDeclarationCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.ResourceTypeCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.SearchCompiler.*
import static extension ru.bmstu.rk9.rao.jvmmodel.ResultCompiler.*
import static extension ru.bmstu.rk9.rao.naming.RaoNaming.*
import ru.bmstu.rk9.rao.rao.EntityCreation

class RaoJvmModelInferrer extends AbstractModelInferrer {
	@Inject extension JvmTypesBuilder jvmTypesBuilder

	def dispatch void infer(RaoModel element, IJvmDeclaredTypeAcceptor acceptor, boolean isPreIndexingPhase) {
		acceptor.accept(element.toClass(QualifiedName.create(element.eResource.URI.projectName, element.nameGeneric))) [
			for (entity : element.objects) {
				entity.compileRaoEntity(it, isPreIndexingPhase)
			}

			element.compileResourceInitialization(it, isPreIndexingPhase)
		]
	}

	def dispatch compileRaoEntity(EntityCreation entity, JvmDeclaredType it, boolean isPreIndexingPhase) {
		if (!isPreIndexingPhase && entity.constructor != null)
			members += entity.asField(jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase)
	}

	def dispatch compileRaoEntity(EnumDeclaration enumDeclaration, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += enumDeclaration.asType(jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase)
	}

	def dispatch compileRaoEntity(FunctionDeclaration function, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += function.asMethod(jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase)
	}

	def dispatch compileRaoEntity(DefaultMethod method, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += method.asClass(jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase)
	}

	def dispatch compileRaoEntity(ResourceType resourceType, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += resourceType.asClass(jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase)
	}

	def dispatch compileRaoEntity(Generator generator, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += generator.asClass(jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase)
	}

	def dispatch compileRaoEntity(Event event, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += event.asClass(jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase)
	}

	def dispatch compileRaoEntity(Pattern pattern, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += pattern.asClass(jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase);
	}

	def dispatch compileRaoEntity(Logic logic, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += logic.asClass(jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase);
	}

	def dispatch compileRaoEntity(Search search, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += search.asClass(jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase);
	}

	def dispatch compileRaoEntity(Frame frame, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += frame.asClass(jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase);
	}

	def dispatch compileRaoEntity(ResourceDeclaration resource, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += resource.asGetter(jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase)
		members += resource.asField(jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase)
	}

	def dispatch compileRaoEntity(DataSource dataSource, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += dataSource.asClass(jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase);
	}

	def dispatch compileRaoEntity(Result result, JvmDeclaredType it, boolean isPreIndexingPhase) {
		if (!isPreIndexingPhase && result.constructor != null)
			members += result.asField(jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase);
	}

	def compileResourceInitialization(RaoModel element, JvmDeclaredType it, boolean isPreIndexingPhase) {
		members += element.asGlobalInitializationMethod(jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase)
		members += element.asGlobalInitializationState(jvmTypesBuilder, _typeReferenceBuilder, it, isPreIndexingPhase)
	}
}
