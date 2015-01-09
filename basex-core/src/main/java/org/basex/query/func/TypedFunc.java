package org.basex.query.func;

import org.basex.query.expr.*;
import org.basex.query.util.*;

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
  public final Ann ann;

  /**
   * Constructor.
   * @param fun function expression
   * @param ann annotations
   */
  TypedFunc(final Expr fun, final Ann ann) {
    this.fun = fun;
    this.ann = ann;
  }

  /**
   * Creates a type constructor function.
   * @param cast cast expression
   * @return typed function
   */
  static TypedFunc constr(final Cast cast) {
    return new TypedFunc(cast, new Ann());
  }

  /**
   * Creates a type constructor function.
   * @param f java function
   * @return typed function
   */
  static TypedFunc java(final JavaMapping f) {
    return new TypedFunc(f, new Ann());
  }
}
