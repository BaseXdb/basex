package org.basex.query.expr;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * Function call.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class PartFunApp extends Single {

  /** Free variables. */
  final Var[] vars;

  /**
   * Function constructor for dynamic calls.
   * @param ii input info
   * @param call function expression
   * @param arg arguments
   */
  public PartFunApp(final InputInfo ii, final Expr call, final Var[] arg) {
    super(ii, call);
    vars = arg;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    for(Var v : vars) v.plan(ser);
    expr.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return expr.toString();
  }
}
