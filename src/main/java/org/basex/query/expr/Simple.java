package org.basex.query.expr;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;

/**
 * Simple expression without arguments.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class Simple extends Expr {
  @Override
  public Expr comp(final QueryContext ctx) {
    return this;
  }

  @Override
  public boolean uses(final Use u, final QueryContext ctx) {
    return u == Use.ELM;
  }

  @Override
  public String color() {
    return "FFFF66";
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(this);
  }
}
