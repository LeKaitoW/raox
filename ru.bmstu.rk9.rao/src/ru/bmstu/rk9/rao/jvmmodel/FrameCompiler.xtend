package ru.bmstu.rk9.rao.jvmmodel

import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder;

import ru.bmstu.rk9.rao.rao.Frame;
import org.eclipse.xtext.naming.QualifiedName
import ru.bmstu.rk9.rao.lib.animation.BackgroundData
import ru.bmstu.rk9.rao.lib.animation.AnimationContext

class FrameCompiler extends RaoEntityCompiler {
	def static asClass(Frame frame, JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder typeReferenceBuilder,
		JvmDeclaredType it, boolean isPreIndexingPhase) {

		initializeCurrent(jvmTypesBuilder, typeReferenceBuilder)

		val frameQualifiedName = QualifiedName.create(qualifiedName, frame.name)

		return frame.toClass(frameQualifiedName) [
			static = true
			superTypes += typeRef(ru.bmstu.rk9.rao.lib.animation.AnimationFrame)

			// TODO make configurable
			members += frame.toMethod("getBackgroundData", typeRef(BackgroundData)) [
				final = true
				annotations += overrideAnnotation
				body = '''
					return new ru.bmstu.rk9.rao.lib.animation.BackgroundData(500, 500, ru.bmstu.rk9.rao.lib.animation.RaoColor.WHITE);
				'''
			]

			members += frame.toMethod("getTypeName", typeRef(String)) [
				final = true
				annotations += overrideAnnotation
				body = '''
					return "«frameQualifiedName»";
				'''
			]

			members += frame.toMethod("draw", typeRef(void)) [
				final = true
				annotations += overrideAnnotation
				parameters += frame.toParameter("it", typeRef(AnimationContext))
				body = frame.body
			]
		]
	}
}
