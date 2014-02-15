package org.basex.query.util;

import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.type.*;

/**
 * Wrapper that provides types for function expressions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class TypedFunc {
  /** Function expression. */
  public final Expr fun;
  /** Function type. */
  public final FuncType type;
  /** Annotations. */
  public final Ann ann;

  /**
   * Constructor.
   * @param f function expression
   * @param a annotations
   * @param ret return type
   * @param args argument types
   */
  private TypedFunc(final Expr f, final Ann a, final SeqType ret, final SeqType... args) {
    this(f, a, FuncType.get(ret, args));
  }

  /**
   * Constructor.
   * @param f function expression
   * @param a annotations
   * @param ft function type
   */
  public TypedFunc(final Expr f, final Ann a, final FuncType ft) {
    fun = f;
    type = ft;
    ann = a;
  }

  /**
   * Creates a type constructor function.
   * @param cast cast expression
   * @param to type to cast to
   * @return typed function
   */
  public static TypedFunc constr(final Cast cast, final SeqType to) {
    return new TypedFunc(cast, new Ann(), to, SeqType.AAT_ZO);
  }


  /**
   * Creates a type constructor function.
   * @param f java function
   * @return typed function
   */
  public static TypedFunc java(final JavaMapping f) {
    return new TypedFunc(f, new Ann(), FuncType.arity(f.expr.length));
  }
}
