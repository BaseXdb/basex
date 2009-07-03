package org.basex.query.ft;

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
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class FTContainsIndex extends FTContains {
  /** Not flag. */
  private final boolean not;
  /** Current node item. */
  private FTItem ftn;
  /** Node iterator. */
  private FTIter fti;

  /**
   * Constructor.
   * @param e contains, select and optional ignore expression
   * @param fte full-text expression
   * @param n not flag
   */
  public FTContainsIndex(final Expr e, final FTExpr fte, final boolean n) {
    super(e, fte);
    not = n;
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
      found = (ftn != null && n.pre == ftn.pre) ^ not;
    }
    // reset index iterator after all nodes have been processed
    if(n == null) fti = null;

    // add entry to visualization
    if(found && ctx.ftpos != null && !not) ctx.ftpos.add(ftn.pre, ftn.all);

    ctx.fttoken = tmp;
    return Bln.get(found ? 1 : 0);
  }
}
