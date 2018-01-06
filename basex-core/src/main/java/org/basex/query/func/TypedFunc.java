package org.basex.query.func;

import org.basex.query.expr.*;
import org.basex.query.util.list.*;

/**
 * Wrapper that provides types for function expressions.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Leo Woerteler
 */
public final class TypedFunc {
  /** Function expression. */
  public final Expr func;
  /** Annotations. */
  public final AnnList anns;

  /**
   * Constructor.
   * @param func function expression
   * @param anns annotations
   */
  TypedFunc(final Expr func, final AnnList anns) {
    this.func = func;
    this.anns = anns;
  }

  /**
   * Creates a type constructor function.
   * @param cast cast expression
   * @return typed function
   */
  static TypedFunc constr(final Cast cast) {
    return new TypedFunc(cast, new AnnList());
  }

  /**
   * Creates a type constructor function.
   * @param f java function
   * @return typed function
   */
  static TypedFunc java(final JavaFunction f) {
    return new TypedFunc(f, new AnnList());
  }
}
