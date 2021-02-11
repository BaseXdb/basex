package org.basex.query.func;

import org.basex.query.expr.*;
import org.basex.query.util.list.*;

/**
 * Wrapper that provides types for function expressions.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class TypedFunc {
  /** Function expression. */
  final Expr func;
  /** Annotations. */
  final AnnList anns;

  /**
   * Constructor.
   * @param func function expression
   */
  TypedFunc(final Expr func) {
    this(func, null);
  }

  /**
   * Constructor.
   * @param func function expression
   * @param anns annotations (can be {@code null})
   */
  TypedFunc(final Expr func, final AnnList anns) {
    this.func = func;
    this.anns = anns != null ? anns : new AnnList();
  }
}
