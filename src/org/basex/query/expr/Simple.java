package org.basex.query.expr;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.util.Var;

/**
 * Simple expression without arguments.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class Simple extends Expr {
  @Override
  @SuppressWarnings("unused")
  public Expr comp(final QueryContext ctx) throws QueryException {
    return this;
  }
  
  @Override
  public boolean usesPos(final QueryContext ctx) {
    return false;
  }

  @Override
  public int countVar(final Var v) {
    return 0;
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
