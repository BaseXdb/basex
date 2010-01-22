package org.basex.query.ft;

import static org.basex.query.QueryTokens.*;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Bln;
import org.basex.query.item.DBNode;
import org.basex.query.item.FTItem;
import org.basex.query.iter.FTIter;
import org.basex.query.iter.Iter;
import org.basex.util.Tokenizer;

/**
 * Sequential FTContains expression with index access.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Sebastian Gath
 */
public final class FTContainsIndex extends FTContains {
  /** Index context. */
  final IndexContext ictx;
  /** Current node item. */
  private FTItem ftn;
  /** Node iterator. */
  private FTIter fti;

  /**
   * Constructor.
   * @param e contains, select and optional ignore expression
   * @param f full-text expression
   * @param ic index context
   */
  public FTContainsIndex(final Expr e, final FTExpr f, final IndexContext ic) {
    super(e, f);
    ictx = ic;
  }

  @Override
  public Bln atomic(final QueryContext ctx) throws QueryException {
    final Iter ir = expr.iter(ctx);
    final Tokenizer tmp = ctx.fttoken;
    ctx.fttoken = ft;

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
    if(found && ctx.ftpos != null && !ictx.not) ctx.ftpos.add(ftn.pre, ftn.all);

    ctx.fttoken = tmp;
    return Bln.get(found ? 1 : 0);
  }

  @Override
  public String toString() {
    return expr + " " + CONTAINS + " " + TEXT + " " + ftexpr;
  }
}
