package org.basex.query.xpath.locpath;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.values.NodeSet;

/**
 * Relative Location Path Expression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class LocPathRel extends LocPath {
  @Override
  public NodeSet eval(final XPContext ctx) throws QueryException {
    return steps.eval(ctx);
  }

  @Override
  public String toString() {
    final String path = steps.toString();
    return path.length() != 0 ? path.substring(1) : path;
  }
}
