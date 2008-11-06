package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.iter.Iter;

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
    return checkCtx(ctx);
  }

  @Override
  public boolean uses(final Using use) {
    return false;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(this, NS, timer());
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
