package org.basex.query.expr;

import java.io.IOException;
import java.util.Arrays;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FunItem;
import org.basex.query.item.FunType;
import org.basex.query.item.SeqType;
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
  public Expr comp(final QueryContext ctx) throws QueryException {
    final Expr app = super.comp(ctx);

    final SeqType[] at = new SeqType[vars.length];
    Arrays.fill(at, SeqType.ITEM_ZM);
    return new FunItem(vars, app, FunType.get(at, app.type()));
  }

  @Override
  public String toString() {
    return expr.toString();
  }
}
