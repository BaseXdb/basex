package org.basex.query.ft;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.ft.*;

/**
 * Sequential FTContains expression with index access.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Sebastian Gath
 */
final class FTContainsIndex extends FTContains {
  /** Index context. */
  private final IndexContext ictx;
  /** Current node item. */
  private FTNode ftn;
  /** Node iterator. */
  private FTIter fti;

  /**
   * Constructor.
   * @param ii input info
   * @param e contains, select and optional ignore expression
   * @param f full-text expression
   * @param ic index context
   */
  FTContainsIndex(final InputInfo ii, final Expr e, final FTExpr f,
      final IndexContext ic) {
    super(e, f, ii);
    ictx = ic;
  }

  @Override
  public Bln item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final Iter ir = expr.iter(ctx);
    final FTLexer tmp = ctx.fttoken;
    ctx.fttoken = lex;

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
      found = (ftn != null && n.pre == ftn.pre) ^ ictx.not;
    }
    // reset index iterator after all nodes have been processed
    if(n == null) fti = null;

    // add entry to visualization
    if(found && ctx.ftpos != null && !ictx.not) {
      ctx.ftpos.add(ftn.data, ftn.pre, ftn.all);
    }

    ctx.fttoken = tmp;
    return Bln.get(found ? 1 : 0);
  }

  @Override
  public String toString() {
    return expr + " " + CONTAINS + ' ' + TEXT + ' ' + ftexpr;
  }
}
