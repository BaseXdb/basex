package org.basex.query.xpath.locpath;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.item.Nod;

/**
 * Relative Location Path Expression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class LocPathRel extends LocPath {
  @Override
  public Nod eval(final XPContext ctx) throws QueryException {
    return steps.eval(ctx);
  }
}
