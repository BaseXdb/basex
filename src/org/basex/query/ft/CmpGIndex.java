package org.basex.query.ft;

import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Arr;
import org.basex.query.expr.Expr;
import org.basex.query.item.Bln;
import org.basex.query.item.DBNode;
import org.basex.query.iter.Iter;

/**
 * Sequential equality expression with index access.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class CmpGIndex extends Arr {
  /** Index context. */
  final IndexContext ictx;
  /** Current node item. */
  private DBNode ftn;
  /** Node iterator. */
  private Iter fti;

  /**
   * Constructor.
   * @param e contains, select and optional ignore expression
   * @param f full-text expression
   * @param ic index context
   */
  public CmpGIndex(final Expr e, final Expr f, final IndexContext ic) {
    super(e, f);
    ictx = ic;
  }

  @Override
  public Bln atomic(final QueryContext ctx) throws QueryException {
    final Iter ir = expr[0].iter(ctx);

    // create index iterator
    if(fti == null) {
      fti = expr[1].iter(ctx);
      ftn = (DBNode) fti.next();
    }

    // find next relevant index entry
    boolean found = false;
    DBNode n = null;
    while(!found && (n = (DBNode) ir.next()) != null) {
      // find entry with pre value equal to or larger than current node
      while(ftn != null && n.pre > ftn.pre) ftn = (DBNode) fti.next();
      found = (ftn != null && n.pre == ftn.pre) ^ ictx.not;
    }
    // reset index iterator after all nodes have been processed
    if(n == null) fti = null;

    return Bln.get(found ? 1 : 0);
  }

  @Override
  public String toString() {
    return toString("=");
  }
}
