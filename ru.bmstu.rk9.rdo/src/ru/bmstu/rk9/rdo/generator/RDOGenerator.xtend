package ru.bmstu.rk9.rdo.generator

import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.IGenerator
import org.eclipse.xtext.generator.IFileSystemAccess

import ru.bmstu.rk9.rdo.rdo.RDOModel

import ru.bmstu.rk9.rdo.rdo.ResourceType

import ru.bmstu.rk9.rdo.rdo.ResourceDeclaration

import ru.bmstu.rk9.rdo.rdo.RDOType

import ru.bmstu.rk9.rdo.rdo.RDOEnum
import ru.bmstu.rk9.rdo.rdo.RDOSuchAs
import ru.bmstu.rk9.rdo.rdo.RDOArray
import ru.bmstu.rk9.rdo.rdo.RDOOwnType
import ru.bmstu.rk9.rdo.customizations.RDOQualifiedNameProvider

class RDOGenerator implements IGenerator {
	
	override void doGenerate(Resource resource, IFileSystemAccess fsa) {
//		for (e : resource.allContents.toIterable.filter(typeof(ResourceType))) {
//			fsa.generateFile("resourceTypes/" + e.name + ".java", e.compile)
//		}
		
		val rssName = RDOQualifiedNameProvider.computeFromURI(resource.contents.head as RDOModel)

		if (rssName.length > 0)
			fsa.generateFile("resources/" + rssName + ".java", compileResources(resource.contents.head as RDOModel))
	}

//	def compile(ResourceType rtp) {
//		'''
//		package resourceTypes;
//		
//		public class «rtp.name» {
//		
//			private static final boolean isPermanent = «IF rtp.type.literal == 'permanent'»true«ELSE»false«ENDIF»;
//		
//			«FOR parameter : rtp.parameters»
//			private «processType(parameter.type)» «parameter.name»;
//			«ENDFOR»
//		}
//		'''
//	}
//	
//	def dispatch processType(RDOBasicType type) {
//		switch type.type {
//			case INTEGER : "int"
//			case REAL    : "double"
//			case BOOLEAN : "boolean"
//			case STRING  : "String"		
//		}
//	}
//
//	def dispatch processType(RDOEnum type) {
//		'''ENUM'''
//	}
//
//	def dispatch processType(RDOSuchAs type) {
//		'''SUCH_AS'''
//	}
//
//	def dispatch processType(RDOArray type) {
//		'''ARRAY'''
//	}
//	
//	def dispatch processType(RDOOwnType type) {
//		'''CUSTOM'''
//	}


	def compileResources(RDOModel model) {
		'''
		«FOR r : model.eAllContents.toIterable.filter(typeof(ResourceDeclaration))»
			«r.reference.name» «r.name»();
		«ENDFOR»
		'''
	}

}

