package org.basex.query.xpath.locpath;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.values.NodeSet;

/**
 * Absolute Location Path Expression. This location path is evaluated from the
 * root context.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class LocPathAbs extends LocPath {
  /** Root node. */
  private NodeSet nodeset;
  
  @Override
  public NodeSet eval(final XPContext ctx) throws QueryException {
    // only evaluated once as result for absolute traversals is always the same
    if(nodeset == null) {
      final NodeSet ns = ctx.local;
      ctx.local = new NodeSet(new int[] { 0 }, ctx);
      nodeset = steps.eval(ctx);
      ctx.local = ns;
    }
    return nodeset;
  }
 
  @Override
  public String toString() {
    return steps.toString();
  }
}
