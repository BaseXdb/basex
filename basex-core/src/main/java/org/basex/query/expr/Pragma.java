package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Abstract pragma expression.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public abstract class Pragma extends ExprInfo {
  /** QName. */
  final QNm name;
  /** Pragma value. */
  final byte[] value;

  /**
   * Constructor.
   * @param name name of pragma
   * @param value optional value
   */
  Pragma(final QNm name, final byte[] value) {
    this.name = name;
    this.value = value;
  }

  /**
   * Initializes the pragma expression.
   * @param qc query context
   * @param info input info
   * @return state before pragmas was set
   * @throws QueryException query exception
   */
  abstract Object init(QueryContext qc, InputInfo info) throws QueryException;

  /**
   * Finalizes the pragma expression.
   * @param qc query context
   * @param state state before pragmas was set
   */
  abstract void finish(QueryContext qc, Object state);

  /**
   * Traverses this expression, notifying the visitor of declared and used variables,
   * and checking the tree for other recursive properties.
   * @param visitor visitor
   */
  abstract void accept(ASTVisitor visitor);

  /**
   * Indicates if an expression has one of the specified compiler properties.
   * @param flags flag to be checked
   * @return result of check
   * @see Expr#has(Flag...)
   */
  public abstract boolean has(Flag... flags);

  /**
   * Creates a copy of this pragma.
   * @return copy
   */
  public abstract Pragma copy();

  /**
   * {@inheritDoc}
   * Must be overwritten by implementing class.
   */
  @Override
  public boolean equals(final Object obj) {
    if(!(obj instanceof Pragma)) return false;
    final Pragma p = (Pragma) obj;
    return name.eq(p.name) && Token.eq(value, p.value);
  }

  @Override
  public final void plan(final QueryPlan plan) {
    plan.add(plan.create(this, VALUEE, value), name);
  }

  @Override
  public final void plan(final QueryString qs) {
    qs.token(PRAGMA).token(name).token(value).token(PRAGMA2);
  }
}
