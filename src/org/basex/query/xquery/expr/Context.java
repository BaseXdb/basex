package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Err;

/**
 * Context Item.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Context extends Expr {
  @Override
  public Expr comp(final XQContext ctx) {
    return this;
  }
  
  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    Item it = ctx.item;
    if(it == null) Err.or(XPNOCTX, this);
    return it.iter();
  }

  @Override
  public boolean uses(final Using use) {
    return false;
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.emptyElement(this);
  }

  @Override
  public String color() {
    return "FFCC66";
  }

  @Override
  public String toString() {
    return ".";
  }
}
