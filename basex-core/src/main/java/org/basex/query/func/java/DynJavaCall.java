package org.basex.query.func.java;

import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.util.*;

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
