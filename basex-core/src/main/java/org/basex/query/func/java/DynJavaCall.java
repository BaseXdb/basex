package org.basex.query.func.java;

import static org.basex.query.QueryError.*;

import java.lang.reflect.*;

import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
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
   * Returns an error for the specified execution candidates.
   * @param execs executables (constructors, methods)
   * @param candidates candidates
   * @return exception
   */
  final QueryException candidates(final Executable[] execs, final Executable[] candidates) {
    final int cl = candidates.length;
    if(cl > 1) return JAVAMULTIPLE_X_X.get(info, name(), paramTypes(candidates, false));

    final Executable single = execs.length == 1 ? execs[0] : null;
    final int el = exprs.length;
    final ExprList tmp = new ExprList(el);
    int e = single != null && !isStatic(single) || el > 0 &&
        (exprs[0] instanceof XQJava || exprs[0] instanceof JavaCall) ? 0 : -1;
    while(++e < el) tmp.add(exprs[e]);

    if(single != null) return JAVAARGS_X_X_X.get(info, name(), paramTypes(single, true),
        argTypes(tmp.finish()));

    return JAVANONE_X_X_X.get(info, name(), argTypes(tmp.finish()), paramTypes(execs, true));
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
