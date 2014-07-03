package org.basex.query.ft;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * Sequential FTContains expression with index access.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Sebastian Gath
 */
final class FTContainsIndex extends FTContains {
  /** Current node item. */
  private FTNode ftn;
  /** Node iterator. */
  private FTIter fti;
  /** {@code not} flag. */
  private final boolean not;

  /**
   * Constructor.
   * @param info input info
   * @param expr contains, select and optional ignore expression
   * @param ftexpr full-text expression
   * @param not {@code not} flag
   */
  FTContainsIndex(final InputInfo info, final Expr expr, final FTExpr ftexpr, final boolean not) {
    super(expr, ftexpr, info);
    this.not = not;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter ir = expr.iter(qc);
    final FTLexer tmp = qc.ftToken;
    qc.ftToken = lex;

    // create index iterator
    if(fti == null) {
      fti = ftexpr.iter(qc);
      ftn = fti.next();
    }

    // find next relevant index entry
    boolean found = false;
    DBNode n = null;
    while(!found && (n = (DBNode) ir.next()) != null) {
      // find entry with pre value equal to or larger than current node
      while(ftn != null && n.pre > ftn.pre) ftn = fti.next();
      found = (ftn != null && n.pre == ftn.pre) ^ not;
    }
    // reset index iterator after all nodes have been processed
    if(n == null) fti = null;

    // cache entry for visualizations or ft:mark/ft:extract
    if(found && qc.ftPosData != null && !not) {
      qc.ftPosData.add(ftn.data, ftn.pre, ftn.all);
    }

    qc.ftToken = tmp;
    return Bln.get(found ? 1 : 0);
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return copyType(new FTContainsIndex(info, expr.copy(qc, scp, vs),
        ftexpr.copy(qc, scp, vs), not));
  }
}
