package org.basex.query.func.java;

import static org.basex.query.QueryError.*;

import java.lang.reflect.*;
import java.util.*;

import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.Array;

/**
 * Dynamic invocation of a Java constructor, field or method.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
abstract class DynJavaCall extends JavaCall {
  /** Java class. */
  final Class<?> clazz;
  /** Types provided in the query (can be {@code null}). */
  final String[] types;

  /**
   * Constructor.
   * @param clazz Java class
   * @param types types provided in the parsed expression string (can be {@code null})
   * @param args arguments
   * @param sc static context
   * @param info input info
   */
  DynJavaCall(final Class<?> clazz, final String[] types, final Expr[] args, final StaticContext sc,
      final InputInfo info) {
    super(args, Perm.ADMIN, false, sc, info);
    this.clazz = clazz;
    this.types = types;
  }

  /**
   * Evaluates the first argument.
   * @param args arguments
   * @param stat static flag
   * @return Java object (can be {@code null})
   * @throws QueryException query exception
   */
  final Object instance(final Value[] args, final boolean stat) throws QueryException {
    if(stat) return null;
    final Object object = args[0].toJava();
    if(object == null) throw instanceExpected(null);
    return object;
  }

  /**
   * Returns an error for the specified execution candidates.
   * @param candidates candidates
   * @param execs executables (constructors, methods)
   * @return exception
   */
  final QueryException noCandidate(final ArrayList<JavaCandidate> candidates,
      final Executable[] execs) {

    final int cl = candidates.size();
    if(cl > 1) {
      final ArrayList<Executable> list = new ArrayList<>(cl);
      for(final JavaCandidate jc : candidates) list.add(jc.executable);
      return JAVAMULTIPLE_X_X.get(info, name(), paramTypes(list.toArray(Executable[]::new), false));
    }

    final Executable single = execs.length == 1 ? execs[0] : null;
    final int el = exprs.length;
    final ExprList list = new ExprList(el);
    int e = single != null && !isStatic(single) || el > 0 &&
        (exprs[0] instanceof XQJava || exprs[0] instanceof JavaCall) ? 0 : -1;
    while(++e < el) list.add(exprs[e]);

    final String args = argTypes(list.finish());
    return single != null ? JAVAARGS_X_X_X.get(info, name(), paramTypes(single, true), args) :
      JAVANONE_X_X_X.get(info, name(), args, paramTypes(execs, true));
  }

  /**
   * Returns an error for field/method invocations in which first argument is no class instance.
   * @param ex exception (can be {@code null})
   * @return exception
   */
  final QueryException instanceExpected(final Exception ex) {
    if(ex != null) Util.debug(ex);
    return JAVANOINSTANCE_X_X.get(info, JavaCall.className(clazz), JavaCall.argType(exprs[0]));
  }

  @Override
  public final boolean has(final Flag... flags) {
    return Flag.NDT.in(flags) || super.has(flags);
  }

  /**
   * {@inheritDoc}
   * Must be overwritten by implementing class.
   */
  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof DynJavaCall)) return false;
    final DynJavaCall j = (DynJavaCall) obj;
    return clazz.equals(j.clazz) && Array.equals(types, j.types) && super.equals(obj);
  }
}
