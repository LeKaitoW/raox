package ru.bmstu.rk9.rao.lib.runtime;

import org.eclipse.xtext.xbase.lib.Inline;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Objects;

/* NB: This class is mostly a copy of org.eclipse.xtext.xbase.lib.ObjectExtensions class, with the only
 * difference: operator_equals() method has been changed, as original one works buggy for rao code.
 * This class should be updated for every new version of xtext rao project adopts.
 * */
@GwtCompatible
public class RaoObjectExtensions {
	@Pure
	@Inline(value = "($1 == null ? true : !$1.equals($2))")
	public static boolean operator_notEquals(Object a, Object b) {
		return a == null ? true : !a.equals(b);
	}

	@Pure
	@Inline(value = "($1 == null ? false : $1.equals($2))")
	public static boolean operator_equals(Object a, Object b) {
		return a == null ? false : a.equals(b);
	}

	@Pure
	@Inline(value = "($1 == $2)", constantExpression = true)
	public static boolean identityEquals(Object a, Object b) {
		return a == b;
	}

	@Pure
	@Inline(value = "($1 == $2)", constantExpression = true)
	public static boolean operator_tripleEquals(Object a, Object b) {
		return a == b;
	}

	@Pure
	@Inline(value = "($1 != $2)", constantExpression = true)
	public static boolean operator_tripleNotEquals(Object a, Object b) {
		return a != b;
	}

	@Pure
	@Inline(value = "$3.$4of($1, $2)", imported = Pair.class)
	public static <A, B> Pair<A, B> operator_mappedTo(A a, B b) {
		return Pair.of(a, b);
	}

	public static <T> T operator_doubleArrow(T object, Procedure1<? super T> block) {
		block.apply(object);
		return object;
	}

	@Pure /*
			 * not guaranteed pure , since toString() is invoked on the argument
			 * a
			 */
	@Inline("($1 + $2)")
	public static String operator_plus(Object a, String b) {
		return a + b;
	}

	@Pure
	public static <T> T operator_elvis(T first, T second) {
		if (first != null)
			return first;
		return second;
	}
}
