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
 * @author BaseX Team 2005-13, BSD License
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
   * @param ii input info
   * @param e contains, select and optional ignore expression
   * @param f full-text expression
   * @param nt {@code not} flag
   */
  FTContainsIndex(final InputInfo ii, final Expr e, final FTExpr f, final boolean nt) {
    super(e, f, ii);
    not = nt;
  }

  @Override
  public Bln item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final Iter ir = expr.iter(ctx);
    final FTLexer tmp = ctx.ftToken;
    ctx.ftToken = lex;

    // create index iterator
    if(fti == null) {
      fti = ftexpr.iter(ctx);
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
    if(found && ctx.ftPosData != null && !not) {
      ctx.ftPosData.add(ftn.data, ftn.pre, ftn.all);
    }

    ctx.ftToken = tmp;
    return Bln.get(found ? 1 : 0);
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    return copyType(new FTContainsIndex(info, expr.copy(ctx, scp, vs),
        ftexpr.copy(ctx, scp, vs), not));
  }
}
