package ru.bmstu.rk9.rao.jvmmodel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.xtext.common.types.JvmTypeParameter;
import org.eclipse.xtext.common.types.JvmTypeReference;

public class TupleInfoManager {
	public static class TupleInfo {
		public TupleInfo(String name, GenericTupleInfo genericTupleInfo, boolean isUnique) {
			this.name = name;
			this.genericTupleInfo = genericTupleInfo;
			this.resolveMethodName = "__resolve" + name;
			this.isUnique = isUnique;
		}

		public final String resolveMethodName;
		public final List<TupleElementInfo> tupleElementsInfo = new ArrayList<>();
		public final GenericTupleInfo genericTupleInfo;
		public final String name;
		public final boolean isUnique;
	}

	public static class GenericTupleInfo {
		public GenericTupleInfo(int size) {
			this.typeReferencesArray = new JvmTypeReference[size];
			this.staticTypeReferencesArray = new JvmTypeReference[size];
			this.genericName = "Tuple" + size;
		}

		public final String genericName;
		public final JvmTypeReference[] typeReferencesArray;
		public final JvmTypeReference[] staticTypeReferencesArray;
	}

	public static class TupleElementInfo {
		public TupleElementInfo(String name, GenericTupleElementInfo genericInfo) {
			this.name = name;
			this.genericInfo = genericInfo;
		}

		public final String name;
		public final GenericTupleElementInfo genericInfo;
	}

	public static class GenericTupleElementInfo {
		public GenericTupleElementInfo(JvmTypeParameter jvmTypeParameter, JvmTypeParameter staticJvmTypeParameter) {
			this.jvmTypeParameter = jvmTypeParameter;
			this.staticJvmTypeParameter = staticJvmTypeParameter;
		}

		public final JvmTypeParameter jvmTypeParameter;
		public final JvmTypeParameter staticJvmTypeParameter;
	}

	public final Set<Integer> uniqueGenericTupleSizes = new HashSet<>();
}
