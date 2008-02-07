package org.basex.query.xquery.expr;

import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Scoring;

/**
 * Or expression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Or extends Arr {
  /**
   * Constructor.
   * @param e expression list
   */
  public Or(final Expr[] e) {
    super(e);
  }
  
  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    double d = 0;
    boolean found = false;
    for(final Expr e : expr) {
      final Item it = ctx.iter(e).ebv();
      if(it.bool()) {
        if(it.score() == 0) return Bln.TRUE.iter();
        d = Scoring.or(d, it.score());
        found = true;
      }
    }
    return (!found ? Bln.FALSE : new Bln(found, d)).iter();
  }

  @Override
  public String toString() {
    return toString(" or ");
  }
}
