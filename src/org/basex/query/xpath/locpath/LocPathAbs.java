package org.basex.query.xpath.locpath;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.item.Nod;

/**
 * Absolute Location Path Expression. This location path is evaluated from the
 * root context.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class LocPathAbs extends LocPath {
  /** Root node. */
  private Nod nodeset;
  
  @Override
  public Nod eval(final XPContext ctx) throws QueryException {
    // only evaluated once as result for absolute paths is always the same
    if(nodeset == null) {
      final Nod ns = ctx.item;
      ctx.item = new Nod(ns.data.doc(), ctx);
      nodeset = steps.eval(ctx);
      ctx.item = ns;
    }
    return nodeset;
  }
 
  @Override
  public String toString() {
    return "/" + super.toString();
  }
}
