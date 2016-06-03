package ru.bmstu.rk9.rao.jvmmodel

import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import org.eclipse.xtext.common.types.JvmAnnotationReference
import org.eclipse.xtext.common.types.impl.TypesFactoryImpl
import org.eclipse.xtext.common.types.JvmAnnotationType

abstract class RaoEntityCompiler {
	protected static extension JvmTypesBuilder currentJvmTypesBuilder;
	protected static extension JvmTypeReferenceBuilder currentJvmTypeReferenceBuilder;

	def protected static initializeCurrent(JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder jvmTypeReferenceBuilder) {
		currentJvmTypesBuilder = jvmTypesBuilder;
		currentJvmTypeReferenceBuilder = jvmTypeReferenceBuilder;
	}

	def protected static JvmAnnotationReference overrideAnnotation() {
		val anno = TypesFactoryImpl.eINSTANCE.createJvmAnnotationReference
		val annoType = typeRef(Override).type as JvmAnnotationType
		anno.setAnnotation(annoType)
		return anno
	}
}