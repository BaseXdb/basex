package org.basex.query.func;

import org.basex.query.expr.*;
import org.basex.query.util.list.*;

/**
 * Wrapper that provides types for function expressions.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
public final class TypedFunc {
  /** Function expression. */
  public final Expr fun;
  /** Annotations. */
  public final AnnList anns;

  /**
   * Constructor.
   * @param fun function expression
   * @param anns annotations
   */
  TypedFunc(final Expr fun, final AnnList anns) {
    this.fun = fun;
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
  static TypedFunc java(final JavaMapping f) {
    return new TypedFunc(f, new AnnList());
  }
}
