package org.basex.query.util.parse;

import org.basex.query.expr.*;
import org.basex.query.util.hash.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.util.list.*;

/**
 * Parsed function arguments.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FuncArgs {
  /** Expressions. */
  private final ExprList exprs = new ExprList(4);
  /** Keyword-based expressions (initialized if required). */
  private QNmMap<Expr> keywords;
  /** Placeholders (initialized if required). */
  private IntList holes;

  /**
   * Constructor.
   * @param exprs initial arguments
   */
  public FuncArgs(final Expr... exprs) {
    this.exprs.add(exprs);
  }

  /**
   * Adds an expression.
   * @param expr expression to be added ({@code null} for placeholder)
   */
  public void add(final Expr expr) {
    if(expr != null) {
      exprs.add(expr);
    } else {
      if(holes == null) holes = new IntList(4);
      holes.add(holes.size() + exprs.size());
    }
  }

  /**
   * Adds a keyword expression.
   * @param qnm name of keyword
   * @param expr expression to be added ({@code null} for placeholder)
   * @return {@code true} if the entry already exists
   */
  public boolean add(final QNm qnm, final Expr expr) {
    if(keywords == null) keywords = new QNmMap<>();
    final boolean contains = keywords.contains(qnm);
    keywords.put(qnm, expr);
    return contains;
  }

  /**
   * Returns all expressions.
   * @return expressions
   */
  public Expr[] exprs() {
    return exprs.finish();
  }

  /**
   * Returns an array with the placeholder positions.
   * @return placeholders (can be {@code null})
   */
  public int[] holes() {
    return holes != null ? holes.finish() : null;
  }

  /**
   * Returns all keyword expressions.
   * @return keyword expressions (can be {@code null})
   */
  public QNmMap<Expr> keywords() {
    return keywords;
  }
}
