package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;

import java.io.IOException;

import org.basex.data.Serializer;
import org.basex.query.item.QNm;
import org.basex.query.util.Var;
import org.basex.util.Array;
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
  public PartFunApp(final InputInfo ii, final Expr call, final Expr[] arg) {
    super(ii, call);
    vars = findVars(arg);
  }

  /**
   * Binds all place-holders by variable references and returns the bound
   * variables.
   * @param arg argument array
   * @return bound variables
   */
  private Var[] findVars(final Expr[] arg) {
    Var[] vs = {};
    for(int i = 0; i < arg.length; i++) {
      if(arg[i] == null) {
        final Var v = new Var(input, new QNm(concat(VAR, token(i))));
        Array.add(vs, v);
        arg[i] = new VarRef(input, v);
      }
    }
    return vs;
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
