package org.basex.query.util;

import org.basex.query.expr.Cast;
import org.basex.query.expr.Expr;
import org.basex.query.func.FunJava;
import org.basex.query.item.FunType;
import org.basex.query.item.SeqType;

/**
 * Wrapper that provides types for function expressions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Leo Woerteler
 */
public final class TypedFunc {
  /** Function expression. */
  public final Expr fun;
  /** Function type. */
  public final FunType type;

  /**
   * Constructor.
   * @param f function expression
   * @param ret return type
   * @param args argument types
   */
  public TypedFunc(final Expr f, final SeqType ret, final SeqType... args) {
    this(f, FunType.get(args, ret));
  }

  /**
   * Constructor.
   * @param f function expression
   * @param ft function type
   */
  public TypedFunc(final Expr f, final FunType ft) {
    fun = f;
    type = ft;
  }

  /**
   * Gets the function's return type.
   * @return return type
   */
  public SeqType ret() {
    return type.ret;
  }

  /**
   * Creates a type constructor function.
   * @param cast cast expression
   * @param to type to cast to
   * @return typed function
   */
  public static TypedFunc constr(final Cast cast, final SeqType to) {
    return new TypedFunc(cast, to, SeqType.AAT_ZO);
  }

  /**
   * Creates a type constructor function.
   * @param f java function
   * @return typed function
   */
  public static TypedFunc java(final FunJava f) {
    return new TypedFunc(f, FunType.arity(f.expr.length));
  }
}
