package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.Expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Abstract pragma expression.
 *
 * @author BaseX Team 2005-17, BSD License
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

  @Override
  public final void plan(final FElem plan) {
    addPlan(plan, planElem(VAL, value), name);
  }

  /**
   * Initializes the pragma expression.
   * @param qc query context
   * @param info input info
   * @return cached information
   * @throws QueryException query exception
   */
  abstract Object init(QueryContext qc, InputInfo info) throws QueryException;

  /**
   * Finalizes the pragma expression.
   * @param qc query context
   * @param cache cached information
   */
  abstract void finish(QueryContext qc, Object cache);

  /**
   * Indicates if an expression has the specified compiler property.
   * @param flag flag to be checked
   * @return result of check
   * @see Expr#has
   */
  public abstract boolean has(Flag flag);

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
  public final String toString() {
    final TokenBuilder tb = new TokenBuilder(PRAGMA + ' ' + name + ' ');
    if(value.length != 0) tb.add(value).add(' ');
    return tb.add(PRAGMA2).toString();
  }
}
