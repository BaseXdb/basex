package org.basex.query.xquery.expr;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.util.Var;

/**
 * Simple expression without arguments.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class Simple extends Expr {
  @Override
  public final Expr comp(final XQContext ctx) {
    return this;
  }
  
  @Override
  public boolean usesPos() {
    return false;
  }

  @Override
  public final boolean usesVar(final Var v) {
    return false;
  }
  
  @Override
  public final String color() {
    return "FFFF66";
  }
  
  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(this);
  }
}
