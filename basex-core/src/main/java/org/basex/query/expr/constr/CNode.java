package org.basex.query.expr.constr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Node constructor.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public abstract class CNode extends Arr {
  /** Static context. */
  final StaticContext sc;
  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param name name
   */
  CNode(final StaticContext sc, final InputInfo info, final Expr... name) {
    super(info, name);
    this.sc = sc;
    size = 1;
  }

  @Override
  public abstract ANode item(final QueryContext qc, final InputInfo ii) throws QueryException;

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.CNS || super.has(flag);
  }

  /**
   * Returns a string info for the expression.
   * @param pref info prefix
   * @return string
   */
  static String info(final String pref) {
    return pref + " constructor";
  }

  @Override
  protected String toString(final String pref) {
    return pref + " { " + (exprs.length == 0 ? "()" : super.toString(SEP)) + " }";
  }
}
