package org.basex.query.expr.constr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Node constructor.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public abstract class CNode extends Arr {
  /** Static context. */
  final StaticContext sc;

  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param seqType sequence type
   * @param exprs expressions
   */
  CNode(final StaticContext sc, final InputInfo info, final SeqType seqType, final Expr... exprs) {
    super(info, seqType, exprs);
    this.sc = sc;
  }

  @Override
  public abstract ANode item(QueryContext qc, InputInfo ii) throws QueryException;

  @Override
  public boolean has(final Flag... flags) {
    return Flag.CNS.in(flags) || super.has(flags);
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
    return pref + " { " + super.toString(SEP) + " }";
  }
}
