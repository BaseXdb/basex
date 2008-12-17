package org.basex.query.xpath.expr;

import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.item.Bln;

/**
 * FTBool expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class FTBool extends FTArrayExpr {
  /** Fulltext option. */
  public Bln value;
  
  /**
   * Constructor.
   * @param b boolean value
   */
  public FTBool(final boolean b) {
    super();
    value = Bln.get(b);
  }

  @Override
  public Bln eval(final XPContext ctx) {
    return value;
  }
}
