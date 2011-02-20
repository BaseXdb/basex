package org.basex.query.expr;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * Simple expression without arguments.
 *
 * @author BaseX Team 2005-11, BSD License
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
  public final int count(final Var v) {
    return 0;
  }

  @Override
  public boolean removable(final Var v) {
    return true;
  }

  @Override
  public Expr remove(final Var v) {
    return this;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(this);
  }
}
