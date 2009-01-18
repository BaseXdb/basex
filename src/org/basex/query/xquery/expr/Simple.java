package org.basex.query.xquery.expr;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.util.Var;

/**
 * Simple expression without arguments.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class Simple extends Expr {
  @Override
  @SuppressWarnings("unused")
  public Expr comp(final XQContext ctx) throws XQException {
    return this;
  }
  
  @Override
  public boolean usesPos(final XQContext ctx) {
    return false;
  }

  @Override
  public boolean usesVar(final Var v) {
    return false;
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
