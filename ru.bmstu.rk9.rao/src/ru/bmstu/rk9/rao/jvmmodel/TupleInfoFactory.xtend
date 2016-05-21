package ru.bmstu.rk9.rao.jvmmodel

import ru.bmstu.rk9.rao.rao.RelevantResourceTuple
import org.eclipse.xtext.common.types.impl.TypesFactoryImpl
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder
import ru.bmstu.rk9.rao.jvmmodel.TupleInfoManager.TupleInfo
import ru.bmstu.rk9.rao.jvmmodel.TupleInfoManager.GenericTupleInfo
import ru.bmstu.rk9.rao.jvmmodel.TupleInfoManager.TupleElementInfo
import ru.bmstu.rk9.rao.jvmmodel.TupleInfoManager.GenericTupleElementInfo
import java.util.List
import java.util.Map
import java.util.HashMap

class TupleInfoFactory extends RaoEntityCompiler {
	def static Map<RelevantResourceTuple, TupleInfo> createTuplesInfo(List<RelevantResourceTuple> tuples,
		JvmTypesBuilder jvmTypesBuilder, JvmTypeReferenceBuilder jvmTypeReferenceBuilder) {
		initializeCurrent(jvmTypesBuilder, jvmTypeReferenceBuilder);

		val tupleInfoMap = new HashMap<RelevantResourceTuple, TupleInfo>()
		val tupleInfoManager = new TupleInfoManager

		for (tuple : tuples) {
			val tupleSize = tuple.names.size
			var tupleName = "Tuple"
			for (name : tuple.names) {
				tupleName = tupleName + name.toFirstUpper
			}

			var GenericTupleInfo genericTupleInfo = new GenericTupleInfo(tupleSize)
			var boolean isUnique

			if (!tupleInfoManager.uniqueGenericTupleSizes.contains(tupleSize)) {
				genericTupleInfo = new GenericTupleInfo(tupleSize)
				tupleInfoManager.uniqueGenericTupleSizes.add(tupleSize)
				isUnique = true
			} else {
				isUnique = false
			}

			var tupleInfo = new TupleInfo(tupleName, genericTupleInfo, isUnique)

			for (name : tuple.names) {
				val index = tuple.names.indexOf(name)
				val paramName = "_r" + index
				val typeParameter = createTypeParameter(name)
				val staticTypeParameter = createTypeParameter(name)
				tupleInfo.genericTupleInfo.typeReferencesArray.set(index, typeRef(typeParameter))
				tupleInfo.genericTupleInfo.staticTypeReferencesArray.set(index, typeRef(staticTypeParameter))
				tupleInfo.tupleElementsInfo +=
					new TupleElementInfo(paramName, new GenericTupleElementInfo(typeParameter, staticTypeParameter))
			}

			tupleInfoMap.put(tuple, tupleInfo)
		}

		return tupleInfoMap
	}

	def static createTypeParameter(String name) {
		val typeParameter = TypesFactoryImpl.eINSTANCE.createJvmTypeParameter
		val constraint = TypesFactoryImpl.eINSTANCE.createJvmUpperBound
		constraint.typeReference = typeRef(ru.bmstu.rk9.rao.lib.resource.ComparableResource, {
			typeRef(typeParameter)
		})
		typeParameter.name = name.toFirstUpper
		typeParameter.constraints += constraint

		return typeParameter
	}
}
