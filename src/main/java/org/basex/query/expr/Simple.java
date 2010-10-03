package org.basex.query.expr;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * Simple expression without arguments.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class Simple extends ParseExpr {
  /**
   * Constructor.
   * @param ii input info
   */
  public Simple(final InputInfo ii) {
    super(ii);
  }

  @Override
  public Expr comp(final QueryContext ctx) {
    return this;
  }

  @Override
  public boolean uses(final Use u) {
    return false;
  }

  @Override
  public final boolean uses(final Var v) {
    return false;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(this);
  }
}
